package org.geppetto.testbackend.test.neuroml;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.extensions.cpsuite.ClasspathSuite.ClassnameFilters;
import org.junit.runner.RunWith;

@RunWith(ClasspathSuite.class)
@ClassnameFilters({"!.*AllTests","!.*S3ManagerTest","!.*HDF5ReaderTest"})
public class AllGeppettoTests
{

}
