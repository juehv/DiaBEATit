package de.heoegbr.diabeatit.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.Instant;
import java.util.List;

import de.heoegbr.diabeatit.data.container.event.NoteEvent;

/** Data Access Object used to access the {@link NoteEvent}s stored in the database */
@Dao
public interface NoteEventDao {
    /**
     *
     * @return a live dataset with a list of #NoteEvent not older than 12 hours
     */
    @Query("SELECT * FROM NoteEvent WHERE datetime((timestamp/1000), 'unixepoch', 'localtime') >=  datetime('now', '-12 hours') ORDER BY timestamp DESC")
    LiveData<List<NoteEvent>> getLiveData();

    /** Insert a list of {@link NoteEvent}s into the database
     *
     * @param events Events which should be inserted into the database
     */
    @Insert
    void insertAll(NoteEvent... events);

    /** Delete a {@link NoteEvent} from the database
     *
     * @param event     Event that should be deleted.
     */
    @Delete
    void delete(NoteEvent event);

    @Query("SELECT * FROM NoteEvent WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC LIMIT 24")
    List<NoteEvent> getEventInDateTimeRange(Instant from, Instant to);
}
