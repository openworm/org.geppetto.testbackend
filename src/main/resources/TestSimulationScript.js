Simulation.addWatchLists([{name:"test_dummy", variablePaths:["dummyServices.dummyDouble", "dummyServices.dummyFloat"]}]);

Simulation.start();

Simulation.startWatch();

G.addWidget(Widgets.PLOT);

var options = {yaxis:{min:0,max:1},xaxis:{min:0,max:40,show:false}};

Plot1.setOptions(options)

Plot1.plotState("dummyDouble");

Plot1.setPosition(400,300);

Plot1.setSize(300, 400);

G.addWidget(Widgets.PLOT);

Plot2.setOptions(options)

Plot2.plotState("dummyFloat");

G.wait(1000);

Plot1.plotState("dummyFloat");