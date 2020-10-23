package de.heoegbr.diabeatit.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.heoegbr.diabeatit.assistant.boluscalculator.IobCalculator;
import de.heoegbr.diabeatit.data.container.event.BasalEvent;
import de.heoegbr.diabeatit.data.container.event.BgReadingEvent;
import de.heoegbr.diabeatit.data.container.event.BolusEvent;
import de.heoegbr.diabeatit.data.container.event.DiaryEvent;
import de.heoegbr.diabeatit.data.container.event.MealEvent;
import de.heoegbr.diabeatit.data.container.event.NoteEvent;
import de.heoegbr.diabeatit.data.container.event.PredictionEvent;
import de.heoegbr.diabeatit.data.container.event.SportsEvent;
import de.heoegbr.diabeatit.data.dao.BasalEventDao;
import de.heoegbr.diabeatit.data.dao.BgReadingDao;
import de.heoegbr.diabeatit.data.dao.BolusEventDao;
import de.heoegbr.diabeatit.data.dao.MealEventDao;
import de.heoegbr.diabeatit.data.dao.NoteEventDao;
import de.heoegbr.diabeatit.data.dao.PredictionEventDao;
import de.heoegbr.diabeatit.data.dao.SportsEventDao;
import de.heoegbr.diabeatit.data.localdb.DiabeatitDatabase;

/**
 * Manages {@link DiaryEvent}s. Provides an interface to listen for changes as well as keeps the
 * events in the Database up to date.
 */
public class DiaryRepository {
    public static final String TAG = "DIARY_REPOSITORY";

    public static DiaryRepository INSTANCE = null;
    private final BgReadingDao mBgReadingEventDao;
    private final BolusEventDao mBolusEventDao;
    private final BasalEventDao mBasalEventDao;
    private final MealEventDao mMealEventDao;
    private final SportsEventDao mSportsEventDao;
    private final NoteEventDao mNoteEventDao;
    private final PredictionEventDao mPredictionEventDao;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private LiveData<List<BgReadingEvent>> mBgReadings;
    private List<BgReadingEvent> mBgReadingsStatic;
    MediatorLiveData trigger = null;
    private LiveData<List<BolusEvent>> mBolusEvents;
    private List<BolusEvent> mBolusEventsStatic;
    private LiveData<List<BasalEvent>> mBasalEvents;
    private List<BasalEvent> mBasalEventsStatic;
    private LiveData<List<MealEvent>> mCarbEvents;
    private List<MealEvent> mMealEventsStatic;
    private LiveData<List<SportsEvent>> mSportsEvents;
    private List<SportsEvent> mSportsEventsStatic;
    private LiveData<List<NoteEvent>> mNoteEvents;
    private List<NoteEvent> mNoteEventsStatic;
    private BgReadingEvent mMostRecentBgValueStatic = null;
    private LiveData<List<PredictionEvent>> mPredictionEvents;
    private PredictionEvent mMostRecentPreditcionStatic = null;

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

    private DiaryRepository(final Context context) {
        DiabeatitDatabase db = DiabeatitDatabase.getDatabase(context);
        mBgReadingEventDao = db.bgReadingDao();
        mBolusEventDao = db.bolusEventDao();
        mBasalEventDao = db.basalEventDao();
        mMealEventDao = db.carbsEventDao();
        mSportsEventDao = db.sportsEventDao();
        mNoteEventDao = db.noteEventDao();
        mPredictionEventDao = db.predictionEventDao();

        mBgReadings = mBgReadingEventDao.getLiveReadings();
        mBgReadings.observeForever(bgReadingEvents -> {
//            Log.d(TAG, "Live Data: " + bgReadingEvents.get(0).value + ", " + bgReadingEvents.get(bgReadingEvents.size() - 1).value);
            mBgReadingsStatic = bgReadingEvents;
            if (!bgReadingEvents.isEmpty())
                mMostRecentBgValueStatic = bgReadingEvents.get(0);
        });
        mBolusEvents = mBolusEventDao.getLiveData();
        mBolusEvents.observeForever(bolusEvents -> {
            mBolusEventsStatic = bolusEvents;
        });
        mBasalEvents = mBasalEventDao.getLiveData();
        mBasalEvents.observeForever(bolusEvents -> {
            mBasalEventsStatic = bolusEvents;
        });
        mCarbEvents = mMealEventDao.getLiveData();
        mCarbEvents.observeForever(carbEvents -> {
            mMealEventsStatic = carbEvents;
        });
        mSportsEvents = mSportsEventDao.getLiveData();
        mSportsEvents.observeForever(sportsEvents -> {
            mSportsEventsStatic = sportsEvents;
        });
        mNoteEvents = mNoteEventDao.getLiveData();
        mNoteEvents.observeForever(noteEvents -> {
            mNoteEventsStatic = noteEvents;
        });
        mPredictionEvents = mPredictionEventDao.getLiveReadings();
        mPredictionEvents.observeForever(predictionEvent -> {
            if (!predictionEvent.isEmpty())
                mMostRecentPreditcionStatic = predictionEvent.get(0);
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
                case DiaryEvent.TYPE_MEAL:
                    mMealEventDao.insertAll((MealEvent) event);
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
                case DiaryEvent.TYPE_MEAL:
                    List<MealEvent> carbs = mMealEventDao.getEventInDateTimeRange(from, to);
                    if (carbs.isEmpty()) {
                        mMealEventDao.insertAll((MealEvent) event);
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
                case DiaryEvent.TYPE_PREDICTION:
                    List<PredictionEvent> prediticons = mPredictionEventDao.getEventInDateTimeRange(from, to);
                    if (prediticons.isEmpty()) {
                        mPredictionEventDao.insertAll((PredictionEvent) event);
                    }
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
        if (mBgReadingsStatic != null) events.addAll(mBgReadingsStatic);
        if (mBolusEventsStatic != null) events.addAll(mBolusEventsStatic);
        if (mBasalEventsStatic != null) events.addAll(mBasalEventsStatic);
        if (mMealEventsStatic != null) events.addAll(mMealEventsStatic);
        if (mSportsEventsStatic != null) events.addAll(mSportsEventsStatic);
        if (mNoteEventsStatic != null) events.addAll(mNoteEventsStatic);

        events.sort((a, b) -> b.timestamp.compareTo(a.timestamp));

        return events;
    }

    /**
     * Returns most recent bolus events
     *
     * @return
     */
    public List<BolusEvent> getBolusEventsStatic() {
        return mBolusEventsStatic;
    }

    /**
     * Returns Insulin-On-Board, calculated from static Bosuls Events data.
     *
     * @param dia duration of insulin activity in minutes (usually 300 to 360 min)
     * @return
     */
    public double getIOB(double dia, double peek) {
        Instant oldestInterestingBolus = Instant.now().minusSeconds(Math.round(dia * 60) + 1);
        double iob = 0;
        if (mBolusEventsStatic != null) {
            synchronized (mBolusEventsStatic) {
                for (BolusEvent item : mBolusEventsStatic) {
                    if (item.timestamp.isAfter(oldestInterestingBolus)) {
                        iob += IobCalculator.getActiveIobFromBolus(item, dia, peek);
                    }
                }
            }
        }
        return iob;
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
                case DiaryEvent.TYPE_MEAL:
                    mMealEventDao.delete((MealEvent) event);
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

    public List<DiaryEvent> getPlotEvents() {
        List<DiaryEvent> events = new ArrayList<>();
        if (mBgReadingsStatic != null) events.addAll(mBgReadingsStatic);
        if (mBolusEventsStatic != null) events.addAll(mBolusEventsStatic);
        if (mBasalEventsStatic != null) events.addAll(mBasalEventsStatic);
        if (mMealEventsStatic != null) events.addAll(mMealEventsStatic);
        if (mSportsEventsStatic != null) events.addAll(mSportsEventsStatic);
        if (mMostRecentPreditcionStatic != null) events.add(mMostRecentPreditcionStatic);
        events.sort((a, b) -> b.timestamp.compareTo(a.timestamp));

        return events;
    }

    public MediatorLiveData getDataTriggerForPredictions() {
        if (trigger == null) {
            trigger = new MediatorLiveData<>();
            trigger.addSource(mBgReadings, events -> {
                trigger.setValue(events);
            });
            trigger.addSource(mBolusEvents, events -> {
                trigger.setValue(events);
            });
            trigger.addSource(mBasalEvents, events -> {
                trigger.setValue(events);
            });
            trigger.addSource(mCarbEvents, events -> {
                trigger.setValue(events);
            });
            trigger.addSource(mSportsEvents, events -> {
                trigger.setValue(events);
            });
        }
        return trigger;
    }

    /**
     * Do not call from GUI thread.
     * Does not return notes and predictions
     *
     * @param from
     * @return
     */
    public List<DiaryEvent> getPredictionEventsInSameThread(Instant from) {
        Instant to = Instant.now();
        List<DiaryEvent> events = new ArrayList<>();
        events.addAll(mBgReadingEventDao.getEventInDateTimeRange(from, to));
        events.addAll(mBolusEventDao.getEventInDateTimeRange(from, to));
        events.addAll(mBasalEventDao.getEventInDateTimeRange(from, to));
        events.addAll(mMealEventDao.getEventInDateTimeRange(from, to));
        events.addAll(mSportsEventDao.getEventInDateTimeRange(from, to));
        events.sort((a, b) -> b.timestamp.compareTo(a.timestamp));
        return events;
    }

    public LiveData<List<BgReadingEvent>> getLiveBgEvents() {
        return mBgReadings;
    }

    public LiveData<List<BolusEvent>> getLiveBolusEvents() {
        return mBolusEvents;
    }

    public LiveData<List<BasalEvent>> getLiveBasalEvents() {
        return mBasalEvents;
    }

    public LiveData<List<MealEvent>> getLiveCarbEvents() {
        return mCarbEvents;
    }

    public LiveData<List<SportsEvent>> getLiveSportsEvents() {
        return mSportsEvents;
    }

    public LiveData<List<PredictionEvent>> getmPredictionEvents() {
        return mPredictionEvents;
    }

    /**
     * @return Most recent BG value or null if database is empty.
     */
    public BgReadingEvent getMostRecentBgEvent() {
        return mMostRecentBgValueStatic;
    }

}