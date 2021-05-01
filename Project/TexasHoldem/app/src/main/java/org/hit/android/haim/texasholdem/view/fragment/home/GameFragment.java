package org.hit.android.haim.texasholdem.view.fragment.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import org.hit.android.haim.texasholdem.R;


/**
 * This fragment displays a game.<br/>
 * A game can be against AI or other players from network.
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class GameFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_ai, container, false);
    }
}