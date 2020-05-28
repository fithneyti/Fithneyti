package com.example.fithneyti;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;

public class sign extends AppCompatActivity {
    Button signIn,signUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
         signIn = findViewById(R.id.signIn);
         signUp = findViewById(R.id.signUp);

        Fragment fragment;
        fragment = new SignUpFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.SignUpFragment,fragment);
        ft.commit();
    }

    public void changeFragment(View view){
        Fragment fragment;
        if (view == findViewById(R.id.signIn)){
            signIn.setBackgroundResource(R.drawable.signcolor);
            signUp.setBackgroundColor(Color.parseColor("white"));
            fragment = new signInFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.SignUpFragment,fragment);
            ft.commit();
        }
        if (view == findViewById(R.id.signUp)){
            signUp.setBackgroundResource(R.drawable.signcolor);
            signIn.setBackgroundColor(Color.parseColor("white"));
            fragment = new SignUpFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.SignUpFragment,fragment);
            ft.commit();
        }
    }
}
