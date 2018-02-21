package com.gianlu.pyxreloaded.cards;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;

public abstract class BlackCard {

    public abstract int getId();

    public abstract String getText();

    public abstract String getWatermark();

    public abstract int getDraw();

    public abstract int getPick();

    @Override
    public final boolean equals(final Object other) {
        return other instanceof BlackCard && ((BlackCard) other).getId() == getId();
    }

    @Override
    public final int hashCode() {
        return getId();
    }

    public final JsonWrapper getClientDataJson() {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GeneralKeys.CARD_ID, getId());
        obj.add(Consts.BlackCardData.TEXT, getText());
        obj.add(Consts.BlackCardData.DRAW, getDraw());
        obj.add(Consts.BlackCardData.PICK, getPick());
        obj.add(Consts.BlackCardData.WATERMARK, getWatermark());
        return obj;
    }

    @Override
    public String toString() {
        return String.format("%s %s (id:%d, draw:%d, pick:%d, watermark:%s)", getClass().getName(), getText(), getId(), getDraw(), getPick(), getWatermark());
    }
}
