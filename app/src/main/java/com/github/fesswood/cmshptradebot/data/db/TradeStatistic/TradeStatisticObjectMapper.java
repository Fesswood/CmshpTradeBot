package com.github.fesswood.cmshptradebot.data.db.TradeStatistic;

import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleEntry;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import eu.verdelhan.ta4j.Tick;

import static com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticModel.*;

/**
 * Created by fesswood on 29.05.16.
 */
public class TradeStatisticObjectMapper {

    private static final String TAG = TradeStatisticObjectMapper.class.getSimpleName();

    public static TradeStatisticModel mapToTradeStatisticModel(String[] rawFields,
                                                               @StatisticPeriod int period) {
        String date = rawFields[0];
        String minPrice = rawFields[1];
        String openingPrice = rawFields[2];
        String closingPrice = rawFields[3];
        String maxPrice = rawFields[4];
        String amount = rawFields[6];
        String partCount = rawFields[5];
        long timestamp = dateFormatting(date, period);
        return new TradeStatisticModel(timestamp,
                Double.parseDouble(minPrice),
                Double.parseDouble(maxPrice),
                Double.parseDouble(openingPrice),
                Double.parseDouble(closingPrice),
                Double.parseDouble(amount),
                Double.parseDouble(partCount),
                period);
    }

    public static Tick mapToTick(TradeStatisticModel model) {
        return new Tick(new DateTime(model.getTimestamp()),
                "" + model.getOpeningPrice(),
                "" + model.getMaxPrice(),
                "" + model.getMinPrice(),
                "" + model.getClosingPrice(),
                "" + model.getPartCount());
    }

    public static CandleEntry mapToCandleEntry(int index, TradeStatisticModel model) {
        return new CandleEntry(index,
                (float) model.getMaxPrice(),
                (float) model.getMinPrice(),
                (float) model.getOpeningPrice(),
                (float) model.getClosingPrice(),
                "FUCKING DATA HERE");
    }

    public static BarEntry mapToBarEntry(int index, TradeStatisticModel model) {
        return new BarEntry((float) model.getPartCount(), index);
    }

    private static long dateFormatting(String date, @StatisticPeriod int period) {
        String dateWithoutText = null;
        switch (period) {
            case HOUR:
                dateWithoutText = date.replace("\'", "").replace(".", ":");
                break;
            case DAY:
                dateWithoutText = date.replace("\'", "").replace(" час", ":00");
                break;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date parse = formatter.parse(dateWithoutText);
            return parse.getTime();
        } catch (ParseException e) {
            Log.e(TAG, "dateFormatting: ", e);
        }
        return 0;
    }

}
