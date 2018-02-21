package com.gianlu.pyxreloaded.cardcast;

import com.gianlu.pyxreloaded.cards.WhiteCard;


public class CardcastWhiteCard extends WhiteCard {
    private final int id;
    private final String text;
    private final String deckId;

    CardcastWhiteCard(final int id, final String text, final String deckId) {
        this.id = id;
        this.text = text;
        this.deckId = deckId;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getWatermark() {
        return deckId;
    }

    @Override
    public boolean isWriteIn() {
        return false;
    }
}
