package com.example.emergencyambulancebookingapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Calling extends AppCompatActivity {
    private TextView textViewEmergencyNumber;
    private Button buttonEmergencyCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        textViewEmergencyNumber = findViewById(R.id.textViewEmergencyNumber);
        buttonEmergencyCall = findViewById(R.id.buttonEmergencyCall);

        // Set the emergency number
        final String emergencyNumber = "+880123456789";
        textViewEmergencyNumber.setText("Emergency Number: " + emergencyNumber);

        // Set click listener for the call button
        buttonEmergencyCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEmergencyCall(emergencyNumber);
            }
        });
    }

    private void makeEmergencyCall(String emergencyNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + emergencyNumber));
        startActivity(intent);
    }
}