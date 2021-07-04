package org.hit.android.haim.texasholdem.common.model.bean.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A player's hand consists out of two cards
 * @author Haim Adrian
 * @since 08-May-21
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Hand extends CardsHolder {
    // Define it explicitly so we will use the correct super ctor
    @JsonCreator
    public Hand(@JsonProperty("cards") List<Card> cards) {
        super(cards);
    }

    @Override
    protected int getAmountOfCards() {
        return 2;
    }
}

