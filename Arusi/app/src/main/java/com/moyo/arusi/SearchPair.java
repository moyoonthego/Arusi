package com.moyo.arusi;

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
    private static Map queryData = new HashMap<>();

    public static int match_val = 0;
    public static String match_uid;

    public static void setupSearching() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("query");

        // storing the current users query in our own custom map (to use for searches later)
        querying = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot querySnapshot : dataSnapshot.getChildren()) {
                    queryData.put(querySnapshot.toString(), querySnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("TAG", "Failed to read app title value.", error.toException());
            }
        };

        currentUser.addValueEventListener(querying);
        Log.d("TAG", "THIS DONE FIRST");
    }

    public static void startSearching() {
        mAuth = FirebaseAuth.getInstance();
        UsersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        // start doing the match up
        checking = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("TAG", dataSnapshot.toString());
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    int cur_match_val = 0;
                    Log.d("TAG", userSnapshot.toString());
                    // looping thru all users, checking to make sure user isn't pairing with self
                    if (!(String.valueOf(userSnapshot.toString()).equals(mAuth.getUid()))) {
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
                        if (cur_match_val > match_val) {
                            match_val = cur_match_val;
                            match_uid = String.valueOf(userSnapshot.toString());
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

        UsersDb.addValueEventListener(checking);

        newData.put("match",match_uid);
        newData.put("found","true");
        newData.put("searching","false");
        UsersDb.child(mAuth.getUid()).updateChildren(newData);
        Log.d("TAG", match_uid.toString());
    }

    public static void stopSearching() {
        UsersDb.removeEventListener(checking);
        Log.d("TAG", "~~~~~~~~~~~~~~~FINISHED SEARCHING~~~~~~~~~~~~");
    }
}
