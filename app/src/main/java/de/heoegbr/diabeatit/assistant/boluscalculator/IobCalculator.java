package de.heoegbr.diabeatit.assistant.boluscalculator;

import java.time.Instant;

import de.heoegbr.diabeatit.data.container.event.BolusEvent;

public class IobCalculator {

    /**
     * Generates a multiplier to be applied to the initial bolus to result in remaining IOB
     * This is used in Oref0 but seems to work not perfectly fine :(
     *
     * @param timeAgo time span to the bolus event
     * @param dia     duration of insulin activity in minutes (usually 300 to 360 min)
     * @return multiplier to apply to the initial bolus to result in remaining insulin on board
     */
    @Deprecated
    public static double getIobMultiplierFromTimeAgo(double timeAgo, double dia) {
        double tot = 100;
        if (timeAgo <= 0.0) {
            tot = 100;
        } else if (timeAgo >= dia) {
            tot = 0.0;
        } else {
            if (dia <= 180) {
                tot = -3.203e-7 * Math.pow(timeAgo, 4) + 1.354e-4 * Math.pow(timeAgo, 3) - 1.759e-2 *
                        Math.pow(timeAgo, 2) + 9.255e-2 * timeAgo + 99.951;
            } else if (dia <= 240) {
                tot = -3.31e-8 * Math.pow(timeAgo, 4) + 2.53e-5 * Math.pow(timeAgo, 3) - 5.51e-3 *
                        Math.pow(timeAgo, 2) - 9.086e-2 * timeAgo + 99.95;
            } else if (dia <= 300) {
                tot = -2.95e-8 * Math.pow(timeAgo, 4) + 2.32e-5 * Math.pow(timeAgo, 3) - 5.55e-3 *
                        Math.pow(timeAgo, 2) + 4.49e-2 * timeAgo + 99.3;
            } else if (dia <= 360) {
                tot = -1.493e-8 * Math.pow(timeAgo, 4) + 1.413e-5 * Math.pow(timeAgo, 3) - 4.095e-3 *
                        Math.pow(timeAgo, 2) + 6.365e-2 * timeAgo + 99.7;
            }
        }
        return 1 - tot / 100;
    }

    public static double getActiveIobFromBolus(BolusEvent bolusEvent, double dia, double peak) {
        return getIobFromBolusForTimePoint(bolusEvent, Instant.now(), dia, peak);
    }

    public static double getIobFromBolusForTimePoint(BolusEvent bolusEvent, Instant timepoint, double dia, double peak) {
        double result = 0;
        double timeAgo = timepoint.minusSeconds(bolusEvent.timestamp.getEpochSecond()).getEpochSecond();
        timeAgo = timeAgo / 60; // in minutes

        if (bolusEvent.value != 0d) {
            double td = dia;
            double tp = peak;

            // force the IOB to 0 if over DIA hours have passed
            if (timeAgo < td) {
                double tau = tp * (1 - tp / td) / (1 - 2 * tp / td);
                double a = 2 * tau / td;
                double S = 1 / (1 - a + (1 + a) * Math.exp(-td / tau));
                result = bolusEvent.value * (1 - S * (1 - a) * ((Math.pow(timeAgo, 2) /
                        (tau * td * (1 - a)) - timeAgo / tau - 1) * Math.exp(-timeAgo / tau) + 1));
            }
        }

        return result;
    }
}
