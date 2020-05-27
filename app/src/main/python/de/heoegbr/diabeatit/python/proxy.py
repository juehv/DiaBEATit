# -*- coding: utf-8 -*-
from de.heoegbr.diabeatit.assistant.prediction import PredictionBase
from de.heoegbr.diabeatit.data.container.event import DiaryEvent
from java import jarray, Override, static_proxy


class PythonPredictionProxy(static_proxy(PredictionBase)):

    @Override(jarray(DiaryEvent), [jarray(DiaryEvent)])
    def predict(self, *historicEvents):
        returnList = []
        for event in historicEvents:
            if (event.type == DiaryEvent.TYPE_BG):
                returnList.append(event)

        return returnList
