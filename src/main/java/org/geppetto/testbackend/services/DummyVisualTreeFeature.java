package org.geppetto.testbackend.services;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.utils.SetNatives;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.features.IVisualTreeFeature;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.ANode;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.CylinderNode;
import org.geppetto.core.model.runtime.EntityNode;
import org.geppetto.core.model.runtime.ParticleNode;
import org.geppetto.core.model.runtime.SphereNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.model.state.visitors.RemoveTimeStepsVisitor;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.core.visualisation.model.Point;
import org.geppetto.testbackend.services.DummySimulatorService.TEST_NO;
import org.geppetto.testbackend.utilities.ProcessCaller;

public class DummyVisualTreeFeature implements IVisualTreeFeature{
	
	private static Log _logger = LogFactory.getLog(DummyVisualTreeFeature.class);

	private static final String TEST = "TEST";

	private String hdf5FileName = "H5DatasetRead.h5";

	private Random randomGenerator;

	private GeppettoFeature type = GeppettoFeature.VISUAL_TREE_FEATURE;

	@Override
	public GeppettoFeature getType() {
		return type ;
	}
	
	@Override
	public boolean populateVisualTree(AspectNode aspectNode) throws ModelInterpreterException {
		ModelWrapper modelWrapper = (ModelWrapper) aspectNode.getModel();

		RemoveTimeStepsVisitor removeVisitor = new RemoveTimeStepsVisitor(1);
		aspectNode.getSubTree(AspectTreeType.VISUALIZATION_TREE).apply(removeVisitor);

		try {
			populateEntityForTest(aspectNode,(TEST_NO)modelWrapper.getModel(TEST));
		} catch (GeppettoExecutionException e) {
			throw new ModelInterpreterException(e);
		}
		return true;
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
	
	private Random getRandomGenerator()
	{
		if(randomGenerator == null)
		{
			randomGenerator = new Random();
		}
		return randomGenerator;
	}
}