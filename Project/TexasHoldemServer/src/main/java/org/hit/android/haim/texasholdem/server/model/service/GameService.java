package org.hit.android.haim.texasholdem.server.model.service;

import org.hit.android.haim.texasholdem.common.model.bean.game.GameSettings;
import org.hit.android.haim.texasholdem.common.model.bean.game.Player;
import org.hit.android.haim.texasholdem.common.model.game.GameEngine;
import org.hit.android.haim.texasholdem.common.util.CustomThreadFactory;
import org.hit.android.haim.texasholdem.server.controller.common.Base64;
import org.hit.android.haim.texasholdem.server.model.bean.user.User;
import org.hit.android.haim.texasholdem.server.model.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Access to games using this class
 *
 * @author Haim Adrian
 * @since 21-Mar-21
 */
@Service
public class GameService {
    private final GameRepository gameRepository = GameRepository.getInstance();

    @Autowired
    private UserService userService;

    /**
     * We keep a single thread pool scheduler that runs every 15 minutes and takes care of
     * clearing up inactive games.<br/>
     * A game is considered inactive when it is opened for more than a hour, and there are no players or 1 player only.
     */
    private final ScheduledExecutorService cleanupExecutor;

    /**
     * Constructs a new {@link GameService}
     */
    public GameService() {
        cleanupExecutor = Executors.newSingleThreadScheduledExecutor(new CustomThreadFactory("GamesCleanupScheduler"));
        cleanupExecutor.scheduleAtFixedRate(() -> gameRepository.findAll().forEach(game -> {
            // If game was opened more than a hour ago, and there is 1 player at most, close it.
            if (((System.currentTimeMillis() - game.getTimeCreated()) > TimeUnit.HOURS.toMillis(1)) &&
                (game.getPlayers().size() <= 1)) {
                gameRepository.stopGame(game.getId());
            }
        }), 0, 15, TimeUnit.MINUTES);
    }

    /**
     * See {@link GameRepository#createNewGame(GameSettings, GameEngine.PlayerUpdateListener)}
     */
    public GameEngine createNewGame(GameSettings settings) {
        // Create a new game and listen to every single user update. (Update of chips affects the coins of a user)
        return gameRepository.createNewGame(settings, (player, chips) -> {
            // The chips that we receive here can be positive, when player earns chips, or negative when player
            // loses chips. So here we just add this amount to the amount of user's coins, so we will persist the most up to date value.
            Optional<? extends User> user = userService.findById(player.getId());
            user.ifPresent(value -> userService.updateCoins(value, user.get().getCoins() + chips));
        });
    }

    /**
     * See {@link GameRepository#findGameById(int)}
     */
    public Optional<GameEngine> findById(String gameHash) {
        int gameId = gameIdFromGameHash(gameHash);
        return gameRepository.findGameById(gameId);
    }

    /**
     * See {@link GameRepository#findGameByCreator(String)}
     */
    public Optional<GameEngine> findByCreatorId(String creatorId) {
        return gameRepository.findGameByCreator(creatorId);
    }

    /**
     * Add a player to a game.<br/>
     * If there is no game with the specified game hash, or game is full, or game is currently active, an exception will be thrown.
     * @param gameHash Hash of the game to join to
     * @param player The player that joins a game
     */
    public void joinGame(String gameHash, Player player) {
        int gameId = gameIdFromGameHash(gameHash);
        Optional<GameEngine> game = gameRepository.findGameById(gameId);
        if (game.isEmpty()) {
            throw new IllegalArgumentException("Game not found: " + gameHash);
        }

        // addPlayer throws exception in case the game is active
        game.get().addPlayer(player);
    }

    /**
     * Remove a player from a game.<br/>
     * If there is no game with the specified game hash an exception will be thrown.
     * @param gameHash Hash of a game to leave
     * @param userId The user identifier of the players which is leaving
     */
    public void leaveGame(String gameHash, String userId) {
        int gameId = gameIdFromGameHash(gameHash);
        Optional<GameEngine> game = gameRepository.findGameById(gameId);
        if (game.isEmpty()) {
            throw new IllegalArgumentException("Game not found: " + gameHash);
        }

        // Find a player by its identifier, and remove him from game, in case he is part of the specified game.
        Player player = game.get().getPlayers().getPlayerById(userId);
        if (player != null) {
            game.get().removePlayer(player);
        }
    }

    /**
     * See {@link GameRepository#startGame(int)}
     */
    public void startGame(String gameHash) {
        gameRepository.startGame(gameIdFromGameHash(gameHash));
    }

    /**
     * See {@link GameRepository#stopGame(int)}
     */
    public void stopGame(String gameHash) {
        gameRepository.stopGame(gameIdFromGameHash(gameHash));
    }

    /**
     * Shut down the cleanup executor.
     */
    public void shutdown() {
        cleanupExecutor.shutdownNow();
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
}

