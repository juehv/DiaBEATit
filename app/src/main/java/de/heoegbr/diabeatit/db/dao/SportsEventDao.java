package de.heoegbr.diabeatit.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import de.heoegbr.diabeatit.db.container.event.SportsEvent;

/** Data Access Object used to access the {@link SportsEvent}s stored in the database */
@Dao
public interface SportsEventDao {
    /** Get a list of all {@link SportsEvent}s stored in the database
     *
     * @return A list containing all {@link SportsEvent}s stored in the database
     */
    @Query("SELECT * FROM SportsEvent")
    List<SportsEvent> getAll();

    @Query("SELECT * FROM SportsEvent ORDER BY timestamp DESC LIMIT 100")
    LiveData<List<SportsEvent>> getLiveData();

    /** Get the 512 most recent {@link SportsEvent}s
     *
     * @return  A list of {@link SportsEvent}s, ordered by {@link SportsEvent#TIMESTAMP} and limited
     *          to 512 entries
     */
    @Query("SELECT * FROM SportsEvent ORDER BY timestamp DESC LIMIT 512")
    List<SportsEvent> getLimited();

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
}
