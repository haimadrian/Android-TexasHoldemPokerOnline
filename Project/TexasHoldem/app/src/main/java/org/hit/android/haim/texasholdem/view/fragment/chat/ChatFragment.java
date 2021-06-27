package org.hit.android.haim.texasholdem.view.fragment.chat;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.common.model.bean.chat.Channel;
import org.hit.android.haim.texasholdem.common.model.bean.chat.Message;
import org.hit.android.haim.texasholdem.databinding.FragmentChatBinding;
import org.hit.android.haim.texasholdem.model.chat.Chat;
import org.hit.android.haim.texasholdem.model.game.Game;
import org.hit.android.haim.texasholdem.view.activity.MainActivity;
import org.hit.android.haim.texasholdem.view.fragment.ViewBindedFragment;

import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FlipInBottomXAnimator;

/**
 * The chat fragment responsible for showing messages arrived from other players, and
 * to let the current player to send messages in a chat.<br/>
 * It is available for use during online games only.
 * @author Haim Adrian
 * @since 14-Apr-21
 */
public class ChatFragment extends ViewBindedFragment<FragmentChatBinding> implements Chat.ChatListener {
    private static final String LOGGER = ChatFragment.class.getSimpleName();
    private static final String STORED_CHANNEL_NAME_KEY = ChatFragment.class.getName() + ".CHANNEL_NAME";

    /**
     * Game hash is used as channel name
     */
    private String channelName;

    /**
     * {@link MessageCardAdapter} is used for displaying messages in the chat (in the recycler view)
     */
    private MessageCardAdapter messageCardAdapter;

    /**
     * Keep a reference to {@link MainActivity}, so we can handle HTTP failures at one location.
     */
    private MainActivity mainActivity;

    /**
     * Keep a reference to this fragment's view, so we can use it for toasts
     */
    private View fragmentView;

    /**
     * Constructs a new {@link ChatFragment}, using the game hash as channel name, so we can
     * work with backend on the messages in a game's chat.
     * @param channelName Game hash is used as channel name
     */
    public ChatFragment(String channelName) {
        super(R.layout.fragment_game, FragmentChatBinding::bind);

        Log.d(LOGGER, this.toString() + ".new");
        this.channelName = channelName;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(STORED_CHANNEL_NAME_KEY)) {
            channelName = savedInstanceState.getString(STORED_CHANNEL_NAME_KEY);
        }

        fragmentView = view;
        mainActivity = (MainActivity) getActivity();

        if (channelName != null) {
            getBinding().textViewChatName.setText(channelName);
        }

        getBinding().imageViewSend.setOnClickListener(this::onSendButtonClicked);
        getBinding().messagesRecyclerView.setHasFixedSize(true);

        // Set vertical layout and custom animator to the recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false);
        //layoutManager.setStackFromEnd(true);
        getBinding().messagesRecyclerView.setLayoutManager(layoutManager);
        getBinding().messagesRecyclerView.setItemAnimator(new FlipInBottomXAnimator(new OvershootInterpolator()));

        // When we start, make sure we get all messages in channel to display old messages
        List<Message> data = Game.getInstance().getChat().getMessages();
        initializeMessagesCardAdapter(data, getBinding().messagesRecyclerView);

        // Register chat listener, so we will receive updates on new messages
        Game.getInstance().getChat().addChatListener(this);

        // Update the participants text view with details from server
        updateChatParticipantsTextView();
    }

    @Override
    public void onDestroyView() {
        // Remove the listener so we will not register ourselves over and over
        // in case onCreateView is raised more than once.
        Game.getInstance().getChat().removeChatListener(this);

        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STORED_CHANNEL_NAME_KEY, channelName);
    }

    @Override
    public void onMessageArrived(Message message) {
        new Handler().post(() -> {
            int messagePosition = messageCardAdapter.getItemCount();
            messageCardAdapter.getMessages().add(message);
            messageCardAdapter.notifyItemInserted(messagePosition);
            messageCardAdapter.notifyItemRangeChanged(messagePosition, messageCardAdapter.getMessages().size());
            getBinding().messagesRecyclerView.scrollToPosition(messagePosition);

            updateChatParticipantsTextView();
        });
    }

    @Override
    public void onError(String errorMessage) {
        new Handler().post(() -> {
            Snackbar.make(fragmentView, errorMessage, Snackbar.LENGTH_LONG).show();
        });
    }

    /**
     * Update the text view with amount of participants in chat
     */
    private void updateChatParticipantsTextView() {
        Channel channelInfo = Game.getInstance().getChat().getChannelInfo();
        if (channelInfo != null) {
            getBinding().textViewChatParticipants.setText(String.format(getString(R.string.participants), channelInfo.getUsers().size()));
        }
    }

    /**
     * Create a {@link MessageCardAdapter} to set to a specified recycler view.<br/>
     * When there is data, we will scroll the recycler view to the latest message, in order to display
     * the most recent  messages
     * @param data Existing messages to display as cards in the specified recycler view
     * @param messagesRecyclerView The messages recycler view
     */
    private void initializeMessagesCardAdapter(List<Message> data, RecyclerView messagesRecyclerView) {
        messageCardAdapter = new MessageCardAdapter(data, mainActivity.getUser());
        messagesRecyclerView.setAdapter(new ScaleInAnimationAdapter(new AlphaInAnimationAdapter(messageCardAdapter)));

        if (!data.isEmpty()) {
            messagesRecyclerView.scrollToPosition(data.size() - 1);
        }
    }

    /**
     * Occurs when user presses the "Send" button.<br/>
     * We will get the input message that user entered and send it to the server so other players will see it.<br/>
     * In case the input message is empty, we show a toast to the user, telling him to write something
     * @param view Sender
     */
    void onSendButtonClicked(View view) {
        if ((messageCardAdapter != null) && (channelName != null)) {
            String message = getBinding().editTextWriteMessage.getText().toString().trim();
            getBinding().editTextWriteMessage.setText("");
            if (message.isEmpty()) {
                Toast.makeText(mainActivity, "Cannot send empty message", Toast.LENGTH_SHORT).show();
            } else {
                Game.getInstance().getChat().sendMessage(mainActivity.getUser().getId(), message, error -> Snackbar.make(fragmentView, error, Snackbar.LENGTH_LONG).show());
            }
        } else {
            Toast.makeText(mainActivity, "Cannot create messages. Reload.", Toast.LENGTH_LONG).show();
        }
    }
}