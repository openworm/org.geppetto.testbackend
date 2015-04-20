package org.geppetto.testbackend.services;

import org.geppetto.core.features.IWatchableVariableListFeature;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.VariableNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.services.GeppettoFeature;

public class DummySimulationTreeFeature implements IWatchableVariableListFeature
{
	private AspectSubTreeNode simulationTree;
	
	private GeppettoFeature type = GeppettoFeature.WATCHABLE_VARIABLE_LIST_FEATURE;

	@Override
	public GeppettoFeature getType()
	{
		return type;
	}

	@Override
	public boolean listWatchableVariables(AspectNode aspectNode) throws ModelInterpreterException
	{
		boolean modified = true;

		simulationTree = (AspectSubTreeNode) aspectNode.getSubTree(AspectTreeType.SIMULATION_TREE);
		simulationTree.setId(AspectTreeType.SIMULATION_TREE.toString());
		simulationTree.setModified(modified);
		
		VariableNode dummyDouble = new VariableNode("dummyDouble");
		dummyDouble.setName("dummyDouble");
		simulationTree.addChild(dummyDouble);

		VariableNode dummyFloat = new VariableNode("dummyFloat");
		dummyFloat.setName("dummyFloat");
		simulationTree.addChild(dummyFloat);

		return modified;
	}

	

}
