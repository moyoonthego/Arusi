package com.moyo.arusi;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import pub.devrel.easypermissions.EasyPermissions;

public class MatchboxActivity extends AppCompatActivity {

    // Database content
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private String currentUId;
    private FirebaseUser firebaseUser;
    private DatabaseReference currentMatchesDb;
    private ValueEventListener checking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matchbox);

        // Setting up header
        Typeface myFont = Typeface.createFromAsset(getAssets(), "fonts/productsansbold.ttf");
        final TextView edittext = (TextView) findViewById(R.id.whoareyou);
        edittext.setTypeface(myFont);

        final LinearLayout myRoot = (LinearLayout) findViewById(R.id.rootview);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        currentMatchesDb = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).child("matches");

        checking = new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // print all contacts in order here in this loop
                    LinearLayout a = new LinearLayout(MatchboxActivity.this);
                    a.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout b = new LinearLayout(MatchboxActivity.this);
                    b.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams paramsN = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    paramsN.setMargins(40,30,0,6);
                    LinearLayout.LayoutParams paramsP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    paramsP.setMargins(29, 5, 50, 20);

                    TextView name = (new TextView(MatchboxActivity.this));
                    name.setText(String.valueOf(postSnapshot.child("name").getValue()));
                    name.setTextSize(22);
                    name.setTypeface(null, Typeface.BOLD);
                    name.setLayoutParams(paramsN);
                    name.setTextColor(Color.WHITE);
                    TextView phonenumber = (new TextView(MatchboxActivity.this));
                    phonenumber.setText(String.valueOf(postSnapshot.child("phone").getValue()));
                    phonenumber.setTextSize(16);
                    phonenumber.setTypeface(null, Typeface.BOLD_ITALIC);
                    phonenumber.setLayoutParams(paramsP);
                    phonenumber.setTextColor(Color.WHITE);
                    TextView email = (new TextView(MatchboxActivity.this));
                    email.setText(String.valueOf(postSnapshot.child("email").getValue()));
                    email.setTextSize(13);
                    email.setTextColor(Color.WHITE);
                    View line = new View(myRoot.getContext());
                    line.setLayoutParams(new LinearLayout.LayoutParams(3000,
                            5));
                    line.setBackgroundColor(R.color.colorPrimaryDark);

                    a.addView(name);
                    b.addView(phonenumber);
                    b.addView(email);
                    a.addView(b);
                    a.addView(line);
                    myRoot.addView(a);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("TAG", "Failed to read app title value.", error.toException());
            }
        };

        // Setting up saved data
        currentMatchesDb.addValueEventListener(checking);
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentMatchesDb.removeEventListener(checking);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        currentMatchesDb.removeEventListener(checking);
    }

}
