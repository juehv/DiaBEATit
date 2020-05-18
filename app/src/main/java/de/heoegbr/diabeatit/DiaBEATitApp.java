package de.heoegbr.diabeatit;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import de.heoegbr.diabeatit.interfacing.xdrip.XdripBgSource;


public class DiaBEATitApp extends Application {
    public static final String DEFAULT_NOTIFICAITON_CHANNEL_ID = "de.heoegbr.diabeatit.notifications";
    private static final String TAG = "MAINAPP";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "OnCreate");

        registerBroadcastReceivers(getApplicationContext());
        createNotificationChannel();

        JodaTimeAndroid.init(getApplicationContext());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    DEFAULT_NOTIFICAITON_CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            getSystemService(NotificationManager.class).createNotificationChannel(serviceChannel);
        }
    }

    private void registerBroadcastReceivers(Context context) {
        context.registerReceiver(
                new XdripBgSource(),
                new IntentFilter(XdripBgSource.XDRIP_ACTION_NEW_ESTIMATE));
        Log.d(TAG, "Receiver registered.");
    }

}
