package com.hlyue.totpauthenticator.models;

import android.support.annotation.NonNull;

import com.google.common.base.Joiner;
import com.google.common.io.BaseEncoding;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

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

    public static AuthInstance getInstance(@NonNull final String string) {
        String[] split = string.split("_");
        String path = new String(BaseEncoding.base64().decode(split[0]), StandardCharsets.UTF_8);
        String issuer = new String(BaseEncoding.base64().decode(split[1]), StandardCharsets.UTF_8);
        String secret = new String(BaseEncoding.base64().decode(split[2]), StandardCharsets.UTF_8);
        return getInstance(path, issuer, secret);
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
