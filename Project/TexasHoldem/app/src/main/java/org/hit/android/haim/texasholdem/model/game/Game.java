package org.hit.android.haim.texasholdem.model.game;

import android.util.Log;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import org.hit.android.haim.texasholdem.common.model.bean.game.Player;
import org.hit.android.haim.texasholdem.common.model.bean.game.PlayerAction;
import org.hit.android.haim.texasholdem.common.model.game.Chips;
import org.hit.android.haim.texasholdem.common.model.game.GameEngine;
import org.hit.android.haim.texasholdem.common.model.game.Pot;
import org.hit.android.haim.texasholdem.common.util.CustomThreadFactory;
import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.model.chat.Chat;
import org.hit.android.haim.texasholdem.view.GameSoundService;
import org.hit.android.haim.texasholdem.web.HttpStatus;
import org.hit.android.haim.texasholdem.web.SimpleCallback;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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
     * Single thread pool for refreshing seats availability while user selects a seat. Once
     * a game is started, this thread is stopped cause user cannot select another seat while
     * game is running.
     */
    private ScheduledExecutorService seatsAvailabilityRefreshThread;

    /**
     * Single thread pool for refreshing game engine during a running game.<br/>
     * We ask the cloud about game engine info to know when a game is started, and what steps were taken.
     */
    private ScheduledExecutorService gameRefreshThread;

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

    /**
     * When we join a game, we will leave it at {@link #stop(Runnable)}.<br/>
     * If we have not joined a game, then there is nothing to leave.
     */
    @Getter
    @Setter
    private boolean isJoinedGame = false;

    /**
     * Keep a reference to the last notification we performed, so we can make the notifications unique
     * and notify about new game steps only.
     * @see GameStepNotificationInfo
     */
    private GameStepNotificationInfo lastNotification = null;

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
     * Initialize a new game.<br/>
     * This method is called from {@link org.hit.android.haim.texasholdem.view.fragment.home.PlayNetworkFragment}
     * when user joins a game, or when user creates a new network game.<br/>
     * or from {@link org.hit.android.haim.texasholdem.view.fragment.home.PlayAiFragment}
     * when user starts a new training game.
     * @param gameSettings User defined settings of that game
     * @param user The user starting a game
     */
    public void init(ClientGameSettings gameSettings, User user) {
        Log.i(LOGGER, "Starting game with settings: " + gameSettings);
        this.gameHash = gameSettings.getGameHash();

        thisPlayer = Player.builder().id(user.getId())
                .name(user.getName())
                .chips(new Chips(gameSettings.getChips()))
                .build();

        if (gameSettings.isNetwork()) {
            initNetworkGame();
        }
    }

    /**
     * Initializes network components. Those are game notifier, chat, and game refresh.
     */
    private void initNetworkGame() {
        if (notifierThread != null) {
            stop(null);
        }

        notifierThread = Executors.newFixedThreadPool(2, new CustomThreadFactory("GameNotifier"));

        // Refresh seats availability every 500 milliseconds, to get "realtime" updates.
        seatsAvailabilityRefreshThread = Executors.newSingleThreadScheduledExecutor(new CustomThreadFactory("SeatsAvailabilityRefreshThread"));
        seatsAvailabilityRefreshThread.scheduleAtFixedRate(this::refreshPlayers, 0, 1, TimeUnit.SECONDS);

        // Refresh game state once a second, this isn't a realtime update, but good enough without
        // flooding the server with requests.
        gameRefreshThread = Executors.newSingleThreadScheduledExecutor(new CustomThreadFactory("GameRefreshThread"));
        gameRefreshThread.scheduleAtFixedRate(this::refreshGame, 1, 1, TimeUnit.SECONDS);

        // Start the chat so we will get messages from server
        chat = new Chat(gameHash);
        chat.start();
    }

    /**
     * Start the game.<br/>
     * This will change the game state to active. Use this method when all players are ready, to
     * start the game and get notified upon game steps / refreshes.
     * @param runLater Task to run upon success. It will receive empty string in case of success. Otherwise, the error message
     */
    public void start(Consumer<String> runLater) {
        if (gameHash != null) {
            TexasHoldemWebService.getInstance().getGameService().startGame(gameHash).enqueue(new SimpleCallback<JsonNode>() {
                @Override
                public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
                    if (!response.isSuccessful()) {
                        runLater.accept(TexasHoldemWebService.getInstance().readHttpErrorResponse(response));
                    } else {
                        runLater.accept("");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonNode> call, @NonNull Throwable t) {
                    Log.e(LOGGER, "Failed to send START request", t);
                    runLater.accept("");
                }
            });
        }
    }

    /**
     * Stop the game. Disconnects in case of network game
     * @param runLater A task to run after leaving a game
     */
    public void stop(Runnable runLater) {
        Log.i(LOGGER, "Stopping game");
        boolean isRunLaterExecuted = false;
        if (chat != null) {
            chat.stop();
        }

        if (isJoinedGame) {
            isRunLaterExecuted = true;
            TexasHoldemWebService.getInstance().getGameService().leaveGame(gameHash).enqueue(new SimpleCallback<JsonNode>() {
                @Override
                public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
                    Log.d(LOGGER, "Left game.");
                    if (runLater != null) {
                        runLater.run();
                    }
                }
            });
        }

        if (seatsAvailabilityRefreshThread != null) {
            seatsAvailabilityRefreshThread.shutdownNow();
            seatsAvailabilityRefreshThread = null;
        }

        if (gameRefreshThread != null) {
            gameRefreshThread.shutdownNow();
            gameRefreshThread = null;
        }

        if (notifierThread != null) {
            notifierThread.shutdownNow();
            notifierThread = null;
        }

        if (!isRunLaterExecuted && (runLater != null)) {
            runLater.run();
        }
    }

    /**
     * Join {@link #thisPlayer} to game over network.<br/>
     * Before joining a game, make sure you set the position of the player.
     * @param onResponse Once we receive a response from server, we will pass it to this consumer.
     */
    public void joinGame(Consumer<Player> onResponse) {
        // For network game
        if (gameHash != null) {
            TexasHoldemWebService.getInstance().getGameService().joinGame(gameHash, thisPlayer).enqueue(new SimpleCallback<JsonNode>() {
                @Override
                public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
                    if (!response.isSuccessful()) {
                        String errorMessage = TexasHoldemWebService.getInstance().readHttpErrorResponse(response);
                        Log.e(LOGGER, "Failed to join game: " + errorMessage);
                        notifyGameError(errorMessage);
                    } else {
                        isJoinedGame = true;
                        JsonNode body = response.body();
                        try {
                            Player player = TexasHoldemWebService.getInstance().getObjectMapper().readValue(body.toString(), Player.class);
                            Log.d(LOGGER, "Received player: " + player);

                            // Update position based on server
                            Game.this.thisPlayer.setPosition(player.getPosition());
                            onResponse.accept(player);

                            // Send last players update to listener
                            refreshPlayers();
                        } catch (Exception e) {
                            Log.e(LOGGER, "Failed parsing response. Response was: " + body, e);
                            notifyGameError("Something went wrong. Try again.");
                        }
                    }
                }
            });
        }
    }

    /**
     * Check if this game is currently active or not.<br/>
     * We need to act different when a game is active and the GameFragment is re-created. In
     * this case, we would not want to display seat selection animations, but the game state.
     * @return Whether this game is active or not
     */
    public boolean isActive() {
        return gameEngine != null;
    }

    /**
     * Execute a task in case current player is the creator of this game.<br/>
     * When current player is the creator of a game, he has a START button to start a network
     * game after all players joined. So in order to display that button, we need to know if
     * current player is the creator. For this, you can use this method to execute some code
     * in case current player is the creator.
     * @param run A task to run
     */
    public void ifThisPlayerIsTheOwner(Runnable run) {
        // For network game
        if (gameHash != null) {
            TexasHoldemWebService.getInstance().getGameService().getMyGameHash().enqueue(new SimpleCallback<JsonNode>() {
                @Override
                public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
                    if (!response.isSuccessful()) {
                        // Print it in DEBUG. We get NOT FOUND for players that are not the creator.
                        Log.d(LOGGER, "Failed to get my game hash while trying to check if current player is the creator: " + TexasHoldemWebService.getInstance().readHttpErrorResponse(response));
                    } else {
                        JsonNode body = response.body();
                        try {
                            String gameHash = TexasHoldemWebService.getInstance().getObjectMapper().readValue(body.toString(), TextNode.class).asText();
                            Log.d(LOGGER, "Received game hash: " + gameHash);
                            run.run();
                        } catch (Exception e) {
                            Log.e(LOGGER, "Failed parsing response. Response was: " + body, e);
                        }
                    }
                }
            });
        } else {
            // For local game we get here for our player only, so run the action.
            run.run();
        }
    }

    /**
     * Do something in case a specified player is part of a game.<br/>
     * In case of an active game, the {@code runLater} will be executed immediately. Otherwise,
     * we query the cloud to find the game. If there is no such game, runLater will be supplied with null.
     * @param playerId Identifier of this player. (our player)
     * @param runLater A task to run when we have game engine available.
     */
    public void ifPlayerPartOfGame(String playerId, Consumer<GameEngine> runLater) {
        if (gameEngine != null) {
            runLater.accept(gameEngine);
        } else {
            TexasHoldemWebService.getInstance().getGameService().getMyGame().enqueue(new SimpleCallback<JsonNode>() {
                @Override
                public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
                    if (!response.isSuccessful()) {
                        runLater.accept(null);
                    } else {
                        JsonNode body = response.body();
                        try {
                            GameEngine gameEngine = TexasHoldemWebService.getInstance().getObjectMapper().readValue(body.toString(), GameEngine.class);
                            if (gameEngine != null) {
                                gameHash = gameEngine.getGameHash();
                                isJoinedGame = true;
                                Player player = gameEngine.getPlayers().getPlayerById(playerId);

                                if (thisPlayer == null) {
                                    thisPlayer = Player.builder().id(player.getId())
                                            .name(player.getName())
                                            .chips(new Chips(player.getChips().get()))
                                            .build();
                                }

                                thisPlayer.setPosition(player.getPosition());

                                // If Game.init was not called, call it now
                                if (notifierThread == null) {
                                    initNetworkGame();
                                }
                            }

                            runLater.accept(gameEngine);
                        } catch (Exception e) {
                            Log.e(LOGGER, "Failed parsing response. Response was: " + body, e);
                            runLater.accept(null);
                        }
                    }
                }
            });
        }
    }

    /**
     * Get list of players that are connected to our game, from cloud, and notify listener when it is updated.
     */
    private void refreshPlayers() {
        if (gameHash != null) {
            TexasHoldemWebService.getInstance().getGameService().getPlayers(gameHash).enqueue(new SimpleCallback<JsonNode>() {
                @Override
                public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
                    if (!response.isSuccessful()) {
                        Log.e(LOGGER, "Failed to get players");
                    } else {
                        JsonNode body = response.body();
                        try {
                            Set<Player> players = TexasHoldemWebService.getInstance().getObjectMapper().readValue(body.toString(), new TypeReference<Set<Player>>() {
                            });
                            Log.d(LOGGER, "Received players: " + players);

                            if ((Game.this.playersInGame == null) || (!Game.this.playersInGame.equals(players))) {
                                notifyPlayersRefresh(players);
                            }

                            // Keep it for next timer tick
                            Game.this.playersInGame = players;
                        } catch (Exception e) {
                            Log.e(LOGGER, "Failed parsing response. Response was: " + body, e);
                        }
                    }
                }
            });
        }
    }

    /**
     * Ask the cloud for game engine info.<br/>
     * Once the game is started, we stop the seats availability thread, and let the listener get
     * the updates about the game.
     */
    private void refreshGame() {
        if (gameHash != null) {
            TexasHoldemWebService.getInstance().getGameService().getGameInfo(gameHash).enqueue(new SimpleCallback<JsonNode>() {
                @Override
                public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
                    if (!response.isSuccessful()) {
                        // Before user joins a game, we get bad request cause use that not part of
                        // a game cannot receive game updates. Just ignore those failures.
                        if ((response.code() != HttpStatus.BAD_REQUEST.getCode()) && (response.code() != HttpStatus.NOT_FOUND.getCode())) {
                            Log.e(LOGGER, "Failed to get game update");
                            notifyGameError("Game is Out of Sync");
                        }
                    } else {
                        JsonNode body = response.body();
                        try {
                            GameEngine gameEngine = TexasHoldemWebService.getInstance().getObjectMapper().readValue(body.toString(), GameEngine.class);
                            Log.d(LOGGER, "Received game: " + gameEngine);

                            if (gameEngine != null) {
                                // Before game is started, it is in READY state, which means we are
                                // waiting for the creator to start it. Meanwhile we refresh seats.
                                if (gameEngine.getGameState() != GameEngine.GameState.READY) {
                                    Game.this.gameEngine = gameEngine;

                                    // In case seats availability thread is still running, stop it.
                                    // We get here one time only, when a game is started, so we stop
                                    // the seats availability thread.
                                    if (seatsAvailabilityRefreshThread != null) {
                                        seatsAvailabilityRefreshThread.shutdownNow();
                                        seatsAvailabilityRefreshThread = null;
                                    }

                                    notifyGameRefresh(gameEngine);
                                    detectAndNotifyAboutGameStep();
                                }
                            }
                        } catch (Exception e) {
                            Log.e(LOGGER, "Failed parsing response. Response was: " + body, e);
                        }
                    }
                }
            });
        }
    }

    /**
     * Analyze {@link #gameEngine} to find out the last step and notify about it, this way we can
     * play sound effects / animations.
     */
    private void detectAndNotifyAboutGameStep() {
        if (gameEngine != null) {
            GameStepType gameStep = null;
            Player player = gameEngine.getPlayers().getPreviousPlayer();

            if (gameEngine.getLastActionKind() != null) {
                switch (gameEngine.getLastActionKind()) {
                    case FOLD:
                        gameStep = GameStepType.FOLD;
                        break;
                    case CHECK:
                        gameStep = GameStepType.CHECK;
                        break;
                    case CALL:
                        if ((player != null) && (player.getChips().get() == 0)) {
                            gameStep = GameStepType.ALL_IN;
                        } else {
                            gameStep = GameStepType.CALL;
                        }
                        break;
                    case RAISE:
                        if ((player != null) && (player.getChips().get() == 0)) {
                            gameStep = GameStepType.ALL_IN;
                        } else {
                            gameStep = GameStepType.RAISE;
                        }
                        break;
                }
            }

            // Check if need to play timer sound.
            // Don't play timer if there is less than 11 seconds. Anyway we want it to be heard once.
            long timePassed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - gameEngine.getPlayerTurnTimer().getTurnStartTime());
            long timeLeft = gameEngine.getGameSettings().getTurnTime() - timePassed;
            if ((timeLeft >= 11) && (timeLeft <= 13)) {
                // A player action (CHECK, CALL, RAISE, FOLD) has higher priority than timer.
                // So check here to make sure TIMER does not override player action.
                if ((lastNotification == null) || (lastNotification.gameStepType != GameStepType.TIMER)) {
                    gameStep = GameStepType.TIMER;
                }
            }

            // Check for win or lose, in case it is relevant
            Map<String, Pot.PlayerWinning> playerToEarnings = gameEngine.getPlayerToEarnings();
            if (playerToEarnings != null) {
                gameStep = playerToEarnings.containsKey(thisPlayer.getId()) ? GameStepType.WIN : GameStepType.LOSE;
            }

            GameStepNotificationInfo newNotification = new GameStepNotificationInfo();
            newNotification.player = player;
            newNotification.gameStepType = gameStep;

            if (!newNotification.equals(lastNotification)) {
                lastNotification = newNotification;
                notifyGameStep(gameEngine, newNotification.gameStepType);
            }
        }
    }

    /**
     * Execute a {@link PlayerAction}.<br/>
     * Listener will be notified in case there was an error
     * @param playerAction The action to execute
     */
    public void executePlayerAction(PlayerAction playerAction) {
        TexasHoldemWebService.getInstance().getGameService().executePlayerAction(gameHash, playerAction).enqueue(new SimpleCallback<JsonNode>() {
            @Override
            public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
                if (!response.isSuccessful()) {
                    notifyGameError(TexasHoldemWebService.getInstance().readHttpErrorResponse(response));
                }
            }
        });
    }

    public Set<Player> getPlayers() {
        if (gameEngine != null) {
            return gameEngine.getPlayers().getPlayers();
        }

        return playersInGame;
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
     * Go over all listeners except the specified listener, and notify them about a game step.<br/>
     * This is exposed so game fragment can notify when a card is flipped, and sync with flip sound effect
     * @param step The step to notify about
     */
    public void notifyGameStep(GameEngine gameEngine, GameStepType step, GameListener ignore) {
        for (GameListener listener : gameListeners) {
            if (!listener.equals(ignore)) {
                notifierThread.submit(() -> listener.onStep(gameEngine, step));
            }
        }
    }

    /**
     * Go over all listeners and notify them about a game step
     * @param step The step to notify about
     */
    private void notifyGameStep(GameEngine gameEngine, GameStepType step) {
        for (GameListener listener : gameListeners) {
            notifierThread.submit(() -> listener.onStep(gameEngine, step));
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
        void onStep(GameEngine gameEngine, GameStepType step);

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
        SHUFFLE, DEAL_CARD, FLIP_CARD, CHECK, CALL, RAISE, ALL_IN, FOLD, WIN, LOSE,
        /**
         * When it takes too much time for player to play, we use timer sound effect.
         */
        TIMER
    }

    /**
     * We need to keep reference to the last game step notification, so we will not notify
     * about the same step every second. We need to notify about a step once only.
     */
    @Data
    private static class GameStepNotificationInfo {
        private Player player;
        private GameStepType gameStepType;
    }
}
