package org.hit.android.haim.texasholdem.web;

import android.util.Log;

import org.hit.android.haim.texasholdem.model.User;

import java.time.LocalDate;

/**
 * Responsible for performing User requests
 * <ul>
 *     <li>Sign Up</li>
 *     <li>Sign In</li>
 *     <li>Sign Out</li>
 *     <li>Get User Info</li>
 * </ul>
 * @author Haim Adrian
 * @since 23-Mar-2020
 */
public class UserService {
    private String userId = "guest";

    // Hide ctor. Access this class through MapStoriesService.
    UserService() {

    }

    /**
     * Use this method for signing up a user.<br/>
     * Note that user must have all of its fields set. See {@link User#User(String, char[], String, LocalDate, long)}<br/>
     * In case user is already signed up, or there is a wrong input, an IOException will be thrown, so handle it.
     * @param user The user to sign up
     */
    public void signUp(User user, ResponseConsumer<User> responseConsumer) {
        userId = user.getId();
        TexasHoldemWebService.getInstance().put("user/signup", false, user, User.class, responseConsumer);
    }

    /**
     * Use this method for signing in a user.<br/>
     * Note that user must have its id and pwd fields set. See {@link User#User(String, char[])}<br/>
     * We must use this method cause this is the way to get a JWT token.
     * @param user The user to sign in
     */
    public void signIn(User user, VoidResponseConsumer responseHandler) {
        userId = user.getId();
        TexasHoldemWebService.getInstance().post("user/signin", false, user, JwtTokenResponse.class, new ResponseConsumer<JwtTokenResponse>() {
            @Override
            public void onSuccess(JwtTokenResponse response) {
                if ((response != null) && (response.token != null)) {
                    TexasHoldemWebService.getInstance().setJwtToken(response.token);
                } else {
                    Log.e("WEB", "Server has not responded with a valid JWT token");
                }

                if (responseHandler != null) {
                    responseHandler.onSuccess();
                }
            }

            @Override
            public void onError(Throwable t) {
                if (responseHandler != null) {
                    responseHandler.onError(t);
                }
            }
        });
    }

    /**
     * Use this method for signing out a user.<br/>
     * This method will reset the JWT token, so no request will succeed after signing out.
     */
    public void signOut() {
        TexasHoldemWebService.getInstance().put("user/signout", true, "", String.class, new ResponseConsumer<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d("WEB", "Server responded with: " + response);
            }

            @Override
            public void onError(Throwable t) {
                Log.e("WEB", "Error has occurred while signing out", t);
            }
        });

        TexasHoldemWebService.getInstance().setJwtToken(null); // Nothing will be authorized after sign out.
    }

    /**
     * Use this method to get user info by identifier
     */
    public void getUserInfo(String userId, ResponseConsumer<User> responseHandler) {
        TexasHoldemWebService.getInstance().get("user/info/" + userId, true, User.class, responseHandler);
    }

    /**
     * @return The user identifier of connected user, to be used when there is a need to pull data related to this user
     */
    public String getUserId() {
        return userId;
    }

    public static class JwtTokenResponse {
        private String token;
    }
}
