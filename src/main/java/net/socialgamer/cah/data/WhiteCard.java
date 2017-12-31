package net.socialgamer.cah.data;

import com.google.gson.JsonObject;
import net.socialgamer.cah.Constants.WhiteCardData;


public abstract class WhiteCard {
    public static JsonObject getFaceDownCardClientDataJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty(WhiteCardData.ID.toString(), -1);
        obj.addProperty(WhiteCardData.TEXT.toString(), "");
        obj.addProperty(WhiteCardData.WATERMARK.toString(), "");
        obj.addProperty(WhiteCardData.WRITE_IN.toString(), false);
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

    public final JsonObject getClientDataJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty(WhiteCardData.ID.toString(), getId());
        obj.addProperty(WhiteCardData.TEXT.toString(), getText());
        obj.addProperty(WhiteCardData.WATERMARK.toString(), getWatermark());
        obj.addProperty(WhiteCardData.WRITE_IN.toString(), isWriteIn());
        return obj;
    }

    @Override
    public String toString() {
        return String.format("%s %s (id:%d, watermark:%s)", getClass().getName(), getText(), getId(), getWatermark());
    }
}
