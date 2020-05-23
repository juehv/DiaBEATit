package de.heoegbr.diabeatit.db.cloud;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class ScheduleSyncHelper {
    private static final String TAG = "SYNC_SCHEDULER";

    public static void scheduleOneTimeSync(
            @NonNull @NotNull Context context,
            @NonNull @NotNull Class<? extends androidx.work.ListenableWorker> workerClass,
            Data data) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();
        OneTimeWorkRequest.Builder requestBuilder = new OneTimeWorkRequest.Builder(workerClass);
        requestBuilder.setConstraints(constraints);
        if (data != null) {
            requestBuilder.setInputData(data);
        }
        OneTimeWorkRequest oneTimeWorkRequest = requestBuilder.build();
        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest);
        Log.d(TAG, "Cloud sync scheduled");
    }

    public static void schedulePeriodicSync(
            @NonNull @NotNull Context context,
            @NonNull @NotNull Class<? extends androidx.work.ListenableWorker> workerClass,
            @NonNull @NotNull String uniqueWorkName,
            Data data) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();
        PeriodicWorkRequest.Builder requestBuilder = new PeriodicWorkRequest.Builder(workerClass,
                1, TimeUnit.HOURS);
        requestBuilder.addTag(uniqueWorkName);
        requestBuilder.setConstraints(constraints);
        if (data != null) {
            requestBuilder.setInputData(data);
        }
        PeriodicWorkRequest periodicWorkRequest = requestBuilder.build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                uniqueWorkName,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest);
        Log.d(TAG, "*Periodic* cloud sync scheduled");
    }

    public static void stopPeriodicSync(
            @NonNull @NotNull Context context,
            @NonNull @NotNull String uniqueWorkName) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName);
    }
}
