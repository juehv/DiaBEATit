package de.heoegbr.diabeatit.data.source.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.work.Data;

import java.util.concurrent.TimeUnit;

import de.heoegbr.diabeatit.data.source.cloud.ScheduleSyncHelper;
import de.heoegbr.diabeatit.data.source.cloud.nightscout.NightscoutDownloader;

public class DemoMode {
    private static final String WORK_NAME = "DEMO_MODE_WORKER";

    public static void setConfigurationToDemoModeInYourThread(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // setup demo mode server
        prefs.edit().putString(NightscoutDownloader.KEY_SYNC_NS_URL, "https://diabeatit-demo.ns.10be.de").apply();

        // schedule background syncs
        Data.Builder data = new Data.Builder();
        data.putInt(NightscoutDownloader.KEY_NO_OF_VALUE, 12);
        ScheduleSyncHelper.schedulePeriodicCustomSync(context,
                NightscoutDownloader.class,
                WORK_NAME,
                data.build(),
                15, TimeUnit.MINUTES);
    }
}
