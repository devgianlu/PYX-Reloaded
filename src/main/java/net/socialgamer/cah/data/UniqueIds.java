package net.socialgamer.cah.data;

import java.util.UUID;

public class UniqueIds {

    public static String getNewRandomID() {
        return UUID.randomUUID().toString();
    }
}
