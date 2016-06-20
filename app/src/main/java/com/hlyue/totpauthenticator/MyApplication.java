package com.hlyue.totpauthenticator;

import android.app.Application;
import android.content.SharedPreferences;

import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
