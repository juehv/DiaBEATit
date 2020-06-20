package de.heoegbr.diabeatit.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
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
    private TargetZoneCombinedChart chart;
    private int mBgColor;
    private int mBolusColor;
    private int mBasalColor;
    private int mBasalFillColor;
    private int mCarbsColor;
    private int mPredictionColor;
    private int mPredictionMarkerColor;
    private List<LegendEntry> mLegendEntries;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle _b) {
        View root = inflater.inflate(R.layout.d_fragment_home, container, false);

        chart = root.findViewById(R.id.chart_bg);
        mBgColor = Color.parseColor(getString(R.string.chart_color_bg));
        mBolusColor = Color.parseColor(getString(R.string.chart_color_bolus));
        mBasalColor = Color.parseColor(getString(R.string.chart_color_basal));
        mBasalFillColor = Color.parseColor(getString(R.string.chart_color_basal_fill));
        mCarbsColor = Color.parseColor(getString(R.string.chart_color_carbs));
        mPredictionColor = Color.parseColor(getString(R.string.chart_color_prediction));
        mPredictionMarkerColor = Color.parseColor(getString(R.string.chart_color_prediction_marker));
        mLegendEntries = new ArrayList<>();
        setupChart();

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.observeData(getViewLifecycleOwner(), diaryEvents -> {
            updateChart(diaryEvents);
            Log.d(TAG, "updated main chart");
        });

        return root;
    }

    private void updateChart(List<DiaryEvent> diaryEvents) {
        // make sure data is not empty
        if (diaryEvents.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }

        // recreate chart dataset:
        // calculate date of fist visible entry from left
        Instant firstVisibleBgDate = Instant.now()
                .minus(12, ChronoUnit.HOURS);
        // calculate x value of newest bg value
        float lastEntryX = diaryEvents.get(0).timestamp.toEpochMilli() - firstVisibleBgDate.toEpochMilli();
        lastEntryX = lastEntryX / 300000;

        // prepare data lists
        List<Entry> bgEntries = new ArrayList<>();
        double biggestBgValue = 0;
        List<Entry> basalEntries = new ArrayList<>();
        List<List<Entry>> predictionEntries = new ArrayList<>();
        List<List<Entry>> predictionSimulationEntries = new ArrayList<>();
        List<BarEntry> bolusEntries = new ArrayList<>();
        List<BarEntry> carbEntries = new ArrayList<>();
        List<List<BarEntry>> generatedCarbEntries = new ArrayList<>();
        List<List<BarEntry>> generatedIsfEntries = new ArrayList<>();
        for (DiaryEvent item : diaryEvents) {
            // filter values older than 12h
            if (firstVisibleBgDate.isAfter(item.timestamp)) continue;

            // calculate x position
            long offset = item.timestamp.toEpochMilli() - firstVisibleBgDate.toEpochMilli();
            float x = (float) offset / 300000; // 5 min slots
            // split data types
            switch (item.type) {
                case DiaryEvent.TYPE_BG:
                    // limit bg to 430 .. higher values are out of sensor range anyway
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
                case DiaryEvent.TYPE_MEAL:
                    carbEntries.add(new BarEntry(x - 0.25f, (float) item.value));
                    break;
                case DiaryEvent.TYPE_PREDICTION:
                    // Prediction entries are a complete dataset on its own
                    // --> create one dataset per entry
                    PredictionEvent pEvent = (PredictionEvent) item;
                    // carbs
                    if (pEvent.carbSimulation != null) {
                        List<BarEntry> tmpGenCarbEntries = new ArrayList<>();
                        int simCount = pEvent.carbSimulation.size();
                        for (Double simItem : pEvent.carbSimulation) {
                            tmpGenCarbEntries.add(new BarEntry(x - simCount--, simItem.floatValue() * 10));
                        }
                        Collections.sort(tmpGenCarbEntries, new EntryXComparator());
                        generatedCarbEntries.add(tmpGenCarbEntries);
                    }
                    // ISF
                    if (pEvent.isfSimulation != null) {
                        List<BarEntry> tmpIsfCarbEntries = new ArrayList<>();
                        int simCount = pEvent.isfSimulation.size();
                        for (Double simItem : pEvent.isfSimulation) {
                            tmpIsfCarbEntries.add(new BarEntry(x - simCount--, simItem.floatValue() * 50));
                        }
                        Collections.sort(tmpIsfCarbEntries, new EntryXComparator());
                        generatedIsfEntries.add(tmpIsfCarbEntries);
                    }
                    // cgm
                    if (pEvent.cgmSimulation != null) {
                        List<Entry> tmpSimulation = new ArrayList<>();
                        int predCount = 1;
                        int simCount = pEvent.cgmSimulation.size();
                        for (Double simItem : pEvent.cgmSimulation) {
                            tmpSimulation.add(new Entry(x - simCount--, simItem.floatValue()));
                        }
                        if (!tmpSimulation.isEmpty()) {
                            Collections.sort(tmpSimulation, new EntryXComparator());
                            predictionSimulationEntries.add(tmpSimulation);
                        }
                    }
                    if (pEvent.prediction != null) {
                        List<Entry> tmpPrediction = new ArrayList<>();
                        int predCount = 1;
                        for (Double predItem : ((PredictionEvent) item).prediction) {
                            tmpPrediction.add(new Entry(x + predCount, predItem.floatValue()));
                            predCount++;
                        }
                        if (!tmpPrediction.isEmpty()) {
                            Collections.sort(tmpPrediction, new EntryXComparator());
                            predictionEntries.add(tmpPrediction);
                        }
                    }
                    break;
            }
        }


        // sort x entries because chart will crash otherwise
        Collections.sort(bgEntries, new EntryXComparator());
        Collections.sort(basalEntries, new EntryXComparator());
        Collections.sort(bolusEntries, new EntryXComparator());
        Collections.sort(carbEntries, new EntryXComparator());

        // create datasets from entry lists
        LineDataSet bgDataSet = new LineDataSet(bgEntries, getString(R.string.chart_label_bg));
        bgDataSet.setColor(mBgColor);
        bgDataSet.setCircleColor(mBgColor);
        bgDataSet.setDrawValues(false);

        LineDataSet basalDataSet = new LineDataSet(basalEntries, getString(R.string.chart_label_basal));
        basalDataSet.setColor(mBasalColor);
        basalDataSet.setLineWidth(1.5f);
        basalDataSet.setDrawValues(false);
        basalDataSet.setDrawCircles(false);
        basalDataSet.setMode(LineDataSet.Mode.STEPPED);
        basalDataSet.setDrawFilled(true);
        basalDataSet.setFillColor(mBasalFillColor);
        basalDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        LineData ld = new LineData();
        ld.addDataSet(bgDataSet);
        ld.addDataSet(basalDataSet);

        if (!predictionEntries.isEmpty()) {
            for (List<Entry> predictionEntry : predictionEntries) {
                LineDataSet predDataSet = new LineDataSet(predictionEntry, "");
                predDataSet.setColor(mPredictionColor);
                predDataSet.setCircleColor(mPredictionColor);
                predDataSet.setDrawValues(false);
                ld.addDataSet(predDataSet);
            }
        }

        BarDataSet bolusDataSet = new BarDataSet(bolusEntries, getString(R.string.chart_label_bolus));
        bolusDataSet.setColor(mBolusColor);
        bolusDataSet.setDrawValues(false);

        BarDataSet carbDataSet = new BarDataSet(carbEntries, getString(R.string.chart_label_carbs));
        carbDataSet.setColor(mCarbsColor);
        carbDataSet.setDrawValues(false);

        BarData bd = new BarData();
        bd.addDataSet(bolusDataSet);
        bd.addDataSet(carbDataSet);

        // plot prediction data if activated
        if (PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("settings_general_plot_debug", false)) {
            if (!predictionSimulationEntries.isEmpty()) {
                for (List<Entry> simulationEntrie : predictionSimulationEntries) {
                    LineDataSet predDataSet = new LineDataSet(simulationEntrie, "");
                    predDataSet.setColor(Color.parseColor("#de2323"));
//                    predDataSet.setCircleColor(mPredictionColor);
                    predDataSet.setDrawCircles(false);
                    predDataSet.setLineWidth(4f);
                    predDataSet.setDrawValues(false);
                    ld.addDataSet(predDataSet);
                }
            }

            if (!generatedCarbEntries.isEmpty()) {
                for (List<BarEntry> generatedCarbEntry : generatedCarbEntries) {
                    BarDataSet predDataSet = new BarDataSet(generatedCarbEntry, "");
                    predDataSet.setColor(Color.parseColor("#88681299"));
                    predDataSet.setDrawValues(false);
                    bd.addDataSet(predDataSet);
                }
            }

            if (!generatedIsfEntries.isEmpty()) {
                for (List<BarEntry> generatedIsfEntry : generatedIsfEntries) {
                    BarDataSet predDataSet = new BarDataSet(generatedIsfEntry, "");
                    predDataSet.setColor(Color.parseColor("#441aa123"));
                    predDataSet.setDrawValues(false);
                    bd.addDataSet(predDataSet);
                }
            }

        }

        // combine datasets
        CombinedData combinedData = new CombinedData();
        combinedData.setData(ld);
        combinedData.setData(bd);

        // calculate y axis max value
        if (biggestBgValue > 295) {
            chart.getAxisLeft().setAxisMaximum(450.0f);
            chart.getAxisRight().setAxisMaximum(450.0f);
        } else {
            chart.getAxisLeft().setAxisMaximum(300.0f);
            chart.getAxisRight().setAxisMaximum(300.0f);
        }

        // set formatter for x axis (timestamps)
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                long rawReturnValue = firstVisibleBgDate.toEpochMilli() + Math.round(value * 300000);
                return SimpleDateFormat.getTimeInstance(DateFormat.SHORT)
                        .format(new Date(rawReturnValue));
            }
        });

        // set prediction marker
        chart.getXAxis().removeAllLimitLines();
        if (!predictionEntries.isEmpty()) {
            LimitLine predictionLine = new LimitLine(lastEntryX + 0.5f, "-->");
            predictionLine.setLineColor(mPredictionMarkerColor);
            predictionLine.setLineWidth(1.5f);
            predictionLine.enableDashedLine(15f, 5f, 0f);
            chart.getXAxis().addLimitLine(predictionLine);
        }

        // update chart data
        //chart.resetZoom();
        chart.setData(combinedData);
        //chart.invalidate(); // <-- this is called by modeView

        // reset legend
        chart.getLegend().setEntries(mLegendEntries);

        // zoom to current time frame
        // todo calculate zoom
        //chart.zoom(1.2f, 1f, 0, 160, YAxis.AxisDependency.LEFT);
        chart.moveViewToX(lastEntryX - 5f);
    }

    private void setupChart() {
        // make y axis fixed to reasonable values
        chart.setScaleYEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0.0f);
        chart.getAxisLeft().setAxisMaximum(300.0f);
        chart.getAxisRight().setAxisMinimum(0.0f);
        chart.getAxisRight().setAxisMaximum(300.0f);

        // setup x axis
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        // setup legend
        chart.getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);
        chart.getLegend().setDrawInside(true);
        chart.getLegend().setYOffset(85f);
        chart.getLegend().setXOffset(36f);

        mLegendEntries.add(new LegendEntry(getString(R.string.chart_label_bg), Legend.LegendForm.DEFAULT,
                8f, 1f, null, mBgColor));
        mLegendEntries.add(new LegendEntry(getString(R.string.chart_label_basal), Legend.LegendForm.DEFAULT,
                8f, 1f, null, mBasalColor));
        mLegendEntries.add(new LegendEntry(getString(R.string.chart_label_bolus), Legend.LegendForm.DEFAULT,
                8f, 1f, null, mBolusColor));
        mLegendEntries.add(new LegendEntry(getString(R.string.chart_label_carbs), Legend.LegendForm.DEFAULT,
                8f, 1f, null, mCarbsColor));
        chart.getLegend().setEntries(mLegendEntries);
        chart.getLegend().setEnabled(false);

        // don't know what description should do...
        chart.getDescription().setEnabled(false);

        float rangeHigh = 180f;
        float rangeLow = 80f;
        chart.addTargetZone(new TargetZoneCombinedChart.TargetZone(
                Color.parseColor(getString(R.string.chart_color_target_zone)),
                rangeLow, rangeHigh));

        // set limit lines for hyper, hypo, and severe hypo
        LimitLine hypoLine = new LimitLine(rangeLow - 2f, "");
        hypoLine.setLineColor(Color.parseColor(getString(R.string.chart_color_target_line)));
        hypoLine.setLineWidth(1f);
        chart.getAxisLeft().addLimitLine(hypoLine);

        LimitLine severeHypoLine = new LimitLine(51f, "");
        severeHypoLine.setLineColor(Color.parseColor(getString(R.string.chart_color_severe_hypo)));
        severeHypoLine.setLineWidth(1f);
        chart.getAxisLeft().addLimitLine(severeHypoLine);

        LimitLine hyperLine = new LimitLine(rangeHigh - 2f, "");
        hyperLine.setLineColor(Color.parseColor(getString(R.string.chart_color_target_line)));
        hyperLine.setLineWidth(1f);
        chart.getAxisLeft().addLimitLine(hyperLine);
        chart.getAxisLeft().setDrawLimitLinesBehindData(true);
    }


}
