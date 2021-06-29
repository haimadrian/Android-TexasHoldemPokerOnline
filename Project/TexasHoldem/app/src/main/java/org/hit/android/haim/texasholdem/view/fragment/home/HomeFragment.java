package org.hit.android.haim.texasholdem.view.fragment.home;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.databinding.FragmentHomeBinding;
import org.hit.android.haim.texasholdem.view.activity.MainActivity;
import org.hit.android.haim.texasholdem.view.fragment.ViewBindedFragment;

/**
 * Home fragment contains two buttons: Play vs pc and Play on network
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class HomeFragment extends ViewBindedFragment<FragmentHomeBinding> {
    public HomeFragment() {
        super(R.layout.fragment_home, FragmentHomeBinding::bind);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getBinding().buttonPlayAi.setOnClickListener(this::onPlayAiButtonClicked);
        getBinding().buttonPlayNetwork.setOnClickListener(this::onPlayNetworkButtonClicked);
    }

    /**
     * Occurs when user clicks the play button under AI section
     * @param button The play button
     */
    private void onPlayAiButtonClicked(View button) {
        if (getActivity() != null) {
            Toast.makeText(getContext(), "This will be available soon.", Toast.LENGTH_LONG).show();
            //((MainActivity)getActivity()).navigateToFragment(R.id.nav_play_ai);
        }
    }

    /**
     * Occurs when user clicks the play button under Network section
     * @param button The play button
     */
    private void onPlayNetworkButtonClicked(View button) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).navigateToFragment(R.id.nav_play_network);
        }
    }
}