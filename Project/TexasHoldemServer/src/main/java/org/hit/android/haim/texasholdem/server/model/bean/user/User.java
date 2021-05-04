package org.hit.android.haim.texasholdem.server.model.bean.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;

/**
 * Interface representing a user model in the application.
 *
 * @author Haim Adrian
 * @since 21-Mar-21
 */
@JsonDeserialize(as = UserImpl.class) // Deserialize it as UserImpl rather than UserDBImpl
public interface User {
    /**
     * @return User identifier. This is the email address a user is registered with
     */
    String getId();

    /**
     * @return Display name of the user, so we will use it at UI instead of email address.
     */
    String getName();

    /**
     * @return Date of birth of this user
     */
    LocalDate getDateOfBirth();

    /**
     * @return How many coins this user has earned
     */
    long getCoins();

    /**
     * @return Profile image of the user
     */
    byte[] getImage();
}

