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
import androidx.lifecycle.MediatorLiveData;
import androidx.work.Data;

import net.danlew.android.joda.JodaTimeAndroid;

import java.time.Instant;

import de.heoegbr.diabeatit.assistant.prediction.SchedulePredictionHelper;
import de.heoegbr.diabeatit.assistant.prediction.python.PythonPredictionWorker;
import de.heoegbr.diabeatit.data.repository.DiaryRepository;
import de.heoegbr.diabeatit.data.source.cloud.ScheduleSyncHelper;
import de.heoegbr.diabeatit.data.source.cloud.nightscout.NightscoutDownloader;
import de.heoegbr.diabeatit.data.source.xdrip.XdripBgSource;
import de.heoegbr.diabeatit.service.DontDieForegroundService;
import de.heoegbr.diabeatit.ui.setup.SetupActivity;


public class DiaBEATitApp extends Application {
    public static final String DEFAULT_NOTIFICAITON_CHANNEL_ID = "de.heoegbr.diabeatit.notifications";
    private static final String TAG = "MAINAPP";

    private static final int PREDICTION_COOLDOWN_SECONDS = 120; // 2min
    private Instant mLastPrediction = Instant.now().minusSeconds(PREDICTION_COOLDOWN_SECONDS);
    private MediatorLiveData mDataChangeTrigger;

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        JodaTimeAndroid.init(context);

        if (!PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(SetupActivity.SETUP_DEMO_MODE_KEY, false)) {
            // don't start this in demo mode
            Log.e("dd", "NO DEMO MODE");
            registerBroadcastReceivers(context);
            scheduleEnabledBackgroundSync(context);
        }

        scheduleBackgroundPredicitons(context);

        // start "don't die" service
        createNotificationChannel();
        Intent serviceIntent = new Intent(context, DontDieForegroundService.class);
        ContextCompat.startForegroundService(context, serviceIntent);
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

    private void scheduleBackgroundPredicitons(Context context) {
        PythonPredictionWorker.init(context);

        mDataChangeTrigger = DiaryRepository.getRepository(context).getDataTriggerForPredictions();
        mDataChangeTrigger.observeForever(diaryEvents -> {
            // check if cooldown is over
            if (Instant.now().minusSeconds(PREDICTION_COOLDOWN_SECONDS).isAfter(mLastPrediction)) {
                // schedule prediction
                SchedulePredictionHelper.scheduleOneTimePrediction(context,
                        PythonPredictionWorker.class,
                        PythonPredictionWorker.WORK_NAME,
                        null);

                mLastPrediction = Instant.now();
            }
        });
    }

}
