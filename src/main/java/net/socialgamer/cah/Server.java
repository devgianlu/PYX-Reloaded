package net.socialgamer.cah;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import net.socialgamer.cah.cardcast.CardcastService;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.db.LoadedCards;
import net.socialgamer.cah.servlets.*;
import net.socialgamer.cah.servlets.Provider;
import net.socialgamer.cah.task.BroadcastGameListUpdateTask;
import net.socialgamer.cah.task.RefreshAdminTokenTask;
import net.socialgamer.cah.task.UserPingTask;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
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

        GameManager gameManager = new GameManager((manager, options) -> new Game(GameManager.generateGameId(), options, connectedUsers, manager, globalTimer, preferences, cardcastService), 100, updateGameListTask);
        Providers.add(Annotations.GameManager.class, (Provider<GameManager>) () -> gameManager);

        ResourceHandler webContentHandler = Handlers.resource(new PathResourceManager(Paths.get(preferences.getString("webContent", "./WebContent")).toAbsolutePath()));
        CacheControlHandler resourceManager;
        if (preferences.getBoolean("cacheEnabled", true))
            resourceManager = CacheControlHandler.browserManagedCache(webContentHandler);
        else
            resourceManager = CacheControlHandler.disableCache(webContentHandler);

        PathHandler handler = new PathHandler(resourceManager);
        handler.addExactPath("/AjaxServlet", new BaseAjaxHandler())
                .addExactPath("/Events", Handlers.websocket(new EventsHandler()));

        Undertow.Builder server = Undertow.builder()
                .setHandler(handler);

        int port = preferences.getInt("port", 80);

        if (preferences.getBoolean("secure", false)) {
            server.addHttpListener(port, "0.0.0.0", new HttpsRedirect())
                    .addHttpsListener(preferences.getInt("securePort", 443), "0.0.0.0", getSSLContext(
                    new File(preferences.getString("keyStorePath", "")), preferences.getString("keyStorePassword", ""),
                    new File(preferences.getString("trustStorePath", "")), preferences.getString("trustStorePassword", "")));
        } else {
            server.addHttpListener(port, "0.0.0.0");
        }

        server.build().start();
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
