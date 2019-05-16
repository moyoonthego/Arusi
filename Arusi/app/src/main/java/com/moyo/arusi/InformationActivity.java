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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

import static android.app.PendingIntent.getActivity;
import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class InformationActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "TAG";
    //Needed attributes: Name, Age, Cast (Sect), Location, Marital status, Education, Profession, Nationality, (**CONTACT INFO (Phone number, email address)**)
    // Recommended: Height, Weight, Complexion, Mother Tongue, Family Details (Father, Mother, Brother, Sister, Married Siblings)
    private static int RESULT_LOAD_IMAGE = 1;
    private static int REQUEST_LOCATION = 2;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

    public double latitude;
    public double longitude;
    public LocationManager locationManager;
    public Criteria criteria;
    public String bestProvider;


    // Database content
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private String currentUId;
    private FirebaseUser firebaseUser;
    private DatabaseReference currentUserDb;
    private Map oldData;
    private Map newData;
    private Location mylocation;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        // Database setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestId()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
        newData = new HashMap<>();

        // Setting up location retrieval
        getLocation();

        // Setting up spinners
        // marital status
        String[] maritalitems = new String[]{"Available", "Widowed", "Divorced", "Separated"};
        final ArrayAdapter<String> maritaladapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, maritalitems);
        final Button maritalstatus = (Button) findViewById(R.id.marital_status);
        maritalstatus.setOnClickListener(new View.OnClickListener() {

            public void onClick(View w) {
                new AlertDialog.Builder(InformationActivity.this)
                        .setTitle("Select marital status...")
                        .setAdapter(maritaladapter, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // TODO: user specific action
                                maritalstatus.setText(maritaladapter.getItem(which));
                                newData.put("marital", maritaladapter.getItem(which));

                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        // education
        String[] educationitems = new String[]{"College", "University (BSc.)", "University (MSc.)", "University (Phd.)", "Degree", "Self-taught", "Other"};
        final ArrayAdapter<String> educationadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, educationitems);
        final Button education = (Button) findViewById(R.id.education);
        education.setOnClickListener(new View.OnClickListener() {

            public void onClick(View w) {
                new AlertDialog.Builder(InformationActivity.this)
                        .setTitle("Select education status met...")
                        .setAdapter(educationadapter, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // TODO: user specific action
                                education.setText(educationadapter.getItem(which));
                                newData.put("education", educationadapter.getItem(which));

                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        // cast status
        String[] castitems = new String[]{"Shia", "Shia (Syed)", "Sunni", "Sunni (Syed)"};
        final ArrayAdapter<String> castadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, castitems);
        final Button caststatus = (Button) findViewById(R.id.muslim_cast);
        caststatus.setOnClickListener(new View.OnClickListener() {

            public void onClick(View w) {
                new AlertDialog.Builder(InformationActivity.this)
                        .setTitle("Select muslim cast...")
                        .setAdapter(castadapter, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // TODO: user specific action
                                caststatus.setText(castadapter.getItem(which));
                                newData.put("cast", castadapter.getItem(which));

                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        // Setting up header
        Typeface myFont = Typeface.createFromAsset(getAssets(), "fonts/productsansbold.ttf");
        final TextView edittext = (TextView) findViewById(R.id.whoareyou);
        edittext.setTypeface(myFont);

        final TextView disclaimer = (TextView) findViewById(R.id.disclaimer);

        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });


        // Setting up image view
        final ImageView pic = (ImageView) findViewById(R.id.profile_image);
        final ImageView picview = (ImageView) findViewById(R.id.profileimageview);
        picview.setImageDrawable(pic.getDrawable());
        pic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                    picview.setImageDrawable(pic.getDrawable());
                    pic.setVisibility(View.GONE);
                    edittext.setVisibility(View.GONE);
                    picview.setVisibility(View.VISIBLE);
                    picview.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        });


        picview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                picview.setAdjustViewBounds(true);
                picview.setVisibility(View.GONE);
                edittext.setVisibility(View.VISIBLE);
                pic.setVisibility(View.VISIBLE);
            }
        });

        // Setting up saved data
        currentUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (!(String.valueOf(postSnapshot.getValue()).equals(""))) {
                        if (postSnapshot.getKey().equals("marital")) {
                            maritalstatus.setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("education")) {
                            education.setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("lastname")) {
                            ((EditText) findViewById(R.id.lName)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("age")) {
                            ((EditText) findViewById(R.id.age)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("profession")) {
                            ((EditText) findViewById(R.id.profession)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("phone")) {
                            ((EditText) findViewById(R.id.phone)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("contactemail")) {
                            ((EditText) findViewById(R.id.contactemail)).setText(String.valueOf(postSnapshot.getValue()));
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
                            caststatus.setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("name")) {
                            ((EditText) findViewById(R.id.fName)).setText(String.valueOf(postSnapshot.getValue()));
                        } else if (postSnapshot.getKey().equals("gender")) {
                            if (String.valueOf(postSnapshot.getValue()).equals("Female")) {
                                disclaimer.setVisibility(View.VISIBLE);
                            }
                        } else if (postSnapshot.getKey().equals("photo")) {
                            if (!EasyPermissions.hasPermissions(InformationActivity.this, galleryPermissions)) {
                                EasyPermissions.requestPermissions(InformationActivity.this, "Storage access required. Try again.",
                                        101, galleryPermissions);
                            } else {
                                pic.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(postSnapshot.getValue())));
                                picview.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(postSnapshot.getValue())));
                            }
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

    @SuppressLint("MissingPermission")
    protected void getLocation() {
            if (isLocationEnabled(InformationActivity.this)) {
                Log.d(TAG, "Location check passed");
                locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                criteria = new Criteria();
                bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

                //You can still do this if you like, you might get lucky:
                @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(bestProvider);
                if (location != null) {
                    Log.e("TAG", "GPS is on");
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {
                    //This is what you need:
                    locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
                }
            } else {
                // PROMPT FOR LOCATION SERVICES TO BE ENABLED
                new AlertDialog.Builder(this)
                        .setTitle("Enable Arusi Location Permissions")
                        .setMessage("Arusi requires location services to run. Please enable these to continue setting up your profile.")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // IDEA Make a dialogue box before doing this intent that tells them to enable location permissions
                                Toast.makeText(InformationActivity.this, "Your GPS services are disabled. Please turn them on.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Fix this page redirect
                                startActivity(intent);
                                finish();

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);

    }

    public boolean isLocationEnabled(Context context)
    {
        if (!EasyPermissions.hasPermissions(InformationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d(TAG, "Location Permissions not enabled");
            return false;
        } else {
            Log.d(TAG, "Location Permissions enabled");
            return true;
        }
    }

    public void findLocation(View view) {
        view.startAnimation(buttonClick);
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            newData.put("city", city);
            newData.put("state", state);
            newData.put("country", country);
            ((Button) findViewById(R.id.location)).setText(city+", "+state+", "+country);
            currentUserDb.updateChildren(newData);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(InformationActivity.this, "Error retrieving location", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void goToMain(View view) {
        view.startAnimation(buttonClick);
        // Change MainActivity to "Settings"
        newData.put("name", String.valueOf(((EditText) findViewById(R.id.fName)).getText()));
        newData.put("lastname", String.valueOf(((EditText) findViewById(R.id.lName)).getText()));
        newData.put("age", String.valueOf(((EditText) findViewById(R.id.age)).getText()));
        newData.put("profession", String.valueOf(((EditText) findViewById(R.id.profession)).getText()));
        newData.put("phone", String.valueOf(((EditText) findViewById(R.id.phone)).getText()));
        newData.put("contactemail", String.valueOf(((EditText) findViewById(R.id.contactemail)).getText()));
        newData.put("nationality", String.valueOf(((EditText) findViewById(R.id.nationality)).getText()));
        newData.put("location", String.valueOf(((Button) findViewById(R.id.location)).getText()));
        newData.put("weight", String.valueOf(((EditText) findViewById(R.id.weight)).getText()));
        newData.put("height", String.valueOf(((EditText) findViewById(R.id.height)).getText()));
        newData.put("mothertongue", String.valueOf(((EditText) findViewById(R.id.mothertongue)).getText()));
        newData.put("complexion", String.valueOf(((EditText) findViewById(R.id.complexion)).getText()));
        newData.put("familyinfo", String.valueOf(((EditText) findViewById(R.id.familyinfo)).getText()));
        currentUserDb.updateChildren(newData);
        if (newData.get("name").equals("") || newData.get("lastname").equals("") || newData.get("age").equals("")
        || newData.get("profession").equals("") || newData.get("phone").equals("") || newData.get("contactemail").equals("")
                || newData.get("nationality").equals("") || newData.get("location").equals("Location")
                || String.valueOf(((Button) findViewById(R.id.marital_status)).getText()).equals("Marital Status")
                || String.valueOf(((Button) findViewById(R.id.muslim_cast)).getText()).equals("Cast")
                || String.valueOf(((Button) findViewById(R.id.education)).getText()).equals("Education")
        ) {

            Toast.makeText(InformationActivity.this, "Required info not filled in!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent mIntent = getIntent();
            String previousActivity= mIntent.getStringExtra("FROM_ACTIVITY");
            if (previousActivity.equals("Welcome")) {
                Intent intent = new Intent(InformationActivity.this, QueryActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (NullPointerException e) {

        }

        Intent intent = new Intent(InformationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

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


    @Override
    public void onLocationChanged(Location location) {
        mylocation = location;
        //remove location callback:
        locationManager.removeUpdates(this);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(InformationActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }
}
