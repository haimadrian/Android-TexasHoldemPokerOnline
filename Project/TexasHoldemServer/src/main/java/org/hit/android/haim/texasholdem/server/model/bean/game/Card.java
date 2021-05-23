package org.hit.android.haim.texasholdem.server.model.bean.game;

import lombok.*;

/**
 * A card in the game<br/>
 * A card has a rank and suit.
 *
 * @author Haim Adrian
 * @see CardRank
 * @see CardSuit
 * @since 08-May-21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Card implements Comparable<Card> {
    /**
     * Empty card is a card with cardRank={@link CardRank#NONE} and cardSuit={@code null}.<br/>
     * We use empty card when there are card slots (e.g. at {@link Board}) that have not been filled up with a real card yet.
     * This lets us work freely with assumption of 5 cards in a board, without caring about null references or index out of bounds.
     */
    public static final Card EMPTY = new Card(CardRank.NONE, null);

    @Setter(AccessLevel.PRIVATE)
    private CardRank cardRank;

    @Setter(AccessLevel.PRIVATE)
    private CardSuit cardSuit;

    /**
     * Build a card out of string.<br/>
     * The string should be a console format for a card.<br/>
     * For example:
     * <ul>
     *     <li>"T♦" = 10 Diamond</li>
     *     <li>"3♥" = 3 Heart</li>
     *     <li>"J♠" = Jack Spade</li>
     *     <li>"K♣" = King Club</li>
     * </ul>
     *
     * @param str The string to parse
     * @return A card out of the parsed string, or {@code null} in case the string does not represent a card
     * @see CardRank
     * @see CardSuit
     */
    public static Card valueOf(String str) {
        String trimmed = str.trim();
        if (trimmed.length() != 2) {
            return null;
        }

        return new Card(CardRank.valueOfSymbol("" + trimmed.charAt(0)), CardSuit.valueOfSymbol("" + trimmed.charAt(1)));
    }

    @Override
    public int compareTo(Card another) {
        return cardRank.compareTo(another.cardRank);
    }

    /**
     * An enum representing ranks of Poker card.<br/>
     * Rank is based on each element's ordinal value, so we can compare ranks.<br/>
     * Each element has also a {@link #getSymbol() symbol} presentation for console
     *
     * @author Haim Adrian
     * @since 08-May-21
     */
    public enum CardRank {
        NONE("0"), // Define none to get ordinal value of 0, cause we want ordinals to start from 1 for score.
        TWO("2"),
        THREE("3"),
        FOUR("4"),
        FIVE("5"),
        SIX("6"),
        SEVEN("7"),
        EIGHT("8"),
        NINE("9"),
        TEN("T"),
        JACK("J"),
        QUEEN("Q"),
        KING("K"),
        ACE("A"); // Ace has the highest rank (or lowest when straight starting from 1)

        private final String symbol;

        CardRank(String symbol) {
            this.symbol = symbol;
        }

        /**
         * Get a {@link CardRank} based on card symbol
         *
         * @param symbol The symbol to get CardRank for
         * @return The card rank or {@link #NONE} in case there is no card with the specified symbol
         */
        public static CardRank valueOfSymbol(String symbol) {
            CardRank result = NONE;

            for (CardRank value : values()) {
                if (value.getSymbol().equalsIgnoreCase(symbol)) {
                    result = value;
                    break;
                }
            }

            return result;
        }

        /**
         * @return A symbol presentation for console
         */
        public String getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return getSymbol();
        }
    }

    /**
     * An enum representing card suits<br/>
     * Each element has also a {@link #getSymbol() symbol} presentation for console
     *
     * @author Haim Adrian
     * @since 08-May-21
     */
    public enum CardSuit {
        CLUB("♣"),
        DIAMOND("♦"),
        HEART("♥"),
        SPADE("♠");

        private final String symbol;

        CardSuit(String symbol) {
            this.symbol = symbol;
        }

        /**
         * Get a {@link CardSuit} based on suit symbol
         *
         * @param symbol The symbol to get CardSuit for
         * @return The card suit or {@link null} in case there is no suit with the specified symbol
         */
        public static CardSuit valueOfSymbol(String symbol) {
            CardSuit result = null;

            for (CardSuit value : values()) {
                if (value.getSymbol().equalsIgnoreCase(symbol)) {
                    result = value;
                    break;
                }
            }

            return result;
        }

        /**
         * @return A symbol presentation for console
         */
        public String getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return getSymbol();
        }
    }
}

