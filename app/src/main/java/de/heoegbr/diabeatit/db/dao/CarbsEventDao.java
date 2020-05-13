package de.heoegbr.diabeatit.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import de.heoegbr.diabeatit.db.container.event.CarbsEvent;

/** Data Access Object used to access the {@link CarbsEvent}s stored in the database */
@Dao
public interface CarbsEventDao {
    /** Get all {@link CarbsEvent}s in the database
     *
     * @return A list of all {@link CarbsEvent}s found in the database
     */
    @Query("SELECT * FROM carbsevent")
    List<CarbsEvent> getAll();

    /** Get the 512 most recent {@link CarbsEvent}s
     *
     * @return A list of {@link CarbsEvent} ordered by timestamp and limited to 512 entries
     */
    @Query("SELECT * FROM CarbsEvent ORDER BY timestamp DESC LIMIT 512")
    List<CarbsEvent> getLimited();

    /** Insert a list of {@link CarbsEvent}s
     *
     * @param events List of {@link CarbsEvent}s that should be inserted into the database
     */
    @Insert
    void insertAll(CarbsEvent... events);

    /** Delete a {@link CarbsEvent} from the database
     *
     * @param event Event that should be deleted
     */
    @Delete
    void delete(CarbsEvent event);
}
