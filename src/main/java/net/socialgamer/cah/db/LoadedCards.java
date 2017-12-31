package net.socialgamer.cah.db;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class LoadedCards {
    private static final Logger logger = Logger.getLogger(LoadedCards.class.getSimpleName());
    private static final Set<PyxCardSet> sets = new HashSet<>();
    private static final Set<PyxWhiteCard> whiteCards = new HashSet<>();
    private static final Set<PyxBlackCard> blackCards = new HashSet<>();
    private static Connection conn;

    public static void load(String path) throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + path);
        loadWhiteCards();
        loadBlackCards();
        loadSets();
        logger.info("Successfully loaded " + sets.size() + " card sets, " + whiteCards.size() + " white cards and " + blackCards.size() + " black cards.");
    }

    public static Set<PyxCardSet> getLoadedSets() {
        return sets;
    }

    private static void loadWhiteCards() throws SQLException {
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

    private static void loadBlackCards() throws SQLException {
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

    private static Set<Integer> getBlackCardIdsFor(int setId) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT black_card_id FROM card_set_black_card WHERE card_set_id=" + setId)) {

            Set<Integer> ids = new HashSet<>();
            while (resultSet.next()) ids.add(resultSet.getInt(1));
            return ids;
        }
    }

    private static Set<Integer> getWhiteCardIdsFor(int setId) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT white_card_id FROM card_set_white_card WHERE card_set_id=" + setId)) {

            Set<Integer> ids = new HashSet<>();
            while (resultSet.next()) ids.add(resultSet.getInt(1));
            return ids;
        }
    }

    private static void loadSets() throws SQLException {
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
