package de.heoegbr.diabeatit.db.container.event;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Locale;

import de.heoegbr.diabeatit.R;

/**
 * Represents an entry of a manual bolus event.
 */
@Entity
public class BolusEvent extends DiaryEvent {

    /**
     * Amount administered in international units
     */
    @ColumnInfo(name = "bolus")
    public final double bolus;
    /**
     * Optional note that was supplied
     */
    @ColumnInfo(name = "note")
    public final String note;

    /**
     * Create a new bolus event
     *
     * @param timestamp Timestamp of administration
     * @param bolus     Amount of insulin administered in international units.
     * @param note      Optional note supplied
     */
    @Ignore
    public BolusEvent(Instant timestamp, double bolus, String note) {
        super(TYPE.BOLUS, R.string.mi_event_title, R.drawable.ic_fab_insulin, timestamp);
        this.bolus = bolus;
        this.note = note;
    }

    /**
     * Create a new bolus event. Mainly used to create the object from the database
     *
     * @param logEventId Unique ID of this event, serves as primary key and is auto generated
     * @param title      Title of this event
     * @param iconId     Resource ID of an icon that may be dispalyed for this event
     * @param timestamp  Timestamp of administration of the bolus
     * @param bolus      Amount of insulin administered in international units
     * @param note       User supplied optional note
     */
    public BolusEvent(long logEventId, int title, int iconId, Instant timestamp, double bolus, String note) {
        super(TYPE.BOLUS, logEventId, title, iconId, timestamp);
        this.bolus = bolus;
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
        timeV.setText(new SimpleDateFormat("dd.MM.YYYY HH:mm", Locale.GERMAN).format(Date.from(timestamp)));

        contentV.setVisibility(View.VISIBLE);
        noteV.setVisibility(!note.isEmpty() ? View.VISIBLE : View.GONE);
        imgV.setVisibility(View.GONE);

        root.setBackgroundResource(isSelected ? R.drawable.log_event_selected_background : R.drawable.log_event_background);

        contentV.setText(bolus + " IE");
        noteV.setText(note);
    }

}