package org.hit.android.haim.texasholdem.view.fragment.home;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.databinding.FragmentHomeBinding;
import org.hit.android.haim.texasholdem.view.activity.MainActivity;

/**
 * Home fragment contains two buttons: Play vs pc and Play on network
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentHomeBinding.bind(view);
        binding.buttonPlayAi.setOnClickListener(this::onPlayAiButtonClicked);
        binding.buttonPlayNetwork.setOnClickListener(this::onPlayNetworkButtonClicked);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    private void onPlayAiButtonClicked(View imageView) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).navigateToFragment(R.id.nav_play_ai);
        }
    }

    private void onPlayNetworkButtonClicked(View imageView) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).navigateToFragment(R.id.nav_play_network);
        }
    }
}