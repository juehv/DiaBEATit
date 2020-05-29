package de.heoegbr.diabeatit.assistant.prediction;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.jetbrains.annotations.NotNull;

public class SchedulePredictionHelper {
    private static final String TAG = "PREDICTION_SCHEDULER";

    public static void scheduleOneTimePrediction(
            @NonNull @NotNull Context context,
            @NonNull @NotNull Class<? extends androidx.work.ListenableWorker> workerClass,
            String uniqueWorkName,
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
        WorkManager.getInstance(context).enqueueUniqueWork(uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest);
        Log.d(TAG, "Prediction scheduled: " + uniqueWorkName);
    }
}
