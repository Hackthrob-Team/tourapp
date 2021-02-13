package com.hfad.tourapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class BroadcastService extends Service {
    private NotificationManager nMN;
    private NotificationChannel notificationChannel;
    private Notification notification;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BROADCAST", "onStartCommand");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        nMN = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("4567",
                    "Tours",
                    NotificationManager.IMPORTANCE_DEFAULT);
            nMN.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "4567")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        notification = builder.build();
        Log.i("BROADCAST", "Before startForeground");
        startForeground(1, notification);
        Log.i("FOREGROUND", "In fore");
        //do heavy work on a background thread
        stopSelf();
        return START_NOT_STICKY;
    }


}