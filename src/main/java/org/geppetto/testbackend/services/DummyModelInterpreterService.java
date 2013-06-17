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
public class DummyModelInterpreterService implements IModelInterpreter{

	private static Log logger = LogFactory.getLog(DummyModelInterpreterService.class);

	private final static String TEST_NUMBER = "Test Number";

	private final static String TEST_ONE = "TEST_ONE";

	private final static String TEST_TWO = "TEST_TWO";

	private final static String TEST_THREE = "TEST_THREE";

	private final static String TEST_FOUR = "TEST_FOUR";

	private final static String TEST_FIVE = "TEST_FIVE";

	private final static String TEST_SIX = "TEST_SIX";

	private Random randomGenerator;

	private static final int FIFTY_GEOMETRIES = 50;

	private static final int ONE_HUNDRED_GEOMETRIES = 100;

	private static final int ONE_THOUSANDTH_GEOMETRIES = 1000;

	public IModel readModel(URL url) throws ModelInterpreterException {

		logger.info("Reading Model using Dummy Motel Interpreter Service");

		ModelWrapper wrapper = new ModelWrapper(UUID.randomUUID().toString());

		String testName = getTestName(url.toString());

		logger.warn("Wrap Model " + testName);

		wrapper.wrapModel(TEST_NUMBER, testName);

		return wrapper;
	}

	/**
	 * Number of test to perform is included in the url value. 
	 * 
	 * @param url - Dummy URL used to fullfill format requirements for file.
	 *              Number of test included in URL at the end  as in ;
	 *              https://dummy.url/TEST_ONE
	 * @return
	 */
	private String getTestName(String url){

		if(url.contains(TEST_ONE)){
			return TEST_ONE;
		}

		else if(url.contains(TEST_TWO)){
			return TEST_TWO;
		}

		else if(url.contains(TEST_THREE)){
			return TEST_THREE;
		}

		else if(url.contains(TEST_FOUR)){
			return TEST_FOUR;
		}

		else if(url.contains(TEST_FIVE)){
			return TEST_FIVE;
		}

		else if(url.contains(TEST_SIX)){
			return TEST_SIX;
		}
		return "";
	}

	/**
	 * Return a newly created scene after adding geometries with
	 * random positions. 
	 */
	public Scene getSceneFromModel(IModel model, StateTreeRoot treeRoot)
			throws ModelInterpreterException {

		logger.info("Using DummyModelInterpreter to create Scene from IModel");

		ModelWrapper modelWrapper = (ModelWrapper)model;

		//Test Number stored inside model wrapper, retrieve it
		String testNumber = modelWrapper.getModel(TEST_NUMBER).toString();

		//Returning a dummy created scene 
		return getSceneForTest(testNumber);		
	}

	/**
	 * Creates a Scene with random geometries added. 
	 * A different scene is created for each different test
	 * 
	 * @param testNumber - Test Number to be perform
	 * @return
	 */
	private Scene getSceneForTest(String testNumber){

		Scene scene = new Scene();
		List<Entity> sceneEntities; 

		if(testNumber.equals(TEST_ONE)){		
			sceneEntities = createTestOneEntities(scene);
			scene.setEntities(sceneEntities);
		}

		else if(testNumber.equals(TEST_TWO)){
			sceneEntities = createTestTwoEntities(scene);
			scene.setEntities(sceneEntities);
		}

		return scene;
	}

	/**
	 * Add 100 particles to scene for test 1. 
	 * 
	 * @param scene
	 * @return
	 */
	private List<Entity> createTestOneEntities(Scene scene){
		List<Entity> sceneEntities = new ArrayList<Entity>();

		sceneEntities.add(0, new Entity());

		for(int i =0; i<ONE_HUNDRED_GEOMETRIES ; i++){					
			//Create a Position
			Point position = new Point();
			position.setX(0.0);
			position.setY(0.0);
			position.setZ((double) i);

			//Create particle and set position
			Particle particle = new Particle();
			particle.setPosition(position);
			particle.setId("P" + String.valueOf(i));

			//Add created geometries to entities
			sceneEntities.get(0).getGeometries().add(particle);
		}

		return sceneEntities;
	}

	/**
	 * Create test 2 Scene, which consists of 50 triangles and cylinders
	 * 
	 * @param scene
	 * @return
	 */
	private List<Entity> createTestTwoEntities(Scene scene){
		List<Entity> sceneEntities = new ArrayList<Entity>();

		for(int i =0; i<FIFTY_GEOMETRIES; i++){

			//Create a Random position
			Point position = new Point();
			position.setX(0.0);
			position.setY(0.0);
			position.setZ(((double) i) + 0.3);

			//Create a new Cylinder
			Cylinder cylynder = new Cylinder();
			cylynder.setPosition(position);
			cylynder.setId("C" + String.valueOf(i));
			cylynder.setHeight(getRandomGenerator().nextDouble());

			//Create a Random position
			Point position2 = new Point();
			position2.setX(0.0);
			position2.setY(0.0);
			position2.setZ(((double) i) + 0.5);

			//Create new sphere and set values
			Sphere sphere = new Sphere();
			sphere.setPosition(position2);
			sphere.setId("S" + String.valueOf(i));
			sphere.setRadius(3.0);

			//Entities are null in scene since it's a dummy interpreter
			//Add new entity before using it
			sceneEntities.add(i, new Entity());

			//Add created geometries to entities
			sceneEntities.get(i).getGeometries().add(cylynder);
			sceneEntities.get(i).getGeometries().add(sphere);
		}

		return sceneEntities;
	}

	private Random getRandomGenerator(){
		if(randomGenerator == null){
			randomGenerator = new Random();
		}

		return randomGenerator;
	}
}
