package de.heoegbr.diabeatit.ui.setup;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import de.heoegbr.diabeatit.BuildConfig;
import de.heoegbr.diabeatit.R;
import de.heoegbr.diabeatit.ui.home.HomeActivity;

//https://developer.android.com/training/animation/screen-slide
public class SetupActivity extends FragmentActivity {
    public static final String SETUP_COMPLETE_KEY = "setup_wizard_completed";
    private static final String TAG = "SETUP_WIZARD";
    private static final int NUM_PAGES = 5;
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private DotsIndicator dotsIndicator;
    private Button prefButton;
    private Button nextButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        viewPager = findViewById(R.id.pager);
        dotsIndicator = findViewById(R.id.setup_dots_indicator);
        prefButton = findViewById(R.id.setup_prev_button);
        nextButton = findViewById(R.id.setup_next_button);

        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        dotsIndicator.setViewPager2(viewPager);

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
            // finish button mode
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                    .putInt(SETUP_COMPLETE_KEY, BuildConfig.VERSION_CODE).apply();

            startActivity(new Intent(SetupActivity.this, HomeActivity.class));
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
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            //Log.e(TAG, "called");
            return new SetupWelcomeFragment();
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }


    }


}
