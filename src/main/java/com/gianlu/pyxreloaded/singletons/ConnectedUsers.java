package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.EventWrapper;
import com.gianlu.pyxreloaded.data.QueuedMessage;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.data.accounts.UserAccount;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;


public final class ConnectedUsers {
    private static final long PING_TIMEOUT = TimeUnit.SECONDS.toMillis(90);
    private static final long IDLE_TIMEOUT = TimeUnit.MINUTES.toMillis(60);
    private static final Logger logger = Logger.getLogger(ConnectedUsers.class);
    private final int maxUsers;
    private final Map<String, User> users = new HashMap<>();
    private final int chatFloodCount;
    private final int chatFloodTime;
    private final BanList banList;

    public ConnectedUsers(BanList banList, Preferences preferences) {
        this.banList = banList;
        this.maxUsers = preferences.getInt("maxUsers", 400);
        this.chatFloodCount = preferences.getInt("chat/floodCount", 4);
        this.chatFloodTime = preferences.getInt("chat/floodTime", 30) * 1000;
    }

    public void checkChatFlood(User user) throws BaseCahHandler.CahException {
        if (user.getLastMessageTimes().size() >= chatFloodCount) {
            long head = user.getLastMessageTimes().get(0);
            if (System.currentTimeMillis() - head < chatFloodTime)
                throw new BaseCahHandler.CahException(Consts.ErrorCode.TOO_FAST);

            user.getLastMessageTimes().remove(0);
        }
    }

    /**
     * @return true, if the message was a chat command; false, otherwise.
     */
    public boolean runChatCommand(User user, String command) throws BaseCahHandler.CahException {
        if (!user.isAdmin()) throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_ADMIN);

        if (!command.startsWith("/")) return false;
        String[] params = command.substring(1).split("\\s");

        Consts.ChatCommand cmd = Consts.ChatCommand.parse(params[0]);
        if (cmd == null) return false;

        switch (cmd) {
            case BAN:
                User banTarget = getUser(params[1]);
                if (banTarget == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.NO_SUCH_USER);
                banUser(banTarget);
                return true;
            case KICK:
                User kickTarget = getUser(params[1]);
                if (kickTarget == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.NO_SUCH_USER);
                banUser(kickTarget);
                return true;
            default:
                return false;
        }
    }

    public void banUser(@NotNull User user) {
        banList.add(user.getHostname());
        user.enqueueMessage(new QueuedMessage(QueuedMessage.MessageType.KICKED, new EventWrapper(Consts.Event.BANNED)));
        removeUser(user, Consts.DisconnectReason.BANNED);
    }

    public void kickUser(@NotNull User user) {
        user.enqueueMessage(new QueuedMessage(QueuedMessage.MessageType.KICKED, new EventWrapper(Consts.Event.KICKED)));
        removeUser(user, Consts.DisconnectReason.KICKED);
    }

    /**
     * @param userName User name to check.
     * @return True if {@code userName} is a connected user.
     */
    public boolean hasUser(String userName) {
        return users.containsKey(userName.toLowerCase());
    }

    /**
     * Checks to see if the specified {@code user} is allowed to connect, and if so, add the user,
     * as an atomic operation.
     *
     * @param user User to add. {@code getNickname()} is used to determine the nickname.
     */
    @Nullable
    public User checkAndAdd(@NotNull User user) throws BaseCahHandler.CahException {
        PreparingShutdown.get().check();

        synchronized (users) {
            if (hasUser(user.getNickname())) {
                UserAccount account = user.getAccount();
                if (account != null) {
                    User registeredUser = users.get(account.username);
                    if (registeredUser != null) {
                        registeredUser.userDidSomething();
                        return registeredUser;
                    } else {
                        return null;
                    }
                } else {
                    logger.info(String.format("Rejecting existing username %s from %s", user.toString(), user.getHostname()));
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.NICK_IN_USE);
                }
            } else if (users.size() >= maxUsers && !user.isAdmin()) {
                logger.warn(String.format("Rejecting user %s due to too many users (%d >= %d)", user.toString(), users.size(), maxUsers));
                throw new BaseCahHandler.CahException(Consts.ErrorCode.TOO_MANY_USERS);
            } else {
                logger.info(String.format("New user %s from %s (admin=%b)", user.toString(), user.getHostname(), user.isAdmin()));
                users.put(user.getNickname().toLowerCase(), user);
            }

            return null;
        }
    }

    /**
     * Remove a user from the user list, and mark them as invalid so the next time they make a request
     * they can be informed.
     *
     * @param user   User to remove.
     * @param reason Reason the user is being removed.
     */
    public void removeUser(User user, Consts.DisconnectReason reason) {
        synchronized (users) {
            if (users.containsKey(user.getNickname())) {
                logger.info(String.format("Removing user %s because %s", user.toString(), reason));
                user.noLongerValid();
                users.remove(user.getNickname().toLowerCase());

                PreparingShutdown.get().tryShutdown(); // This won't allow the LogoutHandler to send the response but we don't really care about the last user to leave the server
            }
        }
    }

    /**
     * Get the User for the specified nickname, or null if no such user exists.
     *
     * @param nickname user's nickname
     * @return User, or null.
     */
    @Nullable
    public User getUser(String nickname) {
        return users.get(nickname.toLowerCase());
    }

    /**
     * Check for any users that have not communicated with the server within the ping timeout delay,
     * and remove users which have not so communicated. Also remove clients which are still connected,
     * but have not actually done anything for a long time.
     */
    public void checkForPingAndIdleTimeouts() {
        final Map<User, Consts.DisconnectReason> removedUsers = new HashMap<>();
        synchronized (users) {
            Iterator<User> iterator = users.values().iterator();
            while (iterator.hasNext()) {
                User user = iterator.next();

                Consts.DisconnectReason reason = null;
                if (System.currentTimeMillis() - user.getLastReceivedEvents() > PING_TIMEOUT) {
                    if (user.isWaitingPong()) reason = Consts.DisconnectReason.PING_TIMEOUT;
                    else user.sendPing();
                } else if (!user.isAdmin() && System.currentTimeMillis() - user.getLastUserAction() > IDLE_TIMEOUT) {
                    reason = Consts.DisconnectReason.IDLE_TIMEOUT;
                }

                if (reason != null) {
                    removedUsers.put(user, reason); // Changes are reflected to the map
                    iterator.remove();
                }
            }
        }

        // Do this later to not keep users locked
        for (Entry<User, Consts.DisconnectReason> entry : removedUsers.entrySet()) {
            try {
                entry.getKey().noLongerValid();
                logger.info(String.format("Automatically kicking user %s due to %s", entry.getKey(), entry.getValue()));
            } catch (Exception ex) {
                logger.error("Unable to remove pinged-out user", ex);
            }
        }

        PreparingShutdown.get().tryShutdown();
    }

    /**
     * Broadcast a message to all connected players.
     *
     * @param type Type of message to broadcast. This determines the order the messages are returned by
     *             priority.
     * @param ev   Message data to broadcast.
     */
    public void broadcastToAll(QueuedMessage.MessageType type, EventWrapper ev) {
        broadcastToList(users.values(), type, ev);
    }

    /**
     * Broadcast a message to a specified subset of connected players.
     *
     * @param broadcastTo List of users to broadcast the message to.
     * @param type        Type of message to broadcast. This determines the order the messages are returned by
     *                    priority.
     * @param ev          Message data to broadcast.
     */
    public void broadcastToList(Collection<User> broadcastTo, QueuedMessage.MessageType type, EventWrapper ev) {
        for (User user : broadcastTo) user.enqueueMessage(new QueuedMessage(type, ev));
    }

    /**
     * @return A copy of the list of connected users.
     */
    public Collection<User> getUsers() {
        synchronized (users) {
            return new ArrayList<>(users.values());
        }
    }

    /**
     * @return Whether the server can be shutdown safely
     */
    public boolean canShutdown() {
        return users.isEmpty();
    }

    public void preShutdown() {
        synchronized (users) {
            for (User user : users.values()) {
                user.enqueueMessage(new QueuedMessage(QueuedMessage.MessageType.SERVER, new EventWrapper(Consts.Event.SERVER_SHUTDOWN)));
                removeUser(user, Consts.DisconnectReason.SERVER_SHUTDOWN);
            }
        }
    }
}
