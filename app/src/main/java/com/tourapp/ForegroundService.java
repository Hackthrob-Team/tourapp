package com.tourapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


public class ForegroundService extends Service {
    private NotificationManager nMN;
    private NotificationCompat.Builder builder;
    private Geocoder geocoder;
    private String cityName;
    private SharedPreferences prefs;
    private String prevCityName;
    private String stateName;
    private RequestQueue queue;
    private MediaPlayer mediaPlayer;
    private final int NOTIFICATION_ID = 2;

    private final IBinder binder = new LocalBinder();

    // class used for the client binder
    public static class LocalBinder extends Binder {
    }

    @Override
    public IBinder onBind(Intent intent) {
        /*
            throw new UnsupportedOperationException("Not yet implemented");
        */
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BROADCAST", "onStartCommand");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_ONE_SHOT);

        prefs = getSharedPreferences("com.tourapp.preferences", MODE_PRIVATE);
        mediaPlayer = MediaPlayer.create(this,R.raw.notification);

        cityName = intent.getStringExtra("city");
        stateName = intent.getStringExtra("state");

        nMN = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "4567";
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    "Tours",
                    NotificationManager.IMPORTANCE_DEFAULT);
            nMN.createNotificationChannel(notificationChannel);
        }

        builder = new NotificationCompat.Builder(this, "4567")
                .setSmallIcon(R.drawable.ic_app_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(cityName + ", " + stateName);

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);


        Context context = getApplicationContext();

        //do heavy work on a background thread

        // Set the Geocoder
        geocoder = new Geocoder(this);

        // Set up Volley
        queue = Volley.newRequestQueue(context);

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
    }

    @SuppressLint("MissingPermission")
    public void setUpLocationRequests() {
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
                            cityName = address.getLocality();
                            stateName = address.getAdminArea();

                            if ((prevCityName != null && cityName != null &&
                                    !cityName.equals(prevCityName))) {
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
}
