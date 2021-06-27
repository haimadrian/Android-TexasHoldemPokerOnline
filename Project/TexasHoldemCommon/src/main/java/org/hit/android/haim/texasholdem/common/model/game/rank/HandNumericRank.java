package org.hit.android.haim.texasholdem.common.model.game.rank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hit.android.haim.texasholdem.common.model.bean.game.Card;

/**
 * A class used to keep {@link HandRank} and its score as natural number.
 * <p>
 *     The score is based on the ordinal value of a card that involved in the hand rank. For example,
 *     if there is a pair of J, J's ordinal value is 10, so the score will be J+J which is 20. Assuming
 *     that there was another pair, of K's, then pair of kings is better, and K+K is 24. See {@link Card.CardRank}
 * </p>
 * @author Haim Adrian
 * @since 27-Jun-21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor // for Jackson
public class HandNumericRank implements Comparable<HandNumericRank> {
    /**
     * The rank as enum
     */
    private HandRank handRank;

    /**
     * The rank as natural number, to support comparing hands having the same rank.<br/>
     * For example, when there are two flushes, the flush with higher card wins.
     */
    private int score;

    @Override
    public int compareTo(HandNumericRank another) {
        if (another == null) {
            return 1;
        }

        // When rank is the same, compare the score
        if (handRank.compareTo(another.handRank) == 0) {
            return Integer.compare(score, another.score);
        }

        return handRank.compareTo(another.handRank);
    }

    @Override
    public String toString() {
        return handRank == null ? "null" : handRank.name().replaceAll("_", " ");
    }
}

