

package org.geppetto.testbackend.test.neuroml;

import java.util.List;

import org.geppetto.core.common.GeppettoAccessException;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.data.DataManagerHelper;
import org.geppetto.core.data.DefaultGeppettoDataManager;
import org.geppetto.core.data.model.ExperimentStatus;
import org.geppetto.core.data.model.IExperiment;
import org.geppetto.core.manager.Scope;
import org.geppetto.core.services.registry.ApplicationListenerBean;
import org.geppetto.core.simulator.ExternalSimulatorConfig;
import org.geppetto.model.neuroml.services.LEMSConversionService;
import org.geppetto.model.neuroml.services.LEMSModelInterpreterService;
import org.geppetto.model.neuroml.services.NeuroMLModelInterpreterService;
import org.geppetto.persistence.GeppettoDataManager;
import org.geppetto.persistence.db.DBManager;
import org.geppetto.persistence.db.model.GeppettoProject;
import org.geppetto.persistence.db.model.User;
import org.geppetto.persistence.util.DBTestData;
import org.geppetto.simulation.manager.GeppettoManager;
import org.geppetto.simulator.external.services.NeuronSimulatorService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class SimulationRunTest
{

	private DBManager db = new DBManager();

	private User user;

	private GeppettoDataManager dataManager;
	
	private static GeppettoManager manager = new GeppettoManager(Scope.CONNECTION);
	

	public SimulationRunTest()
	{
		db.setPersistenceManagerFactory(DBTestData.getPersistenceManagerFactory());
		dataManager = new GeppettoDataManager();
		dataManager.setDbManager(db);
	}
	
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

	@Test
	public void testExperimentRun() throws GeppettoExecutionException, GeppettoAccessException, InterruptedException
	{
//		user = db.findUserByLogin("guest1");
//		
//		// If I get the project like this experiment doesn't have a parent
//		//GeppettoProject project = user.getGeppettoProjects().get(0);
//		
//		//dataManager.getGeppettoProjectsForUser("guest1");
//		GeppettoProject project = dataManager.getGeppettoProjectById(1l);
//		
//		List<? extends IExperiment> status = manager.checkExperimentsStatus("1", project);
//		Assert.assertEquals(3, status.size());
//		Assert.assertEquals(ExperimentStatus.DESIGN, status.get(0).getStatus());
//		Assert.assertEquals(ExperimentStatus.COMPLETED, status.get(1).getStatus());
//		
//		manager.setUser(user);
//		manager.runExperiment("1", project.getExperiments().get(0));
//		
//		Thread.sleep(30000);
//		
//		project = dataManager.getGeppettoProjectById(4l);
//		manager.runExperiment("1", project.getExperiments().get(0));
//		
//		Thread.sleep(30000);
		
	}

}
