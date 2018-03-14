package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import io.undertow.Undertow;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PreparingShutdown {
    private static final Logger logger = Logger.getLogger(PreparingShutdown.class.getSimpleName());
    private static PreparingShutdown instance;
    private final Undertow server;
    private final ConnectedUsers users;
    private final SocialLogin socials;
    private final LoadedCards loadedCards;
    private final ServerDatabase serverDatabase;
    private boolean value = false;

    private PreparingShutdown(Undertow server, ConnectedUsers users, SocialLogin socials, LoadedCards loadedCards, ServerDatabase serverDatabase) {
        this.server = server;
        this.users = users;
        this.socials = socials;
        this.loadedCards = loadedCards;
        this.serverDatabase = serverDatabase;
    }

    @NotNull
    public static PreparingShutdown get() {
        if (instance == null) throw new IllegalStateException();
        return instance;
    }

    public static void setup(Undertow server, ConnectedUsers users, SocialLogin socials, LoadedCards loadedCards, ServerDatabase serverDatabase) {
        instance = new PreparingShutdown(server, users, socials, loadedCards, serverDatabase);
    }

    public synchronized void check() throws BaseCahHandler.CahException {
        if (value) throw new BaseCahHandler.CahException(Consts.ErrorCode.PREPARING_SHUTDOWN);
    }

    public synchronized void set(boolean set) {
        value = set;
        logger.log(Level.INFO, "Preparing for shutdown set to " + set);
    }

    /**
     * Shutdown the server if it can be done safely
     */
    public void tryShutdown() {
        if (value && users.canShutdown()) {
            shutdown();
        }
    }

    /**
     * Shutdown the server independently of its status
     */
    private void shutdown() {
        logger.log(Level.INFO, "Shutting down the server!");

        try {
            server.stop();
            socials.close();
            loadedCards.close();
            serverDatabase.close();
            System.exit(0);
        } catch (SQLException | IOException ex) {
            logger.log(Level.SEVERE, "Shutdown wasn't clear.", ex);
            System.exit(1);
        }
    }
}
