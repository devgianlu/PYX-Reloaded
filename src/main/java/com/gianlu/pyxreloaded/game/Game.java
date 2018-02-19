package com.gianlu.pyxreloaded.game;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.Utils;
import com.gianlu.pyxreloaded.cardcast.CardcastDeck;
import com.gianlu.pyxreloaded.cardcast.CardcastService;
import com.gianlu.pyxreloaded.cardcast.FailedLoadingSomeCardcastDecks;
import com.gianlu.pyxreloaded.cards.*;
import com.gianlu.pyxreloaded.data.EventWrapper;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.QueuedMessage;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.singletons.ConnectedUsers;
import com.gianlu.pyxreloaded.singletons.LoadedCards;
import com.gianlu.pyxreloaded.singletons.Preferences;
import com.gianlu.pyxreloaded.task.SafeTimerTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Game {
    private static final Logger logger = Logger.getLogger(Game.class);

    /**
     * The minimum number of black cards that must be added to a game for it to be able to start.
     */
    private final int MINIMUM_BLACK_CARDS;

    /**
     * The minimum number of white cards per player limit slots that must be added to a game for it to
     * be able to start.
     * <p>
     * We need 20 * maxPlayers cards. This allows black cards up to "draw 9" to work correctly.
     */
    private final int MINIMUM_WHITE_CARDS_PER_PLAYER;

    /**
     * Time, in milliseconds, to delay before starting a new round.
     */
    private final int ROUND_INTERMISSION;

    /**
     * Duration, in milliseconds, for the minimum timeout a player has to choose a card to play.
     * Minimum 10 seconds.
     */
    private final int PLAY_TIMEOUT_BASE;

    /**
     * Duration, in milliseconds, for the additional timeout a player has to choose a card to play,
     * for each card that must be played. For example, on a PICK 2 card, two times this amount of
     * time is added to {@code PLAY_TIMEOUT_BASE}.
     */
    private final int PLAY_TIMEOUT_PER_CARD;

    /**
     * Duration, in milliseconds, for the minimum timeout a judge has to choose a winner.
     * Minimum combined of this and 2 * {@code JUDGE_TIMEOUT_PER_CARD} is 10 seconds.
     */
    private final int JUDGE_TIMEOUT_BASE;

    /**
     * Duration, in milliseconds, for the additional timeout a judge has to choose a winning card,
     * for each additional card that was played in the round. For example, on a PICK 2 card with
     * 3 non-judge players, 6 times this value is added to {@code JUDGE_TIMEOUT_BASE}.
     */
    private final int JUDGE_TIMEOUT_PER_CARD;
    private final int MAX_SKIPS_BEFORE_KICK;

    private final int id;
    private final List<Player> players = Collections.synchronizedList(new ArrayList<Player>(10));
    private final List<Player> roundPlayers = Collections.synchronizedList(new ArrayList<Player>(9));
    private final PlayerPlayedCardsTracker playedCards = new PlayerPlayedCardsTracker();
    private final List<User> spectators = Collections.synchronizedList(new ArrayList<User>(10));
    private final ConnectedUsers connectedUsers;
    private final GameManager gameManager;
    private final GameOptions options;
    private final Object roundTimerLock = new Object();
    private final Object judgeLock = new Object();
    private final Object blackCardLock = new Object();
    private final LoadedCards loadedCards;
    private final ScheduledThreadPoolExecutor globalTimer;
    private final CardcastService cardcastService;
    private final Set<User> likes = Collections.synchronizedSet(new HashSet<>());
    private final Set<User> dislikes = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, SuggestedGameOptions> suggestedGameOptions = new ConcurrentHashMap<>();
    private Player host;
    private BlackDeck blackDeck;
    private BlackCard blackCard;
    private WhiteDeck whiteDeck;
    private Consts.GameState state;
    private int judgeIndex = 0;
    private volatile ScheduledFuture<?> lastScheduledFuture;


    /**
     * Create a new game.
     *
     * @param id             The game's ID.
     * @param connectedUsers The user manager, for broadcasting messages.
     * @param gameManager    The game manager, for broadcasting game list refresh notices and destroying this game
     *                       when everybody leaves.
     * @param globalTimer    The global timer on which to schedule tasks.
     */
    public Game(int id, GameOptions options, ConnectedUsers connectedUsers, GameManager gameManager, LoadedCards loadedCards, ScheduledThreadPoolExecutor globalTimer, Preferences preferences, CardcastService cardcastService) {
        this.id = id;
        this.connectedUsers = connectedUsers;
        this.gameManager = gameManager;
        this.loadedCards = loadedCards;
        this.globalTimer = globalTimer;
        this.options = options;
        this.cardcastService = cardcastService;
        this.state = Consts.GameState.LOBBY;

        this.MAX_SKIPS_BEFORE_KICK = preferences.getInt("maxSkipsBeforeKick", 2);
        this.ROUND_INTERMISSION = preferences.getInt("roundIntermission", 8) * 1000;
        this.MINIMUM_BLACK_CARDS = preferences.getInt("minBlackCards", 50);
        this.MINIMUM_WHITE_CARDS_PER_PLAYER = preferences.getInt("minWhiteCardsPerPlayer", 20);
        this.PLAY_TIMEOUT_BASE = preferences.getInt("playTimeoutBase", 45) * 1000;
        this.JUDGE_TIMEOUT_BASE = preferences.getInt("judgeTimeoutBase", 40) * 1000;
        this.PLAY_TIMEOUT_PER_CARD = preferences.getInt("playTimeoutPerCard", 15) * 1000;
        this.JUDGE_TIMEOUT_PER_CARD = preferences.getInt("judgeTimeoutPerCard", 7) * 1000;
    }

    private static JsonArray getWhiteCardsDataJson(List<WhiteCard> cards) {
        JsonArray json = new JsonArray(cards.size());
        for (WhiteCard card : cards) json.add(card.getClientDataJson().obj());
        return json;
    }

    /**
     * Count valid users and also remove invalid ones
     *
     * @param users The users to count
     * @return The number of valid users
     */
    private static int countValidUsers(Iterable<User> users) {
        int count = 0;
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isValid()) count++;
            else iterator.remove();
        }
        return count;
    }

    /**
     * Exclusive toggle, possible combinations: one, other or neither.
     *
     * @param one   One list
     * @param other The other list
     * @param user  The user who submitted the toggle
     */
    private static boolean toggleLikeDislike(Set<User> one, Set<User> other, User user) {
        if (!one.contains(user)) {
            if (other.contains(user)) other.remove(user);
            one.add(user);
            return true;
        } else {
            one.remove(user);
            return false;
        }
    }

    public Map<String, SuggestedGameOptions> getSuggestedGameOptions() {
        return suggestedGameOptions;
    }

    /**
     * Add a player to the game.
     *
     * @param user Player to add to this game.
     */
    public void addPlayer(User user) throws BaseCahHandler.CahException {
        logger.info(String.format("%s joined game %d.", user.toString(), id));

        synchronized (players) {
            if (players.size() >= options.playerLimit)
                throw new BaseCahHandler.CahException(Consts.ErrorCode.GAME_FULL);

            user.joinGame(this);
            Player player = new Player(user);
            players.add(player);
            if (host == null) host = player;
        }

        EventWrapper ev = new EventWrapper(this, Consts.Event.GAME_PLAYER_JOIN);
        ev.add(Consts.GeneralKeys.NICKNAME, user.getNickname());
        broadcastToPlayers(QueuedMessage.MessageType.GAME_PLAYER_EVENT, ev);
    }

    /**
     * Get the number of valid likes
     *
     * @return Valid like count
     */
    public int getLikes() {
        synchronized (likes) {
            return countValidUsers(likes);
        }
    }

    /**
     * Get the number of valid dislikes
     *
     * @return Valid dislike count
     */
    public int getDislikes() {
        synchronized (dislikes) {
            return countValidUsers(dislikes);
        }
    }

    /**
     * Summary for like/dislike stuff
     *
     * @param user the subjected user
     * @return A json wrapper with the data
     */
    public JsonWrapper getLikesInfoJson(User user) {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GameInfoData.I_LIKE, userLikes(user));
        obj.add(Consts.GameInfoData.I_DISLIKE, userDislikes(user));
        obj.add(Consts.GameInfoData.LIKES, getLikes());
        obj.add(Consts.GameInfoData.DISLIKES, getDislikes());
        return obj;
    }

    /**
     * Does the user disliked the game?
     *
     * @param user The subject user
     * @return Whether the user dislikes this game
     */
    private boolean userDislikes(User user) {
        return dislikes.contains(user);
    }

    /**
     * Does the user liked the game?
     *
     * @param user The subject user
     * @return Whether the user likes this game
     */
    private boolean userLikes(User user) {
        return likes.contains(user);
    }

    /**
     * Toggle the like status
     *
     * @param user The user who submitted the action
     */
    public void toggleLikeGame(User user) {
        if (toggleLikeDislike(likes, dislikes, user)) {
            EventWrapper obj = new EventWrapper(this, Consts.Event.GAME_LIKE);
            obj.add(Consts.GeneralKeys.NICKNAME, user.getNickname());
            broadcastToPlayers(QueuedMessage.MessageType.GAME_EVENT, obj);
        }
    }

    /**
     * Toggle the dislike status
     *
     * @param user The user who submitted the action
     */
    public void toggleDislikeGame(User user) {
        if (toggleLikeDislike(dislikes, likes, user)) {
            EventWrapper obj = new EventWrapper(this, Consts.Event.GAME_DISLIKE);
            obj.add(Consts.GeneralKeys.NICKNAME, user.getNickname());
            broadcastToPlayers(QueuedMessage.MessageType.GAME_EVENT, obj);
        }
    }

    /**
     * Is the given password correct?
     *
     * @param userPassword A given string
     * @return Whether the password is correct
     */
    public boolean isPasswordCorrect(String userPassword) {
        return getPassword() == null || getPassword().isEmpty() || Objects.equals(userPassword, getPassword());
    }

    /**
     * Remove a player from the game.
     *
     * @param user Player to remove from the game.
     */
    public void removePlayer(User user) {
        logger.info(String.format("Removing %s from game %d.", user.toString(), id));

        Player player = getPlayerForUser(user);
        if (player != null) {
            // If they played this round, remove card from played card list
            List<WhiteCard> cards = playedCards.remove(player);
            if (cards != null && cards.size() > 0) {
                for (WhiteCard card : cards) whiteDeck.discard(card);
            }

            // If they are to play this round, remove them from that list
            if (roundPlayers.remove(player)) {
                if (shouldStartJudging()) judgingState();
            }

            // If they have a hand, return it to discard pile.
            if (player.hand.size() > 0) {
                for (WhiteCard card : player.hand) whiteDeck.discard(card);
            }

            boolean wasJudge = getJudge() == player;

            // Actually remove the user
            players.remove(player);
            user.leaveGame(this);

            // If they are the host, choose another one
            if (host == player) {
                if (!players.isEmpty()) host = players.get(0);
                else host = null;

                notifyPlayerInfoChange(host);
            }

            boolean willStop = players.size() < 3 && state != Consts.GameState.LOBBY;
            // If they was judge, return all played cards to hand, and move to next judge.
            if (wasJudge && (state == Consts.GameState.PLAYING || state == Consts.GameState.JUDGING)) {
                EventWrapper ev = new EventWrapper(this, Consts.Event.GAME_JUDGE_LEFT);
                ev.add(Consts.OngoingGameData.INTERMISSION, ROUND_INTERMISSION);
                ev.add(Consts.OngoingGameData.WILL_STOP, willStop);
                broadcastToPlayers(QueuedMessage.MessageType.GAME_EVENT, ev);

                returnCardsToHand();
                judgeIndex--; // startNextRound will advance it again.
            } else if (players.indexOf(player) < judgeIndex) {
                judgeIndex--; // If they aren't judge but are earlier in judging order, fix the judge index.
            }

            EventWrapper ev = new EventWrapper(this, Consts.Event.GAME_PLAYER_LEAVE);
            ev.add(Consts.GeneralKeys.NICKNAME, user.getNickname());
            ev.add(Consts.OngoingGameData.WILL_STOP, willStop);
            broadcastToPlayers(QueuedMessage.MessageType.GAME_PLAYER_EVENT, ev);

            // Put game in lobby state as there aren't enough players
            if (willStop) {
                resetState(true);
            } else if (wasJudge) { // Start a new round if they are the judge
                synchronized (roundTimerLock) {
                    rescheduleTimer(new SafeTimerTask() {
                        @Override
                        public void process() {
                            startNextRound();
                        }
                    }, ROUND_INTERMISSION);
                }
            }

            // Destroy the game if it's empty
            if (players.size() == 0) gameManager.destroyGame(id);
        }
    }

    /**
     * Add a spectator to the game.
     *
     * @param user Spectator to add to this game.
     */
    public void addSpectator(User user) throws BaseCahHandler.CahException {
        logger.info(String.format("%s joined game %d as a spectator.", user.toString(), id));
        synchronized (spectators) {
            if (spectators.size() >= options.spectatorLimit)
                throw new BaseCahHandler.CahException(Consts.ErrorCode.GAME_FULL);

            user.joinGame(this);
            spectators.add(user);
        }

        EventWrapper ev = new EventWrapper(this, Consts.Event.GAME_SPECTATOR_JOIN);
        ev.add(Consts.GeneralKeys.NICKNAME, user.getNickname());
        broadcastToPlayers(QueuedMessage.MessageType.GAME_PLAYER_EVENT, ev);
    }

    /**
     * Remove a spectator from the game.
     *
     * @param user Spectator to remove from the game.
     */
    public void removeSpectator(User user) {
        logger.info(String.format("Removing spectator %s from game %d.", user.toString(), id));
        synchronized (spectators) {
            if (!spectators.remove(user)) return;
            user.leaveGame(this);
        }

        EventWrapper ev = new EventWrapper(this, Consts.Event.GAME_SPECTATOR_LEAVE);
        ev.add(Consts.GeneralKeys.NICKNAME, user.getNickname());
        broadcastToPlayers(QueuedMessage.MessageType.GAME_PLAYER_EVENT, ev);
    }

    /**
     * Return all played cards to their respective player's hand.
     */
    private void returnCardsToHand() {
        synchronized (playedCards) {
            for (Player player : playedCards.playedPlayers()) {
                List<WhiteCard> cards = playedCards.getCards(player);
                if (cards != null) {
                    player.hand.addAll(cards);
                    sendCardsToPlayer(player, cards, false);
                }
            }

            playedCards.clear();
        }
    }

    /**
     * Broadcast a message to all players in this game.
     *
     * @param type Type of message to broadcast. This determines the order the messages are returned by
     *             priority.
     * @param ev   Message data to broadcast.
     */
    public void broadcastToPlayers(QueuedMessage.MessageType type, EventWrapper ev) {
        connectedUsers.broadcastToList(playersToUsers(), type, ev);
    }

    /**
     * Sends updated player information about a specific player to all players in the game.
     *
     * @param player The player whose information has been changed.
     */
    private void notifyPlayerInfoChange(Player player) {
        if (player == null) return;
        EventWrapper ev = new EventWrapper(this, Consts.Event.GAME_PLAYER_INFO_CHANGE);
        ev.add(Consts.GamePlayerInfo.INFO, getPlayerInfoJson(player));
        broadcastToPlayers(QueuedMessage.MessageType.GAME_PLAYER_EVENT, ev);
    }

    /**
     * Sends updated game information to all players in the game.
     */
    private void notifyGameOptionsChanged() {
        EventWrapper ev = new EventWrapper(this, Consts.Event.GAME_OPTIONS_CHANGED);
        ev.add(Consts.GameOptionsData.OPTIONS, options.toJson(true));
        broadcastToPlayers(QueuedMessage.MessageType.GAME_EVENT, ev);
    }

    /**
     * @return The game's current state.
     */
    public Consts.GameState getState() {
        return state;
    }

    /**
     * @return The user who is the host of this game.
     */
    @Nullable
    public User getHost() {
        if (host == null) return null;
        return host.getUser();
    }

    /**
     * @return All users in this game.
     */
    public List<User> getUsers() {
        return playersToUsers();
    }

    /**
     * @return This game's ID.
     */
    public int getId() {
        return id;
    }

    /**
     * @return This game password
     */
    public String getPassword() {
        return options.password;
    }

    /**
     * Update game options
     *
     * @param newOptions The new options
     */
    public void updateGameSettings(GameOptions newOptions) {
        this.options.update(newOptions);
        notifyGameOptionsChanged();
    }

    /**
     * Suggest a change in the game options
     *
     * @param newOptions The suggested options
     */
    public void suggestGameOptionsModification(SuggestedGameOptions newOptions) throws BaseCahHandler.CahException {
        if (this.options.equals(newOptions) || getHost() == null) return;

        synchronized (suggestedGameOptions) {
            for (SuggestedGameOptions options : suggestedGameOptions.values()) {
                if (options.getSuggester() == newOptions.getSuggester())
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.ALREADY_SUGGESTED);
            }
        }

        String id = Utils.generateAlphanumericString(5);
        suggestedGameOptions.put(id, newOptions);

        EventWrapper obj = new EventWrapper(this, Consts.Event.GAME_OPTIONS_MODIFICATION_SUGGESTED);
        obj.add(Consts.GameSuggestedOptionsData.OPTIONS, newOptions.toJson(id, true));
        getHost().enqueueMessage(new QueuedMessage(QueuedMessage.MessageType.GAME_EVENT, obj));
    }

    /**
     * Apply the suggested game options
     *
     * @param id Suggested game options id
     */
    public void applySuggestedOptions(String id) throws BaseCahHandler.CahException {
        SuggestedGameOptions options = suggestedGameOptions.remove(id);
        if (options == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.INVALID_SUGGESTED_OPTIONS_ID);

        updateGameSettings(options);

        if (getPlayerForUser(options.getSuggester()) != null) {
            options.getSuggester().enqueueMessage(new QueuedMessage(QueuedMessage.MessageType.GAME_PLAYER_EVENT,
                    new EventWrapper(this, Consts.Event.GAME_ACCEPTED_SUGGESTED_OPTIONS)));
        }
    }

    /**
     * Decline the suggested game options
     *
     * @param id Suggested game options id
     */
    public void declineSuggestedOptions(String id) {
        SuggestedGameOptions options = suggestedGameOptions.remove(id);
        if (options != null) {
            if (getPlayerForUser(options.getSuggester()) != null) {
                options.getSuggester().enqueueMessage(new QueuedMessage(QueuedMessage.MessageType.GAME_PLAYER_EVENT,
                        new EventWrapper(this, Consts.Event.GAME_DECLINED_SUGGESTED_OPTIONS)));
            }
        }
    }

    /**
     * @return The Cardcast deck codes in this game
     */
    public Set<String> getCardcastDeckCodes() {
        return options.cardcastSetCodes;
    }

    /**
     * Returns general information about the game
     *
     * @param user            The user who submitted the action
     * @param includePassword Whether to include the password in the response, should be given only to game members
     * @return A summary of the game information
     */
    @Nullable
    public JsonWrapper getInfoJson(@Nullable User user, boolean includePassword) {
        // This is probably happening because the game ceases to exist in the middle of getting the
        // game list. Just return nothing.
        if (host == null) return null;

        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GeneralKeys.GAME_ID, id);
        obj.add(Consts.GameInfoData.LIKES, getLikes());
        obj.add(Consts.GameInfoData.DISLIKES, getDislikes());
        obj.add(Consts.GameInfoData.HOST, host.getUser().getNickname());
        obj.add(Consts.GeneralGameData.STATE, state.toString());
        obj.add(Consts.GameOptionsData.OPTIONS, options.toJson(includePassword));
        obj.add(Consts.GameInfoData.HAS_PASSWORD, options.password != null && !options.password.isEmpty());

        if (user != null) {
            obj.add(Consts.GameInfoData.I_LIKE, userLikes(user));
            obj.add(Consts.GameInfoData.I_DISLIKE, userDislikes(user));
        }

        JsonArray playerNames = new JsonArray();
        for (Player player : players.toArray(new Player[players.size()]))
            playerNames.add(player.getUser().getNickname());
        obj.add(Consts.GameInfoData.PLAYERS, playerNames);

        JsonArray spectatorNames = new JsonArray();
        for (User spectator : spectators.toArray(new User[spectators.size()]))
            spectatorNames.add(spectator.getNickname());
        obj.add(Consts.GameInfoData.SPECTATORS, spectatorNames);

        return obj;
    }

    /**
     * @return All players' info
     */
    public JsonElement getAllPlayersInfoJson() {
        JsonArray json = new JsonArray(players.size());
        for (Player player : players.toArray(new Player[players.size()]))
            json.add(getPlayerInfoJson(player).obj());

        return json;
    }

    /**
     * @return All the players in the game
     */
    public final List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * @param player The given player
     * @return Info about a single player
     */
    @NotNull
    public JsonWrapper getPlayerInfoJson(@NotNull Player player) {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GamePlayerInfo.NAME, player.getUser().getNickname());
        obj.add(Consts.GamePlayerInfo.SCORE, player.getScore());
        obj.add(Consts.GamePlayerInfo.STATUS, getPlayerStatus(player).toString());
        return obj;
    }

    /**
     * Determine the player status for a given player, based on game state.
     *
     * @param player Player for whom to get the state.
     * @return The state of {@param player}, depending on the game's state and what the player has done.
     */
    private Consts.GamePlayerState getPlayerStatus(Player player) {
        switch (state) {
            case LOBBY:
                if (host == player) return Consts.GamePlayerState.HOST;
                else return Consts.GamePlayerState.IDLE;
            case PLAYING:
                if (getJudge() == player) return Consts.GamePlayerState.JUDGE;
                if (!roundPlayers.contains(player)) return Consts.GamePlayerState.IDLE;

                List<WhiteCard> playerCards = playedCards.getCards(player);
                if (playerCards != null && blackCard != null && playerCards.size() >= blackCard.getPick())
                    return Consts.GamePlayerState.IDLE;
                else
                    return Consts.GamePlayerState.PLAYING;
            case JUDGING:
                if (getJudge() == player) return Consts.GamePlayerState.JUDGING;
                else return Consts.GamePlayerState.IDLE;
            case ROUND_OVER:
                if (didPlayerWonGame(player)) return Consts.GamePlayerState.WINNER;
                else return Consts.GamePlayerState.IDLE;
            default:
                throw new IllegalStateException("Unknown GameState " + state.toString());
        }
    }

    /**
     * Start the game, if there are at least 3 players present
     */
    public void start() throws FailedLoadingSomeCardcastDecks, BaseCahHandler.CahException {
        if (state != Consts.GameState.LOBBY)
            throw new BaseCahHandler.CahException(Consts.ErrorCode.ALREADY_STARTED);
        if (!hasEnoughCards()) throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_ENOUGH_CARDS);

        int numPlayers = players.size();
        if (numPlayers >= 3) {
            judgeIndex = (int) (Math.random() * numPlayers);

            logger.info(String.format("Starting game %d with card sets %s, Cardcast %s, %d blanks, %d max players, %d max spectators, %d score limit, players %s.",
                    id, options.cardSetIds, options.cardcastSetCodes, options.blanksInDeck, options.playerLimit, options.spectatorLimit, options.scoreGoal, players));

            List<CardSet> cardSets;
            synchronized (options.cardSetIds) {
                cardSets = loadCardSets();
                blackDeck = loadBlackDeck(cardSets);
                whiteDeck = loadWhiteDeck(cardSets);
            }

            startNextRound();
            gameManager.broadcastGameListRefresh();
        } else {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_ENOUGH_PLAYERS);
        }
    }

    /**
     * Load all the card sets in this game, even Cardcast ones
     *
     * @return A list of card sets
     * @throws FailedLoadingSomeCardcastDecks If some decks couldn't be loaded
     */
    @Nullable
    public List<CardSet> loadCardSets() throws FailedLoadingSomeCardcastDecks {
        synchronized (options.cardSetIds) {
            List<CardSet> cardSets = new ArrayList<>();
            if (!options.getPyxCardSetIds().isEmpty())
                cardSets.addAll(PyxCardSet.loadCardSets(loadedCards, options.getPyxCardSetIds()));

            FailedLoadingSomeCardcastDecks cardcastException = null;
            for (String cardcastId : options.cardcastSetCodes.toArray(new String[0])) {
                CardcastDeck cardcastDeck = cardcastService.loadSet(cardcastId);
                if (cardcastDeck == null) {
                    if (cardcastException == null) cardcastException = new FailedLoadingSomeCardcastDecks();
                    cardcastException.failedDecks.add(cardcastId);

                    logger.error(String.format("Unable to load %s from Cardcast", cardcastId));
                }

                if (cardcastDeck != null) cardSets.add(cardcastDeck);
            }

            if (cardcastException != null) throw cardcastException;
            else return cardSets;
        }
    }

    /**
     * @param cardSets The given decks
     * @return The number of black cards
     */
    public int blackCardsCount(List<CardSet> cardSets) {
        int count = 0;
        for (CardSet cardSet : cardSets) count += cardSet.getBlackCards().size();
        return count;
    }

    /**
     * @param cardSets The given decks
     * @return The number of white cards + the blank cards
     */
    public int whiteCardsCount(List<CardSet> cardSets) {
        int count = 0;
        for (CardSet cardSet : cardSets) count += cardSet.getWhiteCards().size();
        return count + options.blanksInDeck;
    }

    /**
     * @param cardSets The given card sets
     * @return A new singletons of {@code BlackDeck}
     */
    @NotNull
    private BlackDeck loadBlackDeck(List<CardSet> cardSets) {
        return new BlackDeck(cardSets);
    }

    /**
     * @param cardSets The given card sets
     * @return A new singletons of {@code WhiteDeck}
     */
    @NotNull
    private WhiteDeck loadWhiteDeck(List<CardSet> cardSets) {
        return new WhiteDeck(cardSets, options.blanksInDeck);
    }

    /**
     * @return The minimum number of white cards
     */
    public int getRequiredWhiteCardCount() {
        return MINIMUM_WHITE_CARDS_PER_PLAYER * options.playerLimit;
    }

    /**
     * Determine if there are sufficient cards in the selected card sets to start the game
     */
    public boolean hasEnoughCards() throws FailedLoadingSomeCardcastDecks {
        synchronized (options.cardSetIds) {
            List<CardSet> cardSets = loadCardSets();
            return cardSets != null && !cardSets.isEmpty()
                    && blackCardsCount(cardSets) >= MINIMUM_BLACK_CARDS
                    && whiteCardsCount(cardSets) >= getRequiredWhiteCardCount();
        }
    }

    /**
     * Calculate the time accordingly to the game options
     *
     * @param base The base time
     * @return The calculated time
     */
    private int calculateTime(int base) {
        if (options.timerMultiplier == GameOptions.TimeMultiplier.UNLIMITED) return Integer.MAX_VALUE;
        long val = Math.round(base * options.timerMultiplier.factor());
        if (val > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) val;
    }

    /**
     * Warn players that have not yet played that they are running out of time to do so
     */
    private void warnPlayersToPlay() {
        synchronized (roundTimerLock) {
            killRoundTimer();

            synchronized (roundPlayers) {
                for (final Player player : roundPlayers) {
                    List<WhiteCard> cards = playedCards.getCards(player);
                    if (cards == null || cards.size() < blackCard.getPick()) {
                        player.getUser().enqueueMessage(new QueuedMessage(QueuedMessage.MessageType.GAME_EVENT, new EventWrapper(this, Consts.Event.HURRY_UP)));
                    }
                }
            }

            rescheduleTimer(new SafeTimerTask() {
                @Override
                public void process() {
                    skipIdlePlayers();
                }
            }, 10 * 1000);
        }
    }

    /**
     * Warn the judge that is running out of time to judge
     */
    private void warnJudgeToJudge() {
        synchronized (roundTimerLock) {
            killRoundTimer();

            if (state == Consts.GameState.JUDGING) {
                Player judge = getJudge();
                if (judge != null)
                    judge.getUser().enqueueMessage(new QueuedMessage(QueuedMessage.MessageType.GAME_EVENT, new EventWrapper(this, Consts.Event.HURRY_UP)));
            }

            rescheduleTimer(new SafeTimerTask() {
                @Override
                public void process() {
                    skipIdleJudge();
                }
            }, 10 * 1000);
        }
    }

    /**
     * Kick the judge for being idle, return cards to hand and start a new round
     */
    private void skipIdleJudge() {
        killRoundTimer();

        // Prevent them from playing a card while we kick them (or us kicking them while they play!)
        synchronized (judgeLock) {
            if (state != Consts.GameState.JUDGING) return;

            // Not sure why this would happen but it has happened before.
            // I guess they disconnected at the exact wrong time?
            Player judge = getJudge();
            String judgeName = "[unknown]";
            if (judge != null) {
                judge.skipped();
                judgeName = judge.getUser().getNickname();
            }

            logger.info(String.format("Skipping idle judge %s in game %d", judgeName, id));

            broadcastToPlayers(QueuedMessage.MessageType.GAME_EVENT, new EventWrapper(this, Consts.Event.GAME_JUDGE_SKIPPED));
            returnCardsToHand();
            startNextRound();
        }
    }

    /**
     * Skip all the idle players that didn't play
     */
    private void skipIdlePlayers() {
        killRoundTimer();
        List<User> playersToRemove = new ArrayList<>();
        List<Player> playersToUpdateStatus = new ArrayList<>();
        synchronized (roundPlayers) {
            for (Player player : roundPlayers) {
                List<WhiteCard> cards = playedCards.getCards(player);
                if (cards == null || cards.size() < blackCard.getPick()) {
                    logger.info(String.format("Skipping idle player %s in game %d.", player, id));
                    player.skipped();

                    EventWrapper ev;
                    if (player.getSkipCount() >= MAX_SKIPS_BEFORE_KICK || playedCards.size() < 2) {
                        ev = new EventWrapper(this, Consts.Event.GAME_PLAYER_KICKED_IDLE);
                        playersToRemove.add(player.getUser());
                    } else {
                        ev = new EventWrapper(this, Consts.Event.GAME_PLAYER_SKIPPED);
                        playersToUpdateStatus.add(player);
                    }

                    ev.add(Consts.GeneralKeys.NICKNAME, player.getUser().getNickname());
                    broadcastToPlayers(QueuedMessage.MessageType.GAME_EVENT, ev);

                    // Put their cards back
                    List<WhiteCard> returnCards = playedCards.remove(player);
                    if (returnCards != null) {
                        player.hand.addAll(returnCards);
                        sendCardsToPlayer(player, returnCards, false);
                    }
                }
            }
        }

        // Remove the select players
        for (User user : playersToRemove) {
            removePlayer(user);
            user.enqueueMessage(new QueuedMessage(QueuedMessage.MessageType.GAME_PLAYER_EVENT, new EventWrapper(this, Consts.Event.KICKED_FROM_GAME_IDLE)));
        }

        synchronized (playedCards) {
            if (state == Consts.GameState.PLAYING || playersToRemove.size() == 0) {
                if (players.size() < 3 || playedCards.size() < 2) {
                    logger.info(String.format("Resetting game %d due to insufficient players after removing %d idle players.", id, playersToRemove.size()));
                    resetState(true);
                } else {
                    judgingState();
                }
            }
        }

        for (Player player : playersToUpdateStatus) notifyPlayerInfoChange(player);
    }

    /**
     * Kill the current game timer task
     */
    private void killRoundTimer() {
        synchronized (roundTimerLock) {
            if (lastScheduledFuture != null) {
                logger.trace(String.format("Killing timer task %s", lastScheduledFuture));
                lastScheduledFuture.cancel(false);
                lastScheduledFuture = null;
            }
        }
    }

    /**
     * Assign a new task to the game timer
     *
     * @param task    The new task
     * @param timeout The delay of execution
     */
    private void rescheduleTimer(SafeTimerTask task, long timeout) {
        synchronized (roundTimerLock) {
            killRoundTimer();
            logger.trace(String.format("Scheduling timer task %s after %d ms", task, timeout));
            lastScheduledFuture = globalTimer.schedule(task, timeout, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Move the game into the judging state, called when everyone ended its turn
     */
    private void judgingState() {
        killRoundTimer();
        state = Consts.GameState.JUDGING;

        int judgeTimer = calculateTime(JUDGE_TIMEOUT_BASE + (JUDGE_TIMEOUT_PER_CARD * playedCards.size() * blackCard.getPick()));

        EventWrapper ev = new EventWrapper(this, Consts.Event.GAME_STATE_CHANGE);
        ev.add(Consts.GeneralGameData.STATE, Consts.GameState.JUDGING.toString());
        ev.add(Consts.OngoingGameData.WHITE_CARDS, getWhiteCardsJson());
        ev.add(Consts.OngoingGameData.PLAY_TIMER, judgeTimer);
        broadcastToPlayers(QueuedMessage.MessageType.GAME_EVENT, ev);

        notifyPlayerInfoChange(getJudge());

        synchronized (roundTimerLock) {
            rescheduleTimer(new SafeTimerTask() {
                @Override
                public void process() {
                    warnJudgeToJudge();
                }
            }, judgeTimer - 10 * 1000);
        }
    }

    /**
     * Reset the game state to a lobby.
     *
     * @param lostPlayer True if because there are no long enough people to play a game, false if because the
     *                   previous game finished.
     */
    public void resetState(boolean lostPlayer) {
        logger.info(String.format("Resetting game %d to lobby (lostPlayer=%b)", id, lostPlayer));
        killRoundTimer();
        synchronized (players) {
            for (Player player : players) {
                player.hand.clear();
                player.resetScore();
            }
        }

        whiteDeck = null;
        blackDeck = null;
        synchronized (blackCardLock) {
            blackCard = null;
        }

        playedCards.clear();
        roundPlayers.clear();
        state = Consts.GameState.LOBBY;
        judgeIndex = 0;

        EventWrapper ev = new EventWrapper(this, Consts.Event.GAME_STATE_CHANGE);
        ev.add(Consts.GeneralGameData.STATE, Consts.GameState.LOBBY.toString());
        broadcastToPlayers(QueuedMessage.MessageType.GAME_EVENT, ev);

        for (Player player : players)
            notifyPlayerInfoChange(player);

        gameManager.broadcastGameListRefresh();
    }

    /**
     * Check to see if judging should begin, based on the number of players that have played and the
     * number of cards they have played.
     *
     * @return True if judging should begin.
     */
    private boolean shouldStartJudging() {
        if (state != Consts.GameState.PLAYING) return false;

        if (playedCards.size() == roundPlayers.size()) {
            boolean startJudging = true;
            for (List<WhiteCard> cards : playedCards.cards()) {
                if (cards.size() != blackCard.getPick()) {
                    startJudging = false;
                    break;
                }
            }

            return startJudging;
        } else {
            return false;
        }
    }

    /**
     * Start the next round
     */
    private void startNextRound() {
        killRoundTimer();

        // Remove played cards from deck
        synchronized (playedCards) {
            for (List<WhiteCard> cards : playedCards.cards()) {
                for (WhiteCard card : cards) whiteDeck.discard(card);
            }

            playedCards.clear();
        }

        // Pick new judge and update players' list
        synchronized (players) {
            judgeIndex++;
            if (judgeIndex >= players.size()) judgeIndex = 0;

            roundPlayers.clear();
            for (Player player : players) {
                if (player != getJudge()) roundPlayers.add(player);
            }
        }

        // Discard old black card and pick a new one
        synchronized (blackCardLock) {
            if (blackCard != null) blackDeck.discard(blackCard);
            blackCard = getNextBlackCard();
        }

        // Deal cards so that everyone has 10 cards or more if draw > 0
        Player[] playersCopy = players.toArray(new Player[players.size()]);
        for (Player player : playersCopy) {
            boolean clearHand = player.hand.size() == 0;
            List<WhiteCard> newCards = new LinkedList<>();
            while (player.hand.size() < 10) {
                WhiteCard card = getNextWhiteCard();
                player.hand.add(card);
                newCards.add(card);
            }

            // Add blank cards if the black card requires them
            for (int i = 0; i < blackCard.getDraw(); i++) {
                WhiteCard blank = whiteDeck.createBlankCard();
                newCards.add(blank);
                player.hand.add(blank);
            }

            sendCardsToPlayer(player, newCards, clearHand);
        }

        state = Consts.GameState.PLAYING;
        int playTimer = calculateTime(PLAY_TIMEOUT_BASE + (PLAY_TIMEOUT_PER_CARD * blackCard.getPick()));

        EventWrapper ev = new EventWrapper(this, Consts.Event.GAME_STATE_CHANGE);
        ev.add(Consts.OngoingGameData.BLACK_CARD, getBlackCardJson());
        ev.add(Consts.GeneralGameData.STATE, Consts.GameState.PLAYING.toString());
        ev.add(Consts.OngoingGameData.PLAY_TIMER, playTimer);
        broadcastToPlayers(QueuedMessage.MessageType.GAME_EVENT, ev);

        for (Player player : players) notifyPlayerInfoChange(player);

        synchronized (roundTimerLock) {
            rescheduleTimer(new SafeTimerTask() {
                @Override
                public void process() {
                    warnPlayersToPlay();
                }
            }, playTimer - 10 * 1000);
        }
    }

    /**
     * @return The next WhiteCard from the deck, reshuffling if required.
     */
    private WhiteCard getNextWhiteCard() {
        try {
            return whiteDeck.getNextCard();
        } catch (OutOfCardsException e) {
            whiteDeck.reshuffle();

            broadcastToPlayers(QueuedMessage.MessageType.GAME_EVENT, new EventWrapper(this, Consts.Event.GAME_WHITE_RESHUFFLE));
            return getNextWhiteCard();
        }
    }

    /**
     * @return The next BlackCard from the deck, reshuffling if required.
     */
    private BlackCard getNextBlackCard() {
        try {
            return blackDeck.getNextCard();
        } catch (final OutOfCardsException e) {
            blackDeck.reshuffle();

            broadcastToPlayers(QueuedMessage.MessageType.GAME_EVENT, new EventWrapper(this, Consts.Event.GAME_BLACK_RESHUFFLE));
            return getNextBlackCard();
        }
    }

    /**
     * Get the player for a given user
     *
     * @param user The given user
     * @return The corresponding player
     **/
    @Nullable
    public Player getPlayerForUser(User user) {
        for (Player player : players.toArray(new Player[players.size()])) {
            if (player.getUser() == user) return player;
        }

        return null;
    }

    /**
     * @return The black card json data
     */
    @Nullable
    public JsonWrapper getBlackCardJson() {
        synchronized (blackCardLock) {
            if (blackCard != null) return blackCard.getClientDataJson();
            else return null;
        }
    }

    /**
     * @return The white cards json data, shuffled
     */
    private JsonArray getWhiteCardsJson() {
        if (state != Consts.GameState.JUDGING) {
            return new JsonArray();
        } else {
            List<List<WhiteCard>> shuffledPlayedCards = new ArrayList<>(playedCards.cards());
            Collections.shuffle(shuffledPlayedCards);

            JsonArray json = new JsonArray(shuffledPlayedCards.size());
            for (List<WhiteCard> cards : shuffledPlayedCards) json.add(getWhiteCardsDataJson(cards));
            return json;
        }
    }

    /**
     * @param user The user who submitted the request
     * @return The white cards json data to be returned to the client, an array of arrays
     */
    public JsonArray getWhiteCardsJson(User user) {
        // If we're in judge mode, return all of the cards and ignore which user is asking
        if (state == Consts.GameState.JUDGING) {
            return getWhiteCardsJson();
        } else if (state != Consts.GameState.PLAYING) {
            return new JsonArray();
        } else {
            Player player = getPlayerForUser(user);
            synchronized (playedCards) {
                int faceDownCards = playedCards.size();
                JsonArray json = new JsonArray(faceDownCards);

                if (playedCards.hasPlayer(player)) {
                    List<WhiteCard> cards = playedCards.getCards(player);
                    if (cards != null) json.add(getWhiteCardsDataJson(cards));
                    faceDownCards--;
                }

                int numPick = blackCard == null ? 1 : blackCard.getPick() + blackCard.getDraw();
                while (faceDownCards-- > 0) {
                    JsonArray array = new JsonArray(numPick);
                    for (int i = 0; i < numPick; i++) array.add(WhiteCard.getFaceDownCardClientDataJson().obj());
                    json.add(array);
                }

                return json;
            }
        }
    }

    /**
     * Deal hand cards
     *
     * @param player Hand owner
     * @param cards  The player's hand
     */
    private void sendCardsToPlayer(Player player, List<WhiteCard> cards, boolean clear) {
        EventWrapper ev = new EventWrapper(this, Consts.Event.HAND_DEAL);
        ev.add(Consts.OngoingGameData.HAND, getWhiteCardsDataJson(cards));
        ev.add(Consts.OngoingGameData.CLEAR_HAND, clear);
        player.getUser().enqueueMessage(new QueuedMessage(QueuedMessage.MessageType.GAME_EVENT, ev));
    }

    /**
     * @param user The given user
     * @return The player's hand as json
     */
    @NotNull
    public JsonArray getHandJson(User user) {
        Player player = getPlayerForUser(user);
        if (player != null) {
            synchronized (player.hand) {
                return getWhiteCardsDataJson(player.hand);
            }
        } else {
            return new JsonArray();
        }
    }

    /**
     * @return A list of all users in this game.
     */
    private List<User> playersToUsers() {
        List<User> users = new ArrayList<>(players.size());
        for (Player player : players.toArray(new Player[players.size()]))
            users.add(player.getUser());

        synchronized (spectators) {
            users.addAll(spectators);
        }

        return users;
    }

    /**
     * @return The judge for the current round
     */
    private Player getJudge() {
        if (judgeIndex >= 0 && judgeIndex < players.size()) return players.get(judgeIndex);
        else return players.get(0); // Shouldn't happen
    }

    /**
     * Play a card.
     *
     * @param user     User playing the card.
     * @param cardId   ID of the card to play.
     * @param cardText User text for a blank card. Ignored for normal cards.
     * @return The number of cards left to play
     */
    @NotNull
    public JsonWrapper playCard(User user, int cardId, @Nullable String cardText) throws BaseCahHandler.CahException {
        Player player = getPlayerForUser(user);
        if (player != null) {
            player.resetSkipCount();
            if (getJudge() == player || state != Consts.GameState.PLAYING)
                throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_YOUR_TURN);

            int playedCardsCount = playedCards.playedCardsCount(player);
            if (playedCardsCount == blackCard.getPick())
                throw new BaseCahHandler.CahException(Consts.ErrorCode.ALREADY_PLAYED);

            int playedBlankCards = playedCards.playedWriteInCardsCount(player);
            int playedPickCards = playedCardsCount - playedBlankCards;

            WhiteCard playCard = null;
            synchronized (player.hand) {
                Iterator<WhiteCard> iter = player.hand.iterator();
                while (iter.hasNext()) {
                    WhiteCard card = iter.next();
                    if (card.getId() == cardId) {
                        playCard = card;
                        if (WhiteDeck.isBlankCard(card)) {
                            if (cardText == null || cardText.isEmpty())
                                throw new BaseCahHandler.CahException(Consts.ErrorCode.NO_MSG_SPECIFIED);

                            ((BlankWhiteCard) playCard).setText(cardText);
                            playedBlankCards++;
                        } else {
                            if (playedPickCards == blackCard.getPick() - blackCard.getDraw())
                                throw new BaseCahHandler.CahException(Consts.ErrorCode.SHOULD_DRAW_CARD);

                            playedPickCards++;
                        }

                        // Remove the card from their hand. The client will also do so when we return success, so no need to tell it to do so here.
                        iter.remove();
                        break;
                    }
                }
            }

            if (playCard != null) {
                playedCards.addCard(player, playCard);

                notifyPlayerInfoChange(player);
                if (shouldStartJudging()) judgingState();

                JsonWrapper obj = new JsonWrapper();
                obj.add(Consts.OngoingGameData.LEFT_TO_DRAW, blackCard.getDraw() - playedBlankCards);
                obj.add(Consts.OngoingGameData.LEFT_TO_PICK, blackCard.getPick() - blackCard.getDraw() - playedPickCards);
                return obj;
            } else {
                throw new BaseCahHandler.CahException(Consts.ErrorCode.DO_NOT_HAVE_CARD);
            }
        } else {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_IN_THAT_GAME);
        }
    }

    @Nullable
    public JsonWrapper getPlayerToPlayCards(User user) {
        Player player = getPlayerForUser(user);
        if (player == null) // The user may be a spectator
            return null;

        if (blackCard == null)
            return null;

        int playedBlankCards = playedCards.playedWriteInCardsCount(player);

        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.OngoingGameData.LEFT_TO_DRAW, blackCard.getDraw() - playedBlankCards);
        obj.add(Consts.OngoingGameData.LEFT_TO_PICK, blackCard.getPick() - blackCard.getDraw()
                - (playedCards.playedCardsCount(player) - playedBlankCards));
        return obj;
    }

    /**
     * The judge has selected a card. The {@code cardId} passed in may be any white card's ID for
     * black cards that have multiple selection, however only the first card in the set's ID will be
     * passed around to clients.
     *
     * @param judge  Judge user.
     * @param cardId Selected card ID.
     */
    public void judgeCard(User judge, int cardId) throws BaseCahHandler.CahException {
        Player winner;
        synchronized (judgeLock) {
            Player judgePlayer = getPlayerForUser(judge);
            if (getJudge() != judgePlayer) throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_JUDGE);
            else if (state != Consts.GameState.JUDGING)
                throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_YOUR_TURN);

            if (judgePlayer != null) judgePlayer.resetSkipCount();

            winner = playedCards.getPlayerForId(cardId);
            if (winner == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.INVALID_CARD);

            winner.increaseScore();
        }

        state = Consts.GameState.ROUND_OVER;

        boolean won = didPlayerWonGame(winner);
        EventWrapper ev = new EventWrapper(this, Consts.Event.GAME_STATE_CHANGE);
        ev.add(Consts.GeneralGameData.STATE, Consts.GameState.ROUND_OVER.toString());
        ev.add(Consts.OngoingGameData.ROUND_WINNER, winner.getUser().getNickname());
        ev.add(Consts.OngoingGameData.WILL_STOP, won);
        ev.add(Consts.OngoingGameData.WINNING_CARD, Utils.joinCardIds(playedCards.getCards(winner), ","));
        ev.add(Consts.OngoingGameData.INTERMISSION, ROUND_INTERMISSION);
        broadcastToPlayers(QueuedMessage.MessageType.GAME_EVENT, ev);

        notifyPlayerInfoChange(winner); // For score and status

        synchronized (roundTimerLock) {
            if (won) {
                rescheduleTimer(new SafeTimerTask() {
                    @Override
                    public void process() {
                        resetState(false);
                    }
                }, ROUND_INTERMISSION);
            } else {
                rescheduleTimer(new SafeTimerTask() {
                    @Override
                    public void process() {
                        startNextRound();
                    }
                }, ROUND_INTERMISSION);
            }
        }

        Map<String, List<WhiteCard>> cardsBySessionId = new HashMap<>();
        playedCards.cardsByUser().forEach((key, value) -> cardsBySessionId.put(key.getSessionId(), value));
    }

    /**
     * @return The minimum number of black cards
     */
    public int getRequiredBlackCardCount() {
        return MINIMUM_BLACK_CARDS;
    }

    /**
     * @param player The given player
     * @return Whether the given player won the current round
     */
    private boolean didPlayerWonGame(Player player) {
        if (player.getScore() >= options.scoreGoal) {
            if (options.winBy == 0) return true;

            int highestScore = -1;
            synchronized (roundPlayers) {
                for (Player p : roundPlayers) {
                    if (player.equals(p)) continue;
                    if (p.getScore() > highestScore) highestScore = p.getScore();
                }
            }

            return highestScore == -1 || player.getScore() >= highestScore + options.winBy;
        } else {
            return false;
        }
    }
}
