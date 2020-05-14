package de.heoegbr.diabeatit.db.container.event;

import android.content.Context;
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
 * Event representing an exercise event
 */
@Entity
public class SportsEvent extends DiaryEvent {

	/** Duration of the exercise */
	@ColumnInfo(name = "duration")
	public final int duration;
	/** Description by the user */
	@ColumnInfo(name = "description")
	public final String description;

	/**
	 * Create a new sports event, supplying all fields. This is mainly used by the database to
	 * create an event from a table row.
	 * @param logEventId		Unqiue ID of this event, serves as primary key and is auto-generated
	 *                          by the database.
	 * @param title                Title of this event
	 * @param iconId                Resource ID of an icon that may be displayed for this event
	 * @param timestamp            Timestamp of the beginning of the exercise.
	 * @param duration            Duration of the exercise
	 * @param description        Description of the exercise
	 */
	public SportsEvent(long logEventId, int title, int iconId, Instant timestamp, int duration, String description) {
		super(TYPE.SPORT, logEventId, title, iconId, timestamp);
		this.duration = duration;
		this.description = description;
	}

	/**
	 * Create a new sports event
	 * @param timestamp		Timestamp of the beginning of the exercise
	 * @param duration		Duration of the exercise
	 * @param description	Description the user provided
	 */
	public SportsEvent(Instant timestamp, int duration, String description) {
		super(TYPE.SPORT, R.string.ms_event_title, R.drawable.ic_fab_sports, timestamp);
		this.duration = duration;
		this.description = description;
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
		noteV.setVisibility(View.VISIBLE);
		imgV.setVisibility(View.GONE);

		root.setBackgroundResource(isSelected ? R.drawable.log_event_selected_background : R.drawable.log_event_background);

		contentV.setText(context.getString(R.string.ms_event_minutes, duration));
		noteV.setText(description);
	}

}