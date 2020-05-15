package de.heoegbr.diabeatit.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import de.heoegbr.diabeatit.db.container.event.BgReadingEvent;

@Dao
public interface BgReadingDao {
    @Query("SELECT * FROM BgReadingEvent ORDER BY timestamp DESC LIMIT 24")
    LiveData<List<BgReadingEvent>> getLiveReadings();

    @Query("SELECT * FROM BgReadingEvent ORDER BY timestamp DESC LIMIT 24")
    List<BgReadingEvent> getStaticReadings(); // 2 hours

    @Insert
    void insert(BgReadingEvent bgReading);

    @Query("DELETE FROM BgReadingEvent")
    void deleteAll();
}
