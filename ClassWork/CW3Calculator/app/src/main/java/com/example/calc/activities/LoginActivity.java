package com.example.calc.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.calc.R;
import com.example.calc.action.ActionType;
import com.example.calc.server.CalculatorWebService;

public class LoginActivity extends AppCompatActivity {

    private EditText userNameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userNameEditText = findViewById(R.id.userNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
    }

    public void onSignInClicked(View view) {
        String userName = userNameEditText.getText().toString();
        String pwd = passwordEditText.getText().toString();

        CalculatorWebService.getInstance().executeDynamicAction(userName + "##" + pwd, ActionType.CONNECT, response -> {
            if ((response == null) || response.getStatus() != 200) {
                Log.e("Login", "Error returned from server: " + (response != null ? response.getErrorMessage() : null));
                if (response != null) {
                    Toast.makeText(LoginActivity.this, response.getStatus() + " - " + response.getErrorMessage(), Toast.LENGTH_LONG).show();
                }
            } else if (response.getValue().toLowerCase().contains("success")) {
                Toast.makeText(LoginActivity.this, response.getValue(), Toast.LENGTH_LONG).show();

                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra(MainActivity.STORED_SERVER_KEY, true);
                startActivity(i);

                // Finish this activity as we went to home activity
                finish();
            } else {
                Toast.makeText(LoginActivity.this, response.getValue(), Toast.LENGTH_LONG).show();
            }
        });
    }
}