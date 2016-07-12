package com.hlyue.totpauthenticator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hlyue.totpauthenticator.adapter.AuthListAdapter;
import com.hlyue.totpauthenticator.models.AuthInstance;
import com.hlyue.totpauthenticator.models.AuthUtils;
import com.hlyue.totpauthenticator.zxing.IntentIntegrator;
import com.hlyue.totpauthenticator.zxing.IntentResult;

import java.io.IOException;
import java.net.URI;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private final static String TAG = MainActivity.class.getName();
    private final static int RC_SIGN_IN = 0x11;
    private final Handler mHandler = new Handler();
    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private AuthListAdapter mAdapter;
    private Runnable mRunnable;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private DatabaseReference mTotpDataReference;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mRecyclerView = (RecyclerView) findViewById(R.id.auth_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter = new AuthListAdapter());

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        mHandler.postDelayed(mRunnable = new Runnable() {
            int previewsProgress = 0;

            @Override
            public void run() {
                int currentProgress = (int) (AuthUtils.getPendingMS() * progressBar.getMax() / 1000 / 30);
                progressBar.setProgress(currentProgress);
                if (currentProgress < previewsProgress) {
                    mAdapter.notifyDataSetChanged();
                }
                previewsProgress = currentProgress;
                mHandler.postDelayed(this, 250);
            }
        }, 250);

        mAuthListener = firebaseAuth -> init();
    }

    private void init() {
        if (mAuth.getCurrentUser() == null) {
            login();
        } else {
            initData();
            Toast.makeText(this, "Current user: " + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initData() {
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();
        //noinspection ConstantConditions
        mReference.child(mAuth.getCurrentUser().getUid()).child("user").setValue(FirebaseUserUtils.toMap(mAuth.getCurrentUser()));
        mTotpDataReference = mReference.child(mAuth.getCurrentUser().getUid()).child("totp");
        mAdapter.init(mTotpDataReference);
    }

    private void login() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
        try {
            mAdapter.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(this, "login failed", Toast.LENGTH_LONG).show();
            }
        }

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null && intentResult.getContents() != null) {
            Log.d(TAG, intentResult.getContents());
            try {
                URI uri = new URI(intentResult.getContents());
                if (!uri.getScheme().equalsIgnoreCase("otpauth")) {
                    Toast.makeText(this, "Not a vaild 2 factor auth register barcode", Toast.LENGTH_LONG).show();
                    return;
                }
                final AuthInstance authInstance = AuthInstance.getInstance(uri);
                mTotpDataReference.push().setValue(authInstance.toString());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "2 factor auth parse failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "signInWithCredential", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_new) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
    }
}
