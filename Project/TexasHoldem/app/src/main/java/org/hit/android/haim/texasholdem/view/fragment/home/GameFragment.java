package org.hit.android.haim.texasholdem.view.fragment.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatButton;

import com.airbnb.lottie.LottieAnimationView;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.common.model.bean.chat.Message;
import org.hit.android.haim.texasholdem.common.model.bean.game.Board;
import org.hit.android.haim.texasholdem.common.model.bean.game.Card;
import org.hit.android.haim.texasholdem.common.model.bean.game.Hand;
import org.hit.android.haim.texasholdem.common.model.bean.game.Player;
import org.hit.android.haim.texasholdem.common.model.bean.game.PlayerAction;
import org.hit.android.haim.texasholdem.common.model.bean.game.PlayerActionKind;
import org.hit.android.haim.texasholdem.common.model.game.Chips;
import org.hit.android.haim.texasholdem.common.model.game.GameEngine;
import org.hit.android.haim.texasholdem.common.model.game.Pot;
import org.hit.android.haim.texasholdem.databinding.FragmentGameBinding;
import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.model.chat.Chat;
import org.hit.android.haim.texasholdem.model.game.Game;
import org.hit.android.haim.texasholdem.view.GameSoundService;
import org.hit.android.haim.texasholdem.view.activity.MainActivity;
import org.hit.android.haim.texasholdem.view.custom.CardView;
import org.hit.android.haim.texasholdem.view.custom.HandView;
import org.hit.android.haim.texasholdem.view.custom.PlayerView;
import org.hit.android.haim.texasholdem.view.fragment.ViewBindedFragment;
import org.hit.android.haim.texasholdem.web.SimpleCallback;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
     * A reference to the {@link Game} class, to ease access and avoid of "game." syntax.
     */
    private final Game game;

    /**
     * Reference all players around the table, mapped to their position (index)
     * for convenient access
     */
    private Map<Integer, PlayerViewAccessor> players;

    /**
     * See {@link Cards}
     */
    private Cards cardsResource;

    /**
     * Use badge drawable to show amount of new incoming messages over chat button
     */
    private BadgeDrawable chatButtonBadge;

    /**
     * Keep a flag to tell whether seat selected or not.<br/>
     * We need this flag to know if there is a need to display seat selection animations or not.
     * After user selects a seat, we hide the seats selection animations.
     */
    private static boolean isSeatSelected = false;

    /**
     * In case our player is the creator of a game, we show a start button on the board, to let
     * the creator to start a game after waiting for players to join.
     */
    private AppCompatButton startGameButton;

    /**
     * Hold a reference to the active player, so once its turn ended, we will hide his progressbar.
     */
    private Player lastActivePlayer;

    /**
     * Hold a handler as a data member, cause it might be garbage collected before executing tasks that we submit.<br/>
     * When handler refers to null, it means the view is destroyed, and a post action should be discarded.
     */
    private Handler handler;

    /**
     * Keep this flag so we will not flicker highlights during refresh. We want to highlight once and keep it
     * constant without flickers. So we use this flag to exit for {@link #highlightWinnersIfNecessary(GameEngine)}
     * if cards were already highlighted.
     */
    private boolean isHighlighted = false;

    /**
     * Constructs a new {@link GameFragment}
     */
    public GameFragment() {
        super(R.layout.fragment_game, FragmentGameBinding::bind);
        game = Game.getInstance();
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        handler = new Handler(Looper.myLooper());
        cardsResource = new Cards();

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

            if (game.isActive() || isSeatSelected) {
                player.seatSelection.setVisibility(View.GONE);
            } else {
                player.seatSelection.setVisibility(View.VISIBLE);
                player.seatSelection.setOnClickListener(v -> {
                    isSeatSelected = true;
                    players.values().forEach(playerView -> playerView.seatSelection.setVisibility(View.GONE));
                    game.getThisPlayer().setPosition(pos);
                    game.joinGame(p -> {
                        Toast.makeText(GameFragment.this.getContext(), "Selected seat is: " + (p.getPosition() + 1), Toast.LENGTH_LONG).show();
                        ((MainActivity)getActivity()).refreshGameMenuVisibility(); // Should be visible now
                    });
                });
            }
        });

        // Register event listeners
        getBinding().buttonCheck.setOnClickListener(this::onCheckClicked);
        getBinding().buttonRaise.setOnClickListener(this::onRaiseClicked);
        getBinding().buttonFold.setOnClickListener(this::onFoldClicked);
        getBinding().buttonLog.setOnClickListener(this::onGameLogClicked);
        getBinding().buttonQuit.setClickable(true);
        getBinding().buttonQuit.setOnClickListener(this::onQuitButtonClicked);

        // Use badge drawable to show amount of new incoming messages over chat button
        getBinding().buttonChat.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                chatButtonBadge = BadgeDrawable.create(GameFragment.this.getContext());
                chatButtonBadge.setNumber(0);
                chatButtonBadge.setBackgroundColor(getContext().getColor(R.color.red_button_start_color));
                chatButtonBadge.setBadgeTextColor(getContext().getColor(R.color.white));
                chatButtonBadge.setVerticalOffset(20);
                chatButtonBadge.setHorizontalOffset(15);

                BadgeUtils.attachBadgeDrawable(chatButtonBadge, getBinding().buttonChat, getBinding().buttonChatLayout);
                chatButtonBadge.setVisible(false);
                getBinding().buttonChat.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        // Run Game service in background, to play sound effects based on game steps
        getActivity().startService(new Intent(getActivity(), GameSoundService.class));

        // Listen to game and chat updates, now that we have all data members set
        game.addGameListener(this);
        if (game.getGameHash() != null) {
            game.getChat().addChatListener(this);
            getBinding().buttonChat.setVisibility(View.VISIBLE);
            getBinding().buttonChat.setOnClickListener(this::onChatClicked);
        } else {
            getBinding().buttonChat.setVisibility(View.GONE);
        }

        // Display the game hash so players can communicate with their friends
        getBinding().gameHash.setText(String.format(getString(R.string.game_id), game.getGameHash()));
        getBinding().winAnimation.setVisibility(View.INVISIBLE);

        // Refresh the view based on current game engine.
        // In case there is no game engine yet, it means we have just entered. Then select a seat
        GameEngine gameEngine = game.getGameEngine();
        if (gameEngine != null) {
            // In case fragment was recreated during a game, mark seat selection as done.
            isSeatSelected = true;
            ((MainActivity)getActivity()).refreshGameMenuVisibility(); // Should be visible now
            refresh(gameEngine);
        } else if (game.isJoinedGame()) {
            isSeatSelected = true;
            ((MainActivity)getActivity()).refreshGameMenuVisibility(); // Should be visible now
            playersRefresh(game.getPlayers());
        } else {
            // It might be that player quited the app without logout of game.
            // Try to detect such a case now, and just skip seat selection if needed
            game.ifPlayerPartOfGame(((MainActivity)getActivity()).getUser().getId(), gameEngineInner -> {
                if (gameEngineInner != null) {
                    isSeatSelected = true;
                    ((MainActivity)getActivity()).refreshGameMenuVisibility(); // Should be visible now
                    playersRefresh(gameEngineInner.getPlayers().getPlayers());
                } else {
                    playersRefresh(game.getPlayers());
                }
            });
        }

        // Show START button in case current player is the organizer, or show board in case
        // fragment was recreated.
        updateBoardVisibility();
    }

    @Override
    public void onDestroyView() {
        game.removeGameStepListener(this);
        game.getChat().removeChatListener(this);

        chatButtonBadge = null;
        handler = null;
        startGameButton = null;

        super.onDestroyView();
    }

    @Override
    public void onStep(GameEngine gameEngine, Game.GameStepType step) {
        Log.d(LOGGER, "Game Step: " + step);

        if (handler != null) {
            handler.post(() -> {
               if (handler != null) {
                   if (step == Game.GameStepType.FLIP_CARD) {
                       updateBoardVisibility();
                   } else {
                       try {
                           switch (step) {
                               case CALL:
                               case RAISE:
                               case ALL_IN:
                                   getBinding().winAnimation.setVisibility(View.INVISIBLE);
                                   getBinding().buttonCheck.setText(R.string.action_call);
                                   break;
                               case WIN:
                                   getBinding().winAnimation.setVisibility(View.VISIBLE);
                                   getBinding().buttonCheck.setText(R.string.action_check);
                                   break;
                               default:
                                   getBinding().winAnimation.setVisibility(View.INVISIBLE);
                                   getBinding().buttonCheck.setText(R.string.action_check);
                           }
                       } catch (Throwable t) {
                           Log.e(LOGGER, "Error during onStep", t);
                       }
                   }
               }
            });
        }
    }

    // Here we draw the game based on game info.
    @Override
    public void refresh(GameEngine gameEngine) {
        if (handler != null) {
            handler.post(() -> {
                if (handler != null) {
                    try {
                        // When players exit game by killing the app, we get into illegal state. Do quit.
                        Player currentPlayer = gameEngine.getPlayers().getCurrentPlayer();
                        if (currentPlayer == null) {
                            doQuitGame();
                            return;
                        }

                        updateBoardVisibility();
                        updateHandsAndDealer(gameEngine);
                        updateProgressBar(gameEngine);

                        // Update our hand. (this player is the signed in user on this device)
                        Player thisPlayer = gameEngine.getPlayers().getPlayerById(game.getThisPlayer().getId());
                        PlayerViewAccessor thisPlayerView = players.get(thisPlayer.getPosition());
                        Hand myHand = thisPlayer.getHand();
                        revealHand(thisPlayerView.handView, myHand);

                        // Update action buttons
                        boolean isActionButtonEnabled = thisPlayer.equals(gameEngine.getPlayers().getCurrentPlayer());
                        getBinding().buttonRaise.setEnabled(isActionButtonEnabled);
                        getBinding().buttonCheck.setEnabled(isActionButtonEnabled);
                        getBinding().buttonFold.setEnabled(isActionButtonEnabled);

                        // Draw winners indication if necessary
                        highlightWinnersIfNecessary(gameEngine);
                    } catch (Throwable t) {
                        Log.e(LOGGER, "Error during refresh", t);
                    }
                }
            });
        }
    }

    /**
     * Helper method to show/hide hands based on involved players in a game.<br/>
     * In addition, show the dealer.
     * @param gameEngine Game engine to get details from
     */
    private void updateHandsAndDealer(GameEngine gameEngine) {
        Set<Player> involvedPlayers = gameEngine.getPlayers().getInvolvedPlayers();
        Player dealer = gameEngine.getDealer();
        for (Map.Entry<Integer, PlayerViewAccessor> currPlayer : players.entrySet()) {
            // If player left the game, clear its view.
            if ((currPlayer.getValue().player == null) || (gameEngine.getPlayers().getPlayerById(currPlayer.getValue().player.getId()) == null)) {
                currPlayer.getValue().hide();
            } else {
                // Show or hide player's hand, based on player activity in the game
                if ((currPlayer.getValue().player == null) || !involvedPlayers.contains(currPlayer.getValue().player)) {
                    currPlayer.getValue().handView.setVisibility(View.INVISIBLE);
                } else {
                    // Make sure we hide other player hands. (After their won for example)
                    // Do this only in case we do not show the winner
                    if ((gameEngine.getPlayerToEarnings() == null) && !currPlayer.getValue().player.getId().equals(game.getThisPlayer().getId())) {
                        currPlayer.getValue().handView.getFirstCardView().getCardImageView().setImageResource(R.drawable.pack);
                        currPlayer.getValue().handView.getSecondCardView().getCardImageView().setImageResource(R.drawable.pack);
                    }

                    currPlayer.getValue().handView.setVisibility(View.VISIBLE);
                }

                // Update dealer image
                if (dealer != null) {
                    boolean isDealer = dealer.equals(currPlayer.getValue().player);
                    currPlayer.getValue().handView.getDealerImageView().setVisibility(isDealer ? View.VISIBLE : View.INVISIBLE);
                }

                // Update amount of chips
                Player player = gameEngine.getPlayers().getPlayer(currPlayer.getKey());
                if (player != null) {
                    currPlayer.getValue().playerView.getPlayerChipsTextView().setText(player.getChips().getFormatted());

                    // Update player's bet
                    long playerPot = gameEngine.getPot().getPotOfPlayer(player);
                    currPlayer.getValue().setBet(playerPot);
                }
            }
        }
    }

    /**
     * Helper method to hide last player's progress bar and show the progress of current player, based on how
     * much time passed since his turn started.
     * @param gameEngine Game engine to get details from
     */
    private void updateProgressBar(GameEngine gameEngine) {
        Player currentPlayer = gameEngine.getPlayers().getCurrentPlayer();
        PlayerViewAccessor playerViewAccessor = players.get(currentPlayer.getPosition());

        // Hide last progress bar and reset it back to 60, when we detect a player switch.
        int totalProgress = (int)TimeUnit.MILLISECONDS.toSeconds(gameEngine.getGameSettings().getTurnTime());
        if (lastActivePlayer != null) {
            if (!currentPlayer.getId().equals(lastActivePlayer.getId())) {
                Log.d(LOGGER, "Turn moved to another player. Was: " + lastActivePlayer.getName() + ", and now: " + currentPlayer.getName());
                ProgressBar playerProgressBar = players.get(lastActivePlayer.getPosition()).playerView.getPlayerProgressBar();
                playerProgressBar.setVisibility(View.INVISIBLE);
                playerProgressBar.setProgress(totalProgress);
            }
        }

        lastActivePlayer = currentPlayer;

        long timePassed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - gameEngine.getPlayerTurnTimer().getTurnStartTime());
        ProgressBar playerProgressBar = playerViewAccessor.playerView.getPlayerProgressBar();
        playerProgressBar.setMax(totalProgress);
        playerProgressBar.setMin(0);
        playerProgressBar.setVisibility(View.VISIBLE);
        playerProgressBar.setProgress((int) Math.abs(totalProgress - timePassed), true);
    }

    /**
     * Helper method to highlight winners hand, such that it will be clear who won and with what hand.<br/>
     * We leave the winner hand un-highlighted so it will be clear, and we highlight with black overlay those that we
     * want to hide.
     * @param gameEngine Game engine to get details from
     */
    private void highlightWinnersIfNecessary(GameEngine gameEngine) {
        Map<String, Pot.PlayerWinning> playerToEarnings = gameEngine.getPlayerToEarnings();
        if (playerToEarnings == null) {
            if (isHighlighted) {
                Log.d(LOGGER, "Clearing card highlights");
                isHighlighted = false;

                // Remove any highlighting
                getBinding().card0.clearHighlight();
                getBinding().card1.clearHighlight();
                getBinding().card2.clearHighlight();
                getBinding().card3.clearHighlight();
                getBinding().card4.clearHighlight();
                for (PlayerViewAccessor currPlayerView : players.values()) {
                    if ((currPlayerView.playerView.getVisibility() == View.VISIBLE) &&
                            (currPlayerView.handView.getFirstCardView().getVisibility() == View.VISIBLE)) {
                        currPlayerView.handView.getFirstCardView().clearHighlight();
                        currPlayerView.handView.getSecondCardView().clearHighlight();
                    }
                }
            }
        } else if (!isHighlighted) {
            Log.d(LOGGER, "Drawing card highlights");
            isHighlighted = true;

            // Map cards to their corresponding view, so we can highlight the selected cards of a winner
            Map<Card, CardView> cardsToTheirView = new HashMap<>();
            Board board = game.getGameEngine().getBoard();
            board.getFlop1().ifPresent(card -> cardsToTheirView.put(card, getBinding().card0));
            board.getFlop2().ifPresent(card -> cardsToTheirView.put(card, getBinding().card1));
            board.getFlop3().ifPresent(card -> cardsToTheirView.put(card, getBinding().card2));
            board.getTurn().ifPresent(card -> cardsToTheirView.put(card, getBinding().card3));
            board.getRiver().ifPresent(card -> cardsToTheirView.put(card, getBinding().card4));

            // Reveal winners hand and highlight it
            for (Map.Entry<String, Pot.PlayerWinning> currPlayer : playerToEarnings.entrySet()) {
                PlayerViewAccessor currPlayerView = players.get(gameEngine.getPlayers().getPlayerById(currPlayer.getKey()).getPosition());

                // Reveal winner's hand
                Hand currPlayerHand = currPlayer.getValue().getHandRank().getHand();
                revealHand(currPlayerView.handView, currPlayerHand);
                cardsToTheirView.put(currPlayerHand.getCardAt(0).get(), currPlayerView.handView.getFirstCardView());
                cardsToTheirView.put(currPlayerHand.getCardAt(1).get(), currPlayerView.handView.getSecondCardView());

                Card[] selectedCards = currPlayer.getValue().getHandRank().getSelectedCards();
                for (Card selectedCard : selectedCards) {
                    CardView cardView = cardsToTheirView.get(selectedCard);
                    if (cardView != null) {
                        cardView.highlight();
                    }
                }
            }
        }
    }

    /**
     * Show the hand of a player. Set the image resource of cards from hand, into the image views of a hand view.
     * @param handView Hand view to set images to
     * @param hand Hand to know what cards to set
     */
    private void revealHand(HandView handView, Hand hand) {
        hand.getCardAt(0).ifPresent(card -> handView.getFirstCardView().getCardImageView().setImageResource(cardsResource.cardToResource.get(card)));
        hand.getCardAt(1).ifPresent(card -> handView.getSecondCardView().getCardImageView().setImageResource(cardsResource.cardToResource.get(card)));
    }

    @Override
    public void playersRefresh(Set<Player> players) {
        if (handler != null) {
            Set<Player> playersToWorkWith = players == null ? new HashSet<>() : players;
            handler.post(() -> {
                if (handler != null) {
                    try {
                        // Keep available seats
                        Set<Integer> availableSeats = new HashSet<>(SEATS);
                        playersToWorkWith.forEach(p -> {
                            availableSeats.remove(p.getPosition());
                            PlayerViewAccessor playerViewAccessor = this.players.get(p.getPosition());
                            refreshPlayerViewFromPlayer(playerViewAccessor, p);
                        });

                        Log.d(LOGGER, "Players refresh (hiding win animation)");
                        getBinding().winAnimation.setVisibility(View.INVISIBLE);

                        // Make sure animations are visible
                        if (!isSeatSelected) {
                            availableSeats.forEach(seat -> {
                                PlayerViewAccessor playerViewAccessor = GameFragment.this.players.get(seat);
                                playerViewAccessor.seatSelection.setVisibility(View.VISIBLE);
                            });
                        }
                    } catch (Throwable t) {
                        Log.e(LOGGER, "Error while refreshing players", t);
                    }
                }
            });
        }
    }

    /**
     * Hide the cards from board, and show a START button in case current player is the organizer.
     */
    private void updateBoardVisibility() {
        if (!game.isActive()) {
            getBinding().card0.setVisibility(View.INVISIBLE);
            getBinding().card1.setVisibility(View.INVISIBLE);
            getBinding().card2.setVisibility(View.INVISIBLE);
            getBinding().card3.setVisibility(View.INVISIBLE);
            getBinding().card4.setVisibility(View.INVISIBLE);
            getBinding().potAmount.setText(R.string.zero);
            getBinding().potAmount.setVisibility(View.INVISIBLE);

            if (startGameButton == null) {
                game.ifThisPlayerIsTheOwner(() -> {
                    if (startGameButton == null) {
                        startGameButton = new AppCompatButton(new ContextThemeWrapper(getContext(), R.style.ButtonGreenStyle), null, 0);
                        startGameButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        startGameButton.setText(R.string.action_start);
                        startGameButton.setGravity(Gravity.CENTER);
                        startGameButton.setOnClickListener(v -> {
                            game.start(errorMessage -> {
                                if (errorMessage.isEmpty()) {
                                    startGameButton.setVisibility(View.GONE);
                                    getBinding().cardsContainer.removeView(startGameButton);
                                } else {
                                    Snackbar.make(GameFragment.this.getView(), errorMessage, Snackbar.LENGTH_LONG).show();
                                }
                            });
                        });
                        getBinding().cardsContainer.addView(startGameButton);
                    }
                });
            }
        } else {
            // Update board
            Board board = game.getGameEngine().getBoard();
            updateCard(getBinding().card0, board.getFlop1().orElse(null));
            updateCard(getBinding().card1, board.getFlop2().orElse(null));
            updateCard(getBinding().card2, board.getFlop3().orElse(null));
            updateCard(getBinding().card3, board.getTurn().orElse(null));
            updateCard(getBinding().card4, board.getRiver().orElse(null));

            // Update pot
            getBinding().potAmount.setText(new Chips(game.getGameEngine().getPot().sum()).toShorthand());
            getBinding().potAmount.setVisibility(View.VISIBLE);

            // Switch button text based on last action
            PlayerActionKind lastActionKind = game.getGameEngine().getLastActionKind();
            if (lastActionKind == PlayerActionKind.RAISE) {
                getBinding().buttonCheck.setText(R.string.action_call);
            } else if (lastActionKind == PlayerActionKind.CALL) {
                Pot pot = game.getGameEngine().getPot();

                // If for some reason there is no pot, just show call after call.
                if (pot == null) {
                    getBinding().buttonCheck.setText(R.string.action_call);
                } else {
                    // Check if our player is the one who started a bet, and the last call was equal to
                    // our player's bet. If so, out player should see "check" and not "call".
                    long potOfPlayer = pot.getPotOfPlayer(game.getThisPlayer());
                    if (potOfPlayer == pot.getLastBet()) {
                        getBinding().buttonCheck.setText(R.string.action_check);
                    } else {
                        getBinding().buttonCheck.setText(R.string.action_call);
                    }
                }
            } else {
                getBinding().buttonCheck.setText(R.string.action_check);
            }
        }
    }

    /**
     * Updates a single card view. Sets it as visible, ad assign the resource identifier of the
     * cards image representing the specified card
     * @param cardView Card view to update
     * @param card The card to show
     */
    private void updateCard(CardView cardView, Card card) {
        if (card == null) {
            cardView.setVisibility(View.INVISIBLE);
        } else {
            handler.postDelayed(() -> {
                cardView.setVisibility(View.VISIBLE);
                cardView.getCardImageView().setImageResource(cardsResource.cardToResource.getOrDefault(card, R.drawable.pack));
            }, 150);
        }
    }

    /**
     * Refresh a player view widgets based on data of a {@link Player}
     * @param playerView The player view accessor to update its widgets
     * @param player The player to get data from
     */
    private void refreshPlayerViewFromPlayer(PlayerViewAccessor playerView, Player player) {
        playerView.player = player;
        playerView.show();
        playerView.seatSelection.setVisibility(View.GONE);
        playerView.handView.getDealerImageView().setVisibility(View.INVISIBLE);
        playerView.handView.setVisibility(View.INVISIBLE);
        playerView.playerView.getPlayerProgressBar().setVisibility(View.INVISIBLE);
        playerView.playerView.getPlayerImageView().setVisibility(View.VISIBLE);
        playerView.playerView.getPlayerNameTextView().setVisibility(View.VISIBLE);
        playerView.playerView.getPlayerChipsTextView().setVisibility(View.VISIBLE);
        playerView.resetBet();

        String playerLocalName = playerView.playerView.getPlayerNameTextView().getText().toString();
        playerView.playerView.getPlayerChipsTextView().setText(player.getChips().getFormatted());
        playerView.playerView.getPlayerNameTextView().setText(player.getName());

        // Now, before messing with image, make sure local data is different to save time.
        if (!player.getName().equals(playerLocalName)) {
            TexasHoldemWebService.getInstance().getUserService().getUserInfo(player.getId()).enqueue(new UserInfoCallback(playerView));
        }
    }

    @Override
    public void onGameError(String errorMessage) {
        Log.e(LOGGER, "Game Error: " + errorMessage);
        if (handler != null) {
            handler.post(() -> {
                if (handler != null) {
                    Snackbar.make(getView(), errorMessage, BaseTransientBottomBar.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onMessageArrived(Message message) {
        Log.d(LOGGER, "Message arrived: " + message);

        // Do nothing in case view was destroyed
        if ((chatButtonBadge != null) && (handler != null)) {
            handler.post(() -> {
                // Check again cause it runs after our previous check, not immediately.
                if ((chatButtonBadge != null) && (handler != null)) {
                    int currNewMessagesCount = chatButtonBadge.getNumber();

                    if (currNewMessagesCount >= 0) {
                        chatButtonBadge.setVisible(true);
                    }

                    chatButtonBadge.setNumber(currNewMessagesCount + 1);
                }
            });
        }
    }

    @Override
    public void onChatError(String errorMessage) {
        Log.e(LOGGER, "Chat Error: " + errorMessage);
    }

    /**
     * Occurs when user presses the CHECK button, to apply check action if possible
     * @param checkButton The CHECK button
     */
    void onCheckClicked(@SuppressWarnings("unused") View checkButton) {
        try {
            Log.d(LOGGER, "Check/Call");
            PlayerActionKind actionKind = getBinding().buttonCheck.getText().toString().equalsIgnoreCase("check") ? PlayerActionKind.CHECK : PlayerActionKind.CALL;
            game.executePlayerAction(PlayerAction.builder().name(game.getThisPlayer().getName()).actionKind(actionKind).build());
        } catch (Throwable t) {
            Log.e(LOGGER, "Unexpected error", t);
        }
    }

    /**
     * Occurs when user presses the RAISE button, to raise if possible
     * @param raiseButton The RAISE button
     */
    void onRaiseClicked(@SuppressWarnings("unused") View raiseButton) {
        try {
            Log.d(LOGGER, "Raise");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getContext(), R.style.AlertDialogTheme_Light);

            // Get dialog_purchase.xml view
            LayoutInflater li = LayoutInflater.from(this.getContext());
            View promptsView = li.inflate(R.layout.dialog_purchase, null);
            alertDialogBuilder.setView(promptsView);

            ((TextView)promptsView.findViewById(R.id.textViewDialog)).setText(R.string.raise_with);
            EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);

            // Define dialog buttons and respond to user clicks over them
            alertDialogBuilder
                    .setCancelable(true)
                    .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel())
                    .setPositiveButton("Raise", (dialog, id) -> {
                        long amountOfChips = 0;
                        String userInputText = userInput.getText().toString().trim();

                        Log.d(LOGGER, "Raising with " + userInputText + " chips.");
                        try {
                            amountOfChips = Long.parseLong(userInputText);
                        } catch (NumberFormatException e) {
                            Toast.makeText(GameFragment.this.getContext(), "Illegal input: " + userInputText, Toast.LENGTH_LONG).show();
                            dialog.cancel();
                        }

                        // Execute the action
                        if (amountOfChips > 0) {
                            game.executePlayerAction(PlayerAction.builder().name(game.getThisPlayer().getName()).actionKind(PlayerActionKind.RAISE).chips(new Chips(amountOfChips)).build());
                        }
                    });

            // Create and show alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } catch (Throwable t) {
            Log.e(LOGGER, "Unexpected error", t);
        }
    }

    /**
     * Occurs when user presses the FOLD button, to fold if possible
     * @param foldButton The FOLD button
     */
    void onFoldClicked(@SuppressWarnings("unused") View foldButton) {
        try {
            Log.d(LOGGER, "Fold");
            game.executePlayerAction(PlayerAction.builder().name(game.getThisPlayer().getName()).actionKind(PlayerActionKind.FOLD).build());
        } catch (Throwable t) {
            Log.e(LOGGER, "Unexpected error", t);
        }
    }

    private void onQuitButtonClicked(View quitButton) {
        try {
            GameEngine gameEngine = Game.getInstance().getGameEngine();
            if (gameEngine != null) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getContext(), R.style.AlertDialogTheme_Light);

                // Get dialog_purchase.xml view
                LayoutInflater li = LayoutInflater.from(this.getContext());
                View promptsView = li.inflate(R.layout.dialog_game_log, null);
                alertDialogBuilder.setView(promptsView);

                ListView listView = promptsView.findViewById(R.id.listViewPlayerActions);

                // Get string representation of all player actions
                List<String> message = new ArrayList<>(1);
                message.add("Are you sure you want to leave?");
                listView.setAdapter(new ArrayAdapter<>(this.getContext(), R.layout.card_view_log, message));

                // Define dialog buttons and respond to user clicks over them
                alertDialogBuilder
                        .setCancelable(true)
                        .setNegativeButton("No", (dialog, id) -> dialog.cancel())
                        .setPositiveButton("Yes", (dialog, id) -> doQuitGame());

                // Create and show alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else {
                doQuitGame();
            }
        } catch (Throwable t) {
            Log.e(LOGGER, "Unexpected error", t);
        }
    }

    private void doQuitGame() {
        isSeatSelected = false;
        game.stop(() -> ((MainActivity)getActivity()).refreshGameMenuVisibility());
        ((MainActivity)getActivity()).navigateToFragment(R.id.nav_home);
    }

    /**
     * Occurs when user presses the CHAT button, to open chat fragment
     * @param chatButton The CHAT button
     */
    void onChatClicked(@SuppressWarnings("unused") View chatButton) {
        try {
            chatButtonBadge.setNumber(0);
            chatButtonBadge.setVisible(false);

            ((MainActivity)getActivity()).navigateToFragment(R.id.nav_chat);
        } catch (Throwable t) {
            Log.e(LOGGER, "Unexpected error", t);
        }
    }

    /**
     * Occurs when user presses the LOG button, to open game log dialog
     * @param gameLogButton The LOG button
     */
    void onGameLogClicked(@SuppressWarnings("unused") View gameLogButton) {
        try {
            GameEngine gameEngine = Game.getInstance().getGameEngine();
            if (gameEngine != null) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getContext(), R.style.AlertDialogTheme_Light);

                // Get dialog_purchase.xml view
                LayoutInflater li = LayoutInflater.from(this.getContext());
                View promptsView = li.inflate(R.layout.dialog_game_log, null);
                alertDialogBuilder.setView(promptsView);

                ListView listView = promptsView.findViewById(R.id.listViewPlayerActions);

                // Get string representation of all player actions
                List<String> playerActions = new ArrayList<>(gameEngine.getGameLog().getLastRoundPlayerActions().size() +
                        gameEngine.getGameLog().getPlayerActions().size() + 1);
                gameEngine.getGameLog().getLastRoundPlayerActions().stream().map(PlayerAction::toString).forEach(playerActions::add);
                playerActions.add("------------------------------------"); // Line separator.
                gameEngine.getGameLog().getPlayerActions().stream().map(PlayerAction::toString).forEach(playerActions::add);

                listView.setAdapter(new ArrayAdapter<>(this.getContext(), R.layout.card_view_log, playerActions));

                // Scroll to last row
                listView.setSelection(playerActions.size() - 1);

                // Define dialog buttons and respond to user clicks over them
                alertDialogBuilder
                        .setCancelable(true)
                        .setPositiveButton("CLOSE", (dialog, id) -> dialog.cancel());

                // Create and show alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        } catch (Throwable t) {
            Log.e(LOGGER, "Unexpected error", t);
        }
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
         * Keep the real value of a bet, so we can add more bets to this value.
         */
        long bet;

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

        /**
         * Adds a bet to existing bet
         * @param bet The amount of chips to add
         */
        void addBet(long bet) {
            this.bet += bet;
            refreshBet();
        }

        /**
         * Adds a bet to existing bet
         * @param bet The amount of chips to add
         */
        void setBet(long bet) {
            this.bet = bet;
            refreshBet();
        }

        void refreshBet() {
            playerBet.setText(new Chips(this.bet).toShorthand());
            playerBet.setVisibility(this.bet > 0 ? View.VISIBLE: View.INVISIBLE);
        }

        void resetBet() {
            this.bet = 0;
            playerBet.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * This class created to handle User info responses from server.<br/>
     * The server returns the user model when we ask for user info by identifier, so we can display
     * player images.
     */
    private static class UserInfoCallback extends SimpleCallback<JsonNode> {
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
                } catch (Exception e) {
                    Log.e(LOGGER, "Failed parsing response. Response was: " + body, e);
                }
            }
        }
    }

    /**
     * Helper class to get card resource identifier by {@link Card} model.<br/>
     * Useful for drawing cards based on player hands or board.
     */
    private class Cards {
        /**
         * A static map containing the resource identifier of all cards in the game.<br/>
         * This helps us get a card resource identifier by {@link Card} model in O(1), dynamically.
         */
        final Map<Card, Integer> cardToResource = new HashMap<>();

        Cards() {
            try {
                for (Card.CardSuit currCardSuit : Card.CardSuit.values()) {
                    for (Card.CardRank currCardRank : Card.CardRank.values()) {
                        int currCardRes;
                        String drawableToFind = currCardSuit.name().toLowerCase();

                        // Ace's ordinal value is the highest. So use 1 instead.
                        if (currCardRank == Card.CardRank.ACE) {
                            drawableToFind += "1";
                        } else {
                            // 2's ordinal value = 1, 3's ordinal is 2. Hence the plus one.
                            drawableToFind += currCardRank.ordinal() + 1;
                        }

                        // Now get the static value. e.g. R.drawable.club1
                        //currCardRes = getContext().getResources().getIdentifier(drawableToFind, "drawable", getContext().getPackageName());
                        currCardRes = R.drawable.class.getDeclaredField(drawableToFind).getInt(null);

                        cardToResource.put(new Card(currCardRank, currCardSuit), currCardRes);
                    }
                }
            } catch (Exception e) {
                Log.e(LOGGER, "Unable to find card's resource identifier.", e);
            }
        }
    }
}