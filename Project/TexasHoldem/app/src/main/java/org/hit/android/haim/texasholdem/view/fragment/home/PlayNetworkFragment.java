package org.hit.android.haim.texasholdem.view.fragment.home;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.databinding.FragmentPlayNetworkBinding;
import org.hit.android.haim.texasholdem.model.game.ClientGameSettings;
import org.hit.android.haim.texasholdem.model.game.Game;
import org.hit.android.haim.texasholdem.view.activity.MainActivity;
import org.hit.android.haim.texasholdem.view.fragment.ViewBindedFragment;
import org.hit.android.haim.texasholdem.web.SimpleCallback;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;

import retrofit2.Call;
import retrofit2.Response;

/**
 * This fragment lets the user to select whether he wants to join an existing game,
 * or create a new one to share with his friends.
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class PlayNetworkFragment extends ViewBindedFragment<FragmentPlayNetworkBinding> {
    private static final int HTTP_NOT_FOUND = 404;

    public PlayNetworkFragment() {
        super(R.layout.fragment_play_network, FragmentPlayNetworkBinding::bind);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getBinding().buttonJoinGame.setOnClickListener(this::onJoinButtonClicked);
        getBinding().buttonCreateGame.setOnClickListener(this::onCreateButtonClicked);
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
                    if (response.code() == HTTP_NOT_FOUND) {
                        getBinding().editTextGameHash.getEdit().setError("Not Found");
                    } else {
                        Game.getInstance().start(ClientGameSettings.builder().gameHash(gameHash).chips(chipsAmount).build(),
                                                ((MainActivity)getActivity()).getUser());
                        ((MainActivity)PlayNetworkFragment.this.getActivity()).navigateToFragment(R.id.nav_game);
                    }
                }
            });
        }
    }

    /**
     * Occurs when user clicks the create button, to create a new network game
     * @param button The create button
     */
    public void onCreateButtonClicked(View button) {

    }

}