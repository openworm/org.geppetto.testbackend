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
import org.geppetto.core.model.state.StateTreeRoot;
import org.geppetto.core.visualisation.model.Cylinder;
import org.geppetto.core.visualisation.model.Entity;
import org.geppetto.core.visualisation.model.Particle;
import org.geppetto.core.visualisation.model.Point;
import org.geppetto.core.visualisation.model.Scene;
import org.geppetto.core.visualisation.model.Sphere;
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
		return TEST_NO.valueOf(url.substring(url.lastIndexOf("/")+1));
	}

	/**
	 * Return a newly created scene after adding geometries with random positions.
	 */
	public Scene getSceneFromModel(IModel model, StateTreeRoot treeRoot) throws ModelInterpreterException
	{

		logger.info("Using DummyModelInterpreter to create Scene from IModel");

		ModelWrapper modelWrapper = (ModelWrapper) model;
		
		// Returning a dummy created scene
		return getSceneForTest((TEST_NO)modelWrapper.getModel(TEST));
	}

	/**
	 * Creates a Scene with random geometries added. A different scene is created for each different test
	 * 
	 * @param testNumber
	 *            - Test Number to be perform
	 * @return
	 */
	private Scene getSceneForTest(TEST_NO test)
	{

		Scene scene = new Scene();
		switch (test)
		{
			case TEST_ONE:
				scene.setEntities(createTestOneEntities(scene,100));
				break;
			case TEST_TWO:
				scene.setEntities(createTestOneEntities(scene,10000));
				break;
			case TEST_THREE:
				scene.setEntities(createTestOneEntities(scene,100000));
				break;
			case TEST_FOUR:
				scene.setEntities(createTestTwoEntities(scene,50));
				break;
			case TEST_FIVE:
				scene.setEntities(createTestTwoEntities(scene,500));
				break;
			case TEST_SIX:
				scene.setEntities(createTestTwoEntities(scene,20000));
				break;
		}
		return scene;
	}

	/**
	 * Add 100 particles to scene for test 1.
	 * 
	 * @param scene
	 * @return
	 */
	private List<Entity> createTestOneEntities(Scene scene, int numberOfParticles)
	{
		List<Entity> sceneEntities = new ArrayList<Entity>();
		Entity newEntity = new Entity();
		newEntity.setId("E1");
		sceneEntities.add(newEntity);

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
			newEntity.getGeometries().add(particle);
		}

		return sceneEntities;
	}

	/**
	 * Create test 2 Scene, which consists of 50 triangles and cylinders
	 * 
	 * @param scene
	 * @return
	 */
	private List<Entity> createTestTwoEntities(Scene scene, int numberOfGeometries )
	{
		List<Entity> sceneEntities = new ArrayList<Entity>();

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
			Entity newEntity = new Entity();
			newEntity.setId("E" + i);
			sceneEntities.add(newEntity);

			// Add created geometries to entities
			newEntity.getGeometries().add(cylynder);
			newEntity.getGeometries().add(sphere);
		}

		return sceneEntities;
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
