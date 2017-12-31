package net.socialgamer.cah;

import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.cardcast.CardcastService;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.metrics.GeoIP;
import net.socialgamer.cah.metrics.Metrics;
import net.socialgamer.cah.metrics.NoOpMetrics;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.App;
import net.socialgamer.cah.servlets.Provider;
import net.socialgamer.cah.servlets.Providers;
import net.socialgamer.cah.task.BroadcastGameListUpdateTask;
import net.socialgamer.cah.task.UserPingTask;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final long PING_START_DELAY = TimeUnit.SECONDS.toMillis(60);
    private static final long PING_CHECK_DELAY = TimeUnit.SECONDS.toMillis(5);
    private static final long BROADCAST_UPDATE_START_DELAY = TimeUnit.SECONDS.toMillis(60);
    private static final long BROADCAST_UPDATE_DELAY = TimeUnit.SECONDS.toMillis(60);
    private static final int MAX_GAMES = 100;
    private static final int MAX_USERS = 400;

    // TODO: Should load everything from prefs
    public static void main(String[] args) throws IOException {
        Metrics metrics = new NoOpMetrics();
        ScheduledThreadPoolExecutor globalTimer = new ScheduledThreadPoolExecutor(MAX_GAMES + 2);

        ConnectedUsers connectedUsers = new ConnectedUsers(false, MAX_USERS, new GeoIP(new Properties()), metrics);
        Providers.add(Annotations.ConnectedUsers.class, (Provider<ConnectedUsers>) () -> connectedUsers);

        BroadcastGameListUpdateTask updateGameListTask = new BroadcastGameListUpdateTask(connectedUsers);
        globalTimer.scheduleAtFixedRate(updateGameListTask, BROADCAST_UPDATE_START_DELAY, BROADCAST_UPDATE_DELAY, TimeUnit.MILLISECONDS);

        UserPingTask userPingTask = new UserPingTask(connectedUsers, globalTimer);
        globalTimer.scheduleAtFixedRate(userPingTask, PING_START_DELAY, PING_CHECK_DELAY, TimeUnit.MILLISECONDS);

        Providers.add(Annotations.MaxGames.class, (Provider<Integer>) () -> MAX_GAMES);
        Providers.add(Annotations.IncludeInactiveCardsets.class, (Provider<Boolean>) () -> false);

        CardcastService cardcastService = new CardcastService();
        Providers.add(Annotations.CardcastService.class, (Provider<CardcastService>) () -> cardcastService);

        GameManager gameManager = new GameManager(manager -> new Game(manager.getNextGameId(), connectedUsers, manager, globalTimer, Providers.get(Annotations.HibernateSession.class), cardcastService, metrics), 100, updateGameListTask);
        Providers.add(Annotations.GameManager.class, (Provider<GameManager>) () -> gameManager);

        new App(1000).start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }
}
