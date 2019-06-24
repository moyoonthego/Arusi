package com.moyo.arusi;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchPair {

    // Database content
    private static FirebaseAuth mAuth;
    private static FirebaseUser firebaseUser;
    private static DatabaseReference UsersDb;
    private static DatabaseReference currentUser;
    private static ValueEventListener checking;
    private static ValueEventListener querying;
    private static Map newData = new HashMap<>();
    public static Map queryData = new HashMap<>();

    public static int match_val = 0;
    public static String match_uid = "";
    public static String nGender;

    public static void setupSearching() {
    }

    public static void startSearching() {
        UsersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        // start doing the match up

        mAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("query");

        // all this just to get gender...
        final DatabaseReference currentUserInfo = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid());
        currentUserInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nGender = String.valueOf(dataSnapshot.child("gender").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final ArrayList blacklistarray = new ArrayList();
        final ArrayList whitelistarray = new ArrayList();

        // all of this just to get blacklisted matches(perhaps firebase isnt that great... use mongodb next time)
        currentUserInfo.child("blacklist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot blacklistSnapshot : dataSnapshot.getChildren()) {
                    blacklistarray.add(blacklistSnapshot.getKey().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // all of this just to get whitelisted matches(perhaps firebase isnt that great... use mongodb next time)
        currentUserInfo.child("whitelist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot whitelistSnapshot : dataSnapshot.getChildren()) {
                    whitelistarray.add(whitelistSnapshot.getKey().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // storing the current users query in our own custom map (to use for searches later)
        querying = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot querySnapshot : dataSnapshot.getChildren()) {
                    queryData.put(querySnapshot.getKey().toString(), querySnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("TAG", "Failed to read app title value.", error.toException());
            }
        };

        currentUser.addListenerForSingleValueEvent(querying);

        checking = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("USER DATABASE", dataSnapshot.toString());
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    int cur_match_val = 0;
                    Log.d("FOUND USER", userSnapshot.toString());
                    // looping thru all users, checking to make sure user isn't pairing with self, and not with opposite gender either
                    if (!(userSnapshot.getKey().trim().toLowerCase()).equals(mAuth.getUid().toLowerCase().trim()) &&
                            !(userSnapshot.child("gender").getValue().equals(nGender)) &&
                            !(blacklistarray.contains(userSnapshot.getKey().toString())) &&
                            !(whitelistarray.contains(userSnapshot.getKey().toString()))) { // also make sure this user wasn't neglected (swiped left or already matched)
                        Log.d("FOUND USER (MATCH): ", userSnapshot.getKey()+"*********"+blacklistarray.contains(userSnapshot.getKey()));
                        for (DataSnapshot userInfoSnapshot : userSnapshot.getChildren()) {
                            // now to check user against query and add value to our variables
                            // check age query
                            if (userInfoSnapshot.getKey().equals("age") && queryData.containsKey("minage") && queryData.containsKey("maxage")) {
                                if (Integer.valueOf(queryData.get("minage").toString()) <= Integer.valueOf(userInfoSnapshot.getValue().toString()) &&
                                        Integer.valueOf(queryData.get("maxage").toString())  >= Integer.valueOf(userInfoSnapshot.getValue().toString())
                                ) {
                                    cur_match_val = cur_match_val + 1;
                                }
                                // check profession query
                            } else if (userInfoSnapshot.getKey().equals("profession") && queryData.containsKey("profession") && !queryData.get("profession").equals("")) {
                                if (queryData.get("profession").toString().toLowerCase().trim().contains(userInfoSnapshot.getValue().toString().toLowerCase().trim()) ||
                                        userInfoSnapshot.getValue().toString().toLowerCase().trim().contains(queryData.get("profession").toString().toLowerCase().trim())) {
                                    cur_match_val = cur_match_val + 1;
                                }
                                // check marital status
                            } else if (userInfoSnapshot.getKey().equals("marital") && queryData.containsKey("marital") && !queryData.get("marital").equals("")) {
                                if (queryData.get("marital").toString().equals(userInfoSnapshot.getValue().toString())) {
                                    cur_match_val = cur_match_val + 1;
                                }
                                // check degree/education
                            } else if (userInfoSnapshot.getKey().equals("education") && queryData.containsKey("education") && !queryData.get("education").equals("")) {
                                // This case is special... if I'm looking for University MSc., University gets a +1 but University MSc. gets a +2
                                String cur = userInfoSnapshot.getValue().toString();
                                if (queryData.get("education").toString().equals(cur)) {
                                    cur_match_val = cur_match_val + 1;
                                }
                                if (cur.contains(" ")) {
                                    if (queryData.get("education").toString().toLowerCase().contains(cur.substring(0, cur.indexOf(" ")).toLowerCase().trim())) {
                                        cur_match_val = cur_match_val + 1;
                                    }
                                }
                                // check for muslim cast
                            } else if (userInfoSnapshot.getKey().equals("cast") && queryData.containsKey("cast") && !queryData.get("cast").equals("")) {
                                // This case is special... if I'm looking for University MSc., University gets a +1 but University MSc. gets a +2
                                String cur = userInfoSnapshot.getValue().toString();
                                if (queryData.get("cast").toString().equals(cur)) {
                                    cur_match_val = cur_match_val + 1;
                                }
                                if (cur.contains(" ")) {
                                    if (queryData.get("cast").toString().toLowerCase().contains(cur.substring(0, cur.indexOf(" ")).toLowerCase().trim())) {
                                        cur_match_val = cur_match_val + 1;
                                    }
                                }
                                // check nationality
                            } else if (userInfoSnapshot.getKey().equals("nationality") && queryData.containsKey("nationality") && !queryData.get("nationality").equals("")) {
                                if (queryData.get("nationality").toString().toLowerCase().trim().contains(userInfoSnapshot.getValue().toString().toLowerCase().trim()) ||
                                        userInfoSnapshot.getValue().toString().toLowerCase().trim().contains(queryData.get("nationality").toString().toLowerCase().trim())) {
                                    cur_match_val = cur_match_val + 1;
                                }
                                // check for country
                            } else if (userInfoSnapshot.getKey().equals("country") && queryData.containsKey("country") && !queryData.get("country").equals("")) {
                                if (queryData.get("country").toString().toLowerCase().trim().contains(userInfoSnapshot.getValue().toString().toLowerCase().trim())) {
                                    cur_match_val = cur_match_val + 1;
                                }
                                // check state
                            } else if (userInfoSnapshot.getKey().equals("state") && queryData.containsKey("state") && !queryData.get("state").equals("")) {
                                if (queryData.get("state").toString().toLowerCase().trim().contains(userInfoSnapshot.getValue().toString().toLowerCase().trim())) {
                                    cur_match_val = cur_match_val + 1;
                                }
                                // check city
                            } else if (userInfoSnapshot.getKey().equals("city") && queryData.containsKey("city") && !queryData.get("city").equals("")) {
                                if (queryData.get("city").toString().toLowerCase().trim().contains(userInfoSnapshot.getValue().toString().toLowerCase().trim())) {
                                    cur_match_val = cur_match_val + 1;
                                }
                            }
                        }
                        if (cur_match_val >= match_val) {
                            match_val = cur_match_val;
                            match_uid = String.valueOf(userSnapshot.getKey());
                        }
                    }
                }

                newData.put("match",match_uid);
                newData.put("found","true");
                newData.put("searching","false");
                UsersDb.child(mAuth.getUid()).updateChildren(newData);
                Log.d("TAG", "~~~~~~~~~~~~~~~FINISHED SEARCHING~~~~~~~~~~~~:"+match_uid);
                match_val = 0;
                match_uid = "";
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("TAG", "Failed to read app title value.", error.toException());
            }
        };

        UsersDb.addListenerForSingleValueEvent(checking);
    }

    public static void stopSearching() {
        UsersDb.removeEventListener(checking);
    }
}
