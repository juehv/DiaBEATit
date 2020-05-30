package de.heoegbr.diabeatit.assistant.prediction.python;

import java.time.Instant;
import java.util.Arrays;

import de.heoegbr.diabeatit.data.container.event.DiaryEvent;
import de.heoegbr.diabeatit.data.container.event.PredictionEvent;

public class PythonOutputContainer {
    public final PredictionEvent[] events;

    public PythonOutputContainer(PredictionEvent[] events) {
        this.events = events;
    }

    public static PredictionEvent constructPredictionEvent(long timeStamp,
                                                           Double[] prediction,
                                                           Double[] cgmSimulation,
                                                           Double[] carbSimulation,
                                                           Double[] isfSimulation) {
        //TODO mange timestamps
        return new PredictionEvent(DiaryEvent.SOURCE_DEVICE, Instant.ofEpochSecond(timeStamp), "",
                Arrays.asList(prediction),
                Arrays.asList(cgmSimulation),
                Arrays.asList(carbSimulation),
                Arrays.asList(isfSimulation));
    }
}
