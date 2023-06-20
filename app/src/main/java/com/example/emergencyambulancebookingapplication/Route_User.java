package com.example.emergencyambulancebookingapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
    private String userID, paymentAmount, driverUserId;
    private TextView comment, hospitalNameTextView, driverName, driverContactNumber, driverAmbulanceCategory, ambulanceNumberTextView;
    Dialog paymentDialog, ratingDialog;


    String bookingId, customBookingId, ambulanceCategory, ambulanceNumber, date, hospitalName, status, time;
    LatLng dropOffLatLng, pickUpLatLng;
    String fare, distance, rating;


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
        hospitalNameTextView = findViewById(R.id.hospitalNameId);
        driverName = findViewById(R.id.driverNameId);
        driverContactNumber = findViewById(R.id.driverContactNumberid);
        driverAmbulanceCategory = findViewById(R.id.driverAmbulanceCategoryId);
        ambulanceNumberTextView = findViewById(R.id.ambulanceNumberId);

        paymentDialog = new Dialog(this);
        ratingDialog = new Dialog(this);

        showDriverInfo();
        rideComplete();
        generateBookingId();

    }

    private void showDriverInfo() {

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
    }

    private void performActionForValueAssigned() {
        // Perform your action for field value assigned
        // This method will be called when the field value is assigned

        fetchInformation();

        mProgressBar.setVisibility(View.GONE);
        mLinearLayout.setVisibility(View.VISIBLE);
        hospitalNameTextView.setVisibility(View.VISIBLE);
    }

    private void performActionForNoValueAssigned() {
        // Perform your action for no value assigned
        // This method will be called when the field value is not assigned within the specified time

        Toast.makeText(Route_User.this, "The specific field exists but has a null value", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void fetchInformation() {
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
                    hospitalNameTextView.setText(documentSnapshot.getString("hospitalName"));
                    driverName.setText("Driver: " + documentSnapshot.getString("driverProfileName"));
                    driverContactNumber.setText("Driver Number: " + documentSnapshot.getString("driverContactNumber"));
                    driverAmbulanceCategory.setText("Category: " + documentSnapshot.getString("ambulanceCategory"));
                    ambulanceNumberTextView.setText("Ambulance Number: " + documentSnapshot.getString("Ambulance Number"));

//                // Fetch the 'pickUpLatLng' field
//                Map<String, Object> pickUpLatLng = (Map<String, Object>) documentSnapshot.get("dropOffLatLng");
//                // Fetch the 'latitude' and 'longitude' subfields
//                latitude = (double) pickUpLatLng.get("latitude");
//                longitude = (double) pickUpLatLng.get("longitude");
//                startLocation = new LatLng(latitude, longitude);
//                pickAddr.setText(getLocationName(startLocation));
//
//                // Fetch the 'dropOffLatLng' field
//                Map<String, Object> dropOffLatLng = (Map<String, Object>) documentSnapshot.get("dropOffLatLng");
//                // Fetch the 'latitude' and 'longitude' subfields
//                latitude = (double) dropOffLatLng.get("latitude");
//                longitude = (double) dropOffLatLng.get("longitude");
//                endLocation = new LatLng(latitude, longitude);
//                destAddr.setText(getLocationName(endLocation));


                    paymentAmount = String.valueOf(documentSnapshot.getLong("fare"));
                    driverUserId = documentSnapshot.getString("driverId");


                    bookingId = documentSnapshot.getString("bookingId");
                    ambulanceCategory = documentSnapshot.getString("ambulanceCategory");
                    ambulanceNumber = documentSnapshot.getString("ambulanceNumber");
                    date = documentSnapshot.getString("date");
                    hospitalName = documentSnapshot.getString("hospitalName");
                    status = documentSnapshot.getString("status");
                    time = documentSnapshot.getString("time");

//                    dropOffLatLng = documentSnapshot.getString("dropOffLatLng");
//                    pickUpLatLng = documentSnapshot.getString("pickUpLatLng");

                    fare = String.valueOf(documentSnapshot.getLong("fare"));
                    distance = String.valueOf(documentSnapshot.getLong("distance"));

                }
            }
        });
    }


    @Override
    public void onClick(View v) {

    }

    private void rideComplete() {
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
    }

    private void paymentPopUp() {
        paymentDialog.setContentView(R.layout.paymentpopup);
        paymentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        paymentDialog.setCanceledOnTouchOutside(false);

        TextView paymentAmountDsp = paymentDialog.findViewById(R.id.paymentAmountDspId);
        paymentAmountDsp.setText(paymentAmount + " TK");

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

    }

    private void ratingPopUp() {
        ratingDialog.setContentView(R.layout.ratingpopup);
        ratingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ratingDialog.setCanceledOnTouchOutside(false);

        RatingBar ratingBar = ratingDialog.findViewById(R.id.ratingBarId);
        TextView ratingDsp = ratingDialog.findViewById(R.id.ratingDspId);
        Button submitbtn = ratingDialog.findViewById(R.id.submitbtnId);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float ratingValue, boolean fromUser) {
                rating = String.valueOf(ratingValue);
                ratingDsp.setText(String.valueOf(ratingValue));
            }
        });

        // Clicked on Submit Button
        submitbtn.setOnClickListener(v -> {
            DocumentReference additionalDataRef = fStore.collection("users").document(userID).collection("tempRideInformation").document("tempRideInformation");

            // Create a map with the additional data
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("status", "COMPLETED");
            additionalData.put("rating", rating);

            // Store the additional data in the new document reference
            additionalDataRef.set(additionalData, SetOptions.merge())
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "THANK YOU, GOOD LIVE, SAFE LIFE", Toast.LENGTH_SHORT).show();
                        storeRide();
                    })
                    .addOnFailureListener(error -> {
                        Toast.makeText(this, "Failed to store additional data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            ratingDialog.dismiss(); // Dismiss the popup
        });
        paymentDialog.dismiss(); // Dismiss payment popup
        ratingDialog.show();
    }

    private void storeRide() {
        // Create a new document reference for Ride History
        DocumentReference additionalDataRef = fStore.collection("bookings").document(customBookingId);
        // Create a map with the additional data
        Map<String, Object> additionalData = new HashMap<>();

        additionalData.put("bookingId", bookingId);
        additionalData.put("userId", userID);
        additionalData.put("driverId", driverUserId);
        additionalData.put("ambulanceCategory", ambulanceCategory);
        additionalData.put("ambulanceNumber", ambulanceNumber);
        additionalData.put("date", date);
        additionalData.put("time", time);
        additionalData.put("distance", distance);
        additionalData.put("dropOffLatLng", "dropOffLatLng");
        additionalData.put("pickUpLatLng", "pickUpLatLng");
        additionalData.put("fare", paymentAmount);
        additionalData.put("hospitalName", hospitalName);
        additionalData.put("rating", rating);
        additionalData.put("status", status);


        // Store the additional data in the new document reference
        additionalDataRef.set(additionalData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "THANK YOU FOR SAVE OUR LIFE", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Route_User.this, ProfileActivity.class));

                    // Delete the temporary ride request
                    CollectionReference collectionRef = fStore.collection("users").document(userID).collection("tempRideInformation");
                    collectionRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            documentSnapshot.getReference().delete();
                        }
                    }).addOnFailureListener(e -> {
                        // Handle the failure
                    });
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(this, "Failed!!! Try Again\n" + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Create a method to generate the next booking ID
    private void generateBookingId() {
        CollectionReference bookingsCollection = fStore.collection("bookings");
        bookingsCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();

                    // Generate the next booking ID
                    customBookingId = "B_" + String.format("%02d", count + 1) + "_" + fStore.collection("bookings").document().getId();
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to retrieve the bookings collection
                });
    }

    @Override
    public void onBackPressed() {

    }


}