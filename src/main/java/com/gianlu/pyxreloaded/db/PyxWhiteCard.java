package com.gianlu.pyxreloaded.db;

import com.gianlu.pyxreloaded.data.WhiteCard;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PyxWhiteCard extends WhiteCard {
    private final int id;
    private final String text;
    private final String watermark;

    public PyxWhiteCard(ResultSet resultSet) throws SQLException {
        id = resultSet.getInt("id");
        text = resultSet.getString("text");
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
    public String getWatermark() {
        return watermark == null ? "" : watermark;
    }

    @Override
    public boolean isWriteIn() {
        return false;
    }
}
