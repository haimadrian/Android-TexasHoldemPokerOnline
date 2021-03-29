package org.hit.android.haim.texasholdem.view.login;

import androidx.annotation.Nullable;

import org.hit.android.haim.texasholdem.view.model.LoggedInUserView;

/**
 * Authentication result : success (user details) or error message.
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public class LoginResult {
    @Nullable
    private LoggedInUserView success;
    @Nullable
    private String error;

    LoginResult(@Nullable String error) {
        this.error = error;
    }

    LoginResult(@Nullable LoggedInUserView success) {
        this.success = success;
    }

    @Nullable
    public LoggedInUserView getSuccess() {
        return success;
    }

    @Nullable
    public String getError() {
        return error;
    }
}