package de.heoegbr.diabeatit.assistant.prediction.python;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import de.heoegbr.diabeatit.data.container.event.DiaryEvent;
import de.heoegbr.diabeatit.data.container.event.PredictionEvent;
import de.heoegbr.diabeatit.data.repository.DiaryRepository;
import de.heoegbr.diabeatit.python.proxy.PythonPredictionProxy;

public class PythonPredictionWorker extends Worker {
    public static final String WORK_NAME = "pythonPrediction";
    private static final String TAG = "PYHTON_PREDICITON_WORKER";
    private final Context mContext;

    public PythonPredictionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.mContext = context;
    }

    public static void init(Context context) {
        // Python environment initialization
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(context));
            Log.d(TAG, "Python environment Initialized");
        }

        // Init cache path
        PythonInputContainer.dataFrameExportPath = context.getFilesDir().getAbsolutePath() + "/dataframe.csv";
    }

    @NonNull
    @Override
    public Result doWork() {
        // get input data
        Log.d(TAG, "Query input data");
        DiaryRepository repo = DiaryRepository.getRepository(mContext);
        List<DiaryEvent> diaryEvents = repo.getPredictionEventsInSameThread(Instant.now()
                .minus(4, ChronoUnit.HOURS));
        PythonInputContainer inputData = new PythonInputContainer(
                Instant.now().getEpochSecond(),
                diaryEvents.toArray(new DiaryEvent[]{}));


        // perform basic filtering
        int bgCounter = 0;
        Instant newestBgTimestamp = Instant.now().minus(2, ChronoUnit.DAYS);
        for (DiaryEvent item : diaryEvents) {
            if (item.type == DiaryEvent.TYPE_BG) {
                // count bg events
                bgCounter++;
                // track newest bg timestamp
                if (newestBgTimestamp.isBefore(item.timestamp))
                    newestBgTimestamp = item.timestamp;
            }
        }

        // check if enough bg values and newest value is not older than one our
        if (bgCounter > 20 && newestBgTimestamp.isAfter(
                Instant.now().minus(1, ChronoUnit.HOURS))) {
            Log.d(TAG, "Try prediction...");
            // perform the prediction
            PythonPredictionProxy python = new PythonPredictionProxy();
            PythonOutputContainer result = python.predict(inputData);

            Log.d(TAG, "... finished.");

            // save results
            if (result.events.length > 0) {
                for (PredictionEvent predItem : result.events) {
                    repo.insertEventIfNotExist(predItem);
                }
            }

            return Result.success();

        } else {
            Log.d(TAG, "Input data did not meet quality requirements");
        }
        return Result.failure();
    }
}
