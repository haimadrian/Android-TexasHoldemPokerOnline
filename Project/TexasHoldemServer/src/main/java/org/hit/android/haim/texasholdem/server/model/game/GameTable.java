package org.hit.android.haim.texasholdem.server.model.game;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hit.android.haim.texasholdem.server.model.bean.game.GameSettings;
import org.hit.android.haim.texasholdem.server.model.bean.game.Player;

/**
 * @author Haim Adrian
 * @since 11-Jun-21
 */
@Data
@RequiredArgsConstructor
public class GameTable {
    /**
     * Game preferences. See {@link GameSettings}
     */
    @NonNull
    private final GameSettings gameSettings;

    /**
     * The {@link Players} playing in this game
     */
    @NonNull
    private final Players players;

    /**
     * The player assigned the Dealer role.<br/>
     * This player is modified in every single round, clockwise.
     */
    private Player dealer;

    public void shuffleDeck() {

    }
}

