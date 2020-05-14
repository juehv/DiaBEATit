package de.heoegbr.diabeatit;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;


public class DiaBEATitApp extends Application {
    public static final String DEFAULT_NOTIFICAITON_CHANNEL_ID = "de.heoegbr.diabeatit.notifications";
    public static DiaBEATitApp INSTANCE = null;
    private static final String TAG = "MAINAPP";

    @Deprecated
    public static Context getContext() {
        if (INSTANCE != null)
            return INSTANCE.getApplicationContext();
        else
            return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "OnCreate");
        INSTANCE = this;

        createNotificationChannel();
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
}
