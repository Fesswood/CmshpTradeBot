package com.github.fesswood.cmshptradebot.data.db.TradeStatistic;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by fesswood on 29.05.16.
 */
public class TradeStatisticModel extends RealmObject {

    @PrimaryKey
    long mTimestamp;
    double mMinPrice;
    double mMaxPrice;
    double mClosingPrice;
    double mOpeningPrice;
    double mAmountUsd;
    double mPartCount;
    @StatisticPeriod int period;

    public static final int HOUR = 0;
    public static final int DAY = 1;
    public static final int WEEK = 2;
    public static final int MONTH = 3;
    public static final int YEAR = 4;
    @IntDef({
            HOUR,
            DAY,
            WEEK,
            MONTH,
            YEAR,
    })

    @Retention(RetentionPolicy.SOURCE)
    public @interface StatisticPeriod {
    }

    public TradeStatisticModel() {
    }

    public TradeStatisticModel(long timestamp,
                               double minPrice,
                               double maxPrice,
                               double closingPrice,
                               double openingPrice,
                               double amountUsd,
                               double pieceCount,
                               @StatisticPeriod int period) {
        this.mTimestamp = timestamp;
        this.mMinPrice = minPrice;
        this.mMaxPrice = maxPrice;
        this.mClosingPrice = closingPrice;
        this.mOpeningPrice = openingPrice;
        this.mAmountUsd = amountUsd;
        this.mPartCount = pieceCount;
        this.period = period;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    public double getMinPrice() {
        return mMinPrice;
    }

    public void setMinPrice(double minPrice) {
        this.mMinPrice = minPrice;
    }

    public double getMaxPrice() {
        return mMaxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.mMaxPrice = maxPrice;
    }

    public double getClosingPrice() {
        return mClosingPrice;
    }

    public void setClosingPrice(double closingPrice) {
        this.mClosingPrice = closingPrice;
    }

    public double getOpeningPrice() {
        return mOpeningPrice;
    }

    public void setOpeningPrice(double openingPrice) {
        this.mOpeningPrice = openingPrice;
    }

    public double getAmountUsd() {
        return mAmountUsd;
    }

    public void setAmountUsd(double amountUsd) {
        this.mAmountUsd = amountUsd;
    }

    public double getPartCount() {
        return mPartCount;
    }

    public void setPartCount(double partCount) {
        this.mPartCount = partCount;
    }
}
