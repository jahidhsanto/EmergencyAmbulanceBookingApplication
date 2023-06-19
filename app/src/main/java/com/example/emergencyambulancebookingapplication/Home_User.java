package com.example.emergencyambulancebookingapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.collection.ArraySet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home_User extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    Button bookAmbulanceBtn;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap mMap;
    private ArraySet<LatLng> listPoints;
    private LatLng startLocation, endLocation;
    private SearchView mapSearchView;
    private FirebaseAuth fAuth;

    private String userID, ambulanceCategory;
    private FirebaseFirestore fStore;
    private ImageView profileImage;
    private StorageReference storageReference;
    private Geocoder geocoder;
    private Address address;
    private AlertDialog.Builder alertDialogBuilder;
    CardView BLScard, PTScard, ALScard;
    LinearLayout BLSlinear, PTSlinear, ALSlinear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);

        // Navigation Bar ======================

        Toolbar toolbar = findViewById(R.id.toolbar); //Ignore red line errors
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileActivity()).commit();
//            navigationView.setCheckedItem(R.id.nav_home);
//        }

//        if (savedInstanceState == null) {
//            Intent intent = new Intent(this, ProfileActivity.class);
//            startActivity(intent);
//            navigationView.setCheckedItem(R.id.nav_homeId);
//        }


        // Navigation Bar ======================

        profileImage = findViewById(R.id.profileImageId);
        bookAmbulanceBtn = findViewById(R.id.bookAmbulanceButtonId);
        mapSearchView = findViewById(R.id.mapSearchViewId);

        BLScard = findViewById(R.id.BLScardId);
        BLSlinear = findViewById(R.id.BLSlinearId);
        PTScard = findViewById(R.id.PTScardId);
        PTSlinear = findViewById(R.id.PTSlinearId);
        ALScard = findViewById(R.id.ALScardId);
        ALSlinear = findViewById(R.id.ALSlinearId);

        fAuth = FirebaseAuth.getInstance();         // for Authentication
        fStore = FirebaseFirestore.getInstance();   // for store information
        storageReference = FirebaseStorage.getInstance().getReference();    // for store image

        // Button for book Ambulance
        bookAmbulanceBtn.setOnClickListener(this);

        // Action for choosing Ambulance Category
        BLScard.setOnClickListener(this);
        BLSlinear.setOnClickListener(this);
        PTScard.setOnClickListener(this);
        PTSlinear.setOnClickListener(this);
        ALScard.setOnClickListener(this);
        ALSlinear.setOnClickListener(this);

        showProfileImage();
        customMap();
    }

    private void showProfileImage() {
        // show user Profile Image
        StorageReference profileRef = storageReference.child("users/" + fAuth.getCurrentUser().getUid() + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        });
    }

    private void customMap() {
        // Asking user if explanation is needed
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services available.");
        }

        // Search places from SearchView field
        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // get location name which location want to search
                String location = mapSearchView.getQuery().toString();
                List<Address> addressList = null;


                if (location != null) {
                    Geocoder geocoder = new Geocoder(Home_User.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Address address = addressList.get(0);
                    // get Latitude & Longitude position for given address
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    // Add First Marker =============================

                    // Reset marker when already 2
                    if (listPoints.size() == 2) {
                        listPoints.clear();
                        mMap.clear();
                    }

                    // Save first point select
                    listPoints.add(latLng);

                    // Create marker
                    MarkerOptions markerOptions = new MarkerOptions();
                    // Add first marker
                    markerOptions.position(latLng);

                    if (listPoints.size() == 1) {
                        // set marker color for 1st location
                        startLocation = new LatLng(latLng.latitude, latLng.longitude);
                        Address locateAddress = getLocationName(startLocation);

                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                        assert locateAddress != null;
                        if (!locateAddress.getCountryCode().equals("BD")) {
                            getAlert();
                        }
                    }

                    // Add Second Marker =============================
                    else {
                        // Add second marker
                        endLocation = new LatLng(latLng.latitude, latLng.longitude);
                        Address locateAddress = getLocationName(endLocation);

                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                        assert locateAddress != null;
                        if (!locateAddress.getCountryCode().equals("BD")) {
                            getAlert();
                        }
                    }

                    // Add marker in the map
                    mMap.addMarker(markerOptions);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                    // Calculate the distance between the two markers
                    if (listPoints.size() == 2) {
                        getDistance();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);      // Once the mapFragment is initialized, the getMapAsync(this) method sets up a callback to the onMapReady() method in the current class (this).
        listPoints = new ArraySet<>();
    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, 0).show();
            }
            return false;
        }
        return true;
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onClick(View v) {

        if ((v.getId() == R.id.BLScardId) || (v.getId() == R.id.BLSlinearId)) {
            ambulanceCategory = "BLS";
            Toast.makeText(this, "BLS", Toast.LENGTH_SHORT).show();
        }
        if ((v.getId() == R.id.PTScardId) || (v.getId() == R.id.PTSlinearId)) {
            ambulanceCategory = "PTS";
            Toast.makeText(this, "PTS", Toast.LENGTH_SHORT).show();
        }
        if ((v.getId() == R.id.ALScardId) || (v.getId() == R.id.ALSlinearId)) {
            ambulanceCategory = "ALS";
            Toast.makeText(this, "ALS", Toast.LENGTH_SHORT).show();
        }

        if (v.getId() == R.id.bookAmbulanceButtonId) {

            // check all required filed is filledup
            if (startLocation == null || endLocation == null ||
                    (startLocation.latitude == 0 && startLocation.longitude == 0) ||
                    (endLocation.latitude == 0 && endLocation.longitude == 0)) {
                // LatLng objects are empty or have invalid coordinates
                Toast.makeText(this, "Please select start and end locations", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(ambulanceCategory)) {
                Toast.makeText(this, "Please select ambulance category", Toast.LENGTH_SHORT).show();
                return;
            }

            userID = fAuth.getCurrentUser().getUid();
            CollectionReference collectionReference = fStore.collection("users").document(userID).collection("tempRideInformation");
            // Create a new user with a first, middle, and last name
            Map<String, Object> rideInfo = new HashMap<>();
            rideInfo.put("pickUpLatLng", startLocation);
            rideInfo.put("dropOffLatLng", endLocation);
            rideInfo.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));    // Get the current date
            rideInfo.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm a")));   // Get the current time
            rideInfo.put("distance", getDistance());
            rideInfo.put("ambulanceCategory", ambulanceCategory);
            rideInfo.put("fare", getTotalfare());
            rideInfo.put("status", "PENDING");
            rideInfo.put("bookingId", "");      // just check the any driver booked or not.

            // Add a new document with a generated ID
            collectionReference.document("tempRideInformation").set(rideInfo, SetOptions.merge()).addOnSuccessListener(documentReference -> {
                // Document added successfully
                startActivity(new Intent(Home_User.this, AmbulanceList.class));
                Log.d("TAG", "Ride information with ID: " + userID);
            }).addOnFailureListener(e -> {
                // Error adding document
                Log.e("TAG", "Error adding information", e);
            });


        }
    }

    private float getTotalfare() {
        if (getDistance() <= 5) {
            return 1200;
        } else {
            switch (ambulanceCategory) {
                case "BLS":
                    return getDistance() * 300;
                case "PTS":
                    return getDistance() * 500;
                case "ALS":
                    return getDistance() * 700;
            }
        }
        return 0;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Map zoomControll Button
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED

                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If location access is not given then this will execute.
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION.LOCATION_REQUEST});
            Toast.makeText(this, "FIND ERROR", Toast.LENGTH_LONG).show();
            return;
        }
        // Current Location button
        mMap.setMyLocationEnabled(true);


        // Long click on map
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                // Add First Marker =============================
                Toast.makeText(Home_User.this, "LONG CLICKED", Toast.LENGTH_SHORT).show();
                // Reset marker when already 2
                if (listPoints.size() == 2) {
                    listPoints.clear();
                    mMap.clear();
                }

                // Save first point select
                listPoints.add(latLng);

                // Create marker
                MarkerOptions markerOptions = new MarkerOptions();
                // Add first marker
                markerOptions.position(latLng);

                if (listPoints.size() == 1) {
                    // set marker color for 1st location
                    startLocation = new LatLng(latLng.latitude, latLng.longitude);
                    Address locateAddress = getLocationName(startLocation);

                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                    assert locateAddress != null;
                    if (!locateAddress.getCountryCode().equals("BD")) {
                        getAlert();
                    }

                }

                // Add Second Marker =============================
                else {
                    // Add second marker
                    endLocation = new LatLng(latLng.latitude, latLng.longitude);
                    Address locateAddress = getLocationName(endLocation);

                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                    assert locateAddress != null;
                    if (!locateAddress.getCountryCode().equals("BD")) {
                        getAlert();
                    }
                }

                // Add marker in the map
                mMap.addMarker(markerOptions);

                // Calculate the distance between the two markers
                if (listPoints.size() == 2) {
                    getDistance();
                }

                //TODO: request to direction code below
                if (listPoints.size() == 2) {
                    // Create the URL to get request from first marker to second marker
                    String uri = getRequestUri(startLocation, endLocation);
                }
            }
        });
    }

    // Get distance using Latitude & Longitude
    private float getDistance() {
        Location loc1 = new Location("");
        loc1.setLatitude(startLocation.latitude);
        loc1.setLongitude(startLocation.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(endLocation.latitude);
        loc2.setLongitude(endLocation.longitude);

        float distance = loc1.distanceTo(loc2) / 1000;      // distance in meters

        Toast.makeText(this, "Distance: \n " + distance + " KM", Toast.LENGTH_SHORT).show();
        return distance;
    }

    private String getRequestUri(LatLng origin, LatLng dest) {
        // Value of origin
        String str_org = "orign=" + origin.latitude + "," + origin.longitude;
        // Value of destination
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Set value enable the sensor
        String sensor = "sensor-false";
        //Mode for find direction
        String mode = "mode=driving";
        // Build the full param
        String param = str_org + "&" + str_dest + "&" + sensor + "&" + mode;
        // Output format
        String output = "json";
        // Create url to request direction from google map api
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
//        Toast.makeText(this, "Got Direction: " + url, Toast.LENGTH_SHORT).show();
        return url;
    }


    // Show Alert Dialog
    // This will show when select outside of bangladesh
    private void getAlert() {
        alertDialogBuilder = new AlertDialog.Builder(Home_User.this);

        // for setting title
        alertDialogBuilder.setTitle("Warning");

        // for setting message
        alertDialogBuilder.setMessage("Our service is only in Bangladesh." +
                "\nYou selected " + address.getCountryName() +
                "\nPlease reselect the place");

        // for setting Icon
        alertDialogBuilder.setIcon(R.drawable.ic_launcher_foreground);

        alertDialogBuilder.setCancelable(false);

        // for setting positive button
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    // Get the place name from latitude and longitude
    private Address getLocationName(LatLng latLng) {
        geocoder = new Geocoder(Home_User.this);

        try {
            // Get the addresses for the given latitude and longitude
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                address = addresses.get(0);

                // Use the place name as needed
                Toast.makeText(this,
                        "Feature Name: " + address.getFeatureName()
                                + "\nCountry: " + address.getCountryName()
                                + "\nCountryCode: " + address.getCountryCode(), Toast.LENGTH_SHORT).show();
                return address;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_homeId) {
            startActivity(new Intent(this, Home_User.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (item.getItemId() == R.id.nav_bookAmbulanceId) {
            startActivity(new Intent(this, Home_User.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (item.getItemId() == R.id.nav_profileId) {
            startActivity(new Intent(this, ProfileSetting.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (item.getItemId() == R.id.nav_logoutId) {
            fAuth.signOut();
            startActivity(new Intent(this, MainActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        }


//            switch (item.getItemId()) {
//                case R.id.nav_homeId:
//                    startActivity(new Intent(this, Home_User.class));
//                    break;
//                case R.id.nav_bookAmbulanceId:
//                    startActivity(new Intent(this, ProfileActivity.class));
//                    break;
//                case R.id.nav_profileId:
//                    startActivity(new Intent(this, ProfileActivity.class));
//                    break;
//                case R.id.nav_familyMemberId:
//                    startActivity(new Intent(this, ProfileActivity.class));
//                    break;
//                case R.id.nav_emergencyContactsId:
//                    startActivity(new Intent(this, ProfileActivity.class));
//                    break;
//                case R.id.nav_ridehistoryId:
//                    startActivity(new Intent(this, ProfileActivity.class));
//                    break;
//                case R.id.nav_paymentId:
//                    startActivity(new Intent(this, ProfileActivity.class));
//                    break;
//                case R.id.nav_supportId:
//                    startActivity(new Intent(this, ProfileActivity.class));
//                    break;
//                case R.id.nav_faqId:
//                    startActivity(new Intent(this, ProfileActivity.class));
//                    break;
//                case R.id.nav_aboutId:
//                    startActivity(new Intent(this, ProfileActivity.class));
//                    break;
//                case R.id.nav_logoutId:
//                    Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
//                    break;
//            }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}