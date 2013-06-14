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

/**
 * Dummy Implementation of IModelInterpreter.
 * 
 * Creates Scene adding randomly positioned particles and geometries
 * 
 * @author jrmartin
 *
 */
public class DummyModelInterpreterService implements IModelInterpreter{

	private static Log logger = LogFactory.getLog(DummyModelInterpreterService.class);

	private final static int NUMBER_OF_PARTICLES = 33;
	
	public IModel readModel(URL url) throws ModelInterpreterException {
		ModelWrapper wrapper = new ModelWrapper(UUID.randomUUID().toString());
		
		Random randomGenerator = new Random();
		
		for(int i =0; i<NUMBER_OF_PARTICLES; i++){
			Particle particle = new Particle();
			
			//Create a Random position
			Point position = new Point();
			position.setX(randomGenerator.nextDouble());
			position.setY(randomGenerator.nextDouble());
			position.setZ(randomGenerator.nextDouble());
			
			particle.setPosition(position);
			
			//Add Random Particle to ModelWrapper
			wrapper.wrapModel("p(" + String.valueOf(i) +")", particle);
		}
		
		return wrapper;
	}

	public Scene getSceneFromModel(IModel model, StateTreeRoot treeRoot)
			throws ModelInterpreterException {
		
		//Returning a dummy created scene 
		Scene scene = new Scene();

		List<Entity> sceneEntities = scene.getEntities();
		
		//Random number generator for creating random number for 
	    Random randomGenerator = new Random();

		for(Entity entity : sceneEntities){
			
			//Create a Random position
			Point position = new Point();
			position.setX(randomGenerator.nextDouble());
			position.setY(randomGenerator.nextDouble());
			position.setZ(randomGenerator.nextDouble());
			
			//Create dummy Cylynder 
			Cylinder cylynder = new Cylinder();
			cylynder.setPosition(position);
			
			//Create dummy Sphere
			Sphere sphere = new Sphere();
			sphere.setRadius(randomGenerator.nextDouble());
			sphere.setPosition(position);
		
			//Add created geometries to entities
			entity.getGeometries().add(sphere);
			entity.getGeometries().add(cylynder);
		}
		
		scene.setEntities(sceneEntities);
		
		return scene;
	}

}
