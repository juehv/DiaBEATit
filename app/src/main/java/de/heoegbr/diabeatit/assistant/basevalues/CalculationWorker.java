package de.heoegbr.diabeatit.assistant.basevalues;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.heoegbr.diabeatit.data.container.event.DiaryEvent;
import de.heoegbr.diabeatit.data.repository.CalculationsRepository;
import de.heoegbr.diabeatit.data.repository.DiaryRepository;

public class CalculationWorker {
    private static CalculationWorker INSTANCE = null;

    private final List<BaseCalc> calculations = new ArrayList<BaseCalc>();
    private final CalculationsRepository repo;
    private final DiaryRepository diaryRepository;

    private CalculationWorker(Context context){
        repo = CalculationsRepository.getInstance(context);
        diaryRepository = DiaryRepository.getRepository(context);

        calculations.add(new IobCobSlopBolusCalc(repo));
    }

    public static CalculationWorker getInstance(Context context){
        synchronized (INSTANCE) {
            if (INSTANCE == null) {
                INSTANCE = new CalculationWorker(context);
            }
        }
        return  INSTANCE;
    }

    public void registerNewCalculation(BaseCalc calculation){
        calculations.add(calculation);
    }


    public void run(){
        List<DiaryEvent> events = diaryRepository.getEvents();
        for (BaseCalc item : calculations){
            BaseCalc.DataContainer container = new BaseCalc.DataContainer();
            container.events = events;
            item.runInOwnThreadAndPushToDb(container);
        }
    }
}
