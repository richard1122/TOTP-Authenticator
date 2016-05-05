package com.hlyue.totpauthenticator;

import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.hlyue.totpauthenticator.adapter.AuthListAdapter;
import com.hlyue.totpauthenticator.models.AuthUtils;
import com.hlyue.totpauthenticator.zxing.IntentIntegrator;
import com.hlyue.totpauthenticator.zxing.IntentResult;

import java.net.URI;
import java.net.URISyntaxException;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final static String TAG = MainActivity.class.getName();
    private final static int RESOLVE_CONNECTION_REQUEST_CODE = 0x1;

    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private final Handler mHandler = new Handler();
    private AuthListAdapter mAdapter;
    private Runnable mRunnable;
    private GoogleApiClient mApiClient;

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
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Realm.getDefaultInstance().removeAllChangeListeners();
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
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
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mApiClient.connect();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "GAPI onconnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "GAPI onConnectionSuspended");
    }
}
