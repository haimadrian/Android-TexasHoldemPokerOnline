package org.hit.android.haim.texasholdem.view.fragment.home;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.databinding.FragmentPlayAiBinding;
import org.hit.android.haim.texasholdem.view.fragment.ViewBindedFragment;


/**
 * This fragment lets the user to configure the game against AI.<br/>
 * e.g. how many bots there will be, and with how many coins to enter.
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class PlayAiFragment extends ViewBindedFragment<FragmentPlayAiBinding> {

    public PlayAiFragment() {
        super(R.layout.fragment_play_ai, FragmentPlayAiBinding::bind);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getBinding();
    }
}