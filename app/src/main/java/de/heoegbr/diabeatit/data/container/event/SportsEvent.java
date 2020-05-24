package de.heoegbr.diabeatit.data.container.event;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Locale;

import de.heoegbr.diabeatit.R;

/**
 * Event representing an exercise event
 */
@Entity
public class SportsEvent extends DiaryEvent {
    // Intensity Constants
    public static final int INTENSITY_UNKNOWN = 0;
    public static final int INTENSITY_LOW = 1;
    public static final int INTENSITY_MID = 2;
    public static final int INTENSITY_HIGH = 3;
    /**
     * Duration of the exercise
     */
    @ColumnInfo(name = "intensity")
    public final @Intensity
    int intensity;

    public static String getIntensityString(Context context, @Intensity int intesity) {
        switch (intesity) {
            case INTENSITY_UNKNOWN:
                return context.getResources().getString(R.string.ms_intensity_unknown);
            case INTENSITY_LOW:
                return context.getResources().getString(R.string.ms_intensity_low);
            case INTENSITY_MID:
                return context.getResources().getString(R.string.ms_intensity_mid);
            case INTENSITY_HIGH:
                return context.getResources().getString(R.string.ms_intensity_high);
            default:
                return "CODE ERROR IN STRING CONVERSATION";
        }
    }

    /**
     * Create a new sports event
     *
     * @param timestamp   Timestamp of the beginning of the exercise
     * @param duration    Duration of the exercise
     * @param intensity
     * @param description Description the user provided
     */
    //TODO add picture to interface
    @Ignore
    public SportsEvent(@Source int source, Instant timestamp, double duration, @Intensity int intensity,
                       String description) {
        super(TYPE_SPORT, source, R.drawable.ic_fab_sports, timestamp, duration, null, description);
        this.intensity = intensity;
    }

    /**
     * Create a new sports event, supplying all fields. This is mainly used by the database to
     * create an event from a table row.
     *
     * @param logEventId Unqiue ID of this event, serves as primary key and is auto-generated
     *                   by the database.
     * @param iconId     Resource ID of an icon that may be displayed for this event
     * @param timestamp  Timestamp of the beginning of the exercise.
     * @param value      Duration of the exercise
     * @param note       Description of the exercise
     * @param intensity
     */
    public SportsEvent(@Source int source, long logEventId, int iconId, Instant timestamp,
                       double value, Bitmap picture, String note, @Intensity int intensity) {
        super(TYPE_SPORT, source, logEventId, iconId, timestamp, value, picture, note);
        this.intensity = intensity;
    }

    @Override
    public void createLayout(Context context, RelativeLayout root, boolean isSelected) {
        TextView titleV = root.findViewById(R.id.log_event_title);
        ImageView iconV = root.findViewById(R.id.log_event_icon);
        TextView timeV = root.findViewById(R.id.log_event_time);
        TextView contentV = root.findViewById(R.id.log_event_content);
        TextView noteV = root.findViewById(R.id.log_event_note);
        ImageView imgV = root.findViewById(R.id.log_event_picture);

        titleV.setText(context.getResources().getString(R.string.ms_event_title));
        iconV.setImageResource(iconId);
        timeV.setText(new SimpleDateFormat("dd.MM.YYYY HH:mm", Locale.GERMAN).format(Date.from(timestamp)));

        contentV.setVisibility(View.VISIBLE);
        noteV.setVisibility(View.VISIBLE);
        imgV.setVisibility(View.GONE);

        root.setBackgroundResource(isSelected ? R.drawable.log_event_selected_background : R.drawable.log_event_background);


        contentV.setText(context.getString(R.string.ms_event_minutes, value,
                getIntensityString(context, intensity)));
        noteV.setText(note);
    }

    // Declare the @IntDef for these constants
    @IntDef({INTENSITY_UNKNOWN, INTENSITY_LOW, INTENSITY_MID, INTENSITY_HIGH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Intensity {
    }

}