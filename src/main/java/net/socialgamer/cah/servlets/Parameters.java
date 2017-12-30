package net.socialgamer.cah.servlets;

import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class Parameters extends HashMap<String, String> {

    private Parameters() {
    }

    public static Parameters fromSession(NanoHTTPD.IHTTPSession session) throws IOException, NanoHTTPD.ResponseException {
        Map<String, String> body = new HashMap<>();
        session.parseBody(body);

        Parameters params = new Parameters();
        for (NameValuePair pair : URLEncodedUtils.parse(body.get("postData"), Charset.forName("UTF-8")))
            params.put(pair.getName(), pair.getValue());

        return params;
    }

    @Nullable
    public String get(Constants.AjaxRequest key) {
        return get(key.toString());
    }

    public boolean getBoolean(Constants.AjaxRequest key, boolean fallback) {
        return getBoolean(key.toString(), fallback);
    }

    public boolean getBoolean(String key, boolean fallback) {
        String val = get(key);
        if (val == null) return false;

        try {
            return Boolean.parseBoolean(key);
        } catch (IllegalArgumentException | NullPointerException ex) {
            return fallback;
        }
    }
}
