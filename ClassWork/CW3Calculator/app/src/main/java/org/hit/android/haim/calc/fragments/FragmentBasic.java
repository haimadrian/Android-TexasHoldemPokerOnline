package org.hit.android.haim.calc.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.hit.android.haim.calc.R;
import org.hit.android.haim.calc.activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FragmentBasic extends Fragment {
    private boolean isInitialized;

    public FragmentBasic() {
        // Required empty public constructor
        Log.d("Lifecycle", this.toString() + ".new");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Lifecycle", this.toString() + ".onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Lifecycle", this.toString() + ".onViewCreated");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_basic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("Lifecycle", this.toString() + ".onViewCreated");

        if (!isInitialized) {
            isInitialized = true;
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
            view.findViewById(R.id.buttonDiv).setOnClickListener(mainActivity::onOperatorButtonClicked);
            view.findViewById(R.id.buttonMinus).setOnClickListener(mainActivity::onOperatorButtonClicked);
            view.findViewById(R.id.buttonPlus).setOnClickListener(mainActivity::onOperatorButtonClicked);
            view.findViewById(R.id.buttonEquals).setOnClickListener(mainActivity::onEqualsButtonClicked);
            view.findViewById(R.id.buttonClear).setOnClickListener(mainActivity::onClearButtonClicked);
        } else {
            Log.d("Lifecycle", this.toString() + ".onViewCreated skipped");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Lifecycle", this.toString() + ".onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Lifecycle", this.toString() + ".onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Lifecycle", this.toString() + ".onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("Lifecycle", this.toString() + ".onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("Lifecycle", this.toString() + ".onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle", this.toString() + ".onDestroy");
    }
}