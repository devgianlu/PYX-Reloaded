package net.socialgamer.cah.data;

import com.google.gson.JsonObject;
import net.socialgamer.cah.Constants.CardSetData;

import java.util.Set;


public abstract class CardSet {

    public abstract int getId();

    public abstract String getName();

    public abstract String getDescription();

    public abstract boolean isActive();

    public abstract boolean isBaseDeck();

    public abstract int getWeight();

    public abstract Set<? extends BlackCard> getBlackCards();

    public abstract Set<? extends WhiteCard> getWhiteCards();

    public final JsonObject getClientMetadataJson() {
        JsonObject obj = getCommonClientMetadataJson();
        obj.addProperty(CardSetData.BLACK_CARDS_IN_DECK.toString(), getBlackCards().size());
        obj.addProperty(CardSetData.WHITE_CARDS_IN_DECK.toString(), getWhiteCards().size());
        return obj;
    }

    private JsonObject getCommonClientMetadataJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty(CardSetData.ID.toString(), getId());
        obj.addProperty(CardSetData.CARD_SET_NAME.toString(), getName());
        obj.addProperty(CardSetData.CARD_SET_DESCRIPTION.toString(), getDescription());
        obj.addProperty(CardSetData.WEIGHT.toString(), getWeight());
        obj.addProperty(CardSetData.BASE_DECK.toString(), isBaseDeck());
        return obj;
    }

    @Override
    public String toString() {
        return String.format("%s[name=%s, base=%b, id=%d, active=%b, weight=%d, black=%d, white=%d]",
                getClass().getName(), getName(), isBaseDeck(), getId(), isActive(), getWeight(),
                getBlackCards().size(), getWhiteCards().size());
    }
}
