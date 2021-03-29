package org.hit.android.haim.texasholdem.view.fragment.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.view.activity.MainActivity;

/**
 * Home fragment contains two buttons: Play vs pc and Play on network
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.imageViewPc).setOnClickListener(this::onPlayPcButtonClicked);
        view.findViewById(R.id.imageViewNetwork).setOnClickListener(this::onPlayNetworkButtonClicked);
    }

    private void onPlayPcButtonClicked(View imageView) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).navigateToFragment(R.id.nav_play_pc);
        }
    }

    private void onPlayNetworkButtonClicked(View imageView) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).navigateToFragment(R.id.nav_play_network);
        }
    }
}