package com.example.emergencyambulancebookingapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Route_User extends AppCompatActivity implements View.OnClickListener {
    private Handler handler = new Handler();
    LottieAnimationView mProgressBar;
    LinearLayout mLinearLayout;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private String userID, paymentAmount;
    private TextView paymentAmountDsp, comment, hospitalName, driverName, driverContactNumber, driverAmbulanceCategory, ambulanceNumber;
    Dialog paymentDialog, ratingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_user);

        fAuth = FirebaseAuth.getInstance();         // for Authentication
        fStore = FirebaseFirestore.getInstance();   // Initialize Cloud Firestore
        userID = fAuth.getCurrentUser().getUid();

        mProgressBar = findViewById(R.id.progressBarId);
        mLinearLayout = findViewById(R.id.linearLayoutId);

        comment = findViewById(R.id.commentId);
        hospitalName = findViewById(R.id.hospitalNameId);
        driverName = findViewById(R.id.driverNameId);
        driverContactNumber = findViewById(R.id.driverContactNumberid);
        driverAmbulanceCategory = findViewById(R.id.driverAmbulanceCategoryId);
        ambulanceNumber = findViewById(R.id.ambulanceNumberId);

        paymentDialog = new Dialog(this);
        ratingDialog = new Dialog(this);

        try {
            showDriverInfo();
        } catch (Exception e) {
            Toast.makeText(this, "ERROR 1", Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
        try {
            rideComplete();
        } catch (Exception e) {
            Toast.makeText(this, "ERROR 2", Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }

    }

    private void showDriverInfo() {

        try {
            DocumentReference documentRef = fStore.collection("users").document(userID).collection("tempRideInformation").document("tempRideInformation");

            // Set a flag variable to keep track of whether the field value is assigned
            AtomicBoolean isFieldValueAssigned = new AtomicBoolean(false);

            // Start a timer for a certain period
            new Handler().postDelayed(() -> {
                if (isFieldValueAssigned.get()) {
                    // Field value is assigned
                    // Perform the action for field value assigned
                    performActionForValueAssigned();
                } else {
                    // Field value is not assigned within the specified time
                    // Perform the action for no value assigned
                    performActionForNoValueAssigned();
                }
            }, 20000);

            documentRef.addSnapshotListener((documentSnapshot, error) -> {
                if (error != null) {
                    // An error occurred, handle the error
                    Toast.makeText(Route_User.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    if (documentSnapshot.contains("bookingId")) {
                        Object fieldValue = documentSnapshot.get("bookingId");

                        if (fieldValue != null && !fieldValue.equals("")) {
                            // The field has a value assigned
                            isFieldValueAssigned.set(true);

                            // Stop the timer and perform the action for field value assigned
                            // by removing the callbacks from the handler
                            handler.removeCallbacksAndMessages(null);
                            performActionForValueAssigned();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "ERROR 3", Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }

    private void performActionForValueAssigned() {
        try {
            // Perform your action for field value assigned
            // This method will be called when the field value is assigned

            fetchInformation();

            mProgressBar.setVisibility(View.GONE);
            mLinearLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(this, "ERROR 4", Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }

    private void performActionForNoValueAssigned() {
        try
        {
            // Perform your action for no value assigned
            // This method will be called when the field value is not assigned within the specified time

            Toast.makeText(Route_User.this, "The specific field exists but has a null value", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "ERROR 5", Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }

    private void fetchInformation() {
        try
        {
            // Fetch Request information From Firebase
            DocumentReference DocumentReference = fStore.collection("users").document(userID).collection("tempRideInformation").document("tempRideInformation");
            DocumentReference.addSnapshotListener(Route_User.this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        // An error occurred, handle the error
                        Toast.makeText(Route_User.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {

                        comment.setText("You are on route to");
                        hospitalName.setText(documentSnapshot.getString("hospitalName"));
                        driverName.setText("Driver: " + documentSnapshot.getString("driverProfileName"));
                        driverContactNumber.setText("Driver Number: " + documentSnapshot.getString("driverContactNumber"));
                        driverAmbulanceCategory.setText("Category: " + documentSnapshot.getString("ambulanceCategory"));
                        ambulanceNumber.setText("Ambulance Number: " + documentSnapshot.getString("Ambulance Number"));
                        paymentAmountDsp.setText(documentSnapshot.getString("fare"));
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "ERROR 6", Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onClick(View v) {

    }

    private void rideComplete() {
        try {
            DocumentReference documentRef = fStore.collection("users").document(userID).collection("tempRideInformation").document("tempRideInformation");

            documentRef.addSnapshotListener((documentSnapshot, error) -> {
                if (error != null) {
                    // An error occurred, handle the error
                    Toast.makeText(Route_User.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String status = documentSnapshot.getString("status");

                    if (status != null && status.equals("ENDED")) {
                        // The 'status' field value is 'ENDED'
                        // Trigger the activity here
                        paymentPopUp();
                    }
                } else {
                    // The document doesn't exist or an error occurred
                    // Handle the error or perform any necessary actions
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "ERROR 7", Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }

    private void paymentPopUp() {try
        {
            paymentDialog.setContentView(R.layout.paymentpopup);
            paymentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            paymentDialog.setCanceledOnTouchOutside(false);

            paymentDialog.show();

            DocumentReference documentRef = fStore.collection("users").document(userID).collection("tempRideInformation").document("tempRideInformation");

            documentRef.addSnapshotListener((documentSnapshot, error) -> {
                if (error != null) {
                    // An error occurred, handle the error
                    Toast.makeText(Route_User.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String status = documentSnapshot.getString("status");

                    if (status != null && status.equals("PAID")) {
                        // The 'status' field value is 'ENDED'
                        // Trigger the activity here
                        ratingPopUp();
                    }
                } else {
                    // The document doesn't exist or an error occurred
                    // Handle the error or perform any necessary actions
                }
            });

        } catch (Exception e) {
        Toast.makeText(this, "ERROR 8", Toast.LENGTH_SHORT).show();
        throw new RuntimeException(e);
    }
    }

    private void ratingPopUp() {try
        {
            ratingDialog.setContentView(R.layout.ratingpopup);
            ratingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ratingDialog.setCanceledOnTouchOutside(false);

            RatingBar ratingBar = ratingDialog.findViewById(R.id.ratingBarId);
            TextView ratingDsp = ratingDialog.findViewById(R.id.ratingDspId);
            Button submitbtn = ratingDialog.findViewById(R.id.submitbtnId);

            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    ratingDsp.setText(String.valueOf(rating));
                }
            });

//        Clicked on Submit Button
            submitbtn.setOnClickListener(v -> {
                DocumentReference additionalDataRef = fStore.collection("users").document(userID).collection("tempRideInformation").document("tempRideInformation");

                // Create a map with the additional data
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("status", "COMPLETED");

                // Store the additional data in the new document reference
                additionalDataRef.set(additionalData, SetOptions.merge())
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "THANK YOU, GOOD LIVE, SAFE LIFE", Toast.LENGTH_SHORT).show();
                            paymentPopUp();
                        })
                        .addOnFailureListener(error -> {
                            Toast.makeText(this, "Failed to store additional data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                ratingDialog.dismiss(); // Dismiss the popup
            });

            ratingDialog.show();
        } catch (Exception e) {
        Toast.makeText(this, "ERROR 9", Toast.LENGTH_SHORT).show();
        throw new RuntimeException(e);
    }
    }


    @Override
    public void onBackPressed() {

    }


}