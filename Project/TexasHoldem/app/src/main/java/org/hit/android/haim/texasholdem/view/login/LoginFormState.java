package org.hit.android.haim.texasholdem.view.login;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form
 * @author Haim Adrian
 * @since 26-Mar-21
 */
public class LoginFormState {
    @Nullable
    private final Integer topEditTextError;
    @Nullable
    private final Integer bottomEditTextError;
    private final boolean isDataValid;

    LoginFormState(@Nullable Integer topEditTextError, @Nullable Integer bottomEditTextError) {
        this.topEditTextError = topEditTextError;
        this.bottomEditTextError = bottomEditTextError;
        this.isDataValid = false;
    }

    LoginFormState(boolean isDataValid) {
        this.topEditTextError = null;
        this.bottomEditTextError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    public Integer getTopEditTextError() {
        return topEditTextError;
    }

    @Nullable
    public Integer getBottomEditTextError() {
        return bottomEditTextError;
    }

    public boolean isDataValid() {
        return isDataValid;
    }
}