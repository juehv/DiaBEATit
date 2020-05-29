package de.heoegbr.diabeatit.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.Instant;
import java.util.List;

import de.heoegbr.diabeatit.data.container.event.BolusEvent;

/** Data Access Object to access the {@link BolusEvent}s in the database */
@Dao
public interface BolusEventDao {
    /**
     *
     * @return a live dataset with a list of #BolusEvent not older than 12 hours
     */
    @Query("SELECT * FROM BolusEvent WHERE datetime((timestamp/1000), 'unixepoch', 'localtime') >=  datetime('now', '-12 hours') ORDER BY timestamp DESC")
    LiveData<List<BolusEvent>> getLiveData();

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

    @Query("SELECT * FROM BolusEvent WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    List<BolusEvent> getEventInDateTimeRange(Instant from, Instant to);
}
