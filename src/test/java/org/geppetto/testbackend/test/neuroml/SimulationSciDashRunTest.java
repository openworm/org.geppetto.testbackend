

package org.geppetto.testbackend.test.neuroml;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.geppetto.core.beans.SimulatorConfig;
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
import org.geppetto.simulator.scidash.services.ScidashSimulatorService;
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
public class SimulationSciDashRunTest
{	
	private static GeppettoManager manager = new GeppettoManager(Scope.CONNECTION);
	private static IGeppettoProject geppettoProject;
	private static ScidashSimulatorService simulator;

	public SimulationSciDashRunTest()
	{
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings("deprecation")
	@BeforeClass
	public static void setUp() throws Exception
	{
		GenericWebApplicationContext context = new GenericWebApplicationContext();
		BeanDefinition scidashSimulatorServiceBeanDefinition = new RootBeanDefinition(ScidashSimulatorService.class);
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
		
		context.registerBeanDefinition("scidashSimulator", scidashSimulatorServiceBeanDefinition);
		context.registerBeanDefinition("scopedTarget.scidashSimulator", scidashSimulatorServiceBeanDefinition);
		
		ContextRefreshedEvent event = new ContextRefreshedEvent(context);
		ApplicationListenerBean listener = new ApplicationListenerBean();
		listener.onApplicationEvent(event);
		ApplicationContext retrievedContext = ApplicationListenerBean.getApplicationContext("scidashSimulator");
		Assert.assertNotNull(retrievedContext.getBean("scopedTarget.scidashSimulator"));
		Assert.assertTrue(retrievedContext.getBean("scopedTarget.scidashSimulator") instanceof ScidashSimulatorService);
		
		String neuron_home = System.getenv("NEURON_HOME");
		if (!(new File(neuron_home+"/nrniv")).exists())
		{
		    neuron_home = System.getenv("NEURON_HOME")+"/bin/";
		    if (!(new File(neuron_home+"/nrniv")).exists())
		    {
		        throw new GeppettoExecutionException("Please set the environment variable NEURON_HOME to point to your local install of NEURON 7.4");
		    }
		}
		ExternalSimulatorConfig externalConfig = new ExternalSimulatorConfig();
		externalConfig.setSimulatorPath(neuron_home);
		Assert.assertNotNull(externalConfig.getSimulatorPath());
		SimulatorConfig simulatorConfig = new SimulatorConfig();
		simulatorConfig.setSimulatorID("neuronSimulator");
		simulatorConfig.setSimulatorName("neuronSimulator");
		
		((NeuronSimulatorService)retrievedContext.getBean("scopedTarget.neuronSimulator")).setNeuronExternalSimulatorConfig(externalConfig);
		((NeuronSimulatorService)retrievedContext.getBean("scopedTarget.neuronSimulator")).setNeuronSimulatorConfig(simulatorConfig);
				
		DataManagerHelper.setDataManager(new DefaultGeppettoDataManager());
		
		simulator = new ScidashSimulatorService(manager);
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
		manager.setUser(DataManagerHelper.getDataManager().newUser("scidashtestuser", "passauord", true, userGroup));
	}

	/**
	 * Test method for {@link org.geppetto.simulation.manager.frontend.controllers.GeppettoManager#getUser()}.
	 */
	@Test
	public void test02GetUser()
	{
		Assert.assertEquals("scidashtestuser", manager.getUser().getName());
		Assert.assertEquals("passauord", manager.getUser().getPassword());
	}
	
	@Test
	public void test03LoadProject() throws IOException, GeppettoInitializationException, GeppettoExecutionException, GeppettoAccessException
	{
		InputStreamReader inputStreamReader = new InputStreamReader(GeppettoManagerNeuroMLTest.class.getResourceAsStream("/simulationNeuronTest/GEPPETTO.json"));
		geppettoProject = DataManagerHelper.getDataManager().getProjectFromJson(TestUtilities.getGson(), inputStreamReader, null);
		manager.loadProject("1", geppettoProject);

	}

	@Test
	public void test04ExperimentNeuronRun() throws GeppettoExecutionException, GeppettoAccessException, InterruptedException
	{			
		List<? extends IExperiment> status = manager.checkExperimentsStatus("1", geppettoProject);
		Assert.assertEquals(1, status.size());
		Assert.assertEquals(ExperimentStatus.DESIGN, status.get(0).getStatus());
		
		simulator.runExperiment(geppettoProject.getExperiments().get(0).getId(), geppettoProject.getId(), 233333);
		//manager.runExperiment("1", geppettoProject.getExperiments().get(0));
		
		Thread.sleep(90000);		
	}
}
