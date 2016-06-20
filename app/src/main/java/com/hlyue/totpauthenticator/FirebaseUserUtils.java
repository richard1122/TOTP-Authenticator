package com.hlyue.totpauthenticator;

import android.util.Pair;

import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by helin on 2016/5/24.
 */

public class FirebaseUserUtils {
    static Map<String, String> toMap(final FirebaseUser user) {
        final Map<String, String> map = new HashMap<>();
        map.put("email", user.getEmail());
        map.put("displayName", user.getDisplayName());
        map.put("providerId", user.getProviderId());
        return map;
    }
}
