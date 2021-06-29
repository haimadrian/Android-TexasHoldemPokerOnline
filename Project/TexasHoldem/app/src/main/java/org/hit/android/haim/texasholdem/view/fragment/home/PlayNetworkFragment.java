package org.hit.android.haim.texasholdem.view.fragment.home;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.databinding.FragmentPlayNetworkBinding;
import org.hit.android.haim.texasholdem.view.activity.MainActivity;
import org.hit.android.haim.texasholdem.view.fragment.ViewBindedFragment;

/**
 * This fragment lets the user to select whether he wants to join an existing game,
 * or create a new one to share with his friends.
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class PlayNetworkFragment extends ViewBindedFragment<FragmentPlayNetworkBinding> {

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

        boolean isValid = true;
        if (gameHash.isEmpty()) {
            isValid = false;
            getBinding().editTextGameHash.getEdit().setError("Missing");
        }

        if (chipsAmount <= 0) {
            isValid = false;
            getBinding().editTextChipsCountJoin.getEdit().setError("Must be positive");
        } else if (chipsAmount > ((MainActivity)getActivity()).getUser().getCoins()) {
            isValid = false;
            getBinding().editTextChipsCountJoin.getEdit().setError("Too many chips");
            Toast.makeText(getContext(), "Insufficient chips. Please purchase.", Toast.LENGTH_LONG).show();
        }

        if (isValid) {
            Bundle bundle = new Bundle();
            bundle.putString(GameFragment.GAME_HASH_BUNDLE_KEY, gameHash);
            ((MainActivity)getActivity()).navigateToFragment(R.id.nav_game, bundle);
        }
    }

    /**
     * Occurs when user clicks the create button, to create a new network game
     * @param button The create button
     */
    public void onCreateButtonClicked(View button) {

    }

}