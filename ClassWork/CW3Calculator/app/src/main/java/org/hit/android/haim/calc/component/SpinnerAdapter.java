package org.hit.android.haim.calc.component;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import org.hit.android.haim.calc.R;

import java.util.ArrayList;
import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<String> {
    private final String title;

    public SpinnerAdapter(String title, @NonNull Context context, int resource, @NonNull List<String> objects) {
        super(new ContextThemeWrapper(context, R.style.buttonFunctionFont), resource, new ArrayList<String>(objects) {
            {
                // We add the title as first item in order to let user to select "none"
                this.add(0, title);
            }
        });
        this.title = title;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        ((TextView) v).setTextAppearance(R.style.buttonFunctionFont);
        //v.setBackgroundColor(getContext().getResources().getColor(R.color.buttonColor, null));
        v.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.button_action_design, null));
        if (title != null) {
            ((TextView) v).setText(title);
        }

        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);

        ((TextView) v).setTextAppearance(R.style.buttonFunctionFont);
        //v.setBackgroundColor(getContext().getResources().getColor(R.color.buttonLightColor, null));
        v.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.button_action_dropdown_design, null));

        return v;
    }


}
