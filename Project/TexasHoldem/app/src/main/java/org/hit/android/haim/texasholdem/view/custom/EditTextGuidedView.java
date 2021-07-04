package org.hit.android.haim.texasholdem.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.hit.android.haim.texasholdem.R;

import lombok.Getter;
import lombok.Setter;

/**
 * A custom view that contains an edit text and an image next to it.<br/>
 * This custom view represents an edit text with guideline button, to have additional
 * information about that edit text when clicking on the image.
 * @author Haim Adrian
 * @since 28-Mar-21
 */
public class EditTextGuidedView extends LinearLayout {
    public static final int GUIDE_TEXT_HORIZONTAL_PADDING = 24;
    public static final int GUIDE_TEXT_VERTICAL_PADDING = 10;
    /**
     * The content to be displayed within a popup
     */
    @Getter
    @Setter
    private String guide;

    /**
     * The edit text this compound component has
     */
    @Getter
    private EditText edit;

    /**
     * The image sitting right to the edit text (Help)
     */
    @Getter
    private ImageView image;

    /**
     * A reference to the popup that we display when user clicks on the image
     */
    @Getter
    private PopupWindow popUp;

    /**
     * Constructs a new {@link EditTextGuidedView}
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     */
    public EditTextGuidedView(Context context) {
        this(context, null, 0);
    }

    /**
     * Constructs a new {@link EditTextGuidedView}
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public EditTextGuidedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructs a new {@link EditTextGuidedView}
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyle A resource identifier of a style resource that
     *        supplies default values for the view, used only if
     *        defStyleAttr is 0 or can not be found in the theme. Can be 0
     *        to not look for defaults.
     */
    public EditTextGuidedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.EditTextGuidedView, defStyle, 0);

        guide = a.getString(R.styleable.EditTextGuidedView_guide);
        String hint = a.getString(R.styleable.EditTextGuidedView_hint);
        int inputType = a.getInteger(R.styleable.EditTextGuidedView_inputType, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_edit_text_guided, this, true);

        edit = (EditText) getChildAt(0);
        edit.setHint(hint);
        edit.setInputType(inputType);

        image = (ImageView) getChildAt(1);
        image.setOnClickListener(v -> {
            int[] location = new int[2];

            // Get the x, y location and store it in the location[] array
            // location[0] = x, location[1] = y.
            image.getLocationOnScreen(location);

            popUp.showAtLocation(this, Gravity.NO_GRAVITY, location[0], location[1]);
            popUp.update(location[0], location[1] + image.getHeight(), this.getWidth(), 120);
        });

        initPopup();
    }

    /**
     * Initializes the popup that will be displayed when user clicks the image
     */
    private void initPopup() {
        popUp = new PopupWindow(this);
        popUp.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.popup_frame));
        popUp.setClippingEnabled(true);
        popUp.setFocusable(true);

        // The text view containing the information
        TextView guideTextView = new TextView(this.getContext());
        guideTextView.setTextColor(Color.WHITE);
        guideTextView.setText(guide);
        guideTextView.setPadding(GUIDE_TEXT_HORIZONTAL_PADDING, GUIDE_TEXT_VERTICAL_PADDING, GUIDE_TEXT_HORIZONTAL_PADDING, GUIDE_TEXT_VERTICAL_PADDING);

        // The content view is a linear layout containing the text view
        LinearLayout popupContentView = new LinearLayout(this.getContext());
        popupContentView.setOrientation(LinearLayout.VERTICAL);
        popupContentView.addView(guideTextView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        popUp.setContentView(popupContentView);
    }

    /**
     * @return The input text from this edit text, as String.
     */
    public String getText() {
        return edit.getText().toString();
    }

    /**
     *
     * @param defaultValue A value to use in case the input is not an integer
     * @return The input text from this edit text, as int.
     */
    public int getInt(int defaultValue) {
        try {
            return Integer.parseInt(getText());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     *
     * @param defaultValue A value to use in case the input is not a long number
     * @return The input text from this edit text, as long.
     */
    public long getLong(long defaultValue) {
        try {
            return Long.parseLong(getText());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}