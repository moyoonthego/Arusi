package com.moyo.arusi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

import static android.app.PendingIntent.getActivity;
import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class QueryActivity extends AppCompatActivity {

    private static final String TAG = "TAG";
    //Needed attributes: Name, Age, Cast (Sect), Location, Marital status, Education, Profession, Nationality, (**CONTACT INFO (Phone number, email address)**)
    // Recommended: Height, Weight, Complexion, Mother Tongue, Family Details (Father, Mother, Brother, Sister, Married Siblings)
    private static int RESULT_LOAD_IMAGE = 1;
    private static int REQUEST_LOCATION = 2;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
    public Criteria criteria;
    public String bestProvider;


    // Database content
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private String currentUId;
    private FirebaseUser firebaseUser;
    private DatabaseReference currentUserDb;
    private DatabaseReference currentUser;
    private Map oldData;
    private Map newData;

    private String country;
    private String state;
    private String city;


    private TextView minage;
    private TextView maxage;
    private CrystalRangeSeekbar agebar;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        // Database setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestId()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).child("query");
        newData = new HashMap<>();

        // Setting up spinners
        // marital status
        String[] maritalitems = new String[]{"Not specified", "Available", "Widowed", "Divorced", "Separated"};
        final ArrayAdapter<String> maritaladapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, maritalitems);
        final Button maritalstatus = (Button) findViewById(R.id.marital_status);
        maritalstatus.setOnClickListener(new View.OnClickListener() {

            public void onClick(View w) {
                new AlertDialog.Builder(QueryActivity.this)
                        .setTitle("Select marital status...")
                        .setAdapter(maritaladapter, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // TODO: user specific action
                                maritalstatus.setText(maritaladapter.getItem(which));
                                if (maritaladapter.getItem(which).equals("Not specified")) {
                                    newData.put("marital", "");
                                } else {
                                    newData.put("marital", maritaladapter.getItem(which));
                                }

                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        // education
        String[] educationitems = new String[]{"Not specified","College", "University (BSc.)", "University (MSc.)", "University (Phd.)", "Degree", "Self-taught", "Other"};
        final ArrayAdapter<String> educationadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, educationitems);
        final Button education = (Button) findViewById(R.id.education);
        education.setOnClickListener(new View.OnClickListener() {

            public void onClick(View w) {
                new AlertDialog.Builder(QueryActivity.this)
                        .setTitle("Select education status met...")
                        .setAdapter(educationadapter, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // TODO: user specific action
                                education.setText(educationadapter.getItem(which));
                                if (educationadapter.getItem(which).equals("Not specified")) {
                                    newData.put("education", "");
                                } else {
                                    newData.put("education", educationadapter.getItem(which));
                                }

                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        // cast status
        String[] castitems = new String[]{"Not specified", "Shia", "Shia (Syed)", "Sunni", "Sunni (Syed)"};
        final ArrayAdapter<String> castadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, castitems);
        final Button caststatus = (Button) findViewById(R.id.muslim_cast);
        caststatus.setOnClickListener(new View.OnClickListener() {

            public void onClick(View w) {
                new AlertDialog.Builder(QueryActivity.this)
                        .setTitle("Select muslim cast...")
                        .setAdapter(castadapter, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // TODO: user specific action
                                caststatus.setText(castadapter.getItem(which));
                                if (castadapter.getItem(which).equals("Not specified")) {
                                    newData.put("cast", "");
                                } else {
                                    newData.put("cast", castadapter.getItem(which));
                                }

                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        // Setting up header
        Typeface myFont = Typeface.createFromAsset(getAssets(), "fonts/productsansbold.ttf");
        final TextView edittext = (TextView) findViewById(R.id.edittext);
        edittext.setTypeface(myFont);


        // setting up age
        agebar = (CrystalRangeSeekbar) findViewById(R.id.ageseekbar);
        minage = findViewById(R.id.minage);
        maxage = findViewById(R.id.maxage);
        maxage.setTypeface(myFont);
        minage.setTypeface(myFont);
        agebar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                minage.setText(String.valueOf(minValue));
                maxage.setText(String.valueOf(maxValue));
            }
        });


        //setting up location
        // country first
        // cast status
        final ArrayList<String> countryitems =new ArrayList<String>();
        countryitems.add("No selection");
        String[] locales = Locale.getISOCountries();

        for (String countryCode : locales) {

            Locale obj = new Locale("", countryCode);
            countryitems.add(obj.getDisplayCountry());
        }
        final ArrayAdapter<String> countryadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, countryitems);
        final Button countrystatus = (Button) findViewById(R.id.country);
        countrystatus.setOnClickListener(new View.OnClickListener() {

            public void onClick(View w) {
                new AlertDialog.Builder(QueryActivity.this)
                        .setTitle("Select a desired country...")
                        .setAdapter(countryadapter, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // TODO: user specific action
                                countrystatus.setText(countryadapter.getItem(which));
                                if (countryadapter.getItem(which).equals("No selection")) {
                                    newData.put("country", "");
                                } else {
                                    newData.put("country", countryadapter.getItem(which));
                                }
                                country = countryadapter.getItem(which);
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

//        final Map<String, Object> worldMap = null;
//
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Moe\\Documents\\GitHub\\Arusi\\Arusi\\app\\src\\main\\assets\\Countries.json"));
//            String world = reader.readLine();
//            worldMap = new Gson().fromJson(world, new TypeToken<HashMap<String, Object>>() {}.getType()
//            );
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // state second
        final ArrayList<String> stateitems =new ArrayList<String>();
        stateitems.add("No selection");
        final Button statestatus = (Button) findViewById(R.id.state);
        statestatus.setOnClickListener(new View.OnClickListener() {

            public void onClick(View w) {

                // UNFINISHED
                //stateitems.addAll();
                final ArrayAdapter<String> stateadapter = new ArrayAdapter<String>(QueryActivity.this, android.R.layout.simple_spinner_dropdown_item, stateitems);

                new AlertDialog.Builder(QueryActivity.this)
                        .setTitle("Select a desired state...")
                        .setAdapter(stateadapter, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // TODO: user specific action
                                statestatus.setText(stateadapter.getItem(which));
                                if (stateadapter.getItem(which).equals("No selection")) {
                                    newData.put("state", "");
                                } else {
                                    newData.put("state", stateadapter.getItem(which));
                                }
                                state = stateadapter.getItem(which);
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });




        // Setting up saved data
        currentUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot querySnapshot : dataSnapshot.getChildren()) {
                            if (!(String.valueOf(querySnapshot.getValue()).equals(""))) {
                                if (querySnapshot.getKey().equals("marital")) {
                                    maritalstatus.setText(String.valueOf(querySnapshot.getValue()));
                                } else if (querySnapshot.getKey().equals("education")) {
                                    education.setText(String.valueOf(querySnapshot.getValue()));
                                } else if (querySnapshot.getKey().equals("country")) {
                                    countrystatus.setText(String.valueOf(querySnapshot.getValue()));
                                } else if (querySnapshot.getKey().equals("cast")) {
                                    caststatus.setText(String.valueOf(querySnapshot.getValue()));
                                } else if (querySnapshot.getKey().equals("minage")) {
                                    minage.setText(String.valueOf(querySnapshot.getValue()));
                                } else if (querySnapshot.getKey().equals("maxage")) {
                                    maxage.setText(String.valueOf(querySnapshot.getValue()));
                                    agebar.setMaxStartValue(Integer.valueOf(String.valueOf(querySnapshot.getValue())));
                                } else if (querySnapshot.getKey().equals("profession")) {
                                    ((EditText) findViewById(R.id.profession)).setText(String.valueOf(querySnapshot.getValue()));
                                } else if (querySnapshot.getKey().equals("nationality")) {
                                    ((EditText) findViewById(R.id.nationality)).setText(String.valueOf(querySnapshot.getValue()));
                                }
                            }
                        }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });

    }

    public boolean isLocationEnabled(Context context)
    {
        if (!EasyPermissions.hasPermissions(QueryActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d(TAG, "Location Permissions not enabled");
            return false;
        } else {
            Log.d(TAG, "Location Permissions enabled");
            return true;
        }
    }

    public void goToMain(View view) {
        view.startAnimation(buttonClick);
        // Change MainActivity to "Settings"
        //newData.put("age", String.valueOf(((SlideBar) findViewById(R.id.age)).getText()));
        newData.put("profession", String.valueOf(((EditText) findViewById(R.id.profession)).getText()));
        newData.put("nationality", String.valueOf(((EditText) findViewById(R.id.nationality)).getText()));
        newData.put("minage", Integer.valueOf(String.valueOf(minage.getText())));
        newData.put("maxage", Integer.valueOf(String.valueOf(maxage.getText())));
        currentUserDb.updateChildren(newData);
        Intent intent = new Intent(QueryActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void fillCountries() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handling different results here

        // First: image request activity result
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            if (!EasyPermissions.hasPermissions(this, galleryPermissions)) {
                EasyPermissions.requestPermissions(this, "Storage access required. Try again.",
                        101, galleryPermissions);
            } else {

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                ImageView imageView = (ImageView) findViewById(R.id.profile_image);
                imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                newData.put("photo", picturePath);

            }
        }

    }
}
