package org.hit.android.haim.texasholdem.view.fragment.chat;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.hit.android.haim.texasholdem.R;
import org.hit.android.haim.texasholdem.common.model.bean.chat.Message;
import org.hit.android.haim.texasholdem.model.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.Getter;

/**
 * A custom adapter which binds between Message model and a message card view.
 * @author Haim Adrian
 * @since 14-Apr-21
 */
public class MessageCardAdapter extends RecyclerView.Adapter<MessageCardAdapter.MessageViewHolder> {
    private static final String LOGGER = MessageCardAdapter.class.getSimpleName();

    /**
     * Underlying data in the adapter
     */
    private final List<Message> messages;

    /**
     * So we will be able to distinguish between this user style (outgoing) and other users style (incoming)
     */
    private final User user;

    /**
     * The format of message time to show in a message view
     */
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    /**
     * Constructs a new {@link MessageCardAdapter}
     * @param messages Underlying data in the adapter
     * @param user So we will be able to distinguish between this user style (outgoing) and other users style (incoming)
     */
    public MessageCardAdapter(@NonNull List<Message> messages, User user) {
        this.messages = messages;
        this.user = user;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        try {
            Message message = messages.get(position);

            // Outgoing message
            if (user.getId().equalsIgnoreCase(message.getUser().getId())) {
                holder.getMessageCard().setBackgroundResource(R.drawable.card_item_message_outgoing);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(holder.getTextViewSenderTime().getLayoutParams());
                params.setMarginEnd(54);
                params.gravity = Gravity.END | Gravity.BOTTOM;
                holder.getTextViewSenderTime().setLayoutParams(params);

                params = new LinearLayout.LayoutParams(holder.getTextViewSenderMessage().getLayoutParams());
                params.setMarginEnd(54);
                holder.getTextViewSenderMessage().setLayoutParams(params);
            }
            // Incoming message
            else {
                holder.getMessageCard().setBackgroundResource(R.drawable.card_item_message_incoming);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(holder.getTextViewSenderTime().getLayoutParams());
                params.setMarginEnd(0);
                params.gravity = Gravity.END | Gravity.BOTTOM;
                holder.getTextViewSenderTime().setLayoutParams(params);

                params = new LinearLayout.LayoutParams(holder.getTextViewSenderMessage().getLayoutParams());
                params.setMarginEnd(0);
                holder.getTextViewSenderMessage().setLayoutParams(params);
            }

            String userName = message.getUser().getName();
            userName = TextUtils.isEmpty(userName) ? message.getUser().getId().substring(0, message.getUser().getId().indexOf('@')) : userName;
            holder.getTextViewSenderName().setText(userName);
            holder.getTextViewSenderMessage().setText(message.getMessage().replaceAll("\\\\n", System.lineSeparator()));
            holder.getTextViewSenderTime().setText(message.getDateTimeSent().format(timeFormat));
        } catch (Exception e) {
            Log.e(LOGGER, "Error has occurred in onBindViewHolder: " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public List<Message> getMessages() {
        return messages;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        @Getter
        private final View itemView;

        @Getter
        private final TextView textViewSenderName;

        @Getter
        private final TextView textViewSenderMessage;

        @Getter
        private final TextView textViewSenderTime;

        @Getter
        private final LinearLayout messageCard;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            textViewSenderName = itemView.findViewById(R.id.textViewSenderName);
            textViewSenderMessage = itemView.findViewById(R.id.textViewSenderMessage);
            textViewSenderTime = itemView.findViewById(R.id.textViewSenderTime);
            messageCard = itemView.findViewById(R.id.messageCard);
        }
    }
}