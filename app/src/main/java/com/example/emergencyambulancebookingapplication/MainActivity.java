package com.example.emergencyambulancebookingapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        fAuth = FirebaseAuth.getInstance();


        // Check if user is signed in (non-null)
        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

    }

    public void signin(View view) {
        startActivity(new Intent(MainActivity.this, SignInActivity.class));
    }

    public void signup(View view) {
        startActivity(new Intent(MainActivity.this, SignUpActivity.class));
    }
}