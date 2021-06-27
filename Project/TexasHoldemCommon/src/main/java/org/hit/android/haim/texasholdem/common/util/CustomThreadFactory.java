package org.hit.android.haim.texasholdem.common.util;

import lombok.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A thread factory with custom name prefix.
 * @author Haim Adrian
 * @since 27-Jun-21
 */
public class CustomThreadFactory implements ThreadFactory {
    /**
     * A name prefix to use for meaningful thread name
     */
    @NonNull
    private final String namePrefix;

    /**
     * A default thread factory to use for creating new threads, and then modify their name to add te prefix.
     */
    private final ThreadFactory defaultThreadFactory;

    /**
     * Constructs a new {@link CustomThreadFactory}
     * @param namePrefix A name prefix to use for meaningful thread name
     */
    public CustomThreadFactory(@NonNull String namePrefix) {
        this.namePrefix = namePrefix;
        defaultThreadFactory = Executors.defaultThreadFactory();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = defaultThreadFactory.newThread(r);
        t.setName(namePrefix + "-" + t.getName());
        return t;
    }
}

