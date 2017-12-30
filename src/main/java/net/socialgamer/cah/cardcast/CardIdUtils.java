package net.socialgamer.cah.cardcast;

import net.socialgamer.cah.data.GameOptions;

import java.util.concurrent.atomic.AtomicInteger;

class CardIdUtils {
    private final static AtomicInteger cardId = new AtomicInteger(-(GameOptions.MAX_BLANK_CARD_LIMIT + 1));

    public static int getNewId() {
        return cardId.decrementAndGet();
    }
}
