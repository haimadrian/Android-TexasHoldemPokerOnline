package org.hit.android.haim.texasholdem.view;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.common.model.bean.game.Player;
import org.hit.android.haim.texasholdem.common.model.game.GameEngine;
import org.hit.android.haim.texasholdem.model.game.Game;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Play game sound effects in background
 * @author Haim Adrian
 * @since 12-Jun-21
 */
public class GameSoundService extends Service implements Game.GameListener {
    private static final String LOGGER = GameSoundService.class.getSimpleName();

    /**
     * Map between {@link org.hit.android.haim.texasholdem.model.game.Game.GameStepType} to its corresponding
     * media player, so we can play sound effects for each game step.
     */
    private Map<Game.GameStepType, MediaPlayer> mediaPlayers;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOGGER, "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOGGER, "onStartCommand");

        MediaPlayer chipsLongMediaPlayer = MediaPlayer.create(this, R.raw.chips_long);
        mediaPlayers = new HashMap<>(Game.GameStepType.values().length);
        mediaPlayers.put(Game.GameStepType.CALL, MediaPlayer.create(this, R.raw.chips_short));
        mediaPlayers.put(Game.GameStepType.RAISE, chipsLongMediaPlayer);
        mediaPlayers.put(Game.GameStepType.ALL_IN, chipsLongMediaPlayer);
        mediaPlayers.put(Game.GameStepType.TIMER, MediaPlayer.create(this, R.raw.kitchen_timer));
        mediaPlayers.put(Game.GameStepType.CHECK, MediaPlayer.create(this, R.raw.knock_on_door));
        mediaPlayers.put(Game.GameStepType.DEAL_CARD, MediaPlayer.create(this, R.raw.dealing_card));
        mediaPlayers.put(Game.GameStepType.FLIP_CARD, MediaPlayer.create(this, R.raw.flip_card));
        mediaPlayers.put(Game.GameStepType.WIN, MediaPlayer.create(this, R.raw.success));
        mediaPlayers.put(Game.GameStepType.LOSE, MediaPlayer.create(this, R.raw.fail));

        // Listen to game steps so we can play sound effects for each step
        Game.getInstance().addGameListener(this);

        // Use sticky so onStartCommand will be called again in case service is killed.
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOGGER, "onDestroy");
        Game.getInstance().removeGameStepListener(this);
        mediaPlayers.values().forEach(GameSoundService::releaseMediaPlayer);
        mediaPlayers.clear();

        // Stop game to shutdown background threads
        Game.getInstance().stop();

        stopSelf();
        super.onDestroy();
    }

    private static void releaseMediaPlayer(MediaPlayer mediaPlayer) {
        mediaPlayer.stop();
        mediaPlayer.reset(); // Reset media player before releasing it so we will not get "mediaplayer went away with unhandled events" warning
        mediaPlayer.release();
    }

    private static void restartMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer.start();
    }

    @Override
    public void onStep(GameEngine gameEngine, Game.GameStepType step) {
        MediaPlayer mediaPlayer = mediaPlayers.get(step);
        if (mediaPlayer != null) {
            restartMediaPlayer(mediaPlayer);
        }
    }

    @Override
    public void refresh(GameEngine gameEngine) {
        // Do nothing
    }

    @Override
    public void playersRefresh(Set<Player> players) {
        // Do nothing
    }

    @Override
    public void onGameError(String errorMessage) {
        // Do nothing
    }
}
