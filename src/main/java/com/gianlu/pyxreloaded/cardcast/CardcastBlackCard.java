package com.gianlu.pyxreloaded.cardcast;

import com.gianlu.pyxreloaded.data.BlackCard;


public class CardcastBlackCard extends BlackCard {
    private final int id;
    private final String text;
    private final int draw;
    private final int pick;
    private final String deckId;

    CardcastBlackCard(int id, String text, int draw, int pick, String deckId) {
        this.id = id;
        this.text = text;
        this.draw = draw;
        this.pick = pick;
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
    public int getDraw() {
        return draw;
    }

    @Override
    public int getPick() {
        return pick;
    }
}
