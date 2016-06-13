package com.github.fesswood.cmshptradebot;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.DbRepository;
import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticModel;
import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticObjectMapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.codecraft.xsoup.Xsoup;

import static com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticModel.*;

/**
 * Created by fesswood on 22.05.16.
 */
public class ChartDataExtractor {

    private static final String TAG = ChartDataExtractor.class.getSimpleName();
    private Document mDoc;
    private Document mDocPart;
    private String Xpath = "//*[@id=\"last_orders_list_table\"]/table";
    private boolean mIsInitCompleted;
    private Context mContext;
    private int mTradePeriod;
    private DataExtractionFinishedListener FinishedListener;

    AsyncTask<Void, Void, List<TradeStatisticModel>> mAsyncTask;// =new DataExtractionAsyncTask();
    public ChartDataExtractor() {
    }

    public void init(String html, Context context, @StatisticPeriod int period) {
        mContext = context;
        mTradePeriod = period;
        mDoc = Jsoup.parse(html);
        mIsInitCompleted = true;
    }

    @Nullable
    public String getJsScript() {
        if (mIsInitCompleted) {
            String xpathGetJs = "/html/head/script[2]";
            return Xsoup.compile(xpathGetJs).evaluate(mDoc).get();
        } else {
            Log.e(TAG, "getJsScript: ");
        }
        return null;
    }

    public List<TradeStatisticModel> extractRawData() {
        String dataTable = getJsScript();
        mDocPart = Jsoup.parse(dataTable);
        int dataIndexBegin = 0;
        List<TradeStatisticModel> statisticModelList = null;
        if (dataTable != null) {
            dataIndexBegin = dataTable.indexOf("data.addRows([[");
            int dataIndexFinish = dataTable.indexOf("]]);");
            String data = dataTable.substring(dataIndexBegin, dataIndexFinish);
            statisticModelList = extractTradeStatistic(data);
            Log.d(TAG, "extractRawData: " + data);
        }
        return statisticModelList;
    }

    public List<TradeStatisticModel> extractTradeStatistic(String data) {
        Pattern pattern = Pattern.compile("\\[([^\\[\\]]*)\\]");
        Matcher matcher = pattern.matcher(data);
        int start = 0, i = 1;
        List<TradeStatisticModel> statisticModelList = new ArrayList<>();
        while (matcher.find(start)) {
            String rawData = matcher.group(1);
            Log.d(TAG, "rawData: №" + i + " " + rawData);
            statisticModelList.add(extractFromTooltipString(rawData));
            start = matcher.start() + 1;
            i++;
        }
        return statisticModelList;
    }


    private TradeStatisticModel extractFromTooltipString(String rawData) {
        TradeStatisticModel tradeStatisticModel = null;
        Pattern subPattern = Pattern.compile("\\([^\\(\\)]*\\)");
        Matcher subMatcher = subPattern.matcher(rawData);
        if (subMatcher.find()) {
            String tooltipData = subMatcher
                    .group();
            tooltipData = tooltipData.replace("(", "")
                    .replace(")", "");
            String[] split = tooltipData.split(",");
            tradeStatisticModel = TradeStatisticObjectMapper
                    .mapToTradeStatisticModel(split, mTradePeriod);
        }
        return tradeStatisticModel;
    }

    public void runAsyncTask() {
        Log.d(TAG, "runAsyncTask: start execution AsyncTask");
        mAsyncTask = new DataExtractionAsyncTsak();
        mAsyncTask.execute();

    }


    public void setFinishedListener(DataExtractionFinishedListener finishedListener) {
        FinishedListener = finishedListener;
    }

    public interface DataExtractionFinishedListener {
        void finished();
    }

    public class DataExtractionAsyncTsak extends AsyncTask<Void, Void, List<TradeStatisticModel>> {

        @Override
        protected List<TradeStatisticModel> doInBackground(Void[] params) {
            return extractRawData();
        }

        @Override
        protected void onPostExecute(List<TradeStatisticModel> result) {
            Log.d(TAG, "onPostExecute() called with: " + "result = [" + result + "]");
            DbRepository.saveToDb(result);
            DbRepository.checkRowsInDb();
            if (FinishedListener != null) {
                FinishedListener.finished();
            }
        }
    };
}
