package de.heoegbr.diabeatit.service;

import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleService;

import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.StaticData;
import de.heoegbr.diabeatit.db.container.Alert;
import de.heoegbr.diabeatit.ui.NotificationHelper;

/**
 * Foreground services that allows the app to run continuously without being automatically terminated
 */
public class ForegroundService extends LifecycleService {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		// FIXME REWRITE
//		Intent notificationIntent = new Intent(this, MainActivity.class);
//		PendingIntent pendingIntent = PendingIntent.getActivity(this,
//				0, notificationIntent, 0);
//
//		// TODO make notification useful (last broadcast, last received bg reading ...)
//		Notification notification = new NotificationCompat.Builder(this, GlucoProxApp.CHANNEL_ID)
//				.setContentTitle("GlucoProxBLE")
//				.setContentText("Service is running.")
//				.setContentIntent(pendingIntent)
//				.setSmallIcon(R.drawable.ic_service)
//				.build();
//
//		startForeground(1, notification);

		Alert alert = new Alert(Alert.URGENCY.INFO, R.drawable.ic_cake, "Diabeatit running", "The diabeatit service is online.");

		NotificationHelper.createChannel("service", "Keep-Alive", "Foreground Service Notification", android.app.NotificationManager.IMPORTANCE_MIN);
		startForeground(StaticData.FOREGROUND_SERVICE_ID, NotificationHelper.createNotification("service", alert, false));

		return START_NOT_STICKY;

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		super.onBind(intent);
		return null;
	}

}