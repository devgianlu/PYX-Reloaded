package com.gianlu.pyxreloaded.db;

import com.gianlu.pyxreloaded.data.BlackCard;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PyxBlackCard extends BlackCard {
    private final int id;
    private final String text;
    private final int draw;
    private final int pick;
    private final String watermark;

    public PyxBlackCard(ResultSet resultSet) throws SQLException {
        id = resultSet.getInt("id");
        text = resultSet.getString("text");
        draw = resultSet.getInt("draw");
        pick = resultSet.getInt("pick");
        watermark = resultSet.getString("watermark");
    }

    @Override
    public int getId() {
        return id;
    }

    /**
     * @return Card text. HTML is allowed and entities are required.
     */
    @Override
    public String getText() {
        return text;
    }

    @Override
    public int getDraw() {
        return draw;
    }

    @Override
    public int getPick() {
        return pick;
    }

    @Override
    public String getWatermark() {
        return watermark == null ? "" : watermark;
    }
}
