package de.heoegbr.diabeatit.ui.diary;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Locale;

import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.db.container.event.BolusEvent;
import de.heoegbr.diabeatit.db.container.event.DiaryEvent;
import de.heoegbr.diabeatit.db.repository.DiaryRepository;
import de.heoegbr.diabeatit.ui.home.HomeFragment;

public class ManualInsulinEntryActivity extends AppCompatActivity {

    private EditText bolusInput, notesInput;
    private Button selDateB, selTimeB;

    Calendar timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_activity_manual_insulin_entry);

        getSupportActionBar().setTitle(getResources().getString(R.string.mi_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        bolusInput = findViewById(R.id.mi_input);
        notesInput = findViewById(R.id.mi_notes);
        selDateB = findViewById(R.id.mi_date);
        selTimeB = findViewById(R.id.mi_time);

        selDateB.setOnClickListener(v -> selectDate());
        selTimeB.setOnClickListener(v -> selectTime());
        findViewById(R.id.mi_save).setOnClickListener(v -> save());

        timestamp = new Calendar.Builder()
                .setInstant(Instant.now().toEpochMilli())
                .setTimeZone(Calendar.getInstance().getTimeZone())
                .build();

        selDateB.setText(new SimpleDateFormat("dd.MM.YYYY", Locale.GERMAN).format(timestamp.getTime()));
        selTimeB.setText(new SimpleDateFormat("HH:mm", Locale.GERMAN).format(timestamp.getTime()));

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
        if (bolusInput.getText().toString().isEmpty()) {
            bolusInput.setHintTextColor(ContextCompat.getColor(this, R.color.d_important));
            return;
        }

        try {
            double insulin = Double.parseDouble(bolusInput.getText().toString());
            long ts = timestamp.toInstant().toEpochMilli();

            DiaryRepository.getRepository(getApplicationContext())
                    .insertEvent(new BolusEvent(DiaryEvent.SOURCE_USER, timestamp.toInstant(), insulin,
                            notesInput.getText().toString()));

        } catch (Exception ignored) {
            return;
        }

        // Update GUI
        HomeFragment fragment = HomeFragment.getInstance();
        if (fragment != null)
            //FIXME I deleted this sh***
            //fragment.scheduleUpdateGUI(this.getClass().getCanonicalName());

        finish();
    }

}