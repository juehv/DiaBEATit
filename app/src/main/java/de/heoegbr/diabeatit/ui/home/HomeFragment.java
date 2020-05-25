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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.data.container.event.DiaryEvent;
import de.heoegbr.diabeatit.data.container.event.PredictionEvent;


public class HomeFragment extends Fragment {
    private static final String TAG = "HOME_FRAGMENT";
    private static WeakReference<HomeFragment> instance;

    private HomeViewModel homeViewModel;
    private CombinedChart chart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle _b) {
        View root = inflater.inflate(R.layout.d_fragment_home, container, false);

        chart = root.findViewById(R.id.chart_bg);
        chart.setScaleYEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0.0f);
        chart.getAxisLeft().setAxisMaximum(300.0f);
        chart.getAxisRight().setAxisMinimum(0.0f);
        chart.getAxisRight().setAxisMaximum(300.0f);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);
        chart.getLegend().setDrawInside(true);
        chart.getLegend().setYOffset(90f);
        chart.getLegend().setXOffset(38f);
        //chart.getLegend().setEntries();
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);

        // workaorund for trarget range background
        // https://stackoverflow.com/questions/37406003/mpandroidchart-set-background-between-limit-lines
        float rangeHigh = 180f;
        float rangeLow = 80f;
        float increment = (rangeHigh - rangeLow) / 20;
        float metricLine = rangeLow;

        for (int i = 0; i < 20; i++) {
            LimitLine llRange = new LimitLine(metricLine, "");
            llRange.setLineColor(Color.parseColor("#33b5eb45"));
            llRange.setLineWidth(increment - 1.4f);
            chart.getAxisLeft().addLimitLine(llRange);
            metricLine = metricLine + increment;
        }
        LimitLine hypoLine = new LimitLine(rangeLow - 2f, "");
        hypoLine.setLineColor(Color.parseColor("#888888"));
        hypoLine.setLineWidth(1f);
        chart.getAxisLeft().addLimitLine(hypoLine);
        LimitLine severeHypoLine = new LimitLine(51f, "");
        severeHypoLine.setLineColor(Color.parseColor("#88d63131"));
        severeHypoLine.setLineWidth(1f);
        chart.getAxisLeft().addLimitLine(severeHypoLine);
        LimitLine hiperLine = new LimitLine(rangeHigh - 2f, "");
        hiperLine.setLineColor(Color.parseColor("#888888"));
        hiperLine.setLineWidth(1f);
        chart.getAxisLeft().addLimitLine(hiperLine);
        chart.getAxisLeft().setDrawLimitLinesBehindData(true);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.observeData(getViewLifecycleOwner(), diaryEvents -> {
            if (diaryEvents.isEmpty()) {
                chart.clear();
                chart.invalidate();
                return;
            }

            // recreate chart dataset
            Instant firstVisibleBgDate = Instant.now() //diaryEvents.get(0).timestamp
                    .minus(12, ChronoUnit.HOURS);
            float fistX = diaryEvents.get(0).timestamp.toEpochMilli() - firstVisibleBgDate.toEpochMilli();
            fistX = fistX / 300000;

            List<Entry> bgEntries = new ArrayList<>();
            double biggestBgValue = 0;
            List<Entry> basalEntries = new ArrayList<>();
            List<List<Entry>> predictionEntries = new ArrayList<>();
            List<BarEntry> bolusEntries = new ArrayList<>();
            List<BarEntry> carbEntries = new ArrayList<>();
            for (DiaryEvent item : diaryEvents) {
                if (firstVisibleBgDate.isAfter(item.timestamp)) continue;

                // calculate x position
                long offset = item.timestamp.toEpochMilli() - firstVisibleBgDate.toEpochMilli();
                float x = (float) offset / 300000; // 5 min slots
                switch (item.type) {
                    case DiaryEvent.TYPE_BG:
                        float value = item.value > 430.0 ? 430f : (float) item.value;
                        bgEntries.add(new Entry(x, value));
                        biggestBgValue = item.value > biggestBgValue ? item.value : biggestBgValue;
                        break;
                    case DiaryEvent.TYPE_BASAL:
                        basalEntries.add(new Entry(x, (float) item.value * 10));
                        break;
                    case DiaryEvent.TYPE_BOLUS:
                        bolusEntries.add(new BarEntry(x + 0.25f, (float) item.value * 10));
                        break;
                    case DiaryEvent.TYPE_CARB:
                        carbEntries.add(new BarEntry(x - 0.25f, (float) item.value));
                        break;
                    case DiaryEvent.TYPE_PREDICTION:
                        List<Entry> tmpPrediction = new ArrayList<>();
                        int predCount = 1;
                        for (Double predItem : ((PredictionEvent) item).prediction) {
                            tmpPrediction.add(new Entry(x + predCount, predItem.floatValue()));
                            predCount++;
                        }
                        Collections.sort(tmpPrediction, new EntryXComparator());
                        predictionEntries.add(tmpPrediction);
                        break;
                }
            }
            Collections.sort(bgEntries, new EntryXComparator());
            Collections.sort(basalEntries, new EntryXComparator());
            Collections.sort(bolusEntries, new EntryXComparator());
            Collections.sort(carbEntries, new EntryXComparator());

            LineDataSet bgDataSet = new LineDataSet(bgEntries, "BG");
            bgDataSet.setColor(Color.BLUE);
            bgDataSet.setCircleColor(Color.BLUE);
            bgDataSet.setDrawValues(false);
            LineDataSet basalDataSet = new LineDataSet(basalEntries, "Basal");
            basalDataSet.setColor(Color.parseColor("#f2ae00"));
            basalDataSet.setLineWidth(1.5f);
            basalDataSet.setDrawValues(false);
            basalDataSet.setDrawCircles(false);
            basalDataSet.setMode(LineDataSet.Mode.STEPPED);
            basalDataSet.setDrawFilled(true);
            basalDataSet.setFillColor(Color.parseColor("#ffcc4a"));
            basalDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
            LineData ld = new LineData();
            ld.addDataSet(bgDataSet);
            ld.addDataSet(basalDataSet);
            for (List<Entry> predictionEntry : predictionEntries) {
                LineDataSet predDataSet = new LineDataSet(predictionEntry, "");
                predDataSet.setColor(Color.parseColor("#b60dbf"));
                predDataSet.setCircleColor(Color.parseColor("#b60dbf"));
                predDataSet.setDrawValues(false);
                ld.addDataSet(predDataSet);
            }

            BarDataSet bolusDataSet = new BarDataSet(bolusEntries, "Bolus");
            bolusDataSet.setColor(Color.parseColor("#00BB00"));
            bolusDataSet.setDrawValues(false);
            BarDataSet carbDataSet = new BarDataSet(carbEntries, "Carbs");
            carbDataSet.setColor(Color.RED);
            carbDataSet.setDrawValues(false);
            BarData bd = new BarData();
            bd.addDataSet(bolusDataSet);
            bd.addDataSet(carbDataSet);

            CombinedData combinedData = new CombinedData();
            combinedData.setData(ld);
            combinedData.setData(bd);

            if (biggestBgValue > 295) {
                chart.getAxisLeft().setAxisMaximum(450.0f);
                chart.getAxisRight().setAxisMaximum(450.0f);
            } else {
                chart.getAxisLeft().setAxisMaximum(300.0f);
                chart.getAxisRight().setAxisMaximum(300.0f);
            }

            chart.getXAxis().setValueFormatter(new ValueFormatter() {
                private long startValue = firstVisibleBgDate.toEpochMilli();

                @Override
                public String getFormattedValue(float value) {
                    long rawReturnValue = startValue + Math.round(value * 300000);
                    return SimpleDateFormat.getTimeInstance(DateFormat.SHORT)
                            .format(new Date(rawReturnValue));
                }
            });
            chart.zoom(1.25f, 1f, fistX, 180);
            chart.moveViewToX(fistX);


            LimitLine predictionLine = new LimitLine(fistX + 0.2f, "-->");
            predictionLine.setLineColor(Color.parseColor("#707070"));
            predictionLine.setLineWidth(1.5f);
            predictionLine.enableDashedLine(15f, 5f, 0f);
            chart.getXAxis().removeAllLimitLines();
            chart.getXAxis().addLimitLine(predictionLine);

            chart.setData(combinedData);
            chart.invalidate();
            Log.d(TAG, "updated main chart");
        });

        return root;
    }


}
