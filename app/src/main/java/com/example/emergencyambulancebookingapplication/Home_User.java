package com.example.emergencyambulancebookingapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class Home_User extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    Button bookAmbulanceBtn;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap mMap;
    private ArraySet<LatLng> listPoints;
    private LatLng startLocation, endLocation;
    private SearchView mapSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);

        bookAmbulanceBtn = findViewById(R.id.bookAmbulanceButtonId);
        mapSearchView = findViewById(R.id.mapSearchViewId);

        // Button for book Ambulance
        bookAmbulanceBtn.setOnClickListener(this);

        customMap();
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
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        startLocation = new LatLng(latLng.latitude, latLng.longitude);
                    }

                    // Add Second Marker =============================
                    else {
                        // Add second marker
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        endLocation = new LatLng(latLng.latitude, latLng.longitude);
                    }

                    // Add marker in the map
                    mMap.addMarker(markerOptions);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                    // Calculate the distance between the two markers
                    if (listPoints.size() == 2) {
                        Location loc1 = new Location("");
                        loc1.setLatitude(startLocation.latitude);
                        loc1.setLongitude(startLocation.longitude);

                        Location loc2 = new Location("");
                        loc2.setLatitude(endLocation.latitude);
                        loc2.setLongitude(endLocation.longitude);

                        float distance = loc1.distanceTo(loc2) / 1000;

                        // Print the distance in meters
                        Log.d("distance", "Distance: \n" + distance + " KM");
                        TextView textView;
                        textView = findViewById(R.id.distanceViewId);
                        textView.setText("Distance: \n" + distance + " KM");
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
        if (v.getId() == R.id.bookAmbulanceButtonId) {
            startActivity(new Intent(getApplicationContext(), Route_User.class));
        }
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
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    startLocation = new LatLng(latLng.latitude, latLng.longitude);
                }

                // Add Second Marker =============================
                else {
                    // Add second marker
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    endLocation = new LatLng(latLng.latitude, latLng.longitude);
                }

                // Add marker in the map
                mMap.addMarker(markerOptions);

                // Calculate the distance between the two markers
                if (listPoints.size() == 2) {
                    Location loc1 = new Location("");
                    loc1.setLatitude(startLocation.latitude);
                    loc1.setLongitude(startLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(endLocation.latitude);
                    loc2.setLongitude(endLocation.longitude);

                    float distance = loc1.distanceTo(loc2) / 1000;

                    // Print the distance in meters
                    Log.d("distance", "Distance: \n" + distance + " KM");
                    TextView textView;
                    textView = findViewById(R.id.distanceViewId);
                    textView.setText("Distance: \n" + distance + " KM");

                }


                //TODO: request to direction code below
                if (listPoints.size() == 2) {
                    // Create the URL to get request from first marker to second marker
                    String uri = getRequestUri(startLocation, endLocation);
                }
            }
        });
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
}