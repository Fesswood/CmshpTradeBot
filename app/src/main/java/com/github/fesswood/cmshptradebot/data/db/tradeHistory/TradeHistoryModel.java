package com.github.fesswood.cmshptradebot.data.db.tradeHistory;

import io.realm.RealmObject;

/**
 * Created by fesswood on 02.06.16.
 */
public class TradeHistoryModel extends RealmObject {

    String message;
    String dateMark;

    public TradeHistoryModel() {
    }

    public TradeHistoryModel(String dateMark, String message) {
        this.dateMark = dateMark;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateMark() {
        return dateMark;
    }

    public void setDateMark(String dateMark) {
        this.dateMark = dateMark;
    }
}
