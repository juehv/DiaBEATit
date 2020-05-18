package de.heoegbr.diabeatit.db.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.heoegbr.diabeatit.db.DiabeatitDatabase;
import de.heoegbr.diabeatit.db.container.event.BgReadingEvent;
import de.heoegbr.diabeatit.db.container.event.BolusEvent;
import de.heoegbr.diabeatit.db.container.event.CarbEvent;
import de.heoegbr.diabeatit.db.container.event.DiaryEvent;
import de.heoegbr.diabeatit.db.container.event.NoteEvent;
import de.heoegbr.diabeatit.db.container.event.SportsEvent;
import de.heoegbr.diabeatit.db.dao.BgReadingDao;
import de.heoegbr.diabeatit.db.dao.BolusEventDao;
import de.heoegbr.diabeatit.db.dao.CarbsEventDao;
import de.heoegbr.diabeatit.db.dao.NoteEventDao;
import de.heoegbr.diabeatit.db.dao.SportsEventDao;

/**
 * Manages {@link DiaryEvent}s. Provides an interface to listen for changes as well as keeps the
 * events in the Database up to date.
 */
public class DiaryEventStore {
    public static final String TAG = "DIARY_STORE";
    public static DiaryEventStore INSTANCE = null;
    private final BgReadingDao mBgReadingEventDao;
    private final BolusEventDao mBolusEventDao;
    private final CarbsEventDao mCarbsEventDao;
    private final SportsEventDao mSportsEventDao;
    private final NoteEventDao mNoteEventDao;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private List<DiaryEvent> events = new ArrayList<>();
    private LiveData<List<BgReadingEvent>> mAllBgReadings;
    private BgReadingEvent mMostRecentValue = null;

    private DiaryEventStore(final Context context) {
        DiabeatitDatabase db = DiabeatitDatabase.getDatabase(context);
        mBgReadingEventDao = db.bgReadingDao();
        mBolusEventDao = db.bolusEventDao();
        mCarbsEventDao = db.carbsEventDao();
        mSportsEventDao = db.sportsEventDao();
        mNoteEventDao = db.noteEventDao();

        mAllBgReadings = mBgReadingEventDao.getLiveReadings();
        mAllBgReadings.observeForever(bgReadingEvents -> {
//            Log.d(TAG, "Live Data: " + bgReadingEvents.get(0).value + ", " + bgReadingEvents.get(bgReadingEvents.size() - 1).value);
            mMostRecentValue = bgReadingEvents.get(0);
        });

        // Initialization: Load the events from the database
        // TODO change to live data
        mExecutor.execute(() -> {
            events.addAll(mBolusEventDao.getLimited());
            events.addAll(mCarbsEventDao.getLimited());
            events.addAll(mSportsEventDao.getLimited());
            events.addAll(mNoteEventDao.getLimited());

            events.sort((a, b) -> b.timestamp.compareTo(a.timestamp));
        });
    }

    public static DiaryEventStore getRepository(final Context context) {
        if (INSTANCE == null) {
            synchronized (AlertStore.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DiaryEventStore(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Add a new {@link DiaryEvent}. This also inserts it into the database
     *
     * @param event Event to add
     */
    public void insertEvent(DiaryEvent event) {
        events.add(event);
        events.sort((a, b) -> b.timestamp.compareTo(a.timestamp));

        mExecutor.execute(() -> {
            switch (event.type) {
                case DiaryEvent.TYPE_BG:
                    mBgReadingEventDao.insert((BgReadingEvent) event);
                    break;
                case DiaryEvent.TYPE_BOLUS:
                    mBolusEventDao.insertAll((BolusEvent) event);
                    break;
                case DiaryEvent.TYPE_CARB:
                    mCarbsEventDao.insertAll((CarbEvent) event);
                    break;
                case DiaryEvent.TYPE_SPORT:
                    mSportsEventDao.insertAll((SportsEvent) event);
                    break;
                case DiaryEvent.TYPE_NOTE:
                    mNoteEventDao.insertAll((NoteEvent) event);
                    break;
                default:
                    Log.e(TAG, "Can't save event with type: " + event.type);
            }
        });
    }

    public void insertBgIfNotExist(BgReadingEvent event) {
        if (event == null) return;

        mExecutor.execute(() -> {
            Duration widowHalf = Duration.of(1, ChronoUnit.MINUTES);
            Instant from = event.timestamp.minus(widowHalf);
            Instant to = event.timestamp.plus(widowHalf);

            List<BgReadingEvent> readings = mBgReadingEventDao.getBgInDateTimeRange(from, to);
            if (readings.isEmpty()) {
                mBgReadingEventDao.insert(event);
                Log.d(TAG, "Added missing backfill reading to DB " + event.value);
            } else {
                Log.d(TAG, "Found BG in time window. No backfill value added. " + readings.size());
                Log.d(TAG, "ref: " + event.value + ", found: " + readings.get(0).value);
            }
        });
    }

    /**
     * Remove an event. This also removes it from the database
     *
     * @param event Event to remove
     */
    public void removeEvent(DiaryEvent event) {
        events.remove(event);

        mExecutor.execute(() -> {
            switch (event.type) {
                case DiaryEvent.TYPE_BG:
                    mBgReadingEventDao.delete((BgReadingEvent) event);
                    break;
                case DiaryEvent.TYPE_BOLUS:
                    mBolusEventDao.delete((BolusEvent) event);
                    break;
                case DiaryEvent.TYPE_CARB:
                    mCarbsEventDao.delete((CarbEvent) event);
                    break;
                case DiaryEvent.TYPE_SPORT:
                    mSportsEventDao.delete((SportsEvent) event);
                    break;
                case DiaryEvent.TYPE_NOTE:
                    mNoteEventDao.delete((NoteEvent) event);
                    break;
                default:
                    Log.e(TAG, "Can't save event with type: " + event.type);
            }
        });
    }

    /**
     * Get a list of all stored events. This might not be an exhaustive list, since there is a limit
     * on how many events get loaded from the database on start up.
     *
     * @return A list containing all stored {@link DiaryEvent}s
     */
    // TODO change this to loading from db (as it is inconsistent anyway).
    public List<DiaryEvent> getEvents() {
        return new ArrayList<>(events);
    }

    public LiveData<List<BgReadingEvent>> getLiveBgEvents() {
        return mAllBgReadings;
    }

    public BgReadingEvent getMostRecentBgEvent() {
        return mMostRecentValue;
    }

}