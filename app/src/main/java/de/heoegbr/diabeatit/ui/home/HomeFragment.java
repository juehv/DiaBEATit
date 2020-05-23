package de.heoegbr.diabeatit.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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
import de.heoegbr.diabeatit.db.container.event.DiaryEvent;


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
        homeViewModel.observeData(getViewLifecycleOwner(), diaryEvents -> {
            if (diaryEvents.isEmpty()) {
                chart.clear();
                chart.invalidate();
                return;
            }

            // recreate chart dataset
            Instant fistVisibleBgDate = diaryEvents.get(0).timestamp
                    .minus(6, ChronoUnit.HOURS);

            List<Entry> bgEntries = new ArrayList<>();
            List<BarEntry> bolusEntries = new ArrayList<>();
            List<BarEntry> carbEntries = new ArrayList<>();
            for (DiaryEvent item : diaryEvents) {
                // calculate x position
                long offset = item.timestamp.toEpochMilli() - fistVisibleBgDate.toEpochMilli();
                if (offset < 0) continue;
                float x = (float) offset / 300000; //
                // 5 min slots

                switch (item.type) {
                    case DiaryEvent.TYPE_BG:
                        bgEntries.add(new Entry(x, (float) item.value));
                        break;
                    case DiaryEvent.TYPE_BOLUS:
                        bolusEntries.add(new BarEntry(x + 0.25f, (float) item.value * 10));
                        break;
                    case DiaryEvent.TYPE_CARB:
                        carbEntries.add(new BarEntry(x - 0.25f, (float) item.value));
                        break;
                }
            }
            Collections.sort(bgEntries, new EntryXComparator());
            Collections.sort(bolusEntries, new EntryXComparator());
            Collections.sort(carbEntries, new EntryXComparator());

            LineDataSet bgDataSet = new LineDataSet(bgEntries, "BG");
            bgDataSet.setColor(Color.BLUE);
            bgDataSet.setCircleColor(Color.BLUE);
            bgDataSet.setValueTextColor(Color.BLUE);
            LineData ld = new LineData();
            ld.addDataSet(bgDataSet);

            BarDataSet bolusDataSet = new BarDataSet(bolusEntries, "Bolus");
            bolusDataSet.setColor(Color.parseColor("#00BB00"));
            bolusDataSet.setValueTextColor(Color.GREEN);
            BarDataSet carbDataSet = new BarDataSet(carbEntries, "Carbs");
            carbDataSet.setColor(Color.RED);
            carbDataSet.setValueTextColor(Color.RED);
            BarData bd = new BarData();
            bd.addDataSet(bolusDataSet);
            bd.addDataSet(carbDataSet);

            CombinedData combinedData = new CombinedData();
            combinedData.setData(ld);
            combinedData.setData(bd);

            chart.setData(combinedData);
            chart.invalidate();
            Log.d(TAG, "updated main chart");
        });

        return root;
    }


}
