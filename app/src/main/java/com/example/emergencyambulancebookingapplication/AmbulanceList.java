package com.example.emergencyambulancebookingapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class AmbulanceList extends AppCompatActivity implements RecyclerViewInterface {

    RecyclerView recyclerView;
    ArrayList<Driver> driverArrayList;
    MyAdapter myAdapter;
    FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private String userID;

    ProgressDialog progressDialog;

    private static final String CHANNEL_ID = "my_channel";
    private static final String CHANNEL_NAME = "My Channel";
    private static final String CHANNEL_DESC = "Notification Channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_list);

        fAuth = FirebaseAuth.getInstance();         // for Authentication

        userID = fAuth.getCurrentUser().getUid();

        // Initialize Cloud Firestore
        fStore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.show();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        driverArrayList = new ArrayList<Driver>();
        myAdapter = new MyAdapter(AmbulanceList.this, driverArrayList, this);

        recyclerView.setAdapter(myAdapter);

        fetchDriverList();

    }

    private void fetchDriverList() {
        fStore.collection("drivers")
                .whereEqualTo("status", "ONLINE")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            Log.e("Firestore error", error.getMessage());
                            return;
                        }

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                // Get the user ID for nested profile information
                                String driverUserId = dc.getDocument().getId();
                                // Fetch the nested profile information and pass the user ID
                                fetchProfileInformation(driverUserId);
                            }
                        }
                    }
                });
    }

    private void fetchProfileInformation(String driverUserId) {
        fStore.collection("drivers").document(driverUserId).collection("profileInformation")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Driver driver = documentSnapshot.toObject(Driver.class);

                        // Fetch the corresponding TOKEN for the driver
                        fetchStatus(driverUserId, driver);
                    }
                    myAdapter.notifyDataSetChanged();
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    Log.e("Firestore error", e.getMessage());
                });
    }

    private void fetchStatus(String userId, Driver driver) {
        fStore.collection("drivers").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String TOKEN = documentSnapshot.getString("TOKEN");
                        // Set the status field in the user object
                        driver.setTOKEN(TOKEN);
                        driverArrayList.add(driver);
                        myAdapter.notifyDataSetChanged();
                    }
                    if (progressDialog.isShowing() && driverArrayList.size() == 0)
                        progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    Log.e("Firestore error", e.getMessage());
                });
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, "You Clicked: " + driverArrayList.get(position).getFullName(), Toast.LENGTH_SHORT).show();

        FCMSend.pushNotification(
                AmbulanceList.this,
                driverArrayList.get(position).getTOKEN().toString(),
                "Emergency Ambulance Booking Application",
                "Someone needs your help!!!",
                userID
        );
        startActivity(new Intent(this, Route_User.class));
    }

    @Override
    public void onBackPressed() {
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
    }
}