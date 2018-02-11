package com.gianlu.pyxreloaded.cardcast;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

public class FailedLoadingSomeCardcastDecks extends Exception {
    public final List<String> failedDecks = new ArrayList<>();

    public FailedLoadingSomeCardcastDecks() {
    }

    public JsonArray getFailedJson() {
        JsonArray array = new JsonArray(failedDecks.size());
        for (String id : failedDecks) array.add(id);
        return array;
    }
}