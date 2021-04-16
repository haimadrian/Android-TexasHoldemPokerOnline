package org.hit.android.haim.chat.client.fragment;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.hit.android.haim.chat.client.R;
import org.hit.android.haim.chat.client.activity.MainActivity;
import org.hit.android.haim.chat.client.bean.User;

import java.time.LocalDate;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FragmentConnect extends Fragment {

    private final String email;
    private final String name;
    private final String dateOfBirth;
    private final User.Gender gender;

    private EditText editTextEmail;
    private EditText editTextName;
    private EditText editTextDateOfBirth;
    private Spinner spinnerGender;

    public FragmentConnect() {
        this("", "", "", User.Gender.Male);
    }

    public FragmentConnect(String email, String name, String dateOfBirth, User.Gender gender) {
        Log.d("Lifecycle", this.toString() + ".new");
        this.email = email;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Lifecycle", this.toString() + ".onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextEmail = view.findViewById(R.id.editTextEmailConnect);
        editTextName = view.findViewById(R.id.editTextNameConnect);
        editTextDateOfBirth = view.findViewById(R.id.editTextDateOfBirthConnect);
        spinnerGender = view.findViewById(R.id.spinnerGenderConnect);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.gender_options, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
        spinnerGender.setAdapter(adapter);

        editTextEmail.setText(email);
        editTextName.setText(name);
        editTextDateOfBirth.setText(dateOfBirth);
        spinnerGender.setSelection(gender.ordinal());

        MainActivity mainActivity = (MainActivity) getActivity();
        Button buttonGo = view.findViewById(R.id.buttonGoConnect);
        buttonGo.setOnClickListener(v -> {
            if ((mainActivity != null) && verifyInput()) {
                mainActivity.doConnect(editTextEmail.getText().toString().trim(),
                        editTextName.getText().toString().trim(),
                        editTextDateOfBirth.getText().toString().trim(),
                        User.Gender.values()[spinnerGender.getSelectedItemPosition()]);
            }
        });
    }

    private boolean verifyInput() {
        boolean isOk = true;

        if (editTextEmail.getText().toString().trim().isEmpty()) {
            isOk = false;
            editTextEmail.setError("Email is mandatory");
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText().toString().trim()).matches()) {
                isOk = false;
                editTextEmail.setError("Wrong email format. Must be: a@b.c");
            }
        }

        if (editTextName.getText().toString().trim().isEmpty()) {
            isOk = false;
            editTextName.setError("Name is mandatory");
        }

        if (editTextDateOfBirth.getText().toString().trim().isEmpty()) {
            isOk = false;
            editTextDateOfBirth.setError("Date of birth is mandatory");
        } else {
            try {
                LocalDate.parse(editTextDateOfBirth.getText().toString().trim());
            } catch (Exception e) {
                isOk = false;
                editTextDateOfBirth.setError("Wrong format. Must be yyyy-MM-dd");
            }
        }

        return isOk;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle", this.toString() + ".onDestroy");
    }
}