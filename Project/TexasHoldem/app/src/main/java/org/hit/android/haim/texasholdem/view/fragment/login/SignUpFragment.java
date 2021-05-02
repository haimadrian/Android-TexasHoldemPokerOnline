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
import org.hit.android.haim.texasholdem.view.model.login.SignUpViewModel;

/**
 * The fragment that holds nickname and date of birth fields, to let user sign up to the application.<br/>
 * We get here from {@link SignInFragment}, which passes us the username and password.
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public class SignUpFragment extends AbstractSignInFragment<SignUpViewModel> {
    private String username;
    private String password;

    public SignUpFragment() {

    }

    public SignUpFragment(String userName, String password) {
        this.username = userName;
        this.password = password;
    }

    @Override
    protected String getLogTag() {
        return "SignUp";
    }

    @Override
    protected int getGoActionErrorMessage() {
        return R.string.sign_up_failed;
    }

    @Override
    protected Class<SignUpViewModel> getViewModelClass() {
        return SignUpViewModel.class;
    }

    @Override
    protected void executeGoActions(String nickname, String dateOfBirth) {
        getViewModel().signUp(username, password, nickname, dateOfBirth);
    }

    @Override
    protected void updateUiWithUser(User model) {
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), "Successfully signed up. Please sign in", Toast.LENGTH_LONG).show();
        }

        // Successfully signed up,
        if (getActivity() != null) {
            ((LoginActivity)getActivity()).navigateToSignIn();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getTopEditText().setHint(R.string.prompt_nickname);
        getTopEditText().setAutofillHints(getString(R.string.prompt_nickname));
        getTopEditText().setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME);
        getTopEditText().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_face_24, 0, 0, 0);

        getBottomEditTxt().setHint(R.string.prompt_date_of_birth);
        getBottomEditTxt().setAutofillHints(getString(R.string.prompt_date_of_birth_auto_fill));
        getBottomEditTxt().setInputType(EditorInfo.TYPE_CLASS_DATETIME | EditorInfo.TYPE_DATETIME_VARIATION_DATE);
        getBottomEditTxt().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_date_range_24, 0, 0, 0);
        getBottomEditTxt().setImeActionLabel(getString(R.string.action_sign_up), 0);

        getButton().setText(R.string.action_sign_up);
        getLink().setVisibility(View.GONE); // Hide the sign up link, as we are already in sign up view.
    }
}