package org.geppetto.testbackend.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.IModelInterpreter;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.DynamicsSpecificationNode;
import org.geppetto.core.model.runtime.FunctionNode;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.model.values.DoubleValue;
import org.geppetto.testbackend.services.DummySimulatorService.TEST_NO;
import org.geppetto.testbackend.utilities.Utilities;
import org.springframework.stereotype.Service;

/**
 * Dummy Implementation of IModelInterpreter.
 * 
 * Creates Scene adding randomly positioned particles and geometries
 * 
 * @author jrmartin
 * 
 */
@Service
public class DummyModelInterpreterService implements IModelInterpreter
{

	private static Log logger = LogFactory.getLog(DummyModelInterpreterService.class);

	private static final String TEST = "TEST";
	
	public IModel readModel(URL url, List<URL> recordings, String instancePath) throws ModelInterpreterException
	{

		logger.info("Reading Model using Dummy Motel Interpreter Service");

		ModelWrapper wrapper = new ModelWrapper(UUID.randomUUID().toString());
		wrapper.setInstancePath(instancePath);
		TEST_NO test = Utilities.getTestName(url.toString());

		logger.warn("Wrap Model " + test);

		//sets model in wrapper, if it detects the model URL is 
		//neuron (hardcoded in simulation test file), then it adds 
		//python script as part of process
		if(test.toString().equals("neuron")){
			wrapper.wrapModel("process", "/neuron_demos/neuron_python_test.py");
		}else{
			wrapper.wrapModel(TEST, test);
		}
		
		return wrapper;
	}

	@Override
	public boolean populateModelTree(AspectNode aspectNode) {
		AspectSubTreeNode modelTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.MODEL_TREE);
		
		DynamicsSpecificationNode dynamics = new DynamicsSpecificationNode("Dynamics");
		
		PhysicalQuantity value = new PhysicalQuantity();
		value.setScalingFactor("10");
		value.setUnit("ms");
		value.setValue(new DoubleValue(10));
		dynamics.setInitialConditions(value);
		
		FunctionNode function = new FunctionNode("Function");
		function.setExpression("y=x+2");
		
		dynamics.setDynamics(function);
		
		ParameterSpecificationNode parameter = new ParameterSpecificationNode("Parameter");
		
		PhysicalQuantity value1 = new PhysicalQuantity();
		value1.setScalingFactor("10");
		value1.setUnit("ms");
		value1.setValue(new DoubleValue(10));	
		
		parameter.setValue(value1);
		
		FunctionNode functionNode = new FunctionNode("FunctionNode");
		functionNode.setExpression("y=x^2");
		List<String> arguments = new ArrayList<String>();
		arguments.add("1");
		functionNode.setArgument(arguments);
				
		modelTree.addChild(parameter);
		modelTree.addChild(dynamics);
		modelTree.addChild(functionNode);
		
		return false;
	}

	@Override
	public boolean populateRuntimeTree(AspectNode aspectNode) {
		AspectSubTreeNode modelTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.MODEL_TREE);
		AspectSubTreeNode visualizationTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.VISUALIZATION_TREE);
		AspectSubTreeNode simulationTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.WATCH_TREE);
		
		return true;
	}

	@Override
	public String getName()
	{
		return "Dummy Model Interpreter";
	}

}
