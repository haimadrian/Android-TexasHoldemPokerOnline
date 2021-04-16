package org.hit.android.haim.chat.client.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.android.material.snackbar.Snackbar;

import org.hit.android.haim.chat.client.R;
import org.hit.android.haim.chat.client.activity.MainActivity;
import org.hit.android.haim.chat.client.bean.Channel;
import org.hit.android.haim.chat.client.bean.Message;
import org.hit.android.haim.chat.client.web.ChatWebService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FlipInBottomXAnimator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FragmentChannel extends Fragment {
    private static final String STORED_CHANNEL_NAME_KEY = "channelName";

    private String channelName;

    private TextView textViewChannelName;
    private TextView textViewParticipants;
    private EditText editTextMessage;

    private MessageCardAdapter messageCardAdapter;
    private MainActivity mainActivity;
    private View fragmentView;

    private ScheduledExecutorService executor;
    private RecyclerView messagesRecyclerView;

    public FragmentChannel() {
        this(null);
    }

    public FragmentChannel(String channelName) {
        Log.d("Lifecycle", this.toString() + ".new");
        this.channelName = channelName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Lifecycle", this.toString() + ".onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null) && savedInstanceState.containsKey(STORED_CHANNEL_NAME_KEY)) {
            channelName = savedInstanceState.getString(STORED_CHANNEL_NAME_KEY);
        }

        return inflater.inflate(R.layout.fragment_channel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentView = view;
        textViewChannelName = view.findViewById(R.id.textViewChannelNameChannel);
        textViewParticipants = view.findViewById(R.id.textViewChannelParticipantsChannel);
        editTextMessage = view.findViewById(R.id.editTextMessageChannel);

        mainActivity = (MainActivity) getActivity();

        if (channelName != null) {
            textViewChannelName.setText(channelName);
        }

        ImageView imageViewSend = view.findViewById(R.id.imageViewSend);
        imageViewSend.setOnClickListener(this::onSendButtonClicked);

        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false);
        //layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setItemAnimator(new FlipInBottomXAnimator(new OvershootInterpolator()));

        ChatWebService.getInstance().getApi().getAllMessagesInChannel(channelName).enqueue(new Callback<JsonNode>() {
            @Override
            public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
                if (!response.isSuccessful()) {
                    mainActivity.serverErrorHandler(view, response);
                } else {
                    JsonNode body = response.body();
                    List<Message> data = null;
                    try {
                        ObjectReader reader = ChatWebService.getInstance().getObjectMapper().readerFor(new TypeReference<List<Message>>() {});
                        data = reader.readValue(body);
                    } catch (IOException e) {
                        Log.e("Web", "Failed parsing response. Response was: " + body, e);
                        Snackbar.make(view, "Unable to load messages. Reason: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }

                    if (data != null) {
                        initializeMessagesCardAdapter(data, messagesRecyclerView);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonNode> call, Throwable t) {
                Log.e("DeleteChannel", "Error has occurred while trying to load channels", t);
                Snackbar.make(view, "Something went wrong while loading channels: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        // Refresh messages every 1 second.
        scheduleRefresh();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STORED_CHANNEL_NAME_KEY, channelName);
    }

    private void initializeMessagesCardAdapter(List<Message> data, RecyclerView messagesRecyclerView) {
        messageCardAdapter = new MessageCardAdapter(data, mainActivity.getUser());
        messagesRecyclerView.setAdapter(new ScaleInAnimationAdapter(new AlphaInAnimationAdapter(messageCardAdapter)));

        if (!data.isEmpty()) {
            messagesRecyclerView.scrollToPosition(data.size() - 1);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopExecutor();
    }

    @Override
    public void onResume() {
        super.onResume();
        scheduleRefresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle", this.toString() + ".onDestroy");
        stopExecutor();
    }

    void onSendButtonClicked(View view) {
        if ((messageCardAdapter != null) && (channelName != null)) {
            String message = editTextMessage.getText().toString().trim();
            editTextMessage.setText("");
            if (message.isEmpty()) {
                Toast.makeText(mainActivity, "Cannot send empty message", Toast.LENGTH_SHORT).show();
            } else {
                ChatWebService.getInstance().getApi().sendMessage(channelName, mainActivity.getUser().getId(), message).enqueue(new Callback<JsonNode>() {
                    @Override
                    public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
                        if (!response.isSuccessful()) {
                            mainActivity.serverErrorHandler(fragmentView, response);
                        } else {
                            // Refresh is running every one second
                            //refresh();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonNode> call, Throwable t) {
                        Log.e("SendMessage", "Error has occurred while trying to send message", t);
                        Snackbar.make(fragmentView, "Something went wrong while trying to send message: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            Toast.makeText(mainActivity, "Cannot create messages. Reload.", Toast.LENGTH_LONG).show();
        }
    }

    private void refresh() {
        if ((messageCardAdapter) != null && (channelName != null)) {
            if (!messageCardAdapter.getMessages().isEmpty()) {
                Message lastMessage = messageCardAdapter.getMessages().get(messageCardAdapter.getMessages().size() - 1);
                ChatWebService.getInstance().getApi().getLatestMessagesInChannel(channelName, lastMessage.getDateTimeSent()).enqueue(new MessagesResultCallback());
            } else {
                ChatWebService.getInstance().getApi().getAllMessagesInChannel(channelName).enqueue(new MessagesResultCallback());
            }

            ChatWebService.getInstance().getApi().getChannel(channelName).enqueue(new Callback<JsonNode>() {
                @Override
                public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
                    if (!response.isSuccessful()) {
                        mainActivity.serverErrorHandler(fragmentView, response);
                    } else {
                        try {
                            Channel channel = ChatWebService.getInstance().getObjectMapper().readValue(response.body().toString(), Channel.class);
                            textViewParticipants.setText(String.format(getString(R.string.participants), channel.getUsers().size()));
                        } catch (JsonProcessingException e) {
                            Log.e("Refresh", "Error has occurred while trying to read channel", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonNode> call, Throwable t) {
                    Log.e("Refresh", "Error has occurred while trying to read channel info", t);
                    Snackbar.make(fragmentView, "Something went wrong while trying to read channel info: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    private void scheduleRefresh() {
        stopExecutor();

        mainActivity.doReconnect();

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread result = Executors.defaultThreadFactory().newThread(r);
            result.setName(channelName + "-RefreshThread");
            return result;
        });

        Looper myLooper = Looper.myLooper();
        executor.scheduleWithFixedDelay(() -> new Handler(myLooper).post(FragmentChannel.this::refresh), 1, 1, TimeUnit.SECONDS);
    }

    private void stopExecutor() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;

            mainActivity.doDisconnect();
        }
    }

    private class MessagesResultCallback implements Callback<JsonNode> {
        @Override
        public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
            if (!response.isSuccessful()) {
                mainActivity.serverErrorHandler(fragmentView, response);
            } else {
                try {
                    ObjectReader reader = ChatWebService.getInstance().getObjectMapper().readerFor(new TypeReference<List<Message>>() {
                    });
                    List<Message> newMessages = reader.readValue(response.body().toString());
                    newMessages.forEach(message -> {
                        int messagePosition = messageCardAdapter.getItemCount();
                        messageCardAdapter.getMessages().add(message);
                        messageCardAdapter.notifyItemInserted(messagePosition);
                        messageCardAdapter.notifyItemRangeChanged(messagePosition, messageCardAdapter.getMessages().size());
                        messagesRecyclerView.scrollToPosition(messagePosition);
                    });
                } catch (JsonProcessingException e) {
                    Log.e("Refresh", "Error has occurred while trying to read message", e);
                }
            }
        }

        @Override
        public void onFailure(Call<JsonNode> call, Throwable t) {
            Log.e("Refresh", "Error has occurred while trying to read message", t);
            Snackbar.make(fragmentView, "Something went wrong while trying to read message: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }
}