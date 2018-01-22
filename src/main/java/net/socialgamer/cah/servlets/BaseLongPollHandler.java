package net.socialgamer.cah.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Utils;
import net.socialgamer.cah.data.QueuedMessage;
import net.socialgamer.cah.data.User;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class BaseLongPollHandler extends BaseCahHandler {
    /**
     * Minimum amount of time before timing out and returning a no-op, in milliseconds.
     */
    private static final long TIMEOUT_BASE = TimeUnit.SECONDS.toMillis(20);

    /**
     * Randomness factor added to minimum timeout duration, in milliseconds. The maximum timeout delay
     * will be TIMEOUT_BASE + TIMEOUT_RANDOMNESS - 1.
     */
    private static final double TIMEOUT_RANDOMNESS = TimeUnit.SECONDS.toMillis(5);

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
    private static final int WAIT_FOR_MORE_DELAY = 100;

    @Override
    protected JsonElement handleRequest(@Nullable String op, @Nullable User user, Parameters params, HttpServerExchange exchange) {
        if (user == null) return new JsonObject();
        user.contactedServer();

        if (!user.hasQueuedMessages()) {
            try {
                user.waitForNewMessageNotification((long) (TIMEOUT_BASE + Math.random() * TIMEOUT_RANDOMNESS));
            } catch (InterruptedException ignored) {
            }
        }

        if (user.hasQueuedMessages()) {
            try {
                Thread.sleep(WAIT_FOR_MORE_DELAY);
            } catch (InterruptedException ignored) {
            }

            Collection<QueuedMessage> msgs = user.getNextQueuedMessages(MAX_MESSAGES_PER_POLL);
            if (msgs.size() > 0) {
                JsonArray array = new JsonArray(msgs.size());
                for (QueuedMessage qm : msgs) array.add(qm.getData());
                return Utils.singletonJsonObject(Constants.LongPollResponse.EVENT.toString(), array);
            }
        }

        JsonObject obj = new JsonObject();
        obj.addProperty(Constants.LongPollResponse.TIMESTAMP.toString(), System.currentTimeMillis());
        obj.addProperty(Constants.LongPollResponse.EVENT.toString(), Constants.LongPollEvent.NOOP.toString());
        return obj;
    }
}
