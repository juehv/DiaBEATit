package de.heoegbr.diabeatit.data.source.cloud.nightscout;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.heoegbr.diabeatit.BuildConfig;
import de.heoegbr.diabeatit.data.container.event.BasalEvent;
import de.heoegbr.diabeatit.data.container.event.BolusEvent;
import de.heoegbr.diabeatit.data.container.event.DiaryEvent;
import de.heoegbr.diabeatit.data.container.event.MealEvent;
import de.heoegbr.diabeatit.data.container.event.NoteEvent;
import de.heoegbr.diabeatit.data.repository.DiaryRepository;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public class NightscoutDownloader extends Worker {

    public static final String WORK_NAME = "NS_SYNC_WORKER";
    public static final String KEY_NO_OF_VALUE = "noOfValues";
    public static final String KEY_SYNC_NS_URL = "sync_ns_url";
    private static final String TAG = "NS-CLIENT";
    private final Context mContext;
    private final Nightscout mService;
    private final String mHashedSecret;
    private int mNoOfValues;

    public NightscoutDownloader(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
        this.mContext = context;
        this.mNoOfValues = getInputData().getInt(KEY_NO_OF_VALUE, 24);

        Retrofit retrofit = initRetrofit();
        if (retrofit != null) {
            mService = Objects.requireNonNull(initRetrofit()).create(Nightscout.class);
        } else {
            mService = null;
        }

        // set password
        String apiSecret = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("sync_ns_pw", null);
        //noinspection deprecation (NS still uses old hash .. but without salt it's useless anyway)
        mHashedSecret = apiSecret != null ?
                Hashing.sha1().hashBytes(apiSecret.getBytes(Charsets.UTF_8)).toString()
                : null;
    }

    private Retrofit initRetrofit() throws IllegalArgumentException {
        // Check URL and try autofix if not correct
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String url = prefs.getString(KEY_SYNC_NS_URL, "");
        if (url == null || url.isEmpty() || !url.contains("http")) {
            Log.d(TAG, "Empty url - cannot create instance");
            return null;
        }
        url = url.trim();
        if (!url.contains("/api/")) {
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += "api/v1/";
            prefs.edit().putString("sync_ns_url", url).apply();
        } else {
            if (!url.endsWith("/")) {
                url += "/";
                prefs.edit().putString("sync_ns_url", url).apply();
            }
        }

        // create retrofit client
        final Retrofit retrofit;
        final OkHttpClient client = (new OkHttpClient.Builder())
                .addInterceptor(new GzipRequestInterceptor())
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

    @NonNull
    @Override
    public Result doWork() {
        // TODO add proper impementation of an request loop (and make noOfvalues final agin)
        if (mNoOfValues > 500) mNoOfValues = 500;
        // check if initialization was successful
        if (mService == null) return Result.failure();

        // try requests
        DateTime requestTime = DateTime.now().minus(mNoOfValues * 300000);
        Log.e(TAG, "EPOCH " + requestTime.getMillis());
        String dateString = ISODateTimeFormat.dateTimeNoMillis().print(requestTime);
        Log.d(TAG, "Connecting to Nightscout to request from " + dateString);
        try {
            // Request BG
            Response<List<NsBgEntry>> execute = mService.getEntries(mHashedSecret, mNoOfValues)
                    .execute();
            if (execute.isSuccessful()) {
                Log.d(TAG, "HTTP request was successful");
                List<NsBgEntry> data = execute.body();

                for (NsBgEntry item : data) {
                    DiaryRepository.getRepository(mContext)
                            .insertBgIfNotExist(item.toBgReadingEvent(DiaryEvent.SOURCE_CLOUD));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in ns client entries work() " + e.getMessage());
            return Result.retry();
        }

        try {
            // Request treatments
            Response<ResponseBody> execute = mService.getTreatments(mHashedSecret, dateString)
                    .execute();
            if (execute.isSuccessful()) {
                List<DiaryEvent> treatments = processTreatmentResponse(execute.body().string());
                for (DiaryEvent item : treatments) {
                    DiaryRepository.getRepository(mContext)
                            .insertEventIfNotExist(item);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in ns client treatments work() " + e.getMessage());
            return Result.retry();
        }
        return Result.success();
    }

    // copied from xDrip github.com/NightscoutFoundation/xDrip/
    private List<DiaryEvent> processTreatmentResponse(final String response) throws Exception {
        List<DiaryEvent> diaryEvents = new ArrayList<>();
        List<BasalEvent> tmpBasalEvents = new ArrayList<>();
        final JSONArray jsonArray = new JSONArray(response);

        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject tr = (JSONObject) jsonArray.get(i);

            final String eventType = tr.has("eventType") ? tr.getString("eventType") : "<null>";
//            Log.d(TAG, "Found event type:" + eventType);

            // extract treatment data if present
            double carbs = 0;
            double insulin = 0;
            double basalRate = -1;
            String notes = null;

            try {
                final Instant timestamp = Instant.ofEpochMilli(ISODateTimeFormat.dateTimeParser()
                        .parseDateTime(tr.getString("created_at")).getMillis());
                if (timestamp != null) {

                    try {
                        notes = tr.getString("notes");
                    } catch (JSONException e) {
                        // Log.d(TAG, "json processing: " + e);
                    }
                    if ((notes != null) && ((notes.startsWith("AndroidAPS started")
                            || notes.equals("null")
                            || (notes.equals("Bolus Std")))))
                        notes = null;
                    notes = notes == null ? "" : notes;

                    try {
                        carbs = tr.getDouble("carbs");
                        diaryEvents.add(new MealEvent(DiaryEvent.SOURCE_CLOUD, timestamp,
                                null, carbs, notes));
//                        Log.d(TAG, "Created carb event");
                    } catch (JSONException e) {
                        //  Log.d(TAG, "json processing: " + e);
                    }

                    try {
                        insulin = tr.getDouble("insulin");
                        diaryEvents.add(new BolusEvent(DiaryEvent.SOURCE_CLOUD, timestamp,
                                insulin, notes));
//                        Log.d(TAG, "Created bolus event");
                    } catch (JSONException e) {
                        // Log.d(TAG, "json processing: " + e);
                    }

                    try {
                        basalRate = tr.getDouble("absolute");
                        double duration = tr.getDouble("duration");
                        tmpBasalEvents.add(new BasalEvent(DiaryEvent.SOURCE_CLOUD, timestamp,
                                basalRate, duration, notes));
                        Log.d(TAG, "Created basal event");
                    } catch (JSONException e) {
//                         Log.d(TAG, "json processing: " + e);
                    }

                    if (!(carbs > 0 || insulin > 0 || basalRate >= 0) && !notes.isEmpty()) {
                        diaryEvents.add(new NoteEvent(DiaryEvent.SOURCE_CLOUD, timestamp,
                                null, notes));
//                        Log.d(TAG, "Created note event");
                    }

                    // for debugging (find unsupported entries)
                    if (basalRate < 0 && eventType.equalsIgnoreCase("Temp Basal")) {
                        Log.e(TAG, "PRINT UNSUPPORTED TMP BASAL:" + tr.toString());
                    } else if (!(carbs > 0 || insulin > 0 || basalRate >= 0) && notes.isEmpty()) {
                        Log.e(TAG, "PRINT UNKNOWN ENTRY:" + tr.toString());
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Failed to process entry:  " + e);
            }
        }

        // prepare tmp basal events
        BasalEvent lastItem = null;
        for (BasalEvent item : tmpBasalEvents) {
            if (lastItem != null) { // == 0 for double
                // ns saves the *planned* duration at creation, overlapping entries cancel the one before
                // we want the real duration
                if (lastItem.timestamp.plus(Duration.ofMinutes((long) lastItem.duration)).isAfter(item.timestamp)) {
                    // current item is started before last item finished
                    Log.d(TAG, "Found overlapping entry. Create new tmp basal item.");
                    double newDuration = (double) Math.round(Duration.between(lastItem.timestamp,
                            item.timestamp).abs().getSeconds() / 60.0);
                    diaryEvents.add(new BasalEvent(DiaryEvent.SOURCE_CLOUD, lastItem.timestamp,
                            lastItem.value, newDuration, lastItem.note));
                } else {
                    // last item finished without overlap
                    Log.d(TAG, "Found NO overlapping entry. Add basal item unchanged.");
                    diaryEvents.add(lastItem);
                }
                if (item.duration < 0.01) {
                    // item with 0 duration should just cancel the tmp basal before ...
                    lastItem = null;
                    //TODO this is untested as I don't have testdata at the moment
                } else {
                    lastItem = item;
                }
            } else {
                lastItem = item;
            }
        }
        if (lastItem != null) {
            diaryEvents.add(lastItem);
        }
        diaryEvents.sort((a, b) -> b.timestamp.compareTo(a.timestamp));

        return diaryEvents;
    }

    public interface Nightscout {
        @Headers({
                "User-Agent: xDrip+ " + BuildConfig.VERSION_NAME,
        })

        @GET("/api/v1/entries.json")
        Call<List<NsBgEntry>> getEntries(@Header("api-secret") String secret, @Query("count") int count);

        @GET("/api/v1/treatments")
        Call<ResponseBody> getTreatments(@Header("api-secret") String secret, @Query("find[timestamp][$gte]") String dateString);
    }


}