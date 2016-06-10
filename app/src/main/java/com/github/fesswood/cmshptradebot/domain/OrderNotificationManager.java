package com.github.fesswood.cmshptradebot.domain;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.support.v7.app.NotificationCompat;

import com.github.fesswood.cmshptradebot.R;
import com.github.fesswood.cmshptradebot.app.App;

/**
 * Created by fesswood on 09.06.16.
 */
public class OrderNotificationManager {


    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private int notificationId = 1;
    private static OrderNotificationManager sInstance;

    private OrderNotificationManager() {
        mNotificationManager = (NotificationManager) App.getGlobalContext().getSystemService(Service.NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(App.getGlobalContext());
    }


    public static OrderNotificationManager getInstance() {
        if (sInstance == null) {
            sInstance = new OrderNotificationManager();
        }
        return sInstance;
    }

    public void notifySupportLevelBroken(Double level, double price) {
        Notification notification = mNotificationBuilder
                .setSmallIcon(R.drawable.ic_action_bargraph)
                .setAutoCancel(false)
                .setTicker(App.getGlobalContext().getString(R.string.support_level_broken, level, price))
                .setContentTitle(App.getGlobalContext().getString(R.string.support_level_broken_title))
                .setContentText(App.getGlobalContext().getString(R.string.support_level_broken, level, price))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        mNotificationManager.notify(notificationId, notification);
        notificationId++;
    }
}
