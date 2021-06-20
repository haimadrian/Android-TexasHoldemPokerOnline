package org.hit.android.haim.texasholdem.view.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.databinding.FragmentAboutBinding;

/**
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class AboutFragment extends ViewBindedFragment<FragmentAboutBinding> {
    private static final String LOGGER = AboutFragment.class.getSimpleName();

    public AboutFragment() {
        super(R.layout.fragment_about, FragmentAboutBinding::bind);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getBinding().textViewGithubLink.setOnClickListener(v -> {
            Log.d(LOGGER, "Navigating to GitHub");
            Uri uriUrl = Uri.parse("https://github.com/haimadrian/Android1/tree/main/Project");
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        });
    }
}