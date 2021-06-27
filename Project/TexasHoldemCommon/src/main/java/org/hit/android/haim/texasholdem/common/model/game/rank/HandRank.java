package org.hit.android.haim.texasholdem.common.model.game.rank;

/**
 * An enum representing ranks of Poker hand.<br/>
 * Rank is based on each element's ordinal value, so we can compare ranks.
 * @author Haim Adrian
 * @since 08-May-21
 */
public enum HandRank {
    NONE,
    HIGH_CARD,
    PAIR,
    TWO_PAIRS,
    TRIPS, // Three of a kind
    STRAIGHT,
    FLUSH,
    FULL_HOUSE,
    QUADS, // Four of a kind
    STRAIGHT_FLUSH,
    ROYAL_FLUSH
}

