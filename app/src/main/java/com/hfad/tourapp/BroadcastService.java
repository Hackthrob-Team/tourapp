package com.hfad.tourapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

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
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class BroadcastService extends Service {
    private NotificationManager nMN;
    private NotificationChannel notificationChannel;
    private NotificationCompat.Builder builder;
    private Notification notification;
    private Context context;
    private Geocoder geocoder;
    private String cityName;
    private String prevCityName;
    private String stateName;
    private RequestQueue queue;
    private TextToSpeech tts;
    private final String WIKIPEDIA_BASE_URL = "https://en.wikipedia.org/w/api.php?action=query&prop=extracts&exintro&explaintext&format=json&redirects&titles=";
    private final String CHANNEL_ID = "4567";
    private final int NOTIFICATION_ID = 2;

    private final IBinder binder = new LocalBinder();
    private ServiceCallbacks serviceCallbacks;

    // class used for the client binder
    public class LocalBinder extends Binder {
        BroadcastService getService() {
            return BroadcastService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BROADCAST", "onStartCommand");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        serviceCallbacks.doSomething();

        nMN = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID,
                    "Tours",
                    NotificationManager.IMPORTANCE_DEFAULT);
            nMN.createNotificationChannel(notificationChannel);
        }

        builder = new NotificationCompat.Builder(this, "4567")
                .setSmallIcon(R.drawable.ic_app_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_body));

        notification = builder.build();
        Log.i("BROADCAST", "Before startForeground");
        startForeground(NOTIFICATION_ID, notification);
        Log.i("FOREGROUND", "In fore");


        context = getApplicationContext();

        //do heavy work on a background thread

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

        //stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (tts != null){
            tts.stop();
            tts.shutdown();
        }
    }

    public void setCallbacks(ServiceCallbacks callbacks){
        serviceCallbacks = callbacks;
    }

    @SuppressLint("MissingPermission")
    public void setUpLocationRequests() {
        Log.d("Hi2", "here2");
        // Location callback for tracking user's location
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d("Hi3","here3");
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
                            cityName = address.getLocality();
                            stateName = address.getAdminArea();
                            Log.d("Hi4","here4");

                            if ((prevCityName == null) || (prevCityName != null &&
                                    !cityName.equals(prevCityName))) {
                                String countryCode = address.getCountryCode();
                                Log.d("Hi5","here5");
                                if (countryCode.equals("US"))
                                    queue.add(makeRequest(cityName, stateName, "%s%s, %s"));
                                else
                                    queue.add(makeRequest(cityName, address.getCountryName(), "%s%s, %s"));

                                // Change notification
                                builder.setContentText(cityName + ", " + stateName);
                                nMN.notify(NOTIFICATION_ID, builder.build());
                            }
                            prevCityName = cityName;
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

                        Log.d("Hi7",text);

                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);

                        Log.i("ExtractText", text);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, error -> Log.e("ExtractText", "Error"));
    }
}