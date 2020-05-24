package de.heoegbr.diabeatit.data.source.xdrip;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.util.List;

import de.heoegbr.diabeatit.data.source.cloud.nightscout.NsBgEntry;
import de.heoegbr.diabeatit.data.container.event.DiaryEvent;
import de.heoegbr.diabeatit.data.repository.DiaryRepository;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class XdripHttpBackfillWorker extends Worker {
    public static final String TAG = "BACKFILL_WORKER";
    private final Context mContext;

    public XdripHttpBackfillWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    public static Operation scheduleHttpBackfill(Context context) {
        OneTimeWorkRequest oneTimeWorkRequest =
                new OneTimeWorkRequest.Builder(XdripHttpBackfillWorker.class).build();
        Operation returnValue = WorkManager.getInstance(context).enqueue(oneTimeWorkRequest);
        Log.d(TAG, "HTTP backfill scheduled");
        return returnValue;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://127.0.0.1:17580/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            XdripWebService mXdripRestClient = retrofit.create(XdripWebService.class);
            Call<List<NsBgEntry>> bgReadings = mXdripRestClient.requestBg();
            Response<List<NsBgEntry>> execute = bgReadings.execute();
            if (execute.isSuccessful()) {
                Log.d(TAG, "HTTP request was successful");
                List<NsBgEntry> data = execute.body();
                for (NsBgEntry item : data) {
                    DiaryRepository.getRepository(mContext)
                            .insertBgIfNotExist(item.toBgReadingEvent(DiaryEvent.SOURCE_DEVICE));
                }
            }
        } catch (IOException ignored) {
        }
        return Result.success();
    }

    public interface XdripWebService {
        @GET("sgv.json")
        Call<List<NsBgEntry>> requestBg();
    }
}