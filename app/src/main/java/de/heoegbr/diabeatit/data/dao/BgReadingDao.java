package de.heoegbr.diabeatit.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.Instant;
import java.util.List;

import de.heoegbr.diabeatit.data.container.event.BgReadingEvent;

@Dao
public interface BgReadingDao {
    /**
     *
     * @return a live dataset with a list of #BgReadingEvent not older than 12 hours
     */
    @Query("SELECT * FROM BgReadingEvent WHERE datetime((timestamp/1000), 'unixepoch', 'localtime') >=  datetime('now', '-12 hours') ORDER BY timestamp DESC")
    LiveData<List<BgReadingEvent>> getLiveReadings();

    @Query("SELECT * FROM BgReadingEvent WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC LIMIT 24")
    List<BgReadingEvent> getEventInDateTimeRange(Instant from, Instant to);

    @Insert
    void insert(BgReadingEvent... events);

    @Delete
    void delete(BgReadingEvent... events);
}
