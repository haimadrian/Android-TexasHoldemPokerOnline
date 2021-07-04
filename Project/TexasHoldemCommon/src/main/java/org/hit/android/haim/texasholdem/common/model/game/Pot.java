package org.hit.android.haim.texasholdem.common.model.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hit.android.haim.texasholdem.common.model.bean.game.Board;
import org.hit.android.haim.texasholdem.common.model.bean.game.Player;
import org.hit.android.haim.texasholdem.common.model.game.rank.HandRankCalculator;
import org.hit.android.haim.texasholdem.common.model.game.rank.HandRankCalculatorResult;
import org.hit.android.haim.texasholdem.common.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

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
    @JsonSerialize(keyUsing = Player.PlayerKeySerializer.class)
    @JsonDeserialize(keyUsing = Player.PlayerKeyDeserializer.class)
    @JsonProperty
    private final Map<Player, HandPot> pots = new HashMap<>();

    /**
     * Keep pots of players for a round of bets, so we can know the total bet of a player per round, and also
     * get the ability to equal a bet when player that already paid is calling.
     */
    @JsonSerialize(keyUsing = Player.PlayerKeySerializer.class)
    @JsonDeserialize(keyUsing = Player.PlayerKeyDeserializer.class)
    @JsonProperty
    private final Map<Player, HandPot> potsForRound = new HashMap<>();

    /**
     * A reference to the last bet, to make sure a new bet is legal and not below it.<br/>
     * It might be null when we just start a round, in this case the bet will be set according to the small bet player.
     */
    @Getter
    private Long lastBet;

    /**
     * This method is exposed so we can use it from {@link GameEngine#stop()}, to make sure we do not stop
     * a game and lose chips. When there are pots and a game is stopped, the chips are returned to the players.
     * @return A copy of all player pots
     */
    @JsonIgnore
    public Map<Player, Long> getPlayerPots() {
        return pots.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getSum()));
    }

    /**
     * Use this method bet / call.<br/>
     * The method will return how many chips were actually been taken. It might be that a user used
     * his all-in, which is less than the specified amount.
     * @param player The player who is betting
     * @param amount The bet amount
     * @return The actual amount of chips that we used
     * @throws IllegalArgumentException In case {@code amount} is smaller than last bet, and player has enough chips to equal he amount to last bet
     */
    public long bet(Player player, long amount) {
        // If amount is bigger than the ALL-IN of the specified player, use the player's ALL-IN.
        long validatedAmount = Math.min(amount, player.getChips().get()) + getPotOfPlayer(player);

        if (lastBet != null) {
            if ((validatedAmount < lastBet) && (player.getChips().get() > validatedAmount)) {
                throw new IllegalArgumentException("Cannot bet with amount smaller than last bet. [amount=" + validatedAmount + ", lastBet=" + lastBet + "]");
            }
        }

        lastBet = amount + getPotOfPlayer(player);
        long delta = validatedAmount - getPotOfPlayer(player);
        player.getChips().remove(delta);

        pots.computeIfAbsent(player, p -> new HandPot()).add(delta);
        potsForRound.computeIfAbsent(player, p -> new HandPot()).add(delta);

        return delta;
    }

    /**
     * Get existing pot for player, to know how many chips a player have already paid.
     * @param player The player to get its pot
     * @return How many chips the specified player put. Can be 0.
     */
    public long getPotOfPlayer(Player player) {
        if (!potsForRound.containsKey(player)) {
            return 0;
        }

        return potsForRound.get(player).sum;
    }

    /**
     * Clear stored last bet to support resetting a bet round and start betting again.
     */
    public void clearLastBet() {
        lastBet = null;
    }

    /**
     * Call this method when round of bets is over, so we will clear existing players pots.
     */
    public void clearPotsOfRound() {
        potsForRound.clear();
    }

    /**
     * Clear all saved pots
     */
    public void clear() {
        pots.clear();
        clearPotsOfRound();
        clearLastBet();
    }

    /**
     * Use this method when a round is over, and we need to share the pot among winning players.<br/>
     * Note that not all of the money will get to the best hand, as it might be that the best hand belongs to a player
     * that went all-in, and there is some dead money in the pot, of players that bet after the winner who went all-in.
     * In this case, the "dead" money will be split between the other involved winners.
     * @param involvedPlayers See {@link Players#getInvolvedPlayers()}
     * @param board The {@link Board}, to find winning hands.
     * @return A map between a winner and {@link PlayerWinning} reference holding the amount of chips and hand rank.
     */
    public Map<String, PlayerWinning> applyWinning(Set<Player> involvedPlayers, Board board) {
        Map<Player, PlayerWinning> result = new HashMap<>();

        // Use a tree map so we will sort the map based on keys (ranks)
        // We use a reverse order comparator, so the best rank will be first.
        Map<HandRankCalculatorResult, Set<Player>> rankToPlayers = new TreeMap<>(Comparator.reverseOrder());
        for (Player player : involvedPlayers) {
            HandRankCalculatorResult rank = HandRankCalculator.calculate(board, player.getHand());
            rankToPlayers.computeIfAbsent(rank, r -> new HashSet<>()).add(player);
        }

        for (Map.Entry<HandRankCalculatorResult, Set<Player>> currRankToPlayers : rankToPlayers.entrySet()) {
            Set<Player> winners = currRankToPlayers.getValue();

            // Keep spreading pots until we handle all winning players.
            while (!winners.isEmpty()) {
                // Get the minimum sum based on winners, to take this part out from pots, and share among winners.
                long minSum = findMinSumBasedOnWinnersBet(winners);
                Pair<Long, Long> winShareAndRemainder = calculateWinAndRemainder(winners.size(), minSum);
                Player firstWinner = winners.iterator().next();

                // Now iterate over all winners to share the pots among them.
                sharePotsAmongWinners(result, winners, winShareAndRemainder.getFirst(), currRankToPlayers.getKey());

                // Now add the remainder to the first player
                if (firstWinner != null) {
                    firstWinner.getChips().add(winShareAndRemainder.getSecond());
                    result.get(firstWinner).sum += winShareAndRemainder.getSecond();
                }
            }

            // If all pots have been drained, there is no reason to continue.
            // We continue iterating when there is "dead money".
            if (pots.isEmpty()) {
                break;
            }
        }

        // Clear the pots for next round
        pots.clear();

        return result.entrySet().stream().collect(Collectors.toMap(ent -> ent.getKey().getId(), Map.Entry::getValue));
    }

    /**
     * A method to give for each winner the amount of chips he deserves.<br/>
     * The amount of winning chips is calculated at {@link #calculateWinAndRemainder(int, long)}, and we
     * just iterate over all winners and assign for each winner the amount of chips he deserves.<br/>
     * The method will remove a winner from {@code winners}, in case it was fully handled (meaning its pot
     * has drained and the player has already received all the chips he deserves)
     * @param winnerToWinSum How many chips each player earned.
     * @param winners Set of winners to go over and share the pots among them.
     * @param winningSum To know how many chips each winner deserves
     * @param handRank The hand rank of a winner, to save it to the value we put in {@code winnerToWinSum}
     */
    private void sharePotsAmongWinners(Map<Player, PlayerWinning> winnerToWinSum, Set<Player> winners, long winningSum, HandRankCalculatorResult handRank) {
        for (Iterator<Player> winnerIter = winners.iterator(); winnerIter.hasNext();) {
            Player winner = winnerIter.next();

            // If current player's pot has been drained, it means we've finished handling this player, hence
            // we remove him.
            if (pots.get(winner) == null) {
                winnerIter.remove();
            }

            // Add the chips to the winner
            winner.getChips().add(winningSum);
            if (!winnerToWinSum.containsKey(winner)) {
                winnerToWinSum.put(winner, new PlayerWinning(0L, handRank));
            }

            // Add the winning amount to the total amount of current winner.
            // It might be that there will be several iterations, hence we aggregate.
            winnerToWinSum.get(winner).sum += winningSum;
        }
    }

    /**
     * This method will go over the pots of the winners, to find the minimum sum, such
     * that we can make a pot out of that sum to share among the winners.<br/>
     * This is how we handle "dead money". (The "dead money" will stay in {@link #pots})
     * @param winners Set of players to find minimum sum of
     * @return The minimum sum
     */
    private long findMinSumBasedOnWinnersBet(Set<Player> winners) {
        long minSum = Long.MAX_VALUE;
        for (Player currWinner : winners) {
            long sum = pots.get(currWinner).getSum();
            if (sum < minSum) {
                minSum = sum;
            }
        }

        return minSum;
    }

    /**
     * This method calculates how many chips each winner earns, and the remainder, out of all pots.<br/>
     * We take the amount of the specified {@code sumToTakeFromPots} out of all {@link #pots}, and then calculate
     * the winning sum (totalSum / winners.size()), and the remainder.
     * @param amountOfWinners Amount of winners to calculate how many chips each winner earns
     * @param sumToTakeFromPots The amount of chips to take from each pot
     * @return Pair(win, remainder)
     */
    private Pair<Long, Long> calculateWinAndRemainder(int amountOfWinners, long sumToTakeFromPots) {
        long allPots = 0;
        for (Iterator<Map.Entry<Player, HandPot>> potsIterator = pots.entrySet().iterator(); potsIterator.hasNext();) {
            Map.Entry<Player, HandPot> currPot = potsIterator.next();
            allPots += currPot.getValue().takeSum(sumToTakeFromPots);

            // If current pot has drained, remove it.
            // We remove drained pots so we will know when to stop the main applyWinnings loop.
            if (currPot.getValue().getSum() == 0) {
                potsIterator.remove();
            }
        }

        long win = allPots / amountOfWinners;
        long remainder = allPots % amountOfWinners;
        return Pair.of(win, remainder);
    }

    @JsonIgnore
    public long sum() {
        if (pots.isEmpty()) {
            return 0;
        }

        return pots.values().stream().mapToLong(HandPot::getSum).sum();
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

        /**
         * Remove an amount out of sum and return it.<br/>
         * This method is used when we take chips for a winning player, and we allow
         * defining an upper bound to make sure we do not give a player more than he bet.
         * @param upperBound The bound to set
         * @return the sum, considering upper bound.
         */
        public long takeSum(long upperBound) {
            long result = Math.min(sum, upperBound);
            if (upperBound >= sum) {
                sum = 0;
            } else {
                sum -= upperBound;
            }

            return result;
        }
    }

    /**
     * A class to hold how many chips a player earns, and what his hand rank is.<br/>
     * The hand rank is needed so we will be able to show what hand a winner had.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerWinning {
        /**
         * How many chips a player earns
         */
        @Getter
        private long sum = 0;

        /**
         * The hand rank of a player, containing the selected cards, so client can show the hand rank of a winner
         */
        @Getter
        private HandRankCalculatorResult handRank;
    }
}

