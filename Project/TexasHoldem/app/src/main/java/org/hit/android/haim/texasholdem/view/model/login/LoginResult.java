package org.hit.android.haim.texasholdem.view.model.login;

import androidx.annotation.Nullable;

import org.hit.android.haim.texasholdem.model.User;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Authentication result : success (user details) or error message.
 * @author Haim Adrian
 * @since 26-Mar-21
 */
@ToString
@Builder
public class LoginResult {
    /**
     * Indicates a successful result
     */
    @Getter
    @Nullable
    private final User user;

    /**
     * Indicates an error result
     */
    @Getter
    @Nullable
    private final String errorMessage;
}