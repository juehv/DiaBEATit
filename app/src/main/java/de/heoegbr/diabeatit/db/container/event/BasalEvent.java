package de.heoegbr.diabeatit.db.container.event;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.room.Entity;
import androidx.room.Ignore;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Locale;

import de.heoegbr.diabeatit.R;

@Entity
public class BasalEvent extends DiaryEvent {
    /**
     * Duration of an Basal event (in case of an insulin pump)
     */
    public final double duration;

    /**
     * Create a new basal event. For ICT it's the injection, for Pumps it's the basal configuration.
     *
     * @param timestamp   Timestamp of the beginning of the basal injection
     * @param rate        Amount of basal insulin per hour
     * @param duration    Duration of the basal rate
     * @param description Description the user provided
     */
    @Ignore
    public BasalEvent(@Source int source, Instant timestamp, double rate, double duration,
                      String description) {
        super(TYPE_BASAL, source, R.drawable.ic_fab_sports, timestamp, rate, null, description);
        this.duration = duration;
    }

    /**
     * Create a new basal event, supplying all fields. This is mainly used by the database to
     * create an event from a table row.
     *
     * @param logEventId Unqiue ID of this event, serves as primary key and is auto-generated
     *                   by the database.
     * @param iconId     Resource ID of an icon that may be displayed for this event
     * @param timestamp  Timestamp of the beginning of the basal injection.
     * @param value      Rate of basal insulin
     * @param note       Description of the basal event
     * @param duration   Duration of basal event
     */
    public BasalEvent(@Source int source, long logEventId, int iconId, Instant timestamp,
                      double value, Bitmap picture, String note, double duration) {
        super(TYPE_BASAL, source, logEventId, iconId, timestamp, value, picture, note);
        this.duration = duration;
    }

    @Override
    public void createLayout(Context context, RelativeLayout root, boolean isSelected) {
        //TODO implement!
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

        contentV.setText(context.getString(R.string.ms_event_minutes, value));
        noteV.setText(note);
    }
}

