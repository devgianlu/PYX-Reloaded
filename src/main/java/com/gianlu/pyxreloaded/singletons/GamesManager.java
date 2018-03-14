package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.game.GameOptions;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.task.BroadcastGameListUpdateTask;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class GamesManager {
    private static final Logger logger = Logger.getLogger(GamesManager.class);
    private final int maxGames;
    private final Map<Integer, Game> games = new TreeMap<>();
    private final GameProvider gameProvider;
    private final BroadcastGameListUpdateTask broadcastUpdate;

    public GamesManager(GameProvider gameProvider, Preferences preferences, BroadcastGameListUpdateTask broadcastUpdate) {
        this.gameProvider = gameProvider;
        this.maxGames = preferences.getInt("maxGames", 100);
        this.broadcastUpdate = broadcastUpdate;
    }

    public static int generateGameId() {
        return ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
    }

    public int getMaxGames() {
        return maxGames;
    }

    /**
     * Creates a new game and puts the specified user into the game, if there are free game slots.
     * <p>
     * Creating the game and adding the user are done atomically with respect to another game getting
     * created, or even getting the list of active games. It is impossible for another user to join
     * the game before the requesting user.
     *
     * @param user User to place into the game.
     * @return Newly created game
     * @throws BaseCahHandler.CahException If the user is already in a game and cannot join another.
     */
    @NotNull
    public Game createGameWithPlayer(User user, @Nullable GameOptions options) throws BaseCahHandler.CahException {
        PreparingShutdown.get().check();

        synchronized (games) {
            if (games.size() >= getMaxGames())
                throw new BaseCahHandler.CahException(Consts.ErrorCode.TOO_MANY_GAMES);

            Game game = gameProvider.create(this, options);
            games.put(game.getId(), game);

            game.addPlayer(user);
            logger.info(String.format("Created new game %d by user %s.", game.getId(), user.toString()));

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
    public void destroyGame(int gameId) {
        synchronized (games) {
            final Game game = games.remove(gameId);
            if (game == null) return;

            // Remove the players from the game
            List<User> usersToRemove = game.getUsers();
            for (User user : usersToRemove) {
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
     * @return A copy of the list of all current games.
     */
    public Collection<Game> getGameList() {
        synchronized (games) {
            return new ArrayList<>(games.values());
        }
    }

    /**
     * Gets the game with the specified id, or {@code null} if there is no game with that id.
     *
     * @param id Id of game to retrieve.
     * @return The Game, or {@code null} if there is no game with that id.
     */
    public Game getGame(int id) {
        synchronized (games) {
            return games.get(id);
        }
    }

    public interface GameProvider {
        Game create(GamesManager manager, GameOptions options);
    }
}
