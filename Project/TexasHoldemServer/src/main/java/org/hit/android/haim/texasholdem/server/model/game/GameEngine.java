package org.hit.android.haim.texasholdem.server.model.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.hit.android.haim.texasholdem.server.controller.common.Base64;
import org.hit.android.haim.texasholdem.server.model.bean.chat.Channel;
import org.hit.android.haim.texasholdem.server.model.bean.game.Board;
import org.hit.android.haim.texasholdem.server.model.bean.game.GameSettings;
import org.hit.android.haim.texasholdem.server.model.bean.game.Player;
import org.hit.android.haim.texasholdem.server.model.bean.game.PlayerAction;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class that represents a game between several players<br/>
 * A game has some hashcode that users can use in order to connect to the same game and play together.<br/>
 * Game class will manage the gameplay, and support chat mechanism to let the players communicate with each other.
 *
 * @author Haim Adrian
 * @since 08-May-21
 */
@Data
@NoArgsConstructor
@ToString(exclude = {"chat", "deck", "gameLog", "listener", "playersLock"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Log4j2
public class GameEngine {
    private static final AtomicInteger gameCounter = new AtomicInteger(100); // Assume there can be 900 games running in parallel

    /**
     * Accept 7 players at most.
     */
    private static final int MAXIMUM_AMOUNT_OF_PLAYERS = 7;

    /**
     * A unique identifier of this game
     */
    @EqualsAndHashCode.Include
    private int id;

    /**
     * Game preferences.
     * @see GameSettings
     */
    private GameSettings gameSettings;

    /**
     * The {@link Players} playing in this game
     */
    private Players players;

    /**
     * The player assigned the Dealer role.<br/>
     * This player is modified in every single round, clockwise.
     */
    private Player dealer;

    /**
     * The chat of current game.
     * @see Channel
     */
    @JsonIgnore
    private Channel chat;

    /**
     * The log of current round in a game.
     * @see GameLog
     */
    private GameLog gameLog;

    /**
     * Deck of cards we use in order to play.<br/>
     * Selecting cards for players and board.
     * @see Deck
     */
    @JsonIgnore // Do not expose the deck, to avoid of revealing next cards
    private Deck deck;

    /**
     * The board at which we set game cards: 3 X flop, 1 X Turn and 1 X River.
     * @see Board
     */
    private Board board;

    /**
     * The pot of a running game.
     * @see Pot
     */
    private Pot pot;

    /**
     * Use a {@link PlayerTurnTimer} in network games to limit the time for each player turn
     * to {@link GameSettings#getTurnTime()} minute, such that the game continues.<br/>
     * In case of a timeout, the current player is forced to fold.
     */
    private PlayerTurnTimer playerTurnTimer;

    /**
     * Keep a reference to the last action kind, so we can make sure there are no illegal
     * actions. For example, a CHECK is not allowed after CALL or RAISE.<br/>
     * This reference is set to {@code null} when a bet round is started, to allow any action.
     */
    private PlayerAction.ActionKind lastActionKind;

    /**
     * A listener to get notified upon player updates, so we can persist changes in chips amount.
     */
    @NonNull
    @JsonIgnore
    private PlayerUpdateListener listener;

    /**
     * When a round is over, we keep a reference to each player's earnings so the client can
     * retrieve that and show indication.<br/>
     * After several seconds, a new round will be started automatically.<br/>
     * Note that as long as this variable differs from null, all player actions are ignored.
     */
    private Map<Player, Pot.PlayerWinning> playerToEarnings;

    /**
     * When this engine was created, time is in milliseconds since epoch.<br/>
     * We keep it to automatically cleanup inactive games. A game is consider inactive when it
     * is opened for 1 hour, and there is no player, or one player only.
     */
    private long timeCreated;

    /**
     * A state machine containing the current state of game engine. See {@link GameState}
     */
    private AtomicReference<GameState> gameState;

    /**
     * A lock to protect {@link #players}, such that we will not allow for more than 7 players to join
     */
    @JsonIgnore
    private final Lock playersLock = new ReentrantLock();

    /**
     * Constructs a new {@link GameEngine}
     * @param gameSettings Preferences of a game.
     * @param listener A listener to get notified upon player updates, so we can persist changes in chips amount.
     */
    public GameEngine(@NonNull GameSettings gameSettings, @NonNull PlayerUpdateListener listener) {
        this.gameSettings = gameSettings;
        this.listener = listener;
        timeCreated = System.currentTimeMillis();
        id = gameCounter.getAndIncrement();
        players = new Players();
        chat = Channel.builder().name(getGameHash()).build();
        gameLog = new GameLog();
        deck = new Deck();
        board = new Board();
        pot = new Pot();
        playerTurnTimer = gameSettings.isNetwork() ? new PlayerTurnTimer(this::onPlayerTurnTimeout, gameSettings.getTurnTime()) : null;
        gameState = new AtomicReference<>(GameState.READY);

        log.info("GameEngine created: " + this);
    }

    /**
     * Decode a game hash to game identifier.<br/>
     * This method created in order to get game identifier when we receive a game hash from clients.
     * @param gameHash A game hash to decode.
     * @return Game identifier
     * @throws IllegalArgumentException In case the specified hash is null or empty, or it does not represent a game identifier (int)
     */
    public static int gameIdFromGameHash(String gameHash) throws IllegalArgumentException {
        if ((gameHash == null) || (gameHash.isBlank())) {
            throw new IllegalArgumentException("Game hash cannot be null or empty.");
        }

        int gameId = Base64.decodeToInt(gameHash);
        if (gameId < 0) {
            throw new IllegalArgumentException(gameHash + " does not represent a game identifier.");
        }

        return gameId;
    }

    /**
     * @return A unique hash (~4 characters) representing this game
     */
    public String getGameHash() {
        return Base64.encodeToString(id);
    }

    /**
     * Add a player to this game.
     * @param player The player to add
     */
    public void addPlayer(Player player) {
        if (isActive()) {
            throw new IllegalArgumentException("Cannot join an active game. Wait for round to end.");
        } else {
            // Lock on players, to make sure we do not let more than 7 players to join a game.
            if (players.size() < MAXIMUM_AMOUNT_OF_PLAYERS) {
                playersLock.lock();
                try {
                    if (players.size() < MAXIMUM_AMOUNT_OF_PLAYERS) {
                        log.info(getId() + " - Adding player: " + player);
                        players.addPlayer(player);
                        chat.getUsers().add(player);
                    }
                } finally {
                    playersLock.unlock();
                }
            }
        }
    }

    /**
     * Add a player to this game.
     *
     * @param player The player to add
     */
    public void removePlayer(Player player) {
        log.info(getId() + " - Removing player: " + player);
        players.removePlayer(player);
        chat.getUsers().remove(player);
    }

    /**
     * Starts a game.<br/>
     * Shuffling the deck, generating random dealer, taking small bet from the player sits after
     * the dealer, and big bet from the player after the small one. Then, after the mandatory bets,
     * we {@link #moveTurnForward()} to the next player.
     */
    public void start() {
        if (gameState.compareAndSet(GameState.READY, GameState.STARTED)) {
            log.info(getId() + " - Starting new game.");

            // Generate random dealer
            int dealerIndex = new Random().nextInt(players.size());
            dealer = players.getPlayer(dealerIndex);

            // Start the round. (Set min player as the current player, and take mandatory bets)
            startRound();
        }
    }

    /**
     * Execute a player move. This can be check, call, raise or fold.<br/>
     * We ask for the acting player to ensure that it is his turn. If not, an IllegalArgumentException will be thrown.
     *
     * @param player The player who makes the move
     * @param action What move to make
     * @throws IllegalArgumentException In case the specified player is not the current player, or not playing, or action is illegal
     */
    public void executePlayerAction(Player player, PlayerAction action) throws IllegalArgumentException {
        log.info(getId() + " - Executing player action. [player=" + player + ", action=" + action + "]");
        validatePlayerAction(player, action);

        if (playerToEarnings != null) {
            log.info("Player action was ignored because there is currently player earnings available. So as long as it is available," +
                " all player actions are ignored. Clients expected to read game state and wait for next round. [player=" + player + ", action=" + action + "]");
            return;
        }

        Player currPlayer = players.getCurrentPlayer();
        switch (action.getActionKind()) {
            case CALL: {
                long chips = pot.bet(currPlayer, pot.getLastBet() == null ? action.getChips().get() : pot.getLastBet());
                action.setChips(chips);

                // Update listener about update of chips
                listener.onPlayerChipsUpdated(currPlayer, -1 * chips);
                break;
            }
            case RAISE: {
                long chips = pot.bet(currPlayer, action.getChips().get());
                action.setChips(chips);

                // Update listener about update of chips
                listener.onPlayerChipsUpdated(currPlayer, -1 * chips);
                break;
            }
            case FOLD: {
                currPlayer.setPlaying(false);
                break;
            }
            default:
                // Nothing special.
        }

        gameLog.logAction(action);
        lastActionKind = action.getActionKind();

        // Everytime it is the dealer's turn we can continue to next game stage, unless the dealer is raising
        if (currPlayer.equals(dealer) && (action.getActionKind() != PlayerAction.ActionKind.RAISE)) {
            // If we opened a new card, clear last bet to start a new bet round.
            // When the river card is already opened we do not want to clear last bet, so we will be
            // able to recognize that the game has finished.
            if (showNextCard()) {
                pot.clearLastBet();
            }
        }

        applyWinIfNeeded();
        if (playerToEarnings == null) {
            moveTurnForward();
        }
    }

    /**
     * Validates that a player can run the specified player action
     *
     * @param player A player to validate
     * @param action The action to validate
     * @throws IllegalArgumentException In case the specified player is not the current player, or not playing, or action is illegal
     */
    private void validatePlayerAction(Player player, PlayerAction action) {
        if (!player.equals(players.getCurrentPlayer())) {
            throw new IllegalArgumentException("Cannot execute player action when it is not the player's turn.");
        }

        if (!player.isPlaying()) {
            throw new IllegalArgumentException("Player is not playing.");
        }

        if (!action.getActionKind().canComeAfter(lastActionKind)) {
            throw new IllegalArgumentException(action.getActionKind().name() + " is not allowed after " + lastActionKind);
        }

        // In case of a RAISE with sum equals to the last bet, fix it to CALL.
        if ((action.getActionKind() == PlayerAction.ActionKind.RAISE) && (pot.getLastBet() != null) && (action.getChips().get() == pot.getLastBet())) {
            action.setActionKind(PlayerAction.ActionKind.CALL);
        }
    }

    /**
     * Use this method when it was the dealer turn and we are ready to open new card.
     *
     * @return Whether a new card was opened or not
     */
    private boolean showNextCard() {
        log.info(getId() + " - Showing next card.");
        boolean isNewCardShown = false;

        // If no flop, open the flop
        if (board.getFlop1().isEmpty()) {
            log.info(getId() + " - Showing flop.");
            isNewCardShown = true;
            deck.dropCard();
            board.addCard(deck.popCard());
            board.addCard(deck.popCard());
            board.addCard(deck.popCard());
        }
        // Else, if there is no turn yet, open turn
        else if (!board.hasTurn()) {
            log.info(getId() + " - Showing turn.");
            isNewCardShown = true;
            deck.dropCard();
            board.addCard(deck.popCard());
        }
        // Else, if there is no river yet, open river
        else if (!board.hasRiver()) {
            log.info(getId() + " - Showing river.");
            isNewCardShown = true;
            deck.dropCard();
            board.addCard(deck.popCard());
        }

        return isNewCardShown;
    }

    /**
     * Check if it is the very end round (after river) or if there is one active player only, to
     * end a game and apply wins.
     */
    private void applyWinIfNeeded() {
        // When there is a single active player, or we arrived to the dealer after River is shown, end the round.
        Set<Player> involvedPlayers = players.getInvolvedPlayers();
        if ((involvedPlayers.stream().filter(player -> player.getChips().get() > 0).count() == 1) ||
            (board.hasRiver() &&
                players.getCurrentPlayer().equals(dealer) &&
                (lastActionKind != PlayerAction.ActionKind.RAISE) &&
                (pot.getLastBet() != null))) {
            // Sign that we are ready to restart a round, letting new players to join now
            gameState.set(GameState.RESTART);

            log.info(getId() + " - Apply winning.");
            playerToEarnings = pot.applyWinning(involvedPlayers, board);
            log.info(getId() + " - The winners: " + playerToEarnings);

            // Log winners
            //@formatter:off
            playerToEarnings.forEach((key, value) -> gameLog.logAction(PlayerAction.builder()
                                                                                   .name(key.getName())
                                                                                   .chips(new Chips(value.getSum()))
                                                                                   .handRank(value.getHandRank())
                                                                                   .build()));
            //@formatter:on

            // Wait for 10 seconds in background before starting a new round.
            // We wait so clients can draw winning indications
            ExecutorService service = Executors.newSingleThreadExecutor(new CustomizableThreadFactory("RoundLauncher"));
            service.submit(() -> {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                } catch (InterruptedException ignore) {
                }

                // Move the dealer forward
                dealer = players.getPlayer(players.indexOfPlayer(dealer) + 1);
                startRound();
                service.shutdown();
            });
        }
    }

    /**
     * This method is used after we have a {@link #dealer} player defined. We will take mandatory bets
     * (small and big) from the players sitting after the dealer, and {@link #moveTurnForward()} to the next
     * player (after big) that we are waiting for.<br/>
     * Use this method when a game is first {@link #start() started} or when a round is over, and the dealer has been
     * updated.
     */
    private void startRound() {
        gameState.set(GameState.STARTED);

        log.info(getId() + " - Starting new round");
        playerToEarnings = null;

        // Make sure all players are marked as currently playing, as we are starting a new round.
        players.markAllPlayersAsPlaying();

        // Reset game log
        gameLog.clear();

        // Shuffle cards
        deck.shuffle();

        // Deal cards to players
        dealCards();

        // Current must to bet player is the one after the dealer. This player has to add small bet.
        pot.clearLastBet();
        players.setCurrentPlayerIndex(players.indexOfPlayer(dealer) + 1);
        pot.bet(players.getCurrentPlayer(), gameSettings.getSmallBet());

        // Move to next player and take big bet from it.
        pot.bet(players.nextPlayer(), gameSettings.getBigBet());

        // Now the game is officially started and we are waiting for the next player to play.
        moveTurnForward();
    }

    /**
     * Call this method when starting a new round to deal cards to players.<br/>
     * The first player to receive a card is the one after the dealer.<br/>
     * We deal cards in an order like it was in the real world, where each player receive a card,
     * in a circle, until all players own 2 cards.
     */
    private void dealCards() {
        players.getPlayers().forEach(p -> p.getHand().clear());

        int currPlayerIndex = players.indexOfPlayer(dealer) + 1;
        for (int i = 0; i < players.size() * 2; i++) {
            // It might be that some player exited while we are dealing cards, in this
            // case we might deal too many cards. So protect this case and break the loop.
            Player currPlayer = players.getPlayer(currPlayerIndex++);

            if (currPlayer.getHand().size() < 2) {
                currPlayer.getHand().addCard(deck.popCard());
            }
        }
    }

    /**
     * A method used to move the turn from current player to the next playing one, and init the time started
     * of this player, to count the time it takes for this player to start.
     */
    private void moveTurnForward() {
        Player newPlayer = players.nextPlayer();
        log.info(getId() + " - Moving turn to next player: " + newPlayer);

        if (gameSettings.isNetwork()) {
            playerTurnTimer.startOrReset();
        }
    }

    /**
     * This event is raised in case of network game, and it tells that the current player has
     * ran out of time. In this case, we force the current player to fold and move forward to next player.
     */
    private void onPlayerTurnTimeout() {
        log.info(getId() + " - Player turn timeout occurred. [player=" + players.getCurrentPlayer() + "]");
        executePlayerAction(players.getCurrentPlayer(),
            PlayerAction.builder().name(players.getCurrentPlayer().getName()).actionKind(PlayerAction.ActionKind.FOLD).build());
    }

    /**
     * Stop this game engine.<br/>
     * We will stop and if there are bets, we return them back to the players.
     */
    public void stop() {
        // Do this in case game is not already stopped
        if (gameState.compareAndSet(GameState.READY, GameState.STOPPED) ||
            gameState.compareAndSet(GameState.STARTED, GameState.STOPPED) ||
            gameState.compareAndSet(GameState.RESTART, GameState.STOPPED)) {
            log.info(getId() + " - Stopping game.");

            gameState.set(GameState.STOPPED);
            playerTurnTimer.stop();
            gameLog.clear();
            players.clear();
            chat.clear();
            chat.getUsers().clear();
            board.clear();
            dealer = null;
            lastActionKind = null;

            // In case there are pots, return the chips back to their owners.
            Map<Player, Long> pots = pot.getPots();
            if (!pots.isEmpty()) {
                for (Map.Entry<Player, Long> entry : pots.entrySet()) {
                    entry.getKey().getChips().add(entry.getValue());

                    // Update listener about update of chips
                    listener.onPlayerChipsUpdated(entry.getKey(), entry.getValue());
                }
            }
            pot.clear();
        }
    }

    /**
     * Tests whether this game is currently active. A game does not accept new players when it is active.
     * @return Whether current game engine is active (during a round) or not.
     */
    public boolean isActive() {
        return gameState.get() == GameState.STARTED;
    }

    /**
     * Use this as a listener to player updates.<br/>
     * We expose this functionality to let an outside class to save players to disk upon updates,
     * to implement persistence.
     */
    @FunctionalInterface
    public interface PlayerUpdateListener {
        /**
         * This event is raised whenever player's chips are updated during a game
         * @param player The player with the up to date chips
         * @param chips The chips value that was modified. Can be negative when player loses chips
         */
        void onPlayerChipsUpdated(Player player, long chips);
    }

    /**
     * An enum representing current game engine's state:
     * <ul>
     *     <li>{@link #READY}</li>
     *     <li>{@link #RESTART}</li>
     *     <li>{@link #STARTED}</li>
     *     <li>{@link #STOPPED}</li>
     * </ul>
     */
    private enum GameState {
        /**
         * Game is ready to be started. This state is set when a game engine is created, and before it is started at {@link GameEngine#start()}.
         */
        READY,

        /**
         * When a round is over and we have applied winnings, the game is in RESTART state, allowing new players to join.
         */
        RESTART,

        /**
         * Game is started and is about to move to one of the other states.
         */
        STARTED,

        /**
         * Game was stopped by calling {@link GameEngine#stop()}
         */
        STOPPED
    }
}

