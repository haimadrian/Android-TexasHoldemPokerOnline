package org.hit.android.haim.texasholdem.common.model.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hit.android.haim.texasholdem.common.model.bean.game.Board;
import org.hit.android.haim.texasholdem.common.model.bean.game.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class responsible for managing in-game players access.<br/>
 * Players are sorted by insertion order, and backed-by a set to make sure a player
 * is not added twice.<br/>
 * In addition we maintain current playing player, so it is possible to get next player. (Turns)
 * @author Haim Adrian
 * @since 11-Jun-21
 */
@ToString(exclude = {"players"})
public class Players {
    /**
     * All of the players in a game, to back-by the ordered list of players,
     * and make sure there can't be a situation where player is added twice.<br/>
     * Implementation that supports add/remove/random access in O(1)
     */
    @JsonIgnore
    private final Map<Player, Integer> players;

    /**
     * List of players, so we can iterate on, one by another, in the order they were added,
     * thus saving the same order as players are sitting around a table.
     */
    private final List<Player> playersList;

    /**
     * Keep a map of the identifiers of all players in a game.<br/>
     * We keep that in a set to win the ability to check if a user is part of a game in O(1), by user identifier
     */
    @JsonIgnore
    private final Map<String, Player> playersById;

    /**
     * Index of the player we are waiting for, to finish its turn. (Current player)<br/>
     * This index helps us to know who is the next player, at {@link #playersList}.
     */
    @Getter
    private int currentPlayerIndex;

    @Getter
    @Setter
    private int maxAmountOfPlayers;

    /**
     * Constructs a new {@link Players}
     */
    public Players() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Constructs a new {@link Players}
     * @param maxAmountOfPlayers How many players can be added
     */
    public Players(int maxAmountOfPlayers) {
        this.maxAmountOfPlayers = maxAmountOfPlayers;
        players = new HashMap<>();
        playersList = new ArrayList<>();
        playersById = new HashMap<>();
    }

    /**
     * Add a player to this game.<br/>
     * We use {@link Player#getPosition()} as the player index in the list of players. Unless that seat
     * is already taken, so in such a case we will find an available seat for this player, and update its
     * position accordingly.
     * @param player The player to add
     * @throws IllegalArgumentException In case player is already part of the game
     */
    public void addPlayer(Player player) throws IllegalArgumentException {
        if (players.containsKey(player)) {
            throw new IllegalArgumentException("Player " + player + " is already part of the game");
        } else if (playersList.size() == maxAmountOfPlayers) {
            throw new IllegalArgumentException("Full. There are already " + maxAmountOfPlayers + " players");
        } else {
            int playerIndex = player.getPosition();
            while (playersList.get(playerIndex) != null) {
                playerIndex = (playerIndex + 1) % maxAmountOfPlayers;
            }

            // Put the player and map it to its index (position)
            player.setPosition(playerIndex);
            players.put(player, playerIndex);
            playersList.add(playerIndex, player);
            playersById.put(player.getId(), player);
        }
    }

    /**
     * Get a player by its index. Index must be at [0, size()-1]
     * @param playerIndex The index of the player to get
     * @return The player at the specified index
     * @throws IndexOutOfBoundsException in case the specified index was out of bounds. [0, size()-1]
     */
    public Player getPlayer(int playerIndex) throws IndexOutOfBoundsException {
        if ((playerIndex < 0) || (playerIndex >= playersList.size())) {
            throw new IndexOutOfBoundsException("There is no player at: " + playerIndex + ". Try: [0, " + playersList.size() + ")");
        }

        return playersList.get(playerIndex);
    }

    /**
     * Add a player to this game.
     * @param player The player to add
     */
    public void removePlayer(Player player) {
        if (players.containsKey(player)) {
            players.remove(player);
            playersList.remove(player);
            playersById.remove(player.getId());
        }
    }

    /**
     * @param player A player to get its index around the table
     * @return The index of a specified player, or {@code -1} in case player does not exist
     */
    public int indexOfPlayer(Player player) {
        return players.getOrDefault(player, -1);
    }

    /**
     * Retrieve a player by user identifier
     * @param playerId The user identifier
     * @return A player or {@code null} in case this user is not one of the players
     */
    public Player getPlayerById(String playerId) {
        return playersById.get(playerId);
    }

    /**
     * Sets the index of current player. Note that we use modulus to make sure index is not out of bounds.
     * @param playerIndex The index to set as current player
     */
    public void setCurrentPlayerIndex(int playerIndex) {
        this.currentPlayerIndex = playerIndex % playersList.size();
    }

    /**
     * Use this method to get a reference to the current playing player.<br/>
     * We depend on {@link #getCurrentPlayerIndex()} to know which player is the active one.
     * @return A reference to the current player.
     */
    public Player getCurrentPlayer() {
        return getPlayer(getCurrentPlayerIndex());
    }

    /**
     * @return How many players there are
     */
    public int size() {
        return playersList.size();
    }

    /**
     * @return A new set containing the players in this holder
     */
    public Set<Player> getPlayers() {
        return new HashSet<>(players.keySet());
    }

    /**
     * Remove all players from this reference
     */
    public void clear() {
        players.clear();
        playersList.clear();
        playersById.clear();
    }

    /**
     * Move the turn to the next player in the list, and return this player.<br/>
     * Note that the player must be active, which means he is part of the game. (Not folded / went all-in)
     * @return The new player
     */
    public Player nextPlayer() {
        int lastPlayer = currentPlayerIndex;

        // Continue looking for next available player, and protect the loop such that we avoid of
        // going in circle. In case we have reached the player we started from, we break.
        // Note that a player might be playing but he went all-int, so we skip such player.
        Player player;
        do {
            currentPlayerIndex = ((currentPlayerIndex + 1) % playersList.size());
            player = playersList.get(currentPlayerIndex);
        } while ((!player.isPlaying() || player.getChips().get() == 0) && (currentPlayerIndex != lastPlayer));

        return player;
    }

    /**
     * Use this method at the end of a round, to collect all players that are in. ({@link Player#isPlaying()}
     * @return A set of involved players, to send to {@link Pot#applyWinning(Set, Board)}
     */
    public Set<Player> getInvolvedPlayers() {
        return playersList.stream().filter(Player::isPlaying).collect(Collectors.toSet());
    }

    /**
     * Mark all players as currently playing.<br/>
     * Use this method whenever a round is started, to mark all of the players as currently playing.
     */
    public void markAllPlayersAsPlaying() {
        playersList.forEach(player -> player.setPlaying(true));
    }
}

