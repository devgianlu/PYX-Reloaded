package net.socialgamer.cah;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonWrapper {
    public static final JsonWrapper EMPTY = new JsonWrapper();
    private final JsonObject obj;


    public JsonWrapper() {
        obj = new JsonObject();
    }

    public JsonWrapper(Constants.ReturnableData data, JsonElement element) {
        this();
        add(data, element);
    }

    public JsonWrapper(Constants.ReturnableData data, int i) {
        this();
        add(data, i);
    }

    public JsonObject obj() {
        return obj;
    }

    public JsonWrapper add(Constants.ReturnableData data, JsonElement element) {
        obj.add(data.toString(), element);
        return this;
    }

    public JsonWrapper add(Constants.ReturnableData data, boolean bool) {
        obj.addProperty(data.toString(), bool);
        return this;
    }

    public JsonWrapper add(Constants.ReturnableData data, int i) {
        obj.addProperty(data.toString(), i);
        return this;
    }

    public JsonWrapper add(Constants.ReturnableData data, String str) {
        obj.addProperty(data.toString(), str);
        return this;
    }
}
