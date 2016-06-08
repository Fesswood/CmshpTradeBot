package com.github.fesswood.cmshptradebot.app;

import android.app.Application;
import android.util.Log;

import com.github.fesswood.cmshptradebot.BuildConfig;
import com.github.fesswood.cmshptradebot.data.Migration;

import org.greenrobot.eventbus.EventBus;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by fesswood on 31.05.16.
 */
public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        initRealm();

    }

    public static EventBus getBus(){
        return EventBus.getDefault();
    }
    private void initRealm() {
        Log.d(TAG, "initRealm() starts");
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder(getApplicationContext())
                .schemaVersion(Migration.VERSION)
                .migration(new Migration());

        if (BuildConfig.DEBUG) {
            builder.deleteRealmIfMigrationNeeded();
        }
        RealmConfiguration defaultConfiguration = builder.build();
        Realm.setDefaultConfiguration(defaultConfiguration);

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
        } catch (Exception exception) {
            Log.e(TAG, "Can't migrate realm, deleting realm base to not crush", exception);
            Realm.deleteRealm(defaultConfiguration);
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }
}
