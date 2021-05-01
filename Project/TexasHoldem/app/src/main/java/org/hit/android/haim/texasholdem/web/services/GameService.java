package org.hit.android.haim.texasholdem.web.services;

import com.fasterxml.jackson.databind.JsonNode;

import org.hit.android.haim.texasholdem.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Lists all restful web services related to GameController at Texas Holdem backend
 * @author Haim Adrian
 * @since 29-Apr-21
 * @see org.hit.android.haim.texasholdem.web.TexasHoldemWebService
 */
public interface GameService {
    /** @return User or Error */
    @PUT("/user/connect")
    Call<JsonNode> connect(@Body User user);

    /** @return String or Error */
    @PUT("/user/disconnect/{userId}")
    Call<JsonNode> disconnect(@Path("userId") String userId);

    /** @return User or Error */
    @GET("/user/info/{userId}")
    Call<JsonNode> getUserInfo(@Path("userId") String userId);

    /** @return {@code List<User>} or Error */
    @GET("/user/info")
    Call<JsonNode> getUsersInfo(@Body List<String> usersId);
}
