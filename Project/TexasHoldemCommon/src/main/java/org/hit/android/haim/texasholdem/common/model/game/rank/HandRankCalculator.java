package org.hit.android.haim.texasholdem.common.model.game.rank;

import org.hit.android.haim.texasholdem.common.model.bean.game.Board;
import org.hit.android.haim.texasholdem.common.model.bean.game.Card;
import org.hit.android.haim.texasholdem.common.model.bean.game.Hand;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used in order to calculate the rank of a hand, considering both a player's hand and the board.
 * @author Haim Adrian
 * @since 09-May-21
 */
public class HandRankCalculator {
    private HandRankCalculator() {

    }

    /**
     * (7 P 5): All possible permutations when selecting 5 cards out of 7.<br/>
     * We check all of the permutations in order to find the highest hand rank out of all 7 cards.
     * <p>
     *     We first split the precompiled permutations by ',' so we will have all of the permutations as list,
     *     and then we split every permutation by ' ' so we will have array of indices ready.
     * </p>
     */
    //@formatter:off
    private static final List<int[]> allCardPermutations = Arrays.stream("0 1 2 3 4,0 1 2 3 5,0 1 2 3 6,0 1 2 4 5,0 1 2 4 6,0 1 2 5 6,0 1 3 4 5,0 1 3 4 6,0 1 3 5 6,0 1 4 5 6,0 2 3 4 5,0 2 3 4 6,0 2 3 5 6,0 2 4 5 6,0 3 4 5 6,1 2 3 4 5,1 2 3 4 6,1 2 3 5 6,1 2 4 5 6,1 3 4 5 6,2 3 4 5 6"
        .split(","))
        .map(permutation -> Arrays.stream(permutation.trim().split(" ")).mapToInt(Integer::parseInt).toArray())
        .collect(Collectors.toList());
    //@formatter:on

    /**
     * Calculate the rank of a player's hand, based on 5 cards from board, and 2 cards from hand.<br/>
     * The calculator will select the 5 cards having the highest rank
     * @param board The board of a game
     * @param hand Player's hand
     * @return Highest possible rank of the hand and board
     */
    public static HandRankCalculatorResult calculate(Board board, Hand hand) {
        HandNumericRank highestRank = null;
        int[] bestPermutation = null;

        List<Card> allCards = new ArrayList<>(board.size() + hand.size());
        allCards.addAll(board.copyCards());
        allCards.addAll(hand.copyCards());

        // Find the best rank out of all possible permutations
        for (int[] currPermutation : allCardPermutations) {
            Card[] currSelectedCards = new Card[] { allCards.get(currPermutation[0]), allCards.get(currPermutation[1]), allCards.get(currPermutation[2]), allCards.get(currPermutation[3]), allCards.get(currPermutation[4]) };
            Arrays.sort(currSelectedCards);
            HandNumericRank currRank = calculate(currSelectedCards);

            // If we've received a higher rank, keep it.
            if ((highestRank == null) || (currRank.compareTo(highestRank) > 0)) {
                highestRank = currRank;
                bestPermutation = currPermutation;
            }
        }

        return new HandRankCalculatorResult(highestRank, new Card[] { allCards.get(Objects.requireNonNull(bestPermutation)[0]), allCards.get(bestPermutation[1]), allCards.get(bestPermutation[2]), allCards.get(bestPermutation[3]), allCards.get(bestPermutation[4]) });
    }

    /**
     * A helper method used to calculate the rank out of 5 cards.<br/>
     * The method assumes the array of cards is sorted based on cards rank
     * @param cards The cards to calculate rank for
     * @return A {@link HandNumericRank} representing the rank of the specified 5 cards
     */
    private static HandNumericRank calculate(Card[] cards) {
        HandNumericRank rank = evaluatePairings(cards);

        // Score must be zero for having a straight: If there is any pair, it is impossible to find straight, cause we must have 5 different cards for a straight.
        if (rank.getHandRank() == HandRank.NONE) {
            rank = evaluateStraight(cards);
        }

        // Group cards by their suit, so we can check for flush. If there is a single suit only, it means all 5 cards are of same suit.
        Map<Card.CardSuit, List<Card>> cardSuitToCards = Arrays.stream(cards).collect(Collectors.groupingBy(Card::getCardSuit));
        boolean isFlush = cardSuitToCards.size() == 1;

        if (isFlush) {
            if (rank.getHandRank() == HandRank.STRAIGHT) {
                // Ace is always last when sorting, but it might be a A, 2, 3, 4, 5 straight, so check both Ace and King.
                if ((cards[4].getCardRank() == Card.CardRank.ACE) && (cards[3].getCardRank() == Card.CardRank.KING)) {
                    rank = new HandNumericRank(HandRank.ROYAL_FLUSH, rank.getScore());
                } else {
                    rank = new HandNumericRank(HandRank.STRAIGHT_FLUSH, rank.getScore());
                }
            }
            // There might be FULL_HOUSE or QUADS, which are better than FLUSH. So make sure we do not override them.
            else if (HandRank.FLUSH.compareTo(rank.getHandRank()) > 0) {
                // If we are here it means that all cards are of the same suit, so sum their rank up
                rank = new HandNumericRank(HandRank.FLUSH, sumCardsRank(cards));
            }
        }

        // If there was no score, set HIGH_CARD and sum up the cards rank.
        if (rank.getHandRank() == HandRank.NONE) {
            rank = new HandNumericRank(HandRank.HIGH_CARD, sumCardsRank(cards));
        }

        return rank;
    }

    /**
     * A helper method we use in order to check set of 5 cards and see if there is any pairing.<br/>
     * A pairing rank can be one of:
     * <ol>
     *     <li>{@link HandRank#QUADS QUADS}</li>
     *     <li>{@link HandRank#FULL_HOUSE FULL_HOUSE}</li>
     *     <li>{@link HandRank#TRIPS TRIPS}</li>
     *     <li>{@link HandRank#TWO_PAIRS TWO_PAIRS}</li>
     *     <li>{@link HandRank#PAIR PAIR}</li>
     * </ol>
     * @param cards Set of 5 cards to find pairings in
     * @return A {@link HandNumericRank} containing both {@link HandRank} and numeric score, based on involved {@link Card.CardRank}.
     */
    private static HandNumericRank evaluatePairings(Card[] cards) {
        // Group cards by their rank, so it will be easier to lookup for quads / trips / pairs.
        Map<Card.CardRank, List<Card>> cardRankToCards = Arrays.stream(cards).collect(Collectors.groupingBy(Card::getCardRank));

        HandRank handRank = HandRank.NONE;
        int handScore = sumCardsRank(cards);

        for (List<Card> cardsOfSameRank : cardRankToCards.values()) {
            int cardRankOrdinal = cardsOfSameRank.get(0).getCardRank().ordinal();

            // Skip none, so we will be able to calculate score even if there are less than 5 cards.
            if (cardRankOrdinal == Card.CardRank.NONE.ordinal()) {
                continue;
            }

            // Quads
            if (cardsOfSameRank.size() == 4) {
                handRank = HandRank.QUADS;
            }
            // Trips or Full House
            else if (cardsOfSameRank.size() == 3) {
                if (handRank == HandRank.PAIR) {
                    handRank = HandRank.FULL_HOUSE;
                } else {
                    handRank = HandRank.TRIPS;
                }
            }
            // Pair, Two pairs, or Full House.
            else if (cardsOfSameRank.size() == 2) {
                // Make sure we do not override QUADS
                if (handRank != HandRank.QUADS) {
                    if (handRank == HandRank.PAIR) {
                        handRank = HandRank.TWO_PAIRS;
                    } else if (handRank == HandRank.TRIPS) {
                        handRank = HandRank.FULL_HOUSE;
                    } else {
                        handRank = HandRank.PAIR;
                    }
                }
            }
        }

        return new HandNumericRank(handRank, handScore);
    }

    /**
     * A helper method we use in order to check set of 5 cards and see if there is a straight out of those cards.<br/>
     * A straight is a sequence of 5 different cards where the difference between first card and last card rank is 4. e.g. A, 2, 3, 4, 5. or 10, J, Q, K, A.<br/>
     * Note that his method does not check for flush, hence the result can be STRAIGHT, or NONE only, without STRAIGHT_FLUSH.
     *
     * @param cards Set of 5 cards to find straight in
     * @return A {@link HandNumericRank} containing both {@link HandRank} and numeric score, based on involved {@link Card.CardRank}.
     */
    private static HandNumericRank evaluateStraight(Card[] cards) {
        Card.CardRank minimumCardRank = cards[0].getCardRank();

        HandRank handRank = HandRank.NONE;
        int handScore = sumCardsRank(cards);

        // If lowest card rank is none, there is no chance for having a straight, cause we do not have 5 cards..
        // In addition, highest straight is: 10, J, Q, K, A. Hence it is irrelevant to test for straight in case the minimum is J.
        if ((minimumCardRank != Card.CardRank.NONE) && (minimumCardRank.ordinal() <= Card.CardRank.TEN.ordinal())) {
            Set<Card.CardRank> expectedStraight = new HashSet<>(5);
            Set<Card.CardRank> actualSet = Arrays.stream(cards).map(Card::getCardRank).collect(Collectors.toSet());

            // Now check if the starting rank should be first element in the sorted array, or Ace in case we have A, 2, 3, 4, 5
            Card.CardRank startingCardRank = minimumCardRank;
            int startingOrdinal = startingCardRank.ordinal();
            if (actualSet.contains(Card.CardRank.TWO) && actualSet.contains(Card.CardRank.ACE)) {
                startingCardRank = Card.CardRank.ACE;
                startingOrdinal = Card.CardRank.TWO.ordinal() - 1;
            }

            expectedStraight.add(startingCardRank);
            expectedStraight.add(Card.CardRank.values()[startingOrdinal + 1]);
            expectedStraight.add(Card.CardRank.values()[startingOrdinal + 2]);
            expectedStraight.add(Card.CardRank.values()[startingOrdinal + 3]);
            expectedStraight.add(Card.CardRank.values()[startingOrdinal + 4]);

            if (expectedStraight.equals(actualSet)) {
                handRank = HandRank.STRAIGHT;

                // If the straight starts from 1, do not count the ordinal value of Ace, cause Ace has the higher ordinal value
                // and a straight starting from 2 is better than straight starting from 1.
                if (actualSet.contains(Card.CardRank.TWO) && actualSet.contains(Card.CardRank.ACE)) {
                    handScore -= Card.CardRank.ACE.ordinal();
                }
            }
        }

        return new HandNumericRank(handRank, handScore);
    }

    /**
     * Sum up card rank of each card in a specified array
     * @param cards The cards to get score for
     * @return The score for the specified cards
     */
    static int sumCardsRank(Card[] cards) {
        return Arrays.stream(cards).mapToInt(card -> card.getCardRank().ordinal()).sum();
    }
}

