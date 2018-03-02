package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.cards.PyxBlackCard;
import com.gianlu.pyxreloaded.cards.PyxCardSet;
import com.gianlu.pyxreloaded.cards.PyxWhiteCard;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class LoadedCards {
    private static final Logger logger = Logger.getLogger(LoadedCards.class.getSimpleName());
    private final Set<PyxCardSet> sets = new HashSet<>();
    private final Set<PyxWhiteCard> whiteCards = new HashSet<>();
    private final Set<PyxBlackCard> blackCards = new HashSet<>();
    private final Connection conn;

    public LoadedCards(Preferences preferences) throws SQLException {
        conn = DriverManager.getConnection(preferences.getString("pyxDbUrl", "jdbc:sqlite:pyx.sqlite"));
        loadWhiteCards();
        loadBlackCards();
        loadSets();
        logger.info("Successfully loaded " + sets.size() + " card sets, " + whiteCards.size() + " white cards and " + blackCards.size() + " black cards.");
    }

    @NotNull
    public Set<PyxCardSet> getLoadedSets() {
        return sets;
    }

    private void loadWhiteCards() throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM white_cards")) {

            synchronized (whiteCards) {
                whiteCards.clear();
                while (resultSet.next()) {
                    whiteCards.add(new PyxWhiteCard(resultSet));
                }
            }
        }
    }

    private void loadBlackCards() throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM black_cards")) {

            synchronized (blackCards) {
                blackCards.clear();
                while (resultSet.next()) {
                    blackCards.add(new PyxBlackCard(resultSet));
                }
            }
        }
    }

    private Set<Integer> getBlackCardIdsFor(int setId) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT black_card_id FROM card_set_black_card WHERE card_set_id='" + setId + "'")) {

            Set<Integer> ids = new HashSet<>();
            while (resultSet.next()) ids.add(resultSet.getInt(1));
            return ids;
        }
    }

    private Set<Integer> getWhiteCardIdsFor(int setId) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT white_card_id FROM card_set_white_card WHERE card_set_id='" + setId + "'")) {

            Set<Integer> ids = new HashSet<>();
            while (resultSet.next()) ids.add(resultSet.getInt(1));
            return ids;
        }
    }

    private void loadSets() throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM card_set ORDER BY weight, id")) {

            synchronized (sets) {
                sets.clear();
                while (resultSet.next()) {
                    PyxCardSet set = new PyxCardSet(resultSet);

                    Set<Integer> blackCardIds = getBlackCardIdsFor(set.getId());
                    for (PyxBlackCard blackCard : blackCards)
                        if (blackCardIds.contains(blackCard.getId()))
                            set.getBlackCards().add(blackCard);


                    Set<Integer> whiteCardIds = getWhiteCardIdsFor(set.getId());
                    for (PyxWhiteCard whiteCard : whiteCards)
                        if (whiteCardIds.contains(whiteCard.getId()))
                            set.getWhiteCards().add(whiteCard);

                    sets.add(set);
                }
            }
        }
    }
}
