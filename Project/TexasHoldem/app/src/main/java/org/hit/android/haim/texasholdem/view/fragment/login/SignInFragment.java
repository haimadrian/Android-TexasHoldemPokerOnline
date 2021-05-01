package org.hit.android.haim.texasholdem.view.fragment.login;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.model.User;
import org.hit.android.haim.texasholdem.view.activity.LoginActivity;
import org.hit.android.haim.texasholdem.view.model.login.SignInViewModel;

/**
 * The fragment that holds username and password fields, to let user sign in to the application.
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public class SignInFragment extends AbstractSignInFragment<SignInViewModel> {
    private String userId;

    public SignInFragment() {
    }

    public SignInFragment(String userId) {
        this.userId = userId;
    }

    @Override
    protected String getLogTag() {
        return "SignIn";
    }

    @Override
    protected int getGoActionErrorMessage() {
        return R.string.sign_in_failed;
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

        getBottomEditTxt().setHint(R.string.prompt_password);
        getBottomEditTxt().setAutofillHints(getString(R.string.prompt_password));
        getBottomEditTxt().setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        getBottomEditTxt().setImeActionLabel(getString(R.string.action_sign_in), 0);

        getButton().setText(R.string.action_sign_in);
        getLink().setText(R.string.action_sign_up);

        // Check if we have a valid token, to skip sign in.
        if (userId != null) {
            getTopEditText().setText(userId);
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