package de.heoegbr.diabeatit.assistant.base;

import java.util.List;

import de.heoegbr.diabeatit.data.repository.BaseCalcRepository;

public class IobCalc extends BaseCalc {
    public static final String TAG = "IOB_CALC";

    public IobCalc(BaseCalcRepository repo) {
        super(TAG,repo);
    }

    @Override
    public List<Result> runCalculation(DataContainer data) {
        return null;
    }

    @Override
    public boolean checkData(DataContainer data) {
        return false;
    }
}
