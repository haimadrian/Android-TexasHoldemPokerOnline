package org.hit.android.haim.hwrecyclerview.view;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * @author Haim Adrian
 * @since 10-Apr-21
 */
public class SoundService extends Service {
    public static final String SOUND_RES_ID_EXTRA = "soundResId";

    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SoundService", "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SoundService", "onStartCommand");

        int soundResId = intent.getIntExtra(SOUND_RES_ID_EXTRA, 0);
        mediaPlayer = MediaPlayer.create(this, soundResId);
        mediaPlayer.setLooping(true); //set looping
        mediaPlayer.start();

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("SoundService", "onDestroy");
        mediaPlayer.stop();
        mediaPlayer.reset(); // Reset media player before releasing it so we will not get "mediaplayer went away with unhandled events" warning
        mediaPlayer.release();
        stopSelf();
        super.onDestroy();
    }
}
