package net.socialgamer.cah.metrics;

import com.maxmind.geoip2.model.CityResponse;
import net.socialgamer.cah.data.BlackCard;
import net.socialgamer.cah.data.CardSet;
import net.socialgamer.cah.data.WhiteCard;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * A no-op metrics implementation. All data are logged at TRACE then discarded.
 *
 * @author Andy Janata (ajanata@socialgamer.net)
 */
public class NoOpMetrics implements Metrics {

    private static final Logger LOG = Logger.getLogger(NoOpMetrics.class);

    @Override
    public void shutdown() {
        // nothing to do
    }

    @Override
    public void serverStart(final String startupId) {
        LOG.trace(String.format("serverStarted(%s)", startupId));
    }

    @Override
    public void userConnect(final String persistentId, final String sessionId, final CityResponse geoIp,
                            final String agentName, final String agentType, final String agentOs,
                            final String agentLanguage) {
        LOG.trace(String.format("newUser(%s, %s, %s, %s, %s, %s, %s)", persistentId, sessionId, geoIp,
                agentName, agentType, agentOs, agentLanguage));
    }

    @Override
    public void userDisconnect(final String sessionId) {
        LOG.trace(String.format("userDisconnect(%s)", sessionId));
    }

    @Override
    public void gameStart(final String gameId, final Collection<CardSet> decks, final int blanks,
                          final int maxPlayers, final int scoreGoal, final boolean hasPassword) {
        LOG.trace(String.format("gameStart(%s, %s, %d, %d, %d, %s)", gameId, decks.toArray(), blanks,
                maxPlayers, scoreGoal, hasPassword));
    }

    @Override
    public void roundComplete(final String gameId, final String roundId, final String judgeSessionId,
                              final String winnerSessionId, final BlackCard blackCard,
                              final Map<String, List<WhiteCard>> cards) {
        LOG.trace(String.format("roundJudged(%s, %s, %s, %s, %s, %s)", gameId, roundId, judgeSessionId,
                winnerSessionId, blackCard, cards));
    }
}
