package de.heoegbr.diabeatit.assistant.basevalues;

import java.util.List;

import de.heoegbr.diabeatit.data.repository.CalculationsRepository;

public class IobCobSlopBolusCalc extends BaseCalc {
    public static final String TAG = "IOB_CALC";

    public IobCobSlopBolusCalc(CalculationsRepository repo) {
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
