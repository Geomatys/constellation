/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
import java.sql.Connection;
import java.util.logging.Level;
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
import org.geotoolkit.resources.NIOUtilities;
import org.geotoolkit.sos.xml.v100.Capabilities;
import org.geotoolkit.xml.MarshallerPool;

import org.junit.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class PostgridSOSWorkerTest extends SOSWorkerTest {

    private static DefaultDataSource ds2 = null;

    private static DefaultDataSource ds = null;
    
    @BeforeClass
    public static void setUpClass() throws Exception {

        final String url2 = "jdbc:derby:memory:Test2;create=true";
        ds2 = new DefaultDataSource(url2);

        Connection con2 = ds2.getConnection();

        Util.executeSQLScript("org/constellation/sql/structure-mdweb.sql", con2);
        Util.executeSQLScript("org/constellation/sql/mdweb-base-data.sql", con2);
        Util.executeSQLScript("org/constellation/sql/ISO19115-base-data.sql", con2);
        Util.executeSQLScript("org/constellation/sql/mdweb-user-data.sql", con2);
        Util.executeSQLScript("org/constellation/sql/sml-schema_v2.sql", con2);
        Util.executeSQLScript("org/constellation/sql/sml-data_v2.sql", con2);

        final String url = "jdbc:derby:memory:Test1;create=true";
        ds = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        Util.executeSQLScript("org/constellation/sql/structure-observations.sql", con);
        Util.executeSQLScript("org/constellation/sql/sos-data.sql", con);
       

        MarshallerPool pool   = new MarshallerPool(org.constellation.configuration.ObjectFactory.class);
        Marshaller marshaller =  pool.acquireMarshaller();

        
        

        File configDir = new File("SOSWorkerTest");
        if (configDir.exists()) {
            NIOUtilities.deleteDirectory(configDir);
        }

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
            configuration.setSensorIdBase("urn:ogc:object:sensor:GEOM:");
            marshaller.marshal(configuration, configFile);

        }
        pool.release(marshaller);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {

        NIOUtilities.deleteDirectory(new File("SOSWorkerTest"));
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
        File mappingFile = new File("mapping.properties");
        if (mappingFile.exists()) {
            mappingFile.delete();
        }
        if (ds != null) {
            ds.shutdown();
        }
        if (ds2 != null) {
            ds2.shutdown();
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
        worker.setLogLevel(Level.FINER);
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
    public void getCapabilitiesErrorTest() throws Exception {
        super.getCapabilitiesErrorTest();

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
    public void DescribeSensorErrorTest() throws Exception {
       super.DescribeSensorErrorTest();
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
    public void GetObservationErrorTest() throws Exception {
        super.GetObservationErrorTest();
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
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void GetObservationSamplingCurveTest() throws Exception {
        super.GetObservationSamplingCurveTest();
    }

    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void GetResultErrorTest() throws Exception {
        super.GetResultErrorTest();
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
     * Tests the RegisterSensor method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void insertObservationTest() throws Exception {
        super.insertObservationTest();
    }

    /**
     * Tests the RegisterSensor method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void RegisterSensorErrorTest() throws Exception {
        super.RegisterSensorTest();
    }

    /**
     * Tests the RegisterSensor method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void RegisterSensorTest() throws Exception {
        super.RegisterSensorTest();
    }

    /**
     * Tests the GetFeatureOfInterest method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void GetFeatureOfInterestErrorTest() throws Exception {
        super.GetFeatureOfInterestErrorTest();
    }

    /**
     * Tests the GetFeatureOfInterest method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void GetFeatureOfInterestTest() throws Exception {
        super.GetFeatureOfInterestTest();
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
