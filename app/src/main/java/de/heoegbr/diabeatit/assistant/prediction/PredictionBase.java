package de.heoegbr.diabeatit.assistant.prediction;

import de.heoegbr.diabeatit.data.container.event.DiaryEvent;

public abstract class PredictionBase {
    public abstract DiaryEvent[] predict(DiaryEvent[] historicEvents);
}
