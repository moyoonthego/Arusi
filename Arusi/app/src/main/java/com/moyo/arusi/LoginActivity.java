package com.moyo.arusi;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private ProgressBar progressBar;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
    private Button mLogin;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient googleSignInClient;
    private SignInButton googleSignInButton;
    private boolean triggerStay;
    private EditText mEmail, mPassword;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseUser finalUser;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        googleSignInButton = findViewById(R.id.sign_in_button);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestId()
                .requestProfile()
                .requestEmail()
                .build();

        triggerStay = false;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, LoginActivity.this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        mLogin = (Button) findViewById(R.id.login);
        progressBar = findViewById(R.id.progress_circular);

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        finalUser = null;

        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getInstance().getCurrentUser();
                if (user != null) {
                    if (triggerStay) {
                        mAuth.signOut();
                        googleSignInClient.signOut();
                        Intent intent = new Intent(LoginActivity.this, LoginOrRegister.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

            }
        };

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                progressBar.setVisibility(View.VISIBLE);
                signIn();
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                progressBar.setVisibility(View.VISIBLE);
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                if ((email.equals("")) || (password.equals(""))) {
                    Toast.makeText(LoginActivity.this, "Info not filled in!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Sign in error", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user);
        }

    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            String photo = String.valueOf(user.getPhotoUrl());
        }
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Sign in failed, account not found.",  Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.INVISIBLE);

                        Log.d("TAG", "signInWithCredential:success");
                        DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users");
                        // PROBLEM BELOWWWWW
                        currentUserDb.orderByChild("googleverify").equalTo(account.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed. Account nonexistent.", Toast.LENGTH_SHORT).show();
                                    triggerStay = true;
                                    firebaseAuthStateListener.onAuthStateChanged(mAuth);
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);

                        Log.w("TAG", "signInWithCredential:failure", task.getException());

                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                }
            });
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this,LoginOrRegister.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(LoginActivity.this, "Authentication failed: Connection Error",
                Toast.LENGTH_SHORT).show();
    }
}



//    String Uid = ;
//    DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users");
//                    currentUserDb.child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
//@Override
//public void onDataChange(DataSnapshot snapshot) {
//        if (snapshot.exists()) {
//        Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
//        } else {
//        Toast.makeText(LoginActivity.this, "That account does not exist.", Toast.LENGTH_SHORT).show();
//        }
//        }
//
//@Override
//public void onCancelled(@NonNull DatabaseError databaseError) {
//
//        }
//        });