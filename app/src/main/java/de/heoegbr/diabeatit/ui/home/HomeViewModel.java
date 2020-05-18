package de.heoegbr.diabeatit.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import de.heoegbr.diabeatit.db.container.event.BgReadingEvent;
import de.heoegbr.diabeatit.db.repository.DiaryEventStore;

public class HomeViewModel extends AndroidViewModel {

    private final DiaryEventStore mDiaryRepository;
    private final LiveData<List<BgReadingEvent>> mBgReadings;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mDiaryRepository = DiaryEventStore.getRepository(application.getApplicationContext());
        mBgReadings = mDiaryRepository.getLiveBgEvents();
    }

    public LiveData<List<BgReadingEvent>> getBgReadings() {
        return mBgReadings;
    }
}