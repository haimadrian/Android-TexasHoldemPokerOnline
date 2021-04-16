package org.hit.android.haim.chat.client.fragment;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.hit.android.haim.chat.client.R;
import org.hit.android.haim.chat.client.bean.Channel;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * A custom adapter which binds between Channel model and a channel card view.
 */
public class ChannelCardAdapter extends RecyclerView.Adapter<ChannelCardAdapter.ChannelViewHolder> {
    /**
     * Underlying data in the adapter
     */
    private final List<Channel> channels;

    /**
     * An activity context, to get resources from
     */
    private final Activity context;

    /**
     * A listener to listen when user presses a button in a card view
     */
    private final BiConsumer<View, Integer> buttonClickedListener;

    /**
     * Constructs a new {@link ChannelCardAdapter}
     * @param channels Underlying data in the adapter
     * @param context An activity context, to get resources from
     * @param buttonClickedListener A listener to listen when user presses the link in a card view
     */
    public ChannelCardAdapter(@NonNull List<Channel> channels, @NonNull Activity context, @Nullable BiConsumer<View, Integer> buttonClickedListener) {
        this.channels = channels;
        this.context = context;
        this.buttonClickedListener = buttonClickedListener;
    }

    @NonNull
    @Override
    public ChannelCardAdapter.ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChannelCardAdapter.ChannelViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_channel, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelCardAdapter.ChannelViewHolder holder, int position) {
        try {
            Channel channel = channels.get(position);

            holder.getItemView().setTag(channel.getName());
            holder.getTextViewChatName().setText(channel.getName());
            holder.getTextViewParticipants().setText(String.format(context.getString(R.string.participants), channel.getUsers().size()));
            holder.getImageViewJoin().setOnClickListener(view -> {
                if (buttonClickedListener != null) {
                    buttonClickedListener.accept(view, position);
                }
            });
            holder.getImageViewDelete().setOnClickListener(view -> {
                if (buttonClickedListener != null) {
                    buttonClickedListener.accept(view, position);
                }
            });

            holder.getImageViewDelete().setVisibility(channel.isDeletable() ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            Log.e("Error", "Error has occurred in onBindViewHolder: " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public List<Channel> getChannels() {
        return channels;
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {
        private final View itemView;
        private final TextView textViewChatName;
        private final TextView textViewParticipants;
        private final ImageView imageViewJoin;
        private final ImageView imageViewDelete;

        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            textViewChatName = itemView.findViewById(R.id.textViewChatName);
            textViewParticipants = itemView.findViewById(R.id.textViewParticipants);
            imageViewJoin = itemView.findViewById(R.id.imageViewJoin);
            imageViewDelete = itemView.findViewById(R.id.imageViewDelete);
        }

        public View getItemView() {
            return itemView;
        }

        public TextView getTextViewChatName() {
            return textViewChatName;
        }

        public TextView getTextViewParticipants() {
            return textViewParticipants;
        }

        public ImageView getImageViewJoin() {
            return imageViewJoin;
        }

        public ImageView getImageViewDelete() {
            return imageViewDelete;
        }
    }
}