package de.heoegbr.diabeatit.assistant.prediction.python;

public abstract class PythonPredictionBase {
    public abstract float[] predict(PythonDiaryDataContainer historicEvents);
}
