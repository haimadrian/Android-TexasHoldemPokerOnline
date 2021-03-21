package com.example.cw5fragments.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;

import com.example.cw5fragments.R;
import com.example.cw5fragments.fragments.FirstFragment;
import com.example.cw5fragments.fragments.SecondFragment;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
    }

    public void onButtonClicked(View view) {
        fragmentTransaction = fragmentManager.beginTransaction();
        FirstFragment firstFragment = new FirstFragment();
        fragmentTransaction.add(R.id.fragmentFrame, firstFragment).addToBackStack(null).commit();
    }

    public void goToSecondFragment(String param) {
        fragmentTransaction = fragmentManager.beginTransaction();
        SecondFragment secondFragment = SecondFragment.newInstance(param);
        fragmentTransaction.replace(R.id.fragmentFrame, secondFragment).addToBackStack(null).commit();
    }

    public void goToFirstFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        FirstFragment firstFragment = new FirstFragment();
        fragmentTransaction.replace(R.id.fragmentFrame, firstFragment).addToBackStack(null).commit();
    }
}