package de.heoegbr.diabeatit.db.repository;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.heoegbr.diabeatit.db.DiabeatitDatabase;
import de.heoegbr.diabeatit.db.container.event.BolusEvent;
import de.heoegbr.diabeatit.db.container.event.CarbEvent;
import de.heoegbr.diabeatit.db.container.event.DiaryEvent;
import de.heoegbr.diabeatit.db.container.event.NoteEvent;
import de.heoegbr.diabeatit.db.container.event.SportsEvent;
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

	private List<LogEventStoreListener> listeners = new ArrayList<>();
	private List<DiaryEvent> events = new ArrayList<>();

	private BolusEventDao mBolusEventDao;
	private CarbsEventDao mCarbsEventDao;
	private SportsEventDao mSportsEventDao;
	private NoteEventDao mNoteEventDao;
	private Executor mExecutor = Executors.newSingleThreadExecutor();

	private DiaryEventStore(final Context context) {
		DiabeatitDatabase db = DiabeatitDatabase.getDatabase(context);
		mBolusEventDao = db.bolusEventDao();
		mCarbsEventDao = db.carbsEventDao();
		mSportsEventDao = db.sportsEventDao();
		mNoteEventDao = db.noteEventDao();

		// Initialization: Load the events from the database
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
	 * Add a new {@link DiaryEvent}. This also inserts it into the database and notifies any attached
	 * {@link LogEventStoreListener}
	 *
	 * @param event Event to add
	 */
	public void addEvent(DiaryEvent event) {
		events.add(event);
		events.sort((a, b) -> b.timestamp.compareTo(a.timestamp));

		for (LogEventStoreListener l : listeners)
			l.onDatasetChange(event);

		mExecutor.execute(() -> {
			switch (event.type) {
                case DiaryEvent.TYPE_BOLUS:
					mBolusEventDao.insertAll((BolusEvent) event);
                case DiaryEvent.TYPE_CARB:
					mCarbsEventDao.insertAll((CarbEvent) event);
                case DiaryEvent.TYPE_SPORT:
					mSportsEventDao.insertAll((SportsEvent) event);
                case DiaryEvent.TYPE_NOTE:
					mNoteEventDao.insertAll((NoteEvent) event);
				default:
					Log.e(TAG, "Can't save event with type: " + event.type);
			}
		});
	}

	/**
	 * Attach a new listener that gets called whenever the dataset changes
	 *
	 * @param listener Listener to attach
	 */
	public void attachListener(LogEventStoreListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove an event. This also removes it from the database and notifes any attached
	 * {@link LogEventStoreListener}
	 *
	 * @param event Event to remove
	 */
	public void removeEvent(DiaryEvent event) {
		events.remove(event);

		for (LogEventStoreListener l : listeners)
			l.onDatasetChange();

		mExecutor.execute(() -> {
			switch (event.type) {
                case DiaryEvent.TYPE_BOLUS:
					mBolusEventDao.delete((BolusEvent) event);
                case DiaryEvent.TYPE_CARB:
					mCarbsEventDao.delete((CarbEvent) event);
                case DiaryEvent.TYPE_SPORT:
					mSportsEventDao.delete((SportsEvent) event);
                case DiaryEvent.TYPE_NOTE:
					mNoteEventDao.delete((NoteEvent) event);
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
		return new ArrayList<>(events);
	}

	/**
	 * Interface to listen for changes in {@link DiaryEvent}s
	 */
	@Deprecated
	public interface LogEventStoreListener {
		/**
		 * Called when the dataset changes
		 *
		 * @param e List of events that changed
		 */
		void onDatasetChange(DiaryEvent... e);
	}

}