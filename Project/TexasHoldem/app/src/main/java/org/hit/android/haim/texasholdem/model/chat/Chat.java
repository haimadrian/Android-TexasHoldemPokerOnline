package org.hit.android.haim.texasholdem.model.chat;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;

import org.hit.android.haim.texasholdem.common.model.bean.chat.Channel;
import org.hit.android.haim.texasholdem.common.model.bean.chat.Message;
import org.hit.android.haim.texasholdem.common.util.CustomThreadFactory;
import org.hit.android.haim.texasholdem.web.TexasHoldemWebService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The chat class is responsible for chat management.<br/>
 * There is a single instance of this class, as there is a single chat. In this class
 * we have a timer which updates the chat with messages received from server.<br/>
 * We have a listener that we notify to when new messages arrive, so the UI can be updated
 * and show new message count using badge drawable.<br/>
 * The state of this class depends on user selection. If the game is not on network (AI), then
 * there is no chat and we will not launch any resource consuming job in background.
 * That means the chat is relevant for network games only!
 *
 * @author Haim Adrian
 * @since 12-Jun-21
 */
public class Chat {
    private static final String LOGGER = Chat.class.getSimpleName();

    private final Set<ChatListener> listeners;

    /**
     * Chat identifier (game hash) that we use in order to get messages for, from server.
     */
    private final String chatId;

    /**
     * A scheduler that runs every one second, to refresh chat's messages
     */
    private ScheduledExecutorService executor;

    /**
     * All messages in this chat, sorted by time from oldest to newest
     */
    @Getter
    private final List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    /**
     * Reference to channel info. We keep updating it during {@link #refresh()}
     */
    private final AtomicReference<Channel> channelInfo = new AtomicReference<>();

    /**
     * Constructs a new {@link Chat}
     * @param chatId Identifier to use for server requests
     */
    public Chat(String chatId) {
        this.chatId = chatId;
        listeners = new HashSet<>(2);
    }

    /**
     * Start the chat service, such that we will ask the server for updates.
     */
    public void start() {
        Log.i(LOGGER, "Starting chat with id: " + chatId);
        scheduleRefresh();
    }

    /**
     * Stop the chat service, such that we will not longer ask the server for updates.
     */
    public void stop() {
        Log.i(LOGGER, "Stopping chat");
        stopExecutor();
    }

    /**
     * Register a new {@link ChatListener}
     * @param listener The listener
     */
    public void addChatListener(ChatListener listener) {
        listeners.add(listener);
    }

    /**
     * Deregister a {@link ChatListener}
     * @param listener The listener
     */
    public void removeChatListener(ChatListener listener) {
        listeners.remove(listener);
    }

    /**
     * Start a scheduler that runs every 1 second and updates ({@link #refresh()}) messages
     */
    private void scheduleRefresh() {
        stopExecutor();

        executor = Executors.newSingleThreadScheduledExecutor(new CustomThreadFactory("ChatRefreshThread"));
        executor.scheduleWithFixedDelay(Chat.this::refresh, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Stop a previously started scheduler.
     * @see #scheduleRefresh()
     */
    private void stopExecutor() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    /**
     * Go over all listeners and notify them about a message that has been arrived from server
     * @param message The new message to notify about
     */
    private void notifyMessageArrived(Message message) {
        for (ChatListener listener : listeners) {
            listener.onMessageArrived(message);
        }
    }

    /**
     * Go over all listeners and notify them about a failure
     * @param errorMessage The error message to notify about
     */
    private void notifyError(String errorMessage) {
        for (ChatListener listener : listeners) {
            listener.onChatError(errorMessage);
        }
    }

    /**
     * Refresh the chat with ALL or LATEST messages.<br/>
     * In case the chat is empty, we will download all messages from server. Otherwise,
     * we will download recent messages, based on the last message we have locally.
     */
    private void refresh() {
        Response<JsonNode> response = null;

        try {
            if (!messages.isEmpty()) {
                Message lastMessage = messages.get(messages.size() - 1);
                response = TexasHoldemWebService.getInstance().getChatService().getLatestMessagesInChannel(chatId, lastMessage.getDateTimeSent()).execute();
            } else {
                response = TexasHoldemWebService.getInstance().getChatService().getAllMessagesInChannel(chatId).execute();
            }
        } catch (Exception e) {
            Log.e(LOGGER, "Error has occurred while trying to read messages", e);
        }

        if ((response == null) || !response.isSuccessful()) {
            notifyError("Failed getting messages");
        } else {
            try {
                ObjectReader reader = TexasHoldemWebService.getInstance().getObjectMapper().readerFor(new TypeReference<List<Message>>() {});
                List<Message> newMessages = reader.readValue(response.body().toString());
                messages.addAll(newMessages);
                newMessages.forEach(Chat.this::notifyMessageArrived);
            } catch (JsonProcessingException e) {
                Log.e(LOGGER, "Error has occurred while trying to read message", e);
            }
        }

        try {
            Response<JsonNode> channelInfoResponse = TexasHoldemWebService.getInstance().getChatService().getChannel(chatId).execute();
            if (!channelInfoResponse.isSuccessful()) {
                Log.e(LOGGER, "Error has occurred while trying to read channel info.");
            } else {
                try {
                    channelInfo.set(TexasHoldemWebService.getInstance().getObjectMapper().readValue(channelInfoResponse.body().toString(), Channel.class));
                } catch (JsonProcessingException e) {
                    Log.e(LOGGER, "Error has occurred while trying to read channel", e);
                }
            }
        } catch (Exception e) {
            Log.e(LOGGER, "Error has occurred while trying to read channel info", e);
        }
    }

    /**
     * @return Reference to channel info. We keep updating it during {@link #refresh()}
     */
    public Channel getChannelInfo() {
        return channelInfo.get();
    }

    /**
     * Sends a new message to server
     * @param userId User sending the message (This is the logged in user)
     * @param message Message to send
     * @param errorConsumer A consumer which will get notified in case of failures
     */
    public void sendMessage(String userId, String message, Consumer<String> errorConsumer) {
        TexasHoldemWebService.getInstance().getChatService().sendMessage(chatId, userId, message).enqueue(new Callback<JsonNode>() {
            @Override
            public void onResponse(@NotNull Call<JsonNode> call, @NotNull Response<JsonNode> response) {
                if (!response.isSuccessful()) {
                    errorConsumer.accept("Failed to send message. Try again");
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonNode> call, @NotNull Throwable t) {
                errorConsumer.accept("Failed to send message. Reason: " + t.getMessage());
            }
        });
    }

    /**
     * Implement this interface in order to be notified when a game is played on network, and
     * new messages are arrived.
     */
    public interface ChatListener {
        /**
         * This event will be raised for every single message that arrives to the chat, including
         * messages sent by this client.
         * @param message The message with all of its details, ready to be displayed in the chat fragment
         */
        void onMessageArrived(Message message);

        /**
         * This event will be raised in case there was some failure while we were trying to
         * get new messages from server.
         * @param errorMessage The error message to show to user
         */
        void onChatError(String errorMessage);
    }
}
