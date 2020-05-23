package de.heoegbr.diabeatit.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import de.heoegbr.diabeatit.db.container.event.BgReadingEvent;
import de.heoegbr.diabeatit.db.repository.DiaryRepository;

public class HomeViewModel extends AndroidViewModel {

    private final DiaryRepository mDiaryRepository;
    private final LiveData<List<BgReadingEvent>> mBgReadings;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mDiaryRepository = DiaryRepository.getRepository(application.getApplicationContext());
        mBgReadings = mDiaryRepository.getLiveBgEvents();
    }

    public LiveData<List<BgReadingEvent>> getBgReadings() {
        return mBgReadings;
    }
}