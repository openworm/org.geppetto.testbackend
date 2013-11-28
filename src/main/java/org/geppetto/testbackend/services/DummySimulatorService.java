package org.geppetto.testbackend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.data.model.AVariable;
import org.geppetto.core.data.model.SimpleType;
import org.geppetto.core.data.model.SimpleType.Type;
import org.geppetto.core.data.model.SimpleVariable;
import org.geppetto.core.data.model.VariableList;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.state.AStateNode;
import org.geppetto.core.model.state.CompositeStateNode;
import org.geppetto.core.model.state.SimpleStateNode;
import org.geppetto.core.model.state.StateTreeRoot;
import org.geppetto.core.model.values.AValue;
import org.geppetto.core.model.values.DoubleValue;
import org.geppetto.core.model.values.FloatValue;
import org.geppetto.core.model.values.ValuesFactory;
import org.geppetto.core.simulation.IRunConfiguration;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.ASimulator;
import org.springframework.stereotype.Service;

/**
 * Dummy implementation of ISimulator. 
 * @author jrmartin
 *
 */
@Service
public class DummySimulatorService extends ASimulator{

	private static Log logger = LogFactory.getLog(DummySimulatorService.class);
	
	private Random randomGenerator;
	
	// TODO: all this stuff should come from configuration
	private final String aspectID = "dummy";
	private final String WATCH_TREE_ID = "variable-watch";
	private final String MODEL_TREE_ID = "model-interpreter";
	
	private VariableList forceableVariables = new VariableList();
	private VariableList watchableVariables = new VariableList();
	
	private List<String> watchList = new ArrayList<String>();
	private boolean watch = false;

	StateTreeRoot tree = new StateTreeRoot("dummySimulator");

	public void initialize(IModel model, ISimulatorCallbackListener listener) throws GeppettoInitializationException, GeppettoExecutionException
	{		
		// populate watch / force variables
		setWatchableVariables();
		setForceableVariables();
		
		super.initialize(model, listener);
		
		// init statetree
		tree.addChild(new SimpleStateNode(MODEL_TREE_ID));
		((SimpleStateNode)tree.getChildren().get(0)).addValue(ValuesFactory.getDoubleValue(getRandomGenerator().nextDouble()));
		
		getListener().stateTreeUpdated(tree);
	}
	
	@Override
	public void simulate(IRunConfiguration runConfiguration) throws GeppettoExecutionException {		

		// throw some junk into model-interpreter node as if results were being populated
		((SimpleStateNode)tree.getChildren().get(0)).addValue(ValuesFactory.getDoubleValue(getRandomGenerator().nextDouble()));
		
		if(watch)
		{
			// add values of variables being watched to state tree
			updateStateTreeForWatch();
		}
	
		getListener().stateTreeUpdated(tree);
	}

	private void updateStateTreeForWatch() {
		CompositeStateNode variableWatchNode = null;
		
		for(AStateNode node : tree.getChildren())
		{
			if(node.getName().equals(WATCH_TREE_ID))
			{
				// assign if it already exists
				variableWatchNode = (CompositeStateNode) node;
				break;
			}
		}
		
		// add to tree if it doesn't exist
		if(variableWatchNode == null)
		{
			variableWatchNode = new CompositeStateNode(WATCH_TREE_ID);
			tree.addChild(variableWatchNode);
		}
		
		// check which watchable variables are being watched
		for(AVariable var : watchableVariables.getVariables())
		{
			for(String varName : watchList)
			{
				// if they are being watched add to state tree
				if(varName.toLowerCase().equals(var.getName().toLowerCase()))
				{
					SimpleStateNode dummyNode = null;
					
					for(AStateNode child : variableWatchNode.getChildren()){
						if(child.getName().equals(var.getName()))
						{
							// assign if it already exists
							dummyNode = (SimpleStateNode) child;
						}
					}
					
					// only add if it's not already there
					if(dummyNode == null){
						dummyNode = new SimpleStateNode(var.getName());
						variableWatchNode.addChild(dummyNode);
					}
					
					AValue val = null;
					
					// NOTE: this is a dummy simulator so we're making values up - we wouldn't need to do this in a real one
					if(varName.toLowerCase().contains("double"))
					{
						val = ValuesFactory.getDoubleValue(getRandomGenerator().nextDouble());
					}
					else if (varName.toLowerCase().contains("float"))
					{
						val = ValuesFactory.getFloatValue(getRandomGenerator().nextFloat());
					}
					
					dummyNode.addValue(val);
				}
			}
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
		SimpleVariable dummyDouble = new SimpleVariable();
		SimpleType doubleType = new SimpleType();
		doubleType.setType(Type.DOUBLE);
		dummyDouble.setAspect(aspectID);
		dummyDouble.setName("dummyDouble");
		dummyDouble.setType(doubleType);
		// dummyFloat
		SimpleVariable dummyFloat = new SimpleVariable();
		SimpleType floatType = new SimpleType();
		floatType.setType(Type.FLOAT);
		dummyFloat.setAspect(aspectID);
		dummyFloat.setName("dummyFloat");
		dummyFloat.setType(floatType);
		
		vars.add(dummyDouble);
		vars.add(dummyFloat);
		
		this.watchableVariables.setVariables(vars);
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
		dummyInt.setAspect(aspectID);
		dummyInt.setName("dummyInt");
		dummyInt.setType(integerType);
		
		vars.add(dummyInt);
		
		this.forceableVariables.setVariables(vars);
	}

	@Override
	public void addWatchVariables(List<String> variableNames) {
		watchList.addAll(variableNames);
	}

	@Override
	public void startWatch() {
		watch = true;
	}

	@Override
	public void stopWatch() {
		watch = false;
		
		// reset variable-watch branch of the state tree
		flushWatchTree();
	}

	@Override
	public void clearWatchVariables() {
		watchList.clear();
	}
	
	public void flushWatchTree()
	{		
		for(AStateNode node : tree.getChildren())
		{
			if(node.getName().equals(WATCH_TREE_ID))
			{
				// re-assign to empty node
				node = new CompositeStateNode(WATCH_TREE_ID);
				break;
			}
		}
	}
	
	private Random getRandomGenerator()
	{
		if(randomGenerator == null)
		{
			randomGenerator = new Random();
		}
		return randomGenerator;
	}
}
