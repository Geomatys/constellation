/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.constellation.metadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import javax.xml.bind.Unmarshaller;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.generic.database.Automatic;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.TestRunner;
import org.constellation.util.Util;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.xml.AnchoredMarshallerPool;
import org.junit.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
public class FileSystemCSWworkerTest extends CSWworkerTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        deleteTemporaryFile();

        final File configDir = ConfigurationEngine.setupTestEnvironement("FSCSWWorkerTest");

        File CSWDirectory  = new File(configDir, "CSW");
        CSWDirectory.mkdir();
        final File instDirectory = new File(CSWDirectory, "default");
        instDirectory.mkdir();

        //we write the data files
        File dataDirectory = new File(instDirectory, "data");
        dataDirectory.mkdir();
        writeDataFile(dataDirectory, "meta1.xml", "42292_5p_19900609195600");
        writeDataFile(dataDirectory, "meta2.xml", "42292_9s_19900610041000");
        writeDataFile(dataDirectory, "meta3.xml", "39727_22_19750113062500");
        writeDataFile(dataDirectory, "meta4.xml", "11325_158_19640418141800");
        writeDataFile(dataDirectory, "meta5.xml", "40510_145_19930221211500");
        writeDataFile(dataDirectory, "meta-19119.xml", "mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4");
        writeDataFile(dataDirectory, "imageMetadata.xml", "gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        writeDataFile(dataDirectory, "ebrim1.xml", "000068C3-3B49-C671-89CF-10A39BB1B652");
        writeDataFile(dataDirectory, "ebrim2.xml", "urn:uuid:3e195454-42e8-11dd-8329-00e08157d076");
        writeDataFile(dataDirectory, "ebrim3.xml", "urn:motiive:csw-ebrim");
        //writeDataFile(dataDirectory, "error-meta.xml", "urn:error:file");
        writeDataFile(dataDirectory, "meta13.xml", "urn:uuid:1ef30a8b-876d-4828-9246-dcbbyyiioo");

        //we write the configuration file
        Automatic configuration = new Automatic("filesystem", dataDirectory.getPath());
        configuration.putParameter("transactionSecurized", "false");
        configuration.putParameter("shiroAccessible", "false");

        ConfigurationEngine.storeConfiguration("CSW", "default", configuration);

        pool = EBRIMMarshallerPool.getInstance();
        fillPoolAnchor((AnchoredMarshallerPool) pool);

        Unmarshaller u = pool.acquireUnmarshaller();
        pool.recycle(u);

        worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        deleteTemporaryFile();
    }

    public static void deleteTemporaryFile() {
        if (worker != null) {
            worker.destroy();
        }
        ConfigurationEngine.shutdownTestEnvironement("FSCSWWorkerTest");
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
    @Order(order=1)
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
    @Order(order=2)
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
    @Order(order=3)
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
    @Order(order=4)
    public void getRecordsTest() throws Exception {
        super.getRecordsTest();
    }

    @Test
    @Override
    @Order(order=5)
    public void getRecordsSpatialTest() throws Exception {
        super.getRecordsSpatialTest();
    }

    @Test
    @Override
    @Order(order=6)
    public void getRecords191152Test() throws Exception {
        super.getRecords191152Test();
    }


    /**
     * Tests the getRecords method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=7)
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
    @Order(order=8)
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
    @Order(order=9)
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
    @Order(order=10)
    public void transactionDeleteInsertTest() throws Exception {
        super.transactionDeleteInsertTest();
    }

    /**
     * Tests the transaction method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=11)
    public void transactionUpdateTest() throws Exception {
        typeCheckUpdate = false;
        super.transactionUpdateTest();

    }

    public static void writeDataFile(File dataDirectory, String resourceName, String identifier) throws IOException {

        final File dataFile;
        if (System.getProperty("os.name", "").startsWith("Windows")) {
            final String windowsIdentifier = identifier.replace(':', '-');
            dataFile = new File(dataDirectory, windowsIdentifier + ".xml");
        } else {
            dataFile = new File(dataDirectory, identifier + ".xml");
        }
        FileWriter fw = new FileWriter(dataFile);
        InputStream in = Util.getResourceAsStream("org/constellation/xml/metadata/" + resourceName);

        byte[] buffer = new byte[1024];
        int size;

        while ((size = in.read(buffer, 0, 1024)) > 0) {
            fw.write(new String(buffer, 0, size));
        }
        in.close();
        fw.close();
    }
}
