package de.heoegbr.diabeatit.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.db.container.event.DiaryEvent;
import de.heoegbr.diabeatit.db.repository.DiaryEventStore;

public class DiaryActivity extends AppCompatActivity implements DiaryEventAdapter.LogEventViewHolder.ClickListener {

    private DiaryEventAdapter adapter;

    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_activity_log);

        getSupportActionBar().setTitle(getResources().getString(R.string.nav_navigation_log));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        DiaryEventStore diaryEventStore = DiaryEventStore.getRepository(getApplicationContext());
        adapter = new DiaryEventAdapter(this, this, diaryEventStore.getEvents());

        RecyclerView recycler = findViewById(R.id.event_log_layout);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setAdapter(adapter);

        findViewById(R.id.event_log_empty_notice).setVisibility(adapter.events.isEmpty() ? View.VISIBLE : View.GONE);

        diaryEventStore.attachListener(this::change);
    }

    private void change(DiaryEvent... e) {
        if (e.length != 0) {
            adapter.events.addAll(Arrays.asList(e));
            adapter.events.sort((a, b) -> b.timestamp.compareTo(a.timestamp));
        }

        findViewById(R.id.event_log_empty_notice).setVisibility(adapter.events.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClicked(int position) {
        if (actionMode != null)
            toggleSelection(position);
        //else
        //    adapter.removeItem(position);
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }

        toggleSelection(position);

        return true;
    }

    /**
     * Toggle the selection state of an item.
     * <p>
     * If the item was the last one in the selection and is unselected, the
     * selection is stopped.
     * Note that the selection must already be started (actionMode must not be
     * null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private void toggleSelection(int position) {
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemCount();

        if (count == 0)
            actionMode.finish();
        else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.log_hide, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelection();
            actionMode = null;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.log_hide_item:
                    adapter.removeItems(adapter.getSelectedItems());
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }
    }
}

/* Code snippets from https://enoent.fr/posts/recyclerview-basics/ */
abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private SparseBooleanArray selectedItems;

    public SelectableAdapter() {
        selectedItems = new SparseBooleanArray();
    }

    /**
     * Indicates if the item at position position is selected
     *
     * @param position Position of the item to check
     * @return true if the item is selected, false otherwise
     */
    public boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }

    /**
     * Toggle the selection status of the item at a given position
     *
     * @param position Position of the item to toggle the selection status for
     */
    public void toggleSelection(int position) {
        if (selectedItems.get(position, false))
            selectedItems.delete(position);
        else
            selectedItems.put(position, true);

        notifyItemChanged(position);
    }

    /**
     * Clear the selection status for all items
     */
    public void clearSelection() {
        List<Integer> selection = getSelectedItems();
        selectedItems.clear();

        for (Integer i : selection)
            notifyItemChanged(i);
    }

    /**
     * Count the selected items
     *
     * @return Selected items count
     */
    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    /**
     * Indicates the list of selected items
     *
     * @return List of selected items ids
     */
    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); ++i)
            items.add(selectedItems.keyAt(i));

        return items;
    }
}

class DiaryEventAdapter extends SelectableAdapter<DiaryEventAdapter.LogEventViewHolder> {

    private final Context mContext;
    public List<DiaryEvent> events;

    private LogEventViewHolder.ClickListener clickListener;

    public DiaryEventAdapter(Context context, LogEventViewHolder.ClickListener clickListener,
                             List<DiaryEvent> events) {
        mContext = context;
        this.clickListener = clickListener;
        this.events = events;
    }

    public void removeItem(int position) {
        if (multi_mode) {
            DiaryEventStore.getRepository(mContext).removeEvent(events.get(position));
            events.remove(position);
            notifyDataSetChanged();

            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        builder.setTitle(mContext.getString(R.string.event_log_hide_alert_title));
        builder.setMessage(mContext.getString(R.string.event_log_hide_alert_message));
        builder.setPositiveButton(mContext.getString(R.string.event_log_hide_alert_ok), (dialogInterface, i) -> {
            DiaryEventStore.getRepository(mContext).removeEvent(events.get(position));
            events.remove(position);
            notifyDataSetChanged();
        });

        builder.setNegativeButton(mContext.getString(R.string.event_log_hide_alert_cancel), (dialogInterface, i) -> {
        });

        builder.create().show();
    }

    private boolean multi_mode;

    @Override
    public void onBindViewHolder(LogEventViewHolder holder, int position) {
        DiaryEvent event = events.get(position);
        event.createLayout(mContext, holder.view, isSelected(position));

        Log.i("LOGAC", isSelected(position) ? "vis" : "invis");
    }

    public void removeItems(List<Integer> positions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        builder.setTitle(mContext.getString(R.string.event_log_hide_alert_title));
        builder.setMessage(mContext.getString(R.string.event_log_hide_alert_message));
        builder.setPositiveButton(mContext.getString(R.string.event_log_hide_alert_ok), (dialogInterface, i1) -> {
            multi_mode = true;
            removeItemsUnsafe(positions);
            multi_mode = false;
        });

        builder.setNegativeButton(mContext.getString(R.string.event_log_hide_alert_cancel), (dialogInterface, i) -> {
        });

        builder.create().show();
    }

    private void removeItemsUnsafe(List<Integer> positions) {
        // Reverse-sort the list
        Collections.sort(positions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });

        // Split the list in ranges
        while (!positions.isEmpty()) {
            if (positions.size() == 1) {
                removeItem(positions.get(0));
                positions.remove(0);
            } else {
                int count = 1;
                while (positions.size() > count && positions.get(count).equals(positions.get(count - 1) - 1)) {
                    ++count;
                }

                if (count == 1) {
                    removeItem(positions.get(0));
                } else {
                    removeRange(positions.get(count - 1), count);
                }

                for (int i = 0; i < count; ++i) {
                    positions.remove(0);
                }
            }
        }
    }

    private void removeRange(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            removeItem(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    @Override
    public LogEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout view = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.d_log_event, parent, false);

        return new LogEventViewHolder(view, clickListener);
    }

    public static class LogEventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public RelativeLayout view;

        private ClickListener listener;

        public LogEventViewHolder(RelativeLayout view, ClickListener listener) {
            super(view);
            this.view = view;

            this.listener = listener;

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null)
                listener.onItemClicked(getPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null)
                return listener.onItemLongClicked(getPosition());

            return false;
        }

        public interface ClickListener {
            void onItemClicked(int position);

            boolean onItemLongClicked(int position);
        }

    }

    @Override
    public int getItemCount() {
        return events.size();
    }

}