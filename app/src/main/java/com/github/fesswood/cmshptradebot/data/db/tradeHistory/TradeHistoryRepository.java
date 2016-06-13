package com.github.fesswood.cmshptradebot.data.db.tradeHistory;

import android.util.Log;

import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticModel;

import java.util.List;

import io.realm.Realm;

/**
 * Created by fesswood on 31.05.16.
 */
public class TradeHistoryRepository {
    private static final String TAG = "DbRepository";

    public static void saveToDb(TradeHistoryModel model) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.copyToRealm(model));
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
}
