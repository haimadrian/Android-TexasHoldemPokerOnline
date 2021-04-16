package org.hit.android.haim.chat.client.fragment;

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

import org.hit.android.haim.chat.client.R;
import org.hit.android.haim.chat.client.bean.Message;
import org.hit.android.haim.chat.client.bean.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A custom adapter which binds between Message model and a message card view.
 */
public class MessageCardAdapter extends RecyclerView.Adapter<MessageCardAdapter.MessageViewHolder> {
    /**
     * Underlying data in the adapter
     */
    private final List<Message> messages;

    private final User user;

    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    /**
     * Constructs a new {@link MessageCardAdapter}
     * @param messages Underlying data in the adapter
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
            holder.getTextViewSenderMessage().setText(message.getMessage().substring(1, message.getMessage().length() - 1).replaceAll("\\\\n", System.lineSeparator()));
            holder.getTextViewSenderTime().setText(message.getDateTimeSent().format(timeFormat));
        } catch (Exception e) {
            Log.e("Error", "Error has occurred in onBindViewHolder: " + e.getMessage(), e);
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
        private final View itemView;
        private final TextView textViewSenderName;
        private final TextView textViewSenderMessage;
        private final TextView textViewSenderTime;
        private final LinearLayout messageCard;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            textViewSenderName = itemView.findViewById(R.id.textViewSenderName);
            textViewSenderMessage = itemView.findViewById(R.id.textViewSenderMessage);
            textViewSenderTime = itemView.findViewById(R.id.textViewSenderTime);
            messageCard = itemView.findViewById(R.id.messageCard);
        }

        public View getItemView() {
            return itemView;
        }

        public TextView getTextViewSenderName() {
            return textViewSenderName;
        }

        public TextView getTextViewSenderMessage() {
            return textViewSenderMessage;
        }

        public TextView getTextViewSenderTime() {
            return textViewSenderTime;
        }

        public LinearLayout getMessageCard() {
            return messageCard;
        }
    }
}