package com.gianlu.pyxreloaded.cardcast;

import java.util.concurrent.atomic.AtomicInteger;

class CardIdUtils {
    private final static AtomicInteger cardId = new AtomicInteger(Integer.MIN_VALUE);

    public static int getNewId() {
        return cardId.decrementAndGet();
    }
}
