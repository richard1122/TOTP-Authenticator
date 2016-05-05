package com.hlyue.totpauthenticator;

import android.app.backup.BackupManager;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hlyue.totpauthenticator.adapter.AuthListAdapter;
import com.hlyue.totpauthenticator.models.AuthUtils;
import com.hlyue.totpauthenticator.zxing.IntentIntegrator;
import com.hlyue.totpauthenticator.zxing.IntentResult;

import java.net.URI;
import java.net.URISyntaxException;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getName();

    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private final Handler mHandler = new Handler();
    private AuthListAdapter mAdapter;
    private Runnable mRunnable;

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Realm.getDefaultInstance().removeAllChangeListeners();
        mHandler.removeCallbacks(mRunnable);
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
                    Toast.makeText(this, "not a vaild 2 factor auth register barcode", Toast.LENGTH_LONG).show();
                    return;
                }
                AuthUtils.newInstance(uri);
                new BackupManager(this).dataChanged();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "2 factor auth parse failed", Toast.LENGTH_LONG).show();
            }
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
}
