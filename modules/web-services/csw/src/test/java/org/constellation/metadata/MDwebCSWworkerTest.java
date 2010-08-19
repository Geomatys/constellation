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


package org.constellation.metadata;

import java.io.File;
import java.sql.Connection;
import java.util.logging.Level;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.jaxb.AnchoredMarshallerPool;
import org.constellation.util.Util;

import org.geotoolkit.csw.xml.CSWClassesContext;
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.internal.sql.ScriptRunner;
import org.geotoolkit.util.FileUtilities;

import org.junit.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDwebCSWworkerTest extends CSWworkerTest {

    private static File dbDirectory;
    
    @BeforeClass
    public static void setUpClass() throws Exception {

        dbDirectory    = new File("CSWWorkerTestDatabase");

        File configDir = new File("CSWWorkerTest");
        if (configDir.exists()) {
            FileUtilities.deleteDirectory(new File("CSWWorkerTest"));
        }

        if (!configDir.exists()) {
            configDir.mkdir();
            final String url = "jdbc:derby:" + dbDirectory.getPath().replace('\\','/');
            DefaultDataSource ds = new DefaultDataSource(url + ";create=true");

            Connection con = ds.getConnection();

            ScriptRunner sr = new ScriptRunner(con);
            sr.run(Util.getResourceAsStream("org/constellation/sql/structure-mdweb.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/sql/mdweb-base-data.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/sql/ISO19115-base-data.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/sql/ISO19115-data.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/sql/ISO19119-data.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/sql/ISO19108-data.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/sql/mdweb-user-data.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/sql/DC-schema.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/sql/ebrim-schema.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/metadata/sql/csw-data.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/metadata/sql/csw-data-3.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/metadata/sql/csw-data-4.sql"));

            //we write the configuration file
            File configFile = new File(configDir, "config.xml");
            BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
            Automatic configuration = new Automatic("mdweb", bdd);
            final Marshaller marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
            marshaller.marshal(configuration, configFile);
            GenericDatabaseMarshallerPool.getInstance().release(marshaller);
        }
        pool = new AnchoredMarshallerPool(CSWClassesContext.getAllClasses());
        fillPoolAnchor((AnchoredMarshallerPool) pool);

        Unmarshaller u = pool.acquireUnmarshaller();
        skeletonCapabilities = (Capabilities) u.unmarshal(Util.getResourceAsStream("org/constellation/metadata/CSWCapabilities2.0.2.xml"));
        pool.release(u);

        worker = new CSWworker("", configDir);
        worker.setSkeletonCapabilities(skeletonCapabilities);
        worker.setLogLevel(Level.FINER);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (worker != null) {
            worker.destroy();
        }
        FileUtilities.deleteDirectory(dbDirectory);
        FileUtilities.deleteDirectory(new File("CSWWorkerTest"));
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
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
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void getRecordByIdTest() throws Exception {
        super.getRecordByIdTest();
    }

    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void getRecordByIdErrorTest() throws Exception {
        super.getRecordByIdErrorTest();
    }

    /**
     * Tests the getRecords method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void getRecordsTest() throws Exception {
        super.getRecordsTest();
    }

    /**
     * Tests the getRecords method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void getRecordsErrorTest() throws Exception {
        super.getRecordsErrorTest();
    }

    /**
     * Tests the getDomain method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void getDomainTest() throws Exception {
        super.getDomainTest();
    }

    /**
     * Tests the transaction method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void transactionDeleteTest() throws Exception {
        super.transactionDeleteTest();
    }

    /**
     * Tests the transaction method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void transactionInsertTest() throws Exception {
        super.transactionInsertTest();

    }

    /**
     * Tests the describeRecord method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void DescribeRecordTest() throws Exception {
        super.DescribeRecordTest();
    }

    /**
     * Tests the transaction method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void transactionUpdateTest() throws Exception {
        super.transactionUpdateTest();

    }

}
