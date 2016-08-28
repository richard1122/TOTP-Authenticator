package com.hlyue.totpauthenticator.models;

import android.support.annotation.NonNull;

import com.google.common.base.Joiner;
import com.google.common.io.BaseEncoding;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AuthInstance {
    public String path, issuer, secret;

    public static AuthInstance getInstance(URI url) {
        String path = url.getPath();
        Map<String, String> query = AuthUtils.parseQuery(url.getQuery());
        String issuer = query.get("issuer");
        String secret = query.get("secret");
        return AuthInstance.getInstance(path, issuer, secret);
    }

    private static AuthInstance getInstance(@NonNull String path, @NonNull String issuer, @NonNull String secret) {
        AuthInstance authInstance = new AuthInstance();
        authInstance.path = path;
        authInstance.issuer = issuer;
        authInstance.secret = secret;
        return authInstance;
    }

    public static AuthInstance getInstance(@NonNull final Map<String, String> map) {
        String path = map.get("path");
        String issuer = map.get("issuer");
        String secret = map.get("secret");
        return getInstance(path, issuer, secret);
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("path", path);
        map.put("issuer", issuer);
        map.put("secret", secret);
        return map;
    }


    @Override
    public String toString() {
        final String res[] = new String[3];
        res[0] = BaseEncoding.base64().encode(path.getBytes(StandardCharsets.UTF_8));
        res[1] = BaseEncoding.base64().encode(issuer.getBytes(StandardCharsets.UTF_8));
        res[2] = BaseEncoding.base64().encode(secret.getBytes(StandardCharsets.UTF_8));
        return Joiner.on("_").join(res);
    }
}
