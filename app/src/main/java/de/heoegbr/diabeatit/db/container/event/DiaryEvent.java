package de.heoegbr.diabeatit.db.container.event;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.RelativeLayout;

import androidx.annotation.IntDef;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Instant;

/**
 * Parent class for any loggable events, such as inserting a manual bolus.
 */
public abstract class DiaryEvent {
    // Type constants
    public static final int TYPE_NONE = 0;
    public static final int TYPE_BG = 1;
    public static final int TYPE_BOLUS = 2;
    public static final int TYPE_BASAL = 3;
    public static final int TYPE_CARB = 4;
    public static final int TYPE_SPORT = 5;
    public static final int TYPE_NOTE = 6;
    public static final int TYPE_HR = 7;
    public static final int TYPE_STRESS = 8;
    public static final int TYPE_BLOOD_PRESSURE = 9;
    public static final int TYPE_WEIGHT = 10;
    // Source of information constants
    public static final int SOURCE_UNKNOWN = 0;
    public static final int SOURCE_USER = 1;
    public static final int SOURCE_DEVICE = 2;
    public static final int SOURCE_CLOUD = 3;
    @Ignore
    public final @Type
    int type;
    @ColumnInfo(name = "source")
    public final @Source
    int source;
    /**
     * Resource ID of an icon that should be displayed for this event
     * TODO do we need this in db ?? Isn't it static information?
     */
    @ColumnInfo(name = "icon")
    public final int iconId;
    /**
     * Timestamp when this event was created
     */
    @ColumnInfo(name = "timestamp")
    public final Instant timestamp;
    /**
     * Value of the event (CGM value, Bolus units, Carbs in g, Sports duration...)
     */
    @ColumnInfo(name = "value")
    public final double value;
    /**
     * Optional, user supplied Image
     */
    @ColumnInfo(name = "picture")
    public final Bitmap picture;
    /**
     * Optional user supplied note
     */
    @ColumnInfo(name = "note")
    public final String note;
    /**
     * Unique ID for each event -- used in the database
     */
    @PrimaryKey(autoGenerate = true)
    public long logEventId;

    /**
     * Initialize the shared fields on this {@link DiaryEvent}. T
     * <p>
     * This method is mostly used by the subclasses to initalize the fields when loading an object
     * from the database.
     *
     * @param logEventId Unique ID of this event, serves as primary key in the database
     * @param iconId     Resource ID of an icon that might be displayed for this event
     * @param timestamp  Timestamp of creation
     * @param value
     * @param picture
     * @param note
     */
    public DiaryEvent(@Type int type, @Source int source, long logEventId, int iconId,
                      Instant timestamp, double value, Bitmap picture, String note) {
        this(type, source, iconId, timestamp, value, picture, note);
        this.logEventId = logEventId;
    }

    /**
     * Initialize the fields. This method is used by its subclasses to initalize the shared fields
     *
     * @param iconId    Resource ID of an icon that may be displayed for this event
     * @param timestamp Timestamp of creation of this event
     * @param value
     * @param picture
     * @param note
     */
    DiaryEvent(@Type int type, @Source int source, int iconId, Instant timestamp, double value,
               Bitmap picture, String note) {
        this.type = type;
        this.source = source;
        this.iconId = iconId;
        this.timestamp = timestamp;
        this.value = value;
        this.picture = picture;
        this.note = note;
    }

    /**
     * Create a layout for this event -- used in the Log to display various events
     */
    public abstract void createLayout(Context context, RelativeLayout root, boolean isSelected);

    // Declare the @IntDef for these constants
    @IntDef({TYPE_NONE, TYPE_BG, TYPE_BOLUS, TYPE_BASAL, TYPE_CARB, TYPE_SPORT,
            TYPE_NOTE, TYPE_HR, TYPE_STRESS, TYPE_BLOOD_PRESSURE, TYPE_WEIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    // Declare the @IntDef for these constants
    @IntDef({SOURCE_UNKNOWN, SOURCE_USER, SOURCE_DEVICE, SOURCE_CLOUD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Source {
    }

}