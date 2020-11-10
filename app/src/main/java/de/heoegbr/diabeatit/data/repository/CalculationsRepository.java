package de.heoegbr.diabeatit.data.repository;

import android.content.Context;

import java.util.List;

import de.heoegbr.diabeatit.assistant.basevalues.BaseCalc;

public class CalculationsRepository {
    public static final String TAG = "BASE_CALC_REPOSITORY";

    private static CalculationsRepository INSTANCE = null;

    private CalculationsRepository(Context context){

    }

    public static CalculationsRepository getInstance(Context context){
        synchronized (INSTANCE){
            if (INSTANCE == null){
                INSTANCE = new CalculationsRepository(context);
            }
        }
        return INSTANCE;
    }

    public void pushCalculationResultInYourThread(List<BaseCalc.Result> result){
        //TODO implement
    }
}
