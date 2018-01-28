package net.socialgamer.cah.servlets;

import net.socialgamer.cah.data.User;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class Providers {
    private final static Logger logger = Logger.getLogger(Providers.class.getSimpleName());
    private final static Map<Class<? extends Annotation>, Provider<?>> providers = new HashMap<Class<? extends Annotation>, Provider<?>>() {
        @Override
        public Provider<?> put(Class<? extends Annotation> key, Provider<?> value) {
            logger.config("Added provider for " + key);
            return super.put(key, value);
        }
    };

    static {
        add(Annotations.UserFactory.class, (Provider<User.Factory>) () -> new User.Factory() {
            @Override
            public User create(String nickname, String hostname, boolean admin, String persistentId) {
                return new User(nickname, hostname, persistentId, Sessions.generateNewId(), admin);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <P> Provider<P> get(Class<? extends Annotation> cls) {
        return (Provider<P>) providers.get(cls);
    }

    public static void add(Class<? extends Annotation> cls, Provider<?> provider) {
        providers.put(cls, provider);
    }
}
