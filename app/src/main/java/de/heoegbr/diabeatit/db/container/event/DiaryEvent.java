package de.heoegbr.diabeatit.db.container.event;

import android.content.Context;
import android.widget.RelativeLayout;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.Instant;

/**
 * Parent class for any loggable events, such as inserting a manual bolus.
 */
public abstract class DiaryEvent {
	@Ignore
	public final TYPE type;
	/**
	 * Title for the event
	 */
	@ColumnInfo(name = "title")
	public final int title;

	/** Unique ID for each event -- used in the database */
	@PrimaryKey(autoGenerate = true)
	public long logEventId;
	/** Resource ID of an icon that should be displayed for this event */
	@ColumnInfo(name = "icon")
	public final int iconId;
	/** Timestamp when this event was created */
	@ColumnInfo(name = "timestamp")
	public final Instant timestamp;
	/**
	 * 	Initialize the shared fields on this {@link DiaryEvent}. T
	 *
	 * 	This method is mostly used by the subclasses to initalize the fields when loading an object
	 * 	from the database.
	 * @param logEventId	Unique ID of this event, serves as primary key in the database
	 * @param title            Title for this event
	 * @param iconId            Resource ID of an icon that might be displayed for this event
	 * @param timestamp        Timestamp of creation
	 */
	public DiaryEvent(TYPE type, long logEventId, int title, int iconId, Instant timestamp) {
		this(type, title, iconId, timestamp);
		this.logEventId = logEventId;
	}

	/**
	 * Initialize the fields. This method is used by its subclasses to initalize the shared fields
	 * @param title			Title for this event
	 * @param iconId            Resource ID of an icon that may be displayed for this event
	 * @param timestamp		Timestamp of creation of this event
	 */
	public DiaryEvent(TYPE type, int title, int iconId, Instant timestamp) {
		this.type = type;
		this.title = title;
		this.iconId = iconId;
		this.timestamp = timestamp;
	}

	public enum TYPE {NONE, BOLUS, CARB, SPORT, NOTE}

	/** Create a layout for this event -- used in the Log to display various events */
	public abstract void createLayout(Context context, RelativeLayout root, boolean isSelected);

}