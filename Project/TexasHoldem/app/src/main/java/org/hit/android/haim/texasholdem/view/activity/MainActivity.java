package org.hit.android.haim.texasholdem.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.databinding.ActivityMainBinding;
import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.model.game.Game;
import org.hit.android.haim.texasholdem.view.GameSoundService;
import org.hit.android.haim.texasholdem.web.SimpleCallback;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;

import java.io.InputStream;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Main activity has a navigation view where we let user to go to several fragments.<br/>
 * The home fragment contains two buttons: 1. Play vs PC, 2. Play on Network.<br/>
 * There are also preferences fragment and about.<br/>
 * In addition, there are two buttons at the bottom of the navigation view: sign-out and exit.
 * @author Haim Adrian
 * @since 27-Mar-21
 */
public class MainActivity extends AppCompatActivity {
    private static final String LOGGER = MainActivity.class.getSimpleName();

    /**
     * Defines read gallery permissions request type
     */
    private static final int PICK_FROM_GALLERY = 1;

    private AppBarConfiguration mAppBarConfiguration;

    /**
     * <ul>
     *     <li>drawerLayout - Keep a reference to the main content view so we will use it when showing a snack bar</li>
     * </ul>
     */
    private ActivityMainBinding binding;

    // Those fields are from binding.navView.headerView[0]
    // Keep direct references to avoid of the clumsy access to them. :)
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

    /**
     * A reference to the logged in user, so we can access it from any fragment under MainActivity.
     */
    @Getter
    private User user;

    /**
     * Hold a handler as a data member, cause it might be garbage collected before executing tasks that we submit.<br/>
     * When handler refers to null, it means the view is destroyed, and a post action should be discarded.
     */
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.myLooper());

        // Play in landscape orientation only.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavigationView navigationView = binding.navView;

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration =
                new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_preferences, R.id.nav_about)
                .setOpenableLayout(binding.drawerLayout)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Listen to navigation buttons, to handle sign-out and exit here instead of navigating to other fragments.
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // First check if user clicked one of the buttons
            if (itemId == R.id.nav_sign_out) {
                // Execute it later, so we will not break the event handling.
                if (handler != null) {
                    handler.post(() -> {
                        LoginActivity.doSignOut(MainActivity.this);

                        // Finish this activity as we went to login activity
                        MainActivity.this.finish();
                    });
                }
            } else if (itemId == R.id.nav_exit) {
                // Execute it later, so we will not break the event handling.
                if (handler != null) {
                    handler.post(() -> {
                        ExitActivity.exit(MainActivity.this.getApplicationContext());
                        MainActivity.this.finish();
                    });
                }
            }
            // If it is not one of the buttons, then it is a navigation item. Navigate to its identifier
            else {
                navigateToFragment(itemId);
                return true; // Draw selection for navigation items
            }

            return false; // No need to draw selection for buttons
        });

        binding.buttonOpenDrawer.setClickable(true);
        binding.buttonOpenDrawer.setOnClickListener(v -> binding.drawerLayout.open());

        // Hide game menu. We show it when player joins a game only.
        binding.navView.getMenu().findItem(R.id.nav_game).setVisible(false);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        View headerView = binding.navView.getHeaderView(0);
        userTextView = headerView.findViewById(R.id.userTextView);
        coinsTextView = headerView.findViewById(R.id.coinsTextView);
        userImageView = headerView.findViewById(R.id.userImage);
        ImageView buyCoinsImageView = headerView.findViewById(R.id.buyCoins);

        // When user presses the image view, we let him select an image to be used as profile picture.
        // But first we must have permissions granted in order to do that
        userImageView.setOnClickListener(v -> {
            // If permissions are missing, ask user to grant them. This will raise the onRequestPermissionsResult
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, PICK_FROM_GALLERY);
            }
            // Otherwise, permissions were already granted, open image chooser
            else {
                openImageChooser();
            }
        });

        buyCoinsImageView.setOnClickListener(this::onBuyCoinsClicked);

        refreshUserInfo();
    }

    /**
     * Call this method when user joins / leaves a game, so we will show or hide the game menu accordingly.
     */
    public void refreshGameMenuVisibility() {
        // In case view was re-created, check if user already part of game, to show game menu.
        Game.getInstance().ifPlayerPartOfGame(user.getId(), gameEngine -> binding.navView.getMenu().findItem(R.id.nav_game).setVisible(gameEngine != null));
    }

    @Override
    protected void onDestroy() {
        handler = null;
        super.onDestroy();
    }

    /**
     * This is raised when user presses the but icons button. (shopping cart in the navigation panel)<br/>
     * Here we let user purchase coins.
     * @param view Image view
     */
    private void onBuyCoinsClicked(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogTheme_Light);

        // Get dialog_purchase.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_purchase, null);
        alertDialogBuilder.setView(promptsView);

        EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);

        // Define dialog buttons and respond to user clicks over them
        alertDialogBuilder
                .setCancelable(true)
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel())
                .setPositiveButton("Buy", (dialog, id) -> {
                    long amountOfChips = 0;
                    String userInputText = userInput.getText().toString().trim();

                    Log.d(LOGGER, "Purchasing " + userInputText + " chips.");
                    try {
                        amountOfChips = Long.parseLong(userInputText);
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Illegal input: " + userInputText, Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }

                    // Update cloud with the new amount
                    if (amountOfChips > 0) {
                        User user = getUser().toBuilder().coins(getUser().getCoins() + amountOfChips).build();
                        TexasHoldemWebService.getInstance().getUserService().updateCoins(getUser().getId(), user).enqueue(new UserInfoCallback(() ->
                                Toast.makeText(MainActivity.this, "Purchase completed successfully", Toast.LENGTH_LONG).show()));
                    }
                });

        // Create and show alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(LOGGER, this + ".onRequestPermissionsResult(" + requestCode + ")");

        if ((requestCode == PICK_FROM_GALLERY) && (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            openImageChooser();
        }
    }

    // Currently there is no "up button" (back button) and we hide navigation bar.
    // But keep it here so in case we will add it sometime, the action will be redirected to our navigation controller.
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    // We override this method to handle activity result (when user finishes cropping profile image)
    // We might also get here when user select an image from gallery.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOGGER, this + ".onActivityResult(" + requestCode + ", " + resultCode + ")");

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                try {
                    final InputStream imageStream = MainActivity.this.getContentResolver().openInputStream(resultUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    user.setImageBitmap(selectedImage);
                    TexasHoldemWebService.getInstance().getUserService().updateImage(user.getId(), user).enqueue(new UserInfoCallback());
                } catch (Exception e) {
                    Log.e(LOGGER, "Error has occurred while selecting image: " + e.getMessage(), e);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                showSnack(error.getMessage());
                Log.e(LOGGER, "Error while cropping image: ", error);
            }
        } else if ((requestCode == PICK_FROM_GALLERY) && (resultCode == RESULT_OK)) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                try {
                    // Start the image cropping activity. After finishing, the onActivityResult event will be triggered
                    CropImage.activity(selectedImage)
                             .setActivityTitle("Crop Face")
                             .setGuidelines(CropImageView.Guidelines.ON)
                             .setAllowFlipping(true)
                             .setCropShape(CropImageView.CropShape.OVAL)
                             .start(this);
                } catch (Exception e) {
                    Log.e(LOGGER, "Error has occurred while selecting image: " + e.getMessage(), e);
                }
            } else {
                Log.d(LOGGER, "Null image was selected");
            }
        }
    }

    @Override
    public void onBackPressed() {
        // When the current focused fragment is Home's fragment, exit the application.
        // We do this such that in case user travelled the navigation, and went to home screen over and over
        // we will not go back over and over to the home screen. Instead, we will exit from fome screen.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavBackStackEntry currentBackStackEntry = navController.getCurrentBackStackEntry();
        if ((currentBackStackEntry != null) && (currentBackStackEntry.getDestination().getId() == R.id.nav_home)) {
            // Execute it later, so we will not break the event handling.
            if (handler != null) {
                handler.post(() -> {
                    Log.d(LOGGER, "Exiting");
                    Game.getInstance().stop(null);
                    stopService(new Intent(MainActivity.this, GameSoundService.class));

                    ExitActivity.exit(MainActivity.this.getApplicationContext());
                    MainActivity.this.finish();
                });
            }
        } else if ((currentBackStackEntry != null) && (currentBackStackEntry.getDestination().getId() == R.id.nav_game)) {
            // Back press is ignored when we are at the game fragment. For this there is a "quit" button.
            Log.d(LOGGER, "Disabled back press because we are at the game fragment.");
        } else {
            super.onBackPressed();
        }
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
        // Hide burger when on chat fragment
        binding.buttonOpenDrawer.setVisibility(fragmentActionId == R.id.nav_chat ? View.INVISIBLE : View.VISIBLE);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(fragmentActionId, args);
        binding.drawerLayout.closeDrawers();
    }

    /**
     * Programmatically go back (demonstrate user press on the back button) to go back in the
     * navigation controller.<br/>
     * We use this to go back from chat fragment to game fragment.
     */
    public void navigateBack() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.popBackStack();

        // Hide burger when on chat fragment
        NavBackStackEntry currentBackStackEntry = navController.getCurrentBackStackEntry();
        if (currentBackStackEntry != null) {
            binding.buttonOpenDrawer.setVisibility(currentBackStackEntry.getDestination().getId() == R.id.nav_chat ? View.INVISIBLE : View.VISIBLE);
        } else {
            binding.buttonOpenDrawer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Use this method in order to refresh user profile in the navigation view.<br/>
     * We need to refresh it, for example, when user purchase coins, so we will display the up to date amount of coins.
     */
    public void refreshUserInfo() {
        // Get user info, to set it to navigation header
        TexasHoldemWebService.getInstance().getUserService().getUserInfo(TexasHoldemWebService.getInstance().getLoggedInUserId()).enqueue(new UserInfoCallback());
    }

    /**
     * This method will open image chooser so user will select image to update his profile picture.
     */
    @SuppressWarnings("Deprecation")
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_FROM_GALLERY); // Ahoi

        // Comment this out as it does not allow starting activity here...
        /*registerForActivityResult(new ActivityResultContracts.OpenDocument() {
            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, @NonNull String[] input) {
                return super.createIntent(context, input).putExtra(Intent.EXTRA_MIME_TYPES, new String[] { "image/*" });
            }
        }, result -> {

        });*/
    }

    /**
     * Extract error body from an http response, and show it as a snack bar
     * @param response The response to get error body from
     */
    public void handleHttpErrorResponse(String message, Response<?> response) {
        // When we are not authorized, logout and navigate to login page
        if (response.code() == 401) {
            Toast.makeText(this, "Unauthorized. Please sign in", Toast.LENGTH_LONG).show();

            // Execute it later, so we will not break the event handling.
            if (handler != null) {
                handler.post(() -> {
                    TexasHoldemWebService.getInstance().setLoggedInUserId(null);
                    TexasHoldemWebService.getInstance().setJwtToken(null);

                    Intent i = new Intent(this, LoginActivity.class);
                    this.startActivity(i);

                    // Finish this activity as we went to login activity
                    this.finish();
                });
            }
        } else {
            String errorMessage = message + ": " + TexasHoldemWebService.getInstance().readHttpErrorResponse(response);
            showSnack(errorMessage);
        }
    }

    /**
     * Use this method to show some message using snack bar over the main activity
     * @param message The message to display
     */
    public void showSnack(String message) {
        Snackbar.make(binding.drawerLayout, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * This class was created to handle User info responses from server.<br/>
     * The server returns the user model when we ask for user info by identifier, or when
     * we update its details. For example when we update the image or coins.
     */
    private class UserInfoCallback extends SimpleCallback<JsonNode> {
        /**
         * An optional task to run when response is received successfully.
         */
        private Runnable runLater;

        /**
         * Constructs a new {@link UserInfoCallback}
         */
        public UserInfoCallback() {

        }

        /**
         * Constructs a new {@link UserInfoCallback}
         * @param runLater An optional task to run when response is received successfully.
         */
        public UserInfoCallback(Runnable runLater) {
            this.runLater = runLater;
        }

        @Override
        public void onResponse(@NonNull Call<JsonNode> call, @NonNull Response<JsonNode> response) {
            if (!response.isSuccessful()) {
                handleHttpErrorResponse("User info is unavailable", response);
            } else {
                JsonNode body = response.body();
                try {
                    user = TexasHoldemWebService.getInstance().getObjectMapper().readValue(body.toString(), User.class);
                    Log.d(LOGGER, "Received user info: " + user);

                    userTextView.setText(String.format(getString(R.string.nav_header_user), user.getName(), user.getId()));
                    coinsTextView.setText(String.valueOf(user.getCoins()));

                    if (user.getImage() != null) {
                        userImageView.setImageBitmap(user.getImageBitmap());
                    } else {
                        // Default user image
                        userImageView.setImageResource(R.drawable.user);
                    }

                    if (runLater != null) {
                        runLater.run();
                    }

                    refreshGameMenuVisibility();
                } catch (Exception e) {
                    Log.e(LOGGER, "Failed parsing response. Response was: " + body, e);
                    showSnack("User info is unavailable. Reason: " + e.getMessage());
                }
            }
        }
    }
}