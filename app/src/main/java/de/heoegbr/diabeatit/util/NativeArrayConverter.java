package de.heoegbr.diabeatit.util;

import java.util.ArrayList;
import java.util.List;

public class NativeArrayConverter {
    public static List<Double> toArrayList(double[] array) {
        List<Double> returnValue = new ArrayList<>(array.length);
        for (double item : array) {
            returnValue.add(item);
        }
        return returnValue;
    }
}
