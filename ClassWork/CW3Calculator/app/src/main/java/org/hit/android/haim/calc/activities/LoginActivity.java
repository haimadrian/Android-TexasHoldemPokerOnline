package org.hit.android.haim.calc.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import org.hit.android.haim.calc.R;
import org.hit.android.haim.calc.server.CalculatorWebService;
import org.hit.android.haim.calc.server.Response;
import org.hit.android.haim.calc.server.User;

import java.util.function.Consumer;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText displayNameEditText;
    private EditText dateEditText;
    private FrameLayout signUpFrame;
    private AppCompatButton goButton;
    private TextView navigationLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.userEmailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        displayNameEditText = findViewById(R.id.userDisplayNameEditText);
        dateEditText = findViewById(R.id.userDateOfBirthEditText);
        signUpFrame = findViewById(R.id.signUpFragmentLayout);
        goButton = findViewById(R.id.goButton);
        navigationLink = findViewById(R.id.navigationLink);

        signUpFrame.setVisibility(View.GONE);
        goButton.setText(R.string.sign_in);
        navigationLink.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (signUpFrame.getVisibility() == View.VISIBLE) {
            signUpFrame.setVisibility(View.GONE);
            goButton.setText(R.string.sign_in);
            navigationLink.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    public void onSignInClicked(View view) {
        String email = emailEditText.getText().toString();
        String pwd = passwordEditText.getText().toString();

        if (signUpFrame.getVisibility() == View.VISIBLE) {
            String displayName = displayNameEditText.getText().toString();
            //String dateOfBirth = dateEditText.getText().toString();

            CalculatorWebService.getInstance().signUp(new User(email, displayName, null), pwd, new LoginResponseConsumer());
        } else {
            CalculatorWebService.getInstance().signIn(email, pwd, new LoginResponseConsumer());
        }
    }

    public void onSignUpClicked(View view) {
        signUpFrame.setVisibility(View.VISIBLE);
        goButton.setText(R.string.sign_up);
        navigationLink.setVisibility(View.GONE);
    }

    private class LoginResponseConsumer implements Consumer<Response> {
        @Override
        public void accept(Response response) {
            if ((response == null) || response.getStatus() != 200) {
                Log.e("Login", "Error returned from server: " + (response != null ? response.getErrorMessage() : null));
                if (response != null) {
                    Toast.makeText(LoginActivity.this, response.getStatus() + " - " + response.getErrorMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                String responseValue = response.getValue();
                if (responseValue.toLowerCase().contains("success") || responseValue.toLowerCase().contains("welcome")) {
                    Toast.makeText(LoginActivity.this, responseValue, Toast.LENGTH_LONG).show();

                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra(MainActivity.STORED_SERVER_KEY, true);
                    startActivity(i);

                    // Finish this activity as we went to home activity
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, responseValue, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}