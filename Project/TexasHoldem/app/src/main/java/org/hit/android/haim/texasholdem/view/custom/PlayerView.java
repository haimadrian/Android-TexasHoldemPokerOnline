package org.hit.android.haim.texasholdem.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    public PlayerView(Context context) {
        this(context, null, 0);
    }

    public PlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

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
         */
        int orientation = a.getInt(R.styleable.PlayerView_orientation, 1);

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

        playerImageView = (ImageView) getChildAt(2);
        playerNameTextView = (TextView) getChildAt(3);
        playerChipsTextView = (TextView) getChildAt(6);

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
    }
}