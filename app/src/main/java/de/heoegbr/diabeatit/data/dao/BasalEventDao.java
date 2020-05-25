package de.heoegbr.diabeatit.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.Instant;
import java.util.List;

import de.heoegbr.diabeatit.data.container.event.BasalEvent;

/**
 * Data Access Object used to access the {@link de.heoegbr.diabeatit.data.container.event.BasalEvent}s stored in the database
 */
@Dao
public interface BasalEventDao {
    /**
     *
     * @return a live dataset with a list of #BasalEvent not older than 12 hours
     */
    @Query("SELECT * FROM BasalEvent WHERE timestamp <= datetime('now', '-12 hour') ORDER BY timestamp DESC")
    LiveData<List<BasalEvent>> getLiveData();

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
