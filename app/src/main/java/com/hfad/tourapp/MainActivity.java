package com.hfad.tourapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private TextView txtCurrentCity;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEditor;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private final int LOCATION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getPreferences(Context.MODE_PRIVATE);
        setDefaultPrefs();

        // Get the MapView and initialize the instance variable
        mapView = (MapView) findViewById(R.id.mapView);
        // Call the callback with the mapView
        mapView.onCreate(savedInstanceState);

        // Look for a callback when the map is ready inside this class
        mapView.getMapAsync(this);

        txtCurrentCity = (TextView) findViewById(R.id.txtCurrentCity);
        // Set initial text for the current city indicator
        txtCurrentCity.setText(R.string.current_city_loading);

        // Location callback for tracking user's location
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    txtCurrentCity.setText("Location: " + String.valueOf(location.getLatitude())
                            + " " + String.valueOf(location.getLongitude()));
                }
            }
        };
    }

    //Initialize ActionBar for MainActivity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void onSettingsAction(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.settings_button:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
        }
    }

    public void setDefaultPrefs() {
        prefEditor = prefs.edit();
        prefEditor.putBoolean("text-to-speech", true);
        prefEditor.putBoolean("notify", true);
        prefEditor.putBoolean("dark-mode", false);
        prefEditor.apply();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(
                this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(this,
                   new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                           Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                LocationRequest locationRequest = LocationRequest.create()
                        .setInterval(1000)
                        .setFastestInterval(500)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        Looper.getMainLooper());
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}