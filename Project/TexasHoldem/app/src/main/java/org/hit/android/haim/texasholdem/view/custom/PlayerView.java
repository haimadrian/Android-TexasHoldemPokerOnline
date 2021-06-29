package org.hit.android.haim.texasholdem.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.hit.android.haim.texasholdem.R;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * A custom view that contains an image view and two text views: name and chips.<br/>
 * This custom view represents a player in the game fragment.
 * @author Haim Adrian
 * @since 01-May-21
 */
public class PlayerView extends LinearLayout {
    /**
     * The profile image of a player
     */
    @Getter
    private ImageView playerImageView;

    /**
     * The name of a player
     */
    @Getter
    private TextView playerNameTextView;

    /**
     * The amount of chips a player got
     */
    @Getter
    private TextView playerChipsTextView;

    /**
     * The progress bar of a player is used to count down the time of
     * a player's turn, and show an indication of it.
     */
    @Getter
    private ProgressBar playerProgressBar;

    /**
     * Constructs a new {@link PlayerView}
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     */
    public PlayerView(Context context) {
        this(context, null, 0);
    }

    /**
     * Constructs a new {@link PlayerView}
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public PlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructs a new {@link PlayerView}
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyle A resource identifier of a style resource that
     *        supplies default values for the view, used only if
     *        defStyleAttr is 0 or can not be found in the theme. Can be 0
     *        to not look for defaults.
     */
    public PlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        setGravity(Gravity.CENTER);

        // Load attributes (values\attrs_player.xml)
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PlayerView, defStyle, 0);

        /*
            Orientation: Control the orientation of PlayerView main linear layout, such that we can
            configure it to be either horizontal or vertical, based on the location of
            the player around the table.
            0 = HORIZ, 1 = VERT
         */
        int orientation = a.getInt(R.styleable.PlayerView_orientation, VERTICAL);

        /*
            Reverse: Whether to reverse the order of children of PlayerView or not.
            By default, the order is profile image first, and then name + chips.
            In order to be able to paint the name+chips before the image, we have
            introduced the "reverse" parameter, so we will re-arrange children.
         */
        boolean isReversed = a.getBoolean(R.styleable.PlayerView_reverse, false);
        a.recycle();

        setOrientation(orientation);
        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_player, this, true);

        FrameLayout imageAndProgress = (FrameLayout) getChildAt(0);
        playerImageView = (ImageView) imageAndProgress.getChildAt(0);
        playerProgressBar = (ProgressBar) imageAndProgress.getChildAt(1);

        LinearLayout nameAndChipsContainer = (LinearLayout) getChildAt(1);
        playerNameTextView = (TextView) nameAndChipsContainer.getChildAt(0);

        LinearLayout container = (LinearLayout) nameAndChipsContainer.getChildAt(1);
        playerChipsTextView = (TextView) container.getChildAt(1);

        if (isReversed) {
            // Backup children
            List<View> views = new ArrayList<>();
            for (int i = 0; i < getChildCount(); i++) {
                views.add(getChildAt(i));
            }

            // Remove them
            removeAllViews();

            // Add them back in reverse order
            for (int i = views.size() - 1; i >= 0; i--) {
                addView(views.get(i));
            }
        }

        // Set padding to the name and chips container, based on orientation,
        // so the texts will have some space between them to the image
        setPaddingBasedOnLayout(nameAndChipsContainer, orientation, isReversed);
    }

    /**
     * Set padding to the name and chips container, based on orientation,
     * so the texts will have some space between them to the image
     */
    private void setPaddingBasedOnLayout(LinearLayout container, int orientation, boolean isReversed) {
        int paddingValue = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

        LayoutParams params = new LayoutParams(container.getLayoutParams());
        if (orientation == HORIZONTAL) {
            int right = isReversed ? paddingValue : 0;
            params.setMargins(paddingValue, 0, right, 0);
        } else {
            int top = isReversed ? 0 : paddingValue;
            int bottom = isReversed ? paddingValue : 0;
            params.setMargins(0, top, 0, bottom);
        }

        container.setLayoutParams(params);
    }
}