package org.geppetto.testbackend.services;

import java.net.URL;
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

	private final static int NUMBER_OF_PARTICLES = 33;

	private final static String TEST_NUMBER = "Test Number";
	
	private final static String TEST_ONE = "TEST_ONE";
	
	private final static String TEST_TWO = "TEST_TWO";

	private final static String TEST_THREE = "TEST_THREE";

	private final static String TEST_FOUR = "TEST_FOUR";

	private final static String TEST_FIVE = "TEST_FIVE";

	private final static String TEST_SIX = "TEST_SIX";

	private Random randomGenerator;
	
	public IModel readModel(URL url) throws ModelInterpreterException {
		
		logger.info("Reading Model using Dummy Motel Interpreter Service");
		
		ModelWrapper wrapper = new ModelWrapper(UUID.randomUUID().toString());
		
		String testName = getTestName(url.toString());
		
		wrapper.wrapModel(TEST_NUMBER, testName);
		
		return wrapper;
	}
	
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
			
		}
		return "";
	}

	public Scene getSceneFromModel(IModel model, StateTreeRoot treeRoot)
			throws ModelInterpreterException {

		logger.info("Returning Scene using Dummy Model Interpreter Service");
		String testNumber = model.getId();		

		//Returning a dummy created scene 
		return getSceneForTest(testNumber);		
	}
	
	private Scene getSceneForTest(String testNumber){
				
		Scene scene = new Scene();
		
		if(testNumber.equals(TEST_ONE)){
			List<Entity> sceneEntities = scene.getEntities();

			for(int i =0; i<100; i++){
						
				//Create a Random position
				Point position = new Point();
				position.setX(getRandomGenerator().nextDouble());
				position.setY(getRandomGenerator().nextDouble());
				position.setZ(getRandomGenerator().nextDouble());
				
				Particle particle = new Particle();
				particle.setPosition(position);
				particle.setId("P" + String.valueOf(i));
			
				//Add created geometries to entities
				sceneEntities.get(0).getGeometries().add(particle);
			}
			
			scene.setEntities(sceneEntities);
		}
		
		return scene;
	}

	private Random getRandomGenerator(){
		if(randomGenerator == null){
			randomGenerator = new Random();
		}
		
		return randomGenerator;
	}
}
