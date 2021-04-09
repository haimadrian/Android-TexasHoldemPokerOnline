package org.hit.android.haim.hwrecyclerview.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.hit.android.haim.hwrecyclerview.R;
import org.hit.android.haim.hwrecyclerview.model.Character;

import java.util.List;

/**
 * A custom adapter which binds between TVSeries model and a TVSeries card view.
 */
public class CharacterCardAdapter extends RecyclerView.Adapter<CharacterViewHolder> {
    /**
     * Underlying data in the adapter
     */
    private final List<Character> characterData;

    /**
     * A listener to listen when user presses the link in a card view
     */
    private final CharacterClickedListener linkClickListener;

    /**
     * Constructs a new {@link CharacterCardAdapter}
     * @param characterData Underlying data in the adapter
     * @param linkClickListener A listener to listen when user presses the link in a card view
     */
    public CharacterCardAdapter(List<Character> characterData, CharacterClickedListener linkClickListener) {
        this.characterData = characterData;
        this.linkClickListener = linkClickListener;
    }

    @NonNull
    @Override
    public CharacterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CharacterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.character_card_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CharacterViewHolder holder, int position) {
        Character character = characterData.get(position);

        holder.getItemNameTextView().setText(character.getName());
        holder.getItemImageView().setImageResource(character.getImageResId());

        if (character.getSubName() != null) {
            holder.getItemSubNameTextView().setVisibility(View.VISIBLE);
            holder.getItemSubNameTextView().setText(character.getSubName());
        } else {
            holder.getItemSubNameTextView().setVisibility(View.GONE);
        }

        if (linkClickListener != null) {
            holder.getItemMoreLinkTextView().setOnClickListener(v -> linkClickListener.onCharacterClicked(character));
        }
    }

    @Override
    public int getItemCount() {
        return characterData.size();
    }

    @FunctionalInterface
    public interface CharacterClickedListener {
        void onCharacterClicked(Character character);
    }
}