package net.socialgamer.cah;

import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.cardcast.CardcastService;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.db.LoadedCards;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.App;
import net.socialgamer.cah.servlets.Provider;
import net.socialgamer.cah.servlets.Providers;
import net.socialgamer.cah.task.BroadcastGameListUpdateTask;
import net.socialgamer.cah.task.UserPingTask;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final long PING_START_DELAY = TimeUnit.SECONDS.toMillis(60);
    private static final long PING_CHECK_DELAY = TimeUnit.SECONDS.toMillis(5);
    private static final long BROADCAST_UPDATE_START_DELAY = TimeUnit.SECONDS.toMillis(60);
    private static final long BROADCAST_UPDATE_DELAY = TimeUnit.SECONDS.toMillis(60);

    public static void main(String[] args) throws IOException, SQLException {
        Preferences preferences = Preferences.load();
        int maxGames = preferences.getInt("maxGames", 100);
        int maxUsers = preferences.getInt("maxUsers", 400);
        int port = preferences.getInt("port", 80);

        ScheduledThreadPoolExecutor globalTimer = new ScheduledThreadPoolExecutor(maxGames + 2);

        Providers.add(Annotations.Preferences.class, (Provider<Preferences>) () -> preferences);

        LoadedCards.load(preferences.getString("pyxDb", "pyx.sqlite"));

        ConnectedUsers connectedUsers = new ConnectedUsers(false, maxUsers);
        Providers.add(Annotations.ConnectedUsers.class, (Provider<ConnectedUsers>) () -> connectedUsers);

        BroadcastGameListUpdateTask updateGameListTask = new BroadcastGameListUpdateTask(connectedUsers);
        globalTimer.scheduleAtFixedRate(updateGameListTask, BROADCAST_UPDATE_START_DELAY, BROADCAST_UPDATE_DELAY, TimeUnit.MILLISECONDS);

        UserPingTask userPingTask = new UserPingTask(connectedUsers, globalTimer);
        globalTimer.scheduleAtFixedRate(userPingTask, PING_START_DELAY, PING_CHECK_DELAY, TimeUnit.MILLISECONDS);

        Providers.add(Annotations.MaxGames.class, (Provider<Integer>) () -> maxGames);

        CardcastService cardcastService = new CardcastService();
        Providers.add(Annotations.CardcastService.class, (Provider<CardcastService>) () -> cardcastService);

        GameManager gameManager = new GameManager(manager -> new Game(GameManager.generateGameId(), connectedUsers, manager, globalTimer, preferences, cardcastService), 100, updateGameListTask);
        Providers.add(Annotations.GameManager.class, (Provider<GameManager>) () -> gameManager);

        new App(port, new File(preferences.getString("webContent", "./WebContent")), preferences.getString("cors", null)).start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }
}
