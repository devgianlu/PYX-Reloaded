package net.socialgamer.cah.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.data.QueuedMessage;
import net.socialgamer.cah.data.User;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class BaseLongPollHandler extends BaseCahHandler {
    /**
     * Minimum amount of time before timing out and returning a no-op, in nanoseconds.
     */
    private static final long TIMEOUT_BASE = TimeUnit.SECONDS.toNanos(20);

    /**
     * Randomness factor added to minimum timeout duration, in nanoseconds. The maximum timeout delay
     * will be TIMEOUT_BASE + TIMEOUT_RANDOMNESS - 1.
     */
    private static final double TIMEOUT_RANDOMNESS = TimeUnit.SECONDS.toNanos(5);

    /**
     * The maximum number of messages which will be returned to a client during a single poll
     * operation.
     */
    private static final int MAX_MESSAGES_PER_POLL = 20;

    /**
     * An amount of milliseconds to wait after being notified that the user has at least one message
     * to deliver, before we actually deliver messages. This will allow multiple messages that arrive
     * in close proximity to each other to actually be delivered in the same client request.
     */
    private static final int WAIT_FOR_MORE_DELAY = 50;

    @Override
    protected JsonElement handleRequest(@Nullable String op, @Nullable User user, Parameters params, HttpServerExchange exchange) {
        long start = System.nanoTime();
        // Pick a random timeout point between [TIMEOUT_BASE, TIMEOUT_BASE + TIMEOUT_RANDOMNESS)
        // nanoseconds from now.
        long end = (long) (start + TIMEOUT_BASE + Math.random() * TIMEOUT_RANDOMNESS);

        if (user == null) return new JsonObject();
        user.contactedServer();

        while (!(user.hasQueuedMessages()) && System.nanoTime() - end < 0) {
            try {
                user.waitForNewMessageNotification(TimeUnit.NANOSECONDS.toMillis(end - System.nanoTime()));
            } catch (InterruptedException ie) {
                // pass
            }
        }

        if (user.hasQueuedMessages()) {
            try {
                // Delay for a short while in case there will be other messages queued to be delivered.
                // This will certainly happen in some game states. We want to deliver as much to the client
                // in as few round-trips as possible while not waiting too long.
                Thread.sleep(WAIT_FOR_MORE_DELAY);
            } catch (final InterruptedException ie) {
                // pass
            }

            Collection<QueuedMessage> msgs = user.getNextQueuedMessages(MAX_MESSAGES_PER_POLL);
            // just in case...
            if (msgs.size() > 0) {
                JsonArray array = new JsonArray(msgs.size());
                for (QueuedMessage qm : msgs) array.add(qm.getData());
                return array;
            }
        }

        // otherwise, return that there is no new data
        JsonObject obj = new JsonObject();
        obj.addProperty(Constants.LongPollResponse.EVENT.toString(), Constants.LongPollEvent.NOOP.toString());
        obj.addProperty(Constants.LongPollResponse.TIMESTAMP.toString(), System.currentTimeMillis());
        return obj;
    }
}
