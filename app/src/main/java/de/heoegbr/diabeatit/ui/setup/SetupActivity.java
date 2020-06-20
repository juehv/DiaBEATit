package de.heoegbr.diabeatit.ui.setup;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.heoegbr.diabeatit.BuildConfig;
import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.data.source.demo.DemoMode;
import de.heoegbr.diabeatit.ui.home.HomeActivity;

//https://developer.android.com/training/animation/screen-slide
public class SetupActivity extends FragmentActivity {
    public static final String SETUP_COMPLETE_KEY = "setup_wizard_completed";
    public static final String SETUP_LICENCE_AGEED_KEY = "eula_agreed";
    public static final String SETUP_DATACONTRIBUTION_AGEED_KEY = "data_contribution_agreed";
    public static final String SETUP_DEMO_MODE_KEY = "demo_mode";
    private static final String TAG = "SETUP_WIZARD";


    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private DotsIndicator dotsIndicator;
    private Button prefButton;
    private Button nextButton;
    // TODO move to viewmodel?
    private static final int NUM_PAGES = 7;
    private Toolbar toolbar;
    private SharedPreferences prefs;
    private String[] viewPagerTitle;
    private Boolean[] setupWizardStageCompleted;
    private boolean wasDemoModeActiveInitially = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO check if it is an update or initial installtion (based on version code from prefs)
        // TODO check if demo mode is active && new version --> show reinstallation screen
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        wasDemoModeActiveInitially = prefs.getBoolean(SETUP_DEMO_MODE_KEY, false);

        viewPager = findViewById(R.id.pager);
        dotsIndicator = findViewById(R.id.setup_dots_indicator);
        prefButton = findViewById(R.id.setup_prev_button);
        nextButton = findViewById(R.id.setup_next_button);
        toolbar = findViewById(R.id.setup_toolbar);

        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        dotsIndicator.setViewPager2(viewPager);

        // setup title for screens
        viewPagerTitle = new String[NUM_PAGES];
        viewPagerTitle[0] = getString(R.string.setupwizard_welcome_title);
        viewPagerTitle[1] = getString(R.string.setupwizard_licenseagreement_title);
        viewPagerTitle[2] = getString(R.string.setupwizard_datacontribution_title);
        viewPagerTitle[3] = getString(R.string.setupwizard_permission_title);
        viewPagerTitle[4] = getString(R.string.setupwizard_permission_title);
        viewPagerTitle[5] = getString(R.string.setupwizard_demo_title);
        viewPagerTitle[6] = getString(R.string.setupwizard_completed_title);

        // setup next button
        // initialize from preferences
        setupWizardStageCompleted = new Boolean[NUM_PAGES];
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setupWizardStageCompleted[0] = true; // welcome always enabled
        setupWizardStageCompleted[1] = prefs.getBoolean(SETUP_LICENCE_AGEED_KEY, false);
        setupWizardStageCompleted[2] = prefs.getBoolean(SETUP_DATACONTRIBUTION_AGEED_KEY, false);
        setupWizardStageCompleted[3] =
                (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED);
        setupWizardStageCompleted[4] =
                (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED);
        setupWizardStageCompleted[5] = true; // demo mode
        setupWizardStageCompleted[6] = true; // finish screen


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (position == 0) {
                    prefButton.setVisibility(View.INVISIBLE);
                } else if (position == NUM_PAGES - 1) {
                    nextButton.setText(R.string.setupwizard_finish);
                } else {
                    nextButton.setText(R.string.next_button);
                    prefButton.setVisibility(View.VISIBLE);
                    prefButton.setVisibility(View.VISIBLE);
                }

                // set title of current screen
                if (viewPagerTitle.length >= position) {
                    toolbar.setTitle(viewPagerTitle[position]);
                }
            }
        });
    }

    public void nextButtonListener(View view) {
        int pageNo = viewPager.getCurrentItem() + 1;
        // not necessary ...
//        if (pageNo >= NUM_PAGES){
//            pageNo = NUM_PAGES;
//        }
        if (pageNo < NUM_PAGES) {
            viewPager.setCurrentItem(pageNo, true);
        } else {
            // --> finish button mode
            Context context = getApplicationContext();
            // check permissions
            if ((context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
                    || (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)) {
                // inform user that he has to give permissions to finish
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                builder.setMessage(R.string.setupwizard_error_permission_missing_text)
                        .setTitle(R.string.setupwizard_error_permission_missing_title)
                        .setPositiveButton(R.string.ok, null);
                builder.create().show();
            } else if (!prefs.getBoolean(SETUP_LICENCE_AGEED_KEY, false)) {
                // inform user
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                builder.setMessage(R.string.setupwizard_error_license_missing_text)
                        .setTitle(R.string.setupwizard_error_license_missing_title)
                        .setPositiveButton(R.string.ok, null);
                builder.create().show();
            } else if (!prefs.getBoolean(SETUP_DATACONTRIBUTION_AGEED_KEY, false)) {
                // inform user
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                builder.setMessage(R.string.setupwizard_error_datacontribution_missing_text)
                        .setTitle(R.string.setupwizard_error_datacontribution_missing_title)
                        .setPositiveButton(R.string.ok, null);
                builder.create().show();
            } else {
                // finish setup wizard
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putInt(SETUP_COMPLETE_KEY, BuildConfig.VERSION_CODE).apply();
                if (prefs.getBoolean(SETUP_DEMO_MODE_KEY, false)) {
                    DemoMode.setConfigurationToDemoModeInYourThread(context);
                }

                // restart app
//                startActivity(new Intent(SetupActivity.this, HomeActivity.class));
                // todo find a real restart function ... as this is not working
                Intent initIntent = new Intent(SetupActivity.this, HomeActivity.class);
                PendingIntent restartIntent = PendingIntent.getActivity(context,
                        255, initIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, restartIntent);

                finishAffinity();
                //finishAndRemoveTask();
            }
        }

    }

    public void previousButtonListener(View view) {
        int pageNo = viewPager.getCurrentItem() - 1;
        viewPager.setCurrentItem(pageNo, true);
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        private Executor executor = Executors.newSingleThreadExecutor();

        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            Context applicationContext = getApplicationContext();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
            //Log.e(TAG, "called");
            Fragment returnFragment = null;
            switch (position) {
                case 0: // welcome
                    returnFragment = new SetupWelcomeFragment(
                            R.string.setupwizard_welcome_text,
                            false, 0, null, false);
                    break;
                case 1: // License
                    returnFragment = new SetupWelcomeFragment(
                            R.string.setupwizard_licenseagreement_text,
                            true,
                            R.string.i_understand_and_agree,
                            (compoundButton, b) -> {
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                        .edit().putBoolean(SETUP_LICENCE_AGEED_KEY, b).apply();
                                setupWizardStageCompleted[1] = b;
                            },
                            prefs.getBoolean(SETUP_LICENCE_AGEED_KEY, false));
                    break;
                case 2: // User data contribution
                    returnFragment = new SetupWelcomeFragment(
                            R.string.setupwizard_datacontribution_text,
                            true,
                            R.string.i_understand_and_agree,
                            (compoundButton, b) -> {
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                        .edit().putBoolean(SETUP_DATACONTRIBUTION_AGEED_KEY, b).apply();
                                setupWizardStageCompleted[2] = b;
                            },
                            prefs.getBoolean(SETUP_DATACONTRIBUTION_AGEED_KEY, false));
                    break;
                case 3: // Ask for storage permission
                    boolean storagePermission = applicationContext.checkSelfPermission(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED;

                    returnFragment = new SetupWelcomeFragment(
                            R.string.setupwizard_storagepermission_text,
                            true,
                            R.string.permission,
                            (compoundButton, b) -> {
                                // ask for permission and reset if failed
                                if (b) {
                                    // check for permission
                                    if (applicationContext != null
                                            && (applicationContext.checkSelfPermission(
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED)) {
                                        // ask for permission, rationale already shown
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                45);
                                    }
                                }
                            }, storagePermission);
                    break;
                case 4: // Ask for Location permission
                    boolean locationPermission = applicationContext.checkSelfPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;
                    returnFragment = new SetupWelcomeFragment(
                            R.string.setupwizard_locationpermission_text,
                            true,
                            R.string.permission,
                            (compoundButton, b) -> {
                                // ask for permission and reset if failed
                                if (b) {
                                    // check for permission
                                    if (applicationContext != null && (
                                            applicationContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                                    != PackageManager.PERMISSION_GRANTED)) {
                                        // ask for permission, rationale already shown
                                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                44);
                                    }
                                }
                            }, locationPermission);
                    break;
                case 5: // Ask for Demo Mode
                    returnFragment = new SetupWelcomeFragment(
                            R.string.setupwizard_demomode_text,
                            true,
                            R.string.activate,
                            (compoundButton, b) -> {

                                if (wasDemoModeActiveInitially
                                        && !b) {
                                    // when user wants to deactive demo mode after app was running in demo mode
//TODO show message
                                    // demo mode was active before and wrote data to the database
                                    // please reinstall the app to deactivate demo mode
                                    // demo mode stays active
                                    compoundButton.setChecked(true);
                                } else {
                                    // setup app for demo mode
                                    prefs.edit().putBoolean(SETUP_DEMO_MODE_KEY, b).apply();
                                }
                            },
                            prefs.getBoolean(SETUP_DEMO_MODE_KEY, false));
                    break;
                case 6: // thank you
                    returnFragment = new SetupWelcomeFragment(
                            R.string.setupwizard_thankyou_text,
                            false, 0, null, false);
                    break;

            }
            return returnFragment;
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }


    }


}
