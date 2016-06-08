package com.github.fesswood.cmshptradebot.presentation.router;

import android.content.Context;
import android.content.Intent;

import com.github.fesswood.cmshptradebot.presentation.charts.TabbedActivity;
import com.github.fesswood.cmshptradebot.presentation.main.WebVIewActivity;

/**
 * Created by fesswood on 02.06.16.
 */
public class Router {

    public static void routeToWebViewScreen(Context context){
        context.startActivity(new Intent(context, WebVIewActivity.class));
    }

    public static void routeToTabbedActivity(Context context) {
        context.startActivity(new Intent(context, TabbedActivity.class));
    }
}
