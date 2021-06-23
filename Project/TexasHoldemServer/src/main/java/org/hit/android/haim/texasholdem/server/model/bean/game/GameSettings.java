package org.hit.android.haim.texasholdem.server.model.bean.game;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * A class to hold all game preferences.<br/>
 * A game creator defines the preferences, which are stored in this model.
 * @author Haim Adrian
 * @since 12-Jun-21
 */
@Data
@Builder
public class GameSettings {
    /**
     * Small bet of a game.
     */
    private long smallBet;

    /**
     * Big bet of a game.
     */
    private long bigBet;

    /**
     * How much time each player has for its turn. Defaults to 1 minute.
     */
    @Builder.Default
    private long turnTime = TimeUnit.MINUTES.toMillis(1);

    /**
     * True when the code is running at the server, cause it means network game.<br/>
     * Otherwise, when the game engine is running at the client side, this is an AI game.
     */
    private boolean isNetwork;
}
