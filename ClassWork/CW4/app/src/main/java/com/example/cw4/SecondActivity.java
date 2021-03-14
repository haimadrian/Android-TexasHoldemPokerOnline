package com.example.cw4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        prefs = getSharedPreferences(MainActivity.KEY_SHARED, MODE_PRIVATE);
        String name = prefs.getString(MainActivity.TEXT_KEY, "No cookie was found");

        String text = "Intent: " + getIntent().getStringExtra(MainActivity.TEXT_KEY) + System.lineSeparator() +
                "Shared: " + name;

        TextView textView = findViewById(R.id.textViewResult);
        textView.setText(text);

        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    public void onBackButtonClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.TEXT_KEY, getIntent().getStringExtra(MainActivity.TEXT_KEY));
        startActivity(intent);
    }
}