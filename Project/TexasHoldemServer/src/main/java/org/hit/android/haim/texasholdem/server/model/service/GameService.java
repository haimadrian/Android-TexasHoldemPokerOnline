package org.hit.android.haim.texasholdem.server.model.service;

import org.hit.android.haim.texasholdem.server.model.bean.game.GameSettings;
import org.hit.android.haim.texasholdem.server.model.bean.user.User;
import org.hit.android.haim.texasholdem.server.model.game.GameEngine;
import org.hit.android.haim.texasholdem.server.model.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
    public Optional<GameEngine> findById(int id) {
        return gameRepository.findGameById(id);
    }

    /**
     * See {@link GameRepository#closeGame(int)}
     */
    public Optional<GameEngine> endGame(int id) {
        return gameRepository.closeGame(id);
    }
}

