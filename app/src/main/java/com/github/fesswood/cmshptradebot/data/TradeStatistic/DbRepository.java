package com.github.fesswood.cmshptradebot.data.TradeStatistic;

import android.util.Log;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by fesswood on 31.05.16.
 */
public class DbRepository {
    private static final String TAG = "DbRepository";

    public static void saveToDb(List<TradeStatisticModel> statisticModelList) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(statisticModelList));
        realm.close();
    }

    public static List<TradeStatisticModel> getAllTradeStatisticModel() {
        Realm realm = Realm.getDefaultInstance();
        Log.i(TAG, "checkRowsInDb: "+realm.where(TradeStatisticModel.class).count());
        List<TradeStatisticModel> statisticModels =
                realm.copyFromRealm(realm.where(TradeStatisticModel.class).findAll());
        realm.close();
        return statisticModels;
    }



    public static List<TradeStatisticModel> getAllTradeStatisticModelAfter(long timestamp) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<TradeStatisticModel> realmObjects = realm
                .where(TradeStatisticModel.class)
                .greaterThan("mTimestamp", timestamp)
                .findAll();
        List<TradeStatisticModel> statisticModels =
                realm.copyFromRealm(realmObjects);
        realm.close();
        return statisticModels;
    }

    public static void checkRowsInDb() {
        Realm realm = Realm.getDefaultInstance();
        Log.i(TAG, "checkRowsInDb: "+realm.where(TradeStatisticModel.class).count());
        realm.close();
    }
    public static void clearData() {
        Realm realm = Realm.getDefaultInstance();
        realm.close();
    }
}
