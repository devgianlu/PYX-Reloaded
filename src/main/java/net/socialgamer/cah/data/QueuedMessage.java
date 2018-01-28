package net.socialgamer.cah.data;

import net.socialgamer.cah.EventWrapper;
import org.jetbrains.annotations.NotNull;


/**
 * A message to be queued for delivery to a client.
 *
 * @author Andy Janata (ajanata@socialgamer.net)
 */
public class QueuedMessage implements Comparable<QueuedMessage> {
    private final MessageType messageType;
    private final EventWrapper ev;

    /**
     * Create a new queued message.
     *
     * @param messageType Type of message to be queued. The type influences the priority in returning messages
     *                    to the client.
     * @param ev          The data of the message to be queued.
     */
    public QueuedMessage(MessageType messageType, EventWrapper ev) {
        this.messageType = messageType;
        this.ev = ev;
    }

    /**
     * @return The data in the message.
     */
    public EventWrapper getData() {
        return ev;
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
        return messageType.toString() + "_" + ev.toString();
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
