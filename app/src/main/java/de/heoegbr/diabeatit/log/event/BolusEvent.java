package de.heoegbr.diabeatit.log.event;

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
import de.heoegbr.diabeatit.log.LogEvent;

/**
 * Represents an entry of a manual bolus event.
 */
@Entity
public class BolusEvent extends LogEvent {

	/** Amount administered in international units */
	@ColumnInfo(name = "bolus")
	public final double BOLUS;
	/** Optional note that was supplied */
	@ColumnInfo(name = "note")
	public final String NOTE;

	/**
	 * Create a new bolus event
	 * @param timestamp	Timestamp of administration
	 * @param bolus		Amount of insulin administered in international units.
	 * @param note		Optional note supplied
	 */
	public BolusEvent(Instant timestamp, double bolus, String note) {

		super(R.string.mi_event_title, R.drawable.ic_fab_insulin, timestamp);

		BOLUS = bolus;
		NOTE = note;

	}

	/**
	 * 	Create a new bolus event. Mainly used to create the object from the database
	 * @param logEventId	Unique ID of this event, serves as primary key and is auto generated
	 * @param TITLE			Title of this event
	 * @param ICON			Resource ID of an icon that may be dispalyed for this event
	 * @param TIMESTAMP		Timestamp of administration of the bolus
	 * @param BOLUS			Amount of insulin administered in international units
	 * @param NOTE			User supplied optional note
	 */
	public BolusEvent(long logEventId, int TITLE, int ICON, Instant TIMESTAMP, double BOLUS, String NOTE) {
		super(logEventId, TITLE, ICON, TIMESTAMP);
		this.BOLUS = BOLUS;
		this.NOTE = NOTE;
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
		noteV.setVisibility(!NOTE.isEmpty() ? View.VISIBLE : View.GONE);
		imgV.setVisibility(View.GONE);

		root.setBackgroundResource(isSelected ? R.drawable.log_event_selected_background : R.drawable.log_event_background);

		contentV.setText(BOLUS + " IE");
		noteV.setText(NOTE);

	}

}