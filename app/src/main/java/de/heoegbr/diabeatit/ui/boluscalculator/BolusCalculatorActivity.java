package de.heoegbr.diabeatit.ui.boluscalculator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;
import java.io.IOException;
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
import de.heoegbr.diabeatit.assistant.prediction.ModelGlucodyn;
import de.heoegbr.diabeatit.data.container.event.BolusEvent;
import de.heoegbr.diabeatit.data.container.event.DiaryEvent;
import de.heoegbr.diabeatit.data.container.event.MealEvent;
import de.heoegbr.diabeatit.data.container.event.PredictionEvent;
import de.heoegbr.diabeatit.data.repository.DiaryRepository;
import de.heoegbr.diabeatit.ui.home.HomeActivity;
import de.heoegbr.diabeatit.ui.home.HomeViewModel;
import de.heoegbr.diabeatit.ui.home.TargetZoneCombinedChart;
import de.heoegbr.diabeatit.util.NativeArrayConverter;

public class BolusCalculatorActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    // fixme is this business logic?
    // todo get propper patient information
    SimpleBolusCalculator bCalc = new SimpleBolusCalculator(100, 30, 7);
    ModelGlucodyn predictModel = new ModelGlucodyn(90, 30, 180, 7);
    BolusCalculatorResult bolusCalculatorResult = null;
    List<DiaryEvent> bolusPreviewEvents = null;
    private double carbsDouble = 0;
    private double bolusDouble = 0;
    private int mBgColor;
    private int mBolusColor;
    private int mBasalColor;
    private int mBasalFillColor;
    private int mCarbsColor;
    private int mPredictionColor;
    private int mPredictionMarkerColor;
    // FIXME find propper solution for the cool bolus popup feature (make chart with getter?)
    // bolus calculator
    private boolean mExpanded = false;
    private TargetZoneCombinedChart bolusChart;
    private TextView bolusText;
    private EditText carbs;
    private EditText correction;
    private ImageButton cameraButton;
    //    private ImageView cameraPreview;
//    private CardView cameraPreviewContainer;
    private ImageView cameraPreviewIcon;
    private RoundedImageView cameraPreview2;
    private MultiAutoCompleteTextView notes;

    private double currentBG = 100;
    private HomeViewModel homeViewModel;
    private String currentPhotoPath = null;

    public static Bitmap rotateImage(Bitmap source, float angle) {
        if (angle == 0) return source;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private Context getContext() {
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_ok, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // we have only one item so no switch statement
        if (item.getItemId() == R.id.actionbar_ok_item) {
            String noteString = notes.getText().toString();
            if (bolusDouble > 0) {
                BolusEvent bolusEvent = new BolusEvent(DiaryEvent.SOURCE_USER,
                        Instant.now(), bolusDouble, noteString);
                DiaryRepository.getRepository(getContext()).insertEvent(bolusEvent);
            }
            if (carbsDouble > 0) {
                String imagePath = "";
                if (currentPhotoPath != null && !currentPhotoPath.isEmpty())
                    imagePath = currentPhotoPath;

                MealEvent mealEvent = new MealEvent(DiaryEvent.SOURCE_USER, Instant.now(),
                        imagePath, carbsDouble, noteString);
                DiaryRepository.getRepository(getContext()).insertEvent(mealEvent);
            }
            startActivity(new Intent(getContext(), HomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boluscalculator);
        getSupportActionBar().setTitle(getResources().getString(R.string.bolus_calculator_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

//        if (savedInstanceState == null) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.settings, new SettingsActivity.HeaderFragment())
//                    .commit();
//        } else {
//            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
//        }
//        getSupportFragmentManager().addOnBackStackChangedListener(
//                () -> {
//                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
//                        setTitle(R.string.settings_activity_title);
//                    }
//                });
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }


        // bolus calculator
        bolusChart = findViewById(R.id.bc_chart);
        mBgColor = getColor(R.color.chart_color_bg);
        mBolusColor = getColor(R.color.chart_color_bolus);
        mBasalColor = getColor(R.color.chart_color_basal);
        mBasalFillColor = getColor(R.color.chart_color_basal_fill);
        mCarbsColor = getColor(R.color.chart_color_carbs);
        mPredictionColor = getColor(R.color.chart_color_prediction);
        mPredictionMarkerColor = getColor(R.color.chart_color_prediction_marker);
        double target = 100; //TODO target should come from profile
        setupChart(bolusChart, target);

        bolusText = findViewById(R.id.bc_bolus);
        carbs = findViewById(R.id.bc_carbs_input);
        correction = findViewById(R.id.bc_correction_input);
//        cameraCaptureView = root.findViewById(R.id.bc_camera_capture);
//        cameraCaptureView.setOnClickListener(view -> {
//            takePicture();
//        });

        carbs.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                runBolusCalculation(currentBG);
                updateBolusChart(currentBG);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        correction.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                runBolusCalculation(currentBG);
                updateBolusChart(currentBG);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        cameraButton = findViewById(R.id.bc_camera_button);
        cameraButton.setOnClickListener(view -> {
            // copied from: https://developer.android.com/training/camera/photobasics
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ignored) {
                    Log.e("", ignored.getMessage());
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(getContext(),
                            getApplicationContext().getPackageName() + ".provider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }

        });
//        cameraPreview = findViewById(R.id.bc_cam_preview);
//        cameraPreviewContainer = findViewById(R.id.bc_cam_preview_container);
        cameraPreviewIcon = findViewById(R.id.bc_cam_preview_icon);
        cameraPreview2 = findViewById(R.id.bc_cam_preview_round);

        notes = findViewById(R.id.bc_note_input);
        // todo add auto complete code

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.observeData(this, diaryEvents -> {
            currentBG = homeViewModel.getCurrentBgValue();
            runBolusCalculation(homeViewModel.getCurrentBgValue());
            updateBolusChart(diaryEvents, homeViewModel.getCurrentBgValue());
        });

        carbs.requestFocus();
        carbs.postDelayed(() -> {
            InputMethodManager keyboard = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(carbs, 0);
        }, 200);

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getApplication().getFilesDir().getAbsolutePath()); // getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                setPic();
            } catch (Exception ignored) {
            }
            galleryAddPic();
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    private void setPic() throws IOException {
        // TODO revisit picture orientation issue when time
        // https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a

        ExifInterface ei = new ExifInterface(currentPhotoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        float rotation = 0;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
        }

        // Get the dimensions of the View
        int targetW;
        int targetH;
        if (rotation == 0 || rotation == 180) {
            targetW = cameraPreview2.getWidth();
            targetH = cameraPreview2.getHeight();
        } else {
            targetW = cameraPreview2.getHeight();
            targetH = cameraPreview2.getWidth();
        }

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
//        cameraPreview.setImageBitmap(rotateImage(bitmap, rotation));
//        cameraPreviewContainer.setVisibility(View.GONE);
        cameraPreviewIcon.setVisibility(View.VISIBLE);
        cameraPreview2.setImageBitmap(rotateImage(bitmap, rotation));
    }

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

        LineData ld = new LineData();
        ld.addDataSet(bgDataSet);

        if (!predictionEntries.isEmpty()) {
            for (List<Entry> predictionEntry : predictionEntries) {
                LineDataSet predDataSet = new LineDataSet(predictionEntry, "");
                predDataSet.setColor(mPredictionColor);
                predDataSet.setCircleColor(mPredictionColor);
                predDataSet.setDrawValues(false);
                ld.addDataSet(predDataSet);
            }
        }

        // combine datasets
        CombinedData combinedData = new CombinedData();
        combinedData.setData(ld);

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

        // zoom to current time frame
        // todo calculate zoom
        //chart.zoom(1.2f, 1f, 0, 160, YAxis.AxisDependency.LEFT);
        chart.moveViewToX(lastEntryX - 5f);
    }

    private void setupChart(TargetZoneCombinedChart chart, double target) {
        // make y axis fixed to reasonable values
        chart.setScaleYEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0.0f);
        chart.getAxisLeft().setAxisMaximum(300.0f);
        chart.getAxisRight().setAxisMinimum(0.0f);
        chart.getAxisRight().setAxisMaximum(300.0f);

        // setup x axis
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        // setup legend
        chart.getLegend().setEnabled(false);

        // don't know what description should do...
        chart.getDescription().setEnabled(false);

        float rangeHigh = 180f;
        float rangeLow = 80f;
        chart.addTargetZone(new TargetZoneCombinedChart.TargetZone(
                getColor(R.color.chart_color_target_zone),
                rangeLow, rangeHigh));

        // set limit lines for hyper, hypo, and severe hypo
        LimitLine hypoLine = new LimitLine(rangeLow - 2f, "");
        hypoLine.setLineColor(getColor(R.color.chart_color_target_zone_border));
        hypoLine.setLineWidth(1f);
        chart.getAxisLeft().addLimitLine(hypoLine);

        LimitLine severeHypoLine = new LimitLine(51f, "");
        severeHypoLine.setLineColor(getColor(R.color.chart_color_severe_hypo));
        severeHypoLine.setLineWidth(1f);
        chart.getAxisLeft().addLimitLine(severeHypoLine);

        LimitLine hyperLine = new LimitLine(rangeHigh - 2f, "");
        hyperLine.setLineColor(getColor(R.color.chart_color_target_zone_border));
        hyperLine.setLineWidth(1f);
        chart.getAxisLeft().addLimitLine(hyperLine);
        chart.getAxisLeft().setDrawLimitLinesBehindData(true);

        // set target line
        LimitLine targetLine = new LimitLine((float) target, "target");
        targetLine.setLineColor(getColor(R.color.chart_color_target_line));
        targetLine.setLineWidth(2f);
        chart.getAxisLeft().addLimitLine(targetLine);
        chart.getAxisLeft().setDrawLimitLinesBehindData(true);
    }

    private void runBolusCalculation(double bg) {
        carbsDouble = 0;
        double correctionDouble = 0;
        double iob = 0;

        try {
            carbsDouble = Double.parseDouble(carbs.getText().toString());
        } catch (Exception ignored) {
        }
        try {
            correctionDouble = Double.parseDouble(correction.getText().toString());
        } catch (Exception ignored) {
        }

        if (bg > 0) {
            bolusCalculatorResult = bCalc.calculateBolus(bg, 100, iob, carbsDouble, correctionDouble);
            // TODO respect localized format (, or .)
            bolusText.setText(String.format("%.2f", bolusCalculatorResult.bolus) + " I.E.");
            bolusDouble = bolusCalculatorResult.bolus;
        } else {
            bolusText.setText(R.string.bolus_calculator_nobolus);
        }

    }

    private void updateBolusChart(double bg) {
        if (bolusPreviewEvents != null && bolusCalculatorResult != null) {
            List<DiaryEvent> bolusPreviewEventsTmp = new ArrayList<>();

            Instant firstVisibleBgDate = Instant.now()
                    .minus(2, ChronoUnit.HOURS);
            for (DiaryEvent item : bolusPreviewEvents) {
                // filter values older than 2h
                if (firstVisibleBgDate.isAfter(item.timestamp)) continue;
                bolusPreviewEventsTmp.add(item);
            }

            if (bolusCalculatorResult != null) {
                DiaryEvent event = new BolusEvent(DiaryEvent.SOURCE_USER,
                        Instant.now(),
                        bolusCalculatorResult.bolus,
                        null);
                bolusPreviewEventsTmp.add(event);

                //TODO use actual therapy constants
                double[] prediction = predictModel.calculateBgProgression(bg,
                        bolusCalculatorResult.bolus, carbsDouble, 48);
                PredictionEvent bolusPrediction = new PredictionEvent(DiaryEvent.SOURCE_USER,
                        Instant.now(), null, NativeArrayConverter.toArrayList(prediction),
                        null, null, null);
                bolusPreviewEventsTmp.add(bolusPrediction);
            }

            // TODO make propper axis calculation depending on showed values
            // TODO streamline drawing (a lot of stuff is done twise)
            bolusChart.getAxisLeft().setDrawLabels(false);
//            bolusChart.getAxisLeft().setAxisMinimum((float) bg - 80);
//            bolusChart.getAxisLeft().setAxisMaximum((float) bg + 90);
//            bolusChart.getAxisRight().setAxisMinimum((float) bg - 80);
//            bolusChart.getAxisRight().setAxisMaximum((float) bg + 90);

            updateChart(bolusChart, bolusPreviewEventsTmp, false);
        }
    }

    private void updateBolusChart(List<DiaryEvent> diaryEvents, double bg) {
        bolusPreviewEvents = diaryEvents;
        updateBolusChart(bg);
    }
}
