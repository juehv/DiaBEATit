# -*- coding: utf-8 -*-
import numpy as np
from scipy.optimize import Bounds
from scipy.optimize import minimize

"""# Prediction Model 
## Base model from Glucodyn Code
https://github.com/Perceptus/GlucoDyn/blob/master/js/glucodyn/algorithms.js
## More explaination:
https://openaps.readthedocs.io/en/latest/docs/While%20You%20Wait%20For%20Gear/understanding-insulin-on-board-calculations.html
"""


# Predicts BG value based on historic bg values, bolus and carb events
class BgPredict:

    ## carbtype = 60|90|120
    ## sensf =  1|2|3|4
    ## idur = 180|240|300|360
    ## cratio = real
    def __init__(self, carbtype=120, sensf=1, idur=180, cratio=1):
        self.carbtype = carbtype
        self.sensf = sensf
        self.idur = idur
        self.cratio = cratio

        self.cgmhistory = np.zeros(30)
        self.carbEvents = np.zeros(30)
        self.bolusEvents = np.zeros(30)
        self.sensfValues = np.ones(30) * sensf
        self.predictionsteps = 12
        self.simulationinterval = 5

    # Insulin on Board Advanced
    # g = time in minutes from bolus event
    # idur = insulin duration

    # Insulin on Board
    # g = time in minutes from bolus event
    # idur = insulin duration
    def iob(self, g, idur):
        tot = 100
        if g <= 0.0:
            tot = 100
        elif g >= idur:
            tot = 0.0
        else:
            if idur <= 180:
                tot = -3.203e-7 * pow(g, 4) + 1.354e-4 * pow(g, 3) - 1.759e-2 * pow(g,
                                                                                    2) + 9.255e-2 * g + 99.951
            elif idur <= 240:
                tot = -3.31e-8 * pow(g, 4) + 2.53e-5 * pow(g, 3) - 5.51e-3 * pow(g,
                                                                                 2) - 9.086e-2 * g + 99.95
            elif idur <= 300:
                tot = -2.95e-8 * pow(g, 4) + 2.32e-5 * pow(g, 3) - 5.55e-3 * pow(g,
                                                                                 2) + 4.49e-2 * g + 99.3
            elif idur <= 360:
                tot = -1.493e-8 * pow(g, 4) + 1.413e-5 * pow(g, 3) - 4.095e-3 * pow(g,
                                                                                    2) + 6.365e-2 * g + 99.7
        return tot

    # scheiner gi curves fig 7-8 from Think Like a Pancreas, fit with a triangle shaped absorbtion rate curve
    # see basic math pdf on repo for details
    # g is time in minutes, ct is carb type
    def cob(self, g, ct):
        if g <= 0:
            tot = 0
        elif g >= ct:
            tot = 1
        elif (g > 0) and (g <= ct / 2):
            tot = 2.0 / pow(ct, 2) * pow(g, 2)
        else:
            tot = -1.0 + 4.0 / ct * (g - pow(g, 2) / (2.0 * ct))
        return tot

    def deltaBGC(self, g, sensf, cratio, camount, ct):
        return sensf / cratio * camount * self.cob(g, ct)

    def deltaBGI(self, g, bolus, sensf, idur):
        return -bolus * sensf * (1 - self.iob(g, idur) / 100.0)

    def deltaBG(self, g, sensf, cratio, camount, ct, bolus, idur):
        return self.deltaBGI(g, bolus, sensf, idur) + self.deltaBGC(g, sensf, cratio, camount, ct)

    def simulate(self, cgmhistory, carbEvents, bolusEvents, sensfValues, projectinsulin=0,
                 predictionsteps=12, simulationinterval=5, trueprediction=[]):

        self.cgmhistory = cgmhistory
        self.predictionsteps = predictionsteps
        self.simulationinterval = simulationinterval
        self.sensfValues = sensfValues
        self.trueprediction = np.array(trueprediction)

        bginitial = cgmhistory[0]

        simlength = self.predictionsteps  # minutes/simulationinterval
        inputlength = len(cgmhistory)  # minutes/simulationinterval

        totallength = inputlength + simlength

        # prepare arrays:

        self.carbEvents = np.zeros(totallength)
        self.carbEvents[0:len(carbEvents)] = carbEvents

        self.bolusEvents = np.zeros(totallength)
        self.bolusEvents[0:len(bolusEvents)] = bolusEvents

        # substitute bolus for the simulation period with the min of the last seen:
        if (projectinsulin > 0):
            # self.bolusEvents[inputlength:inputlength+predictionsteps] = np.ones(predictionsteps)*np.mean(self.bolusEvents[inputlength-projectinsulin:inputlength])
            self.bolusEvents[inputlength:inputlength + predictionsteps] = np.ones(
                predictionsteps) * np.min(
                self.bolusEvents[inputlength - projectinsulin:inputlength])

        # print(self.carbEvents)
        # print(self.bolusEvents)

        # init simulation
        simt = int(totallength * self.simulationinterval)
        n = totallength  # points in simulation
        dt = simt / n  # delta t for each step

        self.simbgc = np.zeros(totallength)
        self.simbgi = np.zeros(totallength)
        self.simbg = np.ones(totallength) * bginitial

        # Start the simulation:
        for j in range(0, totallength):
            for i in range(0, totallength):
                time = j * self.simulationinterval

                if self.carbEvents[j] > 0:
                    self.simbgc[i] = self.simbgc[i] + self.deltaBGC(i * dt - time,
                                                                    self.sensfValues[j],
                                                                    self.cratio, self.carbEvents[j],
                                                                    self.carbtype)

                if self.bolusEvents[j] > 0:
                    self.simbgi[i] = self.simbgi[i] + self.deltaBGI(i * dt - time,
                                                                    self.bolusEvents[j],
                                                                    self.sensfValues[j], self.idur)

                    # add together for absolute effect calculation
        self.simbg = np.array(self.simbg) + np.array(self.simbgc) + np.array(self.simbgi)
        return self.simbg[0:inputlength], self.simbg[inputlength:]

    def setTruePrediction(self, trueprediction):
        self.trueprediction = np.array(trueprediction)

    # def plotResults(self):
    # plt.figure(figsize=[10,5])

    # plt.plot(self.carbEvents, label="carbEvents")
    # plt.plot(self.bolusEvents, label="bolusEvents")
    # plt.plot(self.simbg, label="Bloodglucose Simulation")

    # if self.predictionsteps>0:
    # plt.plot([len(self.cgmhistory),len(self.cgmhistory)], [0,max(max(self.cgmhistory), max(self.simbg))], color='grey', linestyle='dashed')

    # if(len(self.trueprediction)>0):
    # cgmvalues = np.concatenate((self.cgmhistory,self.trueprediction), axis=0)
    # plt.plot(cgmvalues, label="Bloodglucose Real")
    # else: 
    # plt.plot(self.cgmhistory, label="Bloodglucose Real")

    ## plt.plot(self.simbgc, label="Carb effects")
    ## plt.plot(self.simbgi, label="Insulin effects")

    # plt.legend()
    # plt.show()


"""# The Jorisizer"""


# The jorisizer optimizes the carb and insulin values to resemble the observed CGM history
class Jorisizer:
    def __init__(self, cgmhistory, bolusValues=[], carbValues=[], basalValues=[]):
        self.cgmhistory = cgmhistory
        self.bolusValues = bolusValues
        self.basalValues = basalValues
        self.carbValues = carbValues

        self.totallength = len(cgmhistory)

        self.carbtype = 120
        self.sensf = 1
        self.idur = 180
        self.cratio = 1

        self.maxiter = 20
        self.optimizer = 'L-BFGS-B'

        self.sim = None
        self.values = None

        self.shrinkfactor = 1

        # Calculate weight curves for weighted errors
        self.linweights = np.array(list(map(lambda x: (x + 1) / self.totallength / 2 + 0.2,
                                            range(self.totallength))))  # Linear weights
        self.linweights /= self.linweights.sum()

        self.expweights = np.exp(np.linspace(-3., 0., self.totallength))  # exponential weights
        self.expweights /= self.expweights.sum()

        self.invexpweights = np.exp(np.linspace(0., -3., self.totallength))  # exponential weights
        self.invexpweights /= self.invexpweights.sum()

    # Deconstruct the X array into its components
    def extractValuesandBounds(self, X):

        carbValues = X[0:self.totallength]
        sensfValues = X[self.totallength:]
        return carbValues, self.bolusValues, self.basalValues, sensfValues

    # Construct the onedimensional X array from carbvalues, bolus and basal values and the insulin sensitivity.
    def createXandBounds(self):

        positionslack = 6

        X = np.zeros(2 * self.totallength)

        lb = np.zeros(len(X))
        ub = np.zeros(len(X))

        # Carb Values
        X[0:self.totallength] = self.carbValues
        for i in range(0, self.totallength):
            lb[i] = 0
            ub[i] = 100  # max(X[max(i - positionslack, 0):min(i + positionslack,
            #                               self.totallength)]) * 1.2  # allow adjacent 3 values to be adjusted, with 20% estimation slack

        # Sensitivity values
        X[self.totallength:] = np.ones(self.totallength) * self.sensf
        for i in range(self.totallength, 2 * self.totallength):
            lb[i] = 0.3  # 0.7 * self.sensf
            ub[i] = 3  # 1.5 * self.sensf  # allow +-30 percent sensf change

        bounds = Bounds(lb, ub)
        return X, bounds

    def setparams(self, carbtype=120, sensf=1, idur=180, cratio=1, maxiter=20,
                  optimizer='L-BFGS-B', shrinkfactor=1):
        self.carbtype = carbtype
        self.sensf = sensf
        self.idur = idur
        self.cratio = cratio
        self.maxiter = maxiter
        self.optimizer = optimizer
        self.shrinkfactor = shrinkfactor

    def getValues(self):
        return self.values

    def optimize(self):

        startcgm = self.cgmhistory[0]

        # we will optimize the carbs and the basal rate, the bolus stays as-is.
        # x0 = np.zeros(2*self.totallength)  # both will be merged in this vector
        # x0 = self.shrinkParameters(x0)
        # bounds = np.array([(lb, ub)] * len(x0))

        x0, bounds = self.createXandBounds()

        self.values = minimize(self.errorCalculation, x0,
                               args=(startcgm, self.cgmhistory, self.bolusValues),
                               method=self.optimizer,
                               bounds=bounds,
                               options={'disp': False,
                                        'maxiter': self.maxiter,
                                        # 'maxfun': 1000000,
                                        # 'maxls': 200
                                        }
                               )

        return self.expandParameters(self.values.x)

    def shrinkParameters(self, parameters):
        newlength = round(len(parameters) / self.shrinkfactor)

        params = np.zeros(newlength)

        stepwidth = round(len(parameters) / newlength)
        j = 0
        for i in range(0, len(parameters), stepwidth):
            params[j] = parameters[i]
            j = j + 1
        return params

    def expandParameters(self, params):
        newlength = len(params) * self.shrinkfactor
        parameters = np.zeros(newlength)

        stepwidth = round(newlength / len(params))
        j = 0
        for i in range(0, len(params)):
            parameters[j] = params[i]
            j = j + stepwidth
        return parameters

    def errorCalculation(self, inputs, startcgm, cgmhistory, bolusEvents):
        # print(inputs)

        carbValues, bolusEvents, basalEvents, sensfValues = self.extractValuesandBounds(inputs)

        predictor = BgPredict(carbtype=self.carbtype, sensf=self.sensf, idur=self.idur,
                              cratio=self.cratio)

        insulinEvents = basalEvents + bolusEvents

        cgmvalues = np.ones(len(cgmhistory)) * startcgm  # set to first value in time series
        simulation, prediction = predictor.simulate(cgmvalues, carbValues, insulinEvents,
                                                    sensfValues,
                                                    predictionsteps=0,
                                                    simulationinterval=5)

        # Absolute Error
        error = np.absolute(cgmhistory - simulation.flatten())  # normal error
        # error = error/np.sum(error)

        squared_error = np.power(error, 2)
        weighted_error = np.matmul(squared_error, self.expweights)
        lin_weighted_error = np.matmul(squared_error, self.linweights)

        # Error of derivative:
        d_error = np.absolute(np.diff(cgmhistory) - np.diff(simulation.flatten()))  # diff error
        # d_error = d_error/np.sum(d_error)

        d_squared_error = np.power(d_error, 2)
        d_weighted_error = np.matmul(d_squared_error, self.expweights[1:])
        d_lin_weighted_error = np.matmul(d_squared_error, self.linweights[1:])

        # return np.sqrt(np.average(squared_error))

        # return lin_weighted_error+d_weighted_error+np.sum(d_squared_error[-5:])*10   # promising combination!
        # return weighted_error+np.sum(d_squared_error[-10:])*7
        return weighted_error + d_weighted_error * 7

    def optimizeAndPredict(self, predictionsteps=12, simulationinterval=5, projectinsulin=0):
        self.params = self.optimize()

        carbEvents, bolusValues, basalEvents, sensfValues = self.extractValuesandBounds(self.params)

        insulinEvents = basalEvents + self.bolusValues

        # print the optimized curve:
        self.sim = BgPredict(carbtype=self.carbtype, sensf=self.sensf, idur=self.idur,
                             cratio=self.cratio)
        simulation, prediction = self.sim.simulate(self.cgmhistory, carbEvents, insulinEvents,
                                                   sensfValues,
                                                   projectinsulin=projectinsulin,
                                                   predictionsteps=predictionsteps,
                                                   simulationinterval=simulationinterval
                                                   )

        return np.rint(simulation), np.rint(prediction)

    # def plotresults(self, trueprediction=[]):
    # if(len(trueprediction)>0):
    # self.sim.setTruePrediction(trueprediction)

    # self.sim.plotResults()
    # plt.figure(figsize=[10,3])
    # carbEvents, bolusValues, basalEvents, sensfValues = self.extractValuesandBounds(self.params)
    # plt.plot(sensfValues, label='ISF')
    # plt.legend()
    # plt.show()
