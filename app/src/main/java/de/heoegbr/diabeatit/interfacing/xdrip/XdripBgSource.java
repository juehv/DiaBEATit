package de.heoegbr.diabeatit.interfacing.xdrip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

import de.heoegbr.diabeatit.db.container.event.BgReadingEvent;
import de.heoegbr.diabeatit.db.container.event.DiaryEvent;
import de.heoegbr.diabeatit.db.repository.DiaryRepository;

/**
 * Interface class to receive xdrip broadcasts as BG source.
 * Supports backfill by leveraging the xdrip web server.
 */
public class XdripBgSource extends BroadcastReceiver {
    public static final String XDRIP_ACTION_NEW_ESTIMATE = "com.eveningoutpost.dexdrip.BgEstimate";
    private static final String TAG = "SOURCE_XDRIP";
    private static final String XDRIP_EXTRA_BG_ESTIMATE = "com.eveningoutpost.dexdrip.Extras.BgEstimate";
    private static final String XDRIP_EXTRA_BG_SLOPE_NAME = "com.eveningoutpost.dexdrip.Extras.BgSlopeName";
    private static final String XDRIP_EXTRA_TIMESTAMP = "com.eveningoutpost.dexdrip.Extras.Time";


    private final Calendar mTimestamp;

    public XdripBgSource() {
        mTimestamp = new Calendar.Builder()
                .setInstant(Instant.now().toEpochMilli())
                .setTimeZone(Calendar.getInstance().getTimeZone())
                .build();

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        // check if intent is for me
        if (XDRIP_ACTION_NEW_ESTIMATE.equalsIgnoreCase(intent.getAction())) {
            Log.d(TAG, "Received xDrip data");


            Date d = new Date(bundle.getLong(XDRIP_EXTRA_TIMESTAMP));
            mTimestamp.setTime(d);
            BgReadingEvent bgReadingEvent = new BgReadingEvent(DiaryEvent.SOURCE_DEVICE,
                    mTimestamp.toInstant(), bundle.getDouble(XDRIP_EXTRA_BG_ESTIMATE), "");

            DiaryRepository repo = DiaryRepository.getRepository(context);
            BgReadingEvent lastBgEvent = repo.getMostRecentBgEvent();
            repo.insertEvent(bgReadingEvent);

            if (lastBgEvent == null ||
                    (mTimestamp.toInstant().toEpochMilli() - lastBgEvent.timestamp.toEpochMilli()
                            > 360000)) {// gap > 6 min
                // try backfill via http
                XdripHttpBackfillWorker.scheduleHttpBackfill(context);
            }
        }
    }
}
