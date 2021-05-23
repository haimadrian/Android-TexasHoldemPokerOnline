package org.hit.android.haim.cw8;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.hit.android.haim.cw8.databinding.ActivityMainBinding;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private int imageNum = 0;
    private int timeSeconds = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.goButton.setOnClickListener(view -> {
            binding.goButton.setEnabled(false);
            binding.textView.setText(String.valueOf(timeSeconds));

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    timeSeconds--;

                    runOnUiThread(() -> {
                        if (timeSeconds <= 0) {
                            timer.cancel();
                            binding.textView.setText("");
                            binding.goButton.setEnabled(true);
                            timeSeconds = 3;
                        } else {
                            binding.textView.setText(String.valueOf(timeSeconds));
                        }
                    });
                }
            }, 1000, 1000);

            Timer timer2 = new Timer();
            timer2.schedule(new TimerTask() {
                @Override
                public void run() {
                    imageNum = (imageNum + 1) % 2;

                    runOnUiThread(() -> {
                        if (imageNum == 1) {
                            binding.imageView.setImageResource(R.drawable.deadpool2);
                        } else {
                            binding.imageView.setImageResource(R.drawable.png_clipart_deadpool_marvel_deadpool_illustration);
                        }

                        timer2.cancel();
                    });
                }
            }, 3000);
        });
    }
}