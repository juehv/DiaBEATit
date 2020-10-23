package de.heoegbr.diabeatit.assistant.boluscalculator;

public class BolusCalculatorResult {
    private double bolus;
    private String resultAsText;

    public static String convertBolusValueToText(double value, double icr) {
        if (value >= 0.0) {
            return String.format("%.2f", value) + " I.E.";
        } else {
            return String.format("%.2f", icr * value * -1) + " g";
        }
    }

    public double getBolus() {
        return bolus;
    }

    public void setBolus(double bolus, double icr) {
        this.bolus = bolus;
        this.resultAsText = convertBolusValueToText(bolus, icr);
    }

    public String getResultAsText() {
        return resultAsText;
    }
}
