package de.heoegbr.diabeatit.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.Instant;
import java.util.List;

import de.heoegbr.diabeatit.data.container.event.CarbEvent;

/**
 * Data Access Object used to access the {@link CarbEvent}s stored in the database
 */
@Dao
public interface CarbsEventDao {
    /**
     *
     * @return a live dataset with a list of #CarbEvent not older than 12 hours
     */
    @Query("SELECT * FROM CarbEvent WHERE datetime((timestamp/1000), 'unixepoch', 'localtime') >=  datetime('now', '-12 hours') ORDER BY timestamp DESC")
    LiveData<List<CarbEvent>> getLiveData();

    /** Insert a list of {@link CarbEvent}s
     *
     * @param events List of {@link CarbEvent}s that should be inserted into the database
     */
    @Insert
    void insertAll(CarbEvent... events);

    /** Delete a {@link CarbEvent} from the database
     *
     * @param event Event that should be deleted
     */
    @Delete
    void delete(CarbEvent event);

    @Query("SELECT * FROM CarbEvent WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC LIMIT 24")
    List<CarbEvent> getEventInDateTimeRange(Instant from, Instant to);
}
