package com.example.emergencyambulancebookingapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class Route_User extends AppCompatActivity implements View.OnClickListener {
    private Handler handler;
    LottieAnimationView mProgressBar;
    LinearLayout mLinearLayout;
    ImageButton mNextButton;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private String userID;
    private TextView driverName, driverContactNumber, driverAmbulanceCategory, ambulanceNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_user);

        fAuth = FirebaseAuth.getInstance();         // for Authentication
        fStore = FirebaseFirestore.getInstance();   // Initialize Cloud Firestore
        userID = fAuth.getCurrentUser().getUid();

        // Check request data available or not
        DocumentReference docRef = fStore.collection("users").document(userID).collection("tempRideInformation").document("tempRideInformation");

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document exists
                    mProgressBar = findViewById(R.id.progressBarId);
                    mLinearLayout = findViewById(R.id.linearLayoutId);
                    mNextButton = findViewById(R.id.nextButtonId);

                    driverName = findViewById(R.id.driverNameId);
                    driverContactNumber = findViewById(R.id.driverContactNumberid);
                    driverAmbulanceCategory = findViewById(R.id.driverAmbulanceCategoryId);
                    ambulanceNumber = findViewById(R.id.ambulanceNumberId);

                    showDriverInfo();

                    mNextButton.setOnClickListener(this);

                } else {
                    // Document does not exist
                    // Perform alternative logic or show an error message
                    Toast.makeText(this, "Please start over", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, Home_User.class));
                }
            } else {
                // An error occurred while retrieving the document
                // Handle the error
                onBackPressed();
            }
        });
    }

    private void showDriverInfo() {

        DocumentReference documentRef = fStore.collection("users").document(userID).collection("tempRideInformation").document("tempRideInformation");
        documentRef.addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot.contains("driverProfileName")) {
                Object fieldValue = documentSnapshot.get("driverProfileName");

                if (fieldValue != null) {
                    // The specific field exists and has a non-null value

                    fetchInformation();

                    mProgressBar.setVisibility(View.GONE);
                    mLinearLayout.setVisibility(View.VISIBLE);
                    mNextButton.setVisibility(View.VISIBLE);
                } else {
                    // The specific field exists but has a null value
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Route_User.this, "Please select another Ambulance", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }, 120000);     // wait for 2 minutes
                }
            } else {
                // The specific field does not exist in the document
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Route_User.this, "Please select another Ambulance", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, 120000);     // wait for 2 minutes
            }
        });


    }

    private void fetchInformation() {
        // Fetch Request information From Firebase
        DocumentReference documentOfSender = fStore.collection("users").document(userID).collection("tempRideInformation").document("tempRideInformation");
        documentOfSender.addSnapshotListener(Route_User.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                driverName.setText("Driver: " + documentSnapshot.getString("driverProfileName"));
                driverContactNumber.setText("Driver Number: " + documentSnapshot.getString("driverContactNumber"));
                driverAmbulanceCategory.setText("Category: " + documentSnapshot.getString("ambulanceCategory"));
                ambulanceNumber.setText("Ambulance Number: " + documentSnapshot.getString("Ambulance Number"));
            }
        });
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

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            // Delete the temporary ride request
            CollectionReference collectionRef = fStore.collection("users").document(userID).collection("tempRideInformation");
            collectionRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    documentSnapshot.getReference().delete();
                }
            }).addOnFailureListener(e -> {
                // Handle the failure
            });

            super.onBackPressed(); // Call the superclass method to finish the activity
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to back", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);


    }


}