package de.heoegbr.diabeatit.assistant.boluscalculator;


// for later https://diabetes-leben.com/2019/03/so-bestimmst-du-den-isf-insulin-sensitive-factor.html
// for definition of isf https://www.diabetesselfmanagement.com/diabetes-resources/definitions/insulin-sensitivity-factor/
public class SimpleBolusCalculator {
    private final double mIsf;
    private final double mIcr;

    public SimpleBolusCalculator(double mTarget, double mIsf, double mIcr) {
        this.mIsf = mIsf;
        this.mIcr = mIcr;
    }

    public BolusCalculatorResult calculateBolus(double bg, double target, double iob, double carb, double correction) {
        BolusCalculatorResult result = new BolusCalculatorResult();
        result.bolus = (bg - target) / mIsf + carb / mIcr + correction - iob;
        result.resultAsText = convertBolusValueToText(result.bolus);
        return result;
    }

    public String convertBolusValueToText(double value) {
        if (value >= 0.0) {
            return String.format("%.2f", value) + " I.E.";
        } else {
            return String.format("%.2f", mIcr * value * -1) + " g";
        }
    }
}
