package com.hlyue.totpauthenticator;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class MyApplication extends Application {
    private static SharedPreferences preferences;
    private static final String PREFERENCE = "totp";

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(PREFERENCE, 0);
    }

    @NonNull public static SharedPreferences getTotpPreference() {
        if (preferences == null) throw new RuntimeException("preference not initialized yet.");
        return preferences;
    }
}
