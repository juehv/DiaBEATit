package de.heoegbr.diabeatit.assistant.base;

import java.util.List;

import de.heoegbr.diabeatit.data.repository.BaseCalcRepository;

/**
 * Container class to run a bolus calculator
 */
public class BolusCalc  extends BaseCalc{
    public static final String TAG = "IOB_CALC";

    public BolusCalc(BaseCalcRepository repo) {
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
