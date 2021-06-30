package org.hit.android.haim.texasholdem.model.game;

import org.hit.android.haim.texasholdem.common.model.bean.game.GameSettings;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A class to hold all game preferences, to let UI to configure the {@link Game} class, based on user
 * settings.
 * @author Haim Adrian
 * @since 12-Jun-21
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class ClientGameSettings extends GameSettings {
    /**
     * With how many chips a user enters a game
     */
    private long chips;

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
     * Constructs a new {@link ClientGameSettings}
     * @param chips With how many chips a user enters a game
     * @param amountOfBots With how many bots the user wants to play, in case of local game
     * @param gameHash Hash of a game we are trying to connect to, in case of network game
     */
    public ClientGameSettings(long chips, int amountOfBots, String gameHash) {
        this.chips = chips;
        this.amountOfBots = amountOfBots;
        this.gameHash = gameHash;

        if ((gameHash != null) && !gameHash.isEmpty()) {
            setNetwork(true);
        }
    }
}


