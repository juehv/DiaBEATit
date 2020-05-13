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
	public final int DURATION;
	/** Description by the user */
	@ColumnInfo(name = "description")
	public final String DESCRIPTION;

	/**
	 * Create a new sports event, supplying all fields. This is mainly used by the database to
	 * create an event from a table row.
	 * @param logEventId		Unqiue ID of this event, serves as primary key and is auto-generated
	 *                          by the database.
	 * @param TITLE				Title of this event
	 * @param ICON				Resource ID of an icon that may be displayed for this event
	 * @param TIMESTAMP			Timestamp of the beginning of the exercise.
	 * @param DURATION			Duration of the exercise
	 * @param DESCRIPTION		Description of the exercise
	 */
	public SportsEvent(long logEventId, int TITLE, int ICON, Instant TIMESTAMP, int DURATION, String DESCRIPTION) {
		super(logEventId, TITLE, ICON, TIMESTAMP);
		this.DURATION = DURATION;
		this.DESCRIPTION = DESCRIPTION;
	}

	/**
	 * Create a new sports event
	 * @param timestamp		Timestamp of the beginning of the exercise
	 * @param duration		Duration of the exercise
	 * @param description	Description the user provided
	 */
	public SportsEvent(Instant timestamp, int duration, String description) {

		super(R.string.ms_event_title, R.drawable.ic_fab_sports, timestamp);

		DURATION = duration;
		DESCRIPTION = description;

	}

	@Override
	public void createLayout(Context context, RelativeLayout root, boolean isSelected) {

		TextView titleV = root.findViewById(R.id.log_event_title);
		ImageView iconV = root.findViewById(R.id.log_event_icon);
		TextView timeV = root.findViewById(R.id.log_event_time);
		TextView contentV = root.findViewById(R.id.log_event_content);
		TextView noteV = root.findViewById(R.id.log_event_note);
		ImageView imgV = root.findViewById(R.id.log_event_picture);

		titleV.setText(TITLE);
		iconV.setImageResource(ICON);
		timeV.setText(new SimpleDateFormat("dd.MM.YYYY HH:mm", Locale.GERMAN).format(Date.from(TIMESTAMP)));

		contentV.setVisibility(View.VISIBLE);
		noteV.setVisibility(View.VISIBLE);
		imgV.setVisibility(View.GONE);

		root.setBackgroundResource(isSelected ? R.drawable.log_event_selected_background : R.drawable.log_event_background);

		contentV.setText(context.getString(R.string.ms_event_minutes, DURATION));
		noteV.setText(DESCRIPTION);

	}

}