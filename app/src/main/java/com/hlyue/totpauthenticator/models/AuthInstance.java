package com.hlyue.totpauthenticator.models;

import android.util.Log;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import java.math.BigInteger;
import java.util.Arrays;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by v-linyhe on 9/6/2015.
 */
public class AuthInstance extends RealmObject {
    private @PrimaryKey String primaryKey;
    private String path, issuer, secret;

    public static AuthInstance getInstance(String path, String issuer, String secret) {
        AuthInstance authInstance = new AuthInstance();
        authInstance.setPath(path);
        authInstance.setIssuer(issuer);
        authInstance.setSecret(secret);
        authInstance.setPrimaryKey(AuthUtils.buildPrimaryKey(authInstance));
        return authInstance;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
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
}
