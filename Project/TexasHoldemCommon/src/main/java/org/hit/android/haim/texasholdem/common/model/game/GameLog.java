package org.hit.android.haim.texasholdem.common.model.game;

import lombok.Getter;
import org.hit.android.haim.texasholdem.common.model.bean.game.PlayerAction;

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

    /**
     * @return The most recent player action
     */
    public PlayerAction getLastPlayerAction() {
        if (playerActions.isEmpty()) {
            return null;
        }

        return playerActions.get(playerActions.size() - 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (PlayerAction action : lastRoundPlayerActions) {
            sb.append(action.toString()).append(System.lineSeparator());
        }

        sb.append(System.lineSeparator()).append("-------------------").append(System.lineSeparator());

        for (PlayerAction action : playerActions) {
            sb.append(System.lineSeparator()).append(action.toString());
        }

        return sb.toString();
    }
}

