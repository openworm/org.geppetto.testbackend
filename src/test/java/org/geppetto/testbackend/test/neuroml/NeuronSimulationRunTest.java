package org.geppetto.testbackend.test.neuroml;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.PathConfiguration;
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
import org.geppetto.simulation.GeppettoManagerConfiguration;
import org.geppetto.simulation.manager.GeppettoManager;
import org.geppetto.simulator.external.services.NeuronSimulatorService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.support.GenericWebApplicationContext;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NeuronSimulationRunTest 
{	
	private static GeppettoManager manager;
	private static IGeppettoProject geppettoProject;
	
	private static Log logger = LogFactory.getLog(NeuronSimulationRunTest.class);

	/**
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings("deprecation")
	@BeforeClass
	public static void setUp() throws Exception
	{
		GenericWebApplicationContext context = new GenericWebApplicationContext();
		context.refresh();

		String neuron_home = System.getenv("NEURON_HOME");
		if (!(new File(neuron_home+"/nrniv")).exists())
		{
			neuron_home = System.getenv("NEURON_HOME")+"/bin/";
			if (!(new File(neuron_home+"/nrniv")).exists())
			{
				throw new GeppettoExecutionException("Please set the environment variable NEURON_HOME to point to your local install of NEURON 7.4");
			}
		}

		//Create configuration beans used by neuron service
		BeanDefinition neuronConfiguration = new RootBeanDefinition(SimulatorConfig.class);
		BeanDefinition neuronExternalConfig = new RootBeanDefinition(ExternalSimulatorConfig.class);

		//register config beans with spring context
		context.registerBeanDefinition("neuronExternalSimulatorConfig", neuronExternalConfig);
		context.registerBeanDefinition("neuronSimulatorConfig", neuronConfiguration);

		//retrieve config beans from context and set properties 
		((ExternalSimulatorConfig)context.getBean("neuronExternalSimulatorConfig")).setSimulatorPath(neuron_home);
		((SimulatorConfig)context.getBean("neuronSimulatorConfig")).setSimulatorID("neuronSimulator");
		((SimulatorConfig)context.getBean("neuronSimulatorConfig")).setSimulatorID("neuronSimulator");
		
		BeanDefinition neuroMLModelInterpreterBeanDefinition = new RootBeanDefinition(NeuroMLModelInterpreterService.class);
		neuroMLModelInterpreterBeanDefinition.setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE);
		BeanDefinition lemsModelInterpreterBeanDefinition = new RootBeanDefinition(LEMSModelInterpreterService.class);
		lemsModelInterpreterBeanDefinition.setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE);
		BeanDefinition conversionServiceBeanDefinition = new RootBeanDefinition(LEMSConversionService.class);
		conversionServiceBeanDefinition.setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE);
		BeanDefinition neuronSimulatorServiceBeanDefinition = new RootBeanDefinition(NeuronSimulatorService.class,2,false);
		neuronSimulatorServiceBeanDefinition.setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE);

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
		
		GeppettoManagerConfiguration geppettoManagerConfiguration = new GeppettoManagerConfiguration();
		geppettoManagerConfiguration.setAllowVolatileProjectsSimulation(true);
		manager = new GeppettoManager(Scope.CONNECTION, geppettoManagerConfiguration);
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
		
		manager.runExperiment("1", geppettoProject.getExperiments().get(0));
		
		Thread.sleep(50000);
		
		status = manager.checkExperimentsStatus("1", geppettoProject);
		//give it a little bit more time if the experiment hasn't finished running yet
		if(status.get(0).getStatus() == ExperimentStatus.RUNNING) {
			Thread.sleep(30000);
		}
		
		status = manager.checkExperimentsStatus("1", geppettoProject);
		logger.info("Simulatio done");
		Assert.assertEquals(1, status.size());
		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(0).getStatus());
	}
	
	@Test
	public void test05DeleteProjectFiles() throws GeppettoExecutionException, IOException
	{
		File projectTmPath = new File(PathConfiguration.getProjectTmpPath(Scope.RUN, geppettoProject.getId()));
		Assert.assertTrue(projectTmPath.exists());

		PathConfiguration.deleteProjectTmpFolder(Scope.RUN, geppettoProject.getId());
		
		projectTmPath = new File(PathConfiguration.getProjectTmpPath(Scope.RUN, geppettoProject.getId()));
		Assert.assertFalse(projectTmPath.exists());
	}
}
