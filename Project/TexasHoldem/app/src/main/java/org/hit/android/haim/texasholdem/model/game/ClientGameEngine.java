package org.hit.android.haim.texasholdem.model.game;

import android.util.Log;

import org.hit.android.haim.texasholdem.common.model.game.GameEngine;

/**
 * An implementation of {@link GameEngine}, for the client side.<br/>
 * Here we redirect info messages to {@link Log}.
 * @author Haim Adrian
 * @since 27-Jun-21
 */
public class ClientGameEngine extends GameEngine {
    private static final String LOGGER = ClientGameEngine.class.getSimpleName();

    @Override
    protected void info(String message) {
        Log.i(LOGGER, message);
    }
}
