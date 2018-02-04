package net.socialgamer.cah.servlets;

import com.google.gson.JsonArray;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.Cookies;
import io.undertow.util.Headers;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import net.socialgamer.cah.Consts;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.QueuedMessage;
import net.socialgamer.cah.data.User;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

public class EventsHandler implements WebSocketConnectionCallback {
    /**
     * An amount of milliseconds to wait after being notified that the user has at least one message
     * to deliver, before we actually deliver messages. This will allow multiple messages that arrive
     * in close proximity to each other to actually be delivered in the same client request.
     */
    private static final int WAIT_FOR_MORE_DELAY = 100;

    /**
     * The maximum number of messages which will be returned to a client during a single poll
     * operation.
     */
    private static final int MAX_MESSAGES_PER_POLL = 20;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private static Map<String, Cookie> getRequestCookies(WebSocketHttpExchange exchange) {
        return Cookies.parseRequestCookies(200, false, exchange.getRequestHeaders().get(Headers.COOKIE_STRING));
    }

    private static void sendConnectionError(WebSocketHttpExchange exchange, WebSocketChannel channel, JsonWrapper error) {
        WebSockets.sendText(error.toString(), channel, null);
        exchange.endExchange();
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        Cookie sid = getRequestCookies(exchange).get("PYX-Session");

        User user;
        if (sid == null || (user = Sessions.getUser(sid.getValue())) == null) {
            sendConnectionError(exchange, channel, new JsonWrapper(Consts.ErrorCode.NOT_REGISTERED));
        } else if (!user.isValid()) {
            sendConnectionError(exchange, channel, new JsonWrapper(Consts.ErrorCode.SESSION_EXPIRED));
        } else {
            user.establishedEventsConnection(new EventsSender(user, channel));
        }
    }

    /**
     * Class for handling outgoing event messages, does not receive anything
     */
    public class EventsSender {
        private final User user;
        private final WebSocketChannel channel;
        private final PriorityBlockingQueue<QueuedMessage> messages = new PriorityBlockingQueue<>();

        EventsSender(User user, WebSocketChannel channel) {
            this.user = user;
            this.channel = channel;
        }

        public void enqueue(QueuedMessage message) {
            synchronized (messages) {
                messages.add(message);
            }

            executorService.execute(new EventTask());
        }

        private void handleIoException(IOException ex) {
            if (ex instanceof ClosedChannelException) {
                user.noLongerValid();
            }
        }

        private class EventTask implements Runnable {
            @Override
            public void run() {
                try {
                    Thread.sleep(WAIT_FOR_MORE_DELAY);
                } catch (InterruptedException ignored) {
                }

                ArrayList<QueuedMessage> toSend = new ArrayList<>(MAX_MESSAGES_PER_POLL);
                synchronized (messages) {
                    messages.drainTo(toSend, MAX_MESSAGES_PER_POLL);
                }

                JsonArray array = new JsonArray(toSend.size());
                for (QueuedMessage message : toSend)
                    array.add(message.getData().obj());

                try {
                    WebSockets.sendTextBlocking(new JsonWrapper(Consts.GeneralKeys.EVENTS, array).obj().toString(), channel);
                    user.userReceivedEvents();
                } catch (IOException ex) {
                    handleIoException(ex);
                }
            }
        }
    }
}
