package com.hlyue.totpauthenticator;

import android.app.backup.BackupManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.hlyue.totpauthenticator.adapter.AuthListAdapter;
import com.hlyue.totpauthenticator.models.AuthUtils;
import com.hlyue.totpauthenticator.zxing.IntentIntegrator;
import com.hlyue.totpauthenticator.zxing.IntentResult;

import java.io.IOException;
import java.net.URI;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getName();
    private final static int REQUEST_FIREBASE_SIGNIN = 0x33;
    private final Handler mHandler = new Handler();
    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private AuthListAdapter mAdapter;
    private Runnable mRunnable;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        mAuth = FirebaseAuth.getInstance();

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
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "current user: " + mAuth.getCurrentUser().getEmail());
            Toast.makeText(this, "Current user: " + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
        }
        else {
            Log.w(TAG, "no current user");
            startActivityForResult(
                    AuthUI.getInstance(FirebaseApp.getInstance()).createSignInIntentBuilder().setProviders(AuthUI.GOOGLE_PROVIDER).build(),
                    REQUEST_FIREBASE_SIGNIN);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null && intentResult.getContents() != null) {
            Log.d(TAG, intentResult.getContents());
            try {
                URI uri = new URI(intentResult.getContents());
                if (!uri.getScheme().equalsIgnoreCase("otpauth")) {
                    Toast.makeText(this, "Not a vaild 2 factor auth register barcode", Toast.LENGTH_LONG).show();
                    return;
                }
                AuthUtils.newInstance(uri);
                new BackupManager(this).dataChanged();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "2 factor auth parse failed", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == REQUEST_FIREBASE_SIGNIN) {
            Log.d(TAG, "fireui signin response: " + resultCode);
        }
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
}
