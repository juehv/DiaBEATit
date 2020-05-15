package de.heoegbr.diabeatit.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Locale;

import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.db.container.event.CarbEvent;
import de.heoegbr.diabeatit.db.container.event.DiaryEvent;
import de.heoegbr.diabeatit.db.repository.DiaryEventStore;
import de.heoegbr.diabeatit.ui.home.HomeFragment;

public class ManualCarbsEntryActivity extends AppCompatActivity {

    Uri currentPicture;
    Calendar timestamp;
    private EditText carbsInput, notesInput;
    private ImageView previewV;
    private Button selDateB, selTimeB;
    private ImageButton delPicB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_activity_manual_carbs_entry);

        getSupportActionBar().setTitle(getString(R.string.mc_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        carbsInput = findViewById(R.id.mc_input);
        notesInput = findViewById(R.id.mc_notes);

        previewV = findViewById(R.id.mc_picture_preview);

        delPicB = findViewById(R.id.mc_picture_delete);
        selDateB = findViewById(R.id.mc_date);
        selTimeB = findViewById(R.id.mc_time);

        findViewById(R.id.mc_picture_set).setOnClickListener(v -> selectPicture());
        delPicB.setOnClickListener(v -> deletePicture());
        selDateB.setOnClickListener(v -> selectDate());
        selTimeB.setOnClickListener(v -> selectTime());
        findViewById(R.id.mc_save).setOnClickListener(v -> save());

        timestamp = new Calendar.Builder()
                .setInstant(Instant.now().toEpochMilli())
                .setTimeZone(Calendar.getInstance().getTimeZone())
                .build();

        selDateB.setText(new SimpleDateFormat("dd.MM.YYYY", Locale.GERMAN).format(timestamp.getTime()));
        selTimeB.setText(new SimpleDateFormat("HH:mm", Locale.GERMAN).format(timestamp.getTime()));

    }

    private void selectPicture() {

        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK) {

            try {

                currentPicture = data.getData();
                previewV.setImageURI(currentPicture);

                previewV.setVisibility(View.VISIBLE);
                delPicB.setVisibility(View.VISIBLE);

            } catch (Exception ignored) {
            }

        } else super.onActivityResult(requestCode, resultCode, data);

    }

    private void deletePicture() {

        currentPicture = null;

        delPicB.setVisibility(View.GONE);
        previewV.setVisibility(View.GONE);

    }

    private void selectDate() {

        new DatePickerDialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert,
                (v, y, m, d) -> {
                    timestamp.set(Calendar.YEAR, y);
                    timestamp.set(Calendar.MONTH, m);
                    timestamp.set(Calendar.DAY_OF_MONTH, d);
                    selDateB.setText(new SimpleDateFormat("dd.MM.YYYY", Locale.GERMAN).format(timestamp.getTime()));
                },
                timestamp.get(Calendar.YEAR), timestamp.get(Calendar.MONTH), timestamp.get(Calendar.DAY_OF_MONTH)
        ).show();

    }

    private void selectTime() {

        new TimePickerDialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert,
                (v, h, m) -> {
                    timestamp.set(Calendar.HOUR_OF_DAY, h);
                    timestamp.set(Calendar.MINUTE, m);
                    selTimeB.setText(new SimpleDateFormat("HH:mm", Locale.GERMAN).format(timestamp.getTime()));
                },
                timestamp.get(Calendar.HOUR), timestamp.get(Calendar.MINUTE), true
        ).show();

    }

    private void save() {
        if (carbsInput.getText().toString().isEmpty()) {
            carbsInput.setHintTextColor(ContextCompat.getColor(this, R.color.d_important));
            return;
        }

        try {
            int carbs = Integer.parseInt(carbsInput.getText().toString());
            long ts = timestamp.toInstant().toEpochMilli();
            String notes = notesInput.getText().toString();

            //CarbsGenerator.createCarb(carbs, ts, "ManualCarbsActivity", notes);
            // --> 	TreatmentsPlugin.getPlugin().addToHistoryTreatment(carbInfo, false);

            Bitmap bm = null;
            if (currentPicture != null)
                bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), currentPicture);

            DiaryEventStore.getRepository(getApplicationContext())
                    .addEvent(new CarbEvent(DiaryEvent.SOURCE_USER, timestamp.toInstant(),
                            bm, carbs, notes));

            long min30 = 30 * 60 * 1000;
//			HomeFragment frag = HomeFragment.getInstance();
//			if (ts > (System.currentTimeMillis() - min30) && frag != null )//&& frag.bc != null)
//				frag.bc.setCarbs(carbs);

        } catch (Exception ignored) {
            return;
        }

        // Update GUI
        HomeFragment fragment = HomeFragment.getInstance();
        if (fragment != null)
            fragment.scheduleUpdateGUI(this.getClass().getCanonicalName());

        finish();
    }

}