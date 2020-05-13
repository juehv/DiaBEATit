package de.heoegbr.diabeatit.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import de.heoegbr.diabeatit.log.event.BolusEvent;

/** Data Access Object to access the {@link BolusEvent}s in the database */
@Dao
public interface BolusEventDao {
    /**
     * Get a list of all {@link BolusEvent}s in the database
     * @return A list of all {@link BolusEvent}s in the database.
     */
    @Query("SELECT * FROM BolusEvent")
    List<BolusEvent> getAll();

    /** Get the 512 most recent {@link BolusEvent}s
     *
     * @return A list of {@link BolusEvent}s, maximum 512, ordered by timestamp
     */
    @Query("SELECT * FROM BolusEvent ORDER BY timestamp DESC LIMIT 512")
    List<BolusEvent> getLimited();

    /** Insert a list of {@link BolusEvent} into the database
     *
     * @param events List of {@link BolusEvent}s which should be inserted into the database
     */
    @Insert
    void insertAll(BolusEvent... events);

    /** Delete a {@link BolusEvent} from the database
     *
     * @param event Event that should be deleted.
     */
    @Delete
    void delete(BolusEvent event);
}
