package de.heoegbr.diabeatit.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import java.util.List;

import de.heoegbr.diabeatit.db.container.event.BasalEvent;
import de.heoegbr.diabeatit.db.container.event.BgReadingEvent;
import de.heoegbr.diabeatit.db.container.event.BolusEvent;
import de.heoegbr.diabeatit.db.container.event.CarbEvent;
import de.heoegbr.diabeatit.db.container.event.DiaryEvent;
import de.heoegbr.diabeatit.db.container.event.SportsEvent;
import de.heoegbr.diabeatit.db.repository.DiaryRepository;

public class HomeViewModel extends AndroidViewModel {

    private final DiaryRepository mDiaryRepository;
    private final LiveData<List<BgReadingEvent>> mBgReadings;
    private final LiveData<List<BolusEvent>> mBolusEvents;
    private final LiveData<List<BasalEvent>> mBasalEvents;
    private final LiveData<List<CarbEvent>> mCarbEvents;
    private final LiveData<List<SportsEvent>> mSportEvents;

    private final MediatorLiveData<List<DiaryEvent>> mixed = new MediatorLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mDiaryRepository = DiaryRepository.getRepository(application.getApplicationContext());
        mBgReadings = mDiaryRepository.getLiveBgEvents();
        mBolusEvents = mDiaryRepository.getLiveBolusEvents();
        mBasalEvents = mDiaryRepository.getLiveBasalEvents();
        mCarbEvents = mDiaryRepository.getLiveCarbEvents();
        mSportEvents = mDiaryRepository.getLiveSportsEvents();

        mixed.addSource(mBgReadings, bgReadingEvents -> {
            mixed.setValue(mDiaryRepository.getPlotEvents());
        });
        mixed.addSource(mBolusEvents, bolusEvents -> {
            mixed.setValue(mDiaryRepository.getPlotEvents());
        });
        mixed.addSource(mBasalEvents, basalEvents -> {
            mixed.setValue(mDiaryRepository.getPlotEvents());
        });
        mixed.addSource(mCarbEvents, carbEvents -> {
            mixed.setValue(mDiaryRepository.getPlotEvents());
        });
        mixed.addSource(mSportEvents, sportsEvents -> {
            mixed.setValue(mDiaryRepository.getPlotEvents());
        });
    }

    public void observeData(LifecycleOwner owner, Observer<List<DiaryEvent>> observer) {
        mixed.observe(owner, observer);
    }
}