package com.gianlu.pyxreloaded;

import com.gianlu.pyxreloaded.cardcast.CardcastService;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.game.GameManager;
import com.gianlu.pyxreloaded.paths.AjaxPath;
import com.gianlu.pyxreloaded.paths.EventsPath;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.CustomResourceHandler;
import com.gianlu.pyxreloaded.server.HttpsRedirect;
import com.gianlu.pyxreloaded.server.Provider;
import com.gianlu.pyxreloaded.singletons.*;
import com.gianlu.pyxreloaded.task.BroadcastGameListUpdateTask;
import com.gianlu.pyxreloaded.task.RefreshAdminTokenTask;
import com.gianlu.pyxreloaded.task.UserPingTask;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ResourceHandler;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getSimpleName());
    private static final long PING_START_DELAY = TimeUnit.SECONDS.toMillis(60);
    private static final long PING_CHECK_DELAY = TimeUnit.SECONDS.toMillis(5);
    private static final long BROADCAST_UPDATE_START_DELAY = TimeUnit.SECONDS.toMillis(60);
    private static final long BROADCAST_UPDATE_DELAY = TimeUnit.SECONDS.toMillis(60);
    private static final long REFRESH_ADMIN_TOKEN_DELAY = TimeUnit.MINUTES.toMillis(5);

    public static void main(String[] args) throws IOException, SQLException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        Preferences preferences = Preferences.load(args);
        int maxGames = preferences.getInt("maxGames", 100);
        int maxUsers = preferences.getInt("maxUsers", 400);

        ScheduledThreadPoolExecutor globalTimer = new ScheduledThreadPoolExecutor(maxGames + 2);
        globalTimer.scheduleAtFixedRate(new RefreshAdminTokenTask(), 0, REFRESH_ADMIN_TOKEN_DELAY, TimeUnit.MILLISECONDS);

        Providers.add(Annotations.Preferences.class, (Provider<Preferences>) () -> preferences);

        LoadedCards loadedCards = new LoadedCards(preferences.getString("pyxDbUrl", "jdbc:sqlite:pyx.sqlite"));
        Providers.add(Annotations.LoadedCards.class, (Provider<LoadedCards>) () -> loadedCards);

        ConnectedUsers connectedUsers = new ConnectedUsers(false, maxUsers);
        Providers.add(Annotations.ConnectedUsers.class, (Provider<ConnectedUsers>) () -> connectedUsers);

        ServerDatabase serverDatabase = new ServerDatabase(preferences.getString("serverDbUrl", "jdbc:sqlite:server.sqlite"));

        UsersWithAccount accounts = new UsersWithAccount(serverDatabase);
        Providers.add(Annotations.UsersWithAccount.class, (Provider<UsersWithAccount>) () -> accounts);

        BanList banList = new BanList(serverDatabase);
        Providers.add(Annotations.BanList.class, (Provider<BanList>) () -> banList);

        BroadcastGameListUpdateTask updateGameListTask = new BroadcastGameListUpdateTask(connectedUsers);
        globalTimer.scheduleAtFixedRate(updateGameListTask, BROADCAST_UPDATE_START_DELAY, BROADCAST_UPDATE_DELAY, TimeUnit.MILLISECONDS);

        UserPingTask userPingTask = new UserPingTask(connectedUsers, globalTimer);
        globalTimer.scheduleAtFixedRate(userPingTask, PING_START_DELAY, PING_CHECK_DELAY, TimeUnit.MILLISECONDS);

        Providers.add(Annotations.MaxGames.class, (Provider<Integer>) () -> maxGames);

        CardcastService cardcastService = new CardcastService();
        Providers.add(Annotations.CardcastService.class, (Provider<CardcastService>) () -> cardcastService);

        GameManager gameManager = new GameManager((manager, options) -> new Game(GameManager.generateGameId(), options, connectedUsers, manager, loadedCards, globalTimer, preferences, cardcastService), 100, updateGameListTask);
        Providers.add(Annotations.GameManager.class, (Provider<GameManager>) () -> gameManager);

        ResourceHandler resourceHandler = new CustomResourceHandler(preferences);
        PathHandler pathHandler = new PathHandler(resourceHandler);
        pathHandler.addExactPath("/AjaxServlet", new AjaxPath())
                .addExactPath("/Events", Handlers.websocket(new EventsPath()));

        RoutingHandler router = new RoutingHandler();
        router.setFallbackHandler(pathHandler)
                .get("/game/{gid}/", exchange -> {
                    exchange.setRelativePath("/game.html");
                    resourceHandler.handleRequest(exchange);
                });

        Undertow.Builder server = Undertow.builder()
                .setHandler(router);

        int port = preferences.getInt("port", 80);
        String host = preferences.getString("host", "0.0.0.0");

        if (preferences.getBoolean("secure", false)) {
            server.addHttpListener(port, host, new HttpsRedirect())
                    .addHttpsListener(preferences.getInt("securePort", 443), host, getSSLContext(
                            new File(preferences.getString("keyStorePath", "")), preferences.getString("keyStorePassword", ""),
                            new File(preferences.getString("trustStorePath", "")), preferences.getString("trustStorePassword", "")));
        } else {
            server.addHttpListener(port, host);
        }

        server.build().start();
        logger.info("Server started!");
    }

    private static SSLContext getSSLContext(File keyStorePath, String keyStorePassword, File trustStorePath, String trustStorePassword) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        clientStore.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientStore, keyStorePassword.toCharArray());
        KeyManager[] kms = kmf.getKeyManagers();

        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(trustStorePath), trustStorePassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        TrustManager[] tms = tmf.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kms, tms, new SecureRandom());

        return sslContext;
    }
}
