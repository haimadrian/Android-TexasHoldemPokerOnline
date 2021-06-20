package org.hit.android.haim.texasholdem.server.model.game;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.hit.android.haim.texasholdem.server.controller.common.Base64;
import org.hit.android.haim.texasholdem.server.model.bean.chat.Channel;
import org.hit.android.haim.texasholdem.server.model.bean.game.Board;
import org.hit.android.haim.texasholdem.server.model.bean.game.GameSettings;
import org.hit.android.haim.texasholdem.server.model.bean.game.Player;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class that represents a game between several players<br/>
 * A game has some hashcode that users can use in order to connect to the same game and play together.<br/>
 * Game class will manage the gameplay, and support chat mechanism to let the players communicate with each other.
 *
 * @author Haim Adrian
 * @since 08-May-21
 */
@Data
@ToString(exclude = {"chat", "deck"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GameEngine {
    private static final AtomicInteger gameCounter = new AtomicInteger(100); // Assume there can be 900 games running in parallel

    /**
     * A unique identifier of this game
     */
    @EqualsAndHashCode.Include
    private final int id;

    /**
     * Game preferences. See {@link GameSettings}
     */
    private final GameSettings gameSettings;

    /**
     * The {@link Players} playing in this game
     */
    private final Players players;

    /**
     * The player assigned the Dealer role.<br/>
     * This player is modified in every single round, clockwise.
     */
    private Player dealer;

    /**
     * The chat of current game
     */
    private final Channel chat;

    /**
     * Deck of cards we use in order to play.<br/>
     * Selecting cards for players and board.
     */
    private final Deck deck;

    /**
     * The board at which we set game cards: 3 X flop, 1 X Turn and 1 X River.
     */
    private final Board board;

    /**
     * Constructs a new {@link GameEngine}
     * @param gameSettings Preferences of a game.
     */
    public GameEngine(@NonNull GameSettings gameSettings) {
        this.gameSettings = gameSettings;
        id = gameCounter.getAndIncrement();
        players = new Players();
        chat = Channel.builder().name(getGameHash()).build();
        deck = new Deck();
        board = new Board();
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
        players.addPlayer(player);
        chat.getUsers().add(player);
    }

    /**
     * Add a player to this game.
     * @param player The player to add
     */
    public void removePlayer(Player player) {
        players.removePlayer(player);
        chat.getUsers().remove(player);
    }

    /**
     * Starts a game.<br/>
     * Shuffling the deck, generating random dealer, setting the current player as the player after the dealer.
     */
    public void start() {
        // Shuffle cards
        deck.shuffle();

        // Generate random dealer
        int dealerIndex = new Random().nextInt(players.size());
        dealer = players.getPlayer(dealerIndex);

        // Small bet is the first player who is playing, so we set it as the current player.
        players.setCurrentPlayerIndex(dealerIndex + 3);
    }

    public void stop() {

    }
}

