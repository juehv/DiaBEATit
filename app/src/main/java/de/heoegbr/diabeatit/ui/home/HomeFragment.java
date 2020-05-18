package de.heoegbr.diabeatit.ui.home;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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

    private int numberOfLines = 1;
    private int maxNumberOfLines = 4;
    private int numberOfPoints = 12;

    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Nullable
    @Deprecated
    public static HomeFragment getInstance() {
        return instance.get();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle _b) {
        View root = inflater.inflate(R.layout.d_fragment_home, container, false);

        chart = root.findViewById(R.id.chart_bg);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getBgReadings().observe(getViewLifecycleOwner(), new Observer<List<BgReadingEvent>>() {
            @Override
            public void onChanged(List<BgReadingEvent> bgReadingEvents) {
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
            }
        });

        return root;
    }

    private void prefsChanged(SharedPreferences prefs, String key) {
        Log.d("PREF", String.format("Updated prefs w/ key %s", key));
        switch (key) {
//            case PredictionsPlugin.PREF_KEY_AI_MODEL_PATH:
//            case PredictionsPlugin.PREF_KEY_MODEL_TYPE:
//                PredictionsPlugin.updateFromSettings();
//                scheduleUpdateGUI("Preferences updated");
//                Log.i("PREF", "SharedPreferences updated.");
//                break;
            default:
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    public void updateGUI(final String from) {
//        if (L.isEnabled(L.OVERVIEW))
        Log.d(TAG, "updateGUI entered from: " + from);
        final long updateGUIStart = System.currentTimeMillis();

        if (getActivity() == null)
            return;

//        BgReading actualBG = DatabaseHelper.actualBg();
//        BgReading lastBG = DatabaseHelper.lastBg();
//
//        //final PumpInterface pump = ConfigBuilderPlugin.getPlugin().getActivePump();
//
//        final String units = ProfileFunctions.getInstance().getProfileUnits();
//        final double lowLine = 30.0;// OverviewPlugin.INSTANCE.determineLowLine(units);
//        final double highLine = 300.0; //OverviewPlugin.INSTANCE.determineHighLine(units);
//
//        //Constraint<Boolean> closedLoopEnabled = MainApp.getConstraintChecker().isClosedLoopAllowed();
//
//        // iob
//        TreatmentsPlugin.getPlugin().updateTotalIOBTreatments();
//        TreatmentsPlugin.getPlugin().updateTotalIOBTempBasals();
//        final IobTotal bolusIob = TreatmentsPlugin.getPlugin().getLastCalculationTreatments().round();
//        final IobTotal basalIob = TreatmentsPlugin.getPlugin().getLastCalculationTempBasals().round();
//
//
//        final LoopPlugin.LastRun finalLastRun = LoopPlugin.lastRun;
//        boolean predictionsAvailable;
//        if (Config.APS)
//            predictionsAvailable = finalLastRun != null && finalLastRun.request.hasPredictions;
//        else if (Config.NSCLIENT)
//            predictionsAvailable = true;
//        else
//            predictionsAvailable = false;
//        final boolean finalPredictionsAvailable = predictionsAvailable;
//
//        // ****** GRAPH *******
//
//        new Thread(() -> {
//            // allign to hours
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(System.currentTimeMillis());
//            calendar.set(Calendar.MILLISECOND, 0);
//            calendar.set(Calendar.SECOND, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.add(Calendar.HOUR, 1);
//
//            int hoursToFetch;
//            long toTime;
//            long fromTime;
//            long endTime;
//
//            APSResult apsResult = null;
//
//            if (finalPredictionsAvailable && SP.getBoolean("showprediction", false)) {
//                if (Config.APS)
//                    apsResult = finalLastRun.constraintsProcessed;
//                else
//                    apsResult = NSDeviceStatus.getAPSResult();
//                int predHours = (int) (Math.ceil(apsResult.getLatestPredictionsTime() - System.currentTimeMillis()) / (60 * 60 * 1000));
//                predHours = Math.min(2, predHours);
//                predHours = Math.max(0, predHours);
//                hoursToFetch = rangeToDisplay - predHours;
//                toTime = calendar.getTimeInMillis() + 100000; // little bit more to avoid wrong rounding - Graphview specific
//                fromTime = toTime - T.hours(hoursToFetch).msecs();
//                endTime = toTime + T.hours(predHours).msecs();
//            } else {
//                hoursToFetch = rangeToDisplay;
//                toTime = calendar.getTimeInMillis() + 100000; // little bit more to avoid wrong rounding - Graphview specific
//                fromTime = toTime - T.hours(hoursToFetch).msecs();
//                endTime = toTime;
//            }
//
//
//            final long now = System.currentTimeMillis();
//
//            //  ------------------ 1st graph
//            if (L.isEnabled(L.OVERVIEW))
//                Profiler.log(log, from + " - 1st graph - START", updateGUIStart);
//
//            Profile profile;
//            if (!ProfileFunctions.getInstance().isProfileValid("HOME")) {
//                // TODO
//                Log.e("GRAPH", "No valid profile set");
//                return;
//            } else {
//                profile = ProfileFunctions.getInstance().getProfile();
//            }
//
//            // final ChartDataParser chartDataParser = new ChartDataParser(chart, IobCobCalculatorPlugin.getPlugin());
//            // chartDataParser.addBgReadings(fromTime, toTime, lowLine, highLine, null);
//            try {
//                data.clearSeries();
//                data.addInRangeArea(fromTime, Math.max(endTime, now + 1000*60*60), profile.getTargetLow(), profile.getTargetHigh());
//                data.addBgReadings(fromTime, endTime, lowLine, highLine);
//                data.addNowLine();
//                data.addPredictions();
//                data.addBolusEvents(fromTime);
//                data.addIob(fromTime, now, false, 0.5d);
//                data.formatAxis(fromTime, endTime);
//                Log.d("GRAPH", String.format("fromTime=%d endTime=%d toTime=%d", fromTime, endTime, toTime));
//            } catch (Exception ex) {
//                log.error("Failed to display graph", ex);
//                throw ex;
//            }
//
//            FragmentActivity activity = getActivity();
//            if (activity != null) {
//                activity.runOnUiThread(() -> {
//                    // finally enforce drawing of graphs
//                    data.forceUpdate();
//                    if (L.isEnabled(L.OVERVIEW))
//                        Profiler.log(log, from + " - onDataChanged", updateGUIStart);
//                });
//            }
//        }).start();
//
//        if (L.isEnabled(L.OVERVIEW))
//            Profiler.log(log, from, updateGUIStart);
    }

}
