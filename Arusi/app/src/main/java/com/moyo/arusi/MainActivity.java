package com.moyo.arusi;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private String currentUId;

    private DatabaseReference usersDb;


    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();

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

    public void logoutUser(View view) {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginOrRegister.class);
        startActivity(intent);
        finish();
    }

    public void goToSettings(View view) {
        // Change MainActivity to "Settings"
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.putExtra("userSex", userSex);
        startActivity(intent);
    }
}
