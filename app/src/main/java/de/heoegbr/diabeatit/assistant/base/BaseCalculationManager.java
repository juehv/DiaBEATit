package de.heoegbr.diabeatit.assistant.base;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.heoegbr.diabeatit.data.container.event.DiaryEvent;
import de.heoegbr.diabeatit.data.repository.BaseCalcRepository;
import de.heoegbr.diabeatit.data.repository.DiaryRepository;

public class BaseCalculationManager {
    private static BaseCalculationManager INSTANCE = null;

    private final List<BaseCalc> calculations = new ArrayList<BaseCalc>();
    private final BaseCalcRepository repo;
    private final DiaryRepository diaryRepository;

    private BaseCalculationManager(Context context){
        repo = BaseCalcRepository.getInstance(context);
        diaryRepository = DiaryRepository.getRepository(context);
        registerStandardServices();
    }

    public static BaseCalculationManager getInstance(Context context){
        synchronized (INSTANCE) {
            if (INSTANCE == null) {
                INSTANCE = new BaseCalculationManager(context);
            }
        }
        return  INSTANCE;
    }

    public void registerNewCalculation(BaseCalc calculation){
        calculations.add(calculation);
    }

    private void registerStandardServices(){
        calculations.add(new IobCalc(repo));
        calculations.add(new CobCalc(repo));
        calculations.add(new SlopeCalc(repo));
        calculations.add(new BolusCalc(repo));
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
