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
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.filter.Query;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.internal.sql.ScriptRunner;
import org.geotoolkit.resources.NIOUtilities;
import org.geotoolkit.xml.MarshallerPool;

import org.junit.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class GenericPostgridSOSWorkerTest extends SOSWorkerTest {

    private static DefaultDataSource ds = null;

    @BeforeClass
    public static void setUpClass() throws Exception {

        final String url = "jdbc:derby:memory:GPGTest1;create=true";
        ds = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        ScriptRunner sr = new ScriptRunner(con);
        sr.run(Util.getResourceAsStream("org/constellation/sql/structure-observations.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data.sql"));


        MarshallerPool pool   = new MarshallerPool(org.constellation.configuration.ObjectFactory.class, org.constellation.generic.filter.ObjectFactory.class);
        Marshaller marshaller =  pool.acquireMarshaller();




        File configDir = new File("GPGSOSWorkerTest");
        if (configDir.exists()) {
            NIOUtilities.deleteDirectory(configDir);
        }

        if (!configDir.exists()) {
            configDir.mkdir();

            Unmarshaller unmarshaller = pool.acquireUnmarshaller();

            //we write the configuration file
            File filterFile = new File(configDir, "affinage.xml");
            Query query = (Query) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/affinage.xml"));
            marshaller.marshal(query, filterFile);

            //we write the configuration file
            File configFile = new File(configDir, "config.xml");
            Automatic SMLConfiguration = new Automatic();
            SMLConfiguration.setFormat("nosml");

            Automatic OMConfiguration = (Automatic) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/generic-config.xml"));
            pool.release(unmarshaller);

            OMConfiguration.getBdd().setConnectURL(url);


            SOSConfiguration configuration = new SOSConfiguration(SMLConfiguration, OMConfiguration);
            configuration.setObservationReaderType(ObservationReaderType.GENERIC);
            configuration.setSMLType(DataSourceType.NONE);
            configuration.setObservationFilterType(ObservationFilterType.GENERIC);
            configuration.setPhenomenonIdBase("urn:ogc:def:phenomenon:GEOM:");
            configuration.setProfile("discovery");
            configuration.setObservationTemplateIdBase("urn:ogc:object:observation:template:GEOM:");
            configuration.setObservationIdBase("urn:ogc:object:observation:GEOM:");
            configuration.setSensorIdBase("urn:ogc:object:sensor:GEOM:");
            marshaller.marshal(configuration, configFile);

        }
        pool.release(marshaller);
        init();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {

        NIOUtilities.deleteDirectory(new File("GPGSOSWorkerTest"));
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
    }



    @Before
    public void setUp() throws Exception {
        File configDir = new File("GPGSOSWorkerTest");
        worker = new SOSworker(configDir);
        worker.setSkeletonCapabilities(capabilities);
        worker.setServiceURL(URL);
        worker.setLogLevel(Level.FINER);
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
    @Ignore
    @Override
    public void GetObservationTest() throws Exception {
        super.GetObservationTest();
    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Ignore
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
    @Ignore
    @Override
    public void GetResultTest() throws Exception {
        super.GetResultTest();
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
    @Ignore
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
