package org.hit.android.haim.texasholdem.server.model.bean.game;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An abstract class representing a model that holds cards.<br/>
 * A card holder can be {@link Hand} which holds two cards, or {@link Board} which holds 5 cards.
 * @author Haim Adrian
 * @since 08-May-21
 */
@Data
public abstract class CardsHolder {
    /**
     * The cards this cards holder have. We access cards by index.<br/>
     * Any card in this list is unique. We protect it using the {@link #backedBy} variable.
     */
    @Getter(AccessLevel.PROTECTED)
    private final List<Card> cards;

    /**
     * A copy of cards, to be able to make sure we do not have duplicities in O(1).
     */
    @Getter(AccessLevel.PROTECTED)
    private final Set<Card> backedBy;

    /**
     * Constructs a new {@link CardsHolder}
     */
    public CardsHolder() {
        cards = new ArrayList<>(getAmountOfCards());
        backedBy = new HashSet<>(getAmountOfCards());
    }

    /**
     * @return How many cards this holder can have
     */
    protected abstract int getAmountOfCards();

    /**
     * Add a card to the list of cards. The card is added to the end of the list
     * @param card The card to add
     * @return A reference to this, to support chaining
     */
    public CardsHolder addCard(Card card) {
        if (!backedBy.contains(card)) {
            cards.add(card);
            backedBy.add(card);
        }

        return this;
    }

    /**
     * Add several cards to the list of cards. The cards are added to the end of the list
     * @param cards The cards to add
     * @return A reference to this, to support chaining
     */
    public CardsHolder addCards(Collection<Card> cards) {
        List<Card> nonExistingCards = cards.stream().filter(card -> !backedBy.contains(card)).collect(Collectors.toList());
        this.cards.addAll(nonExistingCards);
        this.backedBy.addAll(nonExistingCards);
        return this;
    }

    /**
     * Remove all cards
     */
    public void clear() {
        cards.clear();
        backedBy.clear();
    }

    /**
     * Get a card based on its index (starting from 0) or return {@link Optional#empty()} in case there is no card
     * at the specified index.
     * @param index The index of the card to get
     * @return The card, or empty
     */
    public Optional<Card> getCardAt(int index) {
        if ((index >= 0) && (cards.size() > index)) {
            return Optional.of(cards.get(index));
        }

        return Optional.empty();
    }

    /**
     * Remove a card based on its index (starting from 0) or return {@link Optional#empty()} in case there is no card
     * at the specified index.
     * @param index The index of the card to remove
     * @return The card, or empty
     */
    protected Optional<Card> removeCardAt(int index) {
        if ((index >= 0) && (cards.size() > index)) {
            return Optional.of(cards.remove(index));
        }

        return Optional.empty();
    }

    /**
     * @return Copy of cards in this cards holder
     */
    public List<Card> copyCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Copying cards of this cards holder, making sure the result contains exactly {@code howManyCards} amount of cards.<br/>
     * If there are more cards than {@code howManyCards}, we will copy first {@code howManyCards} cards. If there are less
     * cards than {@code howManyCards}, we will copy all of the cards, and pad the result with {@link Card#EMPTY}.
     * @return Copy of cards in this cards holder
     */
    public List<Card> copyExactNumberOfCards(int howManyCards) {
        return new ArrayList<>(cards);
    }

    /**
     * @return How many cards this cards holder holds
     */
    public int size() {
        return cards.size();
    }
}

