package org.hit.android.haim.texasholdem.view.fragment.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.common.model.bean.chat.Message;
import org.hit.android.haim.texasholdem.common.model.bean.game.Player;
import org.hit.android.haim.texasholdem.common.model.game.GameEngine;
import org.hit.android.haim.texasholdem.databinding.FragmentGameBinding;
import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.model.chat.Chat;
import org.hit.android.haim.texasholdem.model.game.Game;
import org.hit.android.haim.texasholdem.view.GameSoundService;
import org.hit.android.haim.texasholdem.view.activity.MainActivity;
import org.hit.android.haim.texasholdem.view.custom.HandView;
import org.hit.android.haim.texasholdem.view.custom.PlayerView;
import org.hit.android.haim.texasholdem.view.fragment.ViewBindedFragment;
import org.hit.android.haim.texasholdem.web.SimpleCallback;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;


/**
 * This fragment displays a game.<br/>
 * A game can be against AI or other players from network.
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class GameFragment extends ViewBindedFragment<FragmentGameBinding> implements Game.GameListener, Chat.ChatListener {
    private static final String LOGGER = GameFragment.class.getSimpleName();
    private static final int MAX_AMOUNT_OF_PLAYERS = 7;
    private static final Set<Integer> SEATS = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6));

    /**
     * Reference all players around the table, mapped to their position (index)
     * for convenient access
     */
    private Map<Integer, PlayerViewAccessor> players;

    /**
     * Use badge drawable to show amount of new incoming messages over chat button
     */
    private BadgeDrawable chatButtonBadge;

    /**
     * Keep a flag to tell whether seat selected or not.<br/>
     * We need this flag to know if there is a need to display seat selection animations or not.
     * After user selects a seat, we hide the seats selection animations.
     */
    private boolean isSeatSelected = false;

    /**
     * Constructs a new {@link GameFragment}
     */
    public GameFragment() {
        super(R.layout.fragment_game, FragmentGameBinding::bind);
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int i = 0;
        players = new HashMap<>(MAX_AMOUNT_OF_PLAYERS);
        players.put(i++, new PlayerViewAccessor(null, getBinding().player0, getBinding().hand0, getBinding().player0bet, getBinding().seat0Selection));
        players.put(i++, new PlayerViewAccessor(null, getBinding().player1, getBinding().hand1, getBinding().player1bet, getBinding().seat1Selection));
        players.put(i++, new PlayerViewAccessor(null, getBinding().player2, getBinding().hand2, getBinding().player2bet, getBinding().seat2Selection));
        players.put(i++, new PlayerViewAccessor(null, getBinding().player3, getBinding().hand3, getBinding().player3bet, getBinding().seat3Selection));
        players.put(i++, new PlayerViewAccessor(null, getBinding().player4, getBinding().hand4, getBinding().player4bet, getBinding().seat4Selection));
        players.put(i++, new PlayerViewAccessor(null, getBinding().player5, getBinding().hand5, getBinding().player5bet, getBinding().seat5Selection));
        players.put(i, new PlayerViewAccessor(null, getBinding().player6, getBinding().hand6, getBinding().player6bet, getBinding().seat6Selection));

        // Hide all players. We will display those that joined a game.
        players.forEach((pos, player) -> {
            player.hide();
            player.seatSelection.setOnClickListener(v -> {
                isSeatSelected = true;
                players.values().forEach(playerView -> playerView.seatSelection.setVisibility(View.GONE));
                Game.getInstance().getThisPlayer().setPosition(pos);
                Game.getInstance().joinGame(p -> Toast.makeText(GameFragment.this.getContext(), "Selected seat is: " + p.getPosition(), Toast.LENGTH_LONG).show());
            });
        });

        // Listen to game and chat updates
        Game.getInstance().addGameListener(this);

        if (Game.getInstance().getGameHash() != null) {
            Game.getInstance().getChat().addChatListener(this);
            getBinding().buttonChat.setVisibility(View.VISIBLE);
            getBinding().buttonChat.setOnClickListener(this::onChatClicked);
        } else {
            getBinding().buttonChat.setVisibility(View.GONE);
        }

        // Register event listeners
        getBinding().buttonCheck.setOnClickListener(this::onCheckClicked);
        getBinding().buttonRaise.setOnClickListener(this::onRaiseClicked);
        getBinding().buttonFold.setOnClickListener(this::onFoldClicked);
        getBinding().buttonLog.setOnClickListener(this::onGameLogClicked);

        // Use badge drawable to show amount of new incoming messages over chat button
        chatButtonBadge = BadgeDrawable.create(getContext());
        chatButtonBadge.setVisible(true);
        chatButtonBadge.setNumber(0);
        BadgeUtils.attachBadgeDrawable(chatButtonBadge, getBinding().buttonChat);

        // Run Game service in background, to play sound effects based on game steps
        getActivity().startService(new Intent(getActivity(), GameSoundService.class));

        // Refresh the view based on current game engine.
        // In case there is no game engine yet, it means we have just entered. Then select a seat
        GameEngine gameEngine = Game.getInstance().getGameEngine();
        if (gameEngine != null) {
            refresh(gameEngine);
        } else {
            playersRefresh(new HashSet<>());
        }
    }

    @Override
    public void onDestroyView() {
        Game.getInstance().removeGameStepListener(this);
        Game.getInstance().getChat().removeChatListener(this);

        super.onDestroyView();
    }

    @Override
    public void onStep(Game.GameStepType step) {
        Log.d(LOGGER, "Game Step: " + step);

    }

    @Override
    public void refresh(GameEngine gameEngine) {
        new Handler().post(() -> {

        });
    }

    @Override
    public void playersRefresh(Set<Player> players) {
        new Handler().post(() -> {
            // Keep available seats
            Set<Integer> availableSeats = new HashSet<>(SEATS);
            players.forEach(p -> {
                availableSeats.remove(p.getPosition());
                PlayerViewAccessor playerViewAccessor = this.players.get(p.getPosition());
                refreshPlayerViewFromPlayer(playerViewAccessor, p);
            });

            // Make sure animations are visible
            if (!isSeatSelected) {
                availableSeats.forEach(seat -> {
                    PlayerViewAccessor playerViewAccessor = this.players.get(seat);
                    playerViewAccessor.seatSelection.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    /**
     * Refresh a player view widgets based on data of a {@link Player}
     * @param playerView The player view accessor to update its widgets
     * @param player The player to get data from
     */
    private void refreshPlayerViewFromPlayer(PlayerViewAccessor playerView, Player player) {
        playerView.player = player;
        playerView.setVisible(true);
        playerView.seatSelection.setVisibility(View.GONE);
        playerView.handView.setVisibility(View.INVISIBLE);
        playerView.playerBet.setVisibility(View.INVISIBLE);
        playerView.playerView.getPlayerProgressBar().setVisibility(View.INVISIBLE);

        String playerLocalName = playerView.playerView.getPlayerNameTextView().getText().toString();
        playerView.playerView.getPlayerChipsTextView().setText(player.getChips().toShorthand());
        playerView.playerView.getPlayerNameTextView().setText(player.getName());

        // Now, before messing with image, make sure local data is different to save time.
        if (!player.getName().equals(playerLocalName)) {
            TexasHoldemWebService.getInstance().getUserService().getUserInfo(player.getId()).enqueue(new UserInfoCallback(playerView));
        }
    }

    @Override
    public void onGameError(String errorMessage) {
        Log.e(LOGGER, "Game Error: " + errorMessage);
        new Handler().post(() -> Snackbar.make(getView(), errorMessage, BaseTransientBottomBar.LENGTH_LONG).show());
    }

    @Override
    public void onMessageArrived(Message message) {
        Log.d(LOGGER, "Message arrived: " + message);

        new Handler().post(() -> {
            int currNewMessagesCount = chatButtonBadge.getNumber();

            if (currNewMessagesCount == 0) {
                chatButtonBadge.setVisible(true);
            }

            chatButtonBadge.setNumber(currNewMessagesCount + 1);
        });
    }

    @Override
    public void onChatError(String errorMessage) {
        Log.e(LOGGER, "Chat Error: " + errorMessage);
    }

    /**
     * Occurs when user presses the CHECK button, to apply check action if possible
     * @param checkButton The CHECK button
     */
    void onCheckClicked(View checkButton) {

    }

    /**
     * Occurs when user presses the RAISE button, to raise if possible
     * @param raiseButton The RAISE button
     */
    void onRaiseClicked(View raiseButton) {

    }

    /**
     * Occurs when user presses the FOLD button, to fold if possible
     * @param foldButton The FOLD button
     */
    void onFoldClicked(View foldButton) {

    }

    /**
     * Occurs when user presses the CHAT button, to open chat fragment
     * @param chatButton The CHAT button
     */
    void onChatClicked(View chatButton) {
        chatButtonBadge.setNumber(0);
        chatButtonBadge.setVisible(false);

        ((MainActivity)getActivity()).navigateToFragment(R.id.nav_chat);
    }

    /**
     * Occurs when user presses the LOG button, to open game log dialog
     * @param gameLogButton The LOG button
     */
    void onGameLogClicked(View gameLogButton) {

    }

    /**
     * A class that holds all widgets of a player.<br/>
     * It was created in order to ease the way we work with players during a game.<br/>
     * It was too much to do all of that in a player view since we needed to place different widgets
     * in different areas, and even with different parents.
     */
    private static class PlayerViewAccessor {
        /**
         * The player associated to this player view accessor
         */
        @Nullable
        Player player;

        /**
         * The player view representing this player
         */
        @NonNull
        PlayerView playerView;

        /**
         * The hand (2-cards) of this player
         */
        @NonNull
        HandView handView;

        /**
         * A text view displaying bet sum of this player
         */
        @NonNull
        TextView playerBet;

        /**
         * Before game starts, players select where to sit. This is an animation view we show over seats
         */
        @NonNull
        LottieAnimationView seatSelection;

        /**
         * Constructs a new {@link PlayerViewAccessor}
         */
        public PlayerViewAccessor(@Nullable Player player, @NonNull PlayerView playerView,
                                  @NonNull HandView handView, @NonNull TextView playerBet,
                                  @NonNull LottieAnimationView seatSelection) {
            this.player = player;
            this.playerView = playerView;
            this.handView = handView;
            this.playerBet = playerBet;
            this.seatSelection = seatSelection;
        }

        /**
         * Sets all player component visible
         */
        void show() {
            setVisible(true);
        }

        /**
         * Sets all player component invisible
         */
        void hide() {
            setVisible(false);
        }

        /**
         * Sets the visibility of a player components
         * @param isVisible Visible or invisible
         */
        void setVisible(boolean isVisible) {
            int visibility = isVisible ? View.VISIBLE : View.INVISIBLE;
            playerView.setVisibility(visibility);
            handView.setVisibility(visibility);
            playerBet.setVisibility(visibility);
        }
    }

    /**
     * This class created to handle User info responses from server.<br/>
     * The server returns the user model when we ask for user info by identifier, so we can display
     * player images.
     */
    private class UserInfoCallback extends SimpleCallback<JsonNode> {
        /**
         * The player view to update with user info
         */
        @NonNull
        private final PlayerViewAccessor playerViewAccessor;

        /**
         * Constructs a new {@link UserInfoCallback}
         * @param playerViewAccessor The player view to update with user info
         */
        public UserInfoCallback(@NonNull PlayerViewAccessor playerViewAccessor) {
            this.playerViewAccessor = playerViewAccessor;
        }

        @Override
        public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
            if (!response.isSuccessful()) {
                Log.e(LOGGER, "Failed to get user info of: " + playerViewAccessor.playerView.getPlayerNameTextView().getText().toString());
            } else {
                JsonNode body = response.body();
                try {
                    User user = TexasHoldemWebService.getInstance().getObjectMapper().readValue(body.toString(), User.class);
                    Log.d(LOGGER, "Received user info: " + user);

                    if (user.getImage() != null) {
                        playerViewAccessor.playerView.getPlayerImageView().setImageBitmap(user.getImageBitmap());
                    } else {
                        // Default user image
                        playerViewAccessor.playerView.getPlayerImageView().setImageResource(R.drawable.user);
                    }
                } catch (IOException e) {
                    Log.e(LOGGER, "Failed parsing response. Response was: " + body, e);
                }
            }
        }
    }
}