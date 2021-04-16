package org.hit.android.haim.hwrecyclerview.view;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private final MainActivity context;

    /**
     * A listener to listen when user presses the link in a card view
     */
    private final CharacterCardAdapter.CharacterClickedListener linkClickListener;

    /**
     * Maintain a focused view because the "FocusChanged" event is not raised on the views so we cannot
     * detect when to play a theme song.
     */
    private String focusedTvSeries;

    /**
     * Constructs a new {@link TvSeriesCardAdapter}
     * @param tvSeriesData Underlying data in the adapter
     * @param context An activity context, to get resources from
     * @param linkClickListener A listener to listen when user presses the link in a card view
     */
    public TvSeriesCardAdapter(@NonNull List<TvSeries> tvSeriesData, @NonNull MainActivity context, @Nullable CharacterCardAdapter.CharacterClickedListener linkClickListener, @Nullable String focusedTvSeries) {
        this.tvSeriesData = tvSeriesData;
        this.context = context;
        this.linkClickListener = linkClickListener;
        this.focusedTvSeries = focusedTvSeries;
    }

    @NonNull
    @Override
    public TvSeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TvSeriesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_series_card_view, parent, false));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull TvSeriesViewHolder holder, int position) {
        TvSeries tvSeries = tvSeriesData.get(position);

        View.OnTouchListener touchListener = (v, motionEvent) -> {
            View currentTouchedView = v;
            if (v.getClass().equals(RecyclerView.class)) {
                currentTouchedView = (View)v.getParent();
            }

            if (!currentTouchedView.getTag().equals(focusedTvSeries)) {
                // When there is no focused view it means we have not played any playback, so there is nothing to stop
                if (focusedTvSeries != null) {
                    // Stop playing theme song in background
                    context.stopService(SoundService.class);
                }

                // Play theme song in background
                context.startService(SoundService.class, tvSeries.getThemeSongResId());
                focusedTvSeries = (String) currentTouchedView.getTag();
            }

            return false;
        };

        holder.getItemView().setTag(tvSeries.getId());
        holder.getItemView().setOnTouchListener(touchListener);
        holder.getCharactersRecyclerView().setOnTouchListener(touchListener);

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

    public String getFocusedTvSeries() {
        return focusedTvSeries;
    }
}