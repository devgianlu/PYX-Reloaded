package net.socialgamer.cah.data;

import net.socialgamer.cah.Consts;
import net.socialgamer.cah.JsonWrapper;


public abstract class WhiteCard {
    public static JsonWrapper getFaceDownCardClientDataJson() {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GeneralKeys.CARD_ID, -1);
        obj.add(Consts.WhiteCardData.TEXT, "");
        obj.add(Consts.WhiteCardData.WATERMARK, "");
        obj.add(Consts.WhiteCardData.WRITE_IN, false);
        return obj;
    }

    public abstract int getId();

    public abstract String getText();

    public abstract String getWatermark();

    public abstract boolean isWriteIn();

    @Override
    public final boolean equals(final Object other) {
        return other instanceof WhiteCard && ((WhiteCard) other).getId() == getId();
    }

    @Override
    public final int hashCode() {
        return getId();
    }

    public final JsonWrapper getClientDataJson() {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GeneralKeys.CARD_ID, getId());
        obj.add(Consts.WhiteCardData.TEXT, getText());
        obj.add(Consts.WhiteCardData.WATERMARK, getWatermark());
        obj.add(Consts.WhiteCardData.WRITE_IN, isWriteIn());
        return obj;
    }

    @Override
    public String toString() {
        return String.format("%s %s (id:%d, watermark:%s)", getClass().getName(), getText(), getId(), getWatermark());
    }
}
