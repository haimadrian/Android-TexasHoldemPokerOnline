package org.hit.android.haim.texasholdem.view.model.login;

import androidx.annotation.Nullable;

import lombok.Builder;
import lombok.Getter;

/**
 * Data validation state of the login form
 * @author Haim Adrian
 * @since 26-Mar-21
 */
@Builder
public class LoginFormState {
    @Getter
    @Nullable
    private final Integer topEditTextError;

    @Getter
    @Nullable
    private final Integer bottomEditTextError;

    @Getter
    private final boolean isDataValid;
}