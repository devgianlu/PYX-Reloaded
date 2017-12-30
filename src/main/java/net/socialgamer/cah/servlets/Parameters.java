package net.socialgamer.cah.servlets;

import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class Parameters extends HashMap<String, List<String>> {

    public Parameters(NanoHTTPD.IHTTPSession session) {
        super(session.getParameters());
    }

    @Nullable
    public String getFirst(Constants.AjaxRequest key) {
        return getFirst(key.toString());
    }

    @Nullable
    public String getFirst(String key) {
        List<String> values = get(key);
        if (values != null && !values.isEmpty()) return values.get(0);
        else return null;
    }

    public boolean getFirstBoolean(Constants.AjaxRequest key, boolean fallback) {
        return getFirstBoolean(key.toString(), fallback);
    }

    public boolean getFirstBoolean(String key, boolean fallback) {
        String val = getFirst(key);
        if (val == null) return false;

        try {
            return Boolean.parseBoolean(key);
        } catch (IllegalArgumentException | NullPointerException ex) {
            return fallback;
        }
    }
}
