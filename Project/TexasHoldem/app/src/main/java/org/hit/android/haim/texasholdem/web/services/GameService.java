package org.hit.android.haim.texasholdem.web.services;

import com.fasterxml.jackson.databind.JsonNode;

import org.hit.android.haim.texasholdem.common.model.bean.game.GameSettings;
import org.hit.android.haim.texasholdem.common.model.bean.game.Player;
import org.hit.android.haim.texasholdem.common.model.bean.game.PlayerAction;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Lists all restful web services related to GameController at Texas Holdem backend
 * @author Haim Adrian
 * @since 29-Apr-21
 * @see org.hit.android.haim.texasholdem.web.TexasHoldemWebService
 */
public interface GameService {
    /** @return String (game hash) or Error */
    @POST("/game/new")
    Call<JsonNode> createGame(@Body GameSettings gameSettings);

    /** @return void (200 OK) or Error */
    @PUT("/game/{gameHash}/start")
    Call<JsonNode> startGame(@Path("gameHash") String gameHash);

    /** @return void (200 OK) or Error */
    @PUT("/game/{gameHash}/stop")
    Call<JsonNode> stopGame(@Path("gameHash") String gameHash);

    /** @return String (game hash) or Error */
    @GET("/game/leader/hash")
    Call<JsonNode> getMyGameHash();

    /** @return GameEngine or Error */
    @GET("/game/mygame")
    Call<JsonNode> getMyGame();

    /** @return void (200 OK) or Error */
    @PUT("/game/{gameHash}/join")
    Call<JsonNode> joinGame(@Path("gameHash") String gameHash, @Body Player player);

    /** @return void (200 OK) or Error */
    @PUT("/game/{gameHash}/leave")
    Call<JsonNode> leaveGame(@Path("gameHash") String gameHash);

    /** @return {@code Set<Player>} or Error */
    @GET("/game/{gameHash}/players")
    Call<JsonNode> getPlayers(@Path("gameHash") String gameHash);

    /** @return ClientGameEngine or Error */
    @GET("/game/{gameHash}/info")
    Call<JsonNode> getGameInfo(@Path("gameHash") String gameHash);

    /** @return void (200 OK) or Error */
    @PUT("/game/{gameHash}/action")
    Call<JsonNode> executePlayerAction(@Path("gameHash") String gameHash, @Body PlayerAction playerAction);
}
