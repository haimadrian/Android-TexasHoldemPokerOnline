package org.hit.android.haim.texasholdem.server.model;

import lombok.Data;
import lombok.ToString;
import org.hit.android.haim.texasholdem.server.controller.common.Base64;
import org.hit.android.haim.texasholdem.server.model.bean.chat.Channel;
import org.hit.android.haim.texasholdem.server.model.bean.game.Board;
import org.hit.android.haim.texasholdem.server.model.bean.game.Deck;
import org.hit.android.haim.texasholdem.server.model.bean.game.Player;

import java.util.HashSet;
import java.util.Set;
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
@ToString
public class GameEngine {
    private static final AtomicInteger gameCounter = new AtomicInteger(100); // Assume there can be 900 games running in parallel

    /**
     * A unique identifier of this game
     */
    private final int id;

    /**
     * All of the players in a game
     */
    private final Set<Player> players;

    /**
     * The player chosen as dealer
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
     */
    public GameEngine() {
        id = gameCounter.getAndIncrement();
        players = new HashSet<>();
        chat = Channel.builder().name(Base64.encodeToString(id)).users(players).build();
        deck = new Deck();
        board = new Board();
    }

    public void close() {

    }

    /**
     * Add a player to this game.
     * @param player The player to add
     */
    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * Add a player to this game.
     * @param player The player to add
     */
    public void removePlayer(Player player) {
        players.remove(player);
    }
}

