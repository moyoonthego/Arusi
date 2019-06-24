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
import android.renderscript.Sampler;
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

import com.firebase.ui.storage.images.FirebaseImageLoader;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class YourMatchActivity extends AppCompatActivity {

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

    // Database content
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private String currentUId;
    private FirebaseUser firebaseUser;
    private DatabaseReference currentUserMatch;
    private static Map seenData = new HashMap();
    private static Map newdata = new HashMap();
    private static Map ourInfo = new HashMap();
    private static Map matchInfo = new HashMap();
    private DatabaseReference matchDb = null;
    private DatabaseReference currentUser = null;
    private FirebaseStorage storage;
    private String ourMatch;
    private String matchesMatch;

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
        currentUserMatch = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).child("match");

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

        storage = FirebaseStorage.getInstance();
        final ArrayList whitelistarray = new ArrayList();

        // Setting up saved data
        final ValueEventListener matchinfo = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (!(String.valueOf(postSnapshot.getValue()).equals(""))) {
                        if (postSnapshot.getKey().equals("marital")) {
                            ((Button) findViewById(R.id.marital_status)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("education")) {
                            ((Button) findViewById(R.id.education)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("lastname")) {
                            ((TextView) findViewById(R.id.lName)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("age")) {
                            ((TextView) findViewById(R.id.age)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("profession")) {
                            ((TextView) findViewById(R.id.profession)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("nationality")) {
                            ((TextView) findViewById(R.id.nationality)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("location")) {
                            ((Button) findViewById(R.id.location)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("weight")) {
                            ((TextView) findViewById(R.id.weight)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("height")) {
                            ((TextView) findViewById(R.id.height)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("mothertongue")) {
                            ((TextView) findViewById(R.id.mothertongue)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("complexion")) {
                            ((TextView) findViewById(R.id.complexion)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("familyinfo")) {
                            ((TextView) findViewById(R.id.familyinfo)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("cast")) {
                            ((Button) findViewById(R.id.muslim_cast)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("name")) {
                            ((TextView) findViewById(R.id.fName)).setText(String.valueOf(postSnapshot.getValue()));
                            matchInfo.put("name", String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("photo")) {
                            StorageReference curitem = storage.getReference().child("Users").child(String.valueOf(postSnapshot.getValue()));
                            try {
                                GlideApp.with(YourMatchActivity.this).load(curitem).into(pic);
                                GlideApp.with(YourMatchActivity.this).load(curitem).into(picview);
                            } catch (Throwable e) {
                                Log.e("TAG", "GlideApp causing issues for YourMatchActivity");
                            }
                        } else if (postSnapshot.getKey().equals("match")) { // here, we get the match of this persons match (ie, to check if they like each other)
                            matchesMatch = postSnapshot.getValue().toString();
                        } else if (postSnapshot.getKey().equals("whitelist") && postSnapshot.getChildrenCount() != 0) { // here, we get everyone they liked
                            for (DataSnapshot userSnapshot : postSnapshot.getChildren()) {
                                whitelistarray.add(userSnapshot.getKey().toString());
                            }
                        } else if (postSnapshot.getKey().equals("contactemail")) {
                            matchInfo.put("email", String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("phone")) {
                            matchInfo.put("phone", String.valueOf(postSnapshot.getValue()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("TAG", "Failed to read app title value.", error.toException());
            }
        };

        // Setting up saved data (all of this nonsense just to get the match's info)
        currentUserMatch.getParent().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (postSnapshot.getKey().equals("phone")) {
                        ourInfo.put("phone", String.valueOf(postSnapshot.getValue()));
                    } else if (postSnapshot.getKey().equals("contactemail")) {
                        ourInfo.put("email", String.valueOf(postSnapshot.getValue()));
                    } else if (postSnapshot.getKey().equals("name")) {
                        ourInfo.put("name", String.valueOf(postSnapshot.getValue()));
                    } else if (postSnapshot.getKey().equals("match")) {
                        matchDb = FirebaseDatabase.getInstance().getReference().child("Users").child(String.valueOf(postSnapshot.getValue()));
                        ourMatch = String.valueOf(postSnapshot.getValue());
                        matchDb.addValueEventListener(matchinfo);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("TAG", "Failed to read app title value.", error.toException());
            }
        });

        // setting up buttons
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
                                // removing match
                                newdata.put("searching", "false");
                                newdata.put("found", "false");
                                seenData.put(matchDb.getKey().toString(), "");
                                currentUser.updateChildren(newdata);
                                matchDb.removeEventListener(matchinfo);
                                currentUser.child("viewnewmatch").setValue(false);
                                currentUser.child("whitelist").updateChildren(seenData);
                                final Intent intent = new Intent(YourMatchActivity.this, MainActivity.class);

                                // If they both like each other do this next stuff
                                if (whitelistarray.contains(firebaseUser.getUid())) {
                                    // Mutual Match Yay!
                                    // add them to our matchbox
                                    currentUser.child("matches").child(ourMatch).updateChildren(matchInfo);

                                    // add us to theirs
                                    matchDb.child("matches").child(firebaseUser.getUid()).updateChildren(ourInfo);

                                    // send notification to other user!
                                    matchDb.child("notifymatch").setValue(true);

                                    final DialogInterface.OnClickListener exit = new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            currentUser.updateChildren(newdata);
                                            // exit to main
                                            startActivity(intent);
                                            finish();
                                        }
                                    };

                                    final AlertDialog alertD = new AlertDialog.Builder(YourMatchActivity.this)
                                            .setTitle("Mutual Interest Found!")
                                            .setMessage("Congratulations! Check your Matchbox for this users contact info! We wish you luck in your next steps...")
                                            .setPositiveButton(android.R.string.ok, exit)
                                            .show();

                                } else {
                                    // Oh man they didn't like you, or didn't like you yet
                                    if (matchDb.getParent() != null) {
                                        matchDb.child("match").setValue(firebaseUser.getUid());
                                        matchDb.child("viewnewmatch").setValue(true);
                                        startActivity(intent);
                                        finish();
                                    }
                                }

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
                                newdata.put("searching", "false");
                                newdata.put("found", "false");
                                newdata.put("match", "");
                                seenData.put(matchDb.getKey().toString(), "");
                                currentUser.updateChildren(newdata);
                                currentUser.child("blacklist").updateChildren(seenData);
                                currentUser.child("viewnewmatch").setValue(false);
                                matchDb.removeEventListener(matchinfo);
                                Intent intent = new Intent(YourMatchActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            }
                        })
                        .show();
            }
        });

    }

}
