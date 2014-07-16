package org.geppetto.testbackend.services;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.measure.converter.RationalConverter;
import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.data.model.AVariable;
import org.geppetto.core.data.model.SimpleType;
import org.geppetto.core.data.model.SimpleType.Type;
import org.geppetto.core.data.model.SimpleVariable;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.runtime.ACompositeNode;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.CompositeVariableNode;
import org.geppetto.core.model.runtime.VariableNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.model.values.AValue;
import org.geppetto.core.model.values.ValuesFactory;
import org.geppetto.core.simulation.IRunConfiguration;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.ASimulator;
import org.jscience.physics.amount.Amount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Dummy implementation of ISimulator.
 * 
 * @author jrmartin
 * 
 */
@Service
public class DummySimulatorService extends ASimulator
{

	private static Log _logger = LogFactory.getLog(DummySimulatorService.class);

	@Autowired
	private SimulatorConfig dummySimulatorConfig;
	
	DecimalFormat df = new DecimalFormat("0.E0");

	AspectSubTreeNode tree = new AspectSubTreeNode("dummyServices");
	private Random randomGenerator;
	private double timeTracker = 0;
	private double step = 0.05;
	private String scaleFactor = null;
	
	DecimalFormat df2 = new DecimalFormat("###.##");
	  
	// TODO: all this stuff should come from configuration
	private final String _aspectID = "dummy";

	public DummySimulatorService()
	{
		super();
		_stateTree = new AspectSubTreeNode("dummyServices");
	}

	public void initialize(List<IModel> model, ISimulatorCallbackListener listener) throws GeppettoInitializationException, GeppettoExecutionException
	{
		super.initialize(model, listener);

		if(_stateTree.getSubTree(AspectSubTreeNode.AspectTreeType.MODEL_TREE)!=null){
			_stateTree.getSubTree(AspectSubTreeNode.AspectTreeType.MODEL_TREE).getChildren().clear();
		}
		
		VariableNode child = new VariableNode("dummyChild");
		// init statetree

		PhysicalQuantity q = new PhysicalQuantity();
		q.setValue(ValuesFactory.getDoubleValue(getRandomGenerator().nextDouble()));
		
		((VariableNode)_stateTree.getSubTree(AspectSubTreeNode.AspectTreeType.MODEL_TREE).addChild(child)).addPhysicalQuantity(q);
		
		// populate watch / force variables
		setWatchableVariables();
		setForceableVariables();
				
		getListener().stateTreeUpdated(_stateTree);
	}

	@Override
	public String getName() {
		return this.dummySimulatorConfig.getSimulatorName();
	}

	public void simulate(IRunConfiguration runConfiguration) throws GeppettoExecutionException
	{
		PhysicalQuantity q = new PhysicalQuantity();
		q.setValue(ValuesFactory.getDoubleValue(getRandomGenerator().nextDouble()));
		// throw some junk into model-interpreter node as if results were being populated
		((VariableNode) _stateTree.getSubTree(AspectSubTreeNode.AspectTreeType.MODEL_TREE).getChildren().get(0)).addPhysicalQuantity(q);

		if(isWatching())
		{
			// add values of variables being watched to state tree
			updateStateTreeForWatch();
		}

		getListener().stateTreeUpdated(_stateTree);		
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void updateStateTreeForWatch()
	{
		ACompositeNode watchTree = _stateTree.getSubTree(AspectTreeType.WATCH_TREE);
		updateTimeNode();
		
		// check which watchable variables are being watched
		for(AVariable var : getWatchableVariables().getVariables())
		{
			for(String varName : getWatchList())
			{
				// if they are being watched add to state tree
				if(varName.toLowerCase().equals(var.getName().toLowerCase()))
				{
					VariableNode dummyNode = null;

					for(ANode child : watchTree.getChildren())
					{
						if(child.getName().equals(var.getName()))
						{
							// assign if it already exists
							dummyNode = (VariableNode) child;
						}
					}

					// only add if it's not already there
					if(dummyNode == null)
					{
						dummyNode = new VariableNode(var.getName());
						watchTree.addChild(dummyNode);
					}

					PhysicalQuantity p = new PhysicalQuantity();
					AValue val = null;

					// NOTE: this is a dummy simulator so we're making values up - we wouldn't need to do this in a real one
					if(varName.toLowerCase().contains("double"))
					{
						val = ValuesFactory.getDoubleValue(getRandomGenerator().nextDouble());
					}
					else if(varName.toLowerCase().contains("float"))
					{
						val = ValuesFactory.getFloatValue(getRandomGenerator().nextFloat());
					}
					
					p.setUnit("mV");
					
					if(scaleFactor == null){
						calculateScaleFactor(val);
					}

					p.setScalingFactor(scaleFactor);

					p.setValue(val);
					
					updateTimeNode();
				}
			}
		}
	}



	private void calculateScaleFactor(AValue val) {
		String unit = val.getStringValue() + " " + "mV";
		Amount<?> m2 = Amount.valueOf(unit);

		Unit<?> sUnit = m2.getUnit().getStandardUnit();

		UnitConverter r = m2.getUnit().getConverterTo(sUnit);

		long factor = 0; 
		if(r instanceof RationalConverter ){
			factor = ((RationalConverter) r).getDivisor();
		}
		
		scaleFactor = df.format(factor);;
	}

	/**
	 * Populates some dummy state variables to test list variables functionality
	 * */
	private void setWatchableVariables()
	{
		// NOTE: this could be more elegantly injected via spring
		List<AVariable> vars = new ArrayList<AVariable>();

		// dummyInt
		SimpleVariable dummyDouble = new SimpleVariable();
		SimpleType doubleType = new SimpleType();
		doubleType.setType(Type.DOUBLE);
		dummyDouble.setAspect(_aspectID);
		dummyDouble.setName("dummyDouble");
		dummyDouble.setType(doubleType);
		// dummyFloat
		SimpleVariable dummyFloat = new SimpleVariable();
		SimpleType floatType = new SimpleType();
		floatType.setType(Type.FLOAT);
		dummyFloat.setAspect(_aspectID);
		dummyFloat.setName("dummyFloat");
		dummyFloat.setType(floatType);

		vars.add(dummyDouble);
		vars.add(dummyFloat);

		getWatchableVariables().setVariables(vars);
	}

	/**
	 * Populates some dummy state variables to test list variables functionality
	 * */
	private void setForceableVariables()
	{
		// NOTE: this could be more elegantly injected via spring
		List<AVariable> vars = new ArrayList<AVariable>();

		// dummyInt
		SimpleVariable dummyInt = new SimpleVariable();
		SimpleType integerType = new SimpleType();
		integerType.setType(Type.INTEGER);
		dummyInt.setAspect(_aspectID);
		dummyInt.setName("dummyInt");
		dummyInt.setType(integerType);

		vars.add(dummyInt);

		getForceableVariables().setVariables(vars);
	}

	private Random getRandomGenerator()
	{
		if(randomGenerator == null)
		{
			randomGenerator = new Random();
		}
		return randomGenerator;
	}
	
	/**
	 * Create Time Tree
	 */
	private void updateTimeNode(){
		ACompositeNode time = new CompositeVariableNode();

		if(time.getChildren().size() == 0){
			PhysicalQuantity stepQ = new PhysicalQuantity();
			AValue stepVal = ValuesFactory.getDoubleValue(step);
			stepQ.setValue(stepVal);
			
			PhysicalQuantity timeQ = new PhysicalQuantity();
			AValue timeVal = ValuesFactory.getDoubleValue(timeTracker);
			timeQ.setValue(timeVal);
			
			//Add the name of the simulator to tree time node, to distinguis it from other
			//times from other simulators
			VariableNode name = new VariableNode("simulator");
			PhysicalQuantity q = new PhysicalQuantity();
			q.setValue(ValuesFactory.getStringValue(this.getName()));
			name.addPhysicalQuantity(q);
			
			VariableNode stepNode = new VariableNode("step");
			stepNode.addPhysicalQuantity(stepQ);

			VariableNode timeNode = new VariableNode("time");
			timeNode.addPhysicalQuantity(timeQ);
			
			time.addChild(stepNode);
			time.addChild(timeNode);
		}
		else{
			for(ANode child : time.getChildren()){
				if(child.getName().equals("time")){
					PhysicalQuantity q = new PhysicalQuantity();
					AValue timeVal = ValuesFactory.getDoubleValue(timeTracker);
					q.setValue(timeVal);
					((VariableNode)child).addPhysicalQuantity(q);
				}
				else if(child.getName().equals("step")){
					PhysicalQuantity q = new PhysicalQuantity();
					AValue timeVal = ValuesFactory.getDoubleValue(step);
					q.setValue(timeVal);
					((VariableNode)child).addPhysicalQuantity(q);
				}
			}
		}
		timeTracker += step;
		
		timeTracker = Double.valueOf(df2.format(timeTracker));
	}

	@Override
	public boolean populateVisualTree(AspectNode aspectNode) throws ModelInterpreterException
	{
		// TODO Auto-generated method stub
		return false;
	}
}
