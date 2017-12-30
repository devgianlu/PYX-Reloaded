package net.socialgamer.cah.data;

import java.util.UUID;

class UniqueIDs {

    public static String getNewRandomID() {
        return UUID.randomUUID().toString();
    }
}
