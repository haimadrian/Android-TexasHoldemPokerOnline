package org.hit.android.haim.texasholdem.common.model.bean.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hit.android.haim.texasholdem.common.model.game.Chips;
import org.hit.android.haim.texasholdem.common.model.game.rank.HandRankCalculatorResult;

/**
 * A class to keep a player move. This can be check, raise, fold.<br/>
 * We need to store player moves so we can show game log, and we can also let clients
 * to know what indications to paint. For example, when player raises, we need to
 * show that on board. Or if a player was folded, either manually or automatically due to timeout.
 * @author Haim Adrian
 * @since 22-Jun-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAction {
    /**
     * What move a player took
     */
    @JsonProperty
    private PlayerActionKind actionKind;

    /**
     * In case of call/raise, this holds the amount of chips involved.
     */
    @Builder.Default
    @JsonProperty
    private Chips chips = new Chips();

    /**
     * Name of the player making a move
     */
    @JsonProperty
    private String name;

    /**
     * Hand rank of a player is used to let clients to know with what hand a player won.<br/>
     * During a game this value refers to {@code null}, so we will not have breaches. We assign this value when
     * a round is over only.
     */
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HandRankCalculatorResult handRank;

    /**
     * Sets the amount of chips related to this player action. This is relevant for call/raise only.
     * @param chips The amount of chips to set
     * @throws IllegalArgumentException in case the action kind is one that does not involve chips
     */
    @JsonIgnore
    public PlayerAction setChips(long chips) throws IllegalArgumentException {
        if ((actionKind == PlayerActionKind.CHECK) || (actionKind == PlayerActionKind.FOLD)) {
            throw new IllegalArgumentException("Cannot set chips when taking " + actionKind + " move.");
        }

        this.chips.set(chips);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);

        // When there is no action kind, it means an action that created at GameEngine.applyWinIfNeeded, where we
        // want to log player earnings.
        if (actionKind == null) {
            sb.append(" won ").append(chips.toShorthand()).append(" chips");
            if (handRank != null) {
                sb.append(", with ").append(handRank).append('.');
            }
        } else {
            switch (actionKind) {
                case CHECK:
                    sb.append(" checked.");
                    break;
                case CALL:
                    sb.append(" called ").append(chips.toShorthand()).append(".");
                    break;
                case RAISE:
                    sb.append(" raised by ").append(chips.toShorthand()).append(".");
                    break;
                case FOLD:
                    sb.append(" folded.");
                    break;
            }
        }

        return sb.toString();
    }
}

