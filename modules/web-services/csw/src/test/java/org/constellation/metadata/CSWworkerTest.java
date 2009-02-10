/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

// Junit dependencies
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.cat.csw.v202.BriefRecordType;
import org.constellation.cat.csw.v202.Capabilities;
import org.constellation.cat.csw.v202.ElementSetNameType;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.cat.csw.v202.GetCapabilitiesType;
import org.constellation.cat.csw.v202.GetRecordByIdResponseType;
import org.constellation.cat.csw.v202.GetRecordByIdType;
import org.constellation.cat.csw.v202.RecordType;
import org.constellation.generic.database.Automatic;
import org.constellation.ows.v100.AcceptFormatsType;
import org.constellation.ows.v100.AcceptVersionsType;
import org.constellation.ows.v100.SectionsType;
import org.constellation.util.Util;
import org.constellation.ws.rs.NamespacePrefixMapperImpl;
import org.geotools.metadata.iso.MetaDataImpl;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (geomatys)
 */
public class CSWworkerTest {

    private CSWworker worker;

    private Unmarshaller unmarshaller;

    @BeforeClass
    public static void setUpClass() throws Exception {

        JAXBContext context = JAXBContext.newInstance(org.constellation.generic.database.ObjectFactory.class);
        Marshaller marshaller          = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        NamespacePrefixMapperImpl prefixMapper = new NamespacePrefixMapperImpl("");
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);

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
            marshaller.marshal(configuration, configFile);

        }

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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


        JAXBContext context = JAXBContext.newInstance(MetaDataImpl.class,
                                                      org.constellation.cat.csw.v202.ObjectFactory.class,
                                                      //org.constellation.ebrim.v300.ObjectFactory.class,
                                                      //org.constellation.ebrim.v250.ObjectFactory.class,
                                                      org.constellation.generic.database.ObjectFactory.class);
        unmarshaller      = context.createUnmarshaller();
        Marshaller marshaller          = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        NamespacePrefixMapperImpl prefixMapper = new NamespacePrefixMapperImpl("");
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);

        File configDir = new File("CSWWorkerTest");
        worker = new CSWworker("", unmarshaller, marshaller, configDir);
        Capabilities stcapa = (Capabilities) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/CSWCapabilities2.0.2.xml"));
        worker.setStaticCapabilities(stcapa);
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
    public void getCapabilitiesTest() throws Exception {

        /*
         *  TEST 1 : minimal getCapabilities
         */
        GetCapabilitiesType request = new GetCapabilitiesType("CSW");
        Capabilities result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("2.0.2"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() != null);
        assertTrue(result != null);

        /*
         *  TEST 2 : full get capabilities
         */
        AcceptVersionsType acceptVersions = new AcceptVersionsType("2.0.2");
        SectionsType sections             = new SectionsType("All");
        AcceptFormatsType acceptFormats   = new AcceptFormatsType("application/xml");
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, "", "CSW");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("2.0.2"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() != null);
        assertTrue(result != null);

        /*
         *  TEST 3 : get capabilities section Operation metadata
         */
        acceptVersions = new AcceptVersionsType("2.0.2");
        sections       = new SectionsType("OperationsMetadata");
        acceptFormats  = new AcceptFormatsType("application/xml");
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, "", "CSW");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("2.0.2"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() == null);
        assertTrue(result != null);

        /*
         *  TEST 4 : get capabilities section Service provider
         */
        acceptVersions = new AcceptVersionsType("2.0.2");
        sections       = new SectionsType("ServiceProvider");
        acceptFormats  = new AcceptFormatsType("application/xml");
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, "", "CSW");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("2.0.2"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() != null);
        assertTrue(result != null);

        /*
         *  TEST 5 : get capabilities section Service Identification
         */
        acceptVersions = new AcceptVersionsType("2.0.2");
        sections       = new SectionsType("ServiceIdentification");
        acceptFormats  = new AcceptFormatsType("application/xml");
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, "", "CSW");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("2.0.2"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() == null);
        assertTrue(result != null);
    }

    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getRecordByIdTest() throws Exception {

        /*
         *  TEST 1 : getRecordById with the first metadata in ISO mode.
         */
        GetRecordByIdType request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "application/xml", "http://www.isotc211.org/2005/gmd", Arrays.asList("42292_5p_19900609195600"));
        GetRecordByIdResponseType result = worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 0);
        assertTrue(result.getAny().size() == 1);
        Object obj = result.getAny().get(0);
        assertTrue(obj instanceof MetaDataImpl);

        MetaDataImpl isoResult = (MetaDataImpl) obj;

        MetaDataImpl ExpResult1 = (MetaDataImpl) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1.xml"));

        assertEquals(ExpResult1, isoResult);

        /*
         *  TEST 2 : getRecordById with the first metadata in DC mode (BRIEF).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.BRIEF),
                "application/xml", "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600"));
        result = worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().size() == 0);

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof BriefRecordType);

        BriefRecordType recordResult =  (BriefRecordType) obj;

        BriefRecordType ExpRecResult1 =  ((JAXBElement<BriefRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1BDC.xml"))).getValue();

        assertEquals(ExpRecResult1, recordResult);

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
