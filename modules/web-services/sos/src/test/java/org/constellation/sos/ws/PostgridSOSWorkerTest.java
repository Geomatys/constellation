/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.sos.ws;

// JUnit dependencies
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.ObservationFilterType;
import org.constellation.configuration.ObservationReaderType;
import org.constellation.configuration.ObservationWriterType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.sos.xml.v100.Capabilities;
import org.geotoolkit.xml.MarshallerPool;

import org.junit.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class PostgridSOSWorkerTest extends SOSWorkerTest {

    private static File directory;

    private static File directory2;
    
    @BeforeClass
    public static void setUpClass() throws Exception {

        directory2 = new File(System.getProperty("java.io.tmpdir", "/tmp"), "MDPostgridSOSWorkerTest").getAbsoluteFile();
        String url2 = "jdbc:derby:" + directory2.getPath().replace('\\','/');
        DefaultDataSource ds2 = new DefaultDataSource(url2 + ";create=true");

        Connection con2 = ds2.getConnection();

        Util.executeSQLScript("org/constellation/sos/sql/structure-mdweb.sql", con2);
        Util.executeSQLScript("org/constellation/sos/sql/sml-schema.sql", con2);
        Util.executeSQLScript("org/constellation/sos/sql/sml-data.sql", con2);

        directory = new File(System.getProperty("java.io.tmpdir", "/tmp"), "PostgridSOSWorkerTest").getAbsoluteFile();
        String url = "jdbc:derby:" + directory.getPath().replace('\\','/');
        DefaultDataSource ds = new DefaultDataSource(url + ";create=true");

        Connection con = ds.getConnection();

        Util.executeSQLScript("org/constellation/sos/sql/structure-observations.sql", con);
        Util.executeSQLScript("org/constellation/sos/sql/sos-data.sql", con);
       

        MarshallerPool pool   = new MarshallerPool(org.constellation.configuration.ObjectFactory.class);
        Marshaller marshaller =  pool.acquireMarshaller();

        
        

        File configDir = new File("SOSWorkerTest");
        if (!configDir.exists()) {
            configDir.mkdir();

            //we write the configuration file
            File configFile = new File(configDir, "config.xml");
            Automatic SMLConfiguration = new Automatic();
            BDD smBdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url2, "", "");
            SMLConfiguration.setBdd(smBdd);
            SMLConfiguration.setFormat("mdweb");
            
            Automatic OMConfiguration  = new Automatic();
            BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
            OMConfiguration.setBdd(bdd);
            SOSConfiguration configuration = new SOSConfiguration(SMLConfiguration, OMConfiguration);
            configuration.setObservationReaderType(ObservationReaderType.DEFAULT);
            configuration.setObservationWriterType(ObservationWriterType.DEFAULT);
            configuration.setSMLType(DataSourceType.MDWEB);
            configuration.setObservationFilterType(ObservationFilterType.DEFAULT);
            configuration.setPhenomenonIdBase("urn:ogc:def:phenomenon:GEOM:");
            configuration.setProfile("transactional");
            configuration.setObservationTemplateIdBase("urn:ogc:object:observation:template:GEOM:");
            configuration.setObservationIdBase("urn:ogc:object:observation:GEOM:");
            marshaller.marshal(configuration, configFile);

        }
        pool.release(marshaller);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        deleteDirectory(directory);
        deleteDirectory(directory2);
        deleteDirectory(new File("SOSWorkerTest"));
    }

    public static void deleteDirectory(File dir) {
         if (dir.exists()) {
            if (dir.isDirectory()) {
                for (File f : dir.listFiles()) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    } else {
                        f.delete();
                    }
                }
                dir.delete();
            } else {
                dir.delete();
            }
        }
    }

    @Before
    public void setUp() throws Exception {

        marshallerPool = new MarshallerPool("org.geotoolkit.sos.xml.v100:org.geotoolkit.observation.xml.v100:org.geotoolkit.sml.xml.v100:org.geotoolkit.sampling.xml.v100:org.geotoolkit.swe.xml.v101");

        File configDir = new File("SOSWorkerTest");
        worker = new SOSworker(configDir);
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        Capabilities stcapa = (Capabilities) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/SOSCapabilities1.0.0.xml"));
        worker.setSkeletonCapabilities(stcapa);
        worker.setServiceURL(URL);
        marshallerPool.release(unmarshaller);
    }

    @After
    public void tearDown() throws Exception {
        if (worker != null) {
            worker.destroy();
        }
    }


    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void getCapabilitiesTest() throws Exception {
        super.getCapabilitiesTest();

    }



    /**
     * Tests the DescribeSensor method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void DescribeSensorTest() throws Exception {
       super.DescribeSensorTest();
    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void GetObservationTest() throws Exception {
        super.GetObservationTest();
    }

    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void GetResultTest() throws Exception {
        super.GetResultTest();
    }

    /**
     * Tests the destroy method
     *
     * @throws java.lang.Exception
     */
    @Ignore
    @Override
    public void destroyTest() throws Exception {
        super.destroyTest();
    }
}
