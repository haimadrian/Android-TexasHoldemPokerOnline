package org.hit.android.haim.hwrecyclerview.view;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.hit.android.haim.hwrecyclerview.R;

/**
 * @author Haim Adrian
 * @since 07-Apr-21
 */
public class TvSeriesViewHolder extends RecyclerView.ViewHolder {
    private final TextView itemTitleTextView;
    private final RecyclerView charactersRecyclerView;

    public TvSeriesViewHolder(@NonNull View itemView) {
        super(itemView);
        itemTitleTextView = itemView.findViewById(R.id.itemTitleTextView);
        charactersRecyclerView = itemView.findViewById(R.id.charactersRecyclerView);
    }

    public TextView getItemTitleTextView() {
        return itemTitleTextView;
    }

    public RecyclerView getCharactersRecyclerView() {
        return charactersRecyclerView;
    }
}
