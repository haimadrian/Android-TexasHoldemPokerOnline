package org.hit.android.haim.texasholdem.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.hit.android.haim.texasholdem.R;

import lombok.Getter;

/**
 * A custom view that contains an image view and two text views: name and chips.<br/>
 * This custom view represents a player in the game fragment.
 * @author Haim Adrian
 * @since 01-May-21
 */
public class HandView extends LinearLayout {
    /**
     * First card in hand
     */
    @Getter
    private ImageView firstCardImageView;

    /**
     * Second card in hand
     */
    @Getter
    private ImageView secondCardImageView;

    /**
     * Dealer image, to let outside world to show/hide it
     */
    @Getter
    private ImageView dealerImageView;

    /**
     * Constructs a new {@link HandView}
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     */
    public HandView(Context context) {
        this(context, null, 0);
    }

    /**
     * Constructs a new {@link HandView}
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public HandView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructs a new {@link HandView}
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyle A resource identifier of a style resource that
     *        supplies default values for the view, used only if
     *        defStyleAttr is 0 or can not be found in the theme. Can be 0
     *        to not look for defaults.
     */
    public HandView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        // Load attributes (values\attrs_hand.xml)
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HandView, defStyle, 0);

        // Image resources
        int firstCard = a.getInt(R.styleable.HandView_firstCardSrc, R.drawable.pack);
        int secondCard = a.getInt(R.styleable.HandView_secondCardSrc, R.drawable.pack);

        // DealerPosition: Control where the dealer image will be painted. (Left, Right, Bottom)
        DealerPosition dealerPosition = DealerPosition.values()[a.getInt(R.styleable.HandView_dealerPosition, 0)];

        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_hand, this, true);

        dealerImageView = (ImageView) getChildAt(0);
        LinearLayout cardsContainer = (LinearLayout) getChildAt(1);
        firstCardImageView = (ImageView) cardsContainer.getChildAt(0);
        secondCardImageView = (ImageView) cardsContainer.getChildAt(1);

        firstCardImageView.setImageResource(firstCard);
        secondCardImageView.setImageResource(secondCard);

        // Now remove all views so we will re-arrange them based on dealer location
        removeAllViews();

        setGravity(Gravity.CENTER);
        setOrientation(LinearLayout.HORIZONTAL);

        // Calculate the position of the dealer image, based on specified position.
        int dealerWidth = LayoutParams.MATCH_PARENT;
        int dealerHeight = LayoutParams.MATCH_PARENT;
        int dealerMarginLeft = 0, dealerMarginTop = 0, dealerMarginRight = 0;
        float dealerWeight = 0.8f;
        switch (dealerPosition) {
            case LEFT: {
                addView(dealerImageView);
                addView(cardsContainer);

                // Match to parent's width, to rely on layout weight
                dealerHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
                dealerMarginRight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -42, getResources().getDisplayMetrics());
                break;
            }
            case RIGHT: {
                addView(cardsContainer);
                addView(dealerImageView);

                // Match to parent's width, to rely on layout weight
                dealerHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
                dealerMarginLeft = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -32, getResources().getDisplayMetrics());
                break;
            }
            case BOTTOM: {
                setOrientation(LinearLayout.VERTICAL);
                addView(cardsContainer);
                addView(dealerImageView);

                // Match to parent's height, to rely on layout weight
                dealerWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
                dealerMarginTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, getResources().getDisplayMetrics());
                dealerWeight = 0.45f;
                break;
            }
        }

        LayoutParams params = new LayoutParams(dealerWidth, dealerHeight, dealerWeight);
        params.setMargins(dealerMarginLeft, dealerMarginTop, dealerMarginRight, 0);
        dealerImageView.setLayoutParams(params);
    }

    private enum DealerPosition {
        LEFT, RIGHT, BOTTOM
    }
}