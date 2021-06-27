package org.hit.android.haim.texasholdem.common.model.game;

import org.hit.android.haim.texasholdem.common.model.bean.game.Card;
import org.hit.android.haim.texasholdem.common.model.bean.game.CardsHolder;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Haim Adrian
 * @since 09-May-21
 */
public class Deck extends CardsHolder {
    /**
     * Constructs a new {@link Deck}
     */
    public Deck() {
        addCards(AllCardsRef.ALL_CARDS);
    }

    /**
     * @return All cards in a poker deck
     */
    public static Set<Card> getAllCards() {
        return AllCardsRef.ALL_CARDS;
    }

    @Override
    protected int getAmountOfCards() {
        return AllCardsRef.ALL_CARDS.size();
    }

    @Override
    public CardsHolder addCard(Card card) {
        // Don't call super. Adding cards to a deck is prohibited.
        return this;
    }

    @Override
    public CardsHolder addCards(Collection<Card> cards) {
        // Don't call super. Adding cards to a deck is prohibited.
        return this;
    }

    /**
     * Shuffles the deck
     */
    public void shuffle() {
        Collections.shuffle(getCards(), new SecureRandom());
    }

    /**
     * Pops a card out of the head of the deck and return it
     * @return A card from the head of the deck
     */
    public Card popCard() {
        return removeCardAt(0).orElse(null);
    }

    /**
     * Drops a card out of the deck without returning it
     */
    public void dropCard() {
        removeCardAt(0);
    }

    private static final class AllCardsRef {
        private static final Set<Card> ALL_CARDS;

        static {
            Set<Card> allCards = new HashSet<>(52);
            for (Card.CardRank cardRank : Card.CardRank.values()) {
                if (cardRank != Card.CardRank.NONE) {
                    for (Card.CardSuit cardSuit : Card.CardSuit.values()) {
                        allCards.add(new Card(cardRank, cardSuit));
                    }
                }
            }

            ALL_CARDS = Collections.unmodifiableSet(allCards);
        }
    }
}

