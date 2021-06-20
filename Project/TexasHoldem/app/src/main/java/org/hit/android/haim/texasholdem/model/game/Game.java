package org.hit.android.haim.texasholdem.model.game;

import android.util.Log;

import org.hit.android.haim.texasholdem.model.chat.Chat;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

/**
 * All business logic related to a game is managed by this class.<br/>
 * The class can work in two states, network or AI.
 *
 * @author Haim Adrian
 * @since 12-Jun-21
 */
public class Game {
    private static final String LOGGER = Game.class.getSimpleName();

    /**
     * All {@link GameStepListener}s, those that we update about every step in a running game.<br/>
     * We update {@link org.hit.android.haim.texasholdem.view.GameService} so we will play sound effects,
     * and we also update the {@link org.hit.android.haim.texasholdem.view.fragment.home.GameFragment} so it
     * will show the game process.
     */
    private final Set<GameStepListener> listeners;

    /**
     * When a network game starts, we have a game hash that we receive from server.<br/>
     * We use this game hash in requests from server, such that the server will know which
     * game to serve.
     */
    @Getter
    private String gameHash;

    /**
     * {@link Chat} of this game
     */
    @Getter
    private Chat chat;

    // Hide ctor - Singleton
    private Game() {
        listeners = new HashSet<>(2);
    }

    /**
     * @return The unique instance of {@link Game}
     */
    public static Game getInstance() {
        return GameHolder.INSTANCE;
    }

    /**
     * Start a new game
     * @param gameSettings User defined settings of that game
     */
    public void start(GameSettings gameSettings) {
        Log.i(LOGGER, "Starting game with settings: " + gameSettings);
        this.gameHash = gameSettings.getGameHash();

        // TODO: Check if it is a creation of new network game, or user trying to join existing game

        if (gameSettings.isNetworkGame()) {
            // Start the chat so we will get messages from server
            chat = new Chat(gameHash);
            chat.start();
        }
    }

    /**
     * Stop the game. Disconnects in case of network game
     */
    public void stop() {
        Log.i(LOGGER, "Stopping game");
        if (chat != null) {
            chat.stop();
        }
    }

    /**
     * Register a new {@link GameStepListener}
     * @param listener The listener
     */
    public void addGameStepListener(GameStepListener listener) {
        listeners.add(listener);
    }

    /**
     * Deregister a {@link GameStepListener}
     * @param listener The listener
     */
    public void removeGameStepListener(GameStepListener listener) {
        listeners.remove(listener);
    }

    /**
     * Go over all listeners and notify them about a game step
     * @param step The step to notify about
     */
    private void notifyGameStep(GameStepType step) {
        for (GameStepListener listener : listeners) {
            listener.onStep(step);
        }
    }

    // Thread-safe, lazy-initialization of singleton
    private static class GameHolder {
        private static final Game INSTANCE = new Game();
    }

    /**
     * Implement this interface in order to be notified about game steps, so outside world can
     * respond to steps. For example, playing sound effects in background.
     */
    @FunctionalInterface
    public interface GameStepListener {
        void onStep(GameStepType step);
    }

    public enum GameStepType {
        SHUFFLE, DEAL_CARD, FLIP_CARD, CHECK, CALL, RAISE, ALL_IN, WIN, LOSE,
        /**
         * When it takes too much time for player to play, we use timer sound effect.
         */
        TIMER
    }
}
