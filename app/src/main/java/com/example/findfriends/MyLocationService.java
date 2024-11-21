package com.example.findfriends;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.telephony.SmsManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MyLocationService extends Service {

    private DatabaseHelper databaseHelper; // Add a DatabaseHelper instance

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new DatabaseHelper(this); // Initialize DatabaseHelper
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String phoneNumber = intent.getStringExtra("phone");
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        client.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();

                // Save position and phone number in the database
                databaseHelper.savePosition(phoneNumber, latitude, longitude, String.valueOf(System.currentTimeMillis()));

                // Send SMS with the location
                SmsManager manager = SmsManager.getDefault();
                manager.sendTextMessage(phoneNumber,
                        null,
                        "FindFriends: Ma position est #" + longitude + "#" + latitude,
                        null,
                        null);
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
