package com.gianlu.pyxreloaded.data;

import com.gianlu.pyxreloaded.Consts;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonWrapper {
    public static final JsonWrapper EMPTY = new JsonWrapper();
    private final JsonObject obj;

    public JsonWrapper() {
        obj = new JsonObject();
    }

    public JsonWrapper(Consts.ReturnableKey data, JsonElement element) {
        this();
        add(data, element);
    }

    public JsonWrapper(Consts.ReturnableKey data, int i) {
        this();
        add(data, i);
    }

    public JsonWrapper(Consts.ReturnableKey data, String str) {
        this();
        add(data, str);
    }

    public JsonWrapper(Consts.ErrorCode code) {
        this();
        add(Consts.GeneralKeys.ERROR, true);
        add(Consts.GeneralKeys.ERROR_CODE, code.toString());
    }

    public JsonObject obj() {
        return obj;
    }

    public JsonWrapper add(Consts.ReturnableKey data, JsonElement element) {
        obj.add(data.toString(), element);
        return this;
    }

    public JsonWrapper add(Consts.ReturnableKey data, JsonWrapper wrapper) {
        add(data, wrapper == null ? null : wrapper.obj());
        return this;
    }

    public JsonWrapper add(Consts.ReturnableKey data, boolean bool) {
        obj.addProperty(data.toString(), bool);
        return this;
    }

    public JsonWrapper add(Consts.ReturnableKey data, int i) {
        obj.addProperty(data.toString(), i);
        return this;
    }

    public JsonWrapper add(Consts.ReturnableKey data, String str) {
        obj.addProperty(data.toString(), str);
        return this;
    }

    @Override
    public String toString() {
        return obj.toString();
    }

    public void addAll(JsonWrapper data) {
        if (data == null) return;
        for (String key : data.obj.keySet()) obj.add(key, data.obj.get(key));
    }
}
