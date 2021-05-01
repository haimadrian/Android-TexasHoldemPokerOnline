package org.hit.android.haim.texasholdem.view.fragment.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.view.activity.LoginActivity;
import org.hit.android.haim.texasholdem.view.model.login.AbstractSignInViewModel;
import org.hit.android.haim.texasholdem.view.model.login.LoginFormState;
import org.hit.android.haim.texasholdem.view.model.login.LoginResult;
import org.hit.android.haim.texasholdem.view.model.login.SignInViewModel;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;
import org.hit.android.haim.texasholdem.web.services.UserService;

import java.lang.reflect.Constructor;

/**
 * An abstract fragment to share common code for both sign-in and sign-up fragments.
 * @param <T> Type of the {@link ViewModel} that the derived fragment uses for updates
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public abstract class AbstractSignInFragment<T extends AbstractSignInViewModel> extends Fragment {
    /**
     * The view model that derived class works with.<br/>
     * One of {@link org.hit.android.haim.texasholdem.view.model.login.SignInViewModel} or {@link org.hit.android.haim.texasholdem.view.model.login.SignUpViewModel}
     */
    private T viewModel;

    /**
     * A progress bar to show while working
     */
    private ProgressBar loadingProgressBar;

    /**
     * A reference to the top edit text. (username for sign-in, and nickname for sign-up)
     */
    private EditText topEditText;

    /**
     * A reference to the bottom edit text. (password for sign-in, and birth date for sign-up)
     */
    private EditText bottomEditTxt;

    /**
     * The "Go" button. ("Sign In" or "Sign Up")
     */
    private Button button;

    /**
     * A navigation link to navigate to another fragment. Used by sign-in, and invisible by sign-up)
     */
    private TextView link;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(getLogTag(), this.toString() + ": onCreateView");
        return inflater.inflate(R.layout.fragment_signin, container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(getLogTag(), this.toString() + ": onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        topEditText = view.findViewById(R.id.topEditText);
        bottomEditTxt = view.findViewById(R.id.bottomEditText);
        loadingProgressBar = view.findViewById(R.id.workProgressBar);
        button = view.findViewById(R.id.goButton);
        link = view.findViewById(R.id.navigationLink);

        setEnabledToButtons(false);

        viewModel = new ViewModelProvider(this, new LoginViewModelFactory(getString(getGoActionErrorMessage()))).get(getViewModelClass());

        // Update login view model with every data modification, so we can respond to user input
        // immediately and check if there is something wrong with the input.
        TextWatcher afterTextChangedListener = new LoginTextFieldsWatcher();
        topEditText.addTextChangedListener(afterTextChangedListener);
        bottomEditTxt.addTextChangedListener(afterTextChangedListener);

        // Listen to when user presses "Done" button (keyboard)
        bottomEditTxt.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doGoActions();
            }

            return false;
        });

        // Listen to the sign in button
        button.setOnClickListener(v -> doGoActions());

        // When user presses sign up, switch to sign up fragment
        link.setOnClickListener(link -> ((LoginActivity) requireActivity()).navigateToSignUp(topEditText.getText().toString(), bottomEditTxt.getText().toString()));
    }

    /**
     * @return The tag to use while logging
     */
    protected abstract String getLogTag();

    /**
     * @return The resource identifier of a message to be used when there is something wrong while executing "Go" action (sign-in, sign-up)
     */
    protected abstract int getGoActionErrorMessage();

    /**
     * @return The view model class so we will pass it to a view model provider, for getting an instance of that class
     */
    protected abstract Class<T> getViewModelClass();

    /**
     * This method is called when user presses the "Go" button, or presses "action done" using keyboard.<br/>
     * Implementor should do the signIn / signUp actions.
     * @param topEditText The value in the top edit text
     * @param bottomEditText The value in the bottom edit text
     */
    protected abstract void executeGoActions(String topEditText, String bottomEditText);

    /**
     * When login task is ended successfully, we get here.<br/>
     * Implementor should decide what to do. Whether to go to home page (after sign in), or go to sign in (after sign up)
     * @param model The user view model to get data from and display it
     */
    protected abstract void updateUiWithUser(User model);

    /**
     * The view model that derived class works with.<br/>
     * One of {@link org.hit.android.haim.texasholdem.view.model.login.SignInViewModel} or {@link org.hit.android.haim.texasholdem.view.model.login.SignUpViewModel}
     * @return A reference to the view model
     */
    protected T getViewModel() {
        return viewModel;
    }

    /**
     * @return A reference to the top edit text. (username for sign-in, and nickname for sign-up)
     */
    protected EditText getTopEditText() {
        return topEditText;
    }

    /**
     * @return A reference to the bottom edit text. (password for sign-in, and birth date for sign-up)
     */
    protected EditText getBottomEditTxt() {
        return bottomEditTxt;
    }

    /**
     * @return The "Go" button. ("Sign In" or "Sign Up")
     */
    protected Button getButton() {
        return button;
    }

    /**
     * @return A navigation link to navigate to another fragment. Used by sign-in, and invisible by sign-up)
     */
    protected TextView getLink() {
        return link;
    }

    /**
     * @return A progress bar to show while working
     */
    protected ProgressBar getLoadingProgressBar() {
        return loadingProgressBar;
    }

    /**
     * Perform login tasks.<br/>
     * Display the progress bar and communicate with server in order to login.
     */
    private void doGoActions() {
        // Disable buttons until we finish, so user will not flood the server
        setEnabledToButtons(false);
        loadingProgressBar.setVisibility(View.VISIBLE);

        executeGoActions(topEditText.getText().toString(), bottomEditTxt.getText().toString());
    }

    /**
     * Whenever something is changed in the input (We use a TextWatcher), we get notified so
     * we can enable/disable the sign in button, and show error message if necessary.
     * @param loginFormState The new {@link LoginFormState}
     */
    private void onFormStateChanged(LoginFormState loginFormState) {
        if (loginFormState == null) {
            return;
        }

        setEnabledToButtons(loginFormState.isDataValid());

        if (loginFormState.getTopEditTextError() != null) {
            topEditText.setError(getString(loginFormState.getTopEditTextError()));
        }

        if (loginFormState.getBottomEditTextError() != null) {
            bottomEditTxt.setError(getString(loginFormState.getBottomEditTextError()));
        }
    }

    /**
     * When login tasks ended, hide the progress bar and show failure/welcome message.
     * @param loginResult The {@link LoginResult} indicates success or failure
     */
    private void onCompleted(LoginResult loginResult) {
        loadingProgressBar.setVisibility(View.GONE);

        if (loginResult == null) {
            return;
        }

        if (loginResult.getErrorMessage() != null) {
            showLoginFailed(loginResult.getErrorMessage());
        }

        if (loginResult.getUser() != null) {
            updateUiWithUser(loginResult.getUser());
        }
    }

    /**
     * Toast an error message so the user can see it and figure out what went wrong
     * @param errorMessage The error message to display
     */
    private void showLoginFailed(String errorMessage) {
        setEnabledToButtons(true);
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Enable or disable the sign in button and the sign up link
     * @param isEnabled Whether to enable or disable them
     */
    private void setEnabledToButtons(boolean isEnabled) {
        button.setEnabled(isEnabled);
        link.setEnabled(isEnabled);
    }

    /**
     * A {@link ViewModelProvider.Factory} that can initiate a {@link SignInViewModel} reference
     * to be used by a {@link ViewModelProvider} that connects between the UI and Model layers.<br/>
     * We use this mechanism for updating UI components based on model actions (sign in / sign up)
     */
    private class LoginViewModelFactory implements ViewModelProvider.Factory {
        private final String loginFailedMessage;

        public LoginViewModelFactory(String loginFailedMessage) {
            this.loginFailedMessage = loginFailedMessage;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <TT extends ViewModel> TT create(@NonNull Class<TT> modelClass) {
            try {
                Constructor<TT> ctor = modelClass.getDeclaredConstructor(UserService.class, String.class, LifecycleOwner.class, Observer.class, Observer.class);
                return ctor.newInstance(TexasHoldemWebService.getInstance().getUserService(),
                        loginFailedMessage,
                        getViewLifecycleOwner(),
                        (Observer<LoginFormState>) AbstractSignInFragment.this::onFormStateChanged,
                        (Observer<LoginResult>) AbstractSignInFragment.this::onCompleted);
            } catch (Exception e) {
                Log.e(getLogTag(), "Failed getting constructor of " + modelClass.getName(), e);
            }

            return (TT) new SignInViewModel(TexasHoldemWebService.getInstance().getUserService(),
                    loginFailedMessage,
                    getViewLifecycleOwner(),
                    AbstractSignInFragment.this::onFormStateChanged,
                    AbstractSignInFragment.this::onCompleted);
        }
    }

    /**
     * A listener that reacts to text input changes, so we can update the view model when user
     * enters text to the username or password text edits.<br/>
     * We use this listener to perform input validations that acts immediately for every input
     */
    private class LoginTextFieldsWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d(getLogTag(), "After text changed. [topEditText=" + topEditText.getText().toString() + "]");
            viewModel.onFormDataChanged(topEditText.getText().toString(), bottomEditTxt.getText().toString());
        }
    }
}
