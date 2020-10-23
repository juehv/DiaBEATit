package de.heoegbr.diabeatit.data.container;

public class Profil {

    /**
     * Amount of minutes the insulin is acting
     */
    public static double DURATION_OF_INSULIN_ACTIVITY = 300.0;

    /**
     * Amount of minutes the insulin needs to reach its peek activity
     */
    public static double INSULIN_PEEK_ACTIVITY = 50.0;

    /**
     * Amount the bg is falling after injecting one insulin unit
     */
    public static double INSULIN_SENSITIVITY_FACTOR = 40.0;

    /**
     * Amount of carb needed to cover one insulin unit
     */
    public static double INSULIN_CARB_RATIO = 4.0;

    public static double BG_TARGET = 100;
}
