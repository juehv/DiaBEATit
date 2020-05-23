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

import de.heoegbr.diabeatit.db.container.event.BasalEvent;
import de.heoegbr.diabeatit.db.container.event.BgReadingEvent;
import de.heoegbr.diabeatit.db.container.event.BolusEvent;
import de.heoegbr.diabeatit.db.container.event.CarbEvent;
import de.heoegbr.diabeatit.db.container.event.DiaryEvent;
import de.heoegbr.diabeatit.db.container.event.NoteEvent;
import de.heoegbr.diabeatit.db.container.event.SportsEvent;
import de.heoegbr.diabeatit.db.dao.BasalEventDao;
import de.heoegbr.diabeatit.db.dao.BgReadingDao;
import de.heoegbr.diabeatit.db.dao.BolusEventDao;
import de.heoegbr.diabeatit.db.dao.CarbsEventDao;
import de.heoegbr.diabeatit.db.dao.NoteEventDao;
import de.heoegbr.diabeatit.db.dao.SportsEventDao;
import de.heoegbr.diabeatit.db.localdb.DiabeatitDatabase;

/**
 * Manages {@link DiaryEvent}s. Provides an interface to listen for changes as well as keeps the
 * events in the Database up to date.
 */
public class DiaryRepository {
    public static final String TAG = "DIARY_STORE";
    public static DiaryRepository INSTANCE = null;
    private final BgReadingDao mBgReadingEventDao;
    private final BolusEventDao mBolusEventDao;
    private final BasalEventDao mBasalEventDao;
    private final CarbsEventDao mCarbsEventDao;
    private final SportsEventDao mSportsEventDao;
    private final NoteEventDao mNoteEventDao;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private LiveData<List<BgReadingEvent>> mBgReadings;
    private List<BgReadingEvent> mBgReadingsStatic;
    private BgReadingEvent mMostRecentValue = null;
    private LiveData<List<BolusEvent>> mBolusEvents;
    private List<BolusEvent> mBolusEventsStatic;
    private LiveData<List<BasalEvent>> mBasalEvents;
    private List<BasalEvent> mBasalEventsStatic;
    private LiveData<List<CarbEvent>> mCarbEvents;
    private List<CarbEvent> mCarbEventsStatic;
    private LiveData<List<SportsEvent>> mSportsEvents;
    private List<SportsEvent> mSportsEventsStatic;
    private LiveData<List<NoteEvent>> mNoteEvents;
    private List<NoteEvent> mNoteEventsStatic;

    private DiaryRepository(final Context context) {
        DiabeatitDatabase db = DiabeatitDatabase.getDatabase(context);
        mBgReadingEventDao = db.bgReadingDao();
        mBolusEventDao = db.bolusEventDao();
        mBasalEventDao = db.basalEventDao();
        mCarbsEventDao = db.carbsEventDao();
        mSportsEventDao = db.sportsEventDao();
        mNoteEventDao = db.noteEventDao();

        mBgReadings = mBgReadingEventDao.getLiveReadings();
        mBgReadings.observeForever(bgReadingEvents -> {
//            Log.d(TAG, "Live Data: " + bgReadingEvents.get(0).value + ", " + bgReadingEvents.get(bgReadingEvents.size() - 1).value);
            mBgReadingsStatic = bgReadingEvents;
            if (!bgReadingEvents.isEmpty())
                mMostRecentValue = bgReadingEvents.get(0);
        });
        mBolusEvents = mBolusEventDao.getLiveData();
        mBolusEvents.observeForever(bolusEvents -> {
            mBolusEventsStatic = bolusEvents;
        });
        mBasalEvents = mBasalEventDao.getLiveData();
        mBasalEvents.observeForever(bolusEvents -> {
            mBasalEventsStatic = bolusEvents;
        });
        mCarbEvents = mCarbsEventDao.getLiveData();
        mCarbEvents.observeForever(carbEvents -> {
            mCarbEventsStatic = carbEvents;
        });
        mSportsEvents = mSportsEventDao.getLiveData();
        mSportsEvents.observeForever(sportsEvents -> {
            mSportsEventsStatic = sportsEvents;
        });
        mNoteEvents = mNoteEventDao.getLiveData();
        mNoteEvents.observeForever(noteEvents -> {
            mNoteEventsStatic = noteEvents;
        });
    }

    public static DiaryRepository getRepository(final Context context) {
        if (INSTANCE == null) {
            synchronized (AlertStore.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DiaryRepository(context);
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
        if (event == null) return;

        mExecutor.execute(() -> {
            switch (event.type) {
                case DiaryEvent.TYPE_BG:
                    mBgReadingEventDao.insert((BgReadingEvent) event);
                    break;
                case DiaryEvent.TYPE_BOLUS:
                    mBolusEventDao.insertAll((BolusEvent) event);
                    break;
                case DiaryEvent.TYPE_BASAL:
                    mBasalEventDao.insertAll((BasalEvent) event);
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

            List<BgReadingEvent> readings = mBgReadingEventDao.getEventInDateTimeRange(from, to);
            if (readings.isEmpty()) {
                mBgReadingEventDao.insert(event);
                Log.d(TAG, "Added missing backfill reading to DB " + event.value);
            } else {
                Log.d(TAG, "Found BG in time window. No backfill value added. " + readings.size());
                // Log.d(TAG, "ref: " + event.value + ", found: " + readings.get(0).value);
            }
        });
    }

    public void insertEventIfNotExist(DiaryEvent event) {
        if (event == null) return;

        mExecutor.execute(() -> {
            Duration widowHalf = Duration.of(1, ChronoUnit.MINUTES);
            Instant from = event.timestamp.minus(widowHalf);
            Instant to = event.timestamp.plus(widowHalf);

            switch (event.type) {
                case DiaryEvent.TYPE_BG:
                    List<BgReadingEvent> readings = mBgReadingEventDao.getEventInDateTimeRange(from, to);
                    if (readings.isEmpty()) {
                        mBgReadingEventDao.insert((BgReadingEvent) event);
                    }
                    break;
                case DiaryEvent.TYPE_BOLUS:
                    List<BolusEvent> bolus = mBolusEventDao.getEventInDateTimeRange(from, to);
                    if (bolus.isEmpty()) {
                        mBolusEventDao.insertAll((BolusEvent) event);
                    }
                    break;
                case DiaryEvent.TYPE_BASAL:
                    List<BasalEvent> basal = mBasalEventDao.getEventInDateTimeRange(from, to);
                    if (basal.isEmpty()) {
                        mBasalEventDao.insertAll((BasalEvent) event);
                    }
                    break;
                case DiaryEvent.TYPE_CARB:
                    List<CarbEvent> carbs = mCarbsEventDao.getEventInDateTimeRange(from, to);
                    if (carbs.isEmpty()) {
                        mCarbsEventDao.insertAll((CarbEvent) event);
                    }
                    break;
                case DiaryEvent.TYPE_SPORT:
                    List<SportsEvent> sports = mSportsEventDao.getEventInDateTimeRange(from, to);
                    if (sports.isEmpty()) {
                        mSportsEventDao.insertAll((SportsEvent) event);
                    }
                    break;
                case DiaryEvent.TYPE_NOTE:
                    List<NoteEvent> notes = mNoteEventDao.getEventInDateTimeRange(from, to);
                    if (notes.isEmpty()) {
                        mNoteEventDao.insertAll((NoteEvent) event);
                    }
                    break;
                default:
                    Log.e(TAG, "Can't save event with type: " + event.type);
            }
        });
    }

    /**
     * Remove an event. This also removes it from the database
     *
     * @param event Event to remove
     */
    public void removeEvent(DiaryEvent event) {
        mExecutor.execute(() -> {
            switch (event.type) {
                case DiaryEvent.TYPE_BG:
                    mBgReadingEventDao.delete((BgReadingEvent) event);
                    break;
                case DiaryEvent.TYPE_BOLUS:
                    mBolusEventDao.delete((BolusEvent) event);
                    break;
                case DiaryEvent.TYPE_BASAL:
                    mBasalEventDao.delete((BasalEvent) event);
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
    public List<DiaryEvent> getEvents() {
        List<DiaryEvent> events = new ArrayList<>();
        events.addAll(mBgReadingsStatic);
        events.addAll(mBolusEventsStatic);
        events.addAll(mCarbEventsStatic);
        events.addAll(mSportsEventsStatic);
        events.addAll(mNoteEventsStatic);
        events.addAll(mBasalEventsStatic);
        events.sort((a, b) -> b.timestamp.compareTo(a.timestamp));
        // TODO limit by date not by items
        return events;
    }

    public LiveData<List<BgReadingEvent>> getLiveBgEvents() {
        return mBgReadings;
    }

    /**
     * @return Most recent BG value or null if database is empty.
     */
    public BgReadingEvent getMostRecentBgEvent() {
        return mMostRecentValue;
    }

}