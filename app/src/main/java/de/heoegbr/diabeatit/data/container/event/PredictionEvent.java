package de.heoegbr.diabeatit.data.container.event;

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
import java.util.List;
import java.util.Locale;

import de.heoegbr.diabeatit.R;

@Entity
public class PredictionEvent extends DiaryEvent {

    @ColumnInfo(name = "prediction")
    public final List<Double> prediction;

    @ColumnInfo(name = "cgm_sim")
    public final List<Double> cgmSimulation;

    @ColumnInfo(name = "carb_sim")
    public final List<Double> carbSimulation;

    @ColumnInfo(name = "isf_sim")
    public final List<Double> isfSimulation;

    /**
     * Create a new bolus event
     *
     * @param timestamp  Timestamp of administration
     * @param prediction Prediction time series in 5 min steps
     * @param note       Optional note supplied
     */
    @Ignore
    public PredictionEvent(@Source int source, Instant timestamp, String note, List<Double> prediction,
                           List<Double> cgmSimulation, List<Double> carbSimulation, List<Double> isfSimulation) {
        super(TYPE_PREDICTION, source, R.drawable.ic_fab_insulin, timestamp, 0.0, null, note);
        this.prediction = prediction;
        this.cgmSimulation = cgmSimulation;
        this.carbSimulation = carbSimulation;
        this.isfSimulation = isfSimulation;
    }

    /**
     * Create a new bolus event. Mainly used to create the object from the database
     *
     * @param logEventId Unique ID of this event, serves as primary key and is auto generated
     * @param iconId     Resource ID of an icon that may be dispalyed for this event
     * @param timestamp  Timestamp of administration
     * @param value      ignored
     * @param note       User supplied optional note
     */
    public PredictionEvent(@Source int source, long logEventId, int iconId, Instant timestamp,
                           double value, String picturePath, String note, List<Double> prediction,
                           List<Double> cgmSimulation, List<Double> carbSimulation,
                           List<Double> isfSimulation) {
        super(TYPE_PREDICTION, source, logEventId, iconId, timestamp, value, picturePath, note);
        this.prediction = prediction;
        this.cgmSimulation = cgmSimulation;
        this.carbSimulation = carbSimulation;
        this.isfSimulation = isfSimulation;
    }


    @Override
    public void createLayout(Context context, RelativeLayout root, boolean isSelected) {
        TextView titleV = root.findViewById(R.id.log_event_title);
        ImageView iconV = root.findViewById(R.id.log_event_icon);
        TextView timeV = root.findViewById(R.id.log_event_time);
        TextView contentV = root.findViewById(R.id.log_event_content);
        TextView noteV = root.findViewById(R.id.log_event_note);
        ImageView imgV = root.findViewById(R.id.log_event_picture);

        titleV.setText(context.getResources().getString(R.string.diary_pred_event_title));
        iconV.setImageResource(iconId);
        timeV.setText(new SimpleDateFormat("dd.MM.YYYY HH:mm", Locale.GERMAN).format(Date.from(timestamp)));

        contentV.setVisibility(View.VISIBLE);
        if (note == null)
            noteV.setVisibility(View.GONE);
        else {
            noteV.setVisibility(!note.isEmpty() ? View.VISIBLE : View.GONE);
            noteV.setText(note);
        }
        imgV.setVisibility(View.GONE);

        root.setBackgroundResource(isSelected ? R.drawable.log_event_selected_background : R.drawable.log_event_background);

        contentV.setText(context.getResources().getString(R.string.diary_pred_event_text));
        noteV.setText(note);
    }
}
