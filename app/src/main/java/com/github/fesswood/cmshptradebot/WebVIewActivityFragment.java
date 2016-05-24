package com.github.fesswood.cmshptradebot;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * A placeholder fragment containing a simple view.
 */
public class WebVIewActivityFragment extends Fragment {

    private static final String TAG = WebVIewActivityFragment.class.getSimpleName();
    public static final String BASE_URL = "https://dolevik.com/CMSHP_trade__project";
    public static final String LOGIN_URL = "https://dolevik.com/signin";
    public static final String IFRAME_URL = "https://dolevik.com/charts/get.php?pcode=CMSHP&tline=24ch";
    public static final int LOGIN_STATE = 1;
    private static final int IFRAME_STATE = 2;
    private WebView wvSite;
    private Handler mHandler;
    private boolean mIsFirstLaunch = true;
    private RepeatRunnable mRepeatRunnable;
    private int mUpdateInterval;
    DataExtracter mDataExtracter = new DataExtracter();
    // состояние web view
    private int mWvState;

    public WebVIewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);
        wvSite = (WebView) view.findViewById(R.id.wvSite);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WebSettings webViewSettings = wvSite.getSettings();
        webViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setBuiltInZoomControls(true);
        loadUrl();
        wvSite.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url,
                                      Bitmap favicon) {
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                if (mDataExtracter.isLoginNeeded(url) && mWvState != LOGIN_STATE) {
                    loadLoginPage();
                    mWvState = LOGIN_STATE;
                } else if (mWvState == IFRAME_STATE && IFRAME_URL.equals(url)) {
                    view.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                } else if (!mDataExtracter.isLoginNeeded(url) && !IFRAME_URL.equals(url)) {
                    loadIframe();
                    mWvState = IFRAME_STATE;
                }
            }
        });
        wvSite.setWebChromeClient(new WebChromeClient() {
        });
        wvSite.addJavascriptInterface(new LoadListener(), "HTMLOUT");
        mHandler = new Handler();
        mRepeatRunnable = new RepeatRunnable();
        mUpdateInterval = 29 * 1000 * 1000;
    }

    private void loadIframe() {
        wvSite.loadUrl(IFRAME_URL);
    }

    @Override
    public void onResume() {
        super.onResume();
        //  mHandler.postDelayed(mRepeatRunnable, mUpdateInterval);
    }

    @Override
    public void onPause() {
        super.onPause();
        // mHandler.removeCallbacks(mRepeatRunnable);
    }

    private void loadUrl() {
        wvSite.loadUrl(BASE_URL);
    }


    private void loadLoginPage() {
        wvSite.loadUrl(LOGIN_URL);
    }

    private void reload() {
        wvSite.reload();
    }

    public class RepeatRunnable implements Runnable {

        @Override
        public void run() {
            Log.d(TAG, "run() called with: " + "");
            if (!mIsFirstLaunch) {
                reload();
                Snackbar.make(getView(), R.string.updating_data, Snackbar.LENGTH_LONG).show();
            }
            mIsFirstLaunch = false;
            mHandler.postDelayed(mRepeatRunnable, mUpdateInterval);
        }
    }

    public class LoadListener {
        @JavascriptInterface
        public void processHTML(String html) {
            if (IFRAME_STATE == mWvState) {
                extractData(html);
            }
            Log.d("result", html);

        }
    }

    private void extractData(String html) {
        mDataExtracter.init(html);
        mDataExtracter.extractDatePerHour();
    }
}
