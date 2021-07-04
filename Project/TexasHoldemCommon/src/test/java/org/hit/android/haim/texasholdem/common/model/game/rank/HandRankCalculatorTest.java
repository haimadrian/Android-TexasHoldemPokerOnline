package org.hit.android.haim.texasholdem.common.model.game.rank;

import org.hit.android.haim.texasholdem.common.model.bean.game.Board;
import org.hit.android.haim.texasholdem.common.model.bean.game.Card;
import org.hit.android.haim.texasholdem.common.model.bean.game.Hand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Test several hands to make sure our {@link HandRankCalculator} is working.
 *
 * @author Haim Adrian
 * @since 09-May-21
 */
public class HandRankCalculatorTest {
    public static final String TWO_CLUB = "2♣";
    public static final String TWO_DIAMOND = "2♦";
    public static final String TWO_HEART = "2♥";
    public static final String TWO_SPADE = "2♠";
    public static final String THREE_DIAMOND = "3♦";
    public static final String THREE_HEART = "3♥";
    public static final String FOUR_DIAMOND = "4♦";
    public static final String FIVE_DIAMOND = "5♦";
    public static final String SIX_SPADE = "6♠";
    public static final String SEVEN_CLUB = "7♣";
    public static final String SEVEN_DIAMOND = "7♦";
    public static final String SEVEN_HEART = "7♥";
    public static final String NINE_DIAMOND = "9♦";
    public static final String TEN_CLUB = "T♣";
    public static final String TEN_DIAMOND = "T♦";
    public static final String JACK_CLUB = "J♣";
    public static final String JACK_DIAMOND = "J♦";
    public static final String JACK_HEART = "J♥";
    public static final String JACK_SPADE = "J♠";
    public static final String QUEEN_CLUB = "Q♣";
    public static final String QUEEN_DIAMOND = "Q♦";
    public static final String QUEEN_HEART = "Q♥";
    public static final String QUEEN_SPADE = "Q♠";
    public static final String KING_CLUB = "K♣";
    public static final String KING_DIAMOND = "K♦";
    public static final String ACE_DIAMOND = "A♦";

    private Board board;

    private static Set<Card> newCardsSet(String... cardsStr) {
        Set<Card> cards = new HashSet<>(cardsStr.length);
        for (String card : cardsStr) {
            cards.add(Card.valueOf(card));
        }
        return cards;
    }

    @BeforeEach
    void setUp() {
        board = new Board();
        board.addCard(Card.valueOf(THREE_HEART));
        board.addCard(Card.valueOf(SEVEN_HEART));
        board.addCard(Card.valueOf(JACK_SPADE));
        board.addCard(Card.valueOf(KING_CLUB));
        board.addCard(Card.valueOf(TEN_DIAMOND));
    }

    @Test
    public void testPair_pairOfThreesWithHandAndBoard_success() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(THREE_DIAMOND));
        hand.addCard(Card.valueOf(FIVE_DIAMOND));

        Set<Card> expectedSelection = newCardsSet(THREE_HEART, THREE_DIAMOND, KING_CLUB, JACK_SPADE, TEN_DIAMOND);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.PAIR, calculate.getRank().getHandRank(), "Hand rank supposed to be pair");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })), calculate.getRank().getScore(), "Wrong hand rank");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }

    @Test
    public void testPair_pairOfTwoWithHandOnly_success() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(TWO_DIAMOND));
        hand.addCard(Card.valueOf(TWO_CLUB));

        Set<Card> expectedSelection = newCardsSet(TWO_CLUB, TWO_DIAMOND, KING_CLUB, JACK_SPADE, TEN_DIAMOND);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.PAIR, calculate.getRank().getHandRank(), "Hand rank supposed to be pair");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })), calculate.getRank().getScore(), "Wrong hand rank");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }

    @Test
    public void testHighCard_highCardIsAce_success() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(ACE_DIAMOND));
        hand.addCard(Card.valueOf(TWO_CLUB));

        Set<Card> expectedSelection = newCardsSet(ACE_DIAMOND, SEVEN_HEART, KING_CLUB, JACK_SPADE, TEN_DIAMOND);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.HIGH_CARD, calculate.getRank().getHandRank(), "Hand rank supposed to be high card");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })), calculate.getRank().getScore(), "Wrong hand rank");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }

    @Test
    public void testTwoPairs_pairOfJackAndTen_success() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(JACK_DIAMOND));
        hand.addCard(Card.valueOf(TEN_CLUB));

        Set<Card> expectedSelection = newCardsSet(JACK_DIAMOND, TEN_CLUB, KING_CLUB, JACK_SPADE, TEN_DIAMOND);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.TWO_PAIRS, calculate.getRank().getHandRank(), "Hand rank supposed to be two pairs");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })), calculate.getRank().getScore(), "Wrong hand rank");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }

    @Test
    public void testTrips_tripsOfSeven_success() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(SEVEN_DIAMOND));
        hand.addCard(Card.valueOf(SEVEN_CLUB));

        Set<Card> expectedSelection = newCardsSet(SEVEN_DIAMOND, SEVEN_CLUB, KING_CLUB, JACK_SPADE, SEVEN_HEART);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.TRIPS, calculate.getRank().getHandRank(), "Hand rank supposed to be trips");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })), calculate.getRank().getScore(), "Wrong hand rank");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }

    @Test
    public void testQuads_quadsOfQueen_success() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(QUEEN_DIAMOND));
        hand.addCard(Card.valueOf(QUEEN_CLUB));

        Board board = new Board();
        board.addCard(Card.valueOf(QUEEN_SPADE));
        board.addCard(Card.valueOf(QUEEN_HEART));
        board.addCard(Card.valueOf(JACK_SPADE));
        board.addCard(Card.valueOf(KING_CLUB));
        board.addCard(Card.valueOf(TEN_DIAMOND));

        Set<Card> expectedSelection = newCardsSet(QUEEN_DIAMOND, QUEEN_CLUB, KING_CLUB, QUEEN_SPADE, QUEEN_HEART);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.QUADS, calculate.getRank().getHandRank(), "Hand rank supposed to be quads");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })), calculate.getRank().getScore(), "Wrong hand rank");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }

    @Test
    public void testFlush_flushOfDiamond_success() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(FOUR_DIAMOND));
        hand.addCard(Card.valueOf(FIVE_DIAMOND));

        Board board = new Board();
        board.addCard(Card.valueOf(TWO_SPADE));
        board.addCard(Card.valueOf(TWO_HEART));
        board.addCard(Card.valueOf(THREE_DIAMOND));
        board.addCard(Card.valueOf(JACK_DIAMOND));
        board.addCard(Card.valueOf(TEN_DIAMOND));

        Set<Card> expectedSelection = newCardsSet(JACK_DIAMOND, FOUR_DIAMOND, THREE_DIAMOND, FIVE_DIAMOND, TEN_DIAMOND);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.FLUSH, calculate.getRank().getHandRank(), "Hand rank supposed to be flush");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })), calculate.getRank().getScore(), "Wrong hand rank");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }

    @Test
    public void testStraight_straightFrom1To5_success() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(ACE_DIAMOND));
        hand.addCard(Card.valueOf(TWO_DIAMOND));

        Board board = new Board();
        board.addCard(Card.valueOf(TEN_CLUB));
        board.addCard(Card.valueOf(JACK_SPADE));
        board.addCard(Card.valueOf(THREE_HEART));
        board.addCard(Card.valueOf(FOUR_DIAMOND));
        board.addCard(Card.valueOf(FIVE_DIAMOND));

        Set<Card> expectedSelection = newCardsSet(ACE_DIAMOND, TWO_DIAMOND, THREE_HEART, FOUR_DIAMOND, FIVE_DIAMOND);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.STRAIGHT, calculate.getRank().getHandRank(), "Hand rank supposed to be straight");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })) - Card.CardRank.ACE.ordinal(), calculate.getRank().getScore(), "Wrong hand rank. When straight starts from A, A is not counted into score");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }

    @Test
    public void testStraight_straightFrom1To6_straightFrom2To6IsSelected() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(ACE_DIAMOND));
        hand.addCard(Card.valueOf(TWO_DIAMOND));

        Board board = new Board();
        board.addCard(Card.valueOf(TEN_CLUB));
        board.addCard(Card.valueOf(SIX_SPADE));
        board.addCard(Card.valueOf(THREE_HEART));
        board.addCard(Card.valueOf(FOUR_DIAMOND));
        board.addCard(Card.valueOf(FIVE_DIAMOND));

        Set<Card> expectedSelection = newCardsSet(SIX_SPADE, TWO_DIAMOND, THREE_HEART, FOUR_DIAMOND, FIVE_DIAMOND);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.STRAIGHT, calculate.getRank().getHandRank(), "Hand rank supposed to be straight");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })), calculate.getRank().getScore(), "Wrong hand rank");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }

    @Test
    public void testStraightFlush_straightFlushFrom1To5_success() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(ACE_DIAMOND));
        hand.addCard(Card.valueOf(TWO_DIAMOND));

        Board board = new Board();
        board.addCard(Card.valueOf(TWO_SPADE));
        board.addCard(Card.valueOf(TWO_HEART));
        board.addCard(Card.valueOf(THREE_DIAMOND));
        board.addCard(Card.valueOf(FOUR_DIAMOND));
        board.addCard(Card.valueOf(FIVE_DIAMOND));

        Set<Card> expectedSelection = newCardsSet(ACE_DIAMOND, TWO_DIAMOND, THREE_DIAMOND, FOUR_DIAMOND, FIVE_DIAMOND);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.STRAIGHT_FLUSH, calculate.getRank().getHandRank(), "Hand rank supposed to be straight flush");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })) - Card.CardRank.ACE.ordinal(), calculate.getRank().getScore(), "Wrong hand rank. When straight starts from A, A is not counted into score");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }

    @Test
    public void testRoyalFlush_straightFlushFrom9ToA_royalFlushIsSelected() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(ACE_DIAMOND));
        hand.addCard(Card.valueOf(NINE_DIAMOND));

        Board board = new Board();
        board.addCard(Card.valueOf(QUEEN_DIAMOND));
        board.addCard(Card.valueOf(KING_DIAMOND));
        board.addCard(Card.valueOf(KING_CLUB));
        board.addCard(Card.valueOf(TEN_DIAMOND));
        board.addCard(Card.valueOf(JACK_DIAMOND));

        Set<Card> expectedSelection = newCardsSet(ACE_DIAMOND, QUEEN_DIAMOND, KING_DIAMOND, TEN_DIAMOND, JACK_DIAMOND);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.ROYAL_FLUSH, calculate.getRank().getHandRank(), "Hand rank supposed to be royal flush");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })), calculate.getRank().getScore(), "Wrong hand rank");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }

    @Test
    public void testFullHouse_fullHouseUsingThree2AndTwoJacks_success() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(TWO_DIAMOND));
        hand.addCard(Card.valueOf(JACK_CLUB));

        Board board = new Board();
        board.addCard(Card.valueOf(TWO_SPADE));
        board.addCard(Card.valueOf(TWO_HEART));
        board.addCard(Card.valueOf(JACK_SPADE));
        board.addCard(Card.valueOf(KING_CLUB));
        board.addCard(Card.valueOf(TEN_DIAMOND));

        Set<Card> expectedSelection = newCardsSet(TWO_DIAMOND, JACK_CLUB, JACK_SPADE, TWO_SPADE, TWO_HEART);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.FULL_HOUSE, calculate.getRank().getHandRank(), "Hand rank supposed to be full house");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })), calculate.getRank().getScore(), "Wrong hand rank");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }

    @Test
    public void testFullHouse_fullHouseUsingThree2AndThreeJacks_selectThreeJacks() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(TWO_DIAMOND));
        hand.addCard(Card.valueOf(JACK_CLUB));

        Board board = new Board();
        board.addCard(Card.valueOf(TWO_SPADE));
        board.addCard(Card.valueOf(TWO_HEART));
        board.addCard(Card.valueOf(JACK_SPADE));
        board.addCard(Card.valueOf(JACK_HEART));
        board.addCard(Card.valueOf(TEN_DIAMOND));

        Set<Card> expectedSelection = newCardsSet(JACK_HEART, JACK_CLUB, JACK_SPADE, TWO_SPADE, TWO_HEART);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.FULL_HOUSE, calculate.getRank().getHandRank(), "Hand rank supposed to be full house");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })), calculate.getRank().getScore(), "Wrong hand rank. Supposed to select jacks");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }

    @Test
    public void testFullHouse_fullHouseOrFlush_fullHouseWins() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(TWO_DIAMOND));
        hand.addCard(Card.valueOf(JACK_CLUB));
        Hand hand2 = new Hand();
        hand2.addCard(Card.valueOf(FOUR_DIAMOND));
        hand2.addCard(Card.valueOf(FIVE_DIAMOND));

        Board board = new Board();
        board.addCard(Card.valueOf(TWO_SPADE));
        board.addCard(Card.valueOf(TWO_HEART));
        board.addCard(Card.valueOf(THREE_DIAMOND));
        board.addCard(Card.valueOf(JACK_DIAMOND));
        board.addCard(Card.valueOf(TEN_DIAMOND));

        Set<Card> expectedSelection = newCardsSet(JACK_DIAMOND, JACK_CLUB, TWO_DIAMOND, TWO_SPADE, TWO_HEART);

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);
        HandRankCalculatorResult calculate2 = HandRankCalculator.calculate(board, hand2);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.FULL_HOUSE, calculate.getRank().getHandRank(), "Hand rank supposed to be full house");
        Assertions.assertEquals(HandRank.FLUSH, calculate2.getRank().getHandRank(), "Hand rank supposed to be flush");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })), calculate.getRank().getScore(), "Wrong hand rank");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
        Assertions.assertTrue(calculate.compareTo(calculate2) > 0, "First hand (full house) supposed to win");
    }

    @Test
    public void testPairWithPartialBoard_pairOfTwoWithHandOnly_success() {
        // Arrange
        Hand hand = new Hand();
        hand.addCard(Card.valueOf(TWO_DIAMOND));
        hand.addCard(Card.valueOf(TWO_CLUB));

        Set<Card> expectedSelection = newCardsSet(TWO_CLUB, TWO_DIAMOND, KING_CLUB, JACK_SPADE, TEN_DIAMOND);

        Board board = new Board();
        board.addCard(Card.valueOf(JACK_SPADE));
        board.addCard(Card.valueOf(KING_CLUB));
        board.addCard(Card.valueOf(TEN_DIAMOND));

        // Act
        HandRankCalculatorResult calculate = HandRankCalculator.calculate(board, hand);

        // Assert
        Assertions.assertNotNull(calculate, "Calculation result was null");
        Assertions.assertEquals(HandRank.PAIR, calculate.getRank().getHandRank(), "Hand rank supposed to be pair");
        Assertions.assertEquals(HandRankCalculator.sumCardsRank(expectedSelection.toArray(new Card[] { })), calculate.getRank().getScore(), "Wrong hand rank");
        Assertions.assertEquals(expectedSelection, new HashSet<>(Arrays.asList(calculate.getSelectedCards())), "Wrong winning hand selection");
    }
}

