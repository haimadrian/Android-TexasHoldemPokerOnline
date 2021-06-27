package org.hit.android.haim.texasholdem.common.model.bean.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * A class to hold all game preferences.<br/>
 * A game creator defines the preferences, which are stored in this model.
 * @author Haim Adrian
 * @since 12-Jun-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * The identifier of the user that creates a game.<br/>
     * It is set by {@link org.hit.android.haim.texasholdem.server.controller.GameController#createNewGame(String, GameSettings)}<br/>
     * We need the creator identifier to give a client the ability to retrieve a game hash by creating user identifier.
     */
    @JsonIgnore
    private String creatorId;

    /**
     * True when the code is running at the server, cause it means network game.<br/>
     * Otherwise, when the game engine is running at the client side, this is an AI game.
     */
    @JsonIgnore
    private boolean isNetwork;
}
