package de.heoegbr.diabeatit.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.Instant;
import java.util.List;

import de.heoegbr.diabeatit.data.container.event.PredictionEvent;

@Dao
public interface PredictionEventDao {
    /**
     * @return a live dataset with a list of #BgReadingEvent not older than 12 hours
     */
    @Query("SELECT * FROM PredictionEvent WHERE datetime((timestamp/1000), 'unixepoch', 'localtime') >=  datetime('now', '-12 hours') ORDER BY timestamp DESC")
    LiveData<List<PredictionEvent>> getLiveReadings();

    @Query("SELECT * FROM PredictionEvent WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC LIMIT 24")
    List<PredictionEvent> getEventInDateTimeRange(Instant from, Instant to);

    @Insert
    void insertAll(PredictionEvent... events);

    @Delete
    void delete(PredictionEvent... events);
}
