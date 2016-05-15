package com.hlyue.totpauthenticator.models;

import android.support.annotation.NonNull;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class AuthInstance {
    private String path, issuer, secret;

    public static AuthInstance getInstance(@NonNull String path,@NonNull String issuer,@NonNull String secret) {
        AuthInstance authInstance = new AuthInstance();
        authInstance.setPath(path);
        authInstance.setIssuer(issuer);
        authInstance.setSecret(secret);
        return authInstance;
    }

    public static AuthInstance getInstance(@NonNull final String string) {
        String[] split = string.split("_");
        String path = new String(BaseEncoding.base64().decode(split[0]), StandardCharsets.UTF_8);
        String issuer = new String(BaseEncoding.base64().decode(split[1]), StandardCharsets.UTF_8);
        String secret = new String(BaseEncoding.base64().decode(split[2]), StandardCharsets.UTF_8);
        return getInstance(path, issuer, secret);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String encodeToString() {
        return String.format(Locale.US, "%s_%s_%s", BaseEncoding.base64().encode(path.getBytes(Charsets.UTF_8)),
                BaseEncoding.base64().encode(issuer.getBytes(Charsets.UTF_8)), BaseEncoding.base64().encode(secret.getBytes(Charsets.UTF_8)));
    }
}
