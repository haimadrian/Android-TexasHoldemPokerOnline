package org.hit.android.haim.texasholdem.common.model.bean.game;

import lombok.EqualsAndHashCode;

/**
 * A player's hand consists out of two cards
 * @author Haim Adrian
 * @since 08-May-21
 */
@EqualsAndHashCode(callSuper = true)
public class Hand extends CardsHolder {
    @Override
    protected int getAmountOfCards() {
        return 2;
    }
}

