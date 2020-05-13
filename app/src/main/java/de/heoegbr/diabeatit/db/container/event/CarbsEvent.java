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
public class CarbsEvent extends DiaryEvent {

	/** Optional, user supplied Image of the meal */
	@ColumnInfo(name = "image")
	public final Bitmap IMAGE;
	/** Amount of carbs taken in, in grams */
	@ColumnInfo(name = "carbs")
	public final int CARBS;
	/** Optional user supplied note */
	@ColumnInfo(name = "notes")
	public final String NOTE;

	/**
	 * Create a new carbs event. This constructor is mainly used to generate an object from the
	 * database.
	 * @param logEventId	Unique ID of this object, used as primary key and auto-generated
	 * @param TITLE			Title for this event
	 * @param ICON			Resource ID of an icon that may be displayed for this event
	 * @param TIMESTAMP		Timestamp when the meal was taken
	 * @param IMAGE			Optional image of the meal
	 * @param CARBS			Amount of carbs in grams
	 * @param NOTE			Optional note
	 */
	public CarbsEvent(long logEventId, int TITLE, int ICON, Instant TIMESTAMP, Bitmap IMAGE, int CARBS, String NOTE) {
		super(logEventId, TITLE, ICON, TIMESTAMP);
		this.IMAGE = IMAGE;
		this.CARBS = CARBS;
		this.NOTE = NOTE;
	}

	/**
	 * Create a new carbs event
	 * @param timestamp		Timestamp when the meal was taken
	 * @param image			Image of the meal (Optional)
	 * @param carbs			Amount of carbs in grams
	 * @param note			Optional note
	 */
	public CarbsEvent(Instant timestamp, Bitmap image, int carbs, String note) {

		super(R.string.mc_event_title, R.drawable.ic_fab_carbs, timestamp);

		IMAGE = image;
		CARBS = carbs;
		NOTE = note;

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
		imgV.setVisibility(IMAGE != null ? View.VISIBLE : View.GONE);

		root.setBackgroundResource(isSelected ? R.drawable.log_event_selected_background : R.drawable.log_event_background);

		contentV.setText(CARBS + "g");
		noteV.setText(NOTE);

		if (IMAGE != null)
			imgV.setImageBitmap(IMAGE);

	}

}