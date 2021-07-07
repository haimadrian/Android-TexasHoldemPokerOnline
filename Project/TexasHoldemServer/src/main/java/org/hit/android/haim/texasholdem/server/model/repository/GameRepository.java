package org.hit.android.haim.texasholdem.server.model.repository;

import org.hit.android.haim.texasholdem.common.model.bean.game.GameSettings;
import org.hit.android.haim.texasholdem.common.model.bean.game.Player;
import org.hit.android.haim.texasholdem.common.model.game.GameEngine;
import org.hit.android.haim.texasholdem.server.model.game.ServerGameEngine;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A repository holding all active games.<br/>
 * Do not use this class directly. Instead, use {@link org.hit.android.haim.texasholdem.server.model.service.GameService}
 *
 * @author Haim Adrian
 * @since 08-May-21
 */
public class GameRepository {
    private final Map<Integer, GameEngine> games = new ConcurrentHashMap<>();

    /**
     * A map between user identifier to a game he created.<br/>
     * We hold this map to retrieve game hashes by game creator in O(1)
     */
    private final Map<String, GameEngine> ownerToGame = new ConcurrentHashMap<>();

    /**
     * A map between user identifier to a game he is part of.<br/>
     * We hold this map to retrieve games by players in O(1)
     */
    private final Map<String, GameEngine> playerToGame = new ConcurrentHashMap<>();

    private GameRepository() {

    }

    public static GameRepository getInstance() {
        return SingletonRef.instance;
    }

    /**
     * Create and get a new {@link GameEngine}
     * @param settings Settings of a game
     * @param listener A listener to get notified upon player updates, so we can persist changes in chips amount.
     * @return the newly created game
     */
    public GameEngine createNewGame(GameSettings settings, GameEngine.PlayerUpdateListener listener) {
        if (settings.getSmallBet() <= 0) {
            settings.setSmallBet(1);
        }

        if (settings.getBigBet() <= 1) {
            settings.setBigBet(2);
        }

        if (settings.getTurnTime() <= TimeUnit.SECONDS.toMillis(3)) {
            settings.setTurnTime(TimeUnit.MINUTES.toMillis(1));
        }

        // If there is another game a user created, close it. A user cannot create several games simultaneously.
        GameEngine existingGame = ownerToGame.get(settings.getCreatorId());
        if (existingGame != null) {
            stopGame(existingGame.getId());
        }

        GameEngine game = new ServerGameEngine(settings, listener);
        games.put(game.getId(), game);
        ownerToGame.put(settings.getCreatorId(), game);
        return game;
    }

    /**
     * Get a game by its identifier
     * @param gameId The identifier of a game
     * @return An optional reference to the game. (Empty when there is no game with the given identifier)
     */
    public Optional<GameEngine> findGameById(int gameId) {
        if (!games.containsKey(gameId)) {
            return Optional.empty();
        }

        return Optional.of(games.get(gameId));
    }

    /**
     * Get a game by the identifier of the user created that game
     * @param creatorId The identifier of a user to get the game he created
     * @return An optional reference to the game. (Empty when there is no game with the given identifier)
     */
    public Optional<GameEngine> findGameByCreator(String creatorId) {
        if (!ownerToGame.containsKey(creatorId)) {
            return Optional.empty();
        }

        return Optional.of(ownerToGame.get(creatorId));
    }

    /**
     * Get the game of a player, if there is such
     * @param playerId The identifier of a user to get the game he is part of
     * @return An optional reference to the game. (Empty when there is no game with the given identifier)
     */
    public Optional<GameEngine> findGameByPlayer(String playerId) {
        if (!playerToGame.containsKey(playerId)) {
            return Optional.empty();
        }

        return Optional.of(playerToGame.get(playerId));
    }

    /**
     * @return All game engines
     */
    public Iterable<GameEngine> findAll() {
        // Return a new arraylist to let outside world to iterate over game engines and modify game repository,
        // without failing on modification exception
        return new ArrayList<>(games.values());
    }

    /**
     * Start running a game.
     * @param gameId The identifier of a game
     */
    public void startGame(int gameId) {
        GameEngine existingGame = games.get(gameId);
        if (existingGame != null) {
            existingGame.start();
        }
    }

    /**
     * End a running game.<br/>
     * After closing a game it is deleted, hence you won't be able to find this game.
     * @param gameId The identifier of a game
     */
    public void stopGame(int gameId) {
        GameEngine existingGame = games.remove(gameId);
        if (existingGame != null) {
            ownerToGame.remove(existingGame.getGameSettings().getCreatorId());
            existingGame.stop();
        }
    }

    /**
     * Add player to a game
     * @param gameId The identifier of a game
     * @param player The player that joins
     */
    public void joinGame(int gameId, Player player) {
        GameEngine existingGame = games.get(gameId);
        if (existingGame != null) {
            // Check if user joins another game while he is part of a game already
            GameEngine gameEngine = playerToGame.get(player.getId());
            if ((gameEngine != null) && (gameId != gameEngine.getId())) {
                leaveGame(gameEngine.getId(), player);
            }

            existingGame.addPlayer(player);
            playerToGame.put(player.getId(), existingGame);
        }
    }

    /**
     * Remove player from a game
     * @param gameId The identifier of a game
     * @param player The player that leaves
     */
    public void leaveGame(int gameId, Player player) {
        GameEngine existingGame = games.get(gameId);
        if (existingGame != null) {
            if (existingGame.getPlayers().getPlayerById(player.getId()) != null) {
                playerToGame.remove(player.getId());
                existingGame.removePlayer(player);

                // Check if it was the owner, to remove it from owners map.
                GameEngine gameEngine = ownerToGame.get(player.getId());
                if ((gameEngine != null) && (gameEngine.getId() == gameId)) {
                    ownerToGame.remove(player.getId());
                }

                // If no players left, discard that game.
                if (existingGame.getPlayers().size() == 0) {
                    stopGame(gameId);
                }
            }
        }
    }

    private static class SingletonRef {
        static final GameRepository instance = new GameRepository();
    }
}

