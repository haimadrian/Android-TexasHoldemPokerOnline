package org.hit.android.haim.texasholdem.server.model.repository;

import org.hit.android.haim.texasholdem.server.model.bean.game.GameSettings;
import org.hit.android.haim.texasholdem.server.model.game.GameEngine;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A repository holding all active games.<br/>
 * Do not use this class directly. Instead, use {@link org.hit.android.haim.texasholdem.server.model.service.GameService}
 *
 * @author Haim Adrian
 * @since 08-May-21
 */
public class GameRepository {
    private final Map<Integer, GameEngine> games = new ConcurrentHashMap<>();

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
        GameEngine game = new GameEngine(settings, listener);
        games.put(game.getId(), game);
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
     * End a running game.<br/>
     * After closing a game it is deleted, hence you won't be able to find this game.
     * @param gameId The identifier of a game
     * @return The game in case it was running
     */
    public Optional<GameEngine> closeGame(int gameId) {
        GameEngine existingGame = games.remove(gameId);
        if (existingGame == null) {
            return Optional.empty();
        }

        existingGame.stop();
        return Optional.of(existingGame);
    }

    private static class SingletonRef {
        static final GameRepository instance = new GameRepository();
    }
}

