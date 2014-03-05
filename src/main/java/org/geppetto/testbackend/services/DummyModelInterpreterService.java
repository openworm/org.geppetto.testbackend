package org.geppetto.testbackend.services;

import java.net.URL;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.IModelInterpreter;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.simulation.Aspect;
import org.geppetto.core.model.state.StateTreeRoot;
import org.geppetto.core.model.state.StateTreeRoot.SUBTREE;
import org.geppetto.core.model.state.visitors.RemoveTimeStepsVisitor;
import org.geppetto.core.visualisation.model.CAspect;
import org.geppetto.core.visualisation.model.CEntity;
import org.geppetto.core.visualisation.model.Cylinder;
import org.geppetto.core.visualisation.model.Particle;
import org.geppetto.core.visualisation.model.Point;
import org.geppetto.core.visualisation.model.Sphere;
import org.geppetto.core.visualisation.model.VisualModel;
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

	private enum TEST_NO
	{
		TEST_ONE, TEST_TWO, TEST_THREE, TEST_FOUR, TEST_FIVE, TEST_SIX
	}

	private Random randomGenerator;

	public IModel readModel(URL url) throws ModelInterpreterException
	{

		logger.info("Reading Model using Dummy Motel Interpreter Service");

		ModelWrapper wrapper = new ModelWrapper(UUID.randomUUID().toString());

		TEST_NO test = getTestName(url.toString());

		logger.warn("Wrap Model " + test);

		wrapper.wrapModel(TEST, test);

		return wrapper;
	}

	/**
	 * Number of test to perform is included in the url value.
	 * 
	 * @param url
	 *            - Dummy URL used to fullfill format requirements for file. Number of test included in URL at the end as in ; https://dummy.url/TEST_ONE
	 * @return
	 */
	private TEST_NO getTestName(String url)
	{
		return TEST_NO.valueOf(url.substring(url.lastIndexOf("/") + 1));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.IModelInterpreter#getVisualEntity(org.geppetto.core.model.IModel, org.geppetto.core.model.simulation.Aspect, org.geppetto.core.model.state.StateTreeRoot)
	 */
	@Override
	public CEntity getVisualEntity(IModel model, Aspect aspect, StateTreeRoot stateTree) throws ModelInterpreterException
	{
		logger.info("Using DummyModelInterpreter to create Scene from IModel");

		ModelWrapper modelWrapper = (ModelWrapper) model;

		RemoveTimeStepsVisitor removeVisitor = new RemoveTimeStepsVisitor(1);
		stateTree.getSubTree(SUBTREE.MODEL_TREE).apply(removeVisitor);

		// Returning a dummy created scene
		CEntity centity = new CEntity();
		CAspect caspect = new CAspect();
		aspect.setId(aspect.getId());
		centity.getAspects().add(caspect);
		return populateEntityForTest(centity, (TEST_NO) modelWrapper.getModel(TEST));
	}

	/**
	 * Creates a Scene with random geometries added. A different scene is created for each different test
	 * 
	 * @param testNumber
	 *            - Test Number to be perform
	 * @return
	 */
	private CEntity populateEntityForTest(CEntity entity, TEST_NO test)
	{
		switch(test)
		{
			case TEST_ONE:
				createTestOneEntities(entity, 100);
				break;
			case TEST_TWO:
				createTestOneEntities(entity, 10000);
				break;
			case TEST_THREE:
				createTestOneEntities(entity, 100000);
				break;
			case TEST_FOUR:
				createTestTwoEntities(entity, 50);
				break;
			case TEST_FIVE:
				createTestTwoEntities(entity, 500);
				break;
			case TEST_SIX:
				createTestTwoEntities(entity, 20000);
				break;
		}
		return entity;
	}

	/**
	 * Add 100 particles to scene for test 1.
	 * 
	 * @param scene
	 * @return
	 */
	private void createTestOneEntities(CEntity entity, int numberOfParticles)
	{
		entity.setId("E1");
		
		VisualModel visualModel=new VisualModel();
		visualModel.setId("V1");
		entity.getAspects().get(0).getVisualModel().add(visualModel);
		
		for(int i = 0; i < numberOfParticles; i++)
		{
			// Create a Position
			Point position = new Point();
			position.setX(getRandomGenerator().nextDouble() * 10);
			position.setY(getRandomGenerator().nextDouble() * 10);
			position.setZ(getRandomGenerator().nextDouble() * 10);

			// Create particle and set position
			Particle particle = new Particle();
			particle.setPosition(position);
			particle.setId("P" + i);

			// Add created geometries to entities
			visualModel.getObjects().add(particle);
		}
	}

	/**
	 * Create test 2 Scene, which consists of 50 triangles and cylinders
	 * 
	 * @param scene
	 * @return
	 */
	private void createTestTwoEntities(CEntity newEntity, int numberOfGeometries)
	{

		newEntity.setId("E" + numberOfGeometries);
		VisualModel visualModel=new VisualModel();
		visualModel.setId("V1");
		newEntity.getAspects().get(0).getVisualModel().add(visualModel);
		
		for(int i = 0; i < numberOfGeometries; i++)
		{

			// Create a Random position
			Point position = new Point();
			position.setX(0.0);
			position.setY(0.0);
			position.setZ(((double) i) + 0.3);

			// Create a Random position
			Point position2 = new Point();
			position2.setX(0.0);
			position2.setY(0.0);
			position2.setZ(getRandomGenerator().nextDouble() * 100);

			// Create a new Cylinder
			Cylinder cylynder = new Cylinder();
			cylynder.setPosition(position);
			cylynder.setDistal(position2);
			cylynder.setId("C" + i);
			cylynder.setRadiusBottom(getRandomGenerator().nextDouble() * 10);
			cylynder.setRadiusTop(getRandomGenerator().nextDouble() * 10);

			// Create new sphere and set values
			Sphere sphere = new Sphere();
			sphere.setPosition(position2);
			sphere.setId("S" + i);
			sphere.setRadius(getRandomGenerator().nextDouble() * 10);

			// Add new entity before using it
			

			// Add created geometries to entities
			visualModel.getObjects().add(cylynder);
			visualModel.getObjects().add(sphere);
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
