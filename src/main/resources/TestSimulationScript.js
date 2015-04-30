dummyServices.dummyServices.getSimulationTree();

Simulation.setWatchedVariables([dummyServices.dummyServices.dummyDouble, dummyServices.dummyServices.dummyFloat]);

Simulation.start();

G.addWidget(Widgets.PLOT);

var options = {yaxis:{min:0,max:1},xaxis:{min:0,max:40}};

Plot1.setOptions(options)

Plot1.plotData(dummyDouble);

Plot1.setPosition(32,239);

Plot1.setSize(300, 400);

G.addWidget(Widgets.PLOT);

Plot2.setOptions(options)

Plot2.plotData(dummyFloat);

Plot2.setPosition(835,176);

G.wait(1000);

Plot1.plotData(dummyFloat);