package org.hit.android.haim.texasholdem.common.model.bean.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

/**
 * A board consists out of five cards: 3 x flop, 1 x turn and 1 x river
 * @author Haim Adrian
 * @since 08-May-21
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Board extends CardsHolder {
    /**
     * Texas holdem board contains 5 cards when fully opened.<br/>
     * Pass this value to {@link CardsHolder}
     */
    public static final int AMOUNT_OF_CARDS = 5;

    // Define it explicitly so we will use the correct super ctor
    @JsonCreator
    public Board(@JsonProperty("cards") List<Card> cards) {
        super(cards);
    }

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

    @JsonIgnore
    public Optional<Card> getFlop1() {
        return getCardAt(0);
    }

    @JsonIgnore
    public Optional<Card> getFlop2() {
        return getCardAt(1);
    }

    @JsonIgnore
    public Optional<Card> getFlop3() {
        return getCardAt(2);
    }

    @JsonIgnore
    public Optional<Card> getTurn() {
        return getCardAt(3);
    }

    @JsonIgnore
    public Optional<Card> getRiver() {
        return getCardAt(4);
    }
}

