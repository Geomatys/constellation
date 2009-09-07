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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.xml.bind.Marshaller;
import org.constellation.generic.database.Automatic;
import org.constellation.util.Util;
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.xml.MarshallerPool;
import org.junit.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileSystemCSWworkerTest extends CSWworkerTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        deleteTemporaryFile();

        pool = new MarshallerPool(org.constellation.generic.database.ObjectFactory.class);
        unmarshaller = pool.acquireUnmarshaller();

        File configDir = new File("CSWWorkerTest");
        if (!configDir.exists()) {
            configDir.mkdir();

            //we write the data files
            File dataDirectory = new File(configDir, "data");
            dataDirectory.mkdir();
            writeDataFile(dataDirectory, "meta1.xml", "42292_5p_19900609195600");
            writeDataFile(dataDirectory, "meta2.xml", "42292_9s_19900610041000");
            writeDataFile(dataDirectory, "meta3.xml", "39727_22_19750113062500");
            writeDataFile(dataDirectory, "meta4.xml", "11325_158_19640418141800");
            writeDataFile(dataDirectory, "meta5.xml", "40510_145_19930221211500");

            //we write the configuration file
            File configFile = new File(configDir, "config.xml");
            Automatic configuration = new Automatic("filesystem", dataDirectory.getPath());
            final Marshaller marshaller = pool.acquireMarshaller();
            marshaller.marshal(configuration, configFile);
            pool.release(marshaller);
        }

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        deleteTemporaryFile();
        pool.release(unmarshaller);
    }

    public static void deleteTemporaryFile() {
        File configDirectory = new File("CSWWorkerTest");
        if (configDirectory.exists()) {
            File dataDirectory = new File(configDirectory, "data");
            if (dataDirectory.exists()) {
                for (File f : dataDirectory.listFiles()) {
                    f.delete();
                }
                dataDirectory.delete();
            }
            File indexDirectory = new File(configDirectory, "index");
            if (indexDirectory.exists()) {
                for (File f : indexDirectory.listFiles()) {
                    f.delete();
                }
                indexDirectory.delete();
            }
            File conf = new File(configDirectory, "config.xml");
            conf.delete();
            configDirectory.delete();
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
     * Tests the transaction method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void transactionUpdateTest() throws Exception {
        super.transactionUpdateTest();

    }

    public static void writeDataFile(File dataDirectory, String resourceName, String identifier) throws IOException {

        File dataFile = new File(dataDirectory, identifier + ".xml");
        FileWriter fw = new FileWriter(dataFile);
        InputStream in = Util.getResourceAsStream("org/constellation/metadata/" + resourceName);

        byte[] buffer = new byte[1024];
        int size;

        while ((size = in.read(buffer, 0, 1024)) > 0) {
            fw.write(new String(buffer, 0, size));
        }
        in.close();
        fw.close();
    }
}
