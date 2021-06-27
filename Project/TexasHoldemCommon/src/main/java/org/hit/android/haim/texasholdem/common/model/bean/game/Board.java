package org.hit.android.haim.texasholdem.common.model.bean.game;

import lombok.EqualsAndHashCode;

import java.util.Optional;

/**
 * A board consists out of five cards: 3 x flop, 1 x turn and 1 x river
 * @author Haim Adrian
 * @since 08-May-21
 */
@EqualsAndHashCode(callSuper = true)
public class Board extends CardsHolder {
    /**
     * Texas holdem board contains 5 cards when fully opened.<br/>
     * Pass this value to {@link CardsHolder}
     */
    public static final int AMOUNT_OF_CARDS = 5;

    @Override
    protected int getAmountOfCards() {
        return AMOUNT_OF_CARDS;
    }

    public boolean hasTurn() {
        return getCards().size() > 3;
    }

    public boolean hasRiver() {
        return getCards().size() > 4;
    }

    public Optional<Card> getFlop1() {
        return getCardAt(0);
    }

    public Optional<Card> getFlop2() {
        return getCardAt(1);
    }

    public Optional<Card> getFlop3() {
        return getCardAt(2);
    }

    public Optional<Card> getTurn() {
        return getCardAt(3);
    }

    public Optional<Card> getRiver() {
        return getCardAt(4);
    }
}

