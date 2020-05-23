package de.heoegbr.diabeatit.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import de.heoegbr.diabeatit.DiaBEATitApp;
import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.StaticData;
import de.heoegbr.diabeatit.ui.home.HomeActivity;

/**
 * Foreground services that allows the app to run continuously without being automatically terminated
 */
public class DontDieForegroundService extends LifecycleService {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		Intent notificationIntent = new Intent(getApplicationContext(), HomeActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // ??
		notificationIntent.setAction(StaticData.ASSISTANT_INTENT_CODE); // opens assistant tap
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, DiaBEATitApp.DEFAULT_NOTIFICAITON_CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.assistant_sheet_no_alerts))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_cake)
                .build();

        startForeground(StaticData.FOREGROUND_SERVICE_ID, notification);

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