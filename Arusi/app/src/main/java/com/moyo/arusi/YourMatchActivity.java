package com.moyo.arusi;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class YourMatchActivity extends AppCompatActivity {

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

    // Database content
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private String currentUId;
    private FirebaseUser firebaseUser;
    private DatabaseReference currentUserDb;
    private Map oldData;
    private Map newData;
    private DatabaseReference matchDb = null;
    private DatabaseReference currentUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_match);
        Animation comein = AnimationUtils.loadAnimation(this, R.anim.fab_jump_from_down);


        // Database setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestId()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        currentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
        currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).child("match");

        // Setting up saved data
        currentUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    matchDb = FirebaseDatabase.getInstance().getReference().child("Users").child(String.valueOf(dataSnapshot.getValue()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("TAG", "Failed to read app title value.", error.toException());
            }
        });

        // Setting up saved data
        matchDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (!(String.valueOf(postSnapshot.getValue()).equals(""))) {
                        if (postSnapshot.getKey().equals("marital")) {
                            ((Button) findViewById(R.id.marital_status)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("education")) {
                            ((Button) findViewById(R.id.education)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("lastname")) {
                            ((EditText) findViewById(R.id.lName)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("age")) {
                            ((EditText) findViewById(R.id.age)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("profession")) {
                            ((EditText) findViewById(R.id.profession)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("nationality")) {
                            ((EditText) findViewById(R.id.nationality)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("location")) {
                            ((Button) findViewById(R.id.location)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("weight")) {
                            ((EditText) findViewById(R.id.weight)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("height")) {
                            ((EditText) findViewById(R.id.height)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("mothertongue")) {
                            ((EditText) findViewById(R.id.mothertongue)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("complexion")) {
                            ((EditText) findViewById(R.id.complexion)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("familyinfo")) {
                            ((EditText) findViewById(R.id.familyinfo)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("cast")) {
                            ((Button) findViewById(R.id.muslim_cast)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("name")) {
                            ((EditText) findViewById(R.id.fName)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("photo")) {
                                // UNFINISHED!!!!!!!!!!!!!
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("TAG", "Failed to read app title value.", error.toException());
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.yes);
        fab.startAnimation(comein);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(YourMatchActivity.this, R.anim.fab_jump_to_down);
                view.startAnimation(hyperspaceJumpAnimation);
                new AlertDialog.Builder(YourMatchActivity.this)
                        .setTitle("Interest recieved! Waiting on feedback!")
                        .setMessage("Arusi will add the proposal (with personal info) to your matchbox when mutual interest has been achieved.")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setIcon(android.R.drawable.star_big_on)
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent = new Intent(YourMatchActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            }
                        })
                        .show();
            }
        });


        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.no);
        fab1.startAnimation(comein);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(YourMatchActivity.this, R.anim.fab_jump_to_down);
                view.startAnimation(hyperspaceJumpAnimation);
                new AlertDialog.Builder(YourMatchActivity.this)
                        .setTitle("Match removed")
                        .setMessage("Arusi has removed this proposal from your inbox. Inshallah, we shall search again.")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setIcon(android.R.drawable.ic_delete)
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // removing match
                                newData.put("match", "");
                                currentUser.updateChildren(newData);
                                Intent intent = new Intent(YourMatchActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            }
                        })
                        .show();
            }
        });


        // Setting up image view
        // Setting up header
        Typeface myFont = Typeface.createFromAsset(getAssets(), "fonts/productsansbold.ttf");
        final TextView edittext = (TextView) findViewById(R.id.whoareyou);
        edittext.setTypeface(myFont);
        final ImageView pic = (ImageView) findViewById(R.id.profile_image);
        final ImageView picview = (ImageView) findViewById(R.id.profileimageview);
        picview.setImageDrawable(pic.getDrawable());
        pic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                picview.setImageDrawable(pic.getDrawable());
                pic.setVisibility(View.GONE);
                ((TextView) findViewById(R.id.whoareyou)).setVisibility(View.GONE);
                picview.setVisibility(View.VISIBLE);
                picview.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        });


        picview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                picview.setAdjustViewBounds(true);
                picview.setVisibility(View.GONE);
                ((TextView) findViewById(R.id.whoareyou)).setVisibility(View.VISIBLE);
                pic.setVisibility(View.VISIBLE);
            }
        });

    }

}