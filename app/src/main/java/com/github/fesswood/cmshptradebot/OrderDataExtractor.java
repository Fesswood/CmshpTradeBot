package com.github.fesswood.cmshptradebot;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticModel;
import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticObjectMapper;
import com.github.fesswood.cmshptradebot.data.db.order.OrderModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.codecraft.xsoup.Xsoup;

import static com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticModel.StatisticPeriod;

/**
 * Created by fesswood on 22.05.16.
 */
public class OrderDataExtractor {

    private static final String TAG = OrderDataExtractor.class.getSimpleName();
    public static final String XPATH_FIRST_BUY_ORDER = "//*[@id=\"buy_orders_list_table\"]/table/tbody/tr[1]";
    public static final String XPATH_SECOND_BUY_ORDER = "//*[@id=\"buy_orders_list_table\"]/table/tbody/tr[2]";
    public static final String XPATH_FIRST_SELL_ORDER = "//*[@id=\"sell_orders_list_table\"]/table/tbody/tr[1]";
    public static final String XPATH_SECOND_SELL_ORDER = "//*[@id=\"sell_orders_list_table\"]/table/tbody/tr[2]";
    private Document mDoc;
    private Document mDocPart;
    private String Xpath = "//*[@id=\"last_orders_list_table\"]/table";
    private boolean mIsInitCompleted;
    private Context mContext;
    private int mTradePeriod;
    private DataExtractionFinishedListener FinishedListener;

    AsyncTask<Void, Void, List<OrderModel>> mAsyncTask;
    private String mXpathFirstSellOrder = "//*[@id=\"buy_orders_list_table\"]/table/tbody/tr[2]";


    public OrderDataExtractor() {
    }

    public void init(String html, Context context, @StatisticPeriod int period) {
        mContext = context;
        mTradePeriod = period;
        mDoc = Jsoup.parse(html);
        mIsInitCompleted = true;
    }

    @Nullable
    public OrderModel getOrder(String xpathGetFirstSellOrder) {
        if (mIsInitCompleted) {
            Elements elements = Xsoup.compile(xpathGetFirstSellOrder).evaluate(mDoc).getElements();
            Elements td = elements.get(0).getElementsByTag("td");
            Element price = td.get(0);
            Element partCount = td.get(1);
            if (price != null && partCount != null) {
                return new OrderModel(Float.parseFloat(price.html()), Integer.parseInt(partCount.html().split("\\<sub\\>")[0]));
            }else {
                Log.e(TAG, "getOrder: price or partCount elemnts is null");
            }
        } else {
            Log.e(TAG, "getOrder: init has't been done!");
        }
        return null;
    }

    public List<OrderModel> extractOrderModelList() {
        List<OrderModel> statisticModelList = new ArrayList<>();
        statisticModelList.add(getOrder(XPATH_FIRST_SELL_ORDER));
        statisticModelList.add(getOrder(XPATH_SECOND_SELL_ORDER));
        statisticModelList.add(getOrder(XPATH_FIRST_BUY_ORDER));
        statisticModelList.add(getOrder(XPATH_SECOND_BUY_ORDER));
        Log.d(TAG, "extractRawData: " + Arrays.toString(statisticModelList.toArray()));
        return statisticModelList;
    }

    public void runAsyncTask() {
        Log.d(TAG, "runAsyncTask: start execution AsyncTask");
        mAsyncTask = new DataExtractionAsyncTask();
        mAsyncTask.execute();

    }


    public void setFinishedListener(DataExtractionFinishedListener finishedListener) {
        FinishedListener = finishedListener;
    }

    public interface DataExtractionFinishedListener {
        /**
         * Callback with parsed lasts orders
         *
         * @param orderModels
         */
        void finished(List<OrderModel> orderModels);
    }

    public class DataExtractionAsyncTask extends AsyncTask<Void, Void, List<OrderModel>> {

        @Override
        protected List<OrderModel> doInBackground(Void[] params) {
            return extractOrderModelList();
        }

        @Override
        protected void onPostExecute(List<OrderModel> result) {
            Log.d(TAG, "onPostExecute() called with: " + "result = [" + result + "]");
            if (FinishedListener != null) {
                FinishedListener.finished(result);
            }
        }
    }

    ;
}
