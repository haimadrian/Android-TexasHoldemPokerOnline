package org.hit.android.haim.texasholdem.view.model.login;

import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.fasterxml.jackson.databind.JsonNode;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;
import org.hit.android.haim.texasholdem.web.services.UserService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

/**
 * A view model to connect between the model and view layers.<br/>
 * This class responsible for performing authentication through the model layer, and update the UI using observers.
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public class SignInViewModel extends AbstractSignInViewModel {
    private static final String LOGGER = SignInViewModel.class.getSimpleName();

    /**
     * Constructs a new {@link SignInViewModel}
     * @param userService A reference to {@link UserService} so we will be able to sign in
     * @param listenerOwner The LifeCycle owner which controls the observer
     * @param formStateListener A listener to be notified upon form state changes
     * @param loginListener A listener to be notified upon login responses (success/failure)
     */
    public SignInViewModel(@NonNull UserService userService,
                           @Nullable LifecycleOwner listenerOwner,
                           @Nullable Observer<? super LoginFormState> formStateListener,
                           @Nullable Observer<? super LoginResult> loginListener) {
        super(userService, listenerOwner, formStateListener, loginListener);
    }

    @Override
    public void onFormDataChanged(@Nullable String username, @Nullable String password) {
        if (!isUserNameValid(username)) {
            formStateNotifier.setValue(LoginFormState.builder().topEditTextError(R.string.invalid_username).build());
        } else if (!isPasswordValid(password)) {
            formStateNotifier.setValue(LoginFormState.builder().bottomEditTextError(R.string.invalid_password).build());
        } else {
            formStateNotifier.setValue(LoginFormState.builder().isDataValid(true).build());
        }
    }

    /**
     * Perform user authentication using specified username and password.<br/>
     * This method will use {@link UserService} in order to authenticate the user in front of the server<br/>
     * We will notify the registered login listener once we finish, with a success or fail response.
     * @param username Username to use
     * @param password Password to use
     */
    public void login(@NonNull String username, @NonNull String password) {
        Log.d(LOGGER, this.toString() + ": Authenticating user in front of server. [username=" + username + "]");

        User user = new User(username, password.toCharArray());

        userService.signIn(user).enqueue(new Callback<JsonNode>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
                LoginResult loginResult = null;

                if (!response.isSuccessful()) {
                    String errorMessage = TexasHoldemWebService.getInstance().readHttpErrorResponse(response);
                    loginResult = LoginResult.builder().errorMessage(errorMessage).build();
                } else {
                    JsonNode body = response.body();
                    try {
                        UserService.JwtTokenResponse jwtTokenResponse = TexasHoldemWebService.getInstance().getObjectMapper().readValue(body.toString(), UserService.JwtTokenResponse.class);
                        if ((jwtTokenResponse != null) && (jwtTokenResponse.getToken() != null)) {
                            Log.d(LOGGER, "Server responded with a valid JWT token: " + jwtTokenResponse.getToken());
                            TexasHoldemWebService.getInstance().setJwtToken(jwtTokenResponse.getToken());
                            fastLogin(user.getId());
                        } else {
                            Log.e(LOGGER, "Server has not responded with a valid JWT token");
                            loginResult = LoginResult.builder().errorMessage("Server returned invalid token").build();
                        }
                    } catch (IOException e) {
                        Log.e(LOGGER, "Failed parsing response. Response was: " + body, e);
                        loginResult = LoginResult.builder().errorMessage(e.getMessage()).build();
                    }
                }

                if (loginResult != null) {
                    loginResultNotifier.setValue(loginResult);
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<JsonNode> call, Throwable t) {
                Log.d(LOGGER, "Error has occurred while trying to sign in: " + t.getMessage());
                loginResultNotifier.setValue(LoginResult.builder().errorMessage(t.getMessage()).build());
            }
        });
    }

    /**
     * Use this method for fast sign-in (skip sign in) and get user info.<br/>
     * We have this method so we can keep JWT as shared preferences, and sign-in the user automatically
     * instead of forcing him to enter credentials every time the app is launched.
     * @param userId The user identifier to get info for
     */
    public void fastLogin(@NonNull String userId) {
        TexasHoldemWebService.getInstance().setLoggedInUserId(userId);

        // When we sign in, we do not have display name and coins. We only get an JWT.
        // So here we get user info.
        userService.getUserInfo(userId).enqueue(new LoginCallbackHandler("SignIn", "User info received from server"));
    }

    /**
     * Validates that user name is valid.<br/>
     * A user name is valid if it is an email address only.
     * @param username The user name to validate
     * @return Whether user name is valid or not
     */
    private boolean isUserNameValid(@Nullable String username) {
        if (username == null) {
            return false;
        }

        return !username.trim().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(username).matches();
    }

    /**
     * Validates that password is valid.<br/>
     * A password is valid if it is not null or empty.
     * @param password The password to validate
     * @return Whether password is valid or not
     */
    private boolean isPasswordValid(@Nullable String password) {
        return password != null && password.trim().length() > 5;
    }
}