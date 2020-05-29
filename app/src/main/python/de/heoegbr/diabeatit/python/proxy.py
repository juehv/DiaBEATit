# -*- coding: utf-8 -*-
import numpy as np
import pandas as pd
from android.util import Log;
from com.chaquo.python import Python
from datetime import datetime
from de.heoegbr.diabeatit.assistant.prediction.python import PythonInputContainer
from de.heoegbr.diabeatit.assistant.prediction.python import PythonOutputContainer
from de.heoegbr.diabeatit.assistant.prediction.python import PythonPredictionBase
from de.heoegbr.diabeatit.data.container.event import DiaryEvent
from java import Override, static_proxy

from .predict import Jorisizer


class PythonPredictionProxy(static_proxy(PythonPredictionBase)):

    @Override(PythonOutputContainer, [PythonInputContainer])
    def predict(self, inputContainer):
        # //TODO add safety mechanism (in Java) to not call when cgm is old or has gaps
        # Read diary events into a dataframe
        rawDf = pd.DataFrame(columns=['ts', 'cgm', 'bolus', 'basal', 'meal'])

        # // TODO try a real implementation for time zone offset instead of faking ...
        zoneOffset = 7200;
        for x in inputContainer.events:
            if (x.type == DiaryEvent.TYPE_BG):
                rawDf = rawDf.append(
                    {'ts': datetime.utcfromtimestamp(x.timestamp.getEpochSecond() + zoneOffset),
                     'cgm': x.value},
                    ignore_index=True)
            elif (x.type == DiaryEvent.TYPE_BOLUS):
                rawDf = rawDf.append(
                    {'ts': datetime.utcfromtimestamp(x.timestamp.getEpochSecond() + zoneOffset),
                     'bolus': x.value},
                    ignore_index=True)
            elif (x.type == DiaryEvent.TYPE_BASAL):
                rawDf = rawDf.append(
                    {'ts': datetime.utcfromtimestamp(x.timestamp.getEpochSecond() + zoneOffset),
                     'basal': x.value},
                    ignore_index=True)
            elif (x.type == DiaryEvent.TYPE_MEAL):
                rawDf = rawDf.append(
                    {'ts': datetime.utcfromtimestamp(x.timestamp.getEpochSecond() + zoneOffset),
                     'meal': x.value},
                    ignore_index=True)

        rawDf = rawDf.set_index('ts')
        rawDf = rawDf.sort_values(by=['ts'])

        # resample data to 5 min slots
        resampledDf = rawDf.resample('5min').agg({'cgm': np.mean, 'bolus': np.sum, 'basal': np.sum,
                                                  'meal': np.sum})  # no fill in for NAN values, just resampling to 5min

        # ... and interpolate missing cgm values
        resampledDf['cgm'] = resampledDf['cgm'].interpolate(method='akima',
                                                            limit=6)  # , limit_direction='forward') # good: akima

        Log.e("PYTHON", str(resampledDf))

        # export dataframe to file (for usage in colabs)
        files_dir = str(
            Python.getPlatform().getApplication().getExternalFilesDir("")) + '/dataframe.csv'
        resampledDf.to_csv(files_dir, encoding='utf-8')

        optimizer = Jorisizer(
            resampledDf['cgm'],
            bolusValues=resampledDf['bolus'],
            basalValues=resampledDf['basal'],
            carbValues=resampledDf['meal']
        )

        optimizer.setparams(carbtype=105,
                            sensf=30,
                            idur=180,
                            cratio=1.7,
                            optimizer='L-BFGS-B',
                            # optimizer='SLSQP',
                            maxiter=10,
                            shrinkfactor=2)

        simulation, prediction = optimizer.optimizeAndPredict(predictionsteps=18)

        simulation = np.rint(np.nan_to_num(simulation))
        prediction = np.rint(np.nan_to_num(prediction))

        outputEvents = []
        outputEvents.append(PythonOutputContainer.constructPredictionEvent(
            inputContainer.timestamp,
            prediction.tolist(),
            simulation.tolist()))

        return PythonOutputContainer(outputEvents)
