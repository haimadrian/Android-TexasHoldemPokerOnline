package org.hit.android.haim.texasholdem.common.model.game;

import org.hit.android.haim.texasholdem.common.model.bean.game.*;
import org.hit.android.haim.texasholdem.common.util.JsonUtils;
import org.hit.android.haim.texasholdem.common.util.ThreadContextMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Haim Adrian
 * @since 03-Jul-21
 */
public class GameEngineTest {
    private GameEngine gameEngine;
    private Player player;
    private Hand hand;
    private Card flop1;
    private Card flop2;
    private Card flop3;
    private Player player1;
    private Player player2;
    private Player player3;

    @BeforeEach
    public void setup() {
        gameEngine = new GameEngine(new GameSettings(1, 2, 60000, "id", true), (player, chips) -> {

        });

        hand = new Hand();
        hand.addCard(new Card(Card.CardRank.ACE, Card.CardSuit.CLUB));
        hand.addCard(new Card(Card.CardRank.KING, Card.CardSuit.CLUB));

        player = new Player("id", "name", new Chips(500), false, hand, 1);
        gameEngine.getPlayers().addPlayer(player);

        flop1 = new Card(Card.CardRank.QUEEN, Card.CardSuit.CLUB);
        flop2 = new Card(Card.CardRank.JACK, Card.CardSuit.CLUB);
        flop3 = new Card(Card.CardRank.TEN, Card.CardSuit.CLUB);
        gameEngine.getBoard().addCard(flop1);
        gameEngine.getBoard().addCard(flop2);
        gameEngine.getBoard().addCard(flop3);
    }

    @AfterEach
    public void tearDown() {
        gameEngine.stop();
        gameEngine = null;
        player = null;
        hand = null;
        flop1 = null;
        flop2 = null;
        flop3 = null;
    }

    @Test
    public void testGameEngineToJson_makeSureJacksonDoesNotFail() {
        Throwable t = null;
        GameEngine gameEngine1 = null;
        try {
            String json = JsonUtils.writeValueAsString(gameEngine);
            System.out.println(json);

            gameEngine1 = JsonUtils.readValueFromString(json, GameEngine.class);
            System.out.println(gameEngine1);
        } catch (Exception e) {
            t = e;
            e.printStackTrace();
        }

        Assertions.assertNull(t, "No exception supposed to occur");
        Assertions.assertNotNull(gameEngine1, "Json supposed to be deserialized");
        Assertions.assertEquals(1, gameEngine1.getPlayers().size(), "We have added one player");
    }

    @Test
    public void testGameEngineToJson_addHandAndBoard_makeSureJacksonDoesNotFail() {
        Throwable t = null;
        GameEngine gameEngine1 = null;
        try {
            ThreadContextMap.getInstance().setUserId("id");
            String json = JsonUtils.writeValueAsString(gameEngine);
            System.out.println(json);

            gameEngine1 = JsonUtils.readValueFromString(json, GameEngine.class);
            System.out.println(gameEngine1);
        } catch (Exception e) {
            t = e;
            e.printStackTrace();
        }

        Assertions.assertNull(t, "No exception supposed to occur");
        Assertions.assertNotNull(gameEngine1, "Json supposed to be deserialized");
        Assertions.assertEquals(1, gameEngine1.getPlayers().size(), "We have added one player");
        Assertions.assertEquals(hand, gameEngine1.getPlayers().getPlayer(1).getHand(), "Hand lost");
        Assertions.assertEquals(flop1, gameEngine1.getBoard().getFlop1().orElse(null), "Missing board card");
        Assertions.assertEquals(flop2, gameEngine1.getBoard().getFlop2().orElse(null), "Missing board card");
        Assertions.assertEquals(flop3, gameEngine1.getBoard().getFlop3().orElse(null), "Missing board card");
    }

    @Test
    public void testGameEngineToJson_addHandAndBoard_makeSureJacksonDoesNotFailAndHandIsHidden() {
        Throwable t = null;
        GameEngine gameEngine1 = null;
        try {
            // When the requesting thread does not have the user id, hand should be null
            //ThreadContextMap.getInstance().setUserId("id");
            String json = JsonUtils.writeValueAsString(gameEngine);
            System.out.println(json);

            gameEngine1 = JsonUtils.readValueFromString(json, GameEngine.class);
            System.out.println(gameEngine1);
        } catch (Exception e) {
            t = e;
            e.printStackTrace();
        }

        Assertions.assertNull(t, "No exception supposed to occur");
        Assertions.assertNotNull(gameEngine1, "Json supposed to be deserialized");
        Assertions.assertEquals(1, gameEngine1.getPlayers().size(), "We have added one player");
        Assertions.assertNull(gameEngine1.getPlayers().getPlayer(1).getHand(), "Hand supposed to be hidden");
        Assertions.assertEquals(flop1, gameEngine1.getBoard().getFlop1().orElse(null), "Missing board card");
        Assertions.assertEquals(flop2, gameEngine1.getBoard().getFlop2().orElse(null), "Missing board card");
        Assertions.assertEquals(flop3, gameEngine1.getBoard().getFlop3().orElse(null), "Missing board card");
    }

    @Test
    public void testTurnTimeout() {
        // Arrange
        long turnTimeMillis = 3000;
        Map<Player, Long> playerChipsUpdates = new HashMap<>();
        GameEngine gameEngine = new GameEngine(new GameSettings(1, 2, turnTimeMillis, "id", true), (player, chips) -> playerChipsUpdates.compute(player, (existingPlayer, existingChips) -> existingChips == null ? chips : existingChips + chips));

        try {
            // Players join the game
            Player player1 = new Player("id1", "name1", new Chips(500), false, null, 1);
            Player player2 = new Player("id2", "name2", new Chips(300), false, null, 2);
            Player player3 = new Player("id3", "name3", new Chips(1000), false, null, 6);
            gameEngine.getPlayers().addPlayer(player1);
            gameEngine.getPlayers().addPlayer(player2);
            gameEngine.getPlayers().addPlayer(player3);

            // Admin starts the game
            gameEngine.start();

            Assertions.assertNotNull(gameEngine.getDealer(), "Dealer supposed to be selected");
            Assertions.assertNull(gameEngine.getBoard().getFlop1().orElse(null), "Flop was not supposed to be displayed yet");

            // Verify fold due to timeout (big blind folds)
            Player currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            long chipsBeforeFold = currPlayer.getChips().get();
            try {
                // Wait enough time so current player will fold.
                // Double the time because the scheduler runs every 3 seconds
                Thread.sleep(turnTimeMillis * 2);
            } catch (InterruptedException ignore) {
            }
            Assertions.assertFalse(currPlayer.isPlaying(), "Player supposed to fold due to time out");
            Assertions.assertNull(gameEngine.getBoard().getFlop1().orElse(null), "Flop was not supposed to be displayed yet");
            Assertions.assertEquals(chipsBeforeFold, currPlayer.getChips().get(), "Player folded. No chips modification is expected");
            Assertions.assertEquals(currPlayer.getName() + " folded.", gameEngine.getGameLog().getLastPlayerAction().toString());
        } finally {
            gameEngine.stop();
        }
    }

    @Test
    public void simulateFullGameFlowUntilLastRound_lastPlayerFolds_prevPlayerWins() {
        GameEngine gameEngine = null;
        try {
            gameEngine = simulateFullGameFlowUntilLastRoundInclusive();

            // Fold (small blind player)
            Player prevPlayer = gameEngine.getPlayers().getPreviousPlayer();
            Player currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            long chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.FOLD).build());
            Assertions.assertTrue(gameEngine.getBoard().hasRiver(), "River supposed to be displayed");
            Assertions.assertEquals(chipsBeforeCall, currPlayer.getChips().get(), "No chips should be removed when FOLD.");

            Map<String, Pot.PlayerWinning> playerToEarnings = gameEngine.getPlayerToEarnings();
            Assertions.assertEquals(GameEngine.GameState.RESTART, gameEngine.getGameState(), "Game supposed to be in RESTART state");
            Assertions.assertNotNull(playerToEarnings, "Game supposed to end");
            Assertions.assertFalse(playerToEarnings.isEmpty(), "There should be a winner");
            Assertions.assertTrue(playerToEarnings.containsKey(prevPlayer.getId()), prevPlayer.getName() + " supposed to win");
            Assertions.assertTrue(gameEngine.getGameLog().getLastPlayerAction().toString().contains(prevPlayer.getName() + " won"));
            Assertions.assertNotEquals(500, player1.getChips().get(), "Chips must be modified");
            Assertions.assertNotEquals(300, player2.getChips().get(), "Chips must be modified");
            Assertions.assertNotEquals(1000, player3.getChips().get(), "Chips must be modified");

            long actualEarning;
            if (prevPlayer.equals(player1)) {
                actualEarning = 300 - player2.getChips().get() + 1000 - player3.getChips().get();
                Assertions.assertEquals(actualEarning, player1.getChips().get() - 500, "Not expected earning");
                Assertions.assertNotNull(playerToEarnings.get(player1.getId()).getHandRank().getHand(), "Hand supposed to be available");
                Assertions.assertTrue(playerToEarnings.get(player1.getId()).getSum() > actualEarning, "Sum supposed to contain earning and own bet");
            } else if (prevPlayer.equals(player2)) {
                actualEarning = 500 - player1.getChips().get() + 1000 - player3.getChips().get();
                Assertions.assertEquals(actualEarning, player2.getChips().get() - 300, "Not expected earning");
                Assertions.assertNotNull(playerToEarnings.get(player2.getId()).getHandRank().getHand(), "Hand supposed to be available");
                Assertions.assertTrue(playerToEarnings.get(player2.getId()).getSum() > actualEarning, "Sum supposed to contain earning and own bet");
            } else {
                actualEarning = 300 - player2.getChips().get() + 500 - player1.getChips().get();
                Assertions.assertEquals(actualEarning, player3.getChips().get() - 1000, "Not expected earning");
                Assertions.assertNotNull(playerToEarnings.get(player3.getId()).getHandRank().getHand(), "Hand supposed to be available");
                Assertions.assertTrue(playerToEarnings.get(player3.getId()).getSum() > actualEarning, "Sum supposed to contain earning and own bet");
            }
        } finally {
            if (gameEngine != null) {
                gameEngine.stop();
            }
        }
    }

    @Test
    public void simulateFullGameFlowUntilLastRound_lastPlayerCalls_someoneWins() {
        GameEngine gameEngine = null;
        try {
            gameEngine = simulateFullGameFlowUntilLastRoundInclusive();

            // Call (small blind player)
            Player currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            long chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CALL).build());
            Assertions.assertTrue(gameEngine.getBoard().hasRiver(), "River supposed to be displayed");
            Assertions.assertNotEquals(chipsBeforeCall, currPlayer.getChips().get(), "Chips supposed to be modified.");

            Map<String, Pot.PlayerWinning> playerToEarnings = gameEngine.getPlayerToEarnings();
            Assertions.assertEquals(GameEngine.GameState.RESTART, gameEngine.getGameState(), "Game supposed to be in RESTART state");
            Assertions.assertNotNull(playerToEarnings, "Game supposed to end");
            Assertions.assertFalse(playerToEarnings.isEmpty(), "There should be a winner");
            Assertions.assertNotEquals(500, player1.getChips().get(), "Chips must be modified");
            Assertions.assertNotEquals(300, player2.getChips().get(), "Chips must be modified");
            Assertions.assertNotEquals(1000, player3.getChips().get(), "Chips must be modified");
        } finally {
            if (gameEngine != null) {
                gameEngine.stop();
            }
        }
    }

    @Test
    public void simulateFullGameFlowUntilTurnRound_lastPlayerFolds_prevPlayerWins() {
        GameEngine gameEngine = null;
        try {
            gameEngine = simulateFullGameFlowUntilTurnRound();

            // Fold (small blind player)
            Player currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            long chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.FOLD).build());
            Assertions.assertFalse(gameEngine.getBoard().hasTurn(), "Turn was not supposed to be displayed");
            Assertions.assertFalse(gameEngine.getBoard().hasRiver(), "River was not supposed to be displayed");
            Assertions.assertEquals(chipsBeforeCall, currPlayer.getChips().get(), "No chips should be removed when FOLD.");
            Assertions.assertEquals(currPlayer.getName() + " folded.", gameEngine.getGameLog().getLastPlayerAction().toString());

            // Raise (big blind raise)
            currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.RAISE).chips(new Chips(20)).build());
            Assertions.assertFalse(gameEngine.getBoard().hasTurn(), "Turn was not supposed to be displayed yet");
            Assertions.assertEquals(chipsBeforeCall - 20, currPlayer.getChips().get(), "Player raised 20 chips");
            Assertions.assertEquals(currPlayer.getName() + " raised by 20.", gameEngine.getGameLog().getLastPlayerAction().toString());

            // Dealer folds
            Player prevPlayer = gameEngine.getPlayers().getPreviousPlayer();
            currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.FOLD).build());
            Assertions.assertFalse(gameEngine.getBoard().hasTurn(), "Turn was not supposed to be displayed yet");
            Assertions.assertFalse(gameEngine.getBoard().hasRiver(), "River was not supposed to be displayed");
            Assertions.assertEquals(chipsBeforeCall, currPlayer.getChips().get(), "No chips should be removed when FOLD.");

            Map<String, Pot.PlayerWinning> playerToEarnings = gameEngine.getPlayerToEarnings();
            Assertions.assertEquals(GameEngine.GameState.RESTART, gameEngine.getGameState(), "Game supposed to be in RESTART state");
            Assertions.assertNotNull(playerToEarnings, "Game supposed to end");
            Assertions.assertFalse(playerToEarnings.isEmpty(), "There should be a winner");
            Assertions.assertTrue(playerToEarnings.containsKey(prevPlayer.getId()), prevPlayer.getName() + " supposed to win");
            Assertions.assertTrue(gameEngine.getGameLog().getLastPlayerAction().toString().contains(prevPlayer.getName() + " won"));
            Assertions.assertNotEquals(500, player1.getChips().get(), "Chips must be modified");
            Assertions.assertNotEquals(300, player2.getChips().get(), "Chips must be modified");
            Assertions.assertNotEquals(1000, player3.getChips().get(), "Chips must be modified");

            long actualEarning;
            if (prevPlayer.equals(player1)) {
                actualEarning = 300 - player2.getChips().get() + 1000 - player3.getChips().get();
                Assertions.assertEquals(actualEarning, player1.getChips().get() - 500, "Not expected earning");
                Assertions.assertNotNull(playerToEarnings.get(player1.getId()).getHandRank().getHand(), "Hand supposed to be available");
                Assertions.assertTrue(playerToEarnings.get(player1.getId()).getSum() > actualEarning, "Sum supposed to contain earning and own bet");
            } else if (prevPlayer.equals(player2)) {
                actualEarning = 500 - player1.getChips().get() + 1000 - player3.getChips().get();
                Assertions.assertEquals(actualEarning, player2.getChips().get() - 300, "Not expected earning");
                Assertions.assertNotNull(playerToEarnings.get(player2.getId()).getHandRank().getHand(), "Hand supposed to be available");
                Assertions.assertTrue(playerToEarnings.get(player2.getId()).getSum() > actualEarning, "Sum supposed to contain earning and own bet");
            } else {
                actualEarning = 300 - player2.getChips().get() + 500 - player1.getChips().get();
                Assertions.assertEquals(actualEarning, player3.getChips().get() - 1000, "Not expected earning");
                Assertions.assertNotNull(playerToEarnings.get(player3.getId()).getHandRank().getHand(), "Hand supposed to be available");
                Assertions.assertTrue(playerToEarnings.get(player3.getId()).getSum() > actualEarning, "Sum supposed to contain earning and own bet");
            }
        } finally {
            if (gameEngine != null) {
                gameEngine.stop();
            }
        }
    }

    @Test
    public void simulateFullGameFlowUntilTurnRound_dealerFolds_gameShouldUseAvailableDealerWins() {
        GameEngine gameEngine = null;
        try {
            gameEngine = simulateFullGameFlowUntilTurnRound();

            // Check (small blind player)
            Player currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            long chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CHECK).build());
            Assertions.assertFalse(gameEngine.getBoard().hasTurn(), "Turn was not supposed to be displayed");
            Assertions.assertFalse(gameEngine.getBoard().hasRiver(), "River was not supposed to be displayed");
            Assertions.assertEquals(chipsBeforeCall, currPlayer.getChips().get(), "Player checked, hence no change in chips");
            Assertions.assertEquals(currPlayer.getName() + " checked.", gameEngine.getGameLog().getLastPlayerAction().toString());

            // Raise (big blind raise)
            currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.RAISE).chips(new Chips(20)).build());
            Assertions.assertFalse(gameEngine.getBoard().hasTurn(), "Turn was not supposed to be displayed yet");
            Assertions.assertEquals(chipsBeforeCall - 20, currPlayer.getChips().get(), "Player raised 20 chips");
            Assertions.assertEquals(currPlayer.getName() + " raised by 20.", gameEngine.getGameLog().getLastPlayerAction().toString());

            // Dealer folds
            currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.FOLD).build());
            Assertions.assertFalse(gameEngine.getBoard().hasTurn(), "Turn was not supposed to be displayed yet");
            Assertions.assertFalse(gameEngine.getBoard().hasRiver(), "River was not supposed to be displayed");
            Assertions.assertEquals(chipsBeforeCall, currPlayer.getChips().get(), "No chips should be removed when FOLD.");

            // Call (Small blind should call or fold)
            currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CALL).build());
            Assertions.assertTrue(gameEngine.getBoard().hasTurn(), "Turn supposed to be displayed");
            Assertions.assertFalse(gameEngine.getBoard().hasRiver(), "River was not supposed to be displayed");
            Assertions.assertEquals(chipsBeforeCall - 20, currPlayer.getChips().get(), "Player called 20 chips");
            Assertions.assertEquals(currPlayer.getName() + " called 20.", gameEngine.getGameLog().getLastPlayerAction().toString());

            // Check (small blind player)
            currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CHECK).build());
            Assertions.assertTrue(gameEngine.getBoard().hasTurn(), "Turn supposed to be displayed");
            Assertions.assertFalse(gameEngine.getBoard().hasRiver(), "River was not supposed to be displayed");
            Assertions.assertEquals(chipsBeforeCall, currPlayer.getChips().get(), "Player checked, hence no change in chips");
            Assertions.assertEquals(currPlayer.getName() + " checked.", gameEngine.getGameLog().getLastPlayerAction().toString());

            // Raise (big blind raise)
            currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.RAISE).chips(new Chips(20)).build());
            Assertions.assertTrue(gameEngine.getBoard().hasTurn(), "Turn supposed to be displayed");
            Assertions.assertFalse(gameEngine.getBoard().hasRiver(), "River was not supposed to be displayed");
            Assertions.assertEquals(chipsBeforeCall - 20, currPlayer.getChips().get(), "Player raised 20 chips");
            Assertions.assertEquals(currPlayer.getName() + " raised by 20.", gameEngine.getGameLog().getLastPlayerAction().toString());

            // Call (Small blind should call or fold)
            currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CALL).build());
            Assertions.assertTrue(gameEngine.getBoard().hasRiver(), "River supposed to be displayed");
            Assertions.assertEquals(chipsBeforeCall - 20, currPlayer.getChips().get(), "Player called 20 chips");
            Assertions.assertEquals(currPlayer.getName() + " called 20.", gameEngine.getGameLog().getLastPlayerAction().toString());

            // Raise (small blind player)
            currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.RAISE).chips(new Chips(20)).build());
            Assertions.assertTrue(gameEngine.getBoard().hasRiver(), "River supposed to be displayed");
            Assertions.assertEquals(chipsBeforeCall - 20, currPlayer.getChips().get(), "Player raised 20 chips");
            Assertions.assertEquals(currPlayer.getName() + " raised by 20.", gameEngine.getGameLog().getLastPlayerAction().toString());

            // Fold (big blind)
            Player prevPlayer = gameEngine.getPlayers().getPreviousPlayer();
            currPlayer = gameEngine.getPlayers().getCurrentPlayer();
            chipsBeforeCall = currPlayer.getChips().get();
            gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.FOLD).build());
            Assertions.assertEquals(chipsBeforeCall, currPlayer.getChips().get(), "No chips should be removed when FOLD.");

            Map<String, Pot.PlayerWinning> playerToEarnings = gameEngine.getPlayerToEarnings();
            Assertions.assertEquals(GameEngine.GameState.RESTART, gameEngine.getGameState(), "Game supposed to be in RESTART state");
            Assertions.assertNotNull(playerToEarnings, "Game supposed to end");
            Assertions.assertFalse(playerToEarnings.isEmpty(), "There should be a winner");
            Assertions.assertTrue(playerToEarnings.containsKey(prevPlayer.getId()), prevPlayer.getName() + " supposed to win");
            Assertions.assertTrue(gameEngine.getGameLog().getLastPlayerAction().toString().contains(prevPlayer.getName() + " won"));
            Assertions.assertNotEquals(500, player1.getChips().get(), "Chips must be modified");
            Assertions.assertNotEquals(300, player2.getChips().get(), "Chips must be modified");
            Assertions.assertNotEquals(1000, player3.getChips().get(), "Chips must be modified");

            long actualEarning;
            if (prevPlayer.equals(player1)) {
                actualEarning = 300 - player2.getChips().get() + 1000 - player3.getChips().get();
                Assertions.assertEquals(actualEarning, player1.getChips().get() - 500, "Not expected earning");
                Assertions.assertNotNull(playerToEarnings.get(player1.getId()).getHandRank().getHand(), "Hand supposed to be available");
                Assertions.assertTrue(playerToEarnings.get(player1.getId()).getSum() > actualEarning, "Sum supposed to contain earning and own bet");
            } else if (prevPlayer.equals(player2)) {
                actualEarning = 500 - player1.getChips().get() + 1000 - player3.getChips().get();
                Assertions.assertEquals(actualEarning, player2.getChips().get() - 300, "Not expected earning");
                Assertions.assertNotNull(playerToEarnings.get(player2.getId()).getHandRank().getHand(), "Hand supposed to be available");
                Assertions.assertTrue(playerToEarnings.get(player2.getId()).getSum() > actualEarning, "Sum supposed to contain earning and own bet");
            } else {
                actualEarning = 300 - player2.getChips().get() + 500 - player1.getChips().get();
                Assertions.assertEquals(actualEarning, player3.getChips().get() - 1000, "Not expected earning");
                Assertions.assertNotNull(playerToEarnings.get(player3.getId()).getHandRank().getHand(), "Hand supposed to be available");
                Assertions.assertTrue(playerToEarnings.get(player3.getId()).getSum() > actualEarning, "Sum supposed to contain earning and own bet");
            }
        } finally {
            if (gameEngine != null) {
                gameEngine.stop();
            }
        }
    }

    private GameEngine simulateFullGameFlowUntilTurnRound() {
        // Arrange
        Map<Player, Long> playerChipsUpdates = new HashMap<>();
        GameEngine gameEngine = new GameEngine(new GameSettings(1, 2, 60000, "id", true), (player, chips) -> playerChipsUpdates.compute(player, (existingPlayer, existingChips) -> existingChips == null ? chips : existingChips + chips));

        // Players join the game
        player1 = new Player("id1", "name1", new Chips(500), false, null, 1);
        player2 = new Player("id2", "name2", new Chips(300), false, null, 2);
        player3 = new Player("id3", "name3", new Chips(1000), false, null, 6);
        gameEngine.getPlayers().addPlayer(player1);
        gameEngine.getPlayers().addPlayer(player2);
        gameEngine.getPlayers().addPlayer(player3);

        // Admin starts the game
        gameEngine.start();

        Assertions.assertNotNull(gameEngine.getDealer(), "Dealer supposed to be selected");
        Assertions.assertNull(gameEngine.getBoard().getFlop1().orElse(null), "Flop was not supposed to be displayed yet");
        Assertions.assertEquals(2, player1.getHand().size(), "Player supposed to get 2 cards");
        Assertions.assertEquals(2, player2.getHand().size(), "Player supposed to get 2 cards");
        Assertions.assertEquals(2, player3.getHand().size(), "Player supposed to get 2 cards");

        // Dealer calls
        Player currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        long chipsBeforeCall = currPlayer.getChips().get();
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CALL).build());
        Assertions.assertNull(gameEngine.getBoard().getFlop1().orElse(null), "Flop was not supposed to be displayed yet");
        Assertions.assertEquals(chipsBeforeCall - 2, currPlayer.getChips().get(), "Player called 2 chips");
        Assertions.assertEquals(currPlayer.getName() + " called 2.", gameEngine.getGameLog().getLastPlayerAction().toString());

        // Small blind calls
        currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        chipsBeforeCall = currPlayer.getChips().get();
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CALL).build());
        Assertions.assertNull(gameEngine.getBoard().getFlop1().orElse(null), "Flop was not supposed to be displayed yet");
        Assertions.assertEquals(chipsBeforeCall - 1, currPlayer.getChips().get(), "Player called 1 chips. Small blind already paid 1");
        Assertions.assertEquals(currPlayer.getName() + " called 1.", gameEngine.getGameLog().getLastPlayerAction().toString());

        // Big blind checks
        currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        chipsBeforeCall = currPlayer.getChips().get();
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CHECK).build());
        Assertions.assertNotNull(gameEngine.getBoard().getFlop1().orElse(null), "Flop supposed to be displayed");
        Assertions.assertEquals(chipsBeforeCall, currPlayer.getChips().get(), "Player checked, hence no change in chips");
        Assertions.assertEquals(currPlayer.getName() + " checked.", gameEngine.getGameLog().getLastPlayerAction().toString());

        return gameEngine;
    }

    private GameEngine simulateFullGameFlowUntilRiverRound() {
        GameEngine gameEngine = simulateFullGameFlowUntilTurnRound();

        // Small blind bet
        Player currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        long chipsBeforeCall = currPlayer.getChips().get();
        Assertions.assertEquals(gameEngine.getSmallBlindPlayer(), currPlayer, "Current player supposed to be small blind");
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.RAISE).chips(new Chips(10)).build());
        Assertions.assertFalse(gameEngine.getBoard().hasTurn(), "Turn was not supposed to be displayed yet");
        Assertions.assertEquals(chipsBeforeCall - 10, currPlayer.getChips().get(), "Player raised 10 chips");
        Assertions.assertEquals(currPlayer.getName() + " raised by 10.", gameEngine.getGameLog().getLastPlayerAction().toString());

        // Verify re-raise (big blind calls)
        currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        chipsBeforeCall = currPlayer.getChips().get();
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CALL).build());
        Assertions.assertFalse(gameEngine.getBoard().hasTurn(), "Turn was not supposed to be displayed yet");
        Assertions.assertEquals(chipsBeforeCall - 10, currPlayer.getChips().get(), "Player called 10 chips.");
        Assertions.assertEquals(currPlayer.getName() + " called 10.", gameEngine.getGameLog().getLastPlayerAction().toString());

        // Verify re-raise (dealer re-raise)
        currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        chipsBeforeCall = currPlayer.getChips().get();
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.RAISE).chips(new Chips(20)).build());
        Assertions.assertFalse(gameEngine.getBoard().hasTurn(), "Turn was not supposed to be displayed yet");
        Assertions.assertEquals(chipsBeforeCall - 20, currPlayer.getChips().get(), "Player raised 20 chips");
        Assertions.assertEquals(currPlayer.getName() + " raised by 20.", gameEngine.getGameLog().getLastPlayerAction().toString());

        // Call (small blind calls)
        currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        chipsBeforeCall = currPlayer.getChips().get();
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CALL).build());
        Assertions.assertFalse(gameEngine.getBoard().hasTurn(), "Turn was not supposed to be displayed yet");
        Assertions.assertEquals(chipsBeforeCall - 10, currPlayer.getChips().get(), "Player called 10 chips in addition to previous 10.");
        Assertions.assertEquals(currPlayer.getName() + " called 10.", gameEngine.getGameLog().getLastPlayerAction().toString());

        // Call (big blind calls)
        currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        chipsBeforeCall = currPlayer.getChips().get();
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CALL).build());
        Assertions.assertTrue(gameEngine.getBoard().hasTurn(), "Turn supposed to be displayed");
        Assertions.assertEquals(chipsBeforeCall - 10, currPlayer.getChips().get(), "Player called 10 chips in addition to previous 10.");
        Assertions.assertEquals(currPlayer.getName() + " called 10.", gameEngine.getGameLog().getLastPlayerAction().toString());

        return gameEngine;
    }

    private GameEngine simulateFullGameFlowUntilLastRoundExclusive() {
        GameEngine gameEngine = simulateFullGameFlowUntilRiverRound();

        // Check (small blind player)
        Player currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        long chipsBeforeCall = currPlayer.getChips().get();
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CHECK).build());
        Assertions.assertFalse(gameEngine.getBoard().hasRiver(), "River was not supposed to be displayed yet");
        Assertions.assertEquals(chipsBeforeCall, currPlayer.getChips().get(), "No chips should be removed when CHECK.");
        Assertions.assertEquals(currPlayer.getName() + " checked.", gameEngine.getGameLog().getLastPlayerAction().toString());

        // Check (big blind player)
        currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        chipsBeforeCall = currPlayer.getChips().get();
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CHECK).build());
        Assertions.assertFalse(gameEngine.getBoard().hasRiver(), "River was not supposed to be displayed yet");
        Assertions.assertEquals(chipsBeforeCall, currPlayer.getChips().get(), "No chips should be removed when CHECK.");
        Assertions.assertEquals(currPlayer.getName() + " checked.", gameEngine.getGameLog().getLastPlayerAction().toString());

        // Check (dealer player)
        currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        chipsBeforeCall = currPlayer.getChips().get();
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CHECK).build());
        Assertions.assertTrue(gameEngine.getBoard().hasRiver(), "River supposed to be displayed");
        Assertions.assertEquals(chipsBeforeCall, currPlayer.getChips().get(), "No chips should be removed when CHECK.");
        Assertions.assertEquals(currPlayer.getName() + " checked.", gameEngine.getGameLog().getLastPlayerAction().toString());

        return gameEngine;
    }

    private GameEngine simulateFullGameFlowUntilLastRoundInclusive() {
        GameEngine gameEngine = simulateFullGameFlowUntilLastRoundExclusive();

        // Check (small blind player)
        Player currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        long chipsBeforeCall = currPlayer.getChips().get();
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.CHECK).build());
        Assertions.assertTrue(gameEngine.getBoard().hasRiver(), "River supposed to be displayed");
        Assertions.assertEquals(chipsBeforeCall, currPlayer.getChips().get(), "No chips should be removed when CHECK.");
        Assertions.assertEquals(currPlayer.getName() + " checked.", gameEngine.getGameLog().getLastPlayerAction().toString());

        // Fold (big blind player)
        currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        chipsBeforeCall = currPlayer.getChips().get();
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.FOLD).build());
        Assertions.assertTrue(gameEngine.getBoard().hasRiver(), "River supposed to be displayed");
        Assertions.assertEquals(chipsBeforeCall, currPlayer.getChips().get(), "No chips should be removed when FOLD.");
        Assertions.assertEquals(currPlayer.getName() + " folded.", gameEngine.getGameLog().getLastPlayerAction().toString());

        // Raise (dealer player)
        currPlayer = gameEngine.getPlayers().getCurrentPlayer();
        chipsBeforeCall = currPlayer.getChips().get();
        gameEngine.executePlayerAction(currPlayer, PlayerAction.builder().name(currPlayer.getName()).actionKind(PlayerActionKind.RAISE).chips(new Chips(20)).build());
        Assertions.assertTrue(gameEngine.getBoard().hasRiver(), "River supposed to be displayed");
        Assertions.assertEquals(chipsBeforeCall - 20, currPlayer.getChips().get(), "Supposed to pay 20 chips.");
        Assertions.assertEquals(currPlayer.getName() + " raised by 20.", gameEngine.getGameLog().getLastPlayerAction().toString());

        return gameEngine;
    }
}

