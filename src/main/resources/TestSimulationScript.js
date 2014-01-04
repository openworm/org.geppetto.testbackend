Simulation.addWatchLists([{name:"test_dummy", variablePaths:["dummyServices.dummyDouble", "dummyServices.dummyFloat"]}]);

Simulation.start();

Simulation.startWatch();

G.addWidget(Widgets.PLOT);

Plot1.plotData(dummyDouble);

Plot1.setPosition(400,300);

Plot1.setSize(300, 400);