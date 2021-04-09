package org.hit.android.haim.hwrecyclerview.view;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.hit.android.haim.hwrecyclerview.R;

/**
 * @author Haim Adrian
 * @since 07-Apr-21
 */
public class CharacterViewHolder extends RecyclerView.ViewHolder {
    private final ImageView itemImageView;
    private final TextView itemNameTextView;
    private final TextView itemSubNameTextView;
    private final TextView itemMoreLinkTextView;

    public CharacterViewHolder(@NonNull View itemView) {
        super(itemView);
        itemImageView = itemView.findViewById(R.id.itemImageView);
        itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
        itemSubNameTextView = itemView.findViewById(R.id.itemSubNameTextView);
        itemMoreLinkTextView = itemView.findViewById(R.id.itemMoreLinkTextView);
    }

    public ImageView getItemImageView() {
        return itemImageView;
    }

    public TextView getItemNameTextView() {
        return itemNameTextView;
    }

    public TextView getItemSubNameTextView() {
        return itemSubNameTextView;
    }

    public TextView getItemMoreLinkTextView() {
        return itemMoreLinkTextView;
    }
}
