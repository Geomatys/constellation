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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.ServiceBusiness;
import org.constellation.generic.database.Automatic;
import static org.constellation.metadata.FileSystemCSWworkerTest.writeDataFile;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.NodeUtilities;
import org.constellation.ws.MimeType;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.ResultType;
import static org.geotoolkit.csw.xml.TypeNames.RECORD_QNAME;
import org.geotoolkit.csw.xml.v202.ElementSetNameType;
import org.geotoolkit.csw.xml.v202.GetRecordsResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordsType;
import org.geotoolkit.csw.xml.v202.QueryConstraintType;
import org.geotoolkit.csw.xml.v202.QueryType;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.ogc.xml.v110.SortByType;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Node;

/**
 * The purpose of this test is to run a "discovery" CSW and launch a spatial request on it.
 * Cause a crash with no closing management of the R-Tree
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
public class TreeCloseTest {

    @Inject
    private ServiceBusiness serviceBusiness;
    
    private static CSWworker worker;

    @PostConstruct
    public void setUpClass() {
        try {
            deleteTemporaryFile();
            
            final File configDir = ConfigurationEngine.setupTestEnvironement("TreeCloseTest");
            
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
            
            //we write the configuration file
            Automatic configuration = new Automatic("filesystem", dataDirectory.getPath());
            configuration.setProfile("discovery");
            configuration.putParameter("transactionSecurized", "false");
            configuration.putParameter("shiroAccessible", "false");
            
            serviceBusiness.create("CSW", "default", configuration, null);
            
            worker = new CSWworker("default");
            worker.setLogLevel(Level.FINER);
        } catch (Exception ex) {
            Logger.getLogger(TreeCloseTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        deleteTemporaryFile();
    }

    public static void deleteTemporaryFile() {
        if (worker != null) {
            worker.destroy();
        }
        ConfigurationEngine.shutdownTestEnvironement("TreeCloseTest");
    }

    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    public void spatialSearchTest() throws Exception {
     
        /*
         *  TEST 1 : getRecords with HITS - DC mode (FULL) - CQL text: BBOX
         */

        List<QName> typeNames             = Arrays.asList(RECORD_QNAME);
        ElementSetNameType elementSetName = new ElementSetNameType(ElementSetType.FULL);
        SortByType sortBy                 = null;
        QueryConstraintType constraint    = new QueryConstraintType("BBOX(ows:BoundingBox, 10,20,30,40)", "1.0.0");
        QueryType query = new QueryType(typeNames, elementSetName, sortBy, constraint);
        GetRecordsType request = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        GetRecordsResponseType result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
        assertEquals(1, result.getSearchResults().getAny().size());
        assertEquals(1, result.getSearchResults().getNumberOfRecordsMatched());
        assertEquals(1, result.getSearchResults().getNumberOfRecordsReturned());
        assertEquals(0, result.getSearchResults().getNextRecord());

        Object obj = result.getSearchResults().getAny().get(0);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }

        if (obj instanceof RecordType) {
            RecordType recordResult = (RecordType) obj;
            assertEquals(recordResult.getIdentifier().getContent().get(0), "42292_9s_19900610041000");
        } else {
            Node recordResult = (Node) obj;
            assertEquals(NodeUtilities.getValuesFromPath(recordResult, "/csw:Record/dc:identifier").get(0), "42292_9s_19900610041000");
        }

    }
}
