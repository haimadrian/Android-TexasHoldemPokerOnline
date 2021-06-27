package org.hit.android.haim.texasholdem.common.model.game.rank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hit.android.haim.texasholdem.common.model.bean.game.Board;
import org.hit.android.haim.texasholdem.common.model.bean.game.Card;
import org.hit.android.haim.texasholdem.common.model.bean.game.Hand;

/**
 * A result model when calling {@link HandRankCalculator#calculate(Board, Hand)}<br/>
 * The result contains both the {@link HandNumericRank rank} and the selected cards that
 * built that rank.
 * @author Haim Adrian
 * @since 27-Jun-21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor // for Jackson
public class HandRankCalculatorResult implements Comparable<HandRankCalculatorResult> {
    private HandNumericRank rank;
    private Card[] selectedCards;

    @Override
    public int compareTo(HandRankCalculatorResult another) {
        return rank.compareTo(another.getRank());
    }

    @Override
    public String toString() {
        // This is used by PlayerAction's toString implementation, to show a hand rank when player wins.
        return rank.toString();
    }
}

