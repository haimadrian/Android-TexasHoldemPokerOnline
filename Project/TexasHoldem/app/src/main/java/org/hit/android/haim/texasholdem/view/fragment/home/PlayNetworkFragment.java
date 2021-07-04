package org.hit.android.haim.texasholdem.view.fragment.home;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.databinding.FragmentPlayNetworkBinding;
import org.hit.android.haim.texasholdem.model.game.ClientGameSettings;
import org.hit.android.haim.texasholdem.model.game.Game;
import org.hit.android.haim.texasholdem.view.activity.MainActivity;
import org.hit.android.haim.texasholdem.view.fragment.ViewBindedFragment;
import org.hit.android.haim.texasholdem.web.HttpStatus;
import org.hit.android.haim.texasholdem.web.SimpleCallback;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

/**
 * This fragment lets the user to select whether he wants to join an existing game,
 * or create a new one to share with his friends.
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class PlayNetworkFragment extends ViewBindedFragment<FragmentPlayNetworkBinding> {
    private static final String LOGGER = PlayNetworkFragment.class.getSimpleName();

    public PlayNetworkFragment() {
        super(R.layout.fragment_play_network, FragmentPlayNetworkBinding::bind);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getBinding().buttonJoinGame.setOnClickListener(this::onJoinButtonClicked);
        getBinding().buttonCreateGame.setOnClickListener(this::onCreateButtonClicked);

        getBinding().editTextTurnTimeout.getEdit().setText(R.string.default_timeout);
    }

    /**
     * Occurs when user clicks the join button, to join an existing network game
     * @param button The join button
     */
    public void onJoinButtonClicked(View button) {
        String gameHash = getBinding().editTextGameHash.getText();
        long chipsAmount = getBinding().editTextChipsCountJoin.getLong(-1);

        if (chipsAmount <= 0) {
            getBinding().editTextChipsCountJoin.getEdit().setError("Must be positive");
        } else if (chipsAmount > ((MainActivity)getActivity()).getUser().getCoins()) {
            getBinding().editTextChipsCountJoin.getEdit().setError("Too many chips");
            Toast.makeText(getContext(), "Insufficient chips. Please purchase.", Toast.LENGTH_LONG).show();
        } else if (gameHash.isEmpty()) {
            getBinding().editTextGameHash.getEdit().setError("Missing");
        } else {
            // Make sure game exists before navigating to GameFragment
            TexasHoldemWebService.getInstance().getGameService().getGameInfo(gameHash).enqueue(new SimpleCallback<JsonNode>() {
                @Override
                public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
                    if (response.code() == HttpStatus.NOT_FOUND.getCode()) {
                        getBinding().editTextGameHash.getEdit().setError("Not Found");
                        return;
                    } else if (response.code() == HttpStatus.BAD_REQUEST.getCode()) {
                        String error = TexasHoldemWebService.getInstance().readHttpErrorResponse(response);

                        // Of course the user is not part of the game. We want to join this game..
                        // Hence if this is the error, just continue. (Game exists)
                        if (!error.toLowerCase().contains("is not part of this game")) {
                            getBinding().editTextGameHash.getEdit().setError(error);
                            return;
                        }
                    }

                    ClientGameSettings gameSettings = new ClientGameSettings(chipsAmount, 0, gameHash);
                    gameSettings.setNetwork(true);

                    Game.getInstance().init(gameSettings, ((MainActivity)getActivity()).getUser());
                    ((MainActivity)PlayNetworkFragment.this.getActivity()).navigateToFragment(R.id.nav_game);
                }
            });
        }
    }

    /**
     * Occurs when user clicks the create button, to create a new network game
     * @param button The create button
     */
    public void onCreateButtonClicked(View button) {
        long chipsAmount = getBinding().editTextChipsCountCreate.getLong(-1);
        long smallBet = getBinding().editTextSmallBet.getLong(-1);
        long bigBet = getBinding().editTextBigBet.getLong(-1);

        if (chipsAmount <= 0) {
            getBinding().editTextChipsCountCreate.getEdit().setError("Must be positive");
        } else if (chipsAmount > ((MainActivity)getActivity()).getUser().getCoins()) {
            getBinding().editTextChipsCountCreate.getEdit().setError("Too many chips");
            Toast.makeText(getContext(), "Insufficient chips. Please purchase.", Toast.LENGTH_LONG).show();
        } else if (smallBet <= 0) {
            getBinding().editTextSmallBet.getEdit().setError("Minimum value is 1");
        } else if (bigBet <= smallBet) {
            getBinding().editTextBigBet.getEdit().setError("Must be bigger than small bet");
        } else {
            // Make sure game exists before navigating to GameFragment
            long turnTimeout = getBinding().editTextTurnTimeout.getLong(60);
            ClientGameSettings gameSettings = new ClientGameSettings(chipsAmount, 0, null);
            gameSettings.setSmallBet(smallBet);
            gameSettings.setBigBet(bigBet);
            gameSettings.setTurnTime(TimeUnit.SECONDS.toMillis(turnTimeout));
            TexasHoldemWebService.getInstance().getGameService().createGame(gameSettings).enqueue(new SimpleCallback<JsonNode>() {
                @Override
                public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
                    JsonNode body = response.body();
                    try {
                        String gameHash = TexasHoldemWebService.getInstance().getObjectMapper().readValue(body.toString(), TextNode.class).asText();
                        Log.d(LOGGER, "Received game hash: " + gameHash);

                        gameSettings.setGameHash(gameHash);
                        gameSettings.setNetwork(true);
                        Game.getInstance().init(gameSettings, ((MainActivity)getActivity()).getUser());
                        ((MainActivity)PlayNetworkFragment.this.getActivity()).navigateToFragment(R.id.nav_game);
                    } catch (Exception e) {
                        Log.e(LOGGER, "Failed parsing response. Response was: " + body, e);
                    }
                }
            });
        }
    }

}