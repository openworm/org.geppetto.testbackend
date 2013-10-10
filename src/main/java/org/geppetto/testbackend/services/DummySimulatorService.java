package org.geppetto.testbackend.services;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.common.Variable;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.state.SimpleStateNode;
import org.geppetto.core.model.state.StateTreeRoot;
import org.geppetto.core.model.values.DoubleValue;
import org.geppetto.core.simulation.IRunConfiguration;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.ASimulator;
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
		try {
			((SimpleStateNode)tree.getChildren().get(0)).addValue(new DoubleValue(0d));
			getListener().stateTreeUpdated(tree);
		} catch (GeppettoExecutionException e) {
			e.printStackTrace();
		}
	}

	public void initialize(IModel model, ISimulatorCallbackListener listener) throws GeppettoInitializationException, GeppettoExecutionException
	{
		super.initialize(model, listener);
		tree.addChild(new SimpleStateNode("dummy"));
		
		try {
			((SimpleStateNode)tree.getChildren().get(0)).addValue(new DoubleValue(0d));
			getListener().stateTreeUpdated(tree);
		} catch (GeppettoExecutionException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public List<Variable> getForceableVariables() {
		// TODO: return some dummy forceable variables 
		return null;
	}

	@Override
	public List<Variable> getWatchableVariables() {
		// TODO: return some dummy watchable variables
		return null;
	}
}
