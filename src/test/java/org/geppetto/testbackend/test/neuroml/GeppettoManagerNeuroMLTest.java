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
package org.geppetto.testbackend.test.neuroml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.data.DataManagerHelper;
import org.geppetto.core.data.DefaultGeppettoDataManager;
import org.geppetto.core.data.model.ExperimentStatus;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.data.model.IExperiment;
import org.geppetto.core.data.model.IGeppettoProject;
import org.geppetto.core.data.model.ResultsFormat;
import org.geppetto.core.manager.Scope;
import org.geppetto.core.services.registry.ApplicationListenerBean;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.ExperimentState;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.GeppettoModel;
import org.geppetto.model.ModelFormat;
import org.geppetto.model.VariableValue;
import org.geppetto.model.neuroml.services.LEMSConversionService;
import org.geppetto.model.neuroml.services.LEMSModelInterpreterService;
import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.geppetto.model.types.Type;
import org.geppetto.model.values.Quantity;
import org.geppetto.model.values.TimeSeries;
import org.geppetto.simulation.manager.ExperimentRunManager;
import org.geppetto.simulation.manager.GeppettoManager;
import org.geppetto.simulation.manager.RuntimeProject;
import org.geppetto.simulator.external.services.NeuronSimulatorService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * 
 * This is an integration test which checks the workkflows of the GeppettoManager. Provides coverage also for RuntimeProject and RuntimeExperiment.
 * 
 * @author matteocantarelli
 *
 */
public class GeppettoManagerNeuroMLTest
{

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private static GeppettoManager manager = new GeppettoManager(Scope.CONNECTION);
	private static IGeppettoProject geppettoProject;
	private static RuntimeProject runtimeProject;
	private static IExperiment addedExperiment;

	/**
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings("deprecation")
	@BeforeClass
	public static void setUp() throws Exception
	{
		GenericWebApplicationContext context = new GenericWebApplicationContext();
		BeanDefinition neuroMLModelInterpreterBeanDefinition = new RootBeanDefinition(NeuroMLModelInterpreterService.class);
		BeanDefinition lemsModelInterpreterBeanDefinition = new RootBeanDefinition(LEMSModelInterpreterService.class);
		BeanDefinition conversionServiceBeanDefinition = new RootBeanDefinition(LEMSConversionService.class);
		BeanDefinition neuronSimulatorServiceBeanDefinition = new RootBeanDefinition(NeuronSimulatorService.class);
		context.registerBeanDefinition("neuroMLModelInterpreter", neuroMLModelInterpreterBeanDefinition);
		context.registerBeanDefinition("scopedTarget.neuroMLModelInterpreter", neuroMLModelInterpreterBeanDefinition);
		context.registerBeanDefinition("lemsModelInterpreter", lemsModelInterpreterBeanDefinition);
		context.registerBeanDefinition("scopedTarget.lemsModelInterpreter", lemsModelInterpreterBeanDefinition);
		context.registerBeanDefinition("lemsConversion", conversionServiceBeanDefinition);
		context.registerBeanDefinition("scopedTarget.lemsConversion", conversionServiceBeanDefinition);
		context.registerBeanDefinition("neuronSimulator", neuronSimulatorServiceBeanDefinition);
		context.registerBeanDefinition("scopedTarget.neuronSimulator", neuronSimulatorServiceBeanDefinition);
		
		ContextRefreshedEvent event = new ContextRefreshedEvent(context);
		ApplicationListenerBean listener = new ApplicationListenerBean();
		listener.onApplicationEvent(event);
		ApplicationContext retrievedContext = ApplicationListenerBean.getApplicationContext("neuroMLModelInterpreter");
		Assert.assertNotNull(retrievedContext.getBean("scopedTarget.neuroMLModelInterpreter"));
		Assert.assertTrue(retrievedContext.getBean("scopedTarget.neuroMLModelInterpreter") instanceof NeuroMLModelInterpreterService);
		Assert.assertNotNull(retrievedContext.getBean("scopedTarget.lemsModelInterpreter"));
		Assert.assertTrue(retrievedContext.getBean("scopedTarget.lemsModelInterpreter") instanceof LEMSModelInterpreterService);
		Assert.assertNotNull(retrievedContext.getBean("scopedTarget.lemsConversion"));
		Assert.assertTrue(retrievedContext.getBean("scopedTarget.lemsConversion") instanceof LEMSConversionService);
		Assert.assertNotNull(retrievedContext.getBean("scopedTarget.neuronSimulator"));
		Assert.assertTrue(retrievedContext.getBean("scopedTarget.neuronSimulator") instanceof NeuronSimulatorService);
		DataManagerHelper.setDataManager(new DefaultGeppettoDataManager());
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#setUser(org.geppetto.core.data.model.IUser)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testSetUser() throws GeppettoExecutionException
	{
		manager.setUser(DataManagerHelper.getDataManager().newUser("nonna", "passauord", true));
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#getUser()}.
	 */
	@Test
	public void testGetUser()
	{
		Assert.assertEquals("nonna", manager.getUser().getName());
		Assert.assertEquals("passauord", manager.getUser().getPassword());
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#loadProject(java.lang.String, org.geppetto.core.data.model.IGeppettoProject)}.
	 * 
	 * @throws IOException
	 * @throws GeppettoExecutionException
	 * @throws GeppettoInitializationException
	 */
	@Test
	public void testLoadProject() throws IOException, GeppettoInitializationException, GeppettoExecutionException
	{
		URL url = new File("./src/test/resources/test/test.json").toURI().toURL();
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		geppettoProject = DataManagerHelper.getDataManager().getProjectFromJson(TestUtilities.getGson(), reader);
		manager.loadProject("1", geppettoProject);

	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#getRuntimeProject(org.geppetto.core.data.model.IGeppettoProject)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testRuntimeProject() throws GeppettoExecutionException
	{
		runtimeProject = manager.getRuntimeProject(geppettoProject);
		Assert.assertNotNull(runtimeProject);

		// Testing ImportType visitor is working properly
		GeppettoModel geppettoModel = runtimeProject.getGeppettoModel();
		Assert.assertEquals(1, geppettoModel.getVariables().get(0).getTypes().size());
		Type type = geppettoModel.getVariables().get(0).getTypes().get(0);
		Assert.assertEquals("network_ACnet2", type.getId());
		Assert.assertEquals("network - network_ACnet2", type.getName());

		// Testing libraries are there
		Assert.assertEquals(2, geppettoModel.getLibraries().size());
		GeppettoLibrary common = geppettoModel.getLibraries().get(1);
		Assert.assertEquals("common", common.getId());
		Assert.assertEquals("Geppetto Common Library", common.getName());
		GeppettoLibrary neuromlLibrary = geppettoModel.getLibraries().get(0);
		Assert.assertEquals("neuroMLLibrary", neuromlLibrary.getId());
		Assert.assertEquals("neuroMLLibrary", neuromlLibrary.getName());

	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#loadExperiment(java.lang.String, org.geppetto.core.data.model.IExperiment)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testLoadExperiment() throws GeppettoExecutionException
	{
		ExperimentState experimentState = manager.loadExperiment("1", geppettoProject.getExperiments().get(0));
		Assert.assertNotNull(experimentState);
		List<VariableValue> parameters = experimentState.getSetParameters();
		List<VariableValue> recordedVariables = experimentState.getRecordedVariables();
		Assert.assertEquals(2, parameters.size());
		Assert.assertEquals(3, recordedVariables.size());
		VariableValue b = recordedVariables.get(0);
		Assert.assertEquals("mediumNet(network_ACnet2).pyramidals_48(pyramidals_48)[4].biophys(biophys).membraneProperties(membraneProperties).iCa(StateVariable)", b.getPointer().getInstancePath());
		VariableValue p1 = parameters.get(1);
		Assert.assertEquals("mediumNet(network_ACnet2).baskets_12(baskets_12)[0].biophys(biophys).intracellularProperties(intracellularProperties).resistivity(resistivity)", p1.getPointer().getInstancePath());
		Quantity q = (Quantity) p1.getValue();
		Assert.assertEquals(0.2, q.getValue(), 0);

	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#checkExperimentsStatus(java.lang.String, org.geppetto.core.data.model.IGeppettoProject)}.
	 */
	@Test
	public void testCheckExperimentsStatus()
	{
		List<? extends IExperiment> status = manager.checkExperimentsStatus("1", geppettoProject);
		Assert.assertEquals(1, status.size());
		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(0).getStatus());
	}

	/**
	 * Test method for
	 * {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#setModelParameters(java.util.Map, org.geppetto.core.data.model.IExperiment, org.geppetto.core.data.model.IGeppettoProject)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testSetModelParametersNegativeNoDesign() throws GeppettoExecutionException
	{
		Map<String, String> parametersMap = new HashMap<String, String>();
		parametersMap.put("testVar(testType).p2(Parameter)", "0.234");
		exception.expect(GeppettoExecutionException.class);
		manager.setModelParameters(parametersMap, runtimeProject.getActiveExperiment(), geppettoProject);

	}

	/**
	 * Test method for
	 * {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#setWatchedVariables(java.util.List, org.geppetto.core.data.model.IExperiment, org.geppetto.core.data.model.IGeppettoProject)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testSetWatchedVariablesNegativeNoDesign() throws GeppettoExecutionException
	{
		List<String> watchedVariables = new ArrayList<String>();
		// the following line stops recording a
		watchedVariables.add("testVar(testType).a(StateVariable)");
		exception.expect(GeppettoExecutionException.class);
		manager.setWatchedVariables(watchedVariables, runtimeProject.getActiveExperiment(), geppettoProject);
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#newExperiment(java.lang.String, org.geppetto.core.data.model.IGeppettoProject)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testNewExperiment() throws GeppettoExecutionException
	{
		Assert.assertEquals(1, geppettoProject.getExperiments().size());
		addedExperiment = manager.newExperiment("2", geppettoProject);
		Assert.assertEquals(2, geppettoProject.getExperiments().size());
		Assert.assertEquals(1, addedExperiment.getAspectConfigurations().size());
		IAspectConfiguration ac = addedExperiment.getAspectConfigurations().get(0);
		ac.getSimulatorConfiguration().setSimulatorId("neuronSimulator");

	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#loadExperiment(java.lang.String, org.geppetto.core.data.model.IExperiment)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testLoadNewExperiment() throws GeppettoExecutionException
	{
		ExperimentState experimentState = manager.loadExperiment("1", geppettoProject.getExperiments().get(1));
		Assert.assertNotNull(experimentState);
		List<VariableValue> parameters = experimentState.getSetParameters();
		List<VariableValue> recordedVariables = experimentState.getRecordedVariables();
		Assert.assertEquals(0, parameters.size());
		Assert.assertEquals(1, recordedVariables.size()); // just time
	}

	/**
	 * Test method for
	 * {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#setModelParameters(java.util.Map, org.geppetto.core.data.model.IExperiment, org.geppetto.core.data.model.IGeppettoProject)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testSetModelParameters() throws GeppettoExecutionException
	{
		Map<String, String> parametersMap = new HashMap<String, String>();
		parametersMap.put("mediumNet(network_ACnet2).pyramidals_48(pyramidals_48)[3].biophys.membraneProperties.Na_pyr_soma_group.Na_pyr.conductance", "0.234");
		ExperimentState experimentState = manager.setModelParameters(parametersMap, runtimeProject.getActiveExperiment(), geppettoProject);
		List<VariableValue> parameters = experimentState.getSetParameters();
		Assert.assertEquals(1, parameters.size());
		VariableValue p2 = parameters.get(0);
		Assert.assertEquals("mediumNet(network_ACnet2).pyramidals_48(pyramidals_48)[3].biophys(biophys).membraneProperties(membraneProperties).Na_pyr_soma_group(Na_pyr_soma_group).Na_pyr(Na_pyr).conductance(Parameter)", p2.getPointer().getInstancePath());
		Quantity q = (Quantity) p2.getValue();
		Assert.assertEquals(0.234, q.getValue(), 0);
	}

	/**
	 * Test method for
	 * {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#setModelParameters(java.util.Map, org.geppetto.core.data.model.IExperiment, org.geppetto.core.data.model.IGeppettoProject)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testSetModelParametersNegativeWrongParameter() throws GeppettoExecutionException
	{
		Map<String, String> parametersMap = new HashMap<String, String>();
		parametersMap.put("testVar(testType).p3(Parameter)", "0.234");
		// p3 does not exist
		exception.expect(GeppettoExecutionException.class);
		manager.setModelParameters(parametersMap, runtimeProject.getActiveExperiment(), geppettoProject);
	}

	/**
	 * Test method for
	 * {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#setWatchedVariables(java.util.List, org.geppetto.core.data.model.IExperiment, org.geppetto.core.data.model.IGeppettoProject)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testSetWatchedVariables() throws GeppettoExecutionException
	{
		List<String> watchedVariables = new ArrayList<String>();
		watchedVariables.add("mediumNet(network_ACnet2).baskets_12(baskets_12)[0].v(StateVariable)");
		watchedVariables.add("mediumNet(network_ACnet2).pyramidals_48(pyramidals_48)[4].biophys.membraneProperties.iCa(StateVariable)");
		ExperimentState experimentState = manager.setWatchedVariables(watchedVariables, runtimeProject.getActiveExperiment(), geppettoProject);
		List<VariableValue> recordedVariables = experimentState.getRecordedVariables();
		Assert.assertEquals(3, recordedVariables.size()); // a+c+time
		Assert.assertEquals(2, addedExperiment.getAspectConfigurations().get(0).getWatchedVariables().size()); // no time inside aspectConfiguration
		VariableValue c = recordedVariables.get(2);
		Assert.assertEquals("mediumNet(network_ACnet2).pyramidals_48(pyramidals_48)[4].biophys(biophys).membraneProperties(membraneProperties).iCa(StateVariable)", c.getPointer().getInstancePath());

		// the following line stops recording a
		List<String> watchedVariables2 = new ArrayList<String>();
		watchedVariables2.add("mediumNet(network_ACnet2).baskets_12(baskets_12)[0].v(StateVariable)");
		ExperimentState experimentState2 = manager.setWatchedVariables(watchedVariables2, runtimeProject.getActiveExperiment(), geppettoProject);
		List<VariableValue> recordedVariables2 = experimentState2.getRecordedVariables();
		Assert.assertEquals(2, recordedVariables2.size());
		Assert.assertEquals(1, addedExperiment.getAspectConfigurations().get(0).getWatchedVariables().size());
		c = recordedVariables2.get(1);
		Assert.assertEquals("mediumNet(network_ACnet2).pyramidals_48(pyramidals_48)[4].biophys(biophys).membraneProperties(membraneProperties).iCa(StateVariable)", c.getPointer().getInstancePath());

		// Let's add a again and b too
		List<String> watchedVariables3 = new ArrayList<String>();
		watchedVariables3.add("mediumNet(network_ACnet2).baskets_12(baskets_12)[0].v(StateVariable)");
		watchedVariables3.add("mediumNet(network_ACnet2).baskets_12(baskets_12)[0].biophys.intracellularProperties.caConc");
		ExperimentState experimentState3 = manager.setWatchedVariables(watchedVariables3, runtimeProject.getActiveExperiment(), geppettoProject);
		List<VariableValue> recordedVariables3 = experimentState3.getRecordedVariables();
		Assert.assertEquals(4, recordedVariables3.size());
		Assert.assertEquals(3, addedExperiment.getAspectConfigurations().get(0).getWatchedVariables().size());
		c = recordedVariables3.get(1);
		VariableValue a = recordedVariables3.get(2);
		VariableValue b = recordedVariables3.get(3);
		Assert.assertEquals("mediumNet(network_ACnet2).pyramidals_48(pyramidals_48)[4].biophys(biophys).membraneProperties(membraneProperties).iCa(StateVariable)", c.getPointer().getInstancePath());
		Assert.assertEquals("mediumNet(network_ACnet2).baskets_12(baskets_12)[0].v(StateVariable)", a.getPointer().getInstancePath());
		Assert.assertEquals("mediumNet(network_ACnet2).baskets_12(baskets_12)[0].biophys(biophys).intracellularProperties(intracellularProperties).caConc(StateVariable)", b.getPointer().getInstancePath());

	}

	/**
	 * Test method for
	 * {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#setWatchedVariables(java.util.List, org.geppetto.core.data.model.IExperiment, org.geppetto.core.data.model.IGeppettoProject)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testSetWatchedVariablesNegativeWrongVariable() throws GeppettoExecutionException
	{
		List<String> watchedVariables = new ArrayList<String>();
		watchedVariables.add("testVar(testType).d(Parameter)");
		// d does not exist
		exception.expect(GeppettoExecutionException.class);
		manager.setWatchedVariables(watchedVariables, runtimeProject.getActiveExperiment(), geppettoProject);
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#checkExperimentsStatus(java.lang.String, org.geppetto.core.data.model.IGeppettoProject)}.
	 */
	@Test
	public void testCheckExperimentsStatusAgain()
	{
		List<? extends IExperiment> status = manager.checkExperimentsStatus("1", geppettoProject);
		Assert.assertEquals(2, status.size());
		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(0).getStatus());
		Assert.assertEquals(ExperimentStatus.DESIGN, status.get(1).getStatus());
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#runExperiment(java.lang.String, org.geppetto.core.data.model.IExperiment)}.
	 * 
	 * @throws GeppettoExecutionException
	 * @throws InterruptedException
	 */
	@Test
	public void testRunExperiment() throws GeppettoExecutionException, InterruptedException
	{
		Assert.assertNotNull(ExperimentRunManager.getInstance());
		Assert.assertEquals(1, addedExperiment.getAspectConfigurations().size());
		IAspectConfiguration ac = addedExperiment.getAspectConfigurations().get(0);
		Assert.assertEquals("mediumNet(network_ACnet2)", ac.getAspect().getInstancePath());
		Assert.assertNotNull(ac.getSimulatorConfiguration());
		manager.runExperiment("1", addedExperiment);
		Assert.assertEquals(3, addedExperiment.getAspectConfigurations().get(0).getWatchedVariables().size());
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#checkExperimentsStatus(java.lang.String, org.geppetto.core.data.model.IGeppettoProject)}.
	 */
	@Test
	public void testCheckExperimentsStatusAgain2()
	{
		List<? extends IExperiment> status = manager.checkExperimentsStatus("1", geppettoProject);
		Assert.assertEquals(2, status.size());
		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(0).getStatus());
		Assert.assertEquals(ExperimentStatus.QUEUED, status.get(1).getStatus());
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#cancelExperimentRun(java.lang.String, org.geppetto.core.data.model.IExperiment)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testCancelExperimentRun() throws GeppettoExecutionException
	{
		manager.cancelExperimentRun("1", addedExperiment);
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#checkExperimentsStatus(java.lang.String, org.geppetto.core.data.model.IGeppettoProject)}.
	 */
	@Test
	public void testCheckExperimentsStatusAgain3()
	{
		List<? extends IExperiment> status = manager.checkExperimentsStatus("1", geppettoProject);
		Assert.assertEquals(2, status.size());
		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(0).getStatus());
		Assert.assertEquals(ExperimentStatus.DESIGN, status.get(1).getStatus());
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#runExperiment(java.lang.String, org.geppetto.core.data.model.IExperiment)}.
	 * 
	 * @throws GeppettoExecutionException
	 * @throws InterruptedException
	 */
	@Test
	public void testRunExperimentAgain() throws GeppettoExecutionException, InterruptedException
	{
		Assert.assertNotNull(ExperimentRunManager.getInstance());
		Assert.assertEquals(1, addedExperiment.getAspectConfigurations().size());
		IAspectConfiguration ac = addedExperiment.getAspectConfigurations().get(0);
		Assert.assertEquals("mediumNet(network_ACnet2)", ac.getAspect().getInstancePath());
		Assert.assertNotNull(ac.getSimulatorConfiguration());
		manager.runExperiment("1", addedExperiment);
		Assert.assertEquals(3, addedExperiment.getAspectConfigurations().get(0).getWatchedVariables().size());
		Thread.sleep(160000);
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#checkExperimentsStatus(java.lang.String, org.geppetto.core.data.model.IGeppettoProject)}.
	 */
	@Test
	public void testCheckExperimentsStatusAgain4()
	{
		List<? extends IExperiment> status = manager.checkExperimentsStatus("1", geppettoProject);
		Assert.assertEquals(2, status.size());
		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(0).getStatus());
		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(1).getStatus());
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#playExperiment(java.lang.String, org.geppetto.core.data.model.IExperiment)}.
	 * 
	 * @throws GeppettoExecutionException
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	@Test
	public void testPlayExperiment() throws GeppettoExecutionException, NumberFormatException, IOException
	{
		ExperimentState experimentState = manager.playExperiment("1", addedExperiment, null);
		List<VariableValue> recorded = experimentState.getRecordedVariables();
		Assert.assertEquals(4, recorded.size());
		VariableValue time = recorded.get(0);
		VariableValue c = recorded.get(1);
		VariableValue a = recorded.get(2);
		VariableValue b = recorded.get(3);
		Assert.assertEquals("time(StateVariable)", time.getPointer().getInstancePath());
		Assert.assertEquals("testVar(testType).c(StateVariable)", c.getPointer().getInstancePath());
		Assert.assertEquals("testVar(testType).a(StateVariable)", a.getPointer().getInstancePath());
		Assert.assertEquals("testVar(testType).b(StateVariable)", b.getPointer().getInstancePath());
		Assert.assertNotNull(time.getValue());
		Assert.assertNotNull(c.getValue());
		Assert.assertNotNull(a.getValue());
		Assert.assertNotNull(b.getValue());

		checkValues(b, 3);
		checkValues(time, 0);
		checkValues(a, 2);
		checkValues(c, 1);

		List<String> filter = new ArrayList<String>();
		filter.add("testVar(testType).a(StateVariable)");
		experimentState = manager.playExperiment("1", addedExperiment, filter);
		Assert.assertEquals("time(StateVariable)", time.getPointer().getInstancePath());
		Assert.assertEquals("testVar(testType).c(StateVariable)", c.getPointer().getInstancePath());
		Assert.assertEquals("testVar(testType).a(StateVariable)", a.getPointer().getInstancePath());
		Assert.assertEquals("testVar(testType).b(StateVariable)", b.getPointer().getInstancePath());
		
		Assert.assertNotNull(time.getValue());
		checkValues(time, 0);
		Assert.assertNull(c.getValue());
		Assert.assertNotNull(a.getValue());
		checkValues(a, 2);
		Assert.assertNull(b.getValue());

	}

	/**
	 * @param variable
	 * @param columnIndexInTheDATFile
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private void checkValues(VariableValue variable, int columnIndexInTheDATFile) throws NumberFormatException, IOException
	{
		// read DAT into a buffered reader
		BufferedReader input = new BufferedReader(new FileReader("./src/test/resources/test/testResults.dat"));

		ArrayList<Double> storedValues = new ArrayList<Double>();

		// read rest of DAT file and extract values
		while(input.read() != -1)
		{
			String line = input.readLine();
			String[] columns = line.split("\\s+");
			storedValues.add(Double.valueOf(columns[columnIndexInTheDATFile]));

		}

		Assert.assertArrayEquals(storedValues.toArray(), ((TimeSeries) variable.getValue()).getValue().toArray());

		input.close();
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#linkDropBoxAccount(java.lang.String)}.
	 * 
	 * @throws Exception
	 */
	public void testLinkDropBoxAccount() throws Exception
	{
		//Dropbox tests are commented out as they require the API token to work which should not go in the code
		//Until someone finds a strategy...
		manager.linkDropBoxAccount("");
	}

	/**
	 * Test method for
	 * {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#uploadModelToDropBox(java.lang.String, org.geppetto.core.data.model.IExperiment, org.geppetto.core.data.model.IGeppettoProject, org.geppetto.model.ModelFormat)}
	 * .
	 * 
	 * @throws Exception
	 */
	public void testUploadModelToDropBox() throws Exception
	{
		manager.uploadModelToDropBox("testVar(testType)", addedExperiment, geppettoProject, ServicesRegistry.getModelFormat("TEST_FORMAT"));
	}

	/**
	 * Test method for
	 * {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#uploadResultsToDropBox(java.lang.String, org.geppetto.core.data.model.IExperiment, org.geppetto.core.data.model.IGeppettoProject, org.geppetto.core.data.model.ResultsFormat)}
	 * .
	 * 
	 * @throws GeppettoExecutionException
	 */
	public void testUploadResultsToDropBox() throws GeppettoExecutionException
	{
		manager.uploadResultsToDropBox("testVar(testType)", addedExperiment, geppettoProject, ResultsFormat.GEPPETTO_RECORDING);
		manager.uploadResultsToDropBox("testVar(testType)", addedExperiment, geppettoProject, ResultsFormat.RAW);
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#unlinkDropBoxAccount(java.lang.String)}.
	 * 
	 * @throws Exception
	 */
	public void testUnlinkDropBoxAccount() throws Exception
	{
		manager.unlinkDropBoxAccount("");
	}

	/**
	 * Test method for
	 * {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#downloadModel(java.lang.String, org.geppetto.model.ModelFormat, org.geppetto.core.data.model.IExperiment, org.geppetto.core.data.model.IGeppettoProject)}
	 * .
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testDownloadModel() throws GeppettoExecutionException
	{
		File model=manager.downloadModel("testVar(testType)", ServicesRegistry.getModelFormat("TEST_FORMAT"), addedExperiment, geppettoProject);
		Assert.assertNotNull(model);
		Assert.assertEquals("ModelFile",model.getName());
	}

	/**
	 * Test method for
	 * {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#downloadResults(java.lang.String, org.geppetto.core.data.model.ResultsFormat, org.geppetto.core.data.model.IExperiment, org.geppetto.core.data.model.IGeppettoProject)}
	 * .
	 * 
	 * @throws GeppettoExecutionException
	 * @throws IOException 
	 */
	@Test
	public void testDownloadResults() throws GeppettoExecutionException, IOException
	{
		URL geppettoRecording=manager.downloadResults("testVar(testType)", ResultsFormat.GEPPETTO_RECORDING, addedExperiment, geppettoProject);
		Assert.assertTrue(geppettoRecording.getPath().endsWith("./src/test/resources/test/testResults.h5"));
		geppettoRecording.openConnection().connect();
		URL rawRecording=manager.downloadResults("testVar(testType)", ResultsFormat.RAW, addedExperiment, geppettoProject);
		rawRecording.openConnection().connect();
		Assert.assertTrue(rawRecording.getPath().endsWith("testVar(testType)/rawRecording.zip"));
	}

	/**
	 * Test method for
	 * {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#getSupportedOuputs(java.lang.String, org.geppetto.core.data.model.IExperiment, org.geppetto.core.data.model.IGeppettoProject)}.
	 * @throws GeppettoExecutionException 
	 */
	@Test
	public void testGetSupportedOuputs() throws GeppettoExecutionException
	{
		List<ModelFormat> formats=manager.getSupportedOuputs("testVar(testType)",  addedExperiment, geppettoProject);
		Assert.assertEquals(1, formats.size());
		Assert.assertEquals(ServicesRegistry.getModelFormat("TEST_FORMAT"), formats.get(0));
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#getScope()}.
	 */
	@Test
	public void testGetScope()
	{
		Assert.assertEquals(Scope.CONNECTION, manager.getScope());
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#deleteExperiment(java.lang.String, org.geppetto.core.data.model.IExperiment)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testDeleteExperiment() throws GeppettoExecutionException
	{
		manager.deleteExperiment("1", addedExperiment);
		Assert.assertEquals(1, geppettoProject.getExperiments().size());
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#checkExperimentsStatus(java.lang.String, org.geppetto.core.data.model.IGeppettoProject)}.
	 */
	@Test
	public void testCheckExperimentsStatusAgain5()
	{
		List<? extends IExperiment> status = manager.checkExperimentsStatus("1", geppettoProject);
		Assert.assertEquals(1, status.size());
		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(0).getStatus());
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#closeProject(java.lang.String, org.geppetto.core.data.model.IGeppettoProject)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testCloseProject() throws GeppettoExecutionException
	{
		manager.closeProject("1", geppettoProject);
		Assert.assertNull(runtimeProject.getActiveExperiment());
		exception.expect(NullPointerException.class);
		Assert.assertNull(runtimeProject.getRuntimeExperiment(addedExperiment).getExperimentState());
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#deleteProject(java.lang.String, org.geppetto.core.data.model.IGeppettoProject)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void testDeleteProject() throws GeppettoExecutionException
	{
		manager.deleteProject("1", geppettoProject);
	}

	@AfterClass
	public static void doYourOneTimeTeardown()
	{
		File geppettoResult = new File("./src/test/resources/test/testResults.h5");
		geppettoResult.delete();
		File rawResult = new File("./src/test/resources/test/rawRecording.zip");
		rawResult.delete();
	}

}