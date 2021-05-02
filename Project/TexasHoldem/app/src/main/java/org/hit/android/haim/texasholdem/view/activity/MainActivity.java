package org.hit.android.haim.texasholdem.view.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.web.SimpleCallback;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

/**
 * Main activity has a navigation view where we let user to go to several fragments.<br/>
 * The home fragment contains two buttons: 1. Play vs PC, 2. Play on Network.<br/>
 * There are also preferences fragment and about.<br/>
 * In addition, there are two buttons at the bottom of the navigation view: sign-out and exit.
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    /**
     * Keep a reference to the main content view so we will use it when showing a snack bar
     */
    private DrawerLayout drawer;

    /**
     * User name text view from the navigation panel, where we display user profile
     */
    private TextView userTextView;

    /**
     * User coins text view from the navigation panel, where we display user profile
     */
    private TextView coinsTextView;

    /**
     * User profile image from the navigation panel, where we display user profile
     */
    private ImageView userImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Play in landscape orientation only.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration =
                new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_preferences, R.id.nav_about)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Listen to navigation buttons, to handle sign-out and exit here instead of navigating to other fragments.
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // First check if user clicked one of the buttons
            if (itemId == R.id.nav_sign_out) {
                LoginActivity.doSignOut(MainActivity.this);
            } else if (itemId == R.id.nav_exit) {
                // Execute it later, so we will not break the event handling.
                new Handler().post(() -> {
                    ExitActivity.exit(MainActivity.this.getApplicationContext());
                    MainActivity.this.finish();
                });
            }
            // If it is not one of the buttons, then it is a navigation item. Navigate to its identifier
            else {
                navigateToFragment(itemId);
                return true; // Draw selection for navigation items
            }

            return false; // No need to draw selection for buttons
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        userTextView = headerView.findViewById(R.id.userTextView);
        coinsTextView = headerView.findViewById(R.id.coinsTextView);
        userImageView = headerView.findViewById(R.id.userImage);
        userImageView.setOnClickListener(v -> {
            // TODO: Select image
        });

        refreshUserInfo();
    }

    /**
     * Use this method in order to refresh user profile in the navigation view.<br/>
     * We need to refresh it, for example, when user purchase coins, so we will display the up to date amount of coins.
     */
    public void refreshUserInfo() {
        // Get user info, to set it to navigation header
        TexasHoldemWebService.getInstance().getUserService().getUserInfo(TexasHoldemWebService.getInstance().getLoggedInUserId()).enqueue(new SimpleCallback<JsonNode>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
                if (!response.isSuccessful()) {
                    handleHttpErrorResponse("User info is unavailable", response);
                } else {
                    JsonNode body = response.body();
                    try {
                        User user = TexasHoldemWebService.getInstance().getObjectMapper().readValue(body.toString(), User.class);

                        userTextView.setText(String.format(getString(R.string.nav_header_user), user.getName(), user.getId()));
                        coinsTextView.setText(String.valueOf(user.getCoins()));

                        if (user.getImage() != null) {
                            userImageView.setImageBitmap(user.getImageBitmap());
                        } else {
                            // Default user image
                            userImageView.setImageResource(R.drawable.user);
                        }
                    } catch (IOException e) {
                        Log.e("Main", "Failed parsing response. Response was: " + body, e);
                        showSnack("User info is unavailable. Reason: " + e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Currently there is no "up button" (back button) and we hide navigation bar.
    // But keep it here so in case we will add it sometime, the action will be redirected to our navigation controller.
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    /**
     * Used when user clicks a button to switch to another fragment
     * @param fragmentActionId Action identifier from navigation\mobile_navigation.xml to perform. (To which fragment to switch)
     */
    public void navigateToFragment(int fragmentActionId) {
        navigateToFragment(fragmentActionId, null);
    }

    /**
     * Used when user clicks a button to switch to another fragment
     * @param fragmentActionId Action identifier from navigation\mobile_navigation.xml to perform. (To which fragment to switch)
     * @param args Optional arguments for the new fragment
     */
    public void navigateToFragment(int fragmentActionId, @Nullable Bundle args) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(fragmentActionId, args);
    }

    /**
     * Extract error body from an http response, and show it as a snack bar
     * @param response The response to get error body from
     */
    public void handleHttpErrorResponse(String message, Response<?> response) {
        String errorMessage;
        try {
            errorMessage = message + ". Reason: " + response.errorBody().string();
        } catch (IOException e) {
            Log.w("Web", "Error has occurred while reading response error as string: " + e);
            errorMessage = message + ". Reason: " + response.message();
        }

        showSnack(errorMessage);
    }

    /**
     * Use this method to show some message using snack bar over the main activity
     * @param message The message to display
     */
    public void showSnack(String message) {
        Snackbar.make(drawer, message, Snackbar.LENGTH_LONG).show();
    }
}