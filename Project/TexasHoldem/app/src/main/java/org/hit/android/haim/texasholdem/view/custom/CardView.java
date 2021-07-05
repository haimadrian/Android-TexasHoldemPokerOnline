package org.hit.android.haim.texasholdem.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.hit.android.haim.texasholdem.R;

import lombok.Getter;

/**
 * A custom view that contains an image view and two text views: name and chips.<br/>
 * This custom view represents a player in the game fragment.
 * @author Haim Adrian
 * @since 01-May-21
 */
public class CardView extends FrameLayout {
    /**
     * A card
     */
    @Getter
    private ImageView cardImageView;

    /**
     * A highlight for the card
     */
    @Getter
    private ImageView cardHighlightImageView;

    /**
     * Constructs a new {@link CardView}
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     */
    public CardView(Context context) {
        this(context, null, 0);
    }

    /**
     * Constructs a new {@link CardView}
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public CardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructs a new {@link CardView}
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyle A resource identifier of a style resource that
     *        supplies default values for the view, used only if
     *        defStyleAttr is 0 or can not be found in the theme. Can be 0
     *        to not look for defaults.
     */
    public CardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        // Load attributes (values\attrs_card.xml)
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CardView, defStyle, 0);

        // Image resources
        int card = a.getInt(R.styleable.CardView_cardSrc, R.drawable.pack);

        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_card, this, true);

        FrameLayout cardContainer = (FrameLayout) getChildAt(0);
        cardImageView = (ImageView) cardContainer.getChildAt(0);
        cardHighlightImageView = (ImageView) cardContainer.getChildAt(1);

        cardImageView.setImageResource(card);

    }

    public void highlight() {
        cardHighlightImageView.setVisibility(View.VISIBLE);
    }

    public void clearHighlight() {
        cardHighlightImageView.setVisibility(View.INVISIBLE);
    }
}