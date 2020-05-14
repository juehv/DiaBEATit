package de.heoegbr.diabeatit.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.assistant.alert.AlertStoreListener;
import de.heoegbr.diabeatit.assistant.alert.DismissedAlertsManager;
import de.heoegbr.diabeatit.db.container.Alert;
import de.heoegbr.diabeatit.db.repository.AlertStore;

public class AlertHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_activity_alert_history);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_alert_history));

        setTheme(R.style.diabeatit);

        AlertStore alertStore = AlertStore.getRepository(getApplicationContext());
        alertStore.dismissedAlertsManager = new DismissedAlertsManager(getApplicationContext(),
                findViewById(R.id.alert_history_layout));

        TextView dismissedEmptyT = findViewById(R.id.alert_history_empty_notice);

        alertStore.attachListener(new AlertStoreListener() {
            @Override
            public void onNewAlert(Alert alert) {
            }

            @Override
            public void onAlertDismissed(Alert alert) {
                dismissedEmptyT.setVisibility(View.GONE);
            }

            @Override
            public void onAlertRestored(Alert alert) {
                if (alertStore.getDismissedAlerts().isEmpty())
                    dismissedEmptyT.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAlertsCleared() {
            }

            @Override
            public void onDataSetInit() {
                dismissedEmptyT.setVisibility(
                        alertStore.getDismissedAlerts().isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}