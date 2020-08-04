package de.heoegbr.diabeatit.assistant.prediction;

/**
 * Predicts BG value based on historic bg values, bolus and carb events.
 * Inspired by implementation of GlycoDyn (http://perceptus.org/about/glucodyn)
 * and OpenAPS implementation
 * Converted from DiaBEATit Python implementation.
 */
public class ModelGlucodyn {
//# Predicts BG value based on historic bg values, bolus and carb events
//class BgPredict:
//
//        ## carbtype = 60|90|120
//        ## sensf =  1|2|3|4
//        ## idur = 180|240|300|360
//        ## cratio = real
//        def __init__(self, carbtype=120, sensf=1, idur=180, cratio=1):
//        self.carbtype = carbtype
//        self.sensf = sensf
//        self.idur = idur
//        self.cratio = cratio
//
//        self.cgmhistory = np.zeros(30)
//        self.carbEvents = np.zeros(30)
//        self.bolusEvents = np.zeros(30)
//        self.sensfValues = np.ones(30) * sensf
//        self.predictionsteps = 12
//        self.simulationinterval = 5
//

    private final double carbAbsorptionTime; // 60/90/120
    private final double sensitivityFactor; // 1/2/3/4 //fixme is this insulin sensitivity?
    private final double insulinDuration; // 180/240/300/360
    private final double carbRatio;

    public ModelGlucodyn(double carbAbsorptionTime, double sensitivityFactor, double insulinDuration, double carbRatio) {
        this.carbAbsorptionTime = carbAbsorptionTime;
        this.sensitivityFactor = sensitivityFactor;
        this.insulinDuration = insulinDuration;
        this.carbRatio = carbRatio;
    }

//    public ModelGlucodyn() {
//        this(120.0, 1.0, 180.0, 1.0);
//    }

//        # Insulin on Board Advanced
//        # g = time in minutes from bolus event
//        # idur = insulin duration
//
//        # Insulin on Board
//        # g = time in minutes from bolus event
//        # idur = insulin duration
//        def iob(self, g, idur):
//        tot = 100
//        if g <= 0.0:
//        tot = 100
//        elif g >= idur:
//        tot = 0.0
//        else:
//        if idur <= 180:
//        tot = -3.203e-7 * pow(g, 4) + 1.354e-4 * pow(g, 3) - 1.759e-2 * pow(g,
//        2) + 9.255e-2 * g + 99.951
//        elif idur <= 240:
//        tot = -3.31e-8 * pow(g, 4) + 2.53e-5 * pow(g, 3) - 5.51e-3 * pow(g,
//        2) - 9.086e-2 * g + 99.95
//        elif idur <= 300:
//        tot = -2.95e-8 * pow(g, 4) + 2.32e-5 * pow(g, 3) - 5.55e-3 * pow(g,
//        2) + 4.49e-2 * g + 99.3
//        elif idur <= 360:
//        tot = -1.493e-8 * pow(g, 4) + 1.413e-5 * pow(g, 3) - 4.095e-3 * pow(g, 2) + 6.365e-2 * g + 99.7
//        return tot

    /**
     * Return an IOB multiplyer to get current iob from a bolus event. This will not work for insulin duration > 6h
     *
     * @param minutesAfterEvent
     * @param insulinDuration
     * @return
     */
    public static double getInsulinOnBoardMultiplier(double minutesAfterEvent, double insulinDuration) {
        double tot = 100.0;
        if (minutesAfterEvent <= 0.0) {
            tot = 100.0;
        } else if (minutesAfterEvent >= insulinDuration) {
            tot = 0.0;
        } else {
            if (insulinDuration <= 180) {
                tot = -3.203e-7 * Math.pow(minutesAfterEvent, 4)
                        + 1.354e-4 * Math.pow(minutesAfterEvent, 3)
                        - 1.759e-2 * Math.pow(minutesAfterEvent, 2)
                        + 9.255e-2 * minutesAfterEvent
                        + 99.951;
            } else if (insulinDuration <= 240) {
                tot = -3.31e-8 * Math.pow(minutesAfterEvent, 4)
                        + 2.53e-5 * Math.pow(minutesAfterEvent, 3)
                        - 5.51e-3 * Math.pow(minutesAfterEvent, 2)
                        - 9.086e-2 * minutesAfterEvent
                        + 99.95;
            } else if (insulinDuration <= 300) {
                tot = -2.95e-8 * Math.pow(minutesAfterEvent, 4)
                        + 2.32e-5 * Math.pow(minutesAfterEvent, 3)
                        - 5.55e-3 * Math.pow(minutesAfterEvent, 2)
                        + 4.49e-2 * minutesAfterEvent
                        + 99.3;
            } else if (insulinDuration <= 360) {
                tot = -1.493e-8 * Math.pow(minutesAfterEvent, 4)
                        + 1.413e-5 * Math.pow(minutesAfterEvent, 3)
                        - 4.095e-3 * Math.pow(minutesAfterEvent, 2)
                        + 6.365e-2 * minutesAfterEvent
                        + 99.7;
            }
        }
        //Log.e("debug", "IOB MP: " + String.format("%f.2", tot));
        return tot;
    }

//        # scheiner gi curves fig 7-8 from Think Like a Pancreas, fit with a triangle shaped absorbtion rate curve
//        # see basic math pdf on repo for details
//        # g is time in minutes, ct is carb type
//        def cob(self, g, ct):
//        if g <= 0:
//        tot = 0
//        elif g >= ct:
//        tot = 1
//        elif (g > 0) and (g <= ct / 2):
//        tot = 2.0 / pow(ct, 2) * pow(g, 2)
//        else:
//        tot = -1.0 + 4.0 / ct * (g - pow(g, 2) / (2.0 * ct))
//        return tot

    /**
     * Generates COB Multiplier to get current cob from carb event.
     *
     * @param minutesAfterEvent
     * @param carbAbsorptionTime
     * @return
     */
    public static double getCarbsOnBoardMultiplier(double minutesAfterEvent, double carbAbsorptionTime) {
        double tot = 0.0;
        if (minutesAfterEvent <= 0) {
            tot = 0.0;
        } else if (minutesAfterEvent >= carbAbsorptionTime) {
            tot = 1;
        } else if (minutesAfterEvent > 0 && minutesAfterEvent <= (carbAbsorptionTime / 2)) {
            tot = 2.0 / Math.pow(carbAbsorptionTime, 2) * Math.pow(minutesAfterEvent, 2);
        } else {
            tot = -1.0 + 4.0 / carbAbsorptionTime
                    * (minutesAfterEvent - Math.pow(minutesAfterEvent, 2)
                    / (2.0 * carbAbsorptionTime));
        }
        return tot;
    }

    /**
     * calculate a single delta for the bg curve by given time after carb event
     *
     * @param minutesAfterEvent
     * @param sensitivityFactor
     * @param carbRatio
     * @param carbAmount
     * @param carbAbsorptionTime
     * @return
     */
    public static double deltaBgCarb(double minutesAfterEvent, double sensitivityFactor, double carbRatio,
                                     double carbAmount, double carbAbsorptionTime) {
        return sensitivityFactor / carbRatio * carbAmount *
                getCarbsOnBoardMultiplier(minutesAfterEvent, carbAbsorptionTime);
    }

    /**
     * calculate a single delta for the bg curve by given time after insulin event
     *
     * @param minutesAfterEvent
     * @param bolus
     * @param sensitivityFactor
     * @param insulinDuration
     * @return
     */
    public static double deltaBgInsulin(double minutesAfterEvent, double bolus, double sensitivityFactor,
                                        double insulinDuration) {
        return -bolus * sensitivityFactor *
                (1 - getInsulinOnBoardMultiplier(minutesAfterEvent, insulinDuration) / 100.0);
    }

    /**
     * calculate a single delta for a bg curve by given time after insulin AND carb event
     *
     * @param minutesAfterEvent
     * @param sensitivityFactor
     * @param carbRatio
     * @param carbAmount
     * @param carbAbsorptionTime
     * @param bolus
     * @param insulinDuration
     * @return
     */
    public static double deltaBgCombined(double minutesAfterEvent, double sensitivityFactor, double carbRatio,
                                         double carbAmount, double carbAbsorptionTime, double bolus,
                                         double insulinDuration) {
        return deltaBgInsulin(minutesAfterEvent, bolus, sensitivityFactor, insulinDuration) +
                deltaBgCarb(minutesAfterEvent, sensitivityFactor, carbRatio, carbAmount, carbAbsorptionTime);
    }


    /**
     * This calculates the delta to a base value, not the delta to the previous value
     *
     * @param bolus
     * @param carbAmount
     * @param predicitonSteps
     * @return
     */
    public double[] calculateBgProgressionDeltas(double bolus, double carbAmount, int predicitonSteps) {
        double[] baseProgression = new double[predicitonSteps];
        for (int i = 0; i < predicitonSteps; i++) {
            baseProgression[i] = 0;
        }
        return calculateBgProgression(baseProgression, bolus, carbAmount);
    }

    public double[] calculateBgProgression(double startValue, double bolus, double carbAmount, int predicitonSteps) {
        double[] baseProgression = new double[predicitonSteps];
        for (int i = 0; i < predicitonSteps; i++) {
            baseProgression[i] = startValue;
        }
        return calculateBgProgression(baseProgression, bolus, carbAmount);
    }

    public double[] calculateBgProgression(double[] baseProgression, double bolus, double carbAmount) {
        int predicitonSteps = baseProgression.length;
        double[] prediction = new double[predicitonSteps];

        for (int i = 0; i < predicitonSteps; i++) {
            prediction[i] = baseProgression[i] + deltaBgCombined((i) * 5,
                    sensitivityFactor, carbRatio, carbAmount, carbAbsorptionTime, bolus, insulinDuration);
        }

        return prediction;
    }
}


//        def simulate(self, cgmhistory, carbEvents, bolusEvents, sensfValues, projectinsulin=0,
//        predictionsteps=12, simulationinterval=5, trueprediction=[]):
//
//        self.cgmhistory = cgmhistory
//        self.predictionsteps = predictionsteps
//        self.simulationinterval = simulationinterval
//        self.sensfValues = sensfValues
//        self.trueprediction = np.array(trueprediction)
//
//        bginitial = cgmhistory[0]
//
//        simlength = self.predictionsteps  # minutes/simulationinterval
//        inputlength = len(cgmhistory)  # minutes/simulationinterval
//
//        totallength = inputlength + simlength
//
//        # prepare arrays:
//
//        self.carbEvents = np.zeros(totallength)
//        self.carbEvents[0:len(carbEvents)] = carbEvents
//
//        self.bolusEvents = np.zeros(totallength)
//        self.bolusEvents[0:len(bolusEvents)] = bolusEvents
//
//        # substitute bolus for the simulation period with the min of the last seen:
//        if (projectinsulin > 0):
//        # self.bolusEvents[inputlength:inputlength+predictionsteps] = np.ones(predictionsteps)*np.mean(self.bolusEvents[inputlength-projectinsulin:inputlength])
//        self.bolusEvents[inputlength:inputlength + predictionsteps] = np.ones(
//        predictionsteps) * np.min(
//        self.bolusEvents[inputlength - projectinsulin:inputlength])
//
//        # print(self.carbEvents)
//        # print(self.bolusEvents)
//
//        # init simulation
//        simt = int(totallength * self.simulationinterval)
//        n = totallength  # points in simulation
//        dt = simt / n  # delta t for each step
//
//        self.simbgc = np.zeros(totallength)
//        self.simbgi = np.zeros(totallength)
//        self.simbg = np.ones(totallength) * bginitial
//
//        # Start the simulation:
//        for j in range(0, totallength):
//        for i in range(0, totallength):
//        time = j * self.simulationinterval
//
//        if self.carbEvents[j] > 0:
//        self.simbgc[i] = self.simbgc[i] + self.deltaBGC(i * dt - time,
//        self.sensfValues[j],
//        self.cratio, self.carbEvents[j],
//        self.carbtype)
//
//        if self.bolusEvents[j] > 0:
//        self.simbgi[i] = self.simbgi[i] + self.deltaBGI(i * dt - time,
//        self.bolusEvents[j],
//        self.sensfValues[j], self.idur)
//
//        # add together for absolute effect calculation
//        self.simbg = np.array(self.simbg) + np.array(self.simbgc) + np.array(self.simbgi)
//        return self.simbg[0:inputlength], self.simbg[inputlength:]
//
//        def setTruePrediction(self, trueprediction):
//        self.trueprediction = np.array(trueprediction)
