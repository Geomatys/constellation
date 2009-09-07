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
import java.util.Arrays;
import javax.xml.bind.Marshaller;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.Util;
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.xml.MarshallerPool;
import org.junit.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDwebCSWworkerTest extends CSWworkerTest {

    private static File directory;
    
    @BeforeClass
    public static void setUpClass() throws Exception {

        pool = new MarshallerPool(org.constellation.generic.database.ObjectFactory.class);
        unmarshaller = pool.acquireUnmarshaller();

        File configDir = new File("CSWWorkerTest");
        if (!configDir.exists()) {
            configDir.mkdir();

            directory = new File(System.getProperty("java.io.tmpdir", "/tmp"), "MDwebCSWworkerTest").getAbsoluteFile();
            String url = "jdbc:derby:" + directory.getPath().replace('\\','/');
            DefaultDataSource ds = new DefaultDataSource(url + ";create=true");

            Connection con = ds.getConnection();

            Util.executeSQLScript("org/constellation/metadata/sql/structure-mdweb.sql", con);
            Util.executeSQLScript("org/constellation/metadata/sql/ISO19115-data.sql", con);
            Util.executeSQLScript("org/constellation/metadata/sql/csw-data.sql", con);

            //we write the configuration file
            File configFile = new File(configDir, "config.xml");
            BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
            Automatic configuration = new Automatic("mdweb", bdd);
            final Marshaller marshaller = pool.acquireMarshaller();
            marshaller.marshal(configuration, configFile);
            pool.release(marshaller);
        }
        pool.release(unmarshaller);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        deleteDirectory(directory);
        deleteDirectory(new File("CSWWorkerTest"));
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

        pool = new AnchorPool(Arrays.asList(CSWClassesContext.getAllClasses()));
        unmarshaller = pool.acquireUnmarshaller();


        File configDir = new File("CSWWorkerTest");
        worker = new CSWworker("", pool, configDir);
        Capabilities stcapa = (Capabilities) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/CSWCapabilities2.0.2.xml"));
        worker.setSkeletonCapabilities(stcapa);

    }

    @After
    public void tearDown() throws Exception {
        if (unmarshaller != null) {
            pool.release(unmarshaller);
        }
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
