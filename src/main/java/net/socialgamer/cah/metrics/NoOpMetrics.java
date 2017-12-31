package net.socialgamer.cah.metrics;

import net.socialgamer.cah.data.BlackCard;
import net.socialgamer.cah.data.CardSet;
import net.socialgamer.cah.data.WhiteCard;
import org.apache.log4j.Logger;

import java.util.Arrays;
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
    public void serverStart(String startupId) {
        LOG.trace(String.format("serverStarted(%s)", startupId));
    }

    @Override
    public void userConnect(String persistentId, String sessionId, String agentName, String agentType, String agentOs, String agentLanguage) {
        LOG.trace(String.format("newUser(%s, %s, %s, %s, %s, %s)", persistentId, sessionId, agentName, agentType, agentOs, agentLanguage));
    }

    @Override
    public void userDisconnect(String sessionId) {
        LOG.trace(String.format("userDisconnect(%s)", sessionId));
    }

    @Override
    public void gameStart(String gameId, Collection<CardSet> decks, int blanks, int maxPlayers, int scoreGoal, boolean hasPassword) {
        LOG.trace(String.format("gameStart(%s, %s, %d, %d, %d, %s)", gameId, Arrays.toString(decks.toArray()), blanks, maxPlayers, scoreGoal, hasPassword));
    }

    @Override
    public void roundComplete(String gameId, String roundId, String judgeSessionId, String winnerSessionId, BlackCard blackCard, Map<String, List<WhiteCard>> cards) {
        LOG.trace(String.format("roundJudged(%s, %s, %s, %s, %s, %s)", gameId, roundId, judgeSessionId, winnerSessionId, blackCard, cards));
    }
}
