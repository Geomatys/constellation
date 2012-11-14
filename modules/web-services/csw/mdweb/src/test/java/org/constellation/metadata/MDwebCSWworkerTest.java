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

import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.util.Util;

import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.xml.AnchoredMarshallerPool;

import org.junit.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDwebCSWworkerTest extends CSWworkerTest {

    private static final File dbDirectory = new File("MDCSWWorkerTestDatabase");

    private static final File configDir = new File("MDCSWWorkerTest");

    @BeforeClass
    public static void setUpClass() throws Exception {

        if (configDir.exists()) {
            FileUtilities.deleteDirectory(new File("CSWWorkerTest"));
        }

        if (!configDir.exists()) {
            configDir.mkdir();
            final String url = "jdbc:derby:" + dbDirectory.getPath().replace('\\','/');
            DefaultDataSource ds = new DefaultDataSource(url + ";create=true");

            Connection con = ds.getConnection();

            DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
            sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql"));
            sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115.sql"));
            sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19119.sql"));
            sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19108.sql"));
            sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115-2.sql"));
            sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/data/defaultRecordSets.sql"));
            sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/users/creation_user.sql"));
            sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/profiles/inputLevels.sql"));
            sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/catalog_web_service.sql"));
            sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ebrimv2.5.sql"));
            sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ebrimv3.0.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-3.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-4.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-6.sql"));
            sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-7.sql"));

            //we write the configuration file
            File configFile = new File(configDir, "config.xml");
            BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
            Automatic configuration = new Automatic("mdweb", bdd);
            configuration.putParameter("transactionSecurized", "false");
            final Marshaller marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
            marshaller.marshal(configuration, configFile);
            GenericDatabaseMarshallerPool.getInstance().release(marshaller);
        }
        pool = EBRIMMarshallerPool.getInstance();
        fillPoolAnchor((AnchoredMarshallerPool) pool);

        worker = new CSWworker(configDir.getName(), configDir);
        //worker.setLogLevel(Level.FINER);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (worker != null) {
            worker.destroy();
        }
        FileUtilities.deleteDirectory(dbDirectory);
        FileUtilities.deleteDirectory(configDir);
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

    @Test
    @Override
    public void getRecords191152Test() throws Exception {
        super.getRecords191152Test();
    }

    @Test
    @Override
    public void getRecordsEbrimTest() throws Exception {
        super.getRecordsEbrimTest();
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
    public void transactionDeleteInsertTest() throws Exception {
        super.transactionDeleteInsertTest();
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
