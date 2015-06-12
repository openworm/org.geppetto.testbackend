/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011 - 2015 OpenWorm.
 * http://openworm.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *
 * Contributors:
 *     	OpenWorm - http://openworm.org/people.html
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE 
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.geppetto.testbackend.services;

import java.text.DecimalFormat;
import java.util.Random;

import javax.measure.converter.RationalConverter;
import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

import org.geppetto.core.model.quantities.Quantity;
import org.geppetto.core.model.runtime.ACompositeNode;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.VariableNode;
import org.geppetto.core.model.state.visitors.DefaultStateVisitor;
import org.geppetto.core.model.values.AValue;
import org.geppetto.core.model.values.ValuesFactory;
import org.jscience.physics.amount.Amount;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 *         This visitor creates a simulation tree in the runtime model.
 * 
 * 
 */
public class CreateDummySimulationTreeVisitor extends DefaultStateVisitor
{
	private AspectSubTreeNode _simulationTree;
	private Random _randomGenerator = new Random();
	private String scaleFactor = null;
	private String _simulatorName = "";

	private DecimalFormat df = new DecimalFormat("0.E0");
	DecimalFormat df2 = new DecimalFormat("###.##");

	private double timeTracker = 0;
	private double step = 0.05;

	public CreateDummySimulationTreeVisitor()
	{
		super();
	}

	public CreateDummySimulationTreeVisitor(AspectSubTreeNode simulationTree, String simulatorName)
	{
		super();
		this._simulationTree = simulationTree;
		this._simulatorName = simulatorName;

		updateTimeNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.state.visitors.DefaultStateVisitor#inAspectNode (org.geppetto.core.model.runtime.AspectNode)
	 */
	@Override
	public boolean inCompositeNode(CompositeNode node)
	{
		// we only visit the nodes which belong to the same aspect
		return super.inCompositeNode(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.state.visitors.DefaultStateVisitor#visitVariableNode (org.geppetto.core.model.runtime.VariableNode)
	 */
	@Override
	public boolean visitVariableNode(VariableNode node)
	{

		VariableNode dummyNode = null;
		String watchedVariable = node.getInstancePath();

		for(ANode child : this._simulationTree.getChildren())
		{
			if(child.getName().equals(watchedVariable))
			{
				// assign if it already exists
				dummyNode = (VariableNode) child;
			}
		}

		// only add if it's not already there
		if(dummyNode == null)
		{
			dummyNode = new VariableNode(watchedVariable);
			dummyNode.setUnit(new org.geppetto.core.model.quantities.Unit("mV"));
			this._simulationTree.addChild(dummyNode);
		}

		Quantity p = new Quantity();
		AValue val = null;

		// NOTE: this is a dummy simulator so we're making values up - we wouldn't need to do this in a real one
		if(watchedVariable.toLowerCase().contains("double"))
		{
			val = ValuesFactory.getDoubleValue(this._randomGenerator.nextDouble());
		}
		else if(watchedVariable.toLowerCase().contains("float"))
		{
			val = ValuesFactory.getFloatValue(this._randomGenerator.nextFloat());
		}

		if(scaleFactor == null)
		{
			calculateScaleFactor(val);
		}

		p.setScalingFactor(scaleFactor);

		p.setValue(val);

		updateTimeNode();
		return super.visitVariableNode(node);
	}

	private void calculateScaleFactor(AValue val)
	{
		String unit = val.getStringValue() + " " + "mV";
		Amount<?> m2 = Amount.valueOf(unit);

		Unit<?> sUnit = m2.getUnit().getStandardUnit();

		UnitConverter r = m2.getUnit().getConverterTo(sUnit);

		long factor = 0;
		if(r instanceof RationalConverter)
		{
			factor = ((RationalConverter) r).getDivisor();
		}

		scaleFactor = df.format(factor);
		;
	}

	/**
	 * Create Time Tree
	 */
	private void updateTimeNode()
	{
		ACompositeNode time = new CompositeNode("dummyID");

		if(time.getChildren().size() == 0)
		{
			Quantity stepQ = new Quantity();
			AValue stepVal = ValuesFactory.getDoubleValue(step);
			stepQ.setValue(stepVal);

			Quantity timeQ = new Quantity();
			AValue timeVal = ValuesFactory.getDoubleValue(timeTracker);
			timeQ.setValue(timeVal);

			// Add the name of the simulator to tree time node, to distinguis it from other
			// times from other simulators
			VariableNode name = new VariableNode("simulator");
			Quantity q = new Quantity();
			q.setValue(ValuesFactory.getStringValue(this._simulatorName));
			name.addQuantity(q);

			VariableNode stepNode = new VariableNode("step");
			stepNode.addQuantity(stepQ);

			VariableNode timeNode = new VariableNode("time");
			timeNode.addQuantity(timeQ);

			time.addChild(stepNode);
			time.addChild(timeNode);
		}
		else
		{
			for(ANode child : time.getChildren())
			{
				if(child.getName().equals("time"))
				{
					Quantity q = new Quantity();
					AValue timeVal = ValuesFactory.getDoubleValue(timeTracker);
					q.setValue(timeVal);
					((VariableNode) child).addQuantity(q);
				}
				else if(child.getName().equals("step"))
				{
					Quantity q = new Quantity();
					AValue timeVal = ValuesFactory.getDoubleValue(step);
					q.setValue(timeVal);
					((VariableNode) child).addQuantity(q);
				}
			}
		}
		timeTracker += step;

		timeTracker = Double.valueOf(df2.format(timeTracker));
	}

}
