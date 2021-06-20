package org.hit.android.haim.texasholdem.view.fragment.home;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.databinding.FragmentGameBinding;
import org.hit.android.haim.texasholdem.view.fragment.ViewBindedFragment;


/**
 * This fragment displays a game.<br/>
 * A game can be against AI or other players from network.
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class GameFragment extends ViewBindedFragment<FragmentGameBinding> {

    public GameFragment() {
        super(R.layout.fragment_game, FragmentGameBinding::bind);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getBinding();
    }
}