package org.hit.android.haim.texasholdem.model.game;

import org.hit.android.haim.texasholdem.model.User;

import lombok.Builder;
import lombok.Data;

/**
 * A class to hold all game preferences, to let UI to configure the {@link Game} class, based on user
 * settings.
 * @author Haim Adrian
 * @since 12-Jun-21
 */
@Data
@Builder
public class GameSettings {
    /**
     * The user creating / accessing a game
     */
    private User user;

    /**
     * With how many chips a user enters a game
     */
    private long coins;

    /**
     * Small bet of a game. It is not primitive cause when user joins a network game,
     * this is configured by the game creator, so in this case the value will be null.
     */
    private Long smallBet;

    /**
     * Big bet of a game. It is not primitive cause when user joins a network game,
     * this is configured by the game creator, so in this case the value will be null.
     */
    private Long bigBet;

    /**
     * With how many bots the user wants to play, in case of local game
     */
    private int amountOfBots;

    /**
     * Hash of a game we are trying to connect to, in case of network game
     */
    @Builder.Default
    private String gameHash = null;

    /**
     * @return Whether this is a network game or local game.
     */
    public boolean isNetworkGame() {
        return gameHash != null;
    }
}
