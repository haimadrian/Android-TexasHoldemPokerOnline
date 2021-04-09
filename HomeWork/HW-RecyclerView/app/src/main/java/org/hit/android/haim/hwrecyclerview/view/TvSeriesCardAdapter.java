package org.hit.android.haim.hwrecyclerview.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.hit.android.haim.hwrecyclerview.R;
import org.hit.android.haim.hwrecyclerview.model.TvSeries;

import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator;

/**
 * A custom adapter which binds between TVSeries model and a TVSeries card view.
 */
public class TvSeriesCardAdapter extends RecyclerView.Adapter<TvSeriesViewHolder> {
    /**
     * Underlying data in the adapter
     */
    private final List<TvSeries> tvSeriesData;

    /**
     * An activity context, to get resources from
     */
    private final Context context;

    /**
     * A listener to listen when user presses the link in a card view
     */
    private final CharacterCardAdapter.CharacterClickedListener linkClickListener;

    /**
     * Constructs a new {@link TvSeriesCardAdapter}
     * @param tvSeriesData Underlying data in the adapter
     * @param context An activity context, to get resources from
     * @param linkClickListener A listener to listen when user presses the link in a card view
     */
    public TvSeriesCardAdapter(List<TvSeries> tvSeriesData, Context context, CharacterCardAdapter.CharacterClickedListener linkClickListener) {
        this.tvSeriesData = tvSeriesData;
        this.context = context;
        this.linkClickListener = linkClickListener;
    }

    @NonNull
    @Override
    public TvSeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TvSeriesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_series_card_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TvSeriesViewHolder holder, int position) {
        TvSeries tvSeries = tvSeriesData.get(position);

        holder.getItemTitleTextView().setText(tvSeries.getName());
        RecyclerView charactersRecyclerView = holder.getCharactersRecyclerView();
        charactersRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        charactersRecyclerView.setLayoutManager(layoutManager);
        charactersRecyclerView.setItemAnimator(new FadeInLeftAnimator());

        CharacterCardAdapter adapter = new CharacterCardAdapter(tvSeries.getCharacters(), linkClickListener);
        charactersRecyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return tvSeriesData.size();
    }
}