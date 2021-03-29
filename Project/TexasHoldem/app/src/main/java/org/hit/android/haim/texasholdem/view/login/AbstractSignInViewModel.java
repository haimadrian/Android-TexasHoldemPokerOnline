package org.hit.android.haim.texasholdem.view.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import org.hit.android.haim.texasholdem.web.UserService;

/**
 * A view model to connect between the model and view layers.<br/>
 * This class responsible for performing authentication through the model layer, and update the UI using observers.
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public abstract class AbstractSignInViewModel extends ViewModel {
    protected final MutableLiveData<LoginFormState> formStateNotifier;
    protected final MutableLiveData<LoginResult> loginResultNotifier;

    protected final String loginFailedMessage;
    protected final UserService userService;

    /**
     * Constructs a new {@link AbstractSignInViewModel}
     * @param userService A reference to {@link UserService} so we will be able to sign in
     * @param loginFailedMessage error message to use when login fails
     * @param listenerOwner The LifeCycle owner which controls the observer
     * @param formStateListener A listener to be notified upon form state changes
     * @param loginListener A listener to be notified upon login responses (success/failure)
     */
    public AbstractSignInViewModel(@NonNull UserService userService,
                                   @NonNull String loginFailedMessage,
                                   @Nullable LifecycleOwner listenerOwner,
                                   @Nullable Observer<? super LoginFormState> formStateListener,
                                   @Nullable Observer<? super LoginResult> loginListener) {
        this.userService = userService;
        this.loginFailedMessage = loginFailedMessage;
        formStateNotifier = new MutableLiveData<>();
        loginResultNotifier = new MutableLiveData<>();

        // Register listeners
        if (listenerOwner != null) {
            if (formStateListener != null) {
                formStateNotifier.observe(listenerOwner, formStateListener);
            }

            if (loginListener != null) {
                loginResultNotifier.observe(listenerOwner, loginListener);
            }
        }
    }

    /**
     * Call this method whenever there is a login data change (username or password) and you want
     * the login form state listener to be notified about validations that view model does.
     * @param topEditText The top edit text (after change) - username for sign-in, nickname for sign-up
     * @param bottomEditText The bottom edit text (after change) - password for sign-in, birth date for sign-up
     */
    public abstract void onFormDataChanged(@Nullable String topEditText, @Nullable String bottomEditText);
}