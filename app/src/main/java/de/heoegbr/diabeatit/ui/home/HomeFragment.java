package de.heoegbr.diabeatit.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.jjoe64.graphview.GraphView;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.heoegbr.diabeatit.DiaBEATitApp;
import de.heoegbr.diabeatit.R;
import io.reactivex.disposables.CompositeDisposable;


public class HomeFragment extends Fragment {
    private static final String TAG = "HOME_FRAGMENT";
    private static WeakReference<HomeFragment> instance;

    private HomeViewModel homeViewModel;
    private int rangeToDisplay = 6; // for graph
    public GraphView graph;
    //private ChartDataParser data;

    private CompositeDisposable disposable = new CompositeDisposable();

    private int numberOfLines = 1;
    private int maxNumberOfLines = 4;
    private int numberOfPoints = 12;

    float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];

    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasPoints = true;
    private boolean hasLines = true;
    private boolean isCubic = false;
    private boolean hasLabels = false;

    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> scheduledUpdate = null;

    SharedPreferences.OnSharedPreferenceChangeListener listener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle _b) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.d_fragment_home, container, false);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        Log.d("MAIN", "Getting graph and creating data");
        graph = root.findViewById(R.id.chart);
        //data = new ChartDataParser(graph);
        scheduleUpdateGUI("");

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                prefsChanged(sharedPreferences, key);
            }
        };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DiaBEATitApp.getContext());
        prefs.registerOnSharedPreferenceChangeListener(listener);

        instance = new WeakReference<>(this);
        return root;
    }

    @Nullable
    public static HomeFragment getInstance() {
        return instance.get();
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        bc = new BolusCalculatorFragment();
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.replace(R.id.bolus_calculator_fragment_container, bc).commit();
        scheduleUpdateGUI("onViewCreated");

        // Update GUI whenever we receive a new BG reading
        // TODO do we want a pub/sub pattern?
//        disposable.add(RxBus.INSTANCE
//            .toObservable(EventNewBG.class)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(event -> scheduleUpdateGUI("New BG Event", 100),
//                        FabricPrivacy::logException));
//        disposable.add(RxBus.INSTANCE
//            .toObservable(EventTreatmentChange.class)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(event -> scheduleUpdateGUI("TREATMENT", 200),
//                        FabricPrivacy::logException));
//        disposable.add(RxBus.INSTANCE
//            .toObservable(EventReloadTreatmentData.class)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(event -> scheduleUpdateGUI("TREATMENT", 200),
//                        FabricPrivacy::logException));
    }

    public void scheduleUpdateGUI(final String from, final long delay) {
        class UpdateRunnable implements Runnable {
            public void run() {
                Activity activity = getActivity();
                if (activity != null)
                    activity.runOnUiThread(() -> {
                        updateGUI(from);
                        scheduledUpdate = null;
                    });
            }
        }
        // prepare task for execution in 400 msec
        // cancel waiting task to prevent multiple updates
        if (scheduledUpdate != null)
            scheduledUpdate.cancel(false);
        Runnable task = new UpdateRunnable();
        scheduledUpdate = worker.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    public void scheduleUpdateGUI(final String from) {
        scheduleUpdateGUI(from, 500);
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
