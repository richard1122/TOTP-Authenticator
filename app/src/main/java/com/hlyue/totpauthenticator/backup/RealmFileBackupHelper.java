package com.hlyue.totpauthenticator.backup;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.hlyue.totpauthenticator.models.AuthInstance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by v-linyhe on 9/6/2015.
 */
public class RealmFileBackupHelper extends BackupAgent {
    /**
     * Construct a helper to manage backup/restore of entire files within the
     * application's data directory hierarchy.
     *
     * @param context The backup agent's Context object
     * @param files   A list of the files to be backed up or restored.
     */
    public final static String KEY = "REALM_DB";

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        Log.d(KEY, "on backup");
        Realm realm = Realm.getDefaultInstance();
        RealmResults<AuthInstance> authInstances = realm.allObjects(AuthInstance.class);
        if (authInstances.size() == 0) return;
        ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
        DataOutputStream outWriter = new DataOutputStream(bufStream);
        Log.d(KEY, "ready to write " + authInstances.size() + " objects");
        outWriter.writeInt(authInstances.size());
        for (AuthInstance instance : authInstances) {
            Log.d(KEY, "ready to write " + instance.getPrimaryKey());
            outWriter.writeUTF(instance.getPath());
            outWriter.writeUTF(instance.getIssuer());
            outWriter.writeUTF(instance.getSecret());
        }
        byte[] buffer = bufStream.toByteArray();
        data.writeEntityHeader(KEY, buffer.length);
        data.writeEntityData(buffer, buffer.length);
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        Log.d(KEY, "on restore");
        Realm realm = Realm.getDefaultInstance();
        while (data.readNextHeader()) {
            if (data.getKey().equals(KEY)) {
                byte[] buffer = new byte[data.getDataSize()];
                data.readEntityData(buffer, 0, data.getDataSize());
                DataInputStream bufStream = new DataInputStream(new ByteArrayInputStream(buffer));
                int len = bufStream.readInt();
                Log.d(KEY, "ready to restore " + len + " objects");
                for (int i = 0; i != len; ++i) {
                    String path = bufStream.readUTF();
                    String issuer = bufStream.readUTF();
                    String secret = bufStream.readUTF();
                    AuthInstance instance = AuthInstance.getInstance(path, issuer, secret);
                    Log.d(KEY, "ready to restore " + instance.getPrimaryKey());
                }
                return;
            }
        }
    }
}
