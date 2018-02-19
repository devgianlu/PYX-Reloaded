package com.gianlu.pyxreloaded.server;

import com.gianlu.pyxreloaded.Consts;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.QueryParameterUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Parameters extends HashMap<String, String> {

    private Parameters() {
    }

    public static Parameters fromExchange(HttpServerExchange exchange, int length) throws IOException {
        ByteBuffer body = ByteBuffer.allocate(length);
        InputStream in = exchange.getInputStream();
        byte[] buffer = new byte[8 * 1024];

        int read;
        while ((read = in.read(buffer)) != -1)
            body.put(buffer, 0, read);

        Parameters params = new Parameters();
        Map<String, Deque<String>> rawParams = QueryParameterUtils.parseQueryString(new String(body.array()), "UTF-8");
        for (Map.Entry<String, Deque<String>> entry : rawParams.entrySet())
            params.put(entry.getKey(), entry.getValue().getFirst());

        return params;
    }

    public boolean has(Consts.ReceivableKey key) {
        return get(key) != null;
    }

    @Nullable
    public String get(Consts.ReceivableKey key) {
        return get(key.toString());
    }

    public boolean getBoolean(Consts.ReceivableKey key, boolean fallback) {
        return getBoolean(key.toString(), fallback);
    }

    private boolean getBoolean(String key, boolean fallback) {
        String val = get(key);
        if (val == null) return false;

        try {
            return Boolean.parseBoolean(val);
        } catch (IllegalArgumentException | NullPointerException ex) {
            return fallback;
        }
    }
}
