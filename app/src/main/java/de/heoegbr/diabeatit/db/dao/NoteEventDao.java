package de.heoegbr.diabeatit.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import de.heoegbr.diabeatit.db.container.event.NoteEvent;

/** Data Access Object used to access the {@link NoteEvent}s stored in the database */
@Dao
public interface NoteEventDao {
    /** Get a list of all {@link NoteEvent}s stored in the database
     *
     * @return A list containing all {@link NoteEvent}s stored in the database
     */
    @Query("SELECT * FROM noteevent")
    List<NoteEvent> getAll();

    /** Get the 512 most recent {@link NoteEvent}s
     *
     * @return List of {@link NoteEvent}s, sorted by timestamp and limited to 512 entries
     */
    @Query("SELECT * FROM NoteEvent ORDER BY timestamp DESC LIMIT 512")
    List<NoteEvent> getLimited();

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
}
