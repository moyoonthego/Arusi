package com.moyo.arusi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import androidx.work.WorkManager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.support.v4.content.ContextCompat.getSystemService;

public class MatchWorker extends Worker {

    public static Context mcontext;
    private static FirebaseAuth mAuth;
    private static DatabaseReference currentUser;
    private boolean mutualinterest = false;
    private boolean interestrecieved = false;

    public MatchWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static void setContext(Context c) {
        mcontext = c;
    }

    @Override
    public Result doWork() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid());

        // First task: notify user to check Matchbox for a new match (someone tht THEY liked)
        // get whether or not someone liked us recently that we liked
        currentUser.child("notifymatch").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Log.d("TAG", "Case 1 checked");
                        mutualinterest = (Boolean) dataSnapshot.getValue();

                        if (mutualinterest) {
                            Log.d("TAG", "Case 1 passed: Mutual interest found");
                            showNotificationMutual();
                            mutualinterest = false;
                            currentUser.child("notifymatch").setValue(false);
                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Second task: notify the user to check out a user that recently liked THEM
        // get whether or not someone liked us recently
        currentUser.child("viewnewmatch").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Log.d("TAG", "Case 2 checked");
                    interestrecieved = (Boolean) dataSnapshot.getValue();

                    if (interestrecieved) {
                        Log.d("TAG", "Case 2 passed: Interest in them found");
                        showNotificationInterest();
                        currentUser.child("found").setValue("true");
                        interestrecieved = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return Result.success();
    }

    void showNotificationInterest() {
        mcontext = getApplicationContext();
        NotificationManager mNotificationManager =
                (NotificationManager) mcontext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ARUSI_CHANNEL",
                    "ARUSI_MATCH_CONNECTION",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "ARUSI_CHANNEL")
                .setSmallIcon(R.drawable.logo) // notification icon
                .setContentTitle("Someone liked your profile! View theirs now!") // title for notification
                .setContentText("Salaam, a user has shown interest in you. Check their profile now and we will share contact info if there is mutual interest.")// message for notification
                .setAutoCancel(true) // clear notification after click
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS | Notification.DEFAULT_LIGHTS)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setLights(0xff00ff00, 300,  100);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(mcontext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

    void showNotificationMutual() {
        mcontext = getApplicationContext();
        NotificationManager mNotificationManager =
                (NotificationManager) mcontext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ARUSI_CHANNEL",
                    "ARUSI_MATCH_CONNECTION",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "ARUSI_CHANNEL")
                .setSmallIcon(R.drawable.logo) // notification icon
                .setContentTitle("Mutual Interest Found! Check Your Matchbox!") // title for notification
                .setContentText("Salaam, a user you recently liked has shown interest in you. Check your matchbox for their contact info")// message for notification
                .setAutoCancel(true) // clear notification after click
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS | Notification.DEFAULT_LIGHTS)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setLights(0xff00ff00, 300,  100);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(mcontext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
