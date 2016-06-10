package com.github.fesswood.cmshptradebot.presentation.main;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.github.fesswood.cmshptradebot.app.App;
import com.github.fesswood.cmshptradebot.data.event.TradeEvent;
import com.github.fesswood.cmshptradebot.data.event.WaitForLoginEvent;
import com.github.fesswood.cmshptradebot.domain.OrderDataExtractor;
import com.github.fesswood.cmshptradebot.R;
import com.github.fesswood.cmshptradebot.data.TradeStatistic.TradeStatisticModel;
import com.github.fesswood.cmshptradebot.data.order.OrderModel;
import com.github.fesswood.cmshptradebot.domain.OrderNotificationManager;
import com.github.fesswood.cmshptradebot.presentation.main.common.WebViewManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class OrderCheckingFragment extends Fragment {

    private static final String TAG = OrderCheckingFragment.class.getSimpleName();
    private Handler mHandler;
    private boolean mIsFirstLaunch = true;
    private RepeatRunnable mRepeatRunnable;
    private int mUpdateInterval;
    private WebViewManager mViewManager;
    OrderDataExtractor mOrderDataExtractor;
    private boolean mIsBotirstLaunch = true;
    private List<Double> mSupportLevel = new ArrayList<>();
    private List<Double> mResistanceLevel = new ArrayList<>();

    public OrderCheckingFragment() {
    }

    public static Fragment newInstance() {
        return new OrderCheckingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);
        mViewManager = new WebViewManager((WebView) view.findViewById(R.id.wvSite));
        mViewManager.enableOrderCheckMode();
        //TODO delete test data
        mSupportLevel.add(0.3);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mOrderDataExtractor = new OrderDataExtractor();
        mHandler = new Handler();
        mRepeatRunnable = new RepeatRunnable();
        mUpdateInterval = 29 * 1000;
        mViewManager.setLoadPageWithDataListener(this::extractData);
        mViewManager.startWithBasicUrl();
        mOrderDataExtractor.setFinishedListener(this::checkOrders);
    }


    private void checkOrders(List<OrderModel> orders) {
        checkSupportLevel(orders.get(0));
        checkResistanceLevel(orders.get(2));
        mHandler.postDelayed(mRepeatRunnable, mUpdateInterval);
    }

    private void checkResistanceLevel(OrderModel orderModel) {

    }

    private void checkSupportLevel(OrderModel orderModel) {
        double price = orderModel.getPrice();
        for (Double level : mSupportLevel) {
            if(level > price){
                OrderNotificationManager.getInstance().notifySupportLevelBroken(level , price);
            }
        }
    }

    @Override
    public void onResume() {
        super.onStart();
        App.getBus().register(this);
    }

    @Override
    public void onPause() {
        App.getBus().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void WaitForLoginEvent(WaitForLoginEvent event) {
        Log.d(TAG, "WaitForLoginEvent: awaiting for login");
        mHandler.postDelayed(mRepeatRunnable, mUpdateInterval);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRepeatRunnable);
    }

    private void extractData(String html) {
        mOrderDataExtractor.init(html, getContext(), TradeStatisticModel.DAY);
        mOrderDataExtractor.runAsyncTask();
    }

    public class RepeatRunnable implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "RepeatRunnable run() called with: " + "");
            mViewManager.loadBasicUrl();
            Snackbar.make(getView(), R.string.updating_data, Snackbar.LENGTH_LONG).show();
        }
    }

}
