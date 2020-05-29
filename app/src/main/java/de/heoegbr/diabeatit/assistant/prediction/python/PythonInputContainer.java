package de.heoegbr.diabeatit.assistant.prediction.python;

import de.heoegbr.diabeatit.data.container.event.DiaryEvent;

public class PythonInputContainer {
    public final long timestamp;
    public final DiaryEvent[] events;

    public PythonInputContainer(long timestamp, DiaryEvent[] events) {
        this.events = events;
        this.timestamp = timestamp;
    }
}
