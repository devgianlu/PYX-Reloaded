package net.socialgamer.cah.data;

import org.jetbrains.annotations.Nullable;

public class BlankWhiteCard extends WhiteCard {
    private static final String BLANK_TEXT = "____";
    private final int id;
    private String text = null;

    public BlankWhiteCard(final int id) {
        this.id = id;
        clear();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    @Nullable
    public String getText() {
        return text;
    }

    public void setText(@Nullable String text) {
        this.text = text;
    }

    public void clear() {
        setText(BLANK_TEXT);
    }

    @Override
    public String getWatermark() {
        return "____";
    }

    @Override
    public boolean isWriteIn() {
        return true;
    }
}
