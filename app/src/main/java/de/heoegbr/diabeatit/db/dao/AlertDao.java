package de.heoegbr.diabeatit.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.heoegbr.diabeatit.db.container.Alert;

/** Data Access Object to access the {@link Alert}s stored in the database */
@Dao
public interface AlertDao {
    /** Get ALL {@link Alert}s from the database
     *
     * @return A list of all the {@link Alert}s in the database
     */
    @Query("SELECT * FROM Alert")
    List<Alert> getAll();

    /** Get all active {@link Alert}s from the database, ordered by timestamp
     *
     * @return A list of all {@link Alert}s that are set to active, ordered by timestamp
     */
    @Query("SELECT * FROM Alert WHERE active = '1' ORDER BY timestamp")
    List<Alert> getActive();

    /**
     * Get dismissed {@link Alert}s from the database, to a maximum of 128 entries and ordered
     * by timestamp.
     *
     * @return A list of the 128 most recent {@link Alert}s which are set to inactive
     */
    @Query("SELECT * FROM Alert WHERE active = '0' ORDER BY timestamp LIMIT 128")
    List<Alert> getDismissedLimited();

    /** Insert a list of {@link Alert}s into the database
     *
     * @param alerts   Collection of {@link Alert}s which should be inserted into the database
     */
    @Insert
    void insertAll(Alert... alerts);

    /** Delete an {@link Alert} from the database
     *
     * @param alert {@link Alert} to be deleted
     */
    @Delete
    void delete(Alert alert);

    /** Update a list of {@link Alert}s in the database
     *
     * @param alerts    List of {@link Alert}s to update in the database
     */
    @Update
    void update(Alert... alerts);
}
