package de.heoegbr.diabeatit.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.Instant;
import java.util.List;

import de.heoegbr.diabeatit.data.container.event.MealEvent;

/**
 * Data Access Object used to access the {@link MealEvent}s stored in the database
 */
@Dao
public interface CarbsEventDao {
    /**
     *
     * @return a live dataset with a list of #CarbEvent not older than 12 hours
     */
    @Query("SELECT * FROM MealEvent WHERE datetime((timestamp/1000), 'unixepoch', 'localtime') >=  datetime('now', '-12 hours') ORDER BY timestamp DESC")
    LiveData<List<MealEvent>> getLiveData();

    /** Insert a list of {@link MealEvent}s
     *
     * @param events List of {@link MealEvent}s that should be inserted into the database
     */
    @Insert
    void insertAll(MealEvent... events);

    /** Delete a {@link MealEvent} from the database
     *
     * @param event Event that should be deleted
     */
    @Delete
    void delete(MealEvent event);

    @Query("SELECT * FROM MealEvent WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC LIMIT 24")
    List<MealEvent> getEventInDateTimeRange(Instant from, Instant to);
}
