#
#
#   File to test current configuration of Mainen Pyr Cell project. 
#
#   To execute this type of file, type '..\nC.bat -python XXX.py' (Windows)
#   or '../nC.sh -python XXX.py' (Linux/Mac). Note: you may have to update the
#   NC_HOME and NC_MAX_MEMORY variables in nC.bat/nC.sh
#
#   Author: Padraig Gleeson
#
#   This file has been developed as part of the neuroConstruct project
#   This work has been funded by the Medical Research Council and the
#   Wellcome Trust
#
#

import sys
import os

from java.io import File

sys.path.append(os.environ["NC_HOME"]+"/pythonNeuroML/nCUtils")

import ncutils as nc

projFile = File(os.getcwd(), "../MainenEtAl_PyramidalCell.ncx")



##############  Main settings  ##################

simConfigs = []

simConfigs.append("SimpleCells-AllSims")

simDt =                 0.002
simDtOverride =         {"LEMS":0.0005}

#simulators =            ["NEURON"]
simulators =            ["NEURON", "GENESIS_PHYS", "GENESIS_SI", "MOOSE_PHYS", "MOOSE_SI", "PSICS", "LEMS"]

numConcurrentSims =     4

varTimestepNeuron =     False

analyseSims =           True
plotSims =              True
plotVoltageOnly =       True
runInBackground =       True

verbose = True

#############################################


def testAll(argv=None):
    if argv is None:
        argv = sys.argv

    print "Loading project from "+ projFile.getCanonicalPath()


    simManager = nc.SimulationManager(projFile,
                                      numConcurrentSims)

    simManager.runMultipleSims(simConfigs=simConfigs,
                               simDt=simDt,
                               simDtOverride=simDtOverride,
                               simulators=simulators,
                               runInBackground=runInBackground)


    simManager.reloadSims(plotVoltageOnly=plotVoltageOnly,
                          plotSims =          plotSims)

    # These were discovered using analyseSims = True above.
    # They need to hold for all simulators
    spikeTimesToCheck = {'One_ChannelML_0': [33.64]}

    spikeTimeAccuracy = 0.01

    report = simManager.checkSims(spikeTimesToCheck = spikeTimesToCheck,
                                  spikeTimeAccuracy = spikeTimeAccuracy,
                                  threshold=-30)

    print report

    return report

if __name__ == "__main__":
    testAll()




