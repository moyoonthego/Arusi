package com.moyo.arusi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicMarkableReference;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    public  Animation floating;
    private FirebaseOptions options;

    private static String arusitag = "checkformatchesforarusi";
    private String currentUId;

    private DatabaseReference usersDb;
    private DatabaseReference currentUserDb;
    private FirebaseUser firebaseUser;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.7F);
    private static ImageView currentpic;
    private static ImageView matchpic;
    private static Button search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Animation floating = AnimationUtils.loadAnimation(this, R.anim.floating);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestId()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginOrRegister.class);
            startActivity(intent);
            finish();
        } else {
            setupDesktop();
        }

    }

    private void setupDesktop() {
        Typeface myFont = Typeface.createFromAsset(getAssets(), "fonts/productsansbold.ttf");
        final TextView edittext = (TextView) findViewById(R.id.welcomer);
        edittext.setTypeface(myFont);
        matchpic = (ImageView) findViewById(R.id.profile_image);

        // constant check for matches
        // finally, enable constant check for matches (work)
        Constraints newconstraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        MatchWorker.setContext(this);
        PeriodicWorkRequest matchcheck = new PeriodicWorkRequest.Builder(MatchWorker.class, 60, TimeUnit.MINUTES)
                .setConstraints(newconstraints)
                .build();
        WorkManager.getInstance().enqueueUniquePeriodicWork(arusitag, ExistingPeriodicWorkPolicy.KEEP, matchcheck);
        Log.d("TAG", "match checking enqueued");

        // Continued...
        floating = AnimationUtils.loadAnimation(this, R.anim.floating);
        currentpic = findViewById(R.id.currentpic);
        currentpic.startAnimation(floating);
        search = findViewById(R.id.buttonSearch);

        // Setting up saved data
        currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
        currentUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                edittext.setText("Salaam, "+dataSnapshot.child("name").getValue()+"...");
                if (dataSnapshot.hasChild("found") && dataSnapshot.child("found").getValue().equals("true")) {
                    currentpic.setImageResource(R.drawable.startsearching);
                    matchpic.setVisibility(View.VISIBLE);
                    matchpic.startAnimation(floating);
                    search.setVisibility(View.GONE);
                    currentpic.setVisibility(View.INVISIBLE);
                    currentpic.setVisibility(View.GONE);
                    String matchname = String.valueOf(dataSnapshot.child("match").getValue());
                    if (!matchname.equals("") && !usersDb.child(matchname).child("photo").equals("")) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        try {
                            StorageReference curitem = storage.getReference().child("Users").child(String.valueOf(dataSnapshot.child("match").getValue()));
                            GlideApp.with(MainActivity.this).load(curitem).into(matchpic);
                        } catch (Throwable e) {
                            // they didnt have a picture
                            Log.e("TAG", "User didn't have a photo!");
                        }
                    }
                } else if (dataSnapshot.hasChild("searching") && dataSnapshot.child("searching").getValue().equals("true")) {
                    if (dataSnapshot.hasChild("query")) {
                        matchpic.setVisibility(View.GONE);
                        search.setVisibility(View.VISIBLE);
                        currentpic.setImageResource(R.drawable.searchinprogress);
                        currentpic.startAnimation(floating);
                        search.setText("Search in Progress");
                        search.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        search.setBackgroundResource(R.drawable.background_button);
                        Toast.makeText(MainActivity.this, "Search in progress. This could take hours. We will notify you when ready.", Toast.LENGTH_LONG).show();
                    } else {
                        search.setVisibility(View.VISIBLE);
                        matchpic.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Please start some Query/Search Parameters before searching for a connection...", Toast.LENGTH_LONG).show();
                        currentpic.setImageResource(R.drawable.startsearching);
                        currentUserDb.child("searching").setValue("false");
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("TAG", "Failed to read app title value.", error.toException());
            }
        });
    }

    public void startSearch(View view) {
        view.startAnimation(buttonClick);

        if (search.getText().toString().contains("Start Searching")) {
            HashMap newdata = new HashMap<String, String>();
            newdata.put("searching", "true");
            newdata.put("found", "false");
            currentUserDb.updateChildren(newdata);
            Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            OneTimeWorkRequest newreq = new OneTimeWorkRequest.Builder(QueryWorker.class)
                    .setConstraints(constraints)
                    .setInitialDelay(2, TimeUnit.HOURS)
                    .build();
            WorkManager.getInstance().enqueue(newreq);
            QueryWorker.setContext(this);
            matchpic.setVisibility(view.GONE);
            currentpic.setVisibility(view.VISIBLE);

            Log.d("TAG", "WorkManager called and tasks commenced");
        }
    }

    public void stopstartsearchAnim() {
        currentpic.setVisibility(View.INVISIBLE);
        currentpic.setVisibility(View.GONE);
    }

    public void logoutUser(View view) { ;
        view.startAnimation(buttonClick);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.popup, null);

        final AlertDialog alertD = new AlertDialog.Builder(this).create();
        alertD.setView(promptView);
        alertD.show();

        Button exit = (Button) promptView.findViewById(R.id.buttonExit);

        Button delete = (Button) promptView.findViewById(R.id.buttonDelete);

        exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, LoginOrRegister.class);
                startActivity(intent);
                // forget last google account signed in with
                googleSignInClient.signOut()
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                            }
                        });
                currentUserDb.child("blacklist").setValue(null);
                mAuth.signOut();
                currentUserDb.child("searching").setValue(false);
                alertD.dismiss();
                finish();

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                usersDb.child(mAuth.getUid()).removeValue();
                mAuth.getCurrentUser().delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("TAG", "User account deleted.");
                                }
                            }
                        });
                // forget last google account signed in with
                googleSignInClient.signOut()
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                            }
                        });
                Toast.makeText(MainActivity.this, "Account successfully deleted.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginOrRegister.class);
                startActivity(intent);
                alertD.dismiss();
                finish();

            }
        });
    }

    public void goToSettings(View view) {
        // Change MainActivity to "Settings"
        view.startAnimation(buttonClick);
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void goToProfile(View view) {
        // Change MainActivity to "Settings"
        view.startAnimation(buttonClick);
        Intent intent = new Intent(MainActivity.this, InformationActivity.class);
        startActivity(intent);
    }

    public void goToQuery(View view) {
        // Change MainActivity to "Settings"
        view.startAnimation(buttonClick);
        Intent intent = new Intent(MainActivity.this, QueryActivity.class);
        startActivity(intent);
    }

    public void goToNewMatch(View view) {
        // Change MainActivity to "Settings"
        Intent intent = new Intent(MainActivity.this, YourMatchActivity.class);
        startActivity(intent);
    }

    public void goToMatchbox(View view) {
        // Change MainActivity to "Settings"
        view.startAnimation(buttonClick);
        Intent intent = new Intent(MainActivity.this, MatchboxActivity.class);
        startActivity(intent);
    }
}
