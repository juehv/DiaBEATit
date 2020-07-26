package de.heoegbr.diabeatit.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;

import java.lang.ref.WeakReference;

import de.heoegbr.diabeatit.DiaBEATitApp;
import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.StaticData;
import de.heoegbr.diabeatit.assistant.alert.AlertStoreListener;
import de.heoegbr.diabeatit.assistant.alert.AlertsManager;
import de.heoegbr.diabeatit.data.container.Alert;
import de.heoegbr.diabeatit.data.repository.AlertStore;
import de.heoegbr.diabeatit.ui.AlertHistoryActivity;
import de.heoegbr.diabeatit.ui.diary.DiaryActivity;
import de.heoegbr.diabeatit.ui.diary.ManualCarbsEntryActivity;
import de.heoegbr.diabeatit.ui.diary.ManualInsulinEntryActivity;
import de.heoegbr.diabeatit.ui.diary.ManualNoteActivity;
import de.heoegbr.diabeatit.ui.diary.ManualSportsEntryActivity;
import de.heoegbr.diabeatit.ui.settings.SettingsActivity;
import de.heoegbr.diabeatit.ui.setup.SetupActivity;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HOME_ACTIVITY";
    private static WeakReference<HomeActivity> instance;
    public NestedScrollView assistantPeekEnveloped;

    private AppBarConfiguration mAppBarConfiguration;
    private FloatingActionsMenu entryMenu;

    private AlertStore mAlertStore; //TODO model view?

    public static HomeActivity getInstance() {
        return instance.get();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // don't build this gui if we go to setup wizard
        if (!DiaBEATitApp.isPermissionsGrandedAndSetupWizardCompleted(getApplicationContext())) {
            startActivity(new Intent(HomeActivity.this, SetupActivity.class));
        } else {
            mAlertStore = AlertStore.getRepository(getApplicationContext());

            setContentView(R.layout.d_activity_home);
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(getResources().getString(R.string.title_activity_home));
            setSupportActionBar(toolbar);

            // TODO what is this good for ?
            getSystemService(android.app.NotificationManager.class).cancelAll();

            assistantPeekEnveloped = findViewById(R.id.assistant_scrollview);

            setupManualEntry();
            setupAssistant();
            setupDrawer();

            Intent intent = getIntent();
            if (intent != null && intent.getAction() != null && intent.getAction().equals(StaticData.ASSISTANT_INTENT_CODE))
                expandAssistant();
        }

        instance = new WeakReference<>(this);
    }

    private void setupManualEntry() {
        entryMenu = findViewById(R.id.manual_entry_fab_menu);
        FloatingActionButton manualInsulinButton = findViewById(R.id.fab_manual_insulin);
        FloatingActionButton manualCarbsButton = findViewById(R.id.fab_manual_carbs);
        FloatingActionButton manualSportsButton = findViewById(R.id.fab_manual_sports);
        FloatingActionButton manualNoteButton = findViewById(R.id.fab_note);

        manualInsulinButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ManualInsulinEntryActivity.class));
            entryMenu.collapseImmediately();
        });

        manualCarbsButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ManualCarbsEntryActivity.class));
            entryMenu.collapseImmediately();
        });

        manualSportsButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ManualSportsEntryActivity.class));
            entryMenu.collapseImmediately();
        });

        manualNoteButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ManualNoteActivity.class));
            entryMenu.collapseImmediately();
        });
    }

    private void setupAssistant() {
        final View nestedScrollView = findViewById(R.id.assistant_scrollview);
        final BottomSheetBehavior assistant = BottomSheetBehavior.from(nestedScrollView);
        final RelativeLayout assistantPeek = findViewById(R.id.assistant_peek);
        final RelativeLayout assistantPeekAlt = findViewById(R.id.assistant_peek_alt);
        final TextView assistantCloseHint = findViewById(R.id.assistant_close_hint);

        assistant.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                entryMenu.setVisibility(newState == BottomSheetBehavior.STATE_COLLAPSED ? View.VISIBLE : View.GONE);
                entryMenu.collapseImmediately();

                assistantPeek.setVisibility(newState == BottomSheetBehavior.STATE_COLLAPSED ? View.VISIBLE : View.GONE);
                assistantPeekAlt.setVisibility(newState != BottomSheetBehavior.STATE_COLLAPSED ? View.VISIBLE : View.GONE);
                assistantCloseHint.setVisibility(newState == BottomSheetBehavior.STATE_EXPANDED ? View.VISIBLE : View.GONE);

                if (StaticData.assistantInhibitClose && newState == BottomSheetBehavior.STATE_DRAGGING)
                    assistant.setState(BottomSheetBehavior.STATE_EXPANDED);

                StaticData.assistantInhibitClose = false;
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
            }
        });

        assistantPeek.setOnClickListener(view -> assistant.setState(BottomSheetBehavior.STATE_EXPANDED));

        mAlertStore.alertsManager = new AlertsManager(getApplicationContext(), findViewById(R.id.assistant_card_list), findViewById(R.id.alert_cardview));

        Button alertClearB = findViewById(R.id.alert_clear_all);
        TextView alertEmptyT = findViewById(R.id.alert_empty_notice);

        alertClearB.setOnClickListener(view -> mAlertStore.clearAlerts());

        Runnable peekUpdater = () -> {
            TextView titleV = assistantPeek.findViewById(R.id.assistant_peek_title);
            TextView descV = assistantPeek.findViewById(R.id.assistant_peek_description);
            ImageView iconV = assistantPeek.findViewById(R.id.assistant_status_icon);

            Alert.URGENCY urgency = mAlertStore.getActiveAlerts().stream()
                    .map(a -> a.urgency).reduce((a, b) -> a.getPriority() > b.getPriority() ? a : b)
                    .orElse(Alert.URGENCY.INFO);
            int amount = (int) mAlertStore.getActiveAlerts().stream().filter(a -> a.urgency.equals(urgency)).count();

            int color = amount == 0 ? getColor(android.R.color.holo_green_light) : getColor(urgency.getRawColor());
            String title = amount == 0 ? getString(R.string.assistant_peek_title_none) : getString(urgency.getPeekTitle());
            String desc = mAlertStore.getActiveAlerts().size() + " " + getString(R.string.assistant_peek_description);
            Drawable icon = amount == 0 ? getDrawable(R.drawable.ic_check) : getDrawable(R.drawable.ic_alert);

            assistantPeek.setBackgroundColor(color);
            titleV.setText(title);
            descV.setText(desc);
            iconV.setImageDrawable(icon);
        };

        mAlertStore.attachListener(new AlertStoreListener() {
            @Override
            public void onNewAlert(Alert alert) {
                alertClearB.setVisibility(View.VISIBLE);
                alertEmptyT.setVisibility(View.GONE);

                peekUpdater.run();
            }

            @Override
            public void onAlertDismissed(Alert alert) {
                peekUpdater.run();
            }

            @Override
            public void onAlertRestored(Alert alert) {
                onNewAlert(alert);
            }

            @Override
            public void onAlertsCleared() {
                alertClearB.setVisibility(View.GONE);
                alertEmptyT.setVisibility(View.VISIBLE);

                peekUpdater.run();
            }

            @Override
            public void onDataSetInit() {
                int len = mAlertStore.getActiveAlerts().size();
                alertClearB.setVisibility(len == 0 ? View.GONE : View.VISIBLE);
                alertEmptyT.setVisibility(len == 0 ? View.VISIBLE : View.GONE);

                peekUpdater.run();
            }
        });

        CardView alertHistoryC = findViewById(R.id.alert_history);
        alertHistoryC.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, AlertHistoryActivity.class)));

        findViewById(R.id.alert_settings).setOnClickListener(
                view -> {
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                    startActivity(intent);
                }
        );

        findViewById(R.id.assistant_card_list).setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                StaticData.assistantInhibitClose = true;
            }

            @Override
            public void onSwipeRight() {
                StaticData.assistantInhibitClose = true;
            }
        });
    }

    private void setupDrawer() {
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_assistant)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navView.setNavigationItemSelectedListener(menuItem -> {
            drawer.closeDrawers();

            switch (menuItem.getItemId()) {
                case R.id.nav_assistant:
                    expandAssistant();
                    break;

                case R.id.nav_log:
                    startActivity(new Intent(HomeActivity.this, DiaryActivity.class));
                    break;

                case R.id.nav_settings:
                    if (PreferenceManager.getDefaultSharedPreferences(this)
                            .getBoolean(SetupActivity.SETUP_DEMO_MODE_KEY, false)) {
                        Toast.makeText(this, "Disabled in Demo Mode.", Toast.LENGTH_LONG).show();
                    } else {
                        startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                    }
                    break;

                case R.id.nav_help:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(StaticData.HANDBOOK_URL)));
                    break;
            }

            return true;
        });
    }

    private void expandAssistant() {
        View nestedScrollView = findViewById(R.id.assistant_scrollview);
        final BottomSheetBehavior assistant = BottomSheetBehavior.from(nestedScrollView);
        final RelativeLayout assistantPeek = findViewById(R.id.assistant_peek);
        final RelativeLayout assistantPeekAlt = findViewById(R.id.assistant_peek_alt);

        entryMenu.setVisibility(View.GONE);
        entryMenu.collapseImmediately();

        assistant.setState(BottomSheetBehavior.STATE_EXPANDED);
        assistantPeek.setVisibility(View.GONE);
        assistantPeekAlt.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        View nestedScrollView = findViewById(R.id.assistant_scrollview);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final BottomSheetBehavior assistant = BottomSheetBehavior.from(nestedScrollView);

        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else if (assistant.getState() == BottomSheetBehavior.STATE_EXPANDED)
            assistant.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else if (HomeFragment.getInstance().isExpanded()) {
            HomeFragment.getInstance().reduceView();
            entryMenu.setVisibility(View.VISIBLE);
            assistantPeekEnveloped.setVisibility(View.VISIBLE);
        } else
            super.onBackPressed();
    }

    /*
            Closes entry menu when user clicks somewhere else
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && entryMenu.isExpanded()) {
            Rect outRect = new Rect();
            entryMenu.getGlobalVisibleRect(outRect);

            if (!outRect.contains((int) event.getRawX(), (int) event.getRawY()))
                entryMenu.collapse();
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //FIXME use somewhere else ?
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }
    }

    // this method is called by fragment_bolus_calculator's text view
    public void onClickBolusExpand(View v) {
        HomeFragment homeFragment = HomeFragment.getInstance();
        if (homeFragment == null) return;

        if (homeFragment.isExpanded()) {
            homeFragment.reduceView();
            entryMenu.setVisibility(View.VISIBLE);
            assistantPeekEnveloped.setVisibility(View.VISIBLE);
        } else {
            homeFragment.expandView();
            entryMenu.setVisibility(View.GONE);
            assistantPeekEnveloped.setVisibility(View.GONE);
        }
    }


}

/* Class from https://gist.github.com/nesquena/ed58f34791da00da9751 under MIT license */
class OnSwipeTouchListener implements View.OnTouchListener {

    private GestureDetector gestureDetector;

    OnSwipeTouchListener(Context c) {
        gestureDetector = new GestureDetector(c, new GestureListener());
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        view.performClick(); //FIXME <-- added because of warning .. will it still work ?
        return gestureDetector.onTouchEvent(motionEvent);
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeUp() {
    }

    public void onSwipeDown() {
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // Determines the fling velocity and then fires the appropriate swipe event accordingly
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeDown();
                        } else {
                            onSwipeUp();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

}