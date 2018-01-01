package net.socialgamer.cah.data;


import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * A user connected to the server.
 *
 * @author Andy Janata (ajanata@socialgamer.net)
 */
public class User {
    private final String nickname;
    private final PriorityBlockingQueue<QueuedMessage> queuedMessages;
    private final Object queuedMessageSynchronization = new Object();
    private final String hostname;
    private final boolean isAdmin;
    private final String persistentId;
    private final String sessionId;
    private final List<Long> lastMessageTimes = Collections.synchronizedList(new LinkedList<Long>());
    private long lastHeardFrom = 0;
    private long lastUserAction = 0;
    private Game currentGame;
    /**
     * Reset when this user object is no longer valid, most likely because it pinged out.
     */
    private boolean valid = true;

    /**
     * Create a new user.
     *
     * @param nickname     The user's nickname.
     * @param hostname     The user's Internet hostname (which will likely just be their IP address).
     * @param isAdmin      Whether this user is an admin.
     * @param persistentId This user's persistent (cross-session) ID.
     * @param sessionId    The unique ID of this session for this server instance.
     */
    public User(String nickname, String hostname, boolean isAdmin, String persistentId, String sessionId) {
        this.nickname = nickname;
        this.hostname = hostname;
        this.isAdmin = isAdmin;
        this.persistentId = persistentId;
        this.sessionId = sessionId;
        this.queuedMessages = new PriorityBlockingQueue<>();
    }

    /**
     * Enqueue a new message to be delivered to the user.
     *
     * @param message Message to enqueue.
     */
    public void enqueueMessage(final QueuedMessage message) {
        synchronized (queuedMessageSynchronization) {
            queuedMessages.add(message);
            queuedMessageSynchronization.notifyAll();
        }
    }

    /**
     * @return True if the user has any messages queued to be delivered.
     */
    public boolean hasQueuedMessages() {
        return !queuedMessages.isEmpty();
    }

    /**
     * Wait for a new message to be queued.
     *
     * @param timeout Maximum time to wait in milliseconds.
     * @throws InterruptedException
     * @see java.lang.Object#wait(long timeout)
     */
    public void waitForNewMessageNotification(final long timeout) throws InterruptedException {
        if (timeout > 0) {
            synchronized (queuedMessageSynchronization) {
                queuedMessageSynchronization.wait(timeout);
            }
        }
    }

    /**
     * This method blocks if there are no messages to return, or perhaps if the queue is being
     * modified by another thread.
     *
     * @return The next message in the queue, or null if interrupted.
     */
    @Nullable
    public QueuedMessage getNextQueuedMessage() {
        try {
            return queuedMessages.take();
        } catch (final InterruptedException ie) {
            return null;
        }
    }

    /**
     * @param maxElements Maximum number of messages to return.
     * @return The next {@code maxElements} messages queued for this user.
     */
    public Collection<QueuedMessage> getNextQueuedMessages(final int maxElements) {
        final ArrayList<QueuedMessage> c = new ArrayList<>(maxElements);
        synchronized (queuedMessageSynchronization) {
            queuedMessages.drainTo(c, maxElements);
        }
        c.trimToSize();
        return c;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPersistentId() {
        return persistentId;
    }

    /**
     * @return The user's nickname.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @return The user's Internet hostname, or IP address.
     */
    public String getHostname() {
        return hostname;
    }

    @Override
    public String toString() {
        return getNickname();
    }

    /**
     * Update the timestamp that we have last heard from this user to the current time.
     */
    public void contactedServer() {
        lastHeardFrom = System.nanoTime();
    }

    /**
     * @return The time the user was last heard from, in nanoseconds.
     */
    public long getLastHeardFrom() {
        return lastHeardFrom;
    }

    public void userDidSomething() {
        lastUserAction = System.nanoTime();
    }

    public long getLastUserAction() {
        return lastUserAction;
    }

    /**
     * @return False when this user object is no longer valid, probably because it pinged out.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Mark this user as no longer valid, probably because they pinged out.
     */
    public void noLongerValid() {
        if (currentGame != null) currentGame.removePlayer(this);
        valid = false;
    }

    /**
     * @return The current game in which this user is participating.
     */
    public Game getGame() {
        return currentGame;
    }

    /**
     * Marks a given game as this user's active game.
     * <p>
     * This should only be called from Game itself.
     *
     * @param game Game in which this user is playing.
     * @throws IllegalStateException Thrown if this user is already in another game.
     */
    void joinGame(final Game game) throws IllegalStateException {
        if (currentGame != null) throw new IllegalStateException("User is already in a game.");
        currentGame = game;
    }

    /**
     * Marks the user as no longer participating in a game.
     * <p>
     * This should only be called from Game itself.
     *
     * @param game Game from which to remove the user.
     */
    void leaveGame(final Game game) {
        if (currentGame == game) currentGame = null;
    }

    public List<Long> getLastMessageTimes() {
        return lastMessageTimes;
    }

    public abstract static class Factory {
        public abstract User create(String nickname, String hostname, boolean isAdmin, String persistentId);
    }
}
