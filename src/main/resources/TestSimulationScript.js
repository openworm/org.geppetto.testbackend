Simulation.addWatchLists([{name:"test_dummy", variablePaths:["dummyServices.dummyDouble", "dummyServices.dummyFloat"]}]);

Simulation.start();

Simulation.startWatch();

G.addWidget(Widgets.PLOT);

var options = {yaxis:{min:0,max:1},xaxis:{min:0,max:100,show:false}};

Plot1.setOptions(options)

Plot1.plotState("dummyDouble");

Plot1.setPosition(400,300);

Plot1.setSize(300, 400);