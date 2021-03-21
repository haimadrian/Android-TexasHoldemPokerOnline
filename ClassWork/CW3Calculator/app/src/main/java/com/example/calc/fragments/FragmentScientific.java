package com.example.calc.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.example.calc.R;
import com.example.calc.action.ActionType;
import com.example.calc.activities.MainActivity;
import com.example.calc.component.SpinnerAdapter;

import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FragmentScientific extends Fragment {
    Spinner spinnerFunc;
    Spinner spinnerTrigo;

    public FragmentScientific() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scientific, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();

        spinnerFunc = view.findViewById(R.id.spinnerFunc);
        spinnerFunc.setAdapter(new SpinnerAdapter("Func   ▼", mainActivity, android.R.layout.simple_spinner_dropdown_item, ActionType.listFunctions().stream().map(ActionType::getActionText).collect(Collectors.toList())));
        spinnerTrigo = view.findViewById(R.id.spinnerTrigo);
        spinnerTrigo.setAdapter(new SpinnerAdapter("Trigo  ▼", mainActivity, android.R.layout.simple_spinner_dropdown_item, ActionType.listTrigoFunctions().stream().map(ActionType::getActionText).collect(Collectors.toList())));

        // Register dropdown listener now, otherwise it listens during creation and runs even though user has not pressed anything.
        spinnerFunc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                try {
                    // Index 0 is kept for the title
                    if (position > 0 && position <= ActionType.listFunctions().size()) {
                        mainActivity.doFunction(String.valueOf(parentView.getItemAtPosition(position)));

                        // Clear selection, so we can select the same function again
                        spinnerFunc.setSelection(0);
                    }
                } catch (Throwable t) {
                    mainActivity.setErrorOutput(String.format(getString(R.string.error), t.getMessage()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        spinnerTrigo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                try {
                    if (position > 0 && position <= ActionType.listTrigoFunctions().size()) {
                        // Index 0 is kept for the title
                        mainActivity.doFunction(String.valueOf(parentView.getItemAtPosition(position)));

                        // Clear selection, so we can select the same function again
                        spinnerTrigo.setSelection(0);
                    }
                } catch (Throwable t) {
                    mainActivity.setErrorOutput(String.format(getString(R.string.error), t.getMessage()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        view.findViewById(R.id.buttonDiv).setOnClickListener(mainActivity::onOperatorButtonClicked);
        view.findViewById(R.id.buttonSquare).setOnClickListener(mainActivity::onFunctionButtonClicked);
        view.findViewById(R.id.buttonSqrt).setOnClickListener(mainActivity::onFunctionButtonClicked);
        view.findViewById(R.id.buttonFactorial).setOnClickListener(mainActivity::onFunctionButtonClicked);

        return view;
    }
}