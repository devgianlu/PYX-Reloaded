package net.socialgamer.cah.data;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;


/**
 * A message to be queued for delivery to a client.
 *
 * @author Andy Janata (ajanata@socialgamer.net)
 */
public class QueuedMessage implements Comparable<QueuedMessage> {
    private final MessageType messageType;
    private final JsonObject data;

    /**
     * Create a new queued message.
     *
     * @param messageType Type of message to be queued. The type influences the priority in returning messages
     *                    to the client.
     * @param data        The data of the message to be queued.
     */
    public QueuedMessage(MessageType messageType, JsonObject data) {
        this.messageType = messageType;
        this.data = data;
    }

    /**
     * @return The data in the message.
     */
    public JsonObject getData() {
        return data;
    }

    /**
     * This is not guaranteed to be consistent with .equals() since we do not care about the data for
     * ordering.
     */
    @Override
    public int compareTo(@NotNull QueuedMessage qm) {
        return this.messageType.getWeight() - qm.messageType.getWeight();
    }

    @Override
    public String toString() {
        return messageType.toString() + "_" + data.toString();
    }

    /**
     * Types of messages that can be queued. The numerical value is the priority that this message
     * should be delivered (lower = more important) compared to other queued messages.
     */
    public enum MessageType {
        KICKED(1), PLAYER_EVENT(2), GAME_EVENT(3), GAME_PLAYER_EVENT(4), CHAT(5);

        private final int weight;

        MessageType(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }
}
