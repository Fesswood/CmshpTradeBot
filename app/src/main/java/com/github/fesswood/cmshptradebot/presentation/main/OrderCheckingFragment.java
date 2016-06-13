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

import com.github.fesswood.cmshptradebot.OrderDataExtractor;
import com.github.fesswood.cmshptradebot.R;
import com.github.fesswood.cmshptradebot.data.api.RestApiImplWorker;
import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticModel;
import com.github.fesswood.cmshptradebot.data.db.order.OrderModel;
import com.github.fesswood.cmshptradebot.data.event.LoginSuccessEvent;
import com.github.fesswood.cmshptradebot.presentation.main.common.WebViewManager;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mOrderDataExtractor = new OrderDataExtractor();
        mHandler = new Handler();
        mRepeatRunnable = new RepeatRunnable();
        mUpdateInterval = 29 * 1000;
        mViewManager.setLoadPageWithDataListener(this::extractData);
        // mViewManager.startWithBasicUrl();
        // mOrderDataExtractor.setFinishedListener(this::checkOrders);
    }

    @Subscribe
    public void onLoginSuccessEvent(LoginSuccessEvent event) {
        RestApiImplWorker.getInstance().enqueueCmshpBody(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: " + response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }


    private void checkOrders(List<OrderModel> orders) {
        checkSupportLevel(orders.get(0));
        checkResistanceLevel(orders.get(2));
        mHandler.postDelayed(mRepeatRunnable, mUpdateInterval);
    }

    private void checkResistanceLevel(OrderModel orderModel) {

    }

    private void checkSupportLevel(OrderModel orderModel) {

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
