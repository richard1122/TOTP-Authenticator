package com.hlyue.totpauthenticator.backup;

import android.app.backup.FileBackupHelper;
import android.content.Context;

/**
 * Created by v-linyhe on 9/6/2015.
 */
public class RealmFileBackupHelper extends FileBackupHelper {
    /**
     * Construct a helper to manage backup/restore of entire files within the
     * application's data directory hierarchy.
     *
     * @param context The backup agent's Context object
     * @param files   A list of the files to be backed up or restored.
     */
    public RealmFileBackupHelper(Context context, String... files) {
        super(context, files);

    }
}
