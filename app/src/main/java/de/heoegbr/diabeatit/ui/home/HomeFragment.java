package de.heoegbr.diabeatit.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.db.container.event.BgReadingEvent;


public class HomeFragment extends Fragment {
    private static final String TAG = "HOME_FRAGMENT";
    private static WeakReference<HomeFragment> instance;

    private HomeViewModel homeViewModel;
    private CombinedChart chart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle _b) {
        View root = inflater.inflate(R.layout.d_fragment_home, container, false);

        chart = root.findViewById(R.id.chart_bg);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getBgReadings().observe(getViewLifecycleOwner(), bgReadingEvents -> {
            if (bgReadingEvents.isEmpty()) {
                chart.clear();
                chart.invalidate();
                return;
            }

            // recreate chart dataset
            Instant fistVisibleBgDate = bgReadingEvents.get(0).timestamp
                    .minus(6, ChronoUnit.HOURS);

            List<Entry> entries = new ArrayList<>();
            for (BgReadingEvent item : bgReadingEvents) {
                // calculate x position
                long offset = item.timestamp.toEpochMilli() - fistVisibleBgDate.toEpochMilli();
                if (offset < 0) continue;
                float x = (float) offset / 300000; // 5 min slots

                entries.add(new Entry(x, (float) item.value));
            }
            Collections.sort(entries, new EntryXComparator());

            LineDataSet bgDataSet = new LineDataSet(entries, "BG");
            bgDataSet.setColor(R.color.mdtp_red);
            bgDataSet.setValueTextColor(R.color.mdtp_light_gray);
            LineData ld = new LineData();
            ld.addDataSet(bgDataSet);

            CombinedData combinedData = new CombinedData();
            combinedData.setData(ld);
            chart.setData(combinedData);
            chart.invalidate();
            Log.d(TAG, "updated main chart");
        });

        return root;
    }


}
