package org.hit.android.haim.hwrecyclerview.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.hit.android.haim.hwrecyclerview.R;
import org.hit.android.haim.hwrecyclerview.model.TvSeries;
import org.hit.android.haim.hwrecyclerview.model.repository.TvSeriesRepository;

import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

import static org.hit.android.haim.hwrecyclerview.view.SoundService.SOUND_RES_ID_EXTRA;

public class MainActivity extends AppCompatActivity {
    private static final String STORED_FOCUSED_TV_KEY = "focusedTvSeries";
    private static final String STORED_SOUND_MODE_KEY = "isSoundOn";

    private static final String SOUND_ON_TAG = "sound-on";
    private static final String SOUND_OFF_TAG = "sound-off";

    /**
     * Keep a reference to the sound service so we will stop it when activity is destroyed
     */
    private static Class<? super SoundService> lastStartedService;
    private ImageView volumeButton;
    private TvSeriesCardAdapter tvSeriesCardAdapter;

    /**
     * Used so we can distinguish between onUserLeaveHint events. This event is raised when
     * user presses the home button, but also when we navigate to browser.
     */
    private boolean isNavigateToBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView tvSeriesRecyclerView = findViewById(R.id.tvSeriesRecyclerView);
        tvSeriesRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        tvSeriesRecyclerView.setLayoutManager(layoutManager);
        tvSeriesRecyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));

        List<TvSeries> data = TvSeriesRepository.getInstance().getAll();
        tvSeriesCardAdapter = new TvSeriesCardAdapter(data,
                this,
                character -> {
                    Log.d("MainActivity", "Navigating to browser for: " + character.getName());
                    isNavigateToBrowser = true;
                    Uri uriUrl = Uri.parse("https://www.google.com/search?q=" + character.getName().replace(' ', '+'));
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                },
                savedInstanceState == null ? null : savedInstanceState.getString(STORED_FOCUSED_TV_KEY));
        tvSeriesRecyclerView.setAdapter(tvSeriesCardAdapter);

        volumeButton = findViewById(R.id.imageViewVolumeButton);
        setVolumeButtonChecked(savedInstanceState == null || savedInstanceState.getBoolean(STORED_SOUND_MODE_KEY));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MainActivity", "onStart");

        // When we are back from "recent apps", this is called without "onCreate", so resume music.
        String focusedTvSeries = tvSeriesCardAdapter.getFocusedTvSeries();
        if (!isNavigateToBrowser && focusedTvSeries != null && lastStartedService == null) {
            startService(SoundService.class, TvSeriesRepository.getInstance().get(focusedTvSeries).getThemeSongResId());
        }

        isNavigateToBrowser = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("MainActivity", "onBackPressed");
        if (lastStartedService != null) {
            stopService(lastStartedService);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.d("MainActivity", "onUserLeaveHint");
        if (!isNavigateToBrowser && lastStartedService != null) {
            stopService(lastStartedService);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("MainActivity", "onSaveInstanceState");
        outState.putString(STORED_FOCUSED_TV_KEY, tvSeriesCardAdapter.getFocusedTvSeries());
        outState.putBoolean(STORED_SOUND_MODE_KEY, isVolumeButtonChecked());
    }

    private void setVolumeButtonChecked(boolean isChecked) {
        if (isChecked) {
            volumeButton.setTag(SOUND_ON_TAG);
            volumeButton.setBackgroundResource(R.drawable.ic_twotone_volume_up_24);
        } else {
            volumeButton.setTag(SOUND_OFF_TAG);
            volumeButton.setBackgroundResource(R.drawable.ic_twotone_volume_off_24);
        }
    }

    private boolean isVolumeButtonChecked() {
        return SOUND_ON_TAG.equals(volumeButton.getTag());
    }

    public void startService(Class<? super SoundService> service, @RawRes int soundResId) {
        Log.d("MainActivity", "Starting service");
        if (lastStartedService != null) {
            stopService(lastStartedService);
        }

        if (isVolumeButtonChecked()) {
            Intent intent = new Intent(this, service);
            intent.putExtra(SOUND_RES_ID_EXTRA, soundResId);
            startService(intent);

            lastStartedService = service;
        }
    }

    public void stopService(Class<? super SoundService> service) {
        Log.d("MainActivity", "Stopping service");
        if (lastStartedService != null) {
            Intent intent = new Intent(this, service);
            stopService(intent);
            lastStartedService = null;
        }
    }

    /**
     * Occurs when user presses the sound on/off toggle
     */
    public void onSoundButtonClick(View view) {
        // Toggle
        boolean isChecked = !isVolumeButtonChecked();
        setVolumeButtonChecked(isChecked);

        if (isChecked) {
            // Play
            String focusedTvSeries = tvSeriesCardAdapter.getFocusedTvSeries();
            if (focusedTvSeries != null) {
                startService(SoundService.class, TvSeriesRepository.getInstance().get(focusedTvSeries).getThemeSongResId());
            }
        } else {
            // Mute
            stopService(SoundService.class);
        }
    }
}