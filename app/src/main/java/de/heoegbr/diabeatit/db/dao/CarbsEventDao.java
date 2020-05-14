package de.heoegbr.diabeatit.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import de.heoegbr.diabeatit.db.container.event.CarbEvent;

/**
 * Data Access Object used to access the {@link CarbEvent}s stored in the database
 */
@Dao
public interface CarbsEventDao {
    /** Get all {@link CarbEvent}s in the database
     *
     * @return A list of all {@link CarbEvent}s found in the database
     */
    @Query("SELECT * FROM CarbEvent")
    List<CarbEvent> getAll();

    /** Get the 512 most recent {@link CarbEvent}s
     *
     * @return A list of {@link CarbEvent} ordered by timestamp and limited to 512 entries
     */
    @Query("SELECT * FROM CarbEvent ORDER BY timestamp DESC LIMIT 512")
    List<CarbEvent> getLimited();

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
}
