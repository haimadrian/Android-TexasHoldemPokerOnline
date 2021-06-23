package org.hit.android.haim.texasholdem.server.model.game;

import lombok.Getter;
import org.hit.android.haim.texasholdem.server.model.bean.game.PlayerAction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Haim Adrian
 * @since 22-Jun-21
 */
public class GameLog {
    /**
     * The actions executed during a game.
     */
    @Getter
    private final List<PlayerAction> playerActions = new ArrayList<>();

    /**
     * The previous actions executed during a game.<br/>
     * This is a backup of {@link #playerActions} when {@link #clear()} is called, so we will
     * show the last game result.
     */
    @Getter
    private final List<PlayerAction> lastRoundPlayerActions = new ArrayList<>();

    /**
     * Store an action to the game log
     * @param action The action to log
     */
    public void logAction(PlayerAction action) {
        playerActions.add(action);
    }

    /**
     * Clear log
     */
    public void clear() {
        // Backup
        lastRoundPlayerActions.clear();
        lastRoundPlayerActions.addAll(playerActions);

        playerActions.clear();
    }
}

