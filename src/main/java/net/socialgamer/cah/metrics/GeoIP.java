package net.socialgamer.cah.metrics;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

/**
 * Wrapper for GeoLite2.
 *
 * @author Andy Janata (ajanata@socialgamer.net)
 */
public class GeoIP {
    private static final Logger LOG = Logger.getLogger(GeoIP.class);
    private final Properties properties;
    private DatabaseReader reader;
    private boolean initialized = false;

    public GeoIP(Properties properties) {
        this.properties = properties;
    }

    /**
     * Look up the given IP address in the GeoIP database.
     *
     * @param addr The address to look up.
     * @return Information about the address, or {@code null} if an error occurred or a GeoIP
     * database was not configured.
     */
    public CityResponse getInfo(final InetAddress addr) {
        try {
            final DatabaseReader r = getReader();
            if (null != r) {
                return r.city(addr);
            }
        } catch (IOException | GeoIp2Exception e) {
            // don't include the stack trace, it throws for addresses it doesn't have in its db...
            LOG.error(String.format("Unable to look up %s: %s", addr, e.getMessage()));
        }
        return null;
    }

    private DatabaseReader getReader() {
        if (initialized) {
            return reader;
        }
        return makeReader();
    }

    private synchronized DatabaseReader makeReader() {
        LOG.info("Attempting to create GeoIP database reader");
        initialized = true;
        if (reader != null) {
            return reader;
        }

        final String dbPath = properties.getProperty("geoip.db");
        if (StringUtils.isNotBlank(dbPath)) {
            final File db = new File(dbPath);
            try {
                reader = new DatabaseReader.Builder(db).withCache(new CHMCache()).build();
            } catch (final IOException e) {
                LOG.error("Unable to create database reader", e);
                reader = null;
            }
        } else {
            reader = null;
        }
        return reader;
    }
}
