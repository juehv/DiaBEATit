package de.heoegbr.diabeatit.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import de.heoegbr.diabeatit.BuildConfig;
import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.StaticData;
import de.heoegbr.diabeatit.assistant.prediction.python.PythonInputContainer;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";

    // Settings vs Properties
    // https://stackoverflow.com/questions/2074384/options-settings-properties-configuration-preferences-when-and-why
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
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
    public void onSaveInstanceState(@NotNull Bundle outState) {
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
            setPreferencesFromResource(R.xml.preferences_header_menu, rootKey);

            // add listener to send a dataframe for prediction
            Preference sendDataframe = findPreference("settings_send_dataframe");
            if (sendDataframe != null) {
                sendDataframe.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        File filelocation = new File(PythonInputContainer.dataFrameExportPath);
                        if (filelocation.exists()) {
                            Intent emailIntent = new Intent(Intent.ACTION_SEND);
                            // set type to email
                            emailIntent.setType("message/rfc822");
                            String[] to = {"info@diabeatit.de"};
                            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                            // the attachment
                            Uri path = FileProvider.getUriForFile(getContext(),
                                    getContext().getApplicationContext().getPackageName() + ".provider",
                                    filelocation);
                            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            emailIntent.putExtra(Intent.EXTRA_STREAM, path);
                            // the mail subject
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[DiaBEATit] Dataframe export");
                            startActivity(Intent.createChooser(emailIntent, "Send email..."));
                        }

                        return true;
                    }
                });
            }

//            // add listener to start demo mode
//            // TODO make this part of setup assistent
//            Preference startDemoMode = findPreference("settings_demo_mode");
//            if (startDemoMode != null) {
//                startDemoMode.setOnPreferenceChangeListener((preference, newValue) -> {
//                    if ((Boolean) newValue) {
//                        // show warning
//                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                        builder.setTitle(R.string.settings_demo_mode_warning_title);
//                        builder.setMessage(R.string.settings_demo_mode_warning_text);
//                        builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
//                            // start demo mode
//                            DemoMode.setConfigurationToDemoMode(getContext());
//                        });
//                        builder.setNegativeButton(R.string.no,(dialogInterface, i) -> {
//                            // reset setting
//                            PreferenceManager.getDefaultSharedPreferences(getContext())
//                                    .edit().putBoolean(preference.getKey(),false).apply();
//                            });
//
//                        AlertDialog dialog = builder.create();
//                        dialog.show();
//                    } else {
//                        //DemoMode.clearConfiguration(getContext());
//                    }
//                    return true;
//                });
//            }
        }
    }

    public static class GeneralFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_general, rootKey);
        }
    }


    public static class PredictionsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_prediction, rootKey);
        }
    }

    public static class HelpFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_help, rootKey);

            // add listener to open online manual
            Preference manual = findPreference("settings_help_manual");
            if (manual != null) {
                manual.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // Open Webpage
                        String url = StaticData.HANDBOOK_URL;
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        return true;
                    }
                });
            }

            // add listener to send feedback
            Preference feedback = findPreference("settings_help_feedback");
            if (feedback != null) {
                feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // Open Github
                        String url = StaticData.FEEDBACK_URL;
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        return true;
                    }
                });
            }

            // add listener to show open source licenses
            Preference ossLicenses = findPreference("settings_help_osslicense");
            if (ossLicenses != null) {
                ossLicenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(getContext(), OssLicensesMenuActivity.class));
                        //OssLicensesMenuActivity.setActivityTitle(getString(R.string.custom_license_title));
                        return true;
                    }
                });
            }


            // version name
            Preference buildVersion = findPreference("settings_help_version");
            if (buildVersion != null) {
                buildVersion.setSummary(BuildConfig.VERSION_NAME);
            }
        }
    }
}
