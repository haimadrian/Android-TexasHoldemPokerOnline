package org.hit.android.haim.texasholdem.server.model.service;

import org.hit.android.haim.texasholdem.server.model.GameEngine;
import org.hit.android.haim.texasholdem.server.model.repository.GameRepository;
import org.hit.android.haim.texasholdem.server.model.repository.UserRepository;
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
    private UserRepository userRepository;

    /**
     * See {@link GameRepository#createNewGame()}
     */
    public GameEngine createNewGame() {
        return gameRepository.createNewGame();
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

