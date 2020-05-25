package de.heoegbr.diabeatit.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.Instant;
import java.util.List;

import de.heoegbr.diabeatit.data.container.event.SportsEvent;

/** Data Access Object used to access the {@link SportsEvent}s stored in the database */
@Dao
public interface SportsEventDao {
    /**
     *
     * @return a live dataset with a list of #SportsEvent not older than 4 hours
     */
    @Query("SELECT * FROM SportsEvent WHERE datetime((timestamp/1000), 'unixepoch', 'localtime') >=  datetime('now', '-4 hours') ORDER BY timestamp DESC")
    LiveData<List<SportsEvent>> getLiveData();

    /** Insert a list of {@link SportsEvent}s into the database
     *
     * @param events    Events that should be inserted into the database
     */
    @Insert
    void insertAll(SportsEvent... events);

    /** Delete a {@link SportsEvent} from the database
     *
     * @param event     Event that should be deleted
     */
    @Delete
    void delete(SportsEvent event);

    @Query("SELECT * FROM SportsEvent WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC LIMIT 24")
    List<SportsEvent> getEventInDateTimeRange(Instant from, Instant to);
}