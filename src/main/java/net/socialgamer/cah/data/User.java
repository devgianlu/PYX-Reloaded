package net.socialgamer.cah.data;


import net.socialgamer.cah.Constants;
import net.socialgamer.cah.servlets.BaseCahHandler;

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
    private final Object queuedMessageLock = new Object();
    private final String hostname;
    private final String sessionId;
    private final boolean admin;
    private final List<Long> lastMessageTimes = Collections.synchronizedList(new LinkedList<Long>());
    private long lastHeardFrom = 0;
    private long lastUserAction = 0;
    private Game currentGame;
    private boolean valid = true;

    /**
     * Create a new user.
     *
     * @param nickname  The user's nickname.
     * @param hostname  The user's Internet hostname (which will likely just be their IP address).
     * @param sessionId The unique ID of this session for this server instance.
     */
    public User(String nickname, String hostname, String sessionId, boolean admin) {
        this.nickname = nickname;
        this.hostname = hostname;
        this.sessionId = sessionId;
        this.admin = admin;
        this.queuedMessages = new PriorityBlockingQueue<>();
    }

    public boolean isAdmin() {
        return admin;
    }

    public void checkChatFlood() throws BaseCahHandler.CahException {
        if (getLastMessageTimes().size() >= Constants.CHAT_FLOOD_MESSAGE_COUNT) {
            long head = getLastMessageTimes().get(0);
            if (System.currentTimeMillis() - head < Constants.CHAT_FLOOD_TIME)
                throw new BaseCahHandler.CahException(Constants.ErrorCode.TOO_FAST);

            getLastMessageTimes().remove(0);
        }
    }

    /**
     * Enqueue a new message to be delivered to the user.
     *
     * @param message Message to enqueue.
     */
    public void enqueueMessage(QueuedMessage message) {
        synchronized (queuedMessageLock) {
            queuedMessages.add(message);
            queuedMessageLock.notifyAll();
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
     * @throws InterruptedException should do that
     * @see java.lang.Object#wait(long timeout)
     */
    public void waitForNewMessageNotification(long timeout) throws InterruptedException {
        if (timeout > 0) {
            synchronized (queuedMessageLock) {
                queuedMessageLock.wait(timeout);
            }
        }
    }

    /**
     * @param maxElements Maximum number of messages to return.
     * @return The next {@code maxElements} messages queued for this user.
     */
    public Collection<QueuedMessage> getNextQueuedMessages(int maxElements) {
        ArrayList<QueuedMessage> c = new ArrayList<>(maxElements);
        synchronized (queuedMessageLock) {
            queuedMessages.drainTo(c, maxElements);
        }

        c.trimToSize();
        return c;
    }

    public String getSessionId() {
        return sessionId;
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
        lastHeardFrom = System.currentTimeMillis();
    }

    /**
     * @return The time the user was last heard from, in nanoseconds.
     */
    public long getLastHeardFrom() {
        return lastHeardFrom;
    }

    public void userDidSomething() {
        lastUserAction = System.currentTimeMillis();
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
    void joinGame(Game game) throws IllegalStateException {
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
    void leaveGame(Game game) {
        if (currentGame == game) currentGame = null;
    }

    public List<Long> getLastMessageTimes() {
        return lastMessageTimes;
    }
}
