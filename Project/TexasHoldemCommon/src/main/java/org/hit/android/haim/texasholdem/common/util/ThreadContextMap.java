package org.hit.android.haim.texasholdem.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A class used to hold user identifier per thread, so we can do some special logic
 * based on current requesting user.<br/>
 * For example, in {@link org.hit.android.haim.texasholdem.common.model.bean.game.Player} class,
 * we hide player hand so players will not be able to get other players hand, except
 * of the requesting player, which must see his own hand. For this purpose, we need to store
 * here the requesting user, and access it from Player class when marshalling model into json.
 * @author Haim Adrian
 * @since 02-Jul-21
 */
public class ThreadContextMap {
    private static final String USER_ID_KEY = "userId";

    /**
     * Map of details we keep for current thread.
     */
    private final ThreadLocal<Map<String, String>> map;

    private ThreadContextMap() {
        map = ThreadLocal.withInitial(HashMap::new);
    }

    /**
     * @return The unique instance of {@link ThreadContextMap}
     */
    public static ThreadContextMap getInstance() {
        return ThreadContextMapRef.INSTANCE;
    }

    /**
     * Stores user identifier to current thread context
     * @param userId The user identifier to store
     */
    public void setUserId(String userId) {
        map.get().put(USER_ID_KEY, userId);
    }

    /**
     * @return The user identifier stored for current thread
     */
    public String getUserId() {
        return map.get().get(USER_ID_KEY);
    }

    private static class ThreadContextMapRef {
        private static final ThreadContextMap INSTANCE = new ThreadContextMap();
    }
}

