package de.heoegbr.diabeatit.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.heoegbr.diabeatit.DiaBEATitApp;
import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.StaticData;
import de.heoegbr.diabeatit.assistant.boluscalculator.BolusCalculatorResult;
import de.heoegbr.diabeatit.assistant.boluscalculator.SimpleBolusCalculator;
import de.heoegbr.diabeatit.data.container.Alert;
import de.heoegbr.diabeatit.data.container.Profil;
import de.heoegbr.diabeatit.data.container.event.BgReadingEvent;
import de.heoegbr.diabeatit.data.repository.AlertStore;
import de.heoegbr.diabeatit.data.repository.DiaryRepository;
import de.heoegbr.diabeatit.ui.home.HomeActivity;

/**
 * Foreground services that allows the app to run continuously without being automatically terminated
 */
public class DontDieForegroundService extends LifecycleService {

    SimpleBolusCalculator bCalc = new SimpleBolusCalculator(Profil.BG_TARGET,
            Profil.INSULIN_SENSITIVITY_FACTOR,
            Profil.INSULIN_CARB_RATIO);
    // TODO model view pattern?
    private LiveData<List<BgReadingEvent>> mBgReadings;

    @Override
    public void onCreate() {
        super.onCreate();
        mBgReadings = DiaryRepository.getRepository(this).getLiveBgEvents();
        mBgReadings.observe(this, bgReadingEvents -> {
            runCalculation(bgReadingEvents);
        });
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
                .setSmallIcon(R.drawable.ic_diabeatit)
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
    public IBinder onBind(@NotNull Intent intent) {
        super.onBind(intent);
        return null;
    }

    // todo this is prototyping code which should be replaced by propper classes (maybe from aaps)
    private void runCalculation(List<BgReadingEvent> bgReadingEvents) {
        DiaryRepository repo = DiaryRepository.getRepository(this);
        //List<DiaryEvent> events = repo.getEvents();

        // calculate Bolus and TODO save to DB
        BgReadingEvent bgEvent = repo.getMostRecentBgEvent();
        double iob = repo.getIOB(Profil.DURATION_OF_INSULIN_ACTIVITY,
                Profil.INSULIN_PEEK_ACTIVITY);
        BolusCalculatorResult bolusCalculatorResult = null;
        if (bgEvent != null && bgEvent.value > 0) {
            bolusCalculatorResult = bCalc.calculateBolus(bgEvent.value,
                    Profil.BG_TARGET, iob, 0, 0);
        }

        // calculate slope
        double lowPassSlope = 0;
        double slope = 0;
        for (int i = bgReadingEvents.size() > 5 ? 5 : bgReadingEvents.size(); i > 0; i--) { // sorted from new to old
            double bgold = bgReadingEvents.get(i).value;
            double bgnew = bgReadingEvents.get(i - 1).value;
            slope = bgnew - bgold;
            lowPassSlope = (lowPassSlope + slope) / 2;
        }

        // analyse
        if ((slope > 10 || lowPassSlope > 10)
                && (bolusCalculatorResult != null && bolusCalculatorResult.getBolus() >= 2.0)
                && bgEvent.value > 180) {
            AlertStore.getRepository(this).newAlert(
                    new Alert(Alert.URGENCY.INFO, R.drawable.ic_cake,
                            "Rise Found",
                            "You should bolus!"));
        }

    }

}