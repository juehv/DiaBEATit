package de.heoegbr.diabeatit.db.container.event;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Locale;

import de.heoegbr.diabeatit.R;

/**
 * Event that represents a meal
 */
@Entity
public class CarbEvent extends DiaryEvent {

    /**
     * Optional, user supplied Image of the meal
     */
    @ColumnInfo(name = "picture")
    public final Bitmap picture;
    /**
     * Amount of carbs taken in, in grams
     */
    @ColumnInfo(name = "carbs")
    public final int carbs;
    /**
     * Optional user supplied note
     */
    @ColumnInfo(name = "notes")
    public final String note;

    /**
     * Create a new carbs event. This constructor is mainly used to generate an object from the
     * database.
     *
     * @param logEventId Unique ID of this object, used as primary key and auto-generated
     * @param title      Title for this event
     * @param iconId     Resource ID of an icon that may be displayed for this event
     * @param timestamp  Timestamp when the meal was taken
     * @param picture    Optional image of the meal
     * @param carbs      Amount of carbs in grams
     * @param note       Optional note
     */
    public CarbEvent(long logEventId, int title, int iconId, Instant timestamp, Bitmap picture, int carbs, String note) {
        super(TYPE.CARB, logEventId, title, iconId, timestamp);
        this.picture = picture;
        this.carbs = carbs;
        this.note = note;
    }

    /**
     * Create a new carbs event
     *
     * @param timestamp Timestamp when the meal was taken
     * @param image     Image of the meal (Optional)
     * @param carbs     Amount of carbs in grams
     * @param note      Optional note
     */
    public CarbEvent(Instant timestamp, Bitmap image, int carbs, String note) {
        super(TYPE.CARB, R.string.mc_event_title, R.drawable.ic_fab_carbs, timestamp);
        picture = image;
        this.carbs = carbs;
        this.note = note;
    }

    @Override
    public void createLayout(Context context, RelativeLayout root, boolean isSelected) {
        TextView titleV = root.findViewById(R.id.log_event_title);
        ImageView iconV = root.findViewById(R.id.log_event_icon);
        TextView timeV = root.findViewById(R.id.log_event_time);
        TextView contentV = root.findViewById(R.id.log_event_content);
        TextView noteV = root.findViewById(R.id.log_event_note);
        ImageView imgV = root.findViewById(R.id.log_event_picture);

        titleV.setText(title);
        iconV.setImageResource(iconId);
        timeV.setText(new SimpleDateFormat("dd.MM.YYYY HH:mm", Locale.GERMAN)
                .format(Date.from(timestamp)));

        contentV.setVisibility(View.VISIBLE);
        noteV.setVisibility(!note.isEmpty() ? View.VISIBLE : View.GONE);
        imgV.setVisibility(picture != null ? View.VISIBLE : View.GONE);

        root.setBackgroundResource(isSelected ?
                R.drawable.log_event_selected_background :
                R.drawable.log_event_background);

        contentV.setText(carbs + "g");
        noteV.setText(note);

        if (picture != null)
            imgV.setImageBitmap(picture);
    }

}