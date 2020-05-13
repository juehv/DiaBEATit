package de.heoegbr.diabeatit.assistant.alert;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.db.container.Alert;
import de.heoegbr.diabeatit.db.repository.AlertStore;

public class DismissedAlertsManager {

  private DismissedAlertAdapter alertAdapter;

  private List<Alert> alerts = new ArrayList<>();

  public DismissedAlertsManager(Context context, RecyclerView recycler) {

	alertAdapter = new DismissedAlertAdapter(context, alerts);

	recycler.setLayoutManager(new LinearLayoutManager(context));
	recycler.setAdapter(alertAdapter);

	AlertStore.attachListener(new AlertStoreListener() {

	  @Override
	  public void onNewAlert(Alert alert) {}

	  @Override
	  public void onAlertDismissed(Alert alert) {

		alerts.add(alert);
		sort();

		alertAdapter.notifyDataSetChanged();

	  }

	  @Override
	  public void onAlertRestored(Alert alert) {

		if (!alerts.contains(alert)) return;

		int index = alerts.indexOf(alert);
		alerts.remove(index);

		alertAdapter.notifyItemRemoved(index);

	  }

	  @Override
	  public void onAlertsCleared() {}

	  @Override
	  public void onDataSetInit() {

		alerts.clear();
		alerts.addAll(Arrays.asList(AlertStore.getDismissedAlerts()));
		sort();

		alertAdapter.notifyDataSetChanged();

	  }

	});

  }

  private void sort() {

	alerts.sort((alert0, alert1) -> alert1.timestamp.compareTo(alert0.timestamp));

  }

}

class DismissedAlertAdapter extends RecyclerView.Adapter<DismissedAlertAdapter.DismissedAlertViewHolder> {

  private final Context CONTEXT;
  private List<Alert> alerts;

  public static class DismissedAlertViewHolder extends RecyclerView.ViewHolder {

	public RelativeLayout view;

	public DismissedAlertViewHolder(RelativeLayout view) {

	  super(view);
	  this.view = view;

	}

  }

  public DismissedAlertAdapter(Context context, List<Alert> alerts) {

	CONTEXT = context;
	this.alerts = alerts;

  }

  @Override
  public DismissedAlertViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

	RelativeLayout view = (RelativeLayout) LayoutInflater.from(parent.getContext())
			.inflate(R.layout.d_alert_history_entry, parent, false);

	return new DismissedAlertViewHolder(view);

  }

  @Override
  public void onBindViewHolder(DismissedAlertViewHolder holder, int position) {

	Alert alert = alerts.get(position);
	RelativeLayout view = holder.view;

	TextView timeV = view.findViewById(R.id.alert_history_entry_time);
	TextView urgencyV = view.findViewById(R.id.alert_history_entry_urgency);
	TextView titleV = view.findViewById(R.id.alert_history_entry_title);
	TextView descV = view.findViewById(R.id.alert_history_entry_description);

	timeV.setText(new SimpleDateFormat("d.M. H:mm").format(alert.timestamp));
	urgencyV.setText(CONTEXT.getResources().getString(alert.URGENCY.getStringId()));
	urgencyV.setTextColor(CONTEXT.getColor(alert.URGENCY.getRawColor()));
	titleV.setText(alert.title);
	descV.setText(Html.fromHtml(alert.desc, Html.FROM_HTML_MODE_COMPACT));

  }

  @Override
  public int getItemCount() { return alerts.size(); }

}