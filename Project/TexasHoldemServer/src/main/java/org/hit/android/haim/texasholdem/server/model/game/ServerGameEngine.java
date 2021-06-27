package org.hit.android.haim.texasholdem.server.model.game;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.hit.android.haim.texasholdem.common.model.bean.game.GameSettings;
import org.hit.android.haim.texasholdem.common.model.game.GameEngine;
import org.hit.android.haim.texasholdem.server.controller.common.Base64;

/**
 * An implementation of {@link GameEngine}, for the server side.<br/>
 * Here we define the game hash and redirect logs to log4j2.
 * @author Haim Adrian
 * @since 27-Jun-21
 */
@Log4j2
@NoArgsConstructor
public class ServerGameEngine extends GameEngine {
    /**
     * Constructs a new {@link ServerGameEngine}
     * @param gameSettings Preferences of a game.
     * @param listener A listener to get notified upon player updates, so we can persist changes in chips amount.
     */
    public ServerGameEngine(@NonNull GameSettings gameSettings, @NonNull PlayerUpdateListener listener) {
        super(gameSettings, listener);
    }

    @Override
    protected void initGameHash() {
        gameHash = Base64.encodeToString(getId());
    }

    @Override
    protected void info(String message) {
        log.info(message);
    }
}

