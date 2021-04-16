package org.hit.android.haim.chat.client.web;

import com.fasterxml.jackson.databind.JsonNode;

import org.hit.android.haim.chat.client.bean.Channel;
import org.hit.android.haim.chat.client.bean.User;

import java.time.LocalDateTime;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * @author Haim Adrian
 * @since 15-Apr-21
 */
public interface APIInterface {
    /* ***************************** User Controller ***************************** */
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


    /* ***************************** Channel Controller ***************************** */
    /** @return {@code List<Channel>} or Error */
    @GET("/channel")
    Call<JsonNode> getAllChannels();

    /** @return Channel or Error */
    @GET("/channel/{channelName}")
    Call<JsonNode> getChannel(@Path("channelName") String channelName);

    /** @return Channel or Error */
    @POST("/channel")
    Call<JsonNode> createChannel(@Body Channel channel);

    /** @return "" or Error */
    @DELETE("/channel/{channelName}")
    Call<JsonNode> deleteChannel(@Path("channelName") String channelName);


    /* ***************************** Message Controller ***************************** */
    /** @return Message or Error */
    @POST("/message/{channelName}/{userId}")
    Call<JsonNode> sendMessage(@Path("channelName") String channelName, @Path("userId") String userId, @Body String message);

    /** @return {@code List<Message>} or Error */
    @GET("/message/{channelName}")
    Call<JsonNode> getAllMessagesInChannel(@Path("channelName") String channelName);

    /** @return {@code List<Message>} or Error */
    @GET("/message/{channelName}/since/{lastMessageDateTime}")
    Call<JsonNode> getLatestMessagesInChannel(@Path("channelName") String channelName, @Path("lastMessageDateTime") LocalDateTime lastMessageDateTime);

    /** @return Message or Error */
    @GET("/message/info/{messageId}")
    Call<JsonNode> getMessageInfo(@Path("userId") String messageId);

    /** @return {@code List<Message>} or Error */
    @GET("/message/info")
    Call<JsonNode> getMessagesInfo(@Body List<String> messagesId);
}
