package org.hit.android.haim.texasholdem.model.game;

import android.util.Log;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import org.hit.android.haim.texasholdem.common.model.bean.game.Player;
import org.hit.android.haim.texasholdem.common.model.game.Chips;
import org.hit.android.haim.texasholdem.common.model.game.GameEngine;
import org.hit.android.haim.texasholdem.common.util.CustomThreadFactory;
import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.model.chat.Chat;
import org.hit.android.haim.texasholdem.view.GameSoundService;
import org.hit.android.haim.texasholdem.web.SimpleCallback;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Response;

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
     * Single thread pool for refreshing seats availability while user selects a seat, or game engine
     * during a running game.
     */
    private ScheduledExecutorService refreshThread;

    /**
     * An executor that runs listener updates asynchronously, to avoid of blocking Game.
     */
    private ExecutorService notifierThread;

    /**
     * All {@link GameListener}s, those that we update about every step in a running game.<br/>
     * We update {@link GameSoundService} so we will play sound effects,
     * and we also update the {@link org.hit.android.haim.texasholdem.view.fragment.home.GameFragment} so it
     * will show the game process.
     */
    private final Set<GameListener> gameListeners;

    /**
     * When a network game starts, we have a game hash that we receive from server.<br/>
     * We use this game hash in requests from server, such that the server will know which
     * game to serve.
     */
    @Getter
    private String gameHash;

    /**
     * The player representing us, this client
     */
    @Getter
    private Player thisPlayer;

    /**
     * {@link Chat} of this game
     */
    @Getter
    private Chat chat;

    /**
     * {@link GameEngine} is received from cloud and analyzed here in order to have game info
     * and understand what step was taken.
     */
    @Getter
    private GameEngine gameEngine;

    /**
     * Keep set of players to compare when we refresh players, to notify only in case there is a difference in players.
     */
    private Set<Player> playersInGame;

    // Hide ctor - Singleton
    private Game() {
        gameListeners = new HashSet<>(2);
    }

    /**
     * @return The unique instance of {@link Game}
     */
    public static Game getInstance() {
        return GameHolder.INSTANCE;
    }

    /**
     * Start a new game.<br/>
     * This method is called from {@link org.hit.android.haim.texasholdem.view.fragment.home.PlayNetworkFragment}
     * when user joins a game, or when user creates a new network game.<br/>
     * or from {@link org.hit.android.haim.texasholdem.view.fragment.home.PlayAiFragment}
     * when user starts a new training game.
     * @param gameSettings User defined settings of that game
     * @param user The user starting a game
     */
    public void start(ClientGameSettings gameSettings, User user) {
        Log.i(LOGGER, "Starting game with settings: " + gameSettings);
        this.gameHash = gameSettings.getGameHash();

        thisPlayer = Player.builder().id(user.getId())
                .name(user.getName())
                .chips(new Chips(gameSettings.getChips()))
                .build();

        if (gameSettings.isNetwork()) {
            notifierThread = Executors.newFixedThreadPool(2, new CustomThreadFactory("GameNotifier"));
            refreshThread = Executors.newSingleThreadScheduledExecutor(new CustomThreadFactory("SeatsAvailabilityRefreshThread"));
            refreshThread.scheduleAtFixedRate(this::refreshPlayers, 0, 500, TimeUnit.MILLISECONDS);

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

        if (refreshThread != null) {
            refreshThread.shutdownNow();
            refreshThread = null;
        }

        if (notifierThread != null) {
            notifierThread.shutdownNow();
            notifierThread = null;
        }
    }

    /**
     * Join {@link #thisPlayer} to game over network.<br/>
     * Before joining a game, make sure you set the position of the player.
     * @param onResponse Once we receive a response from server, we will pass it to this consumer.
     */
    public void joinGame(Consumer<Player> onResponse) {
        TexasHoldemWebService.getInstance().getGameService().joinGame(gameHash, thisPlayer).enqueue(new SimpleCallback<JsonNode>() {
            @Override
            public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
                if (!response.isSuccessful()) {
                    String errorMessage = TexasHoldemWebService.getInstance().readHttpErrorResponse(response);
                    Log.e(LOGGER, "Failed to join game: " + errorMessage);
                    notifyGameError(errorMessage);
                } else {
                    JsonNode body = response.body();
                    try {
                        Player player = TexasHoldemWebService.getInstance().getObjectMapper().readValue(body.toString(), Player.class);
                        Log.d(LOGGER, "Received player: " + player);

                        // Update position based on server
                        Game.this.thisPlayer.setPosition(player.getPosition());
                        onResponse.accept(player);

                        // Send last players update to listener
                        refreshPlayers();
                    } catch (IOException e) {
                        Log.e(LOGGER, "Failed parsing response. Response was: " + body, e);
                        notifyGameError("Something went wrong. Try again.");
                    }
                }
            }
        });
    }

    /**
     * Get list of players that are connected to our game, from cloud, and notify listener when it is updated.
     */
    private void refreshPlayers() {
        TexasHoldemWebService.getInstance().getGameService().getPlayers(gameHash).enqueue(new SimpleCallback<JsonNode>() {
            @Override
            public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
                if (!response.isSuccessful()) {
                    Log.e(LOGGER, "Failed to get players");
                } else {
                    JsonNode body = response.body();
                    try {
                        Set<Player> players = TexasHoldemWebService.getInstance().getObjectMapper().readValue(body.toString(), new TypeReference<Set<Player>>() {});
                        Log.d(LOGGER, "Received players: " + players);

                        if ((Game.this.playersInGame == null) || (!Game.this.playersInGame.equals(players))) {
                            notifyPlayersRefresh(players);
                        }

                        // Keep it for next timer tick
                        Game.this.playersInGame = players;
                    } catch (IOException e) {
                        Log.e(LOGGER, "Failed parsing response. Response was: " + body, e);
                    }
                }
            }
        });
    }

    /**
     * Register a new {@link GameListener}
     * @param listener The listener
     */
    public void addGameListener(GameListener listener) {
        gameListeners.add(listener);
    }

    /**
     * Deregister a {@link GameListener}
     * @param listener The listener
     */
    public void removeGameStepListener(GameListener listener) {
        gameListeners.remove(listener);
    }

    /**
     * Go over all listeners and notify them about a game step
     * @param step The step to notify about
     */
    private void notifyGameStep(GameStepType step) {
        for (GameListener listener : gameListeners) {
            notifierThread.submit(() -> listener.onStep(step));
        }
    }

    /**
     * Go over all listeners and notify them about a game info update
     * @param gameEngine The game info to notify about
     */
    private void notifyGameRefresh(GameEngine gameEngine) {
        for (GameListener listener : gameListeners) {
            notifierThread.submit(() -> listener.refresh(gameEngine));
        }
    }

    /**
     * Go over all listeners and notify them about a players update
     * @param players The players to notify about
     */
    private void notifyPlayersRefresh(Set<Player> players) {
        for (GameListener listener : gameListeners) {
            notifierThread.submit(() -> listener.playersRefresh(players));
        }
    }

    /**
     * Go over all listeners and notify them about a game error
     * @param errorMessage An error to notify about
     */
    private void notifyGameError(String errorMessage) {
        for (GameListener listener : gameListeners) {
            notifierThread.submit(() -> listener.onGameError(errorMessage));
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
    public interface GameListener {
        /**
         * Occurs whenever a new step is detected.<br/>
         * For UI refresh, use {@link #refresh(GameEngine)}
         * @param step The new step
         */
        void onStep(GameStepType step);

        /**
         * Occurs every 1 second to refresh the UI
         * @param gameEngine Reference to {@link GameEngine} to have game info
         */
        void refresh(GameEngine gameEngine);

        /**
         * Occurs when there is a new update about players in a game.<br/>
         * Use it to show available seats.
         * @param players The players that are currently in a game
         */
        void playersRefresh(Set<Player> players);

        /**
         * Occurs when some error occurred while joining a game or getting game info
         * @param errorMessage A message to show to the user about the error
         */
        void onGameError(String errorMessage);
    }

    public enum GameStepType {
        SHUFFLE, DEAL_CARD, FLIP_CARD, CHECK, CALL, RAISE, ALL_IN, WIN, LOSE,
        /**
         * When it takes too much time for player to play, we use timer sound effect.
         */
        TIMER
    }
}
