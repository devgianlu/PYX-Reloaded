package com.gianlu.pyxreloaded.data;


import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.accounts.UserAccount;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.paths.EventsPath;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.singletons.Sessions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class User {
    private final String nickname;
    private final String hostname;
    private final String sessionId;
    private final UserAccount account;
    private final List<Long> lastMessageTimes = Collections.synchronizedList(new LinkedList<Long>());
    private long lastReceivedEvents = 0;
    private long lastUserAction = 0;
    private Game currentGame;
    private boolean valid = true;
    private EventsPath.EventsSender eventsSender = null;
    private boolean waitingPong = false;

    /**
     * Create a new user.
     *
     * @param nickname  The user's nickname.
     * @param hostname  The user's Internet hostname (which will likely just be their IP address).
     * @param sessionId The unique ID of this session for this server singletons.
     */
    public User(String nickname, String hostname, String sessionId) {
        this(nickname, hostname, sessionId, null);
    }

    private User(String nickname, String hostname, @NotNull String sessionId, @Nullable UserAccount account) {
        this.nickname = nickname;
        this.hostname = hostname;
        this.sessionId = sessionId;
        this.account = account;
    }

    @NotNull
    public static User withAccount(UserAccount account, String hostname) {
        return new User(account.username, hostname, Sessions.generateNewId(), account);
    }

    public boolean isEmailVerified() {
        return account != null && account.emailVerified;
    }

    public boolean isAdmin() {
        return account != null && account.admin;
    }

    public void checkChatFlood() throws BaseCahHandler.CahException {
        if (getLastMessageTimes().size() >= Consts.CHAT_FLOOD_MESSAGE_COUNT) {
            long head = getLastMessageTimes().get(0);
            if (System.currentTimeMillis() - head < Consts.CHAT_FLOOD_TIME)
                throw new BaseCahHandler.CahException(Consts.ErrorCode.TOO_FAST);

            getLastMessageTimes().remove(0);
        }
    }

    /**
     * Enqueue a new message to be delivered to the user.
     *
     * @param message Message to enqueue.
     */
    public void enqueueMessage(QueuedMessage message) {
        if (eventsSender != null) eventsSender.enqueue(message);
    }

    /**
     * @return The user's session ID.
     */
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

    public void establishedEventsConnection(EventsPath.EventsSender sender) {
        this.eventsSender = sender;
    }

    @Nullable
    public EventsPath.EventsSender getEventsSender() {
        return eventsSender;
    }

    public void userDidSomething() {
        lastUserAction = System.currentTimeMillis();
        waitingPong = false;
    }

    /**
     * User received some events or responded to a ping
     */
    public void userReceivedEvents() {
        lastReceivedEvents = System.currentTimeMillis();
        waitingPong = false;
    }

    public long getLastUserAction() {
        return lastUserAction;
    }

    public long getLastReceivedEvents() {
        return lastReceivedEvents;
    }

    /**
     * Send a ping to the client
     */
    public void sendPing() {
        waitingPong = true;
        enqueueMessage(new QueuedMessage(QueuedMessage.MessageType.PING, new EventWrapper(Consts.Event.PING)));
    }

    public boolean isWaitingPong() {
        return waitingPong;
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
    @Nullable
    public Game getGame() {
        return currentGame;
    }

    /**
     * Marks a given game as this user's active game.
     * <p>
     * This should only be called from Game itself.
     *
     * @param game Game in which this user is playing.
     * @throws BaseCahHandler.CahException Thrown if this user is already in another game.
     */
    public void joinGame(Game game) throws BaseCahHandler.CahException {
        if (currentGame != null) throw new BaseCahHandler.CahException(Consts.ErrorCode.CANNOT_JOIN_ANOTHER_GAME);
        currentGame = game;
    }

    /**
     * Marks the user as no longer participating in a game.
     * <p>
     * This should only be called from Game itself.
     *
     * @param game Game from which to remove the user.
     */
    public void leaveGame(Game game) {
        if (currentGame == game) currentGame = null;
    }

    public List<Long> getLastMessageTimes() {
        return lastMessageTimes;
    }

    @Nullable
    public UserAccount getAccount() {
        return account;
    }

    public JsonWrapper toSmallJson() {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.UserData.NICKNAME, nickname);
        obj.add(Consts.UserData.IS_ADMIN, isAdmin());
        obj.add(Consts.UserData.HAS_ACCOUNT, isEmailVerified());
        if (account != null) obj.add(Consts.UserData.PICTURE, account.avatarUrl);
        return obj;
    }
}
