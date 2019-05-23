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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    public  Animation floating;
    private FirebaseOptions options;

    private String currentUId;

    private DatabaseReference usersDb;
    private DatabaseReference currentUserDb;
    private FirebaseUser firebaseUser;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.7F);
    private static ImageView currentpic;
    private static Button search;


    ListView listView;
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
        // Setting up saved data
        currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
        currentUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                edittext.setText("Salaam, "+dataSnapshot.child("name").getValue()+"...");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("TAG", "Failed to read app title value.", error.toException());
            }
        });

        // Continued...
        floating = AnimationUtils.loadAnimation(this, R.anim.floating);
        currentpic = findViewById(R.id.mypic);
        currentpic.startAnimation(floating);
        search = findViewById(R.id.buttonSearch);
        SharedPreferences shared = getSharedPreferences("com.moyo.arusi", MODE_PRIVATE);

        if (shared.getString("buttontitle", "").equals("Search in Progress")) {
            currentpic.setImageResource(R.drawable.searchinprogress);
            currentpic.startAnimation(floating);
            search.setText("Search in Progress");
            search.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            search.setBackgroundResource(R.drawable.background_button);
        }
    }

    public void startSearch(View view) {
        view.startAnimation(buttonClick);
        if (search.getText().equals("Start Searching   ")) {

                currentpic.setImageResource(R.drawable.searchinprogress);
                currentpic.startAnimation(floating);
                search.setText("Search in Progress");
                search.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                search.setBackgroundResource(R.drawable.background_button);
                SharedPreferences.Editor editor = getSharedPreferences("com.moyo.arusi", MODE_PRIVATE).edit();
                editor.putString("buttontitle", String.valueOf(search.getText()));
                editor.commit();
        }
    }

    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectionsDb = usersDb.child(userSex).child(currentUId).child("connections").child("yeps").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Toast.makeText(MainActivity.this, "new Connection", Toast.LENGTH_LONG).show();
                    usersDb.child(oppositeUserSex).child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).setValue(true);
                    usersDb.child(userSex).child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private String userSex;
    private String oppositeUserSex;

    public void checkUserSex(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference maleDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Male");
        maleDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals(user.getUid())){
                    userSex = "Male";
                    oppositeUserSex = "Female";
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        DatabaseReference femaleDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Female");
        femaleDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals(user.getUid())){
                    userSex = "Female";
                    oppositeUserSex = "Male";
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void logoutUser(View view) { ;
        view.startAnimation(buttonClick);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.popup, null);

        final AlertDialog alertD = new AlertDialog.Builder(this).create();

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
                mAuth.signOut();

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
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
                usersDb.child(mAuth.getUid()).removeValue();
                FirebaseAuth.getInstance().getCurrentUser().delete();
                Toast.makeText(MainActivity.this, "Account successfully deleted.", Toast.LENGTH_SHORT).show();

            }
        });

        alertD.setView(promptView);

        alertD.show();

    }

    public void goToSettings(View view) {
        // Change MainActivity to "Settings"
        view.startAnimation(buttonClick);
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.putExtra("userSex", userSex);
        startActivity(intent);
    }

    public void goToProfile(View view) {
        // Change MainActivity to "Settings"
        view.startAnimation(buttonClick);
        Intent intent = new Intent(MainActivity.this, InformationActivity.class);
        intent.putExtra("userSex", userSex);
        startActivity(intent);
    }

    public void goToQuery(View view) {
        // Change MainActivity to "Settings"
        view.startAnimation(buttonClick);
        Intent intent = new Intent(MainActivity.this, QueryActivity.class);
        startActivity(intent);
    }

    public void goToMatchbox(View view) {
        // Change MainActivity to "Settings"
        view.startAnimation(buttonClick);
        Intent intent = new Intent(MainActivity.this, MatchboxActivity.class);
        startActivity(intent);
    }
}
