package com.github.fesswood.cmshptradebot.data.db;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by fesswood on 31.05.16.
 */
public class Migration implements RealmMigration {

    public static final long VERSION = 0;
    private static final String TAG = Migration.class.getSimpleName();

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        if (oldVersion == 1) {
            Log.e(TAG, "migrate: do migrate there");
            ++oldVersion;
        }
    }
}