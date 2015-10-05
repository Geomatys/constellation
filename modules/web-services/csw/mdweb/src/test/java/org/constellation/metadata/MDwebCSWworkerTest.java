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

import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.ServiceBusiness;
import org.constellation.admin.SpringHelper;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.skos.xml.Concept;
import org.geotoolkit.skos.xml.Value;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.junit.runner.RunWith;
import org.mdweb.io.sql.ThesaurusDatabaseWriter;
import org.mdweb.model.thesaurus.ISOLanguageCode;
import org.mdweb.sql.ThesaurusDatabaseCreator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import org.apache.sis.util.logging.Logging;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
public class MDwebCSWworkerTest extends CSWworkerTest {

    @Inject
    private ServiceBusiness serviceBusiness;

    private static final File dbDirectory = new File("MDCSWWorkerTestDatabase");
    private static final File dbTHDirectory = new File("MDCSWWorkerTestThesaurusDatabase");

    private static  String url;

    private static String thUrl;

    @BeforeClass
    public static void setUpClass() throws Exception {
        url = "jdbc:derby:" + dbDirectory.getPath().replace('\\','/');
        DefaultDataSource ds = new DefaultDataSource(url + ";create=true");

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        LOGGER.info("Inserting ISO schemas...");
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19119.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19108.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115-2.sql"));
        LOGGER.info("Inserting Ebrim schemas...");
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/data/defaultRecordSets.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/users/creation_user.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/profiles/inputLevels.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/catalog_web_service.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ebrimv2.5.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ebrimv3.0.sql"));
        LOGGER.info("Inserting datas...");
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-3.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-4.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-6.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-7.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-9.sql"));

        LOGGER.info("Writing thesauri...");
        thUrl = "jdbc:derby:" + dbTHDirectory.getPath().replace('\\','/');
        ThesaurusDatabaseCreator thCreator = new ThesaurusDatabaseCreator(dbTHDirectory);
        thCreator.createBaseStructureThesaurus();

        DefaultDataSource thds = new DefaultDataSource(thUrl + ";create=true");

        final ThesaurusDatabaseWriter writer = new ThesaurusDatabaseWriter(thds, "default", true, "th:test", "Test thesaurus",
                "various word used for Anchor", Arrays.asList(ISOLanguageCode.ENG), ISOLanguageCode.ENG);
        writer.store();
        writer.writeConcept(new Concept("SDN:L231:3:CDI", new Value("Common Data Index record", "en")));
        writer.writeConcept(new Concept("SDN:C320:2:FR", new Value("France", "en")));
        writer.writeConcept(new Concept("SDN:L101:2:4326", new Value("EPSG:4326", "en")));
        writer.writeConcept(new Concept("SDN:C371:1:2", new Value("2", "en")));
        writer.writeConcept(new Concept("SDN:C371:1:35", new Value("35", "en")));
        writer.writeConcept(new Concept("SDN:P021:35:ATTN", new Value("Transmittance and attenuance of the water column", "en")));
        writer.writeConcept(new Concept("SDN:P021:35:CNDC", new Value("Electrical conductivity of the water column", "en")));
        writer.writeConcept(new Concept("SDN:P021:35:DOXY", new Value("Dissolved oxygen parameters in the water column", "en")));
        writer.writeConcept(new Concept("SDN:P021:35:EXCO", new Value("Light extinction and diffusion coefficients", "en")));
        writer.writeConcept(new Concept("SDN:P021:35:HEXC", new Value("Dissolved noble gas concentration parameters in the water column", "en")));
        writer.writeConcept(new Concept("SDN:P021:35:OPBS", new Value("Optical backscatter", "en")));
        writer.writeConcept(new Concept("SDN:P021:35:PSAL", new Value("Salinity of the water column", "en")));
        writer.writeConcept(new Concept("SDN:P021:35:SCOX", new Value("Dissolved concentration parameters for 'other' gases in the water column", "en")));
        writer.writeConcept(new Concept("SDN:P021:35:TEMP", new Value("Temperature of the water column", "en")));
        writer.writeConcept(new Concept("SDN:P021:35:VSRA", new Value("Visible waveband radiance and irradiance measurements in the atmosphere", "en")));
        writer.writeConcept(new Concept("SDN:P021:35:VSRW", new Value("Visible waveband radiance and irradiance measurements in the water column", "en")));
        writer.writeConcept(new Concept("SDN:L241:1:MEDATLAS", new Value("MEDATLAS ASCII", "en")));
        writer.writeConcept(new Concept("SDN:L231:3:EDMED", new Value("EDMED record", "en")));
        writer.writeConcept(new Concept("SDN:EDMERP::9585", new Value("OCEANOGRAPHIC DATA CENTER", "en")));
    }

    @PostConstruct
    public void setUp() {
        SpringHelper.setApplicationContext(applicationContext);
        try {
            if (!serviceBusiness.getServiceIdentifiers("csw").contains("default")) {
                final File configDir = ConfigurationEngine.setupTestEnvironement("MDCSWWorkerTest");

                File CSWDirectory  = new File(configDir, "CSW");
                CSWDirectory.mkdir();
                final File instDirectory = new File(CSWDirectory, "default");
                instDirectory.mkdir();



                //we write the configuration file
                final BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
                Automatic configuration = new Automatic("mdweb", bdd);
                configuration.putParameter("transactionSecurized", "false");
                configuration.putParameter("shiroAccessible", "false");
                final BDD thBdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", thUrl, "", "");
                thBdd.setSchema("default");
                configuration.setThesaurus(Arrays.asList(thBdd));

                serviceBusiness.create("csw", "default", configuration, null, null);

                pool = EBRIMMarshallerPool.getInstance();
                //fillPoolAnchor((AnchoredMarshallerPool) pool);

                worker = new CSWworker("default");
            }
        } catch (Exception ex) {
            Logging.getLogger("org.constellation.metadata").log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (worker != null) {
            worker.destroy();
        }
        FileUtilities.deleteDirectory(dbDirectory);
        FileUtilities.deleteDirectory(dbTHDirectory);
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
        ConfigurationEngine.shutdownTestEnvironement("MDCSWWorkerTest");
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

    @Test
    @Override
    @Order(order=7)
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
    @Order(order=8)
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
    @Order(order=9)
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
    @Order(order = 10)
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
    @Order(order=11)
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
    @Order(order=12)
    public void transactionUpdateTest() throws Exception {
        super.transactionUpdateTest();

    }

}
