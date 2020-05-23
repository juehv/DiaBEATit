package de.heoegbr.diabeatit.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.Instant;
import java.util.List;

import de.heoegbr.diabeatit.db.container.event.BasalEvent;

/**
 * Data Access Object used to access the {@link de.heoegbr.diabeatit.db.container.event.BasalEvent}s stored in the database
 */
@Dao
public interface BasalEventDao {
    /**
     * Get a list of all {@link BasalEvent}s stored in the database
     *
     * @return A list containing all {@link BasalEvent}s stored in the database
     */
    @Query("SELECT * FROM BasalEvent")
    List<BasalEvent> getAll();

    @Query("SELECT * FROM BasalEvent ORDER BY timestamp DESC LIMIT 100")
    LiveData<List<BasalEvent>> getLiveData();

    /**
     * Get the 512 most recent {@link BasalEvent}s
     *
     * @return A list of {@link BasalEvent}s, ordered by {@link BasalEvent#timestamp} and limited
     * to 512 entries
     */
    @Query("SELECT * FROM BasalEvent ORDER BY timestamp DESC LIMIT 512")
    List<BasalEvent> getLimited();

    /**
     * Insert a list of {@link BasalEvent}s into the database
     *
     * @param events Events that should be inserted into the database
     */
    @Insert
    void insertAll(BasalEvent... events);

    /**
     * Delete a {@link BasalEvent} from the database
     *
     * @param event Event that should be deleted
     */
    @Delete
    void delete(BasalEvent event);

    @Query("SELECT * FROM BasalEvent WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC LIMIT 24")
    List<BasalEvent> getEventInDateTimeRange(Instant from, Instant to);
}
