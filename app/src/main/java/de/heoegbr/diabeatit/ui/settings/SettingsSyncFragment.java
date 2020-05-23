package de.heoegbr.diabeatit.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.Data;

import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.db.cloud.ScheduleSyncHelper;
import de.heoegbr.diabeatit.db.cloud.nightscout.NightscoutDownloader;

public class SettingsSyncFragment extends PreferenceFragmentCompat {
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
                    editText -> editText.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_URI
                            | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
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

