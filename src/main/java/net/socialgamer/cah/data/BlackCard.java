package net.socialgamer.cah.data;

import com.google.gson.JsonObject;
import net.socialgamer.cah.Constants.BlackCardData;


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

    public final JsonObject getClientDataJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty(BlackCardData.ID.toString(), getId());
        obj.addProperty(BlackCardData.TEXT.toString(), getText());
        obj.addProperty(BlackCardData.DRAW.toString(), getDraw());
        obj.addProperty(BlackCardData.PICK.toString(), getPick());
        obj.addProperty(BlackCardData.WATERMARK.toString(), getWatermark());
        return obj;
    }

    @Override
    public String toString() {
        return String.format("%s %s (id:%d, draw:%d, pick:%d, watermark:%s)", getClass().getName(), getText(), getId(), getDraw(), getPick(), getWatermark());
    }
}
