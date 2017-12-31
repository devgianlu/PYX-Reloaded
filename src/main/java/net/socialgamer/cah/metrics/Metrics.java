package net.socialgamer.cah.metrics;

import net.socialgamer.cah.data.BlackCard;
import net.socialgamer.cah.data.CardSet;
import net.socialgamer.cah.data.WhiteCard;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Collect metrics about card plays, and correlate them with (anonymized) user data.
 *
 * @author Andy Janata (ajanata@socialgamer.net)
 */
public interface Metrics {
    void shutdown();

    void serverStart(String startupId);

    void userConnect(String persistentId, String sessionId, String agentName, String agentType, String agentOs, String agentLanguage);

    void userDisconnect(String sessionId);

    void roundComplete(String gameId, String roundId, String judgeSessionId, String winnerSessionId, BlackCard blackCard, Map<String, List<WhiteCard>> cards);

    void gameStart(String gameId, Collection<CardSet> decks, int blanks, int maxPlayers, int scoreGoal, boolean hasPassword);
}
