package com.hfad.tourapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap gMap;
    private TextView txtCurrentCity;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEditor;
    private final int LOCATION_REQUEST_CODE = 123;
    private Geocoder geocoder;
    private Context context = this;
    private String cityName;
    private String prevCityName;
    private String stateName;
    private RequestQueue queue;
    private final Intent serviceIntent = new Intent(this, BroadcastService.class);
    public TextToSpeech tts;
    public static final String WIKIPEDIA_BASE_URL = "https://en.wikipedia.org/w/api.php?action=query&prop=extracts&exintro&explaintext&format=json&redirects&titles=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("com.hfad.tourapp.preferences", Context.MODE_PRIVATE);
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

        // Set the Geocoder
        geocoder = new Geocoder(this);

        // Set up Volley
        queue = Volley.newRequestQueue(context);

        // Set up TextToSpeech
        tts = new TextToSpeech(this, status -> {});
        tts.setLanguage(Locale.US);

        // Check to see if permission is granted from previous runs
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            setUpLocationRequests();
        }
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
        Log.i("RESUMING", "Resuming rn");
        super.onResume();
        mapView.onResume();
        stopService(serviceIntent);
    }

    @Override
    protected void onPause() {
        Log.i("PAUSE", "Pausing rn");
        super.onPause();
        mapView.onPause();
        startService(serviceIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();

        if (tts != null){
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

        if (tts != null){
            tts.stop();
            tts.shutdown();
        }
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

    //Sets the default preferences for the application
    public void setDefaultPrefs() {
        prefEditor = prefs.edit();
        prefEditor.putBoolean("minimal", false);
        prefEditor.putBoolean("text-to-speech", true);
        prefEditor.putBoolean("notify", true);
        prefEditor.putBoolean("dark-mode", false);
        prefEditor.apply();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("On map ready", "RUNNING");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(this,
                   new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                           Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        gMap = googleMap;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                setUpLocationRequests();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("MissingPermission")
    private void setUpLocationRequests() {
        // Location callback for tracking user's location
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    try {
                        List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                        if (addresses.size() > 0) {
                            Address address = addresses.get(0);
                            txtCurrentCity.setText("Current city: " + address.getLocality() + ", "
                                    + address.getAdminArea());
                            cityName = address.getLocality();
                            stateName = address.getAdminArea();

                            if ((prevCityName == null) || (prevCityName != null &&
                                    !cityName.equals(prevCityName))) {
                                String countryCode = address.getCountryCode();
                                if (countryCode.equals("US"))
                                    queue.add(makeRequest(cityName, stateName, "%s%s, %s"));
                                else
                                    queue.add(makeRequest(cityName, address.getCountryName(), "%s%s, %s"));
                            }
                            prevCityName = cityName;
                            if (gMap != null)
                                gMap.moveCamera(CameraUpdateFactory.
                                        newLatLngZoom(new LatLng(lat, lng), gMap.getCameraPosition().zoom));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(1000)
                .setFastestInterval(500)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(
                this);
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private JsonObjectRequest makeRequest(String city, String state, String formatString) {
        // Code to make a city request
        return new JsonObjectRequest(Request.Method.GET, String.format(formatString, WIKIPEDIA_BASE_URL, city, state), null,
                response -> {
                    try {
                        JSONObject pages = response.getJSONObject("query")
                                .getJSONObject("pages");
                        String pageId = pages.names().getString(0);
                        String text = pages.getJSONObject(pageId).getString("extract");

                        if (tts.isSpeaking())
                            tts.stop();

                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);

                        Log.i("ExtractText", text);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, error -> Log.e("ExtractText", "Error"));
    }
}