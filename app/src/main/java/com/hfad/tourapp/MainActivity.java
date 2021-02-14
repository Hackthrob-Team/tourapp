package com.hfad.tourapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private LocationCallback locationCallback;
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap gMap;
    private TextView txtCurrentCity;
    private SharedPreferences prefs;
    private MediaPlayer mediaPlayer;
    private final int LOCATION_REQUEST_CODE = 123;
    private Geocoder geocoder;
    private final Context context = this;
    private String cityName;
    private String prevCityName;
    private String stateName;
    private RequestQueue queue;
    private static boolean instanceRunning = false;
    public static TextToSpeech tts;
    public static final String WIKIPEDIA_BASE_URL = "https://en.wikipedia.org/w/api.php?action=query&prop=extracts&exintro&explaintext&format=json&redirects&titles=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver() {
//            @OnLifecycleEvent(Lifecycle.Event.ON_START)
//            void startForegroundTasks() {
//                Log.i("FOREGROUND", "In fore");
//            }
//
//            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//            void startBackgroundTasks() {
//                Log.i("FOREGROUND", "Out fore");
//                /*
//                if (tts != null){
//                    tts.stop();
//                }
//                 */
//
//                Intent serviceIntent = new Intent(MainActivity.this, ForegroundService.class);
//                context.startService(serviceIntent);
//            }
//        });

        prefs = getSharedPreferences("com.hfad.tourapp.preferences", Context.MODE_PRIVATE);
        mediaPlayer = MediaPlayer.create(this, R.raw.notification);
        setDefaultPrefs();

        // Get the MapView and initialize the instance variable
        mapView = findViewById(R.id.mapView);
        // Call the callback with the mapView
        mapView.onCreate(savedInstanceState);

        // Look for a callback when the map is ready inside this class
        mapView.getMapAsync(this);

        txtCurrentCity = findViewById(R.id.txtCurrentCity);
        // Set initial text for the current city indicator
        txtCurrentCity.setText(R.string.current_city_loading);

        // Set the Geocoder
        geocoder = new Geocoder(this);

        // Set up Volley
        queue = Volley.newRequestQueue(context);

        // Set up TextToSpeech
        tts = new TextToSpeech(this, status -> {});
        tts.setLanguage(Locale.US);
        tts.setPitch(1.25f);

        // Check to see if permission is granted from previous runs
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            if (!instanceRunning) {
                setUpLocationRequests();
                instanceRunning = true;
            }
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
        super.onResume();
        mapView.onResume();
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();

        if (!isFinishing()) {
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            serviceIntent.putExtra("city", cityName);
            serviceIntent.putExtra("state", stateName);
            context.startService(serviceIntent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();

        if (isFinishing()) {
            if (tts != null) {
                tts.stop();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

        if (tts != null){
            tts.shutdown();
        }

        fusedLocationClient.removeLocationUpdates(locationCallback);
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
        instanceRunning = false;
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
        if (item.getItemId() == R.id.settings_button) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
    }

    //Sets the default preferences for the application
    public void setDefaultPrefs() {
//        SharedPreferences.Editor prefEditor = prefs.edit();
//        prefEditor.putBoolean("welcome", true);
//        prefEditor.putBoolean("notify", true);
//        prefEditor.putBoolean("speech-limit", false);
//        prefEditor.putInt("word-count", 100);
//        prefEditor.apply();
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
                instanceRunning = true;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("MissingPermission")
    public void setUpLocationRequests() {
        // Location callback for tracking user's location
        locationCallback = new LocationCallback() {
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
                            txtCurrentCity.setText(getResources()
                                    .getString(R.string.current_city_textview,
                                            address.getLocality(), address.getAdminArea()));
                            cityName = address.getLocality();
                            stateName = address.getAdminArea();

                            if ((prevCityName == null && cityName != null) || (prevCityName != null &&
                                    !cityName.equals(prevCityName))) {
                                if (MainActivity.tts.isSpeaking())
                                    MainActivity.tts.stop();

                                if (prefs.getBoolean("notify", SettingsActivity.DEFAULT_NOTIFY)) {
                                    mediaPlayer.start();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        MainActivity.tts.playSilentUtterance(2000, TextToSpeech.QUEUE_FLUSH, null);
                                    } else {
                                        MainActivity.tts.playSilence(2000, TextToSpeech.QUEUE_FLUSH, null);
                                    }
                                }

                                String countryCode = address.getCountryCode();

                                if (prefs.getBoolean("welcome", SettingsActivity.DEFAULT_WELCOME))
                                    tts.speak("Welcome to " + cityName + ", " + stateName,
                                            TextToSpeech.QUEUE_ADD, null);
                                else {
                                    if (countryCode.equals("US"))
                                        queue.add(makeRequest(cityName, stateName));
                                    else
                                        queue.add(makeRequest(cityName, address.getCountryName()));
                                }

                                if (gMap != null) {
                                    lat = address.getLatitude();
                                    lng = address.getLongitude();
                                    gMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(cityName + ", " + stateName)
                                    );
                                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(lat, lng), 14
                                    ));
                                }
                            }
                            prevCityName = cityName;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NullPointerException ignored) {

                    }
                }
            }
        };
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(1000)
                .setFastestInterval(500)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(
                this);
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private JsonObjectRequest makeRequest(String city, String state) {
        // Code to make a city request
        return new JsonObjectRequest(Request.Method.GET, String.format("%s%s, %s", WIKIPEDIA_BASE_URL, city, state), null,
                response -> {
                    try {
                        JSONObject pages = response.getJSONObject("query")
                                .getJSONObject("pages");
                        String pageId = Objects.requireNonNull(pages.names()).getString(0);
                        String text = pages.getJSONObject(pageId).getString("extract");

                        if (prefs.getBoolean("speech-limit", SettingsActivity.DEFAULT_SPEECH_LIMIT)) {
                            int wordCount = prefs.getInt("word-count", SettingsActivity.DEFAULT_WORD_COUNT);

                            String[] words = text.split(" ");
                            text = "";

                            for (int i = 0; i < words.length && i < wordCount; ++i) {
                                text += words[i] + " ";
                            }
                        }

                        tts.speak(text, TextToSpeech.QUEUE_ADD, null);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, error -> Log.e("ExtractText", "Error"));
    }
}