package de.heoegbr.diabeatit.assistant.boluscalculator.constrain;

public abstract class AbstractConstrain {
    protected final double mStartValue;
    protected double mResultValue;

    protected AbstractConstrain(double value) {
        this.mStartValue = value;
    }

    public abstract AbstractConstrain apply();

    public double toValue() {
        return mResultValue;
    }
}
