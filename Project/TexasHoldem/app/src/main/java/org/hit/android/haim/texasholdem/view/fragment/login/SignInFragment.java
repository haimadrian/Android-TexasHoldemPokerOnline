package org.hit.android.haim.texasholdem.view.fragment.login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.view.activity.LoginActivity;
import org.hit.android.haim.texasholdem.view.model.login.SignInViewModel;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;

/**
 * The fragment that holds username and password fields, to let user sign in to the application.
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public class SignInFragment extends AbstractSignInFragment<SignInViewModel> {
    private static final String LOGGER = SignInFragment.class.getSimpleName();

    private String userId;
    private boolean isAlreadyCreated;

    public SignInFragment() {
    }

    public SignInFragment(String userId) {
        this.userId = userId;
    }

    @Override
    protected String getLogTag() {
        return LOGGER;
    }

    @Override
    protected Class<SignInViewModel> getViewModelClass() {
        return SignInViewModel.class;
    }

    @Override
    protected void executeGoActions(String username, String password) {
        getViewModel().login(username, password);
    }

    @Override
    protected void updateUiWithUser(User model) {
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), String.format(getString(R.string.welcome), model.getName()), Toast.LENGTH_LONG).show();
        }

        // Successfully signed in. Go to home page
        if (getActivity() != null) {
            ((LoginActivity)getActivity()).userSignedInSuccessfully(model);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getTopEditText().setHint(R.string.prompt_username);
        getTopEditText().setAutofillHints(getString(R.string.prompt_username));
        getTopEditText().setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME);
        getTopEditText().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_email_24, 0, 0, 0);

        getBottomEditText().setHint(R.string.prompt_password);
        getBottomEditText().setAutofillHints(getString(R.string.prompt_password));
        getBottomEditText().setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        getBottomEditText().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_lock_24, 0, 0, 0);
        getBottomEditText().setImeActionLabel(getString(R.string.action_sign_in), 0);
        getBottomEditText().setLongClickable(true); // Re-enable paste (Disabled by SignUpFragment)
        getBottomEditText().setFocusable(true); // Enable focus (Disabled by SignUpFragment)
        getBottomEditText().setOnClickListener(null);

        getButton().setText(R.string.action_sign_in);
        getLink().setText(R.string.action_sign_up);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Call it here because listener is not notified during construction, so it is leaving buttons
        // enabled even though the state is illegal.
        setEnabledToButtons(false);

        // Check if we have a valid token, to skip sign in.
        if (userId != null) {
            Log.d(LOGGER, "isAlreadyCreated=" + isAlreadyCreated);
            getTopEditText().setText(userId);

            // Do not sign in automatically when the fragment is reconstructed. e.g. when user presses back button
            // Note that we might get here whenever the server responds with 401 unauthorized. This is handled by MainActivity which sets
            // the JWT token to null. Hence we make sure JWT differs from null, to be able to fast login.
            if ((!isAlreadyCreated) && (TexasHoldemWebService.getInstance().getJwtToken() != null)) {
                isAlreadyCreated = true;
                if (getContext() != null && getContext().getApplicationContext() != null) {
                    Toast.makeText(getContext().getApplicationContext(), getString(R.string.signing_in), Toast.LENGTH_SHORT).show();
                }

                getLoadingProgressBar().setVisibility(View.VISIBLE);

                // Set the token, so it will be used as header.
                // In case it is invalid, server will respond with error 401 unauthorized.
                getViewModel().fastLogin(userId);
            }
        }
    }
}