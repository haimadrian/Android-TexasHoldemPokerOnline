package org.hit.android.haim.texasholdem.view.login;

import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.view.model.LoggedInUserView;
import org.hit.android.haim.texasholdem.web.ResponseConsumer;
import org.hit.android.haim.texasholdem.web.UserService;
import org.hit.android.haim.texasholdem.web.VoidResponseConsumer;

/**
 * A view model to connect between the model and view layers.<br/>
 * This class responsible for performing authentication through the model layer, and update the UI using observers.
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public class SignInViewModel extends AbstractSignInViewModel {
    /**
     * Constructs a new {@link SignInViewModel}
     * @param userService A reference to {@link UserService} so we will be able to sign in
     * @param loginFailedMessage error message to use when login fails
     * @param listenerOwner The LifeCycle owner which controls the observer
     * @param formStateListener A listener to be notified upon form state changes
     * @param loginListener A listener to be notified upon login responses (success/failure)
     */
    public SignInViewModel(@NonNull UserService userService,
                           @NonNull String loginFailedMessage,
                           @Nullable LifecycleOwner listenerOwner,
                           @Nullable Observer<? super LoginFormState> formStateListener,
                           @Nullable Observer<? super LoginResult> loginListener) {
        super(userService, loginFailedMessage, listenerOwner, formStateListener, loginListener);
    }

    @Override
    public void onFormDataChanged(@Nullable String username, @Nullable String password) {
        Log.d("SignIn", this.toString() + ": Login data changed. [username=" + username + "]");
        if (!isUserNameValid(username)) {
            formStateNotifier.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            formStateNotifier.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            formStateNotifier.setValue(new LoginFormState(true));
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
        Log.d("SignIn", this.toString() + ": Authenticating user in front of server. [username=" + username + "]");

        User user = new User(username, password.toCharArray());

        userService.signIn(user, new VoidResponseConsumer() {
            @Override
            public void onSuccess() {
                fastLogin(user.getId());
            }

            @Override
            public void onError(Throwable thrown) {
                Log.d("Login", "Error has occurred while trying to sign in: " + thrown.getMessage());
                loginResultNotifier.setValue(new LoginResult(loginFailedMessage + ". Reason: " + thrown.getMessage()));
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
        // When we sign in, we do not have display name and coins. We only get an JWT.
        // So here we get user info.
        userService.getUserInfo(userId, new ResponseConsumer<User>() {
            @Override
            public void onSuccess(User userInfo) {
                Log.d("Login", "User info received from server: " + userInfo + "]");
                loginResultNotifier.setValue(new LoginResult(new LoggedInUserView(userInfo.getId(), userInfo.getName(), userInfo.getDateOfBirth(), userInfo.getCoins())));
            }

            @Override
            public void onError(Throwable thrown) {
                Log.d("Login", "Error has occurred while trying to get user info: " + thrown.getMessage());
                loginResultNotifier.setValue(new LoginResult(loginFailedMessage + ". Reason: " + thrown.getMessage()));
            }
        });
    }

    /**
     * Validates that user name is valid.<br/>
     * A user name is valid if it is an email address, or if it is not empty.
     * @param username The user name to validate
     * @return Whether user name is valid or not
     */
    private boolean isUserNameValid(@Nullable String username) {
        if (username == null) {
            return false;
        }

        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        }

        return !username.trim().isEmpty();
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