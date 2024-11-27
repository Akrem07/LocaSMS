package com.example.findfriends;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.findfriends.MapsActivity;
import com.example.findfriends.MyLocationService;

public class MySMSReceiver extends BroadcastReceiver {

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String messageBody, phoneNumber;

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                if (messages.length > 0) {
                    messageBody = messages[0].getMessageBody();
                    phoneNumber = messages[0].getDisplayOriginatingAddress();

                    Toast.makeText(context,
                            "Message : " + messageBody + " Reçu de la part de: " + phoneNumber,
                            Toast.LENGTH_LONG).show();

                    if (messageBody.contains("FindFriends: envoyer moi votre position")) {
                        // Start service to get location and send it back
                        Intent serviceIntent = new Intent(context, MyLocationService.class);
                        serviceIntent.putExtra("phone", phoneNumber);

                        // Use a static name (replace "YourName" with the name you prefer)
//                        String name = "YourName"; // Static name used here
//                        serviceIntent.putExtra("name", name);  // Pass the name as a static value
                        context.startService(serviceIntent);
                    }

                    if (messageBody.contains("FindFriends: Ma position est ")) {
                        String[] t = messageBody.split("#");
                        if (t.length == 4) {
                            String longitude = t[1];
                            String latitude = t[2];
                            String name = t[3]; // Extract name (can still be passed from the message)

                            DatabaseHelper dbHelper = new DatabaseHelper(context);
                            dbHelper.savePosition(phoneNumber, Double.parseDouble(latitude), Double.parseDouble(longitude), String.valueOf(System.currentTimeMillis()), name);

                            // Add a notification to alert the user
                            NotificationCompat.Builder myNotif = new NotificationCompat.Builder(context, "channel")
                                    .setContentTitle("Position reçue")
                                    .setContentText("Appuyez pour voir sur la carte")
                                    .setSmallIcon(android.R.drawable.ic_dialog_map)
                                    .setAutoCancel(true);

                            Intent i2 = new Intent(context, MapsActivity.class);
                            i2.putExtra("longitude", longitude);
                            i2.putExtra("latitude", latitude);
                            PendingIntent pi = PendingIntent.getActivity(context, 0, i2, PendingIntent.FLAG_MUTABLE);
                            myNotif.setContentIntent(pi);

                            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
                            NotificationChannel canal = new NotificationChannel("channel", "Canal pour notre app", NotificationManager.IMPORTANCE_DEFAULT);
                            managerCompat.createNotificationChannel(canal);
                            managerCompat.notify(1, myNotif.build());
                        }
                    }
                }
            }
        }
    }
}
