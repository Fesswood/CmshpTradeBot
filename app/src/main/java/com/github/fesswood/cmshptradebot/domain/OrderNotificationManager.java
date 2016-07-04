package com.github.fesswood.cmshptradebot.domain;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import com.github.fesswood.cmshptradebot.R;
import com.github.fesswood.cmshptradebot.app.App;

/**
 * Created by fesswood on 09.06.16.
 */
public class OrderNotificationManager {


    private final Uri mSoundUri;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private int notificationId = 1;
    private static OrderNotificationManager sInstance;

    private OrderNotificationManager() {
        mNotificationManager = (NotificationManager) App.getGlobalContext().getSystemService(Service.NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(App.getGlobalContext());
        mSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
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
                .setSound(mSoundUri)
                .build();
        notification.defaults = Notification.DEFAULT_VIBRATE;
        mNotificationManager.notify(notificationId, notification);
    }


    public void notifyResistanceLevelBroken(Double level, double price) {
        Notification notification = mNotificationBuilder
                .setSmallIcon(R.drawable.ic_broken_image)
                .setAutoCancel(false)
                .setTicker(App.getGlobalContext().getString(R.string.resist_level_broken, level, price))
                .setContentTitle(App.getGlobalContext().getString(R.string.resist_level_broken_title))
                .setContentText(App.getGlobalContext().getString(R.string.resist_level_broken, level, price))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(mSoundUri)
                .build();
        notification.defaults = Notification.DEFAULT_VIBRATE;
        Ringtone r = RingtoneManager.getRingtone(App.getGlobalContext(), mSoundUri);
        r.play();
        mNotificationManager.notify(notificationId, notification);
    }
}
