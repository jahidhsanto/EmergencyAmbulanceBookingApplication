package com.example.emergencyambulancebookingapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private EditText mfullName, mEmail, mPhone, mPassword01, mPassword02;
    private Button mRegisterBtn;
    private TextView mLoginBtn;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private LottieAnimationView progressBar;
    private String userID;
    CardView card1, card2, card3, card4, cardButtonSignUp;
    private boolean is8char = false, hasUpper = false, hasnum = false, hasSpecialSymbol = false, isSignupClickable = false;
    private LinearLayout passwordQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mfullName = findViewById(R.id.fullNameId);
        mEmail = findViewById(R.id.emailId);
        mPhone = findViewById(R.id.phoneId);
        mPassword01 = findViewById(R.id.pass01Id);
        mPassword02 = findViewById(R.id.pass02Id);
        mLoginBtn = findViewById(R.id.signinTxtId);
        mRegisterBtn = findViewById(R.id.signupBtnId);

        card1 = (CardView) findViewById(R.id.card1);
        card2 = (CardView) findViewById(R.id.card2);
        card3 = (CardView) findViewById(R.id.card3);
        card4 = (CardView) findViewById(R.id.card4);
        passwordQuality = findViewById(R.id.passwordQualityId);

        inputChanged();

        // Initialize Firebase Auth
        fAuth = FirebaseAuth.getInstance();

        // Initialize Cloud Firestore
        fStore = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressBarId);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = mfullName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String phone = mPhone.getText().toString().trim();
                String password01 = mPassword01.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is Required.");
                    return;
                }
                if (TextUtils.isEmpty(password01)) {
                    mPassword01.setError("Password is Required.");
                    return;
                }
                if (password01.length() < 6) {
                    mPassword01.setError("Password Must be >= 6 Characters");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                // register the user in firebase
                fAuth.createUserWithEmailAndPassword(email, password01).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "User Created", Toast.LENGTH_SHORT).show();


                            userID = fAuth.getCurrentUser().getUid();
                            CollectionReference collectionReference = fStore.collection("users").document(userID).collection("profileInformation");
                            // Create a new user with a first, middle, and last name
                            Map<String, Object> user = new HashMap<>();
                            user.put("fName", fullName);
                            user.put("email", email);
                            user.put("phone", phone);

                            // Add a new document with a generated ID
                            collectionReference.document("profileInformation").set(user).addOnSuccessListener(documentReference -> {
                                // Document added successfully
                                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                                Log.d("TAG", "onSuccess: user Profile is created for " + userID);

                            }).addOnFailureListener(e -> {
                                // Error adding document
                                Log.e("TAG", "Error adding document", e);
                            });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            }
        });

    }

    private void inputChanged() {

        mPassword01.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("ResourceType")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordValidate();
                if (is8char && hasnum && hasSpecialSymbol && hasUpper) {
                    new Handler().postDelayed(() -> passwordQuality.setVisibility(View.GONE), 2000);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
//
        });
//
        mPassword02.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("ResourceType")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password1 = mPassword01.getText().toString();
                String password2 = mPassword02.getText().toString();
                if (password1.equals(password2)) {
                    mRegisterBtn.setBackgroundResource(R.drawable.button_blue);
                    mRegisterBtn.setEnabled(true);
                } else {
                    mRegisterBtn.setBackgroundResource(R.drawable.button_disable);
                    mRegisterBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
//
        });

    }

    @SuppressLint("ResourceType")
    private void passwordValidate() {
        passwordQuality.setVisibility(View.VISIBLE);

        String password = mPassword01.getText().toString();


        // 8 character
        if (password.length() >= 8) {
            is8char = true;
            card1.setCardBackgroundColor(Color.parseColor(getString(R.color.colorAccent)));
        } else {
            is8char = false;
            card1.setCardBackgroundColor(Color.parseColor(getString(R.color.colorGrey)));
        }

        //number
        if (password.matches("(.*[0-9].*)")) {
            hasnum = true;
            card2.setCardBackgroundColor(Color.parseColor(getString(R.color.colorAccent)));
        } else {
            hasUpper = false;
            card2.setCardBackgroundColor(Color.parseColor(getString(R.color.colorGrey)));
        }
        //upper case
        if (password.matches("(.*[A-Z].*)")) {
            hasUpper = true;
            card3.setCardBackgroundColor(Color.parseColor(getString(R.color.colorAccent)));
        } else {
            hasUpper = false;
            card3.setCardBackgroundColor(Color.parseColor(getString(R.color.colorGrey)));
        }

        //symbol
        if (password.matches("^(?=.*[_.()$&@]).*$")) {
            hasSpecialSymbol = true;
            card4.setCardBackgroundColor(Color.parseColor(getString(R.color.colorAccent)));
        } else {
            hasSpecialSymbol = false;
            card4.setCardBackgroundColor(Color.parseColor(getString(R.color.colorGrey)));
        }
//
    }

}