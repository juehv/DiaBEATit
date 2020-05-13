package de.heoegbr.diabeatit.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Locale;

import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.log.LogEventStore;
import de.heoegbr.diabeatit.log.event.SportsEvent;

public class ManualSportsEntryActivity extends AppCompatActivity {

    private EditText descriptionInput;
    private Button selDateB, selTimeB, selDurB;

    Calendar timestamp;
    int durationMinutes = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_activity_manual_sports_entry);

        getSupportActionBar().setTitle(getResources().getString(R.string.ms_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        descriptionInput = findViewById(R.id.ms_description);

        selDateB = findViewById(R.id.ms_date);
        selTimeB = findViewById(R.id.ms_time);
        selDurB = findViewById(R.id.ms_duration);

        selDateB.setOnClickListener(v -> selectDate());
        selTimeB.setOnClickListener(v -> selectTime());
        selDurB.setOnClickListener(v -> selectDuration());
        findViewById(R.id.ms_save).setOnClickListener(v -> save());

        timestamp = new Calendar.Builder()
                .setInstant(Instant.now().toEpochMilli())
                .setTimeZone(Calendar.getInstance().getTimeZone())
                .build();

        selDateB.setText(new SimpleDateFormat("dd.MM.YYYY", Locale.GERMAN).format(timestamp.getTime()));
        selTimeB.setText(new SimpleDateFormat("HH:mm", Locale.GERMAN).format(timestamp.getTime()));
        selDurB.setText(durationMinutes + "m");

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

    private void selectDuration() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.ms_duration_title));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHintTextColor(Color.rgb(77, 77, 77));
        input.setHint(getString(R.string.ms_duration_hint));
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.ms_duration_ok), (dialog, which) -> {
                if (!input.getText().toString().isEmpty() && input.getText().toString().length() < 6)
                    selDurB.setText((durationMinutes = Integer.parseInt(input.getText().toString())) + "m");
        });

        builder.setNegativeButton(getString(R.string.ms_duration_cancel), (dialog, which) -> dialog.cancel());

        builder.show();

    }

    private void save() {

        if (descriptionInput.getText().toString().isEmpty()) {

            descriptionInput.setHintTextColor(ContextCompat.getColor(this, R.color.d_important));
            return;

        }

        LogEventStore.addEvent(new SportsEvent(timestamp.toInstant(), durationMinutes, descriptionInput.getText().toString()));

        finish();

    }

}