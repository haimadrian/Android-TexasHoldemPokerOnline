package org.hit.android.haim.texasholdem.view.activity;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fasterxml.jackson.databind.JsonNode;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.view.fragment.login.SignInFragment;
import org.hit.android.haim.texasholdem.view.fragment.login.SignUpFragment;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

/**
 * The login activity consists of 2 text fields: username and password.<br/>
 * In order to sign in, user will enter username and password. <br/>
 * In case user is not registered, we will switch to the sign up fragment, to fill in date of birth
 * and the display name.
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * Name of our shared preferences file, which is private to our app only
     */
    public static final String TEXAS_HOLDEM_PREFS = "texasHoldemPrefs";

    /**
     * Name of userId key in our shared preferences file.<br/>
     * We store user identifier so we can skip sign in next time user launches the application.<br/>
     * User identifier is used for retrieving user info. Such a request will fail if the token is invalid, so we can
     * do a sign-in process. But if it succeeds, we can skip sign-in.
     */
    public static final String USER_ID = "userId";

    /**
     * Name of jwtToken key in our shared preferences file.<br/>
     * We store jwtToken so we can skip sign in next time user launches the application.<br/>
     * We set this jwt token to {@link TexasHoldemWebService}, which uses it as header. The server
     * will authorize the userInfo request. In case jwt is invalid, server will respond with 401 unauthorized.
     */
    public static final String USER_JWT_TOKEN = "jwtToken";

    /**
     * Used to switch between sign in fragment and sign up fragment.<br/>
     * Default fragment is the sign in. We try to sign in, and if we fail because of
     * not signed up, we will switch to the sign up fragment so user will fill in two additional fields.
     */
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    /**
     * Use this method when user selects to sign out, so we will clear shared preferences and sign him out of server.<br/>
     * When signing out, we immediately switch to the sign in activity.
     * @param context The context for initializing an intent with.
     */
    public static void doSignOut(ContextWrapper context) {
        TexasHoldemWebService.getInstance().getUserService().signOut().enqueue(new Callback<JsonNode>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
                TexasHoldemWebService.getInstance().setLoggedInUserId(null);

                Log.d("WEB", "Server responded with: " + response);
                Toast.makeText(context, "Signed out successfully", Toast.LENGTH_LONG).show();
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<JsonNode> call, Throwable t) {
                Log.e("WEB", "Error has occurred while signing out", t);
            }
        });
        TexasHoldemWebService.getInstance().setJwtToken(null); // Nothing will be authorized after sign out.

        // Clear data from shared preferences
        SharedPreferences.Editor editor = context.getSharedPreferences(LoginActivity.TEXAS_HOLDEM_PREFS, MODE_PRIVATE).edit();
        editor.remove(LoginActivity.USER_ID);
        editor.remove(LoginActivity.USER_JWT_TOKEN);
        editor.apply();

        Intent i = new Intent(context.getApplicationContext(), LoginActivity.class);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("Login", this.toString() + ": onCreate");
        super.onCreate(savedInstanceState);

        // Play in landscape orientation only.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Initialize it so it will be able to access resources (certificate)
        TexasHoldemWebService.getInstance().init(getApplicationContext());

        setContentView(R.layout.activity_login);

        // Try to see if we have a stored token, so we will skip sign in
        SharedPreferences sharedPreferences = getSharedPreferences(TEXAS_HOLDEM_PREFS, MODE_PRIVATE);
        String userId = sharedPreferences.getString(USER_ID, null);
        String jwtToken = sharedPreferences.getString(USER_JWT_TOKEN, null);

        if (jwtToken != null) {
            TexasHoldemWebService.getInstance().setJwtToken(jwtToken);
        }

        // Show the sign in fragment
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        SignInFragment signInFragment = new SignInFragment(userId);
        fragmentTransaction.add(R.id.loginFragmentsFrame, signInFragment).addToBackStack(null).commit();
    }

    /**
     * Used when user clicks the sign-up link, so we switch to sign up fragment
     * @param userName Entered username
     * @param password Entered password
     */
    public void navigateToSignUp(String userName, String password) {
        fragmentTransaction = fragmentManager.beginTransaction();
        SignUpFragment signUpFragment = new SignUpFragment(userName, password);
        fragmentTransaction.replace(R.id.loginFragmentsFrame, signUpFragment).addToBackStack(null).commit();
    }

    /**
     * Used after user successfully signed up, so we let him sign in
     */
    public void navigateToSignIn() {
        // Here we do not pass shared preferences cause we get to this method from sign-up, and if we are during sign-up, it means
        // the shared preferences were invalid.
        fragmentTransaction = fragmentManager.beginTransaction();
        SignInFragment signInFragment = new SignInFragment();
        fragmentTransaction.replace(R.id.loginFragmentsFrame, signInFragment).addToBackStack(null).commit();
    }

    /**
     * Call this method when user is signed in successfully, to switch to main activity
     * @param user The signed in user
     */
    public void userSignedInSuccessfully(User user) {
        // First, save the jwtToken as a shared preference, so we will be able to connect automatically next time
        // We want the cookie to be stored in our device only, without letting other apps to get it
        SharedPreferences.Editor editor = getSharedPreferences(TEXAS_HOLDEM_PREFS, MODE_PRIVATE).edit();
        editor.putString(USER_ID, user.getId());
        editor.putString(USER_JWT_TOKEN, TexasHoldemWebService.getInstance().getJwtToken());
        editor.apply();

        // Second, switch to home page
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);

        // Finish this activity as we went to home activity
        finish();
    }

    public void onQuitButtonClicked(View view) {
        ExitActivity.exit(this.getApplicationContext());
        finish();
    }
}