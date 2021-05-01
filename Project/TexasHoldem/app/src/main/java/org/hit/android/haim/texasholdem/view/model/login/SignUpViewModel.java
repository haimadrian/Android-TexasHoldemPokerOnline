package org.hit.android.haim.texasholdem.view.model.login;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.web.services.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * A view model to connect between the model and view layers.<br/>
 * This class responsible for signing up a user and performing authentication through the model layer, and update the UI using observers.
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public class SignUpViewModel extends AbstractSignInViewModel {
    /**
     * Constructs a new {@link SignUpViewModel}
     * @param userService A reference to {@link UserService} so we will be able to sign up
     * @param signUpFailedMessage error message to use when sign up fails
     * @param listenerOwner The LifeCycle owner which controls the observer
     * @param formStateListener A listener to be notified upon form state changes
     * @param signUpListener A listener to be notified upon sign up responses (success/failure)
     */
    public SignUpViewModel(@NonNull UserService userService,
                           @NonNull String signUpFailedMessage,
                           @Nullable LifecycleOwner listenerOwner,
                           @Nullable Observer<? super LoginFormState> formStateListener,
                           @Nullable Observer<? super LoginResult> signUpListener) {
        super(userService, signUpFailedMessage, listenerOwner, formStateListener, signUpListener);
    }

    @Override
    public void onFormDataChanged(@Nullable String nickname, @Nullable String dateOfBirth) {
        Log.d("SignUp", this.toString() + ": SignUp data changed. [dateOfBirth=" + dateOfBirth + "]");
        if (!isDateOfBirthValid(dateOfBirth)) {
            formStateNotifier.setValue(LoginFormState.builder().topEditTextError(R.string.invalid_birth_date).build());
        } else if (!isNicknameValid(nickname)) {
            formStateNotifier.setValue(LoginFormState.builder().bottomEditTextError(R.string.invalid_nickname).build());
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
     * @param nickname Nickname to use
     * @param dateOfBirth Birth date to use
     */
    public void signUp(@NonNull String username, @NonNull String password, @NonNull String nickname, @NonNull String dateOfBirth) {
        Log.d("SignUp", this.toString() + ": Signing up user to server. [username=" + username + "]");

        if (!isDateOfBirthValid(dateOfBirth)) {
            loginResultNotifier.setValue(LoginResult.builder().errorMessage(loginFailedMessage + ". Reason: Illegal date format. (Use yyyy-MM-dd)").build());
        } else if (!isNicknameValid(nickname)) {
            loginResultNotifier.setValue(LoginResult.builder().errorMessage(loginFailedMessage + ". Reason: Illegal nickname. (Use non-empty nickname)").build());
        } else {
            User user = new User(username, password.toCharArray(), nickname, LocalDate.parse(dateOfBirth), 0, null);
            userService.signUp(user).enqueue(new LoginCallbackHandler("SignUp", "Signed up successfully"));
        }
    }

    /**
     * Validates that birth date is valid. (yyyy-MM-dd)
     * @param dateOfBirth The date to validate
     * @return Whether date is valid or not
     */
    private boolean isDateOfBirthValid(@Nullable String dateOfBirth) {
        if ((dateOfBirth == null) || (dateOfBirth.trim().isEmpty())) {
            return false;
        }

        try {
            LocalDate.parse(dateOfBirth);
        } catch (DateTimeParseException ignore) {
            return false;
        }

        return true;
    }

    /**
     * Validates that nickname is valid. (Non blank)
     * @param nickname The nickname to validate
     * @return Whether nickname is valid or not
     */
    private boolean isNicknameValid(@Nullable String nickname) {
        return (nickname != null) && (!nickname.trim().isEmpty());
    }
}