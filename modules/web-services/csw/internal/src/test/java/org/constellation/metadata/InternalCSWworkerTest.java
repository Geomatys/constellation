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
import java.io.StringWriter;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.xml.XML;
import org.constellation.admin.SpringHelper;
import org.constellation.business.IMetadataBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.database.api.repository.MetadataRepository;
import org.constellation.generic.database.Automatic;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.xml.AnchoredMarshallerPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
public class InternalCSWworkerTest extends CSWworkerTest {

    @Inject
    private IServiceBusiness serviceBusiness;

    @Inject
    private IProviderBusiness providerBusiness;

    @Inject
    private IMetadataBusiness metadataBusiness;

    private static File configDirectory;

    private static boolean initialized = false;
    
    @BeforeClass
    public static void initTestDir() {
        configDirectory = ConfigDirectory.setupTestEnvironement("InternalCSWWorkerTest");
    }
    
    @PostConstruct
    public void setUpClass() {
        onlyIso = true;
        SpringHelper.setApplicationContext(applicationContext);
        try {
            if (!initialized) {
                serviceBusiness.deleteAll();
                providerBusiness.removeAll();
                metadataBusiness.deleteAllMetadata();

                pool = EBRIMMarshallerPool.getInstance();
                fillPoolAnchor((AnchoredMarshallerPool) pool);

                File CSWDirectory  = new File(configDirectory, "CSW");
                CSWDirectory.mkdir();
                final File instDirectory = new File(CSWDirectory, "default");
                instDirectory.mkdir();

                //we write the data files
                writeProvider("meta1.xml",         "42292_5p_19900609195600");
                writeProvider("meta2.xml",         "42292_9s_19900610041000");
                writeProvider("meta3.xml",         "39727_22_19750113062500");
                writeProvider("meta4.xml",         "11325_158_19640418141800");
                writeProvider("meta5.xml",         "40510_145_19930221211500");
                writeProvider("meta-19119.xml",    "mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4");
                writeProvider("imageMetadata.xml", "gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
                /*writeProvider("ebrim1.xml",        "000068C3-3B49-C671-89CF-10A39BB1B652");
                writeProvider("ebrim2.xml",        "urn:uuid:3e195454-42e8-11dd-8329-00e08157d076");
                writeProvider("ebrim3.xml",        "urn:motiive:csw-ebrim");
                writeProvider("meta13.xml",        "urn:uuid:1ef30a8b-876d-4828-9246-dcbbyyiioo");*/

                //we write the configuration file
                final String nulll = null;
                Automatic configuration = new Automatic("internal", nulll);
                configuration.putParameter("shiroAccessible", "false");

                serviceBusiness.create("csw", "default", configuration, null);

                worker = new CSWworker("default");
                worker.setLogLevel(Level.FINER);
                initialized = true;
            }
        } catch (Exception ex) {
            Logger.getLogger(InternalCSWworkerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
            SpringHelper.getBean(IServiceBusiness.class).deleteAll();
            SpringHelper.getBean(IProviderBusiness.class).removeAll();
            SpringHelper.getBean(IMetadataBusiness.class).deleteAllMetadata();

        if (worker != null) {
            worker.destroy();
        }
        ConfigDirectory.shutdownTestEnvironement("InternalCSWWorkerTest");
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

    public void writeProvider(String resourceName, String identifier) throws Exception {

        Unmarshaller u = pool.acquireUnmarshaller();
        u.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
        Object obj = u.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/" + resourceName));
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement)obj).getValue();
        }
        DefaultMetadata meta = (DefaultMetadata) obj; 
        pool.recycle(u);

        Marshaller m = pool.acquireMarshaller();
        m.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
        final StringWriter sw = new StringWriter();
        m.marshal(meta, sw);
        pool.recycle(m);
        
        
        metadataBusiness.updateMetadata(identifier, sw.toString());
    }
}
