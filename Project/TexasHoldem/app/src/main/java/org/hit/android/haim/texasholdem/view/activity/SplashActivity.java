package org.hit.android.haim.texasholdem.view.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import org.hit.android.haim.texasholdem.R;

/**
 * The first activity that we display is a splash screen with cards animation<br/>
 * This activity does nothing except of waiting for some seconds for the animation to finish and then
 * switches to the {@link LoginActivity}
 * @author Haim Adrian
 * @since 15-Jun-21
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        Intent mainActivity = new Intent(this, LoginActivity.class);
        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out);

        // Run it later on UI thread
        new Handler().postDelayed(() -> {
            startActivity(mainActivity, activityOptions.toBundle());
        }, 4500);
    }
}