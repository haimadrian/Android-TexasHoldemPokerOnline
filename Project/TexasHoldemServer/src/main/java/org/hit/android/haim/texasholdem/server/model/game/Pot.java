package org.hit.android.haim.texasholdem.server.model.game;

import lombok.Data;
import lombok.Getter;
import org.hit.android.haim.texasholdem.server.model.bean.game.Board;
import org.hit.android.haim.texasholdem.server.model.bean.game.Player;
import org.springframework.data.util.Pair;

import java.util.*;

/**
 * This class responsible for bets.<br/>
 * We count all of the bets and keep them in this class, also maintaining side pots when there is a player that
 * went all-in, but other players want to keep raising more than the all-in of another player.<br/>
 * In this scenario, there will be a pot with the all-in of that player, and any other raise, more than the all-in,
 * will be managed in a side pot.<br/>
 * As part of it, we must be aware of the players in this class.
 * @author Haim Adrian
 * @since 12-Jun-21
 */
public class Pot {
    /**
     * Map between pot to a set of contributing players.<br/>
     * This allows us to create side pots when there is a player that went all-int, and other players continue raising.
     */
    private final Map<Player, HandPot> pots = new HashMap<>();

    /**
     * A reference to the last bet, to make sure a new bet is legal and not below it.<br/>
     * It might be null when we just start a round, in this case the bet will be set according to the small bet player.
     */
    @Getter
    private Long lastBet;

    /**
     * Use this method bet / call
     * @param player The player who is betting
     * @param amount The bet amount
     */
    public void bet(Player player, long amount) {
        pots.computeIfAbsent(player, p -> new HandPot()).add(amount);
    }

    /**
     * Use this method when a round is over, and we need to share the pot among winning players.<br/>
     * Note that not all of the money will get to the best hand, as it might be that the best hand belongs to a player
     * that went all-in, and there is some dead money in the pot, of players that bet after the winner who went all-in.
     * In this case, the "dead" money will be split between the other involved winners.
     * @param involvedPlayers See {@link Players#getInvolvedPlayers()}
     * @param board The {@link Board}, to find winning hands.
     * @return A list of pairs, where each pair represents a pot (amount) and the players that the pot has been split between them.
     */
    public List<Pair<Set<Player>, Long>> applyWinning(Set<Player> involvedPlayers, Board board) {
        List<Pair<Set<Player>, Long>> result = new ArrayList<>();

        Map<Player, HandRankCalculator.HandNumericRank> playerToRank = new HashMap<>(involvedPlayers.size());
        for (Player player : involvedPlayers) {
            playerToRank.put(player, HandRankCalculator.calculate(board, player.getHand()).getRank());
        }

        // Clear the pots for next round
        pots.clear();

        return result;
    }

    /**
     * Reference to a single pot.<br/>
     * Usually there is one pot, but there might be more when players are out of chips.
     */
    @Data
    private static class HandPot {
        private long sum = 0;

        public void add(long amount) {
            sum += amount;
        }
    }
}

