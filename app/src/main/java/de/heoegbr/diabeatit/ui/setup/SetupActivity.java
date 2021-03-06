package de.heoegbr.diabeatit.ui.setup;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.heoegbr.diabeatit.BuildConfig;
import de.heoegbr.diabeatit.DiaBEATitApp;
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

    public static final int SETUP_PAGE_WELCOME = 0;
    public static final int SETUP_PAGE_LICENSE = 1;
    public static final int SETUP_PAGE_CONTRIBUTION = 2;
    public static final int SETUP_PAGE_STORAGE = 3;
    public static final int SETUP_PAGE_LOCATION = 4;
    public static final int SETUP_PAGE_CAMERA = 5;
    public static final int SETUP_PAGE_PROFILE = 6;
    public static final int SETUP_PAGE_DONE = 7;


    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private DotsIndicator dotsIndicator;
    private Button prefButton;
    private Button nextButton;
    // TODO move to viewmodel?
    private static final int NUM_PAGES = 8;
//    private Toolbar toolbar;
    private SharedPreferences prefs;
    private String[] viewPagerTitle;
    private Boolean[] setupWizardStageCompleted;
    private boolean wasDemoModeActiveInitially = false;
    private HashMap<Integer, Fragment> fragmentStorage = new HashMap<>();

    @Override
    public void onBackPressed() {
        if (viewPager != null) {
            int pageNo = viewPager.getCurrentItem() - 1;
            viewPager.setCurrentItem(pageNo, true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO check if it is an update or initial installtion (based on version code from prefs)
        // TODO check if demo mode is active && new version --> show reinstallation screen
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_wizard);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        wasDemoModeActiveInitially = prefs.getBoolean(SETUP_DEMO_MODE_KEY, false);

        viewPager = findViewById(R.id.pager);
        dotsIndicator = findViewById(R.id.setup_dots_indicator);
        prefButton = findViewById(R.id.setup_prev_button);
        nextButton = findViewById(R.id.setup_next_button);
//        toolbar = findViewById(R.id.setup_toolbar);

        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        dotsIndicator.setViewPager2(viewPager);

        // setup title for screens
//        viewPagerTitle = new String[NUM_PAGES];
//        viewPagerTitle[0] = getString(R.string.setupwizard_welcome_title);
//        viewPagerTitle[1] = getString(R.string.setupwizard_licenseagreement_title);
//        viewPagerTitle[2] = getString(R.string.setupwizard_datacontribution_title);
//        viewPagerTitle[3] = getString(R.string.setupwizard_permission_title);
//        viewPagerTitle[4] = getString(R.string.setupwizard_permission_title);
//        viewPagerTitle[5] = getString(R.string.setupwizard_demo_title);
//        viewPagerTitle[6] = getString(R.string.setupwizard_completed_title);

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
                    nextButton.setText(R.string.finish_button);
                } else {
                    nextButton.setText(R.string.next_button);
                    prefButton.setVisibility(View.VISIBLE);
                    prefButton.setVisibility(View.VISIBLE);
                }

                // set title of current screen
//                if (viewPagerTitle.length >= position) {
////                    toolbar.setTitle(viewPagerTitle[position]);
//                }
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
                    != PackageManager.PERMISSION_GRANTED)
                    || (context.checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
            ) {
                // inform user that he has to give permissions to finish
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                builder.setMessage(R.string.setupwizard_error_permission_missing_text)
                        .setTitle(R.string.setupwizard_error_permission_missing_title)
                        .setPositiveButton(R.string.ok_button, null);
                builder.create().show();
            } else if (!prefs.getBoolean(SETUP_LICENCE_AGEED_KEY, false)) {
                // inform user
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                builder.setMessage(R.string.setupwizard_error_license_missing_text)
                        .setTitle(R.string.setupwizard_error_license_missing_title)
                        .setPositiveButton(R.string.ok_button, null);
                builder.create().show();
            } else if (!prefs.getBoolean(SETUP_DATACONTRIBUTION_AGEED_KEY, false)) {
                // inform user
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                builder.setMessage(R.string.setupwizard_error_datacontribution_missing_text)
                        .setTitle(R.string.setupwizard_error_datacontribution_missing_title)
                        .setPositiveButton(R.string.ok_button, null);
                builder.create().show();
            } else {
                // finish setup wizard
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putInt(SETUP_COMPLETE_KEY, BuildConfig.VERSION_CODE).apply();
                if (prefs.getBoolean(SETUP_DEMO_MODE_KEY, false)) {
                    DemoMode.setConfigurationToDemoModeInYourThread(context);
                }

                // initialize App
                if (context instanceof DiaBEATitApp) {
                    ((DiaBEATitApp) context).initializeApp(context);
                    startActivity(new Intent(SetupActivity.this, HomeActivity.class));
                } else {
                    // hack .. will most probably restart the app
                    System.exit(0);
                }
            }
        }

    }

    public void previousButtonListener(View view) {
        int pageNo = viewPager.getCurrentItem() - 1;
        viewPager.setCurrentItem(pageNo, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SETUP_PAGE_STORAGE:
            case SETUP_PAGE_LOCATION:
            case SETUP_PAGE_CAMERA:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }  else {
                    // reset slider of request
                    Fragment wizardPage = fragmentStorage.get(requestCode);
                    if (wizardPage instanceof SetupWelcomeFragment){
                        ((SetupWelcomeFragment) wizardPage).resetSwitch();
                    }
                }
                return;
            default:
                super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
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
                case SETUP_PAGE_WELCOME: // welcome
                    returnFragment = new SetupWelcomeFragment(
                            R.string.setupwizard_welcome_text,
                            false, 0, null, false);
                    break;
                case SETUP_PAGE_LICENSE: // License
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
                case SETUP_PAGE_CONTRIBUTION: // User data contribution
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
                case SETUP_PAGE_STORAGE: // Ask for storage permission
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
                                                SETUP_PAGE_STORAGE);
                                    }
                                }
                            }, storagePermission);
                    break;
                case SETUP_PAGE_LOCATION: // Ask for Location permission
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
                                                SETUP_PAGE_LOCATION);
                                    }
                                }
                            }, locationPermission);
                    break;
//                case 5: // Ask for Demo Mode
//                    returnFragment = new SetupWelcomeFragment(
//                            R.string.setupwizard_camera_text,
//                            true,
//                            R.string.activate,
//                            (compoundButton, b) -> {
//
//                                if (wasDemoModeActiveInitially
//                                        && !b) {
//                                    // when user wants to deactive demo mode after app was running in demo mode
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
//                                    builder.setMessage(R.string.setupwizard_demomode_warning_text)
//                                            .setTitle(R.string.setupwizard_demomode_warning_title)
//                                            .setPositiveButton(R.string.ok_button, null);
//                                    builder.create().show();
//                                    compoundButton.setChecked(true);
//                                } else {
//                                    // setup app for demo mode
//                                    prefs.edit().putBoolean(SETUP_DEMO_MODE_KEY, b).apply();
//                                }
//                            },
//                            prefs.getBoolean(SETUP_DEMO_MODE_KEY, false));
//                    break;

                case SETUP_PAGE_CAMERA: // Ask for Camera Permission
                    boolean cameraPermission = applicationContext.checkSelfPermission(
                            Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED;
                    returnFragment = new SetupWelcomeFragment(
                            R.string.setupwizard_camera_text,
                            true,
                            R.string.permission,
                            (compoundButton, b) -> {
                                // ask for permission and reset if failed
                                if (b) {
                                    // check for permission
                                    if (applicationContext != null && (
                                            applicationContext.checkSelfPermission(Manifest.permission.CAMERA)
                                                    != PackageManager.PERMISSION_GRANTED)) {
                                        // ask for permission, rationale already shown
                                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                                SETUP_PAGE_CAMERA);
                                    }
                                }
                            }, cameraPermission);
                    break;
                case SETUP_PAGE_PROFILE:
                    //TODO implement
                    returnFragment = new SetupProfileFragment();
                    break;
                case SETUP_PAGE_DONE: // thank you
                    returnFragment = new SetupWelcomeFragment(
                            R.string.setupwizard_thankyou_text,
                            false, 0, null, false);
                    break;

            }
            if (returnFragment != null){
                fragmentStorage.put(position,returnFragment);
            }
            return returnFragment;

        }



        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }


    }


}
