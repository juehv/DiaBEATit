package de.heoegbr.diabeatit.assistant.prediction.python;

public abstract class PythonPredictionBase {
    public abstract PythonOutputContainer predict(PythonInputContainer historicEvents);
}
