package de.heoegbr.diabeatit;

import android.app.Application;
import android.content.Context;
import android.util.Log;


public class DiaBEATitApp extends Application {
    private static String TAG = "MAINAPP";
    public static DiaBEATitApp INSTANCE = null;

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
    }
}
