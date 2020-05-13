package de.heoegbr.diabeatit.log;

import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;

import de.heoegbr.diabeatit.DiaBEATitApp;
import de.heoegbr.diabeatit.StaticData;
import de.heoegbr.diabeatit.db.DiabeatitDatabase;
import de.heoegbr.diabeatit.log.event.BolusEvent;
import de.heoegbr.diabeatit.log.event.CarbsEvent;
import de.heoegbr.diabeatit.log.event.NoteEvent;
import de.heoegbr.diabeatit.log.event.SportsEvent;

/**
 * Manages {@link LogEvent}s. Provides an interface to listen for changes as well as keeps the
 * events in the Database up to date.
 */
public class LogEventStore {

	private static List<LogEventStoreListener> listeners = new ArrayList<>();
	private static List<LogEvent> events = new ArrayList<>();

	static {
		// Initialization: Load the events from the database
		// Note that this is blocking the thread since it needs to be run synchronously
		DiabeatitDatabase db = Room.databaseBuilder(
				DiaBEATitApp.getContext(),
					DiabeatitDatabase.class,
					StaticData.ROOM_DATABASE_NAME)
				.allowMainThreadQueries()
				.build();

		events.addAll(db.bolusEventDao().getLimited());
		events.addAll(db.carbsEventDao().getLimited());
		events.addAll(db.sportsEventDao().getLimited());
		events.addAll(db.noteEventDao().getLimited());

		events.sort((a, b) -> b.TIMESTAMP.compareTo(a.TIMESTAMP));

		// Notify any atteached listeners of the changed dataset. This should be an empty list in
		// almost all cases, however since we might attach internal listeners in this static block
		// it still gets called, just in case.
		for (LogEventStoreListener l : listeners)
			l.onDatasetChange((LogEvent[]) events.toArray());

	}

	/**
	 * Interface to listen for changes in {@link LogEvent}s
	 */
	public interface LogEventStoreListener {

		/**
		 * Called when the dataset changes
		 * @param e		List of events that changed
		 */
		void onDatasetChange(LogEvent... e);

	}

	/**
	 * Attach a new listener that gets called whenever the dataset changes
	 * @param listener	Listener to attach
	 */
	public static void attachListener(LogEventStoreListener listener) {

		listeners.add(listener);

	}

	/**
	 * Add a new {@link LogEvent}. This also inserts it into the database and notifies any attached
	 * {@link LogEventStoreListener}
	 * @param event		Event to add
	 */
	public static void addEvent(LogEvent event) {

		events.add(event);
		events.sort((a, b) -> b.TIMESTAMP.compareTo(a.TIMESTAMP));

		for (LogEventStoreListener l : listeners)
			l.onDatasetChange(event);

		new Thread(() -> {

			DiabeatitDatabase db = Room.databaseBuilder(
					DiaBEATitApp.getContext(),
						DiabeatitDatabase.class,
						StaticData.ROOM_DATABASE_NAME)
					.build();

			if (event instanceof BolusEvent)
				db.bolusEventDao().insertAll((BolusEvent) event);
			else if (event instanceof CarbsEvent)
				db.carbsEventDao().insertAll((CarbsEvent) event);
			else if (event instanceof SportsEvent)
				db.sportsEventDao().insertAll((SportsEvent) event);
			else if (event instanceof NoteEvent)
				db.noteEventDao().insertAll((NoteEvent) event);

		}).start();

	}

	/**
	 * Remove an event. This also removes it from the database and notifes any attached
	 * {@link LogEventStoreListener}
	 * @param event		Event to remove
	 */
	public static void removeEvent(LogEvent event) {

		events.remove(event);

		for (LogEventStoreListener l : listeners)
			l.onDatasetChange();

		// Start a new thread removing it from the database, this will run concurrently and wont
		// block
		new Thread(() -> {
			DiabeatitDatabase db = Room.databaseBuilder(DiaBEATitApp.getContext(),
					DiabeatitDatabase.class,
					StaticData.ROOM_DATABASE_NAME).build();

			if (event instanceof BolusEvent)
				db.bolusEventDao().delete((BolusEvent) event);
			else if (event instanceof CarbsEvent)
				db.carbsEventDao().delete((CarbsEvent) event);
			else if (event instanceof SportsEvent)
				db.sportsEventDao().delete((SportsEvent) event);
			else if (event instanceof NoteEvent)
				db.noteEventDao().delete((NoteEvent) event);

		}).start();

	}

	/**
	 * Get a list of all stored events. This might not be an exhaustive list, since there is a limit
	 * on how many events get loaded from the database on start up.
	 * @return	A list containing all stored {@link LogEvent}s
	 */
	public static List<LogEvent> getEvents() {

		return new ArrayList<>(events);

	}

}