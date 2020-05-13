package de.heoegbr.diabeatit.ui.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.Html;

import androidx.core.app.NotificationCompat;

import java.util.HashMap;

import javax.annotation.Nullable;

import de.heoegbr.diabeatit.DiaBEATitApp;
import de.heoegbr.diabeatit.StaticData;
import de.heoegbr.diabeatit.db.container.Alert;
import de.heoegbr.diabeatit.ui.HomeActivity;

/**
 * Manages log events and database connection.
 */
@Deprecated
public class NotificationStore {

	public static final String DEFAULT_CHANNEL_ID = "default";

	private static HashMap<String, NotificationChannel> channels = new HashMap<>();
	private static HashMap<Integer, Alert> activeNotifications = new HashMap<>();

	static {

		createChannel(DEFAULT_CHANNEL_ID, "Default Channel", "Universal Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);

	}

	/**
	 * Creates an Android notification channel. If the channel ID already exists, the process will be aborted.
	 *
	 * @param id Unique name of the channel.
	 * @param name Name for the Android notification settings.
	 * @param description Description for the Android notification settings.
	 * @param importance {@link NotificationManager} IMPORTANCE ordinal.
	 */
	public static void createChannel(String id, String name, String description, int importance) {

		if (channels.containsKey(id)) return;

		NotificationChannel channel = new NotificationChannel(id, name, importance);
		channel.setDescription(description);

		NotificationManager notificationManager = DiaBEATitApp.getContext().getSystemService(NotificationManager.class);
		notificationManager.createNotificationChannel(channel);

		channels.put(id, channel);

	}

	/**
	 * Creates a notification object.
	 *
	 * @param channel Notification channel ID.
	 * @param alert Associated {@link Alert}.
	 * @param autoCancel Flags if the notification should be destroyed on opening it.
	 * @return Notification associated with the given alert.
	 */
	public static Notification createNotification(@Nullable String channel, Alert alert, boolean autoCancel) {

		if (channel == null) channel = DEFAULT_CHANNEL_ID;

		Intent intent = new Intent(DiaBEATitApp.getContext(), HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.setAction(StaticData.ASSISTANT_INTENT_CODE);
		PendingIntent pendingIntent = PendingIntent.getActivity(DiaBEATitApp.getContext(), 0, intent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(DiaBEATitApp.getContext(), channel)
						.setSmallIcon(alert.ICON_ID)
						.setContentTitle(alert.title)
						.setContentText(Html.fromHtml(alert.desc, Html.FROM_HTML_MODE_COMPACT))
						.setPriority(NotificationCompat.PRIORITY_DEFAULT)
						.setAutoCancel(autoCancel)
						.setContentIntent(pendingIntent)
						.setGroup(channel);

		return builder.build();

	}

	/**
	 * Sends a notification for the given alert.
	 *
	 * @param channel Channel ID to use.
	 * @param alert Associated {@link Alert}.
	 * @return Session-unique notification ID.
	 */
	public static int sendNotification(@Nullable String channel, Alert alert) {

		int id = nextId();
		DiaBEATitApp.getContext().getSystemService(NotificationManager.class).notify(id, createNotification(channel, alert, true));

		activeNotifications.put(id, alert);
		return id;

	}

	/**
	 * Destroys the notification with the given ID.
	 *
	 * @param id The notification's ID.
	 */
	public static void removeNotification(int id) {

		DiaBEATitApp.getContext().getSystemService(NotificationManager.class).cancel(id);

	}

	/**
	 * Deletes all notification entries. This does not destroy their notifications first.
	 */
	public static void reset() {

		activeNotifications.clear();

	}

	/**
	 * Returns the next free notification ID.
	 *
	 * @return A free notification ID.
	 */
	private static int nextId() {

		return activeNotifications.keySet().stream().reduce((a, b) -> a > b ? a : b).orElse(1) + 1;

	}

}