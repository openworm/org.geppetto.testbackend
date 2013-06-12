package org.geppetto.testbackend.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.state.StateTreeRoot;
import org.geppetto.core.simulation.IRunConfiguration;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.ASimulator;
import org.geppetto.core.solver.ISolver;

public class DummySimulatorService extends ASimulator{

	private static Log logger = LogFactory.getLog(DummySimulatorService.class);
		
	private ISolver sphSolver;

	@Override
	public void simulate(IRunConfiguration runConfiguration)
			throws GeppettoExecutionException {
		logger.info("Dummy Simulate method invoke");
		StateTreeRoot results=sphSolver.solve(runConfiguration);
		getListener().stateTreeUpdated(results);	
	}
	
	public void initialize(IModel model, ISimulatorCallbackListener listener) throws GeppettoInitializationException
	{
		super.initialize(model, listener);
		sphSolver.initialize(model);
	}

}
