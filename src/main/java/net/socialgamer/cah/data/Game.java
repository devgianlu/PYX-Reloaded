package net.socialgamer.cah.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.Preferences;
import net.socialgamer.cah.cardcast.CardcastDeck;
import net.socialgamer.cah.cardcast.CardcastService;
import net.socialgamer.cah.cardcast.FailedLoadingSomeCardcastDecks;
import net.socialgamer.cah.data.QueuedMessage.MessageType;
import net.socialgamer.cah.db.PyxCardSet;
import net.socialgamer.cah.task.SafeTimerTask;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Game data and logic class. Games are simple finite state machines, with 3 states that wait for
 * user input, and 3 transient states that it quickly passes through on the way back to a waiting
 * state:
 * <p>
 * ......Lobby.----------->.Dealing.(transient).-------->.Playing
 * .......^........................^.........................|....................
 * .......|.v----.Win.(transient).<+------.Judging.<---------+....................
 * .....Reset.(transient)
 * <p>
 * Lobby is the default state. When the game host sends a start game event, the game moves to the
 * Dealing state, where it deals out cards to every player and automatically moves into the Playing
 * state. After all players have played a card, the game moves to Judging and waits for the judge to
 * pick a card. The game either moves to Win, if a player reached the win goal, or Dealing
 * otherwise. Win moves through Reset to reset the game back to default state. The game also
 * immediately moves through Reset at any point there are fewer than 3 players in the game.
 *
 * @author Andy Janata (ajanata@socialgamer.net)
 */
public class Game {
    private final static Set<String> FINITE_PLAYTIMES;
    private static final Logger logger = Logger.getLogger(Game.class);

    static {
        final Set<String> finitePlaytimes = new TreeSet<>(Arrays.asList("0.25x", "0.5x", "0.75x", "1x", "1.25x", "1.5x", "1.75x", "2x", "2.5x", "3x", "4x", "5x", "10x"));
        FINITE_PLAYTIMES = Collections.unmodifiableSet(finitePlaytimes);
    }

    /**
     * The minimum number of black cards that must be added to a game for it to be able to start.
     */
    public final int MINIMUM_BLACK_CARDS;

    /**
     * The minimum number of white cards per player limit slots that must be added to a game for it to
     * be able to start.
     * <p>
     * We need 20 * maxPlayers cards. This allows black cards up to "draw 9" to work correctly.
     */
    public final int MINIMUM_WHITE_CARDS_PER_PLAYER;

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
    /**
     * All players present in the game.
     */
    private final List<Player> players = Collections.synchronizedList(new ArrayList<Player>(10));
    /**
     * Players participating in the current round.
     */
    private final List<Player> roundPlayers = Collections.synchronizedList(new ArrayList<Player>(9));
    private final PlayerPlayedCardsTracker playedCards = new PlayerPlayedCardsTracker();
    private final List<User> spectators = Collections.synchronizedList(new ArrayList<User>(10));
    private final ConnectedUsers connectedUsers;
    private final GameManager gameManager;
    private final Object blackCardLock = new Object();
    private final GameOptions options;
    private final Set<String> cardcastDeckIds = Collections.synchronizedSet(new HashSet<String>());
    /**
     * Lock object to prevent judging during idle judge detection and vice-versa.
     */
    private final Object judgeLock = new Object();
    /**
     * Lock to prevent missing timer updates.
     */
    private final Object roundTimerLock = new Object();
    private final ScheduledThreadPoolExecutor globalTimer;
    private final CardcastService cardcastService;
    private Player host;
    private BlackDeck blackDeck;
    private BlackCard blackCard;
    private WhiteDeck whiteDeck;
    private GameState state;
    private int judgeIndex = 0;
    private volatile ScheduledFuture<?> lastScheduledFuture;
    private String currentUniqueId;

    /**
     * Create a new game.
     *
     * @param id             The game's ID.
     * @param connectedUsers The user manager, for broadcasting messages.
     * @param gameManager    The game manager, for broadcasting game list refresh notices and destroying this game
     *                       when everybody leaves.
     * @param globalTimer    The global timer on which to schedule tasks.
     */
    public Game(int id, ConnectedUsers connectedUsers, GameManager gameManager, ScheduledThreadPoolExecutor globalTimer, Preferences preferences, CardcastService cardcastService) {
        this.id = id;
        this.connectedUsers = connectedUsers;
        this.gameManager = gameManager;
        this.globalTimer = globalTimer;
        this.options = new GameOptions(preferences);
        this.cardcastService = cardcastService;
        this.state = GameState.LOBBY;

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
        for (WhiteCard card : cards) json.add(card.getClientDataJson());
        return json;
    }

    /**
     * Add a player to the game.
     * <p>
     * Synchronizes on {@link #players}.
     *
     * @param user Player to add to this game.
     * @throws TooManyPlayersException Thrown if this game is at its maximum player capacity.
     * @throws IllegalStateException   Thrown if {@code user} is already in a game.
     */
    public void addPlayer(final User user) throws TooManyPlayersException, IllegalStateException {
        logger.info(String.format("%s joined game %d.", user.toString(), id));
        synchronized (players) {
            if (options.playerLimit >= 3 && players.size() >= options.playerLimit) throw new TooManyPlayersException();

            // this will throw IllegalStateException if the user is already in a game, including this one.
            user.joinGame(this);
            Player player = new Player(user);
            players.add(player);
            if (host == null) host = player;
        }

        JsonObject obj = getEventJson(LongPollEvent.GAME_PLAYER_JOIN);
        obj.addProperty(LongPollResponse.NICKNAME.toString(), user.getNickname());
        broadcastToPlayers(MessageType.GAME_PLAYER_EVENT, obj);
    }

    public boolean isPasswordCorrect(String userPassword) {
        return getPassword() == null || getPassword().isEmpty() || Objects.equals(userPassword, getPassword());
    }

    /**
     * Remove a player from the game.
     * <br/>
     * Synchronizes on {@link #players}, {@link #playedCards}, {@link #whiteDeck}, and
     * {@link #roundTimerLock}.
     *
     * @param user Player to remove from the game.
     * @return True if {@code user} was the last player in the game.
     */
    public boolean removePlayer(final User user) {
        logger.info(String.format("Removing %s from game %d.", user.toString(), id));

        boolean wasJudge = false;
        Player player = getPlayerForUser(user);
        if (player != null) {
            // If they played this round, remove card from played card list.
            List<WhiteCard> cards = playedCards.remove(player);
            if (cards != null && cards.size() > 0) {
                for (WhiteCard card : cards) whiteDeck.discard(card);
            }

            // If they are to play this round, remove them from that list.
            if (roundPlayers.remove(player)) {
                if (shouldStartJudging()) judgingState();
            }

            // If they have a hand, return it to discard pile.
            if (player.hand.size() > 0) {
                for (WhiteCard card : player.hand) whiteDeck.discard(card);
            }

            // If they are judge, return all played cards to hand, and move to next judge.
            if (getJudge() == player && (state == GameState.PLAYING || state == GameState.JUDGING)) {
                JsonObject obj = getEventJson(LongPollEvent.GAME_JUDGE_LEFT);
                obj.addProperty(LongPollResponse.INTERMISSION.toString(), ROUND_INTERMISSION);
                broadcastToPlayers(MessageType.GAME_EVENT, obj);

                returnCardsToHand();
                // startNextRound will advance it again.
                judgeIndex--;
                // Can't start the next round right here.
                wasJudge = true;
            } else if (players.indexOf(player) < judgeIndex) {
                // If they aren't judge but are earlier in judging order, fix the judge index.
                judgeIndex--;
            }

            // we can't actually remove them until down here because we need to deal with the judge
            // index stuff first.
            players.remove(player);
            user.leaveGame(this);

            // do this down here so the person that left doesn't get the notice too
            JsonObject obj = getEventJson(LongPollEvent.GAME_PLAYER_LEAVE);
            obj.addProperty(LongPollResponse.NICKNAME.toString(), user.getNickname());
            broadcastToPlayers(MessageType.GAME_PLAYER_EVENT, obj);

            // Don't do this anymore, it was driving up a crazy amount of traffic.
            // gameManager.broadcastGameListRefresh();

            if (host == player) {
                if (players.size() > 0) host = players.get(0);
                else host = null;
            }

            // this seems terrible
            if (players.size() == 0) gameManager.destroyGame(id);

            if (players.size() < 3 && state != GameState.LOBBY) {
                logger.info(String.format("Resetting game %d due to too few players after someone left.", id));
                resetState(true);
            } else if (wasJudge) {
                synchronized (roundTimerLock) {
                    rescheduleTimer(new SafeTimerTask() {
                        @Override
                        public void process() {
                            startNextRound();
                        }
                    }, ROUND_INTERMISSION);
                }
            }

            return players.size() == 0;
        }

        return false;
    }

    /**
     * Add a spectator to the game.
     * <p>
     * Synchronizes on {@link #spectators}.
     *
     * @param user Spectator to add to this game.
     * @throws TooManySpectatorsException Thrown if this game is at its maximum spectator capacity.
     * @throws IllegalStateException      Thrown if {@code user} is already in a game.
     */
    public void addSpectator(User user) throws TooManySpectatorsException, IllegalStateException {
        logger.info(String.format("%s joined game %d as a spectator.", user.toString(), id));
        synchronized (spectators) {
            if (spectators.size() >= options.spectatorLimit) throw new TooManySpectatorsException();

            // this will throw IllegalStateException if the user is already in a game, including this one.
            user.joinGame(this);
            spectators.add(user);
        }

        JsonObject obj = getEventJson(LongPollEvent.GAME_SPECTATOR_JOIN);
        obj.addProperty(LongPollResponse.NICKNAME.toString(), user.getNickname());
        broadcastToPlayers(MessageType.GAME_PLAYER_EVENT, obj);

        gameManager.broadcastGameListRefresh();
    }

    /**
     * Remove a spectator from the game.
     * <br/>
     * Synchronizes on {@link #spectators}.
     *
     * @param user Spectator to remove from the game.
     */
    public void removeSpectator(final User user) {
        logger.info(String.format("Removing spectator %s from game %d.", user.toString(), id));
        synchronized (spectators) {
            if (!spectators.remove(user)) return;
            // not actually spectating
            user.leaveGame(this);
        }

        // do this down here so the person that left doesn't get the notice too
        JsonObject obj = getEventJson(LongPollEvent.GAME_SPECTATOR_LEAVE);
        obj.addProperty(LongPollResponse.NICKNAME.toString(), user.getNickname());
        broadcastToPlayers(MessageType.GAME_PLAYER_EVENT, obj);

        // Don't do this anymore, it was driving up a crazy amount of traffic.
        // gameManager.broadcastGameListRefresh();
    }

    /**
     * Return all played cards to their respective player's hand.
     * <br/>
     * Synchronizes on {@link #playedCards}.
     */
    private void returnCardsToHand() {
        synchronized (playedCards) {
            for (Player p : playedCards.playedPlayers()) {
                p.hand.addAll(playedCards.getCards(p));
                sendCardsToPlayer(p, playedCards.getCards(p));
            }

            // prevent startNextRound from discarding cards
            playedCards.clear();
        }
    }

    /**
     * Broadcast a message to all players in this game.
     *
     * @param type       Type of message to broadcast. This determines the order the messages are returned by
     *                   priority.
     * @param masterData Message data to broadcast.
     */
    public void broadcastToPlayers(MessageType type, JsonObject masterData) {
        connectedUsers.broadcastToList(playersToUsers(), type, masterData);
    }

    /**
     * Sends updated player information about a specific player to all players in the game.
     *
     * @param player The player whose information has been changed.
     */
    public void notifyPlayerInfoChange(Player player) {
        JsonObject obj = getEventJson(LongPollEvent.GAME_PLAYER_INFO_CHANGE);
        obj.add(LongPollResponse.PLAYER_INFO.toString(), getPlayerInfoJson(player));
        broadcastToPlayers(MessageType.GAME_PLAYER_EVENT, obj);
    }

    /**
     * Sends updated game information to all players in the game.
     */
    private void notifyGameOptionsChanged() {
        JsonObject obj = getEventJson(LongPollEvent.GAME_OPTIONS_CHANGED);
        obj.add(LongPollResponse.GAME_INFO.toString(), getInfoJson(true));
        broadcastToPlayers(MessageType.GAME_EVENT, obj);
    }

    /**
     * @return The game's current state.
     */
    public GameState getState() {
        return state;
    }

    /**
     * @return The {@code User} who is the host of this game.
     */
    public User getHost() {
        if (host == null) return null;
        return host.getUser();
    }

    /**
     * @return All {@code User}s in this game.
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

    public String getPassword() {
        return options.password;
    }

    public void updateGameSettings(GameOptions newOptions) {
        this.options.update(newOptions);
        notifyGameOptionsChanged();
    }

    public Set<String> getCardcastDeckIds() {
        return cardcastDeckIds;
    }

    /**
     * Get information about this game, without the game's password.
     * <br/>
     * Synchronizes on {@link #players}.
     *
     * @return This game's general information: ID, host, state, player list, etc.
     */
    @Nullable
    public Map<GameInfo, Object> getInfo() {
        return getInfo(false);
    }

    @NotNull
    public JsonObject getInfoJson(boolean includePassword) {
        JsonObject obj = new JsonObject();
        obj.addProperty(GameInfo.ID.toString(), id);

        // This is probably happening because the game ceases to exist in the middle of getting the
        // game list. Just return nothing.
        if (host == null) return new JsonObject();

        obj.addProperty(GameInfo.HOST.toString(), host.getUser().getNickname());
        obj.addProperty(GameInfo.STATE.toString(), state.toString());
        obj.add(GameInfo.GAME_OPTIONS.toString(), options.toJson(includePassword));
        obj.addProperty(GameInfo.HAS_PASSWORD.toString(), options.password != null && !options.password.equals(""));

        JsonArray playerNames = new JsonArray();
        for (Player player : players.toArray(new Player[players.size()]))
            playerNames.add(player.getUser().getNickname());
        obj.add(GameInfo.PLAYERS.toString(), playerNames);

        JsonArray spectatorNames = new JsonArray();
        for (User spectator : spectators.toArray(new User[spectators.size()]))
            spectatorNames.add(spectator.getNickname());
        obj.add(GameInfo.SPECTATORS.toString(), spectatorNames);

        return obj;
    }

    /**
     * Get information about this game.
     * <br/>
     * Synchronizes on {@link #players}.
     *
     * @param includePassword Include the actual password with the information. This should only be
     *                        sent to people in the game.
     * @return This game's general information: ID, host, state, player list, etc.
     */
    @Nullable
    public Map<GameInfo, Object> getInfo(boolean includePassword) {
        final Map<GameInfo, Object> info = new HashMap<>();
        info.put(GameInfo.ID, id);

        // This is probably happening because the game ceases to exist in the middle of getting the
        // game list. Just return nothing.
        if (host == null) return null;

        info.put(GameInfo.HOST, host.getUser().getNickname());
        info.put(GameInfo.STATE, state.toString());
        info.put(GameInfo.GAME_OPTIONS, options.serialize(includePassword));
        info.put(GameInfo.HAS_PASSWORD, options.password != null && !options.password.equals(""));

        List<String> playerNames = new ArrayList<>(players.size());
        for (Player player : players.toArray(new Player[players.size()]))
            playerNames.add(player.getUser().getNickname());
        info.put(GameInfo.PLAYERS, playerNames);

        List<String> spectatorNames = new ArrayList<>(players.size());
        for (User spectator : spectators.toArray(new User[spectators.size()]))
            spectatorNames.add(spectator.getNickname());
        info.put(GameInfo.SPECTATORS, spectatorNames);

        return info;
    }

    public JsonElement getAllPlayersInfoJson() {
        JsonArray json = new JsonArray(players.size());
        for (Player player : players.toArray(new Player[players.size()]))
            json.add(getPlayerInfoJson(player));

        return json;
    }

    public final List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    @NotNull
    public JsonObject getPlayerInfoJson(Player player) {
        if (player == null) return new JsonObject();
        JsonObject obj = new JsonObject();
        obj.addProperty(GamePlayerInfo.NAME.toString(), player.getUser().getNickname());
        obj.addProperty(GamePlayerInfo.SCORE.toString(), player.getScore());
        obj.addProperty(GamePlayerInfo.STATUS.toString(), getPlayerStatus(player).toString());
        return obj;
    }

    /**
     * Determine the player status for a given player, based on game state.
     *
     * @param player Player for whom to get the state.
     * @return The state of {@code player}, one of {@code HOST}, {@code IDLE}, {@code JUDGE},
     * {@code PLAYING}, {@code JUDGING}, or {@code WINNER}, depending on the game's state and
     * what the player has done.
     */
    private GamePlayerStatus getPlayerStatus(final Player player) {
        final GamePlayerStatus playerStatus;

        switch (state) {
            case LOBBY:
                if (host == player) playerStatus = GamePlayerStatus.HOST;
                else playerStatus = GamePlayerStatus.IDLE;
                break;
            case PLAYING:
                if (getJudge() == player) {
                    playerStatus = GamePlayerStatus.JUDGE;
                } else {
                    if (!roundPlayers.contains(player)) {
                        playerStatus = GamePlayerStatus.IDLE;
                        break;
                    }

                    List<WhiteCard> playerCards = playedCards.getCards(player);
                    if (playerCards != null && blackCard != null && playerCards.size() == blackCard.getPick()) {
                        playerStatus = GamePlayerStatus.IDLE;
                    } else {
                        playerStatus = GamePlayerStatus.PLAYING;
                    }
                }
                break;
            case JUDGING:
                if (getJudge() == player) playerStatus = GamePlayerStatus.JUDGING;
                else playerStatus = GamePlayerStatus.IDLE;
                break;
            case ROUND_OVER:
                if (getJudge() == player) playerStatus = GamePlayerStatus.JUDGE;
                else if (player.getScore() >= options.scoreGoal)
                    playerStatus = GamePlayerStatus.WINNER;     // TODO win-by-x
                else playerStatus = GamePlayerStatus.IDLE;
                break;
            default:
                throw new IllegalStateException("Unknown GameState " + state.toString());
        }

        return playerStatus;
    }

    /**
     * Start the game, if there are at least 3 players present. This does not do any access checking!
     * <br/>
     * Synchronizes on {@link #players}.
     *
     * @return True if the game is started. Would only be false if there aren't enough players, or the
     * game is already started, or doesn't have enough cards, but hopefully callers and
     * clients would prevent that from happening!
     */
    @Nullable
    public ErrorCode start() throws FailedLoadingSomeCardcastDecks {
        if (state != GameState.LOBBY) return ErrorCode.ALREADY_STARTED;
        if (!hasEnoughCards()) return ErrorCode.NOT_ENOUGH_CARDS;

        int numPlayers = players.size();
        if (numPlayers >= 3) {
            // Pick a random start judge, though the "next" judge will actually go first.
            judgeIndex = (int) (Math.random() * numPlayers);

            currentUniqueId = UniqueIds.getNewRandomID();
            logger.info(String.format("Starting game %d with card sets %s, Cardcast %s, %d blanks, %d "
                            + "max players, %d max spectators, %d score limit, players %s, unique %s.",
                    id, options.cardSetIds, cardcastDeckIds, options.blanksInDeck, options.playerLimit,
                    options.spectatorLimit, options.scoreGoal, players, currentUniqueId));

            // do this stuff outside the players lock; they will lock players again later for much less
            // time, and not at the same time as trying to lock users, which has caused deadlocks
            List<CardSet> cardSets;
            synchronized (options.cardSetIds) {
                cardSets = loadCardSets();
                blackDeck = loadBlackDeck(cardSets);
                whiteDeck = loadWhiteDeck(cardSets);
            }

            startNextRound();
            gameManager.broadcastGameListRefresh();

            return null;
        } else {
            return ErrorCode.NOT_ENOUGH_PLAYERS;
        }
    }

    @Nullable
    public List<CardSet> loadCardSets() throws FailedLoadingSomeCardcastDecks {
        synchronized (options.cardSetIds) {
            List<CardSet> cardSets = new ArrayList<>();
            if (!options.getPyxCardSetIds().isEmpty())
                cardSets.addAll(PyxCardSet.loadCardSets(options.getPyxCardSetIds()));

            FailedLoadingSomeCardcastDecks cardcastException = null;
            for (String cardcastId : cardcastDeckIds.toArray(new String[0])) {
                // Ideally, we can assume that anything in that set is going to load, but it is entirely
                // possible that the cache has expired and we can't re-load it for some reason, so
                // let's be safe.
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

    public int blackCardsCount(List<CardSet> cardSets) {
        int count = 0;
        for (CardSet cardSet : cardSets) count += cardSet.getBlackCards().size();
        return count;
    }

    public int whiteCardsCount(List<CardSet> cardSets) {
        int count = 0;
        for (CardSet cardSet : cardSets) count += cardSet.getWhiteCards().size();
        return count + options.blanksInDeck;
    }

    @NotNull
    private BlackDeck loadBlackDeck(List<CardSet> cardSets) {
        return new BlackDeck(cardSets);
    }

    @NotNull
    private WhiteDeck loadWhiteDeck(List<CardSet> cardSets) {
        return new WhiteDeck(cardSets, options.blanksInDeck);
    }

    public int getRequiredWhiteCardCount() {
        return MINIMUM_WHITE_CARDS_PER_PLAYER * options.playerLimit;
    }

    /**
     * Determine if there are sufficient cards in the selected card sets to start the game.
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
     * Move the game into the {@code DEALING} state, and deal cards. The game immediately then moves
     * into the {@code PLAYING} state.
     * <br/>
     */
    private void dealState() {
        state = GameState.DEALING;
        Player[] playersCopy = players.toArray(new Player[players.size()]);
        for (Player player : playersCopy) {
            List<WhiteCard> newCards = new LinkedList<>();
            while (player.hand.size() < 10) {
                WhiteCard card = getNextWhiteCard();
                player.hand.add(card);
                newCards.add(card);
            }

            sendCardsToPlayer(player, newCards);
        }

        playingState();
    }

    /**
     * Move the game into the {@code PLAYING} state, drawing a new Black Card and dispatching a
     * message to all players.
     * <br/>
     * Synchronizes on {@link #players}, {@link #blackCardLock}, and {@link #roundTimerLock}.
     */
    private void playingState() {
        state = GameState.PLAYING;
        playedCards.clear();

        BlackCard newBlackCard;
        synchronized (blackCardLock) {
            if (blackCard != null) blackDeck.discard(blackCard);
            newBlackCard = blackCard = getNextBlackCard();
        }

        if (newBlackCard.getDraw() > 0) {
            synchronized (players) {
                for (Player player : players) {
                    if (getJudge() == player) continue;

                    List<WhiteCard> cards = new ArrayList<>(newBlackCard.getDraw());
                    for (int i = 0; i < newBlackCard.getDraw(); i++) cards.add(getNextWhiteCard());

                    player.hand.addAll(cards);
                    sendCardsToPlayer(player, cards);
                }
            }
        }

        // Perhaps figure out a better way to do this...
        int playTimer = calculateTime(PLAY_TIMEOUT_BASE + (PLAY_TIMEOUT_PER_CARD * blackCard.getPick()));

        JsonObject obj = getEventJson(LongPollEvent.GAME_STATE_CHANGE);
        obj.add(LongPollResponse.BLACK_CARD.toString(), getBlackCardJson());
        obj.addProperty(LongPollResponse.GAME_STATE.toString(), GameState.PLAYING.toString());
        obj.addProperty(LongPollResponse.PLAY_TIMER.toString(), playTimer);
        broadcastToPlayers(MessageType.GAME_EVENT, obj);

        synchronized (roundTimerLock) {
            // 10 second warning
            rescheduleTimer(new SafeTimerTask() {
                @Override
                public void process() {
                    warnPlayersToPlay();
                }
            }, playTimer - 10 * 1000);
        }
    }

    private int calculateTime(int base) {
        double factor = 1.0d;
        String tm = options.timerMultiplier;
        if (tm.equals("Unlimited")) return Integer.MAX_VALUE;
        if (FINITE_PLAYTIMES.contains(tm)) factor = Double.valueOf(tm.substring(0, tm.length() - 1));
        long retval = Math.round(base * factor);
        if (retval > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) retval;
    }

    /**
     * Warn players that have not yet played that they are running out of time to do so.
     * <br/>
     * Synchronizes on {@link #roundTimerLock} and {@link #roundPlayers}.
     */
    private void warnPlayersToPlay() {
        // have to do this all synchronized in case they play while we're processing this
        synchronized (roundTimerLock) {
            killRoundTimer();

            synchronized (roundPlayers) {
                for (final Player player : roundPlayers) {
                    List<WhiteCard> cards = playedCards.getCards(player);
                    if (cards == null || cards.size() < blackCard.getPick()) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty(LongPollResponse.EVENT.toString(), LongPollEvent.HURRY_UP.toString());
                        obj.addProperty(LongPollResponse.GAME_ID.toString(), this.id);
                        player.getUser().enqueueMessage(new QueuedMessage(MessageType.GAME_EVENT, obj));
                    }
                }
            }

            // 10 seconds to finish playing
            rescheduleTimer(new SafeTimerTask() {
                @Override
                public void process() {
                    skipIdlePlayers();
                }
            }, 10 * 1000);
        }
    }

    private void warnJudgeToJudge() {
        // have to do this all synchronized in case they play while we're processing this
        synchronized (roundTimerLock) {
            killRoundTimer();

            if (state == GameState.JUDGING) {
                JsonObject obj = new JsonObject();
                obj.addProperty(LongPollResponse.EVENT.toString(), LongPollEvent.HURRY_UP.toString());
                obj.addProperty(LongPollResponse.GAME_ID.toString(), this.id);
                Player judge = getJudge();
                if (judge != null) judge.getUser().enqueueMessage(new QueuedMessage(MessageType.GAME_EVENT, obj));
            }

            // 10 seconds to finish playing
            rescheduleTimer(new SafeTimerTask() {
                @Override
                public void process() {
                    skipIdleJudge();
                }
            }, 10 * 1000);
        }
    }

    private void skipIdleJudge() {
        killRoundTimer();
        // prevent them from playing a card while we kick them (or us kicking them while they play!)
        synchronized (judgeLock) {
            if (state != GameState.JUDGING) return;

            // Not sure why this would happen but it has happened before.
            // I guess they disconnected at the exact wrong time?
            Player judge = getJudge();
            String judgeName = "[unknown]";
            if (judge != null) {
                judge.skipped();
                judgeName = judge.getUser().getNickname();
            }

            logger.info(String.format("Skipping idle judge %s in game %d", judgeName, id));

            broadcastToPlayers(MessageType.GAME_EVENT, getEventJson(LongPollEvent.GAME_JUDGE_SKIPPED));
            returnCardsToHand();
            startNextRound();
        }
    }

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

                    JsonObject obj;
                    if (player.getSkipCount() >= MAX_SKIPS_BEFORE_KICK || playedCards.size() < 2) {
                        obj = getEventJson(LongPollEvent.GAME_PLAYER_KICKED_IDLE);
                        playersToRemove.add(player.getUser());
                    } else {
                        obj = getEventJson(LongPollEvent.GAME_PLAYER_SKIPPED);
                        playersToUpdateStatus.add(player);
                    }

                    obj.addProperty(LongPollResponse.NICKNAME.toString(), player.getUser().getNickname());
                    broadcastToPlayers(MessageType.GAME_EVENT, obj);

                    // put their cards back
                    List<WhiteCard> returnCards = playedCards.remove(player);
                    if (returnCards != null) {
                        player.hand.addAll(returnCards);
                        sendCardsToPlayer(player, returnCards);
                    }
                }
            }
        }

        for (User user : playersToRemove) {
            removePlayer(user);
            user.enqueueMessage(new QueuedMessage(MessageType.GAME_PLAYER_EVENT, getEventJson(LongPollEvent.KICKED_FROM_GAME_IDLE)));
        }

        synchronized (playedCards) {
            if (state == GameState.PLAYING || playersToRemove.size() == 0) {
                // not sure how much of this check is actually required
                if (players.size() < 3 || playedCards.size() < 2) {
                    logger.info(String.format(
                            "Resetting game %d due to insufficient players after removing %d idle players.",
                            id, playersToRemove.size()));
                    resetState(true);
                } else {
                    judgingState();
                }
            }
        }

        // have to do this after we move to judging state
        for (Player player : playersToUpdateStatus) notifyPlayerInfoChange(player);
    }

    private void killRoundTimer() {
        synchronized (roundTimerLock) {
            if (lastScheduledFuture != null) {
                logger.trace(String.format("Killing timer task %s", lastScheduledFuture));
                lastScheduledFuture.cancel(false);
                lastScheduledFuture = null;
            }
        }
    }

    private void rescheduleTimer(SafeTimerTask task, long timeout) {
        synchronized (roundTimerLock) {
            killRoundTimer();
            logger.trace(String.format("Scheduling timer task %s after %d ms", task, timeout));
            lastScheduledFuture = globalTimer.schedule(task, timeout, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Move the game into the {@code JUDGING} state.
     */
    private void judgingState() {
        killRoundTimer();
        state = GameState.JUDGING;

        // Perhaps figure out a better way to do this...
        int judgeTimer = calculateTime(JUDGE_TIMEOUT_BASE + (JUDGE_TIMEOUT_PER_CARD * playedCards.size() * blackCard.getPick()));

        JsonObject obj = getEventJson(LongPollEvent.GAME_STATE_CHANGE);
        obj.addProperty(LongPollResponse.GAME_STATE.toString(), GameState.JUDGING.toString());
        obj.add(LongPollResponse.WHITE_CARDS.toString(), getWhiteCardsJson());
        obj.addProperty(LongPollResponse.PLAY_TIMER.toString(), judgeTimer);
        broadcastToPlayers(MessageType.GAME_EVENT, obj);

        notifyPlayerInfoChange(getJudge());

        synchronized (roundTimerLock) {
            // 10 second warning
            rescheduleTimer(new SafeTimerTask() {
                @Override
                public void process() {
                    warnJudgeToJudge();
                }
            }, judgeTimer - 10 * 1000);
        }
    }

    /**
     * Move the game into the {@code WIN} state, which really just moves into the game reset logic.
     */
    private void winState() {
        resetState(false);
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
        state = GameState.LOBBY;
        Player judge = getJudge();
        judgeIndex = 0;

        JsonObject obj = getEventJson(LongPollEvent.GAME_STATE_CHANGE);
        obj.addProperty(LongPollResponse.GAME_STATE.toString(), GameState.LOBBY.toString());
        broadcastToPlayers(MessageType.GAME_EVENT, obj);

        if (host != null) notifyPlayerInfoChange(host);
        if (judge != null) notifyPlayerInfoChange(judge);

        gameManager.broadcastGameListRefresh();
    }

    /**
     * Check to see if judging should begin, based on the number of players that have played and the
     * number of cards they have played.
     *
     * @return True if judging should begin.
     */
    private boolean shouldStartJudging() {
        if (state != GameState.PLAYING) return false;

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
     * Start the next round. Clear out the list of played cards into the discard pile, pick a new
     * judge, set the list of players participating in the round, and move into the {@code DEALING}
     * state.
     */
    private void startNextRound() {
        killRoundTimer();

        synchronized (playedCards) {
            for (List<WhiteCard> cards : playedCards.cards()) {
                for (WhiteCard card : cards) whiteDeck.discard(card);
            }
        }

        synchronized (players) {
            judgeIndex++;
            if (judgeIndex >= players.size()) judgeIndex = 0;

            roundPlayers.clear();
            for (Player player : players) {
                if (player != getJudge()) roundPlayers.add(player);
            }
        }

        dealState();
    }

    public JsonObject getEventJson(LongPollEvent event) {
        JsonObject obj = new JsonObject();
        obj.addProperty(LongPollResponse.EVENT.toString(), event.toString());
        obj.addProperty(LongPollResponse.GAME_ID.toString(), id);
        return obj;
    }

    /**
     * @return The next White Card from the deck, reshuffling if required.
     */
    private WhiteCard getNextWhiteCard() {
        try {
            return whiteDeck.getNextCard();
        } catch (final OutOfCardsException e) {
            whiteDeck.reshuffle();

            broadcastToPlayers(MessageType.GAME_EVENT, getEventJson(LongPollEvent.GAME_WHITE_RESHUFFLE));
            return getNextWhiteCard();
        }
    }

    /**
     * @return The next Black Card from the deck, reshuffling if required.
     */
    private BlackCard getNextBlackCard() {
        try {
            return blackDeck.getNextCard();
        } catch (final OutOfCardsException e) {
            blackDeck.reshuffle();

            broadcastToPlayers(MessageType.GAME_EVENT, getEventJson(LongPollEvent.GAME_BLACK_RESHUFFLE));
            return getNextBlackCard();
        }
    }

    /**
     * Get the {@code Player} object for a given {@code User} object.
     *
     * @param user the user
     * @return The {@code Player} object representing {@code user} in this game, or {@code null} if
     * {@code user} is not in this game.
     */
    @Nullable
    public Player getPlayerForUser(User user) {
        for (Player player : players.toArray(new Player[players.size()])) {
            if (player.getUser() == user) return player;
        }

        return null;
    }

    @Nullable
    public JsonObject getBlackCardJson() {
        synchronized (blackCardLock) {
            if (blackCard != null) return blackCard.getClientDataJson();
            else return null;
        }
    }

    private JsonArray getWhiteCardsJson() {
        if (state != GameState.JUDGING) {
            return new JsonArray();
        } else {
            List<List<WhiteCard>> shuffledPlayedCards = new ArrayList<>(playedCards.cards());
            Collections.shuffle(shuffledPlayedCards);

            JsonArray json = new JsonArray(shuffledPlayedCards.size());
            for (List<WhiteCard> cards : shuffledPlayedCards) json.add(getWhiteCardsDataJson(cards));
            return json;
        }
    }

    // this is an array of arrays
    public JsonArray getWhiteCardsJson(User user) {
        // if we're in judge mode, return all of the cards and ignore which user is asking
        if (state == GameState.JUDGING) {
            return getWhiteCardsJson();
        } else if (state != GameState.PLAYING) {
            return new JsonArray();
        } else {
            Player player = getPlayerForUser(user);
            synchronized (playedCards) {
                int faceDownCards = playedCards.size();
                JsonArray json = new JsonArray(faceDownCards);

                if (playedCards.hasPlayer(player)) {
                    json.add(getWhiteCardsDataJson(playedCards.getCards(player)));
                    faceDownCards--;
                }

                int numPick = blackCard == null ? 1 : blackCard.getPick() + blackCard.getDraw();
                while (faceDownCards-- > 0) {
                    JsonArray array = new JsonArray(numPick);
                    for (int i = 0; i < numPick; i++) array.add(WhiteCard.getFaceDownCardClientDataJson());
                    json.add(array);
                }

                return json;
            }
        }
    }

    /**
     * Send a list of {@code WhiteCard}s to a player.
     *
     * @param player Player to send the cards to.
     * @param cards  The cards to send the player.
     */
    private void sendCardsToPlayer(Player player, List<WhiteCard> cards) {
        JsonObject obj = getEventJson(LongPollEvent.HAND_DEAL);
        obj.add(LongPollResponse.HAND.toString(), getWhiteCardsDataJson(cards));
        player.getUser().enqueueMessage(new QueuedMessage(MessageType.GAME_EVENT, obj));
    }

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
     * @return A list of all {@code User}s in this game.
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
     * @return The judge for the current round, or {@code null} if the judge index is somehow invalid.
     */
    @Nullable
    private Player getJudge() {
        if (judgeIndex >= 0 && judgeIndex < players.size()) return players.get(judgeIndex);
        else return null;
    }

    /**
     * Play a card.
     *
     * @param user     User playing the card.
     * @param cardId   ID of the card to play.
     * @param cardText User text for a blank card.  Ignored for normal cards.
     * @return An {@code ErrorCode} if the play was unsuccessful ({@code user} doesn't have the card,
     * {@code user} is the judge, etc.), or {@code null} if there was no error and the play
     * was successful.
     */
    @Nullable
    public ErrorCode playCard(final User user, final int cardId, final String cardText) {
        final Player player = getPlayerForUser(user);
        if (player != null) {
            player.resetSkipCount();
            if (getJudge() == player || state != GameState.PLAYING) return ErrorCode.NOT_YOUR_TURN;

            WhiteCard playCard = null;
            synchronized (player.hand) {
                Iterator<WhiteCard> iter = player.hand.iterator();
                while (iter.hasNext()) {
                    WhiteCard card = iter.next();
                    if (card.getId() == cardId) {
                        playCard = card;
                        if (WhiteDeck.isBlankCard(card)) ((BlankWhiteCard) playCard).setText(cardText);

                        // remove the card from their hand. the client will also do so when we return
                        // success, so no need to tell it to do so here.
                        iter.remove();
                        break;
                    }
                }
            }

            if (playCard != null) {
                playedCards.addCard(player, playCard);
                notifyPlayerInfoChange(player);
                if (shouldStartJudging()) judgingState();
                return null;
            } else {
                return ErrorCode.DO_NOT_HAVE_CARD;
            }
        } else {
            return null;
        }
    }

    /**
     * The judge has selected a card. The {@code cardId} passed in may be any white card's ID for
     * black cards that have multiple selection, however only the first card in the set's ID will be
     * passed around to clients.
     *
     * @param judge  Judge user.
     * @param cardId Selected card ID.
     * @return Error code if there is an error, or null if success.
     */
    @Nullable
    public ErrorCode judgeCard(final User judge, final int cardId) {
        final Player cardPlayer;
        synchronized (judgeLock) {
            final Player judgePlayer = getPlayerForUser(judge);
            if (getJudge() != judgePlayer) return ErrorCode.NOT_JUDGE;
            else if (state != GameState.JUDGING) return ErrorCode.NOT_YOUR_TURN;

            // shouldn't ever happen, but just in case...
            if (judgePlayer != null) judgePlayer.resetSkipCount();

            cardPlayer = playedCards.getPlayerForId(cardId);
            if (cardPlayer == null) return ErrorCode.INVALID_CARD;

            cardPlayer.increaseScore();
            state = GameState.ROUND_OVER;
        }

        int clientCardId = playedCards.getCards(cardPlayer).get(0).getId();

        JsonObject obj = getEventJson(LongPollEvent.GAME_ROUND_COMPLETE);
        obj.addProperty(LongPollResponse.ROUND_WINNER.toString(), cardPlayer.getUser().getNickname());
        obj.addProperty(LongPollResponse.WINNING_CARD.toString(), clientCardId);
        obj.addProperty(LongPollResponse.INTERMISSION.toString(), ROUND_INTERMISSION);
        broadcastToPlayers(MessageType.GAME_EVENT, obj);

        notifyPlayerInfoChange(getJudge());
        notifyPlayerInfoChange(cardPlayer);

        // TODO win-by-x option
        synchronized (roundTimerLock) {
            if (cardPlayer.getScore() >= options.scoreGoal) {
                rescheduleTimer(new SafeTimerTask() {
                    @Override
                    public void process() {
                        winState();
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
        return null;
    }

    public int getRequiredBlackCardCount() {
        return MINIMUM_BLACK_CARDS;
    }

    /**
     * Exception to be thrown when there are too many players in a game.
     */
    public class TooManyPlayersException extends Exception {
        private static final long serialVersionUID = -6603422097641992017L;
    }

    /**
     * Exception to be thrown when there are too many spectators in a game.
     */
    public class TooManySpectatorsException extends Exception {
        private static final long serialVersionUID = -6603422097641992018L;
    }
}
