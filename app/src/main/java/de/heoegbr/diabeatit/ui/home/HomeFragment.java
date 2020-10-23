package de.heoegbr.diabeatit.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import de.heoegbr.diabeatit.assistant.boluscalculator.BolusCalculatorResult;
import de.heoegbr.diabeatit.assistant.boluscalculator.SimpleBolusCalculator;
import de.heoegbr.diabeatit.data.container.Profil;
import de.heoegbr.diabeatit.data.container.event.DiaryEvent;
import de.heoegbr.diabeatit.data.container.event.PredictionEvent;
import de.heoegbr.diabeatit.data.repository.DiaryRepository;
import de.heoegbr.diabeatit.ui.boluscalculator.BolusCalculatorActivity;


public class HomeFragment extends Fragment {
    private static final String TAG = "HOME_FRAGMENT";
    private static WeakReference<HomeFragment> instance;

    private int mBgColor;
    private int mBolusColor;
    private int mBasalColor;
    private int mBasalFillColor;
    private int mCarbsColor;
    private int mPredictionColor;
    private int mPredictionMarkerColor;
    private List<LegendEntry> mLegendEntries;
    public TargetZoneCombinedChart cgmChart;


    //    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        childFragment = new BolusCalculatorFragment();
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.replace(R.id.bolus_calculator_fragment_container, childFragment).commit();
//    }
    // Fixme implement model view pattern correctly
    private HomeViewModel homeViewModel;
    // todo get propper patient information
    SimpleBolusCalculator bCalc = new SimpleBolusCalculator(Profil.BG_TARGET,
            Profil.INSULIN_SENSITIVITY_FACTOR,
            Profil.INSULIN_CARB_RATIO);

    @Nullable
    public static HomeFragment getInstance() {
        return instance.get();
    }

    private Button bolusSuggestionButton;

    private void updateChart(TargetZoneCombinedChart chart, List<DiaryEvent> diaryEvents
            , boolean debugEnable) {
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
                .getBoolean("settings_general_plot_debug", false) && debugEnable) {
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

    private void setupChart(TargetZoneCombinedChart chart) {
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
                getContext().getColor(R.color.chart_color_target_zone),
                rangeLow, rangeHigh));

        // set limit lines for hyper, hypo, and severe hypo
        LimitLine hypoLine = new LimitLine(rangeLow - 2f, "");
        hypoLine.setLineColor(getContext().getColor(R.color.chart_color_target_zone_border));
        hypoLine.setLineWidth(1f);
        chart.getAxisLeft().addLimitLine(hypoLine);

        LimitLine severeHypoLine = new LimitLine(51f, "");
        severeHypoLine.setLineColor(getContext().getColor(R.color.chart_color_severe_hypo));
        severeHypoLine.setLineWidth(1f);
        chart.getAxisLeft().addLimitLine(severeHypoLine);

        LimitLine hyperLine = new LimitLine(rangeHigh - 2f, "");
        hyperLine.setLineColor(getContext().getColor(R.color.chart_color_target_zone_border));
        hyperLine.setLineWidth(1f);
        chart.getAxisLeft().addLimitLine(hyperLine);
        chart.getAxisLeft().setDrawLimitLinesBehindData(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle _b) {
        View root = inflater.inflate(R.layout.d_fragment_home, container, false);

        cgmChart = root.findViewById(R.id.chart_bg);
        mBgColor = getContext().getColor(R.color.chart_color_bg);
        mBolusColor = getContext().getColor(R.color.chart_color_bolus);
        mBasalColor = getContext().getColor(R.color.chart_color_basal);
        mBasalFillColor = getContext().getColor(R.color.chart_color_basal_fill);
        mCarbsColor = getContext().getColor(R.color.chart_color_carbs);
        mPredictionColor = getContext().getColor(R.color.chart_color_prediction);
        mPredictionMarkerColor = getContext().getColor(R.color.chart_color_prediction_marker);
        mLegendEntries = new ArrayList<>();
        setupChart(cgmChart);

        bolusSuggestionButton = root.findViewById(R.id.bolus_calculator_button);
        bolusSuggestionButton.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), BolusCalculatorActivity.class));
        });

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.observeData(getViewLifecycleOwner(), diaryEvents -> {
            updateChart(cgmChart, diaryEvents, true);
            Log.d(TAG, "updated main chart");

            runBolusCalculation(homeViewModel.getCurrentBgValue());
        });

        instance = new WeakReference<>(this);
        return root;
    }

    private void runBolusCalculation(double bg) {
        double iob = DiaryRepository.getRepository(getContext())
                .getIOB(Profil.DURATION_OF_INSULIN_ACTIVITY,
                        Profil.INSULIN_PEEK_ACTIVITY);

        if (bg > 0) {
            BolusCalculatorResult bolusCalculatorResult = bCalc.calculateBolus(bg,
                    Profil.BG_TARGET, iob, 0, 0);
            // TODO respect localized format (, or .)
            bolusSuggestionButton.setText(bolusCalculatorResult.getResultAsText());
        } else {
            bolusSuggestionButton.setText(R.string.bolus_calculator_nobolus);
        }

    }
//
//    public void expandView() {
//        mExpanded = true;
//
//        cgmChart.setVisibility(View.GONE);
//
//        extraInput.setVisibility(View.VISIBLE);
//        bolusChart.setVisibility(View.VISIBLE);
//
//        carbs.requestFocus();
//        carbs.postDelayed(() -> {
//            InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(
//                    Context.INPUT_METHOD_SERVICE);
//            keyboard.showSoftInput(carbs, 0);
//        }, 200);
//
////        cameraCaptureView.setSurfaceTextureListener(buildListener());
////        openCamera();
//    }
//
//    public void reduceView() {
//        mExpanded = false;
//
//        cgmChart.setVisibility(View.VISIBLE);
//
//        extraInput.setVisibility(View.GONE);
//        bolusChart.setVisibility(View.GONE);
//
//        InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(
//                Context.INPUT_METHOD_SERVICE);
//        keyboard.hideSoftInputFromWindow(getView().getWindowToken(), 0);
//
//        carbs.setText("");
//        carbs.clearFocus();
//        correction.setText("");
//        correction.clearFocus();
//
////        closeCamera();
//    }

//    public boolean isExpanded() {
//        return mExpanded;
//    }


//    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
//    private static final int REQUEST_CAMERA_PERMISSION = 200;
//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        ORIENTATIONS.append(Surface.ROTATION_270, 180);
//    }
//
//    protected CameraDevice cameraDevice;
//    protected CameraCaptureSession cameraCaptureSessions;
//    protected CaptureRequest captureRequest;
//    protected CaptureRequest.Builder captureRequestBuilder;
//    private TextureView cameraCaptureView;
//    private String cameraId;
//    private Size imageDimension;
//    private ImageReader imageReader;
//    private File file;
//    private boolean mFlashSupported;
//    private Handler mBackgroundHandler;
//    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(CameraDevice camera) {
//            //This is called when the camera is open
//            Log.e(TAG, "onOpened");
//            cameraDevice = camera;
//            createCameraPreview();
//        }
//
//        @Override
//        public void onDisconnected(CameraDevice camera) {
//            cameraDevice.close();
//        }
//
//        @Override
//        public void onError(CameraDevice camera, int error) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//    };
//    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
//        @Override
//        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//            super.onCaptureCompleted(session, request, result);
//            Toast.makeText(getContext(), "Saved:" + file, Toast.LENGTH_SHORT).show();
//            createCameraPreview();
//        }
//    };
//    private HandlerThread mBackgroundThread;
    // camera feature
    // copied from https://inducesmile.com/android/android-camera2-api-example-tutorial/
    // FIXME bring in clean form
    // FIXME drains the battery very fast (even when not visible ..)
    // cooler version -_> https://material.io/collections/machine-learning/object-detection-live-camera.html#usage
    // runde ecken --> https://github.com/developer-anees/android-round-camera2video-preview/tree/master/app/src/main/java/in/round/camera/preview
    // * clean code
    // * disable camera when invisible
    // * reset with expand
    // * make picture fices after shot
    // * delete picture with additional click
//
//    TextureView.SurfaceTextureListener buildListener() {
//        return new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//                //open your camera here
//                openCamera();
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//                // Transform you image captured size according to the surface width and height
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                closeCamera();
//                return false;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//            }
//        };
//    }
//
//    protected void startBackgroundThread() {
//        mBackgroundThread = new HandlerThread("Camera Background");
//        mBackgroundThread.start();
//        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
//    }
//
//    protected void stopBackgroundThread() {
//        mBackgroundThread.quitSafely();
//        try {
//            mBackgroundThread.join();
//            mBackgroundThread = null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    protected void takePicture() {
//        if (null == cameraDevice) {
//            Log.e(TAG, "cameraDevice is null");
//            return;
//        }
//        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
//        try {
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
//            Size[] jpegSizes = null;
//            if (characteristics != null) {
//                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
//            }
//            int width = 640;
//            int height = 480;
//            if (jpegSizes != null && 0 < jpegSizes.length) {
//                width = jpegSizes[0].getWidth();
//                height = jpegSizes[0].getHeight();
//            }
//            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
//            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
//            outputSurfaces.add(reader.getSurface());
//            outputSurfaces.add(new Surface(cameraCaptureView.getSurfaceTexture()));
//            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            captureBuilder.addTarget(reader.getSurface());
//            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//            // Orientation
//            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
//            final File file = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
//            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader reader) {
//                    Image image = null;
//                    try {
//                        image = reader.acquireLatestImage();
//                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                        byte[] bytes = new byte[buffer.capacity()];
//                        buffer.get(bytes);
//                        save(bytes);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                        if (image != null) {
//                            image.close();
//                        }
//                    }
//                }
//
//                private void save(byte[] bytes) throws IOException {
//                    OutputStream output = null;
//                    try {
//                        output = new FileOutputStream(file);
//                        output.write(bytes);
//                    } finally {
//                        if (null != output) {
//                            output.close();
//                        }
//                    }
//                }
//            };
//            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
//            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
//                @Override
//                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//                    super.onCaptureCompleted(session, request, result);
//                    Toast.makeText(getContext(), "Saved:" + file, Toast.LENGTH_SHORT).show();
//                    createCameraPreview();
//                }
//            };
//            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(CameraCaptureSession session) {
//                    try {
//                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
//                    } catch (CameraAccessException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onConfigureFailed(CameraCaptureSession session) {
//                }
//            }, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    protected void createCameraPreview() {
//        try {
//            SurfaceTexture texture = cameraCaptureView.getSurfaceTexture();
//            assert texture != null;
//            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
//            Surface surface = new Surface(texture);
//            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            captureRequestBuilder.addTarget(surface);
//            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    //The camera is already closed
//                    if (null == cameraDevice) {
//                        return;
//                    }
//                    // When the session is ready, we start displaying the preview.
//                    cameraCaptureSessions = cameraCaptureSession;
//                    updatePreview();
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Toast.makeText(getContext(), "Configuration change", Toast.LENGTH_SHORT).show();
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void openCamera() {
//        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
//        Log.e(TAG, "is camera open");
//        try {
//            cameraId = manager.getCameraIdList()[0];
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            assert map != null;
//            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
//            // Add permission for camera and let user grant the permission
//            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
//                return;
//            }
//            manager.openCamera(cameraId, stateCallback, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        Log.e(TAG, "openCamera X");
//    }
//
//    protected void updatePreview() {
//        if (null == cameraDevice) {
//            Log.e(TAG, "updatePreview error, return");
//        }
//        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//        try {
//            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // TODO call on app pause--> take a look at the ref how they did solve it...
//    private void closeCamera() {
//        Log.e(getTag(), "close camera");
//        if (null != cameraDevice) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//        if (null != imageReader) {
//            imageReader.close();
//            imageReader = null;
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                // close the app
//                Toast.makeText(getContext(), "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
//                //finish();
//            }
//        }
//    }

}
