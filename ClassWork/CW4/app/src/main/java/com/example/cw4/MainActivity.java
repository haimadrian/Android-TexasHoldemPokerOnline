package com.example.cw4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String TEXT_KEY = "text";
    public static final String KEY_SHARED = "MainActivityLogin";

    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Shared preferences for persistent caching
        String cookie = getSharedPreferences(KEY_SHARED, MODE_PRIVATE).getString(TEXT_KEY, null);
        if (cookie != null) {
            EditText editText = findViewById(R.id.sendEditText);
            editText.setText(cookie);
        }

        // Intent for in memory caching
        Intent intent = getIntent();
        if (intent.hasExtra(TEXT_KEY)) {
            String text = intent.getStringExtra(TEXT_KEY);

            EditText editText = findViewById(R.id.sendEditText);
            editText.setText(text);
        }
    }

    public void onSendButtonClicked(View view) {
        EditText editText = findViewById(R.id.sendEditText);
        String str = editText.getText().toString();

        // We want the cookie to be stored in our device only, without letting other apps to get it
        editor = getSharedPreferences(KEY_SHARED, MODE_PRIVATE).edit();

        // We can store a lot of values in one editor
        editor.putString(TEXT_KEY, str);
        editor.apply(); // Create the cookie

        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra(TEXT_KEY, str);
        startActivity(intent);
    }
}