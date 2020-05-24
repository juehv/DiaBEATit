package de.heoegbr.diabeatit.data.container.event;

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
public class BgReadingEvent extends DiaryEvent {

    /**
     * Create a new bolus event
     *
     * @param timestamp Timestamp of administration
     * @param bg        Amount of insulin administered in international units.
     * @param note      Optional note supplied
     */
    @Ignore
    public BgReadingEvent(@Source int source, Instant timestamp, double bg, String note) {
        super(TYPE_BG, source, R.drawable.ic_fab_insulin, timestamp, bg, null, note);
    }

    /**
     * Create a new bolus event. Mainly used to create the object from the database
     *
     * @param logEventId Unique ID of this event, serves as primary key and is auto generated
     * @param iconId     Resource ID of an icon that may be dispalyed for this event
     * @param timestamp  Timestamp of administration of the bolus
     * @param value      Amount of insulin administered in international units
     * @param note       User supplied optional note
     */
    public BgReadingEvent(@Source int source, long logEventId, int iconId, Instant timestamp,
                          double value, Bitmap picture, String note) {
        super(TYPE_BG, source, logEventId, iconId, timestamp, value, picture, note);
    }

    @Override
    public void createLayout(Context context, RelativeLayout root, boolean isSelected) {
        TextView titleV = root.findViewById(R.id.log_event_title);
        ImageView iconV = root.findViewById(R.id.log_event_icon);
        TextView timeV = root.findViewById(R.id.log_event_time);
        TextView contentV = root.findViewById(R.id.log_event_content);
        TextView noteV = root.findViewById(R.id.log_event_note);
        ImageView imgV = root.findViewById(R.id.log_event_picture);

        titleV.setText(context.getResources().getString(R.string.mg_event_title));
        iconV.setImageResource(iconId);
        timeV.setText(new SimpleDateFormat("dd.MM.YYYY HH:mm", Locale.GERMAN).format(Date.from(timestamp)));

        contentV.setVisibility(View.VISIBLE);
        noteV.setVisibility(!note.isEmpty() ? View.VISIBLE : View.GONE);
        imgV.setVisibility(View.GONE);

        root.setBackgroundResource(isSelected ? R.drawable.log_event_selected_background : R.drawable.log_event_background);

        contentV.setText(value + " IE");
        noteV.setText(note);
    }
}
