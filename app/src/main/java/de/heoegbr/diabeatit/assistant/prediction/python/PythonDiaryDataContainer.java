package de.heoegbr.diabeatit.assistant.prediction.python;

import de.heoegbr.diabeatit.data.container.event.DiaryEvent;

public class PythonDiaryDataContainer {
    public final DiaryEvent[] events;

    public PythonDiaryDataContainer(DiaryEvent[] events) {
        this.events = events;
    }
}
