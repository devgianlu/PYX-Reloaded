package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.server.BaseCahHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class PreparingShutdown {
    private static AtomicBoolean instance = new AtomicBoolean(false);

    public synchronized static boolean get() {
        return instance.get();
    }

    public synchronized static void check() throws BaseCahHandler.CahException {
        if (get()) throw new BaseCahHandler.CahException(Consts.ErrorCode.PREPARING_SHUTDOWN);
    }

    public synchronized static void set(boolean set) {
        instance.set(set);
    }
}
