package de.heoegbr.diabeatit.db.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import de.heoegbr.diabeatit.assistant.alert.AlertStoreListener;
import de.heoegbr.diabeatit.assistant.alert.AlertsManager;
import de.heoegbr.diabeatit.assistant.alert.DismissedAlertsManager;
import de.heoegbr.diabeatit.db.container.Alert;
import de.heoegbr.diabeatit.db.dao.AlertDao;
import de.heoegbr.diabeatit.db.localdb.DiabeatitDatabase;

public class AlertStore {
    public static final String TAG = "ALERT_STORE";
    public static AlertStore INSTANCE = null;

    //TODO why are they here and not part of the home activity (programming pattern?)
    public DismissedAlertsManager dismissedAlertsManager;
    public AlertsManager alertsManager;

    private List<Alert> alerts = new ArrayList<>();
    private List<AlertStoreListener> listeners = new ArrayList<>();

    private Context mContext;
    private AlertDao mAlertDao;
    private Executor mExecutor = Executors.newSingleThreadExecutor();

    private AlertStore(final Context context) {
        mContext = context;
        DiabeatitDatabase db = DiabeatitDatabase.getDatabase(context);
        mAlertDao = db.alertDao();

        // load existing alerts from the database.
        mExecutor.execute(() -> {
            List<Alert> items = new ArrayList<>();
            items.addAll(mAlertDao.getActive());
            items.addAll(mAlertDao.getDismissedLimited());
            initAlerts(items);
        });
    }

    public static AlertStore getRepository(final Context context) {
        if (INSTANCE == null) {
            synchronized (AlertStore.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AlertStore(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Attaches an {@link AlertStoreListener} to the AlertStore.
     * It will receive an initial {@link AlertStoreListener#onDataSetInit()} call and future data set updates
     *
     * @param listener The listener to add
     */
    public void attachListener(@NonNull AlertStoreListener listener) {
        listeners.add(listener);
        listener.onDataSetInit();
    }

    /**
     * Detaches the given {@link AlertStoreListener} from the AlertStore.
     * It will not receive any further notification calls
     *
     * @param listener The listener to remove
     */
    public void detachListener(AlertStoreListener listener) {
        listeners.remove(listener);
    }

    /**
     * Sets the list of active and dismissed alerts to the given array.
     * This will overwrite all currently stored alerts.
     * Distinction of active/dismissed is made on basis of {@link Alert#active}
     *
     * @param alertBundle The new data set
     */
    private void initAlerts(@NonNull List<Alert> alertBundle) {
        alerts = new ArrayList<>();
        alerts.addAll(alertBundle);

        for (Alert alert : alerts)
            alert.sendNotification(mContext);

        for (AlertStoreListener l : listeners)
            l.onDataSetInit();
    }

    /**
     * Adds the given alert to the list of active alerts. This will notify the listeners via
     * {@link AlertStoreListener#onNewAlert(Alert)}.
     * It's {@link Alert#active} flag will be set to true
     *
     * @param alert The alert to add
     */
    public void newAlert(@NonNull Alert alert) {
        alert.active = true;

        alerts.add(alert);
        alert.sendNotification(mContext);

        for (AlertStoreListener l : listeners)
            l.onNewAlert(alert);

        mExecutor.execute(() -> mAlertDao.insertAll(alert));
    }

    /**
     * This will move the given alert from the active to the dismissed alerts. It will notify the listeners via
     * {@link AlertStoreListener#onAlertDismissed(Alert)}.
     * If the given alert was the last active alert, this will also trigger a notification for
     * {@link AlertStoreListener#onAlertsCleared()}.
     * It's {@link Alert#active} flag will be set to false.
     * If the given alert is not present in the list of active alerts, this call will be ignored
     *
     * @param alert The alert to dismiss
     */
    public void dismissAlert(@NonNull Alert alert) {
        if (!alerts.contains(alert)) return;

        int index = alerts.indexOf(alert);
        alerts.get(index).active = false;
        alert.destroyNotification(mContext);

        updateDatabaseEntry(alert);

        for (AlertStoreListener l : listeners)
            l.onAlertDismissed(alert);

        if (getActiveAlerts().isEmpty())
            for (AlertStoreListener l : listeners)
                l.onAlertsCleared();
    }

    /**
     * This will move the given alert from the dismissed to the active alerts. It will notify the listeners via
     * {@link AlertStoreListener#onAlertRestored(Alert)}.
     * It's {@link Alert#active} flag will be set to false.
     * If the given alert is not present in the list of dismissed alerts, this call will be redirected to
     * {@link #newAlert(Alert)}
     *
     * @param alert The alert to restore
     */
    public void restoreAlert(@NonNull Alert alert) {
        if (!alerts.contains(alert)) {

            newAlert(alert);
            return;

        }

        int index = alerts.indexOf(alert);
        alerts.get(index).active = true;
        alert.sendNotification(mContext);

        updateDatabaseEntry(alert);

        for (AlertStoreListener l : listeners)
            l.onAlertRestored(alert);
    }

    /**
     * This will remove all active alerts by calling {@link #dismissAlert(Alert)} in sequence.
     * These calls will trigger dismissal notifications
     */
    public void clearAlerts() {
        for (Alert alert : getActiveAlerts())
            dismissAlert(alert);
    }

    /**
     * This will return a list of all alerts with the {@link Alert#active} flag set
     *
     * @return all active alerts as an array
     */
    public List<Alert> getActiveAlerts() {
        return alerts.stream().filter(a -> a.active).collect(Collectors.toList());
    }

    /**
     * This will return a list of all alerts with the {@link Alert#active} flag not set
     *
     * @return all dismissed alerts as an array
     */
    public List<Alert> getDismissedAlerts() {
        return alerts.stream().filter(a -> !a.active).collect(Collectors.toList());
    }

    /**
     * Dispatch a thread updating the alert in the database
     *
     * @param alert Alert that needs to be updated
     */
    private void updateDatabaseEntry(Alert alert) {
        mExecutor.execute(() -> mAlertDao.update(alert));
    }

}