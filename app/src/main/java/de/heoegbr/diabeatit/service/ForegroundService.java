package de.heoegbr.diabeatit.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.StaticData;
import de.heoegbr.diabeatit.db.container.Alert;
import de.heoegbr.diabeatit.ui.notification.NotificationStore;

/**
 * Foreground services that allows the app to run continuously without being automatically terminated
 */
public class ForegroundService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Alert alert = new Alert(Alert.Urgency.INFO, R.drawable.ic_cake, "Diabeatit running", "The diabeatit service is online.");

		NotificationStore.createChannel("service", "Keep-Alive", "Foreground Service Notification", NotificationManager.IMPORTANCE_MIN);
		startForeground(StaticData.FOREGROUND_SERVICE_ID, NotificationStore.createNotification("service", alert, false));

		return START_NOT_STICKY;

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}