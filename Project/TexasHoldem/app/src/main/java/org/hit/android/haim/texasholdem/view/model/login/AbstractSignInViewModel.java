package org.hit.android.haim.texasholdem.view.model.login;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.fasterxml.jackson.databind.JsonNode;

import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;
import org.hit.android.haim.texasholdem.web.services.UserService;

import lombok.Data;
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
public abstract class AbstractSignInViewModel extends ViewModel {
    protected final MutableLiveData<LoginFormState> formStateNotifier;
    protected final MutableLiveData<LoginResult> loginResultNotifier;

    protected final UserService userService;

    /**
     * Constructs a new {@link AbstractSignInViewModel}
     * @param userService A reference to {@link UserService} so we will be able to sign in
     * @param listenerOwner The LifeCycle owner which controls the observer
     * @param formStateListener A listener to be notified upon form state changes
     * @param loginListener A listener to be notified upon login responses (success/failure)
     */
    public AbstractSignInViewModel(@NonNull UserService userService,
                                   @Nullable LifecycleOwner listenerOwner,
                                   @Nullable Observer<? super LoginFormState> formStateListener,
                                   @Nullable Observer<? super LoginResult> loginListener) {
        this.userService = userService;
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

    /**
     * A callback handler which is used in order to handle the response of a signin / signup request.<br/>
     * Derived classes use it.
     */
    @Data
    protected class LoginCallbackHandler implements Callback<JsonNode> {
        private final String logTag;
        private final String successLogMessage;

        @Override
        @EverythingIsNonNull
        public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
            LoginResult loginResult;

            if (!response.isSuccessful()) {
                String errorMessage = TexasHoldemWebService.getInstance().readHttpErrorResponse(response);
                loginResult = LoginResult.builder().errorMessage(errorMessage).build();
            } else {
                JsonNode body = response.body();
                try {
                    User userInfo = TexasHoldemWebService.getInstance().getObjectMapper().readValue(body.toString(), User.class);
                    Log.d(logTag, successLogMessage + ": [" + userInfo + "]");
                    loginResult = LoginResult.builder().user(userInfo).build();
                } catch (Exception e) {
                    Log.e(logTag, "Failed parsing response. Response was: " + body, e);
                    loginResult = LoginResult.builder().errorMessage(e.getMessage()).build();
                }
            }

            loginResultNotifier.setValue(loginResult);
        }

        @Override
        @EverythingIsNonNull
        public void onFailure(Call<JsonNode> call, Throwable t) {
            Log.d(logTag, "Error has occurred while trying to " + logTag + ": " + t.getMessage());
            loginResultNotifier.setValue(LoginResult.builder().errorMessage(t.getMessage()).build());
        }
    }
}