package com.github.fesswood.cmshptradebot.presentation.main;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.github.fesswood.cmshptradebot.domain.ChartDataExtractor;
import com.github.fesswood.cmshptradebot.R;
import com.github.fesswood.cmshptradebot.app.App;
import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticModel;
import com.github.fesswood.cmshptradebot.data.db.TradingBotOnMovingTimeSeries;
import com.github.fesswood.cmshptradebot.data.event.TradeEvent;
import com.github.fesswood.cmshptradebot.data.db.tradeHistory.TradeHistoryModel;
import com.github.fesswood.cmshptradebot.data.db.tradeHistory.TradeHistoryRepository;
import com.github.fesswood.cmshptradebot.presentation.main.common.WebViewManager;

import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class WebVIewActivityFragment extends Fragment implements TradingBotOnMovingTimeSeries.StragegyListener {

    private static final String TAG = WebVIewActivityFragment.class.getSimpleName();
    private Handler mHandler;
    private boolean mIsFirstLaunch = true;
    private RepeatRunnable mRepeatRunnable;
    private int mUpdateInterval;
    private WebViewManager mViewManager;
    ChartDataExtractor mChartDataExtractor;
    private TradingBotOnMovingTimeSeries mTradingBot;
    private boolean mIsBotirstLaunch = true;

    public WebVIewActivityFragment() {
    }

    public static Fragment newInstance() {
        return new WebVIewActivityFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);
        mViewManager = new WebViewManager((WebView) view.findViewById(R.id.wvSite));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mChartDataExtractor = new ChartDataExtractor();
        mTradingBot = new TradingBotOnMovingTimeSeries();
        mHandler = new Handler();
        mRepeatRunnable = new RepeatRunnable();
        mUpdateInterval = 29 * 1000;
        mViewManager.setLoadPageWithDataListener(this::extractData);
        mViewManager.startWithBasicUrl();
        mChartDataExtractor.setFinishedListener(this::startTrade);
    }

    private void startTrade() {
        if (mIsBotirstLaunch) {
            Log.i(TAG, "startTrade: init bot");
            mTradingBot.init(this);
            mTradingBot.checkStrategy();
            mIsBotirstLaunch = false;
        } else {
            Log.i(TAG, "startTrade: add Tick from Db");
            mTradingBot.addTicksFromDb();
            mTradingBot.checkStrategy();
        }
        mHandler.postDelayed(mRepeatRunnable, mUpdateInterval);
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        //
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRepeatRunnable);
    }

    private void extractData(String html) {
        mChartDataExtractor.init(html, getContext(), TradeStatisticModel.DAY);
        mChartDataExtractor.runAsyncTask();
    }

    @Override
    public void exit(double price, int amount) {
        mViewManager.exitFromMarket(price, amount);
        String message = "exit from market; price " + price + " " + amount
                + " \n" + mTradingBot.calculateProfit();
        Log.i(TAG, "exit: " + message);
        TradeHistoryRepository
                .saveToDb(new TradeHistoryModel("" + System.currentTimeMillis(), message));
        App.getBus().post(new TradeEvent(new Date() + " " + message + "\n"));
        mHandler.removeCallbacks(mRepeatRunnable);
        mHandler.postDelayed(mRepeatRunnable, mUpdateInterval * 2);
    }

    @Override
    public void enter(double price, int amount) {
        mViewManager.enterToMarket(price, amount);
        String message = String.format("enter from market; price %.4f %d \n%s", price, amount, mTradingBot.calculateProfit());
        TradeHistoryRepository
                .saveToDb(new TradeHistoryModel("" + System.currentTimeMillis(), message + "\n"));
        Log.i(TAG, "enter: " + message);
        App.getBus().post(new TradeEvent(new Date() + " " + message));
        mHandler.removeCallbacks(mRepeatRunnable);
        mHandler.postDelayed(mRepeatRunnable, mUpdateInterval * 2);
    }

    public class RepeatRunnable implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "RepeatRunnable run() called with: " + "");
            mViewManager.loadData24hUrl();
            Snackbar.make(getView(), R.string.updating_data, Snackbar.LENGTH_LONG).show();
        }
    }

}
