package de.heoegbr.diabeatit.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.Data;

import de.heoegbr.diabeatit.BuildConfig;
import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.db.cloud.ScheduleSyncHelper;
import de.heoegbr.diabeatit.db.cloud.nightscout.NightscoutDownloader;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";

    // Settings vs Properties
    // https://stackoverflow.com/questions/2074384/options-settings-properties-configuration-preferences-when-and-why
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_settings_activity);
        getSupportActionBar().setTitle(getResources().getString(R.string.settings_activity_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(
                () -> {
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        setTitle(R.string.settings_activity_title);
                    }
                });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    public static class HeaderFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.d_header_preferences, rootKey);

            // add listener to send feedback
            Preference feedback = findPreference("feedback");
            if (feedback != null) {
                feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // Open Github
                        String url = "https://github.com/juehv/DiaBEATit/issues";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        return true;
                    }
                });
            }

            // version name
            Preference buildVersion = findPreference("version");
            if (buildVersion != null) {
                buildVersion.setSummary(BuildConfig.VERSION_NAME);
            }
        }
    }

    public static class GeneralFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.d_general_preferences, rootKey);
        }
    }

    public static class SyncFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.d_sync_preferences, rootKey);

            //############# Nightscout ##########
            // regular download
            SwitchPreferenceCompat nsDownloadEnable = findPreference("sync_ns_download_en");
            if (nsDownloadEnable != null) {
                // add listener to schedule background service
                nsDownloadEnable.setOnPreferenceChangeListener((preference, newValue) -> {
                    if ((Boolean) newValue) {
                        Data.Builder data = new Data.Builder();
                        data.putInt(NightscoutDownloader.KEY_NO_OF_VALUE, 24);
                        ScheduleSyncHelper.schedulePeriodicSync(getContext(),
                                NightscoutDownloader.class,
                                NightscoutDownloader.WORK_NAME,
                                data.build());
                    } else {
                        ScheduleSyncHelper.stopPeriodicSync(getContext(),
                                NightscoutDownloader.WORK_NAME);
                    }
                    return true;
                });
            }

            // url field
            EditTextPreference nsUrl = findPreference("sync_ns_url");
            if (nsUrl != null) {
                nsUrl.setOnBindEditTextListener(
                        editText -> editText.setInputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_URI)
                );

                Preference.SummaryProvider<EditTextPreference> sumProv = new Preference.SummaryProvider<EditTextPreference>() {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

                    @Override
                    public CharSequence provideSummary(EditTextPreference preference) {
                        return prefs.getString("sync_ns_url", getString(R.string.settings_sync_ns_url_summary));
                    }
                };

                nsUrl.setSummaryProvider(sumProv);
            }

            // password field
            EditTextPreference nsPw = findPreference("sync_ns_pw");
            if (nsPw != null) {
                nsPw.setOnBindEditTextListener(
                        editText -> editText.setInputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                );
            }

            // Historic sync
            Preference nsHistoricSync = findPreference("sync_ns_historic");
            if (nsHistoricSync != null) {
                nsHistoricSync.setOnPreferenceClickListener(preference -> {
                    Data.Builder data = new Data.Builder();
                    data.putInt(NightscoutDownloader.KEY_NO_OF_VALUE, 210240); // two years
                    ScheduleSyncHelper.scheduleOneTimeSync(getContext(),
                            NightscoutDownloader.class,
                            data.build());
                    Toast.makeText(getContext(),
                            getString(R.string.settings_sync_ns_rationale),
                            Toast.LENGTH_LONG).show();
                    return true;
                });
            }
            //############# END Nightscout ##########
        }
    }

    public static class PredictionsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.d_prediction_prefs, rootKey);
        }
    }
}
