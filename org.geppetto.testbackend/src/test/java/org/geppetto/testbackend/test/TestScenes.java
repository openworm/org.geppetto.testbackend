package org.geppetto.testbackend.test;

import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.geppetto.core.model.IModel;
import org.geppetto.core.simulation.ITimeConfiguration;
import org.geppetto.core.simulator.ISimulator;
import org.geppetto.core.visualisation.model.Cylinder;
import org.geppetto.core.visualisation.model.Sphere;
import org.geppetto.model.sph.SPHModel;
import org.geppetto.model.sph.SPHParticle;
import org.geppetto.model.sph.services.SPHModelInterpreterService;
import org.geppetto.model.sph.x.SPHParticleX;
import org.geppetto.simulator.sph.SPHSimulatorService;
import org.junit.Test;

/**
 * Tests different scenes and features in Geppetto. 
 * 
 */
public class TestScenes{
	
	/**
	 * Test scene for streams of particles through URL file.
	 * @throws Exception
	 */
	@Test
	public void testStreamURLParticles() throws Exception{
		
		SPHModelInterpreterService modelInterpreter = new SPHModelInterpreterService();
		//User sample file with particlesS
		URL url = new URL("https://www.dropbox.com/s/9kx2p8qspdgphd4/sphModel_15.xml?dl=1");
		
		List<IModel> models = modelInterpreter.readModel(url);
		//Make sure Interpreter returned non-empty list of models
		Assert.assertTrue(!models.isEmpty());
		
		SPHModel model = (SPHModel)models.get(0);
		//Check model contains particles
		Assert.assertNotNull(model.getParticles());
		Assert.assertTrue(!(model.getParticles().isEmpty()));

		//Test Model List contains particles
		Assert.assertTrue(!(model.getParticles().isEmpty()));

		//Start simulation of model
		boolean simulate = simulateModel((IModel)model, null);

		Assert.assertTrue(simulate);
	}
	
	/**
	 * Create random particles, add them to SPHModel and simulate the model.
	 */
	@Test
	public void testStreamParticles(){
		
		//Create Random Particles
		SPHParticle particle1 = new SPHParticleX(1,1,1,5);
		SPHParticle particle2 = new SPHParticleX(2,2,2,10);
		SPHParticle particle3 = new SPHParticleX(1,2,1,8);
		SPHParticle particle4 = new SPHParticleX(3,3,3, 1);
		SPHParticle particle5 = new SPHParticleX(5,5,5,9);

		//Create Model Object and add random particles
		SPHModel model = new SPHModel();
		model.getParticles().add(particle1);
		model.getParticles().add(particle2);
		model.getParticles().add(particle3);
		model.getParticles().add(particle4);
		model.getParticles().add(particle5);

		//Test Model List contains particles
		Assert.assertTrue(!(model.getParticles().isEmpty()));
		
		//Start simulation of model
		boolean simulate = simulateModel((IModel)model, null);
		
		Assert.assertTrue(simulate);
	}
	
	/**
	 * Create random spheres and triangles to stream. 
	 * 
	 */
	@Test
	public void testGeometries(){
	
		Sphere sphere = new Sphere();
		Cylinder cylynder = new Cylinder();
		
		simulateModel((IModel)sphere, null);
		
		simulateModel((IModel)cylynder, null);
	}
	
	/**
	 * Takes a Model object and simulates it. 
	 * 
	 * @param model
	 * @param timeConfiguration
	 */
	public boolean simulateModel(IModel model, ITimeConfiguration timeConfiguration){
		
		ISimulator simulator = new SPHSimulatorService();
		simulator.startSimulatorCycle();
		simulator.simulate(model, timeConfiguration);
		simulator.endSimulatorCycle();
		
		return true;
	}
}