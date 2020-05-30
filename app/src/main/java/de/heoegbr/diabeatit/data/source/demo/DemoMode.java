package de.heoegbr.diabeatit.data.source.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.work.Data;

import java.util.concurrent.TimeUnit;

import de.heoegbr.diabeatit.data.localdb.DiabeatitDatabase;
import de.heoegbr.diabeatit.data.source.cloud.ScheduleSyncHelper;
import de.heoegbr.diabeatit.data.source.cloud.nightscout.NightscoutDownloader;

public class DemoMode {
    private static final String WORK_NAME = NightscoutDownloader.WORK_NAME + "_DEMO";

    public static void setConfigurationToDemoMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // clear preferences
        prefs.edit().clear().apply();
        // clear database
        DiabeatitDatabase.getDatabase(context).clearAllTables();

        // setup preferences to demo mode
        prefs.edit().putString("sync_ns_url", "https://diabeatit.ns.10be.de/").apply();

        // schedule background syncs
        Data.Builder data = new Data.Builder();
        data.putInt(NightscoutDownloader.KEY_NO_OF_VALUE, 12);
        ScheduleSyncHelper.schedulePeriodicCustomSync(context,
                NightscoutDownloader.class,
                WORK_NAME,
                data.build(),
                15, TimeUnit.MINUTES);
    }

    public static void clearConfiguration(Context context) {
        // stop background sync
        ScheduleSyncHelper.stopPeriodicSync(context, WORK_NAME);
        // clear preferences to disable demo mode
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
        // clear database from fake data
        DiabeatitDatabase.getDatabase(context).clearAllTables();
    }
}
