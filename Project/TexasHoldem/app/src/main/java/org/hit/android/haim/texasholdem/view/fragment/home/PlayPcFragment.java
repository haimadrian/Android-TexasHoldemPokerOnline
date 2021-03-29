package org.hit.android.haim.texasholdem.view.fragment.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hit.android.haim.texasholdem.R;


/**
 * This fragment lets the user to configure the game against pc.<br/>
 * e.g. how many bots there will be, and with how many coins to enter.
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class PlayPcFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_pc, container, false);
    }
}