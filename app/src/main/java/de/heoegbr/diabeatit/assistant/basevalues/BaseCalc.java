package de.heoegbr.diabeatit.assistant.basevalues;

import android.util.Log;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.heoegbr.diabeatit.data.container.event.DiaryEvent;
import de.heoegbr.diabeatit.data.repository.CalculationsRepository;

public abstract class BaseCalc {
    protected final String TAG;

    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final CalculationsRepository repo;

    protected BaseCalc(String tag, CalculationsRepository repo) {
        this.TAG = tag;
        this.repo = repo;
    }

    public abstract List<Result> runCalculation(DataContainer data);

    public abstract boolean checkData(DataContainer data);

    public void runInOwnThreadAndPushToDb(DataContainer data) {
        if (checkData(data))
            mExecutor.execute(() -> {
                List<Result> calcResult = runCalculation(data);
                if (calcResult != null)
                    repo.pushCalculationResultInYourThread(calcResult);
            });
        else
            Log.e(TAG,"Malformed input data");
    }

    public static class DataContainer {
        public List<DiaryEvent> events;
    }

    public class Result {
        public Instant timestamp;
        public int Type;
        public double result;
    }
}
