package com.gianlu.pyxreloaded.cards;

import com.gianlu.pyxreloaded.singletons.LoadedCards;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PyxCardSet extends CardSet {
    private final Set<PyxBlackCard> blackCards;
    private final Set<PyxWhiteCard> whiteCards;
    private final int id;
    private final String name;
    private final String description;
    private final boolean active;
    private final boolean base_deck;
    private final int weight;

    public PyxCardSet(ResultSet resultSet) throws SQLException {
        id = resultSet.getInt("id");
        name = resultSet.getString("name");
        active = resultSet.getInt("active") == 1;
        base_deck = resultSet.getInt("base_deck") == 1;
        description = resultSet.getString("description");
        weight = resultSet.getInt("weight");

        blackCards = new HashSet<>();
        whiteCards = new HashSet<>();
    }

    public static List<PyxCardSet> loadCardSets(LoadedCards loadedCards, Set<Integer> ids) {
        List<PyxCardSet> sets = new ArrayList<>();
        for (PyxCardSet set : loadedCards.getLoadedSets())
            if (ids.contains(set.id)) sets.add(set);

        return sets;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Set<PyxBlackCard> getBlackCards() {
        return blackCards;
    }

    @Override
    public Set<PyxWhiteCard> getWhiteCards() {
        return whiteCards;
    }

    @Override
    public boolean isBaseDeck() {
        return base_deck;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getWeight() {
        return weight;
    }
}
