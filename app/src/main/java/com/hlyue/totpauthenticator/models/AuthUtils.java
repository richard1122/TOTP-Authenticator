package com.hlyue.totpauthenticator.models;

import com.google.common.base.Splitter;
import com.google.common.io.BaseEncoding;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.realm.Realm;

/**
 * Created by v-linyhe on 9/6/2015.
 */
public class AuthUtils {
    private static final String HMAC_ALGO = "HmacSHA1";

    public static String buildPrimaryKey(AuthInstance instance) {
        return instance.getIssuer() + "/" + instance.getPath();
    }

    public static void newInstance(URI url) {
        String path = url.getPath();
        Map<String, String> query = parseQuery(url.getQuery());
        String issuer = query.get("issuer");
        String secret = query.get("secret");

        AuthInstance authInstance = AuthInstance.getInstance(path, issuer, secret);
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(authInstance);
        realm.commitTransaction();
    }

    public static long getPendingMS() {
        return System.currentTimeMillis() % (30 * 1000);
    }

    public static int calculateTOTP(AuthInstance instance) {
        byte[] secretBytes = BaseEncoding.base32().decode(instance.getSecret().toUpperCase());
        long time = (System.currentTimeMillis() / 1000 / 30);
        byte[] timeBytes = ByteBuffer.allocate(8).putLong(time).array();
        SecretKeySpec signKey = new SecretKeySpec(secretBytes, HMAC_ALGO);
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(signKey);
            byte[] hash = mac.doFinal(timeBytes);
            int offset = hash[hash.length - 1] & 0xf;
            int truncatedHash = ByteBuffer.wrap(hash, offset, 4).order(ByteOrder.BIG_ENDIAN).getInt();
            truncatedHash &= 0x7fff_ffff;
            return (int) (truncatedHash % 1000000);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return -1;
    }


    private static Map<String, String> parseQuery(String query) {
        return Splitter.on('&').trimResults().withKeyValueSeparator('=').split(query);
    }
}
