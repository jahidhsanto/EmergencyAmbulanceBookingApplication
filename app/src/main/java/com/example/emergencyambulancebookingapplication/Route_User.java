package com.example.emergencyambulancebookingapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class Route_User extends AppCompatActivity implements View.OnClickListener {
    private Handler handler;
    LottieAnimationView mProgressBar;
    LinearLayout mLinearLayout;
    ImageButton mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_user);

        mProgressBar = findViewById(R.id.progressBarId);
        mLinearLayout = findViewById(R.id.linearLayoutId);
        mNextButton = findViewById(R.id.nextButtonId);

//Loading====
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.VISIBLE);
                mNextButton.setVisibility(View.VISIBLE);
            }
        }, 20000);
//Loading====

        mNextButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.nextButtonId) {
            //Bottom Sheet===================
            BottomSheetDialog sheetDialog;

            sheetDialog = new BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme);

            sheetDialog.setContentView(R.layout.sheet_layout);

            sheetDialog.show();
            //Bottom Sheet===================
        }

    }
}