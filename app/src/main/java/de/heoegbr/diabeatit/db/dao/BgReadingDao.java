package de.heoegbr.diabeatit.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.Instant;
import java.util.List;

import de.heoegbr.diabeatit.db.container.event.BgReadingEvent;

@Dao
public interface BgReadingDao {
    @Query("SELECT * FROM BgReadingEvent ORDER BY timestamp DESC LIMIT 24")
    LiveData<List<BgReadingEvent>> getLiveReadings();

    @Query("SELECT * FROM BgReadingEvent ORDER BY timestamp DESC LIMIT 24")
    List<BgReadingEvent> getStaticReadings(); // 2 hours

    @Query("SELECT * FROM BgReadingEvent WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC LIMIT 24")
    List<BgReadingEvent> getEventInDateTimeRange(Instant from, Instant to);

    @Insert
    void insert(BgReadingEvent... events);

    @Delete
    void delete(BgReadingEvent... events);
}
