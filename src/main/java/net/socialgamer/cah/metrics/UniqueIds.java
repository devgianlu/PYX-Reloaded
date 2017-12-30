package net.socialgamer.cah.metrics;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;


/**
 * <p>A provider for server-wide unique IDs, which will not conflict with another server or if this
 * server restarts.</p>
 * <p>Unique IDs are composed of the following elements, separated by underscores:
 * <ul><li>server hostname (or a random UUID if the hostname cannot be determined)</li>
 * <li>the number of milliseconds since the UNIX epoch at the time the server was started</li>
 * <li>a long interger that strictly increases each time an ID is requested</li></ul></p>
 *
 * @author Andy Janata (ajanata@socialgamer.net)
 */
public class UniqueIds {
    private static final Logger LOG = Logger.getLogger(UniqueIds.class);
    private static final String hostname;

    static {
        String hn;
        try {
            hn = InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            hn = UUID.randomUUID().toString();
            LOG.warn(String.format("Unable to determine hostname, using %s instead.", hn));
        }
        hostname = hn;
    }

    private final AtomicLong counter = new AtomicLong(0);
    private final Date serverStarted;

    public UniqueIds(Date serverStarted) {
        this.serverStarted = serverStarted;
    }

    public String get() {
        return String.format("%s_%d_%d", hostname, serverStarted.getTime(), counter.getAndIncrement());
    }
}
