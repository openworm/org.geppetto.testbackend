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
import org.springframework.stereotype.Service;

/**
 * Dummy implementation of ISimulator. 
 * 
 * @author jrmartin
 *
 */
@Service
public class DummySimulatorService extends ASimulator{

	private static Log logger = LogFactory.getLog(DummySimulatorService.class);
		
	StateTreeRoot tree = new StateTreeRoot("dummyServices");
	
	@Override
	public void simulate(IRunConfiguration runConfiguration)
			throws GeppettoExecutionException {
			logger.info("Simulate in Dummy Simulator Service");
	}
	
	public void initialize(IModel model, ISimulatorCallbackListener listener) throws GeppettoInitializationException
	{
		logger.info("Initialize dummy simulator services and model");
		super.initialize(model, listener);
	}

}
