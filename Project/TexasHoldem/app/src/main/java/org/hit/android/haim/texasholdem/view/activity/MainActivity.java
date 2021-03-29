package org.hit.android.haim.texasholdem.view.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.view.model.LoggedInUserView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Play in landscape orientation only.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
                new Handler(Looper.getMainLooper()).post(() -> {
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

        // Get user info, to set it to navigation header
        LoggedInUserView user = (LoggedInUserView)getIntent().getSerializableExtra(LoginActivity.USER_EXTRA_NAME);

        View headerView = navigationView.getHeaderView(0);
        ((TextView) headerView.findViewById(R.id.userTextView)).setText(String.format(getString(R.string.nav_header_user), user.getNickName(), user.getUserId()));
        ((TextView) headerView.findViewById(R.id.coinsTextView)).setText(String.valueOf(user.getCoins()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

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
}