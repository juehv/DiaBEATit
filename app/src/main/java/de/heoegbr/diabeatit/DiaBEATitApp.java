package de.heoegbr.diabeatit;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.work.Data;

import net.danlew.android.joda.JodaTimeAndroid;

import de.heoegbr.diabeatit.data.source.cloud.ScheduleSyncHelper;
import de.heoegbr.diabeatit.data.source.cloud.nightscout.NightscoutDownloader;
import de.heoegbr.diabeatit.data.source.xdrip.XdripBgSource;
import de.heoegbr.diabeatit.service.DontDieForegroundService;


public class DiaBEATitApp extends Application {
    public static final String DEFAULT_NOTIFICAITON_CHANNEL_ID = "de.heoegbr.diabeatit.notifications";
    private static final String TAG = "MAINAPP";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "OnCreate");
        JodaTimeAndroid.init(getApplicationContext());

        registerBroadcastReceivers(getApplicationContext());

        createNotificationChannel();
        Intent serviceIntent = new Intent(getApplicationContext(), DontDieForegroundService.class);
        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);

        scheduleEnabledBackgroundSync(getApplicationContext());
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

    private void scheduleEnabledBackgroundSync(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // Nightscout
        if (prefs.getBoolean("sync_ns_download_en", false)) {
            Data.Builder data = new Data.Builder();
            data.putInt(NightscoutDownloader.KEY_NO_OF_VALUE, 24);
            ScheduleSyncHelper.schedulePeriodicSync(context,
                    NightscoutDownloader.class,
                    NightscoutDownloader.WORK_NAME,
                    data.build());
        }
    }

}
