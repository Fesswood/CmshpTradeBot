package com.github.fesswood.cmshptradebot;

import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.CookieManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import us.codecraft.xsoup.Xsoup;

/**
 * Created by fesswood on 22.05.16.
 */
public class DataExtracter {

    private static final String TAG = DataExtracter.class.getSimpleName();
    private Document mDoc;
    private Document mDocPart;
    private String Xpath = "//*[@id=\"last_orders_list_table\"]/table";
    private String XpathGetJs = "/html/head/script[2]";
    private boolean mIsInitCompleted;

    public DataExtracter() {
    }

    public void init(String html) {
        mDoc = Jsoup.parse(html);
        mIsInitCompleted = true;
    }

    public boolean isLoginNeeded(String url) {
        String cookies = CookieManager.getInstance().getCookie(url);
        Log.d(TAG, "All the cookies in a string:" + cookies);
        return !cookies.contains("dole_usr") || !cookies.contains("dole_usr_sec");
    }

    @Nullable
    public String getJsScript() {
        if (mIsInitCompleted) {
            return Xsoup.compile(XpathGetJs).evaluate(mDoc).get();
        } else {
            Log.e(TAG, "getJsScript: ");
        }
        return null;
    }

    public void extractDatePerHour() {
        String dataTable = getJsScript();
        mDocPart = Jsoup.parse(dataTable);
        int dataIndexBegin = dataTable.indexOf("data.addRows([[");
        int dataIndexFinish = dataTable.indexOf("]]);");
        String data = dataTable.substring(dataIndexBegin,dataIndexFinish);
        Log.d(TAG, "extractDatePerHour: "+data);
    }
}
