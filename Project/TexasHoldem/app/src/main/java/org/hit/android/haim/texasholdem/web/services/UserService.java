package org.hit.android.haim.texasholdem.web.services;

import com.fasterxml.jackson.databind.JsonNode;

import org.hit.android.haim.texasholdem.model.User;

import java.util.List;

import lombok.Getter;
import lombok.ToString;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Lists all restful web services related to UserController at Texas Holdem backend
 * @author Haim Adrian
 * @since 15-Apr-21
 * @see org.hit.android.haim.texasholdem.web.TexasHoldemWebService
 */
public interface UserService {
    /**
     * Use this method for signing up a user.<br/>
     * Note that user must have all of its fields set. See {@link User}<br/>
     * In case user is already signed up, or there is a wrong input, an error response will be received.
     * @param user The user to sign up
     * @return {@link User} or Error
     */
    @PUT("/user/signup")
    Call<JsonNode> signUp(@Body User user);

    /**
     * Use this method for signing in a user.<br/>
     * Note that user must have its id and pwd fields set. See {@link User#User(String, char[])}<br/>
     * We must use this method cause this is the way to get a JWT token.
     * @param user The user to sign in
     * @return {@link JwtTokenResponse} or Error
     */
    @POST("/user/signin")
    Call<JsonNode> signIn(@Body User user);

    /**
     * Use this method for signing out a user.<br/>
     * Remember to reset the JWT token, so no request will succeed after signing out.
     * @return String or Error
     */
    @PUT("/user/signout")
    Call<JsonNode> signOut();

    /** @return User or Error */
    @GET("/user/{userId}/info")
    Call<JsonNode> getUserInfo(@Path("userId") String userId);

    /** @return {@code List<User>} or Error */
    @GET("/user/info")
    Call<JsonNode> getUsersInfo(@Body List<String> usersId);

    /**
     * Update the amount of coins a user have.
     * @return User or Error
     */
    @POST("/user/{userId}/coins")
    Call<JsonNode> updateCoins(@Path("userId") String userId, @Body User user);

    /**
     * Update the profile picture of a user.
     * @return User or Error
     */
    @POST("/user/{userId}/image")
    Call<JsonNode> updateImage(@Path("userId") String userId, @Body User user);

    /**
     * A response of signin API.<br/>
     * This is a jwt token received from server in order to authorize the client after signing in.
     */
    @ToString
    class JwtTokenResponse {
        @Getter
        private String token;
    }
}
