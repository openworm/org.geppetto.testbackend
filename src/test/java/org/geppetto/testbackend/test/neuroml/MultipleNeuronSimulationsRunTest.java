package org.geppetto.testbackend.test.neuroml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.common.GeppettoAccessException;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.data.DataManagerHelper;
import org.geppetto.core.data.DefaultGeppettoDataManager;
import org.geppetto.core.data.model.ExperimentStatus;
import org.geppetto.core.data.model.IExperiment;
import org.geppetto.core.data.model.IGeppettoProject;
import org.geppetto.core.data.model.IUserGroup;
import org.geppetto.core.data.model.UserPrivileges;
import org.geppetto.core.manager.Scope;
import org.geppetto.core.services.registry.ApplicationListenerBean;
import org.geppetto.core.simulator.ExternalSimulatorConfig;
import org.geppetto.model.neuroml.services.LEMSConversionService;
import org.geppetto.model.neuroml.services.LEMSModelInterpreterService;
import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.geppetto.simulation.manager.GeppettoManager;
import org.geppetto.simulator.external.services.NeuronSimulatorService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.support.GenericWebApplicationContext;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultipleNeuronSimulationsRunTest 
{	
	private static GeppettoManager manager = new GeppettoManager(Scope.CONNECTION);
	private static IGeppettoProject geppettoProject;
	
	private static Log logger = LogFactory.getLog(MultipleNeuronSimulationsRunTest.class);

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

		ExternalSimulatorConfig externalConfig = new ExternalSimulatorConfig();
		externalConfig.setSimulatorPath(System.getenv("NEURON_HOME"));
		
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
		
		((NeuronSimulatorService)retrievedContext.getBean("scopedTarget.neuronSimulator")).setNeuronExternalSimulatorConfig(externalConfig);
		
		DataManagerHelper.setDataManager(new DefaultGeppettoDataManager());
	}
	
	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#setUser(org.geppetto.core.data.model.IUser)}.
	 * 
	 * @throws GeppettoExecutionException
	 */
	@Test
	public void test01SetUser() throws GeppettoExecutionException
	{
		long value = 1000l * 1000 * 1000;
		List<UserPrivileges> privileges = new ArrayList<UserPrivileges>();
		privileges.add(UserPrivileges.RUN_EXPERIMENT);
		privileges.add(UserPrivileges.READ_PROJECT);
		IUserGroup userGroup = DataManagerHelper.getDataManager().newUserGroup("unaccountableAristocrats", privileges, value, value * 2);
		manager.setUser(DataManagerHelper.getDataManager().newUser("neurontestuser", "passauord", true, userGroup));
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#getUser()}.
	 */
	@Test
	public void test02GetUser()
	{
		Assert.assertEquals("neurontestuser", manager.getUser().getName());
		Assert.assertEquals("passauord", manager.getUser().getPassword());
	}
	
	@Test
	public void test03LoadProject() throws IOException, GeppettoInitializationException, GeppettoExecutionException, GeppettoAccessException
	{
		InputStreamReader inputStreamReader = new InputStreamReader(GeppettoManagerNeuroMLTest.class.getResourceAsStream("/multipleSimulationNeuronTest/GEPPETTO.json"));
		geppettoProject = DataManagerHelper.getDataManager().getProjectFromJson(TestUtilities.getGson(), inputStreamReader, null);
		manager.loadProject("1", geppettoProject);

	}

	@Test
	public void test04ExperimentNeuronRun() throws GeppettoExecutionException, GeppettoAccessException, InterruptedException
	{			
		List<? extends IExperiment> status = manager.checkExperimentsStatus("1", geppettoProject);
		Assert.assertEquals(5, status.size());
		Assert.assertEquals(ExperimentStatus.DESIGN, status.get(0).getStatus());  //test design status on experiment
		Assert.assertEquals(ExperimentStatus.DESIGN, status.get(1).getStatus());  //test design status on experiment
		Assert.assertEquals(ExperimentStatus.DESIGN, status.get(2).getStatus());  //test design status on experiment
		Assert.assertEquals(ExperimentStatus.DESIGN, status.get(3).getStatus());  //test design status on experiment
		Assert.assertEquals(ExperimentStatus.DESIGN, status.get(4).getStatus());  //test design status on experiment
		
		Assert.assertEquals(0, status.get(0).getSimulationResults().size());  //test empty experiment results list pre-running
		Assert.assertEquals(0, status.get(1).getSimulationResults().size());  //test empty experiment results list pre-running
		Assert.assertEquals(0, status.get(2).getSimulationResults().size());  //test empty experiment results list pre-running
		Assert.assertEquals(0, status.get(3).getSimulationResults().size());  //test empty experiment results list pre-running
		Assert.assertEquals(0, status.get(4).getSimulationResults().size());  //test empty experiment results list pre-running
		
		manager.runExperiment("1",geppettoProject.getExperiments().get(0));
		manager.runExperiment("1",geppettoProject.getExperiments().get(1));
		manager.runExperiment("1",geppettoProject.getExperiments().get(2));
		manager.runExperiment("1",geppettoProject.getExperiments().get(3));
		manager.runExperiment("1",geppettoProject.getExperiments().get(4));
		
		Thread.sleep(150000);
		
		status = manager.checkExperimentsStatus("5", geppettoProject);
		if(status.get(0).getStatus() != ExperimentStatus.COMPLETED) {
			Thread.sleep(30000);
		}
		status = manager.checkExperimentsStatus("1", geppettoProject);
		Assert.assertEquals(5, status.size());  
		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(0).getStatus());  //test completion of experiment run
		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(1).getStatus());  //test completion of experiment run
		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(2).getStatus());  //test completion of experiment run
		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(3).getStatus());  //test completion of experiment run
		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(4).getStatus());  //test completion of experiment run

		Assert.assertEquals(2, status.get(0).getSimulationResults().size());  //test experiment simulation list results
		Assert.assertEquals(2, status.get(1).getSimulationResults().size());  //test experiment simulation list results
		Assert.assertEquals(2, status.get(2).getSimulationResults().size());  //test experiment simulation list results
		Assert.assertEquals(2, status.get(3).getSimulationResults().size());  //test experiment simulation list results
		Assert.assertEquals(2, status.get(4).getSimulationResults().size());  //test experiment simulation list results
	}
}
