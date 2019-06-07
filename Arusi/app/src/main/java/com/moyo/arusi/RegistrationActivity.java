package com.moyo.arusi;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.SignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.GoogleAuthProvider;

import android.util.Log;
import android.widget.ProgressBar;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private ProgressBar progressBar;
    private Button mRegister;
    private RadioButton mRadio;
    private EditText mEmail, mPassword, mName;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount mAcct = null;

    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient googleSignInClient;
    private SignInButton googleSignInButton;
    private String gender = "";
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        progressBar = findViewById(R.id.progress_circular);
        mAuth = FirebaseAuth.getInstance();

        googleSignInButton = findViewById(R.id.sign_in_button);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestProfile()
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        mRegister = findViewById(R.id.register);
        mRadio = (RadioButton) findViewById(((RadioGroup) findViewById(R.id.radioSex)).getCheckedRadioButtonId());

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mName = findViewById(R.id.name);

        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mAuth = FirebaseAuth.getInstance();
                if (mAuth.getUid() != null){
                    Intent intent = new Intent(RegistrationActivity.this, Welcome.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                progressBar.setVisibility(View.VISIBLE);
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String name = mName.getText().toString();
                mRadio = (RadioButton) findViewById(((RadioGroup) findViewById(R.id.radioSex)).getCheckedRadioButtonId());
                if (mRadio != null) {
                    gender = String.valueOf(mRadio.getText());
                }
                if ((email.equals("")) || (password.equals(""))  || (name.equals(""))) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(RegistrationActivity.this, "Info not filled in!", Toast.LENGTH_SHORT).show();
                } else if (gender == "") {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(RegistrationActivity.this, "Gender not given!", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                    Toast.makeText(RegistrationActivity.this, "Weak password found. Please ensure you have at least 6 characters.",
                                            Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                } else {
                                    Toast.makeText(RegistrationActivity.this, "Sign up error, try again", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                String userId = mAuth.getCurrentUser().getUid();
                                DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                                Map userInfo = new HashMap<>();
                                userInfo.put("contactemail", email);
                                userInfo.put("email", email);
                                userInfo.put("password", password);
                                userInfo.put("gender", gender);
                                userInfo.put("name", name);
                                userInfo.put("profileImageUrl", "default");
                                userInfo.put("googleverify", "null");
                                currentUserDb.updateChildren(userInfo);
                                mAuth.getCurrentUser().sendEmailVerification();
                            }
                        }
                    });
                }
            }
        });

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }

    private void updateUI(FirebaseUser user) {

    }

    private void updateUIGoogle(GoogleSignInAccount acct) {

        if (mRadio != null) {
            gender = String.valueOf(mRadio.getText());
        }
        FirebaseUser user = mAuth.getCurrentUser();

        String name = "";
        String email = "";
        String password = "";

        if (acct != null) {
            name = acct.getGivenName();
            email = acct.getEmail();
        } else {
            Toast.makeText(RegistrationActivity.this, "Authentication Error", Toast.LENGTH_SHORT).show();
        }
        String userId = user.getUid();
        DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        Map userInfo = new HashMap<>();
        userInfo.put("email", email);
        userInfo.put("contactemail", email);
        userInfo.put("gender", gender);
        userInfo.put("name", acct.getGivenName());
        userInfo.put("phone", user.getPhoneNumber());
        userInfo.put("lastname", acct.getFamilyName());
        userInfo.put("password", "arusi");
        userInfo.put("googleverify", acct.getId());
        currentUserDb.updateChildren(userInfo);

        mAuth.getCurrentUser().sendEmailVerification();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mRadio = (RadioButton) findViewById(((RadioGroup) findViewById(R.id.radioSex)).getCheckedRadioButtonId());
        if (mRadio != null) {
            gender = String.valueOf(mRadio.getText());
        }
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            mAcct = task.getResult();
            if (gender == "") {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(RegistrationActivity.this, "Gender not given!", Toast.LENGTH_SHORT).show();
            } else if (mAcct != null) {
                firebaseAuthWithGoogle();
            }
        }
    }

    private void firebaseAuthWithGoogle() {
        Log.d("TAG", "firebaseAuthWithGoogle:" + mAcct.getId());
        final AuthCredential credential = GoogleAuthProvider.getCredential(mAcct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                final String gender = String.valueOf(mRadio.getText());
                if (gender == "") {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(RegistrationActivity.this, "Gender not given!", Toast.LENGTH_SHORT).show();
                } else if (task.isSuccessful()) {
                    progressBar.setVisibility(View.INVISIBLE);

                    Log.d("TAG", "signInWithCredential:success");
                    updateUIGoogle(mAcct);

                } else {
                    progressBar.setVisibility(View.INVISIBLE);

                    Log.w("TAG", "signInWithCredential:failure", task.getException());
                    if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                        Toast.makeText(RegistrationActivity.this, "Weak password found. Please ensure you have at least 6 characters.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                    updateUI(null);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(RegistrationActivity.this, "Connection failure...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this,LoginOrRegister.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }
}

