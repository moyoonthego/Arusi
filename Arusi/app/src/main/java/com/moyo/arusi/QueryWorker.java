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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.support.v4.content.ContextCompat.getSystemService;

public class QueryWorker extends Worker {

    public static Context mcontext;
    private static FirebaseAuth mAuth;
    private static DatabaseReference currentUser;

    public QueryWorker(
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
        currentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("query");

        // Do the work here
        SearchPair.setupSearching();
        SearchPair.startSearching();

        currentUser.child("searching").setValue("false");
        currentUser.child("searching").setValue("true");

        showNotification();
        Log.d("WORKER", "Match making completed.");
        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }

    void showNotification() {
        mcontext = getApplicationContext();
        NotificationManager mNotificationManager =
                (NotificationManager) mcontext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ARUSI_CHANNEL",
                    "ARUSI_MATCHING",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "ARUSI_CHANNEL")
                .setSmallIcon(R.drawable.logo) // notification icon
                .setContentTitle("Arusi Connection Search Completed") // title for notification
                .setContentText("Salaam, We've found a potential connection! Come check it out!")// message for notification
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