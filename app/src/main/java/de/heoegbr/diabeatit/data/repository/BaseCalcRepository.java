package de.heoegbr.diabeatit.data.repository;

import android.content.Context;

import java.util.List;

import de.heoegbr.diabeatit.assistant.base.BaseCalc;

public class BaseCalcRepository {
    public static final String TAG = "BASE_CALC_REPOSITORY";

    private static BaseCalcRepository INSTANCE = null;

    private BaseCalcRepository(Context context){

    }

    public static BaseCalcRepository getInstance(Context context){
        synchronized (INSTANCE){
            if (INSTANCE == null){
                INSTANCE = new BaseCalcRepository(context);
            }
        }
        return INSTANCE;
    }

    public void pushCalculationResultInYourThread(List<BaseCalc.Result> result){
        //TODO implement
    }
}
