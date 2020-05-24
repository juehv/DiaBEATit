package de.heoegbr.diabeatit.assistant.alert;

import de.heoegbr.diabeatit.data.container.Alert;

public interface AlertStoreListener {

  /**
   * Called when an alert has been added (added to active alerts)
   * @param alert
   */
  void onNewAlert(Alert alert);

  /**
   * Called when an alert has been dismissed (moved to history)
   * @param alert
   */
  void onAlertDismissed(Alert alert);

  /**
   * Called when a previously dismissed alert has been restored to active alerts
   * @param alert
   */
  void onAlertRestored(Alert alert);

  /**
   * Called when there are no active alerts left
   */
  void onAlertsCleared();

  /**
   * Called when the alert list is initialized from disk.
   * Alerts must be re-synced manually
   */
  void onDataSetInit();

}
