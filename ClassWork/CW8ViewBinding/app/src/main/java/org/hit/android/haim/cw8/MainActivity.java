package org.hit.android.haim.cw8;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;

import org.hit.android.haim.cw8.databinding.ActivityMainBinding;

import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
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

    public void onEditTextClick(View view) {
        final Calendar cldr = Calendar.getInstance();
        int currYear = cldr.get(Calendar.YEAR) - 18, currMonth = Month.JANUARY.ordinal(), currDay = 1;
        String currentDate = binding.textEdit.getText().toString();
        if (!currentDate.isEmpty()) {
            LocalDate date = LocalDate.parse(currentDate);
            currYear = date.getYear();
            currMonth = date.getMonthValue() - 1;
            currDay = date.getDayOfMonth();
        }

        DatePickerDialog picker = new DatePickerDialog(this,
                R.style.MySpinnerDatePickerStyle,
                (view1, year, month, day) -> binding.textEdit.setText(LocalDate.of(year, month + 1, day).toString()),
                currYear,
                currMonth,
                currDay);
        picker.setTitle("Select date of birth");
        picker.show();
    }
}