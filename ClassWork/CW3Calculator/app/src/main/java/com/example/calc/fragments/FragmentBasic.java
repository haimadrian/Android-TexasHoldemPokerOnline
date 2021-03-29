package com.example.calc.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.calc.R;
import com.example.calc.activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FragmentBasic extends Fragment {

    public FragmentBasic() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_basic, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();

        Button button2 = view.findViewById(R.id.button2);
        Button button3 = view.findViewById(R.id.button3);

        button2.setOnLongClickListener(view2 -> {
            try {
                registerForContextMenu(button2);
                mainActivity.openContextMenu(button2);
            } catch (Throwable t) {
                mainActivity.setErrorOutput(String.format(getString(R.string.error), t.getMessage()));
            }
            return true;
        });
        button3.setOnLongClickListener(view2 -> {
            try {
                registerForContextMenu(button3);
                mainActivity.openContextMenu(button3);
            } catch (Throwable t) {
                mainActivity.setErrorOutput(String.format(getString(R.string.error), t.getMessage()));
            }
            return true;
        });

        view.findViewById(R.id.button0).setOnClickListener(mainActivity::onOperandButtonClicked);
        view.findViewById(R.id.button1).setOnClickListener(mainActivity::onOperandButtonClicked);
        view.findViewById(R.id.button2).setOnClickListener(mainActivity::onOperandButtonClicked);
        view.findViewById(R.id.button3).setOnClickListener(mainActivity::onOperandButtonClicked);
        view.findViewById(R.id.button4).setOnClickListener(mainActivity::onOperandButtonClicked);
        view.findViewById(R.id.button5).setOnClickListener(mainActivity::onOperandButtonClicked);
        view.findViewById(R.id.button6).setOnClickListener(mainActivity::onOperandButtonClicked);
        view.findViewById(R.id.button7).setOnClickListener(mainActivity::onOperandButtonClicked);
        view.findViewById(R.id.button8).setOnClickListener(mainActivity::onOperandButtonClicked);
        view.findViewById(R.id.button9).setOnClickListener(mainActivity::onOperandButtonClicked);
        view.findViewById(R.id.buttonMul).setOnClickListener(mainActivity::onOperatorButtonClicked);
        view.findViewById(R.id.buttonMinus).setOnClickListener(mainActivity::onOperatorButtonClicked);
        view.findViewById(R.id.buttonPlus).setOnClickListener(mainActivity::onOperatorButtonClicked);
        view.findViewById(R.id.buttonEquals).setOnClickListener(mainActivity::onEqualsButtonClicked);
        view.findViewById(R.id.buttonClear).setOnClickListener(mainActivity::onClearButtonClicked);
        view.findViewById(R.id.buttonDot).setOnClickListener(mainActivity::onOperandButtonClicked);

        return view;
    }
}