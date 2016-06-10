package com.github.fesswood.cmshptradebot.presentation.main.common;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.fesswood.cmshptradebot.R;
import com.github.fesswood.cmshptradebot.app.App;
import com.github.fesswood.cmshptradebot.data.UserDetail.ProfileModel;
import com.github.fesswood.cmshptradebot.data.UserDetail.ProfileRepository;
import com.github.fesswood.cmshptradebot.data.event.TradeEvent;
import com.github.fesswood.cmshptradebot.data.event.WaitForLoginEvent;

/**
 * Created by fesswood on 02.06.16.
 */
public class WebViewManager extends WebViewClient {

    private static final int LOAD_BASIC_URL = 1;
    private static final int LOAD_DATA_URL = 3;
    private static final int LOAD_LOGIN_URL = 2;
    private static final int LOAD_DATA_1h_URL = 4;
    private static final String TAG = WebViewManager.class.getSimpleName();
    private static final int TRADE_STATE_ENTER = 1;
    private static final int TRADE_STATE_EXIT = 2;
    private static final int TRADE_STATE_HOLD = 0;
    private static final int MANAGER_MODE_TRADE = 0;
    private static final int MANAGER_MODE_CHECK_ORDER = 1;
    private final String[] urlArray;
    private WebView mWebView;
    private int mWebViewState;
    private LoadPageWithDataListener mLoadPageWithDataListener;
    private double mPrice;
    private int mAmount;
    private int mTradeState;
    private int mWebManagerMode;

    public WebViewManager(WebView webView) {
        this(webView, MANAGER_MODE_TRADE);
    }

    public WebViewManager(WebView webView, int webManagerMode) {
        mWebManagerMode = webManagerMode;
        mWebView = webView;
        Context context = webView.getContext();
        urlArray = context.getResources().getStringArray(R.array.urls);
    }

    public void init() {
        WebSettings webViewSettings = mWebView.getSettings();
        webViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setBuiltInZoomControls(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return true;
            }


        });
        mWebView.setWebViewClient(this);
    }

    public void startWithBasicUrl() {
        init();
        loadBasicUrl();
    }

    public void loadBasicUrl() {
        mWebViewState = LOAD_BASIC_URL;
        mWebView.loadUrl(urlArray[0]);
    }


    private void loadLoginUrl() {
        mWebViewState = LOAD_LOGIN_URL;
        mWebView.loadUrl(urlArray[1]);
    }

    public void loadData24hUrl() {
        mWebViewState = LOAD_DATA_URL;
        mWebView.loadUrl(urlArray[2]);
    }

    public void loadData1hUrl() {
        mWebViewState = LOAD_DATA_1h_URL;
        mWebView.loadUrl(urlArray[3]);
    }

    public void enterToMarket(double price, int amount) {
        mPrice = price;
        mAmount = amount;
        loadBasicUrl();
        mTradeState = TRADE_STATE_ENTER;
    }

    public void exitFromMarket(double price, int amount) {
        mPrice = price;
        mAmount = amount;
        loadBasicUrl();
        mTradeState = TRADE_STATE_EXIT;
    }

    public void reload() {
        mWebView.reload();
    }

    public boolean isLoginNeeded(String url) {
        String cookies = CookieManager.getInstance().getCookie(url);
        Log.d(TAG, "All the cookies in a string:" + cookies);
        return cookies != null && (!cookies.contains("dole_usr") || !cookies.contains("dole_usr_sec"));
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (mWebManagerMode == MANAGER_MODE_TRADE) {
            onPageFinishedTradeMode(view, url);
        } else {
            onPageFinishedOrderMode(view, url);
        }
    }

    private void onPageFinishedOrderMode(WebView view, String url) {
        if (isLoginNeeded(url) && mWebViewState != LOAD_LOGIN_URL) {
            App.getBus().post(new WaitForLoginEvent());
        } else {
            addProccessHtmlCallback(view);
        }
    }

    private void onPageFinishedTradeMode(WebView view, String url) {
        if (isLoginNeeded(url) && mWebViewState != LOAD_LOGIN_URL) {
            loadLoginUrl();
        } else if (mWebViewState == LOAD_LOGIN_URL
                && TextUtils.equals(url, "https://dolevik.com/signin")) {
            prepareLoginForm(view);
        } else if (mWebViewState >= 3) {
            addProccessHtmlCallback(view);
            mTradeState = TRADE_STATE_HOLD;
        } else if (mWebViewState == LOAD_BASIC_URL && (mTradeState > 0)) {
            switch (mTradeState) {
                case TRADE_STATE_ENTER:
                    prepareEnterForm(view);
                    break;
                case TRADE_STATE_EXIT:
                    prepareExitForm(view);
                    break;
            }
            ;
        } else {
            loadData24hUrl();
        }
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return true;
    }


    private void prepareExitForm(WebView view) {
        if (mAmount > 0 && mPrice > 0 && mPrice < 0.245) {
            view.loadUrl("javascript:$(\"#sell_count\").val('" + mAmount + "')");
            view.loadUrl(String.format("javascript:$(\"#sell_price\").val('%.4f')", mPrice));
            //   view.loadUrl("javascript:$(\"#sell_share_button\").click()");
        } else {
            App.getBus().post(new TradeEvent("ERROR: operation not confirmed!"));

        }
    }

    private void prepareEnterForm(WebView view) {
        if (mAmount > 0 && mPrice > 0 && mPrice < 0.28) {
            view.loadUrl("javascript:$(\"#buy_count\").val('" + mAmount + "')");
            view.loadUrl(String.format("javascript:$(\"#buy_price\").val('%.4f')", mPrice));
            // view.loadUrl("javascript:$(\"#buy_share_button\").click()");
        } else {
            App.getBus().post(new TradeEvent("ERROR: operation not confirmed!"));

        }
    }

    private void prepareLoginForm(WebView view) {
        ProfileModel profile = ProfileRepository.getProfile();
        String login = profile.getLogin();
        String password = profile.getPassword();
        view.loadUrl("javascript:$(\"input[name*='email']\").val('" + login + "')");
        view.loadUrl("javascript:$(\"input[name*='password']\").val('" + password + "')");
    }

    private void addProccessHtmlCallback(WebView view) {
        view.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
    }

    public void setLoadPageWithDataListener(LoadPageWithDataListener listener) {
        mWebView.addJavascriptInterface(new LoadListener(), "HTMLOUT");
        mLoadPageWithDataListener = listener;
    }

    public void enableOrderCheckMode() {
        mWebManagerMode = MANAGER_MODE_CHECK_ORDER;
    }

    public void enableTradeMode() {
        mWebManagerMode = MANAGER_MODE_TRADE;
    }

    public class LoadListener {

        @JavascriptInterface
        public void processHTML(String html) {
            if (mWebViewState >= LOAD_DATA_URL ||    mWebManagerMode == MANAGER_MODE_CHECK_ORDER) {
                mLoadPageWithDataListener.dataLoaded(html);
            }
            Log.d("result", html);
        }
    }

    public interface LoadPageWithDataListener {
        void dataLoaded(String data);
    }
}
