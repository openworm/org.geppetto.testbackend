package org.geppetto.testbackend.services;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.measure.converter.RationalConverter;
import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.utils.SetNatives;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.data.model.AVariable;
import org.geppetto.core.data.model.SimpleType;
import org.geppetto.core.data.model.SimpleType.Type;
import org.geppetto.core.data.model.SimpleVariable;
import org.geppetto.core.features.IVariableWatchFeature;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.runtime.ACompositeNode;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.CylinderNode;
import org.geppetto.core.model.runtime.ParticleNode;
import org.geppetto.core.model.runtime.SphereNode;
import org.geppetto.core.model.runtime.VariableNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.model.state.visitors.RemoveTimeStepsVisitor;
import org.geppetto.core.model.values.AValue;
import org.geppetto.core.model.values.ValuesFactory;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.core.services.IModelFormat;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.core.simulation.IRunConfiguration;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.ASimulator;
import org.geppetto.core.simulator.AVariableWatchFeature;
import org.geppetto.core.visualisation.model.Point;
import org.geppetto.testbackend.utilities.ProcessCaller;
import org.jscience.physics.amount.Amount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Dummy implementation of ISimulator.
 * 
 * @author jrmartin
 * 
 */
@Service
public class DummySimulatorService extends ASimulator
{

	private static Log _logger = LogFactory.getLog(DummySimulatorService.class);

	private static final String TEST = "TEST";

	public enum TEST_NO
	{
		TEST_ONE, TEST_TWO, TEST_THREE, TEST_FOUR, TEST_FIVE, TEST_SIX, TEST_SEVEN, TEST_EIGHT, TEST_NINE,TEST_TEN, TEST_ELEVEN
	}
	
	@Autowired
	private SimulatorConfig dummySimulatorConfig;
	
	DecimalFormat df = new DecimalFormat("0.E0");

	private Random randomGenerator;
	private double timeTracker = 0;
	private double step = 0.05;
	private String scaleFactor = null;
	
	DecimalFormat df2 = new DecimalFormat("###.##");
	  
	// TODO: all this stuff should come from configuration
	private final String _aspectID = "dummy";

	private String hdf5FileName = "H5DatasetRead.h5";

	public DummySimulatorService()
	{
		super();
	}

	public void initialize(List<IModel> model, ISimulatorCallbackListener listener) throws GeppettoInitializationException, GeppettoExecutionException
	{
		super.initialize(model, listener);
		
		VariableNode child = new VariableNode("dummyChild");
		// init statetree

		PhysicalQuantity q = new PhysicalQuantity();
		q.setValue(ValuesFactory.getDoubleValue(getRandomGenerator().nextDouble()));
				
		this.addFeature(new AVariableWatchFeature());

		// populate watch / force variables
		setWatchableVariables();
		setForceableVariables();
		
		getListener().stateTreeUpdated();		
	}

	@Override
	public String getName() {
		return this.dummySimulatorConfig.getSimulatorName();
	}

	public void simulate(IRunConfiguration runConfiguration, AspectNode aspect) throws GeppettoExecutionException
	{
		PhysicalQuantity q = new PhysicalQuantity();
		q.setValue(ValuesFactory.getDoubleValue(getRandomGenerator().nextDouble()));

		updateVisualTree(aspect);
		
		if(((IVariableWatchFeature)this.getFeature(GeppettoFeature.VARIABLE_WATCH_FEATURE)).isWatching())
		{
			// add values of variables being watched to state tree
			updateStateTreeForWatch(aspect);
		}

		getListener().stateTreeUpdated();		
	}

	private void updateVisualTree(AspectNode aspect) {
		AspectSubTreeNode vis = (AspectSubTreeNode) aspect.getSubTree(AspectTreeType.VISUALIZATION_TREE);
		
		for(ANode node : vis.getChildren()){
			if(node instanceof CompositeNode){
				for(ANode n : ((CompositeNode) node).getChildren()){
					updateNode((ParticleNode) n);
				}
			}
			else if(node instanceof ParticleNode){
				updateNode((ParticleNode) node);
			}
		}
	}
	
	private void updateNode(ParticleNode particle){
		// Create a Position
		Point position = new Point();
		position.setX(getRandomGenerator().nextDouble() * 10);
		position.setY(getRandomGenerator().nextDouble() * 10);
		position.setZ(getRandomGenerator().nextDouble() * 10);
		
		particle.setPosition(position);
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void updateStateTreeForWatch(AspectNode aspect)
	{
		ACompositeNode watchTree = aspect.getSubTree(AspectTreeType.WATCH_TREE);
		updateTimeNode();

		IVariableWatchFeature watchFeature =
				((IVariableWatchFeature)this.getFeature(GeppettoFeature.VARIABLE_WATCH_FEATURE));

		// check which watchable variables are being watched
		for(AVariable var : watchFeature.getWatcheableVariables().getVariables())
		{
			String varName = var.getName();
			// if they are being watched add to state tree
			if(varName.toLowerCase().equals(var.getName().toLowerCase()))
			{
				VariableNode dummyNode = null;

				for(ANode child : watchTree.getChildren())
				{
					if(child.getName().equals(var.getName()))
					{
						// assign if it already exists
						dummyNode = (VariableNode) child;
					}
				}

				// only add if it's not already there
				if(dummyNode == null)
				{
					dummyNode = new VariableNode(var.getName());
					watchTree.addChild(dummyNode);
				}

				PhysicalQuantity p = new PhysicalQuantity();
				AValue val = null;

				// NOTE: this is a dummy simulator so we're making values up - we wouldn't need to do this in a real one
				if(varName.toLowerCase().contains("double"))
				{
					val = ValuesFactory.getDoubleValue(getRandomGenerator().nextDouble());
				}
				else if(varName.toLowerCase().contains("float"))
				{
					val = ValuesFactory.getFloatValue(getRandomGenerator().nextFloat());
				}

				p.setUnit("mV");

				if(scaleFactor == null){
					calculateScaleFactor(val);
				}

				p.setScalingFactor(scaleFactor);

				p.setValue(val);

				updateTimeNode();
			}

		}
	}



	private void calculateScaleFactor(AValue val) {
		String unit = val.getStringValue() + " " + "mV";
		Amount<?> m2 = Amount.valueOf(unit);

		Unit<?> sUnit = m2.getUnit().getStandardUnit();

		UnitConverter r = m2.getUnit().getConverterTo(sUnit);

		long factor = 0; 
		if(r instanceof RationalConverter ){
			factor = ((RationalConverter) r).getDivisor();
		}
		
		scaleFactor = df.format(factor);;
	}

	/**
	 * Populates some dummy state variables to test list variables functionality
	 * */
	private void setWatchableVariables()
	{
		// NOTE: this could be more elegantly injected via spring
		List<AVariable> vars = new ArrayList<AVariable>();

		// dummyInt
		SimpleVariable dummyDouble = new SimpleVariable();
		SimpleType doubleType = new SimpleType();
		doubleType.setType(Type.DOUBLE);
		dummyDouble.setAspect(_aspectID);
		dummyDouble.setName("dummyDouble");
		dummyDouble.setType(doubleType);
		// dummyFloat
		SimpleVariable dummyFloat = new SimpleVariable();
		SimpleType floatType = new SimpleType();
		floatType.setType(Type.FLOAT);
		dummyFloat.setAspect(_aspectID);
		dummyFloat.setName("dummyFloat");
		dummyFloat.setType(floatType);

		vars.add(dummyDouble);
		vars.add(dummyFloat);

		((IVariableWatchFeature)this.getFeature(GeppettoFeature.VARIABLE_WATCH_FEATURE)).getWatcheableVariables().setVariables(vars);
	}

	/**
	 * Populates some dummy state variables to test list variables functionality
	 * */
	private void setForceableVariables()
	{
		// NOTE: this could be more elegantly injected via spring
		List<AVariable> vars = new ArrayList<AVariable>();

		// dummyInt
		SimpleVariable dummyInt = new SimpleVariable();
		SimpleType integerType = new SimpleType();
		integerType.setType(Type.INTEGER);
		dummyInt.setAspect(_aspectID);
		dummyInt.setName("dummyInt");
		dummyInt.setType(integerType);

		vars.add(dummyInt);

		getForceableVariables().setVariables(vars);
	}

	private Random getRandomGenerator()
	{
		if(randomGenerator == null)
		{
			randomGenerator = new Random();
		}
		return randomGenerator;
	}
	
	/**
	 * Create Time Tree
	 */
	private void updateTimeNode(){
		ACompositeNode time = new CompositeNode("dummyID");

		if(time.getChildren().size() == 0){
			PhysicalQuantity stepQ = new PhysicalQuantity();
			AValue stepVal = ValuesFactory.getDoubleValue(step);
			stepQ.setValue(stepVal);
			
			PhysicalQuantity timeQ = new PhysicalQuantity();
			AValue timeVal = ValuesFactory.getDoubleValue(timeTracker);
			timeQ.setValue(timeVal);
			
			//Add the name of the simulator to tree time node, to distinguis it from other
			//times from other simulators
			VariableNode name = new VariableNode("simulator");
			PhysicalQuantity q = new PhysicalQuantity();
			q.setValue(ValuesFactory.getStringValue(this.getName()));
			name.addPhysicalQuantity(q);
			
			VariableNode stepNode = new VariableNode("step");
			stepNode.addPhysicalQuantity(stepQ);

			VariableNode timeNode = new VariableNode("time");
			timeNode.addPhysicalQuantity(timeQ);
			
			time.addChild(stepNode);
			time.addChild(timeNode);
		}
		else{
			for(ANode child : time.getChildren()){
				if(child.getName().equals("time")){
					PhysicalQuantity q = new PhysicalQuantity();
					AValue timeVal = ValuesFactory.getDoubleValue(timeTracker);
					q.setValue(timeVal);
					((VariableNode)child).addPhysicalQuantity(q);
				}
				else if(child.getName().equals("step")){
					PhysicalQuantity q = new PhysicalQuantity();
					AValue timeVal = ValuesFactory.getDoubleValue(step);
					q.setValue(timeVal);
					((VariableNode)child).addPhysicalQuantity(q);
				}
			}
		}
		timeTracker += step;
		
		timeTracker = Double.valueOf(df2.format(timeTracker));
	}

	/**
	 * Creates a Scene with random geometries added. A different scene is created for each different test
	 * 
	 * @param testNumber
	 *            - Test Number to be perform
	 * @return
	 * @throws GeppettoExecutionException 
	 */
	private void populateEntityForTest(AspectNode aspect, TEST_NO test) throws GeppettoExecutionException
	{
		switch(test)
		{
			case TEST_ONE:
				createTestOneEntities(aspect, 100);
				break;
			case TEST_TWO:
				createTestOneEntities(aspect, 10000);
				break;
			case TEST_THREE:
				createTestOneEntities(aspect, 100000);
				break;
			case TEST_FOUR:
				createTestTwoEntities(aspect, 50);
				break;
			case TEST_FIVE:
				createTestTwoEntities(aspect, 500);
				break;
			case TEST_SIX:
				createTestTwoEntities(aspect, 20000);
				break;
			case TEST_SEVEN:
				createFile("/neuron_demos/nmodl");
				break;
			case TEST_NINE:
			try {
				writeHDF5File();
				readHDF5File();
			} catch (Exception e) {
				_logger.error("Unable to write and/or read hdf5 files " + e);
			}
				break;
		}
	}
	
	private void readHDF5File() throws Exception {
		// retrieve an instance of H5File
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

        if (fileFormat == null) {
            System.err.println("Cannot find HDF5 FileFormat.");
            return;
        }

        // open the file with read and write access
        FileFormat testFile = fileFormat.createInstance(hdf5FileName, FileFormat.WRITE);

        if (testFile == null) {
            System.err.println("Failed to open file: " + hdf5FileName);
            return;
        }

        // open the file and retrieve the file structure
        testFile.open();
        Group root = (Group) ((javax.swing.tree.DefaultMutableTreeNode) testFile.getRootNode()).getUserObject();

        // retrieve the dataset "2D 32-bit integer 20x10"
        Dataset dataset = (Dataset) root.getMemberList().get(0);
        int[] dataRead = (int[]) dataset.read();

        // print out the data values
        System.out.println("\n\nOriginal Data Values");
        for (int i = 0; i < 20; i++) {
            System.out.print("\n" + dataRead[i * 10]);
            for (int j = 1; j < 10; j++) {
                System.out.print(", " + dataRead[i * 10 + j]);
            }
        }

        // change data value and write it to file.
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                dataRead[i * 10 + j]++;
            }
        }
        dataset.write(dataRead);

        // clean and reload the data value
        int[] dataModified = (int[]) dataset.read();

        // print out the modified data values
        System.out.println("\n\nModified Data Values");
        for (int i = 0; i < 20; i++) {
            System.out.print("\n" + dataModified[i * 10]);
            for (int j = 1; j < 10; j++) {
                System.out.print(", " + dataModified[i * 10 + j]);
            }
        }

        // close file resource
        testFile.close();
	}

	/**
     * create the file and add groups and dataset into the file, which is the
     * same as javaExample.H5DatasetCreate
     * 
     * @see javaExample.HDF5DatasetCreate
     * @throws Exception
     */
    public void writeHDF5File() throws Exception {
		SetNatives.getInstance().setHDF5Native(System.getProperty("user.dir"));
		
        long[] dims2D = { 20, 10 };
        
        // retrieve an instance of H5File
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

        if (fileFormat == null) {
            System.err.println("Cannot find HDF5 FileFormat.");
            return;
        }

        // create a new file with a given file name.
        H5File testFile = (H5File) fileFormat.createFile(hdf5FileName, FileFormat.FILE_CREATE_DELETE);

        if (testFile == null) {
            System.err.println("Failed to create file:" + hdf5FileName);
            return;
        }

        // open the file and retrieve the root group
        testFile.open();
        Group root = (Group) ((javax.swing.tree.DefaultMutableTreeNode) testFile.getRootNode()).getUserObject();

        // set the data values
        int[] dataIn = new int[20 * 10];
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                dataIn[i * 10 + j] = 1000 + i * 100 + j;
            }
        }

        // create 2D 32-bit (4 bytes) integer dataset of 20 by 10
        Datatype dtype = testFile.createDatatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE);
        Dataset dataset = testFile
                .createScalarDS("2D 32-bit integer 20x10", root, dtype, dims2D, null, null, 0, dataIn);

        // close file resource
        testFile.close();
    }


	public void createFile(String path) throws GeppettoExecutionException{
		URL url = this.getClass().getClassLoader().getResource(path);
	    File f = null;
	    try {
	        f = new File(url.toURI());
	    } catch (URISyntaxException e) {
	        f = new File(url.getPath());
	    }

        try {
           _logger.info("Trying to compile mods in: " + f.getCanonicalPath());
            ProcessCaller p = new ProcessCaller(f, false);
            p.start();
            _logger.info("Done!");
        }catch (IOException e) {
			throw new GeppettoExecutionException(e);
		}
	}

	/**
	 * Add 100 particles to scene for test 1.
	 * 
	 * @param scene
	 * @return
	 */
	private void createTestOneEntities(AspectNode aspect, int numberOfParticles)
	{
		CompositeNode visualGroup = new CompositeNode("TestOne");
		
		for(int i = 0; i < numberOfParticles; i++)
		{
			// Create a Position
			Point position = new Point();
			position.setX(getRandomGenerator().nextDouble() * 10);
			position.setY(getRandomGenerator().nextDouble() * 10);
			position.setZ(getRandomGenerator().nextDouble() * 10);

			// Create particle and set position
			ParticleNode particle = new ParticleNode("particle-"+i);
			particle.setPosition(position);
			particle.setId("P" + i);

			visualGroup.addChild(particle);
		}
		aspect.getSubTree(AspectTreeType.VISUALIZATION_TREE).addChild(visualGroup);
	}

	/**
	 * Create test 2 Scene, which consists of 50 triangles and cylinders
	 * 
	 * @param scene
	 * @return
	 */
	private void createTestTwoEntities(AspectNode aspect, int numberOfGeometries)
	{

		CompositeNode visualGroup = new CompositeNode("TestTwo");
		
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
			CylinderNode cylynder = new CylinderNode("cylynder");
			cylynder.setPosition(position);
			cylynder.setDistal(position2);
			cylynder.setId("C" + i);
			cylynder.setRadiusBottom(getRandomGenerator().nextDouble() * 10);
			cylynder.setRadiusTop(getRandomGenerator().nextDouble() * 10);

			// Create new sphere and set values
			SphereNode sphere = new SphereNode("sphere");
			sphere.setPosition(position2);
			sphere.setId("S" + i);
			sphere.setRadius(getRandomGenerator().nextDouble() * 10);

			// Add new entity before using it
			

			// Add created geometries to entities
			visualGroup.addChild(cylynder);
			visualGroup.addChild(sphere);
		}
		
		aspect.getSubTree(AspectTreeType.VISUALIZATION_TREE).addChild(visualGroup);
	}

	@Override
	public String getId()
	{
		return "Dummy Simulator";
	}

	@Override
	public void registerGeppettoService()
	{
		List<IModelFormat> modelFormatList = new ArrayList<IModelFormat>();
		modelFormatList.add(ModelFormat.TEST);
		ServicesRegistry.registerSimulatorService(this, modelFormatList);
		
	}
}
