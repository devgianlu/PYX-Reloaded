package net.socialgamer.cah.data;

import net.socialgamer.cah.data.Game.TooManyPlayersException;
import net.socialgamer.cah.task.BroadcastGameListUpdateTask;
import org.apache.log4j.Logger;

import java.util.*;


/**
 * Manage games for the server.
 * <p>
 * This is also a Guice provider for game ids.
 *
 * @author Andy Janata (ajanata@socialgamer.net)
 */
public class GameManager {
    private static final Logger logger = Logger.getLogger(GameManager.class);
    private final Integer maxGames;
    private final Map<Integer, Game> games = new TreeMap<>();
    private final Game game;
    private final BroadcastGameListUpdateTask broadcastUpdate;

    /**
     * Potential next game id.
     */
    private int nextId = 0;

    /**
     * Create a new game manager.
     *
     * @param game     Provider for new {@code Game} instances.
     * @param maxGames Provider for maximum number of games allowed on the server.
     */
    public GameManager(Game game, Integer maxGames, BroadcastGameListUpdateTask broadcastUpdate) {
        this.game = game;
        this.maxGames = maxGames;
        this.broadcastUpdate = broadcastUpdate;
    }

    private int getMaxGames() {
        return maxGames;
    }

    /**
     * Creates a new game, if there are free game slots. Returns {@code null} if there are already the
     * maximum number of games in progress.
     *
     * @return Newly created game, or {@code null} if the maximum number of games are in progress.
     */
    private Game createGame() {
        synchronized (games) {
            if (games.size() >= getMaxGames()) return null;
            if (game.getId() < 0) return null;
            games.put(game.getId(), game);
            return game;
        }
    }

    /**
     * Creates a new game and puts the specified user into the game, if there are free game slots.
     * Returns {@code null} if there are already the maximum number of games in progress.
     * <p>
     * Creating the game and adding the user are done atomically with respect to another game getting
     * created, or even getting the list of active games. It is impossible for another user to join
     * the game before the requesting user.
     *
     * @param user User to place into the game.
     * @return Newly created game, or {@code null} if the maximum number of games are in progress.
     * @throws IllegalStateException If the user is already in a game and cannot join another.
     */
    public Game createGameWithPlayer(final User user) throws IllegalStateException {
        synchronized (games) {
            final Game game = createGame();
            if (game == null) return null;

            try {
                game.addPlayer(user);
                logger.info(String.format("Created new game %d by user %s.",
                        game.getId(), user.toString()));
            } catch (final IllegalStateException ise) {
                destroyGame(game.getId());
                throw ise;
            } catch (final TooManyPlayersException tmpe) {
                // this should never happen -- we just made the game
                throw new Error("Impossible exception: Too many players in new game.", tmpe);
            }

            broadcastGameListRefresh();
            return game;
        }
    }

    /**
     * This probably will not be used very often in the server: Games should normally be deleted when
     * all players leave it. I'm putting this in if only to help with testing.
     * <p>
     * Destroys a game immediately. This will almost certainly cause errors on the client for any
     * players left in the game. If {@code gameId} isn't valid, this method silently returns.
     *
     * @param gameId ID of game to destroy.
     */
    public void destroyGame(final int gameId) {
        synchronized (games) {
            final Game game = games.remove(gameId);
            if (game == null) {
                return;
            }
            // if the prospective next id isn't valid, set it to the id we just removed
            if (nextId == -1 || games.containsKey(nextId)) {
                nextId = gameId;
            }
            // remove the players from the game
            final List<User> usersToRemove = game.getUsers();
            for (final User user : usersToRemove) {
                game.removePlayer(user);
                game.removeSpectator(user);
            }

            logger.info(String.format("Destroyed game %d.", game.getId()));
            broadcastGameListRefresh();
        }
    }

    /**
     * Broadcast an event to all users that they should refresh the game list.
     */
    public void broadcastGameListRefresh() {
        broadcastUpdate.needsUpdate();
    }

    /**
     * Get an unused game ID, or -1 if the maximum number of games are in progress. This should not be
     * called in such a case, though!
     * <p>
     * TODO: make this not suck (!!)
     *
     * @return Next game id, or {@code -1} if the maximum number of games are in progress.
     */
    public Integer getNextGameId() {
        synchronized (games) {
            if (games.size() >= getMaxGames()) return -1;

            if (!games.containsKey(nextId) && nextId >= 0) {
                final int ret = nextId;
                nextId = candidateGameId(ret);
                return ret;
            } else {
                final int ret = candidateGameId();
                nextId = candidateGameId(ret);
                return ret;
            }
        }
    }

    private int candidateGameId() {
        return candidateGameId(-1);
    }

    /**
     * Try to guess a good candidate for the next game id.
     *
     * @param skip An id to skip over.
     * @return A guess for the next game id.
     */
    private int candidateGameId(int skip) {
        synchronized (games) {
            int maxGames = getMaxGames();
            if (games.size() >= maxGames) return -1;

            for (int i = 0; i < maxGames; i++) {
                if (i == skip) continue;
                if (!games.containsKey(i)) return i;
            }

            return -1;
        }
    }

    /**
     * @return A copy of the list of all current games.
     */
    public Collection<Game> getGameList() {
        synchronized (games) {
            // return a copy
            return new ArrayList<>(games.values());
        }
    }

    /**
     * Gets the game with the specified id, or {@code null} if there is no game with that id.
     *
     * @param id Id of game to retrieve.
     * @return The Game, or {@code null} if there is no game with that id.
     */
    public Game getGame(final int id) {
        synchronized (games) {
            return games.get(id);
        }
    }

    Map<Integer, Game> getGames() {
        return games;
    }
}
