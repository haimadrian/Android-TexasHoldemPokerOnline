package org.hit.android.haim.texasholdem.view.fragment.home;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.databinding.FragmentPlayNetworkBinding;
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

        getBinding();
    }

}