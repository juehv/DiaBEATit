package de.heoegbr.diabeatit.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.heoegbr.diabeatit.db.container.Alert;
import de.heoegbr.diabeatit.db.container.event.BgReadingEvent;
import de.heoegbr.diabeatit.db.container.event.BolusEvent;
import de.heoegbr.diabeatit.db.container.event.CarbEvent;
import de.heoegbr.diabeatit.db.container.event.NoteEvent;
import de.heoegbr.diabeatit.db.container.event.SportsEvent;
import de.heoegbr.diabeatit.db.dao.AlertDao;
import de.heoegbr.diabeatit.db.dao.BgReadingDao;
import de.heoegbr.diabeatit.db.dao.BolusEventDao;
import de.heoegbr.diabeatit.db.dao.CarbsEventDao;
import de.heoegbr.diabeatit.db.dao.NoteEventDao;
import de.heoegbr.diabeatit.db.dao.SportsEventDao;

/**
 * Application database for the objects managed by the DiaBEATit project.
 */
@Database(entities = {
        Alert.class,
        BolusEvent.class,
        CarbEvent.class,
        SportsEvent.class,
        NoteEvent.class,
        BgReadingEvent.class
}, version = 1, exportSchema = false)
@TypeConverters({de.heoegbr.diabeatit.db.TypeConverters.class})
public abstract class DiabeatitDatabase extends RoomDatabase {

    private static final int NUMBER_OF_THREADS = 2;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static volatile DiabeatitDatabase INSTANCE;

    public static DiabeatitDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DiabeatitDatabase.class) {
                if (INSTANCE == null) {
                    // TODO multi table update
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DiabeatitDatabase.class, "diabeatit_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Get a data access object for {@link Alert}s
     */
    public abstract AlertDao alertDao();

    /**
     * Get a data access object for {@link BolusEvent}
     */
    public abstract BolusEventDao bolusEventDao();

    /**
     * Get a data access object for {@link CarbEvent}
     */
    public abstract CarbsEventDao carbsEventDao();

    /**
     * Get a data access object for {@link SportsEvent}
     */
    public abstract SportsEventDao sportsEventDao();

    /**
     * Get a data access object for {@link NoteEvent}
     */
    public abstract NoteEventDao noteEventDao();

    /**
     * Get a data access object for {@link BgReadingEvent}
     */
    public abstract BgReadingDao bgReadingDao();

}
