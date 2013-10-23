package org.geppetto.testbackend.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.state.SimpleStateNode;
import org.geppetto.core.model.state.StateTreeRoot;
import org.geppetto.core.model.values.DoubleValue;
import org.geppetto.core.simulation.IRunConfiguration;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.ASimulator;
import org.geppetto.core.pojo.model.AVariable;
import org.geppetto.core.pojo.model.SimpleType;
import org.geppetto.core.pojo.model.SimpleType.Type;
import org.geppetto.core.pojo.model.SimpleVariable;
import org.geppetto.core.pojo.model.VariableList;
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
	
	private VariableList forceableVariables = new VariableList();
	private VariableList watchableVariables = new VariableList();

	StateTreeRoot tree = new StateTreeRoot("dummyServices");

	@Override
	public void simulate(IRunConfiguration runConfiguration) throws GeppettoExecutionException {		
		try {
			((SimpleStateNode)tree.getChildren().get(0)).addValue(new DoubleValue(0d));
			getListener().stateTreeUpdated(tree);
		} catch (GeppettoExecutionException e) {
			e.printStackTrace();
		}
	}

	public void initialize(IModel model, ISimulatorCallbackListener listener) throws GeppettoInitializationException, GeppettoExecutionException
	{
		// populate watch / force variables
		setWatchableVariables();
		setForceableVariables();
		
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
	public VariableList getForceableVariables() {
		// return some dummy forceable variables 
		return forceableVariables;
	}

	@Override
	public VariableList getWatchableVariables() {
		// return some dummy watchable variables
		return watchableVariables;
	}
	
	/**
	 * Populates some dummy state variables to test list variables functionality
	 * */
	private void setWatchableVariables() {
		// NOTE: this could be more elegantly injected via spring
		List<AVariable> vars = new ArrayList<AVariable>();
		
		// dummyInt
		SimpleVariable dummyInt = new SimpleVariable();
		SimpleType integerType = new SimpleType();
		integerType.setType(Type.INTEGER);
		dummyInt.setName("dummyInt");
		dummyInt.setType(integerType);
		// dummyFloat
		SimpleVariable dummyFloat = new SimpleVariable();
		SimpleType floatType = new SimpleType();
		floatType.setType(Type.FLOAT);
		dummyFloat.setName("dummyInt");
		dummyFloat.setType(floatType);
		
		vars.add(dummyInt);
		vars.add(dummyFloat);
		
		this.watchableVariables.setEntities(vars);
	}
	
	/**
	 * Populates some dummy state variables to test list variables functionality
	 * */
	private void setForceableVariables() {
		// NOTE: this could be more elegantly injected via spring
		List<AVariable> vars = new ArrayList<AVariable>();
		
		// dummyInt
		SimpleVariable dummyInt = new SimpleVariable();
		SimpleType integerType = new SimpleType();
		integerType.setType(Type.INTEGER);
		dummyInt.setName("dummyInt");
		dummyInt.setType(integerType);
		
		vars.add(dummyInt);
		
		this.forceableVariables.setEntities(vars);
	}
}
