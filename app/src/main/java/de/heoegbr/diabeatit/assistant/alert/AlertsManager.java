package de.heoegbr.diabeatit.assistant.alert;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.db.container.Alert;
import de.heoegbr.diabeatit.db.repository.AlertStore;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class AlertsManager {

    private final AlertStore mAlertStore;
    private List<Alert> alerts = new ArrayList<>();

    public AlertsManager(Context context, RecyclerView recycler, View alertView) {
        mAlertStore = AlertStore.getRepository(context);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        AlertAdapter alertAdapter = new AlertAdapter(context, alerts);

        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(alertAdapter);
        recycler.setItemAnimator(new SlideInLeftAnimator());
        new ItemTouchHelper(new SwipeToDismissCallback(context, alertAdapter, alertView)).attachToRecyclerView(recycler);

        mAlertStore.attachListener(new AlertStoreListener() {
            @Override
            public void onNewAlert(Alert alert) {
                if (alerts.contains(alert))
                    return;

                alerts.add(alert);
                sort();

                alertAdapter.notifyItemInserted(alerts.indexOf(alert));
            }

            @Override
            public void onAlertDismissed(Alert alert) {
                if (!alerts.contains(alert))
                    return;

                int index = alerts.indexOf(alert);
                alerts.remove(index);

                alertAdapter.notifyItemRemoved(index);
            }

            @Override
            public void onAlertRestored(Alert alert) {
                onNewAlert(alert);
            }

            @Override
            public void onAlertsCleared() {
            }

            @Override
            public void onDataSetInit() {
                alerts.clear();
                alerts.addAll(mAlertStore.getActiveAlerts());
                sort();

                alertAdapter.notifyDataSetChanged();
            }
        });
    }

    private void sort() {
        alerts.sort((alert0, alert1) -> alert1.urgency.getPriority() - alert0.urgency.getPriority());
    }

}

class SwipeToDismissCallback extends ItemTouchHelper.SimpleCallback {

    private final Context mContext;

    private AlertAdapter adapter;
    private View alertView;

    private Alert lastRemoved;

    SwipeToDismissCallback(Context context, AlertAdapter adapter, View alertView) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.mContext = context;
        this.adapter = adapter;
        this.alertView = alertView;

    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        Alert toRemove = adapter.alerts.get(viewHolder.getAdapterPosition());
        AlertStore.getRepository(mContext).dismissAlert(toRemove);
        lastRemoved = toRemove;

        showUndoDialog();
    }

    private void showUndoDialog() {
        Snackbar snackbar = Snackbar.make(alertView, R.string.alert_undo_text, Snackbar.LENGTH_LONG);
        ((TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text)).setTextColor(mContext.getColor(android.R.color.white));
        snackbar.setAction(R.string.alert_undo_action, v -> AlertStore.getRepository(mContext).restoreAlert(lastRemoved));
        snackbar.show();
    }

}

class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private final Context mContext;
    List<Alert> alerts;

    AlertAdapter(Context context, List<Alert> alerts) {
        mContext = context;
        this.alerts = alerts;
    }

    @NotNull
    @Override
    public AlertAdapter.AlertViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView card = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.d_assistant_card, parent, false);

        return new AlertViewHolder(card);
    }

    @Override
    public void onBindViewHolder(AlertViewHolder holder, int position) {
        Alert alert = alerts.get(position);

        /* Get view/layout elements */
        CardView card = holder.card;

        TextView labelV = card.findViewById(R.id.card_label);
        ImageView iconV = card.findViewById(R.id.card_icon);
        TextView titleV = card.findViewById(R.id.card_title);
        TextView descV = card.findViewById(R.id.card_description);

        /* Replace label */
        labelV.setText(mContext.getResources().getString(alert.urgency.getStringId()));
        labelV.setBackground(mContext.getDrawable(alert.urgency.getBackground()));

        /* Set icon and text */
        iconV.setImageDrawable(mContext.getDrawable(alert.iconId));
        titleV.setText(alert.title);
        descV.setText(Html.fromHtml(alert.description, Html.FROM_HTML_MODE_COMPACT));
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        CardView card;

        AlertViewHolder(CardView card) {
            super(card);
            this.card = card;
        }
    }

}