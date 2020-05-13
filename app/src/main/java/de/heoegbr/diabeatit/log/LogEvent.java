package de.heoegbr.diabeatit.log;

import android.content.Context;
import android.widget.RelativeLayout;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import java.time.Instant;

/**
 * Parent class for any loggable events, such as inserting a manual bolus.
 */
public abstract class LogEvent {

	/** Unique ID for each event -- used in the database */
	@PrimaryKey(autoGenerate = true)
	public long logEventId;
	/** Title for the event*/
	@ColumnInfo(name = "title")
	public final int TITLE;
	/** Resource ID of an icon that should be displayed for this event */
	@ColumnInfo(name = "icon")
	public final int ICON;
	/** Timestamp when this event was created */
	@ColumnInfo(name = "timestamp")
	public final Instant TIMESTAMP;

	/**
	 * 	Initialize the shared fields on this {@link LogEvent}. T
	 *
	 * 	This method is mostly used by the subclasses to initalize the fields when loading an object
	 * 	from the database.
	 * @param logEventId	Unique ID of this event, serves as primary key in the database
	 * @param TITLE			Title for this event
	 * @param ICON			Resource ID of an icon that might be displayed for this event
	 * @param TIMESTAMP		Timestamp of creation
	 */
	public LogEvent(long logEventId, int TITLE, int ICON, Instant TIMESTAMP) {
		this(TITLE, ICON, TIMESTAMP);
		this.logEventId = logEventId;
	}

	/**
	 * Initialize the fields. This method is used by its subclasses to initalize the shared fields
	 * @param title			Title for this event
	 * @param icon			Resource ID of an icon that may be displayed for this event
	 * @param timestamp		Timestamp of creation of this event
	 */
	public LogEvent(int title, int icon, Instant timestamp) {

		TITLE = title;
		ICON = icon;
		TIMESTAMP = timestamp;

	}

	/** Create a layout for this event -- used in the Log to display various events */
	public abstract void createLayout(Context context, RelativeLayout root, boolean isSelected);

}