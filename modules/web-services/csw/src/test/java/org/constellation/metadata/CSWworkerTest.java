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

// Junit dependencies
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

// constyellation dependencies
import org.constellation.cat.csw.v202.AcknowledgementType;
import org.constellation.cat.csw.v202.GetRecordsResponseType;
import org.constellation.cat.csw.v202.BriefRecordType;
import org.constellation.cat.csw.v202.Capabilities;
import org.constellation.cat.csw.v202.ElementSetNameType;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.cat.csw.v202.GetCapabilitiesType;
import org.constellation.cat.csw.v202.GetRecordByIdResponseType;
import org.constellation.cat.csw.v202.GetRecordByIdType;
import org.constellation.cat.csw.v202.GetRecordsType;
import org.constellation.cat.csw.v202.QueryConstraintType;
import org.constellation.cat.csw.v202.QueryType;
import org.constellation.cat.csw.v202.RecordType;
import org.constellation.cat.csw.v202.ResultType;
import org.constellation.cat.csw.v202.SummaryRecordType;
import org.constellation.dublincore.v2.elements.SimpleLiteral;
import org.constellation.generic.database.Automatic;
import org.constellation.ogc.SortByType;
import org.constellation.ows.v100.AcceptFormatsType;
import org.constellation.ows.v100.AcceptVersionsType;
import org.constellation.ows.v100.BoundingBoxType;
import org.constellation.ows.v100.SectionsType;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.NamespacePrefixMapperImpl;
import org.geotools.metadata.iso.MetaDataImpl;
import static org.constellation.ows.OWSExceptionCode.*;
import static org.constellation.dublincore.v2.elements.ObjectFactory.*;
import static org.constellation.dublincore.v2.terms.ObjectFactory.*;
import static org.constellation.ows.v100.ObjectFactory._BoundingBox_QNAME;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test the differents methods of CSWWorker with a FileSystem reader/writer.
 * 
 * @author Guilhem Legal (geomatys)
 */
public class CSWworkerTest {

    private CSWworker worker;

    private Unmarshaller unmarshaller;

    @BeforeClass
    public static void setUpClass() throws Exception {
        deleteTemporaryFile();

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
        deleteTemporaryFile();
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

        JAXBContext context = JAXBContext.newInstance(CSWClassesContext.getAllClasses());
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
        
        /*
         *  TEST 6 : get capabilities with wrong version (waiting for an exception)
         */
        acceptVersions = new AcceptVersionsType("2.0.4");
        sections       = new SectionsType("All");
        acceptFormats  = new AcceptFormatsType("text/xml");
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, "", "CSW");

        boolean exLaunched = false;
        try {
            worker.getCapabilities(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), VERSION_NEGOTIATION_FAILED);
            assertEquals(ex.getLocator(), "acceptVersion");
        }

        assertTrue(exLaunched);


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

        BriefRecordType briefResult =  (BriefRecordType) obj;

        BriefRecordType expBriefResult1 =  ((JAXBElement<BriefRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1BDC.xml"))).getValue();

        assertEquals(expBriefResult1, briefResult);

        /*
         *  TEST 3 : getRecordById with the first metadata in DC mode (SUMMARY).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.SUMMARY),
                "application/xml", "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600"));
        result = worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().size() == 0);

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof SummaryRecordType);

        SummaryRecordType sumResult =  (SummaryRecordType) obj;

        SummaryRecordType expSumResult1 =  ((JAXBElement<SummaryRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1SDC.xml"))).getValue();

        assertEquals(expSumResult1, sumResult);

        /*
         *  TEST 4 : getRecordById with the first metadata in DC mode (FULL).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "application/xml", "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600"));
        result = worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().size() == 0);

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof RecordType);

        RecordType recordResult = (RecordType) obj;

        RecordType expRecordResult1 =  ((JAXBElement<RecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1FDC.xml"))).getValue();

        assertEquals(expRecordResult1, recordResult);

        /*
         *  TEST 5 : getRecordById with two metadata in DC mode (FULL).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "application/xml", "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600","42292_9s_19900610041000"));
        result = worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 2);
        assertTrue(result.getAny().size() == 0);

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof RecordType);
        RecordType recordResult1 = (RecordType) obj;

        obj = result.getAbstractRecord().get(1);
        assertTrue(obj instanceof RecordType);
        RecordType recordResult2 = (RecordType) obj;

        RecordType expRecordResult2 =  ((JAXBElement<RecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta2FDC.xml"))).getValue();

        assertEquals(expRecordResult1, recordResult1);
        assertEquals(expRecordResult2, recordResult2);

        /*
         *  TEST 6 : getRecordById with no identifier (waiting an exception).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "application/xml", "http://www.opengis.net/cat/csw/2.0.2", null);
        boolean exLaunched = false;
        try {
            worker.getRecordById(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "id");
        }

        assertTrue(exLaunched);

        /*
         *  TEST 7 : getRecordById with an unvalid identifier (waiting an exception).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "application/xml", "http://www.opengis.net/cat/csw/2.0.2",Arrays.asList("whatever"));
        exLaunched = false;
        try {
            worker.getRecordById(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "id");
        }

        assertTrue(exLaunched);

        /*
         *  TEST 8 : getRecordById with an unvalid outputSchema (waiting an exception).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "application/xml", "http://www.opengis.net/whatever",Arrays.asList("42292_5p_19900609195600"));
        exLaunched = false;
        try {
            worker.getRecordById(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "outputSchema");
        }

        assertTrue(exLaunched);

        /*
         *  TEST 9 : getRecordById with an unvalid outputFormat (waiting an exception).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "ping/pong", "http://www.opengis.net/cat/csw/2.0.2",Arrays.asList("42292_5p_19900609195600"));
        exLaunched = false;
        try {
            worker.getRecordById(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "outputFormat");
        }

        assertTrue(exLaunched);

        /*
         *  TEST 11 : getRecordById with the first metadata with no outputSchema.
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.SUMMARY),
                "application/xml", null, Arrays.asList("42292_5p_19900609195600"));
        result = worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().size() == 0);

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof SummaryRecordType);

        sumResult =  (SummaryRecordType) obj;

        expSumResult1 =  ((JAXBElement<SummaryRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1SDC.xml"))).getValue();

        assertEquals(expSumResult1, sumResult);

        /*
         *  TEST 12 : getRecordById with the first metadata with no outputSchema and no ElementSetName.
         */
        request = new GetRecordByIdType("CSW", "2.0.2", null,
                "application/xml", null, Arrays.asList("42292_5p_19900609195600"));
        result = worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().size() == 0);

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof SummaryRecordType);

        sumResult =  (SummaryRecordType) obj;

        expSumResult1 =  ((JAXBElement<SummaryRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1SDC.xml"))).getValue();

        assertEquals(expSumResult1, sumResult);

    }

    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getRecordsTest() throws Exception {
        
        /*
         *  TEST 1 : getRecords with HITS - DC mode (FULL) - CQL text: Title LIKE *0008411.ctd
         */
        
        List<QName> typeNames             = Arrays.asList(TypeNames._Record_QNAME);
        ElementSetNameType elementSetName = new ElementSetNameType(ElementSetType.FULL);
        SortByType sortBy                 = null;
        QueryConstraintType constraint    = new QueryConstraintType("Title LIKE '%0008411.ctd'", "1.0.0");
        QueryType query = new QueryType(typeNames, elementSetName, sortBy, constraint);
        GetRecordsType request = new GetRecordsType("CSW", "2.0.2", ResultType.HITS, null, "application/xml", "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        GetRecordsResponseType result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAbstractRecord().size() == 0);
        assertTrue(result.getSearchResults().getAny().size() == 0);
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 2);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 0);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        /*
         *  TEST 2 : getRecords with RESULTS - DC mode (FULL) - CQL text: Title LIKE *0008411.ctd
         */

        typeNames      = Arrays.asList(TypeNames._Record_QNAME);
        elementSetName = new ElementSetNameType(ElementSetType.FULL);
        sortBy         = null;
        constraint     = new QueryConstraintType("Title LIKE '%0008411.ctd'", "1.0.0");
        query          = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request        = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAbstractRecord().size() == 2);
        assertTrue(result.getSearchResults().getAny().size() == 0);
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 2);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 2);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        Object obj = result.getSearchResults().getAbstractRecord().get(0);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        assertTrue(obj instanceof RecordType);
        RecordType recordResult1 = (RecordType) obj;

        obj = result.getSearchResults().getAbstractRecord().get(1);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        assertTrue(obj instanceof RecordType);
        RecordType recordResult2 = (RecordType) obj;

        //because the order of the record can be random we re-order the results
        if (!recordResult1.getIdentifier().getContent().get(0).equals("42292_5p_19900609195600")) {
            RecordType temp = recordResult1;
            recordResult1   = recordResult2;
            recordResult2   = temp;
        }

        RecordType expRecordResult1 =  ((JAXBElement<RecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1FDC.xml"))).getValue();
        RecordType expRecordResult2 =  ((JAXBElement<RecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta2FDC.xml"))).getValue();

        assertEquals(expRecordResult1, recordResult1);
        assertEquals(expRecordResult2, recordResult2);

        /*
         *  TEST 3 : getRecords with VALIDATE - DC mode (FULL) - CQL text: Title LIKE *0008411.ctd
         */

        typeNames      = Arrays.asList(TypeNames._Record_QNAME);
        elementSetName = new ElementSetNameType(ElementSetType.FULL);
        sortBy         = null;
        constraint     = new QueryConstraintType("Title LIKE '%0008411.ctd'", "1.0.0");
        query          = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request        = new GetRecordsType("CSW", "2.0.2", ResultType.VALIDATE, null, "application/xml", "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        assertTrue(worker.getRecords(request) instanceof AcknowledgementType);

        /*
         *  TEST 4 : getRecords with RESULTS - DC mode (BRIEF) - CQL text: Title LIKE *0008411.ctd
         */

        typeNames      = Arrays.asList(TypeNames._Record_QNAME);
        elementSetName = new ElementSetNameType(ElementSetType.BRIEF);
        sortBy         = null;
        constraint     = new QueryConstraintType("Title LIKE '%0008411.ctd'", "1.0.0");
        query          = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request        = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAbstractRecord().size() == 2);
        assertTrue(result.getSearchResults().getAny().size() == 0);
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.BRIEF));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 2);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 2);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        obj = result.getSearchResults().getAbstractRecord().get(0);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        assertTrue(obj instanceof BriefRecordType);
        BriefRecordType briefResult1 = (BriefRecordType) obj;

        obj = result.getSearchResults().getAbstractRecord().get(1);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        assertTrue(obj instanceof BriefRecordType);
        BriefRecordType briefResult2 = (BriefRecordType) obj;

        //because the order of the record can be random we re-order the results
        if (!briefResult1.getIdentifier().get(0).getContent().get(0).equals("42292_5p_19900609195600")) {
            BriefRecordType temp = briefResult1;
            briefResult1   = briefResult2;
            briefResult2   = temp;
        }

        BriefRecordType expBriefResult1 =  ((JAXBElement<BriefRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1BDC.xml"))).getValue();
        BriefRecordType expBriefResult2 =  ((JAXBElement<BriefRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta2BDC.xml"))).getValue();

        assertEquals(expBriefResult1, briefResult1);
        assertEquals(expBriefResult2, briefResult2);


        /*
         *  TEST 5 : getRecords with RESULTS - DC mode (Custom) - CQL text: Title LIKE *0008411.ctd
         */

        typeNames        = Arrays.asList(TypeNames._Record_QNAME);
        List<QName> cust = new ArrayList<QName>();
        cust.add(_Identifier_QNAME);
        cust.add(_Subject_QNAME);
        cust.add(_Date_QNAME);
        cust.add(_Format_QNAME);
        sortBy           = null;
        constraint       = new QueryConstraintType("Title LIKE '%0008411.ctd'", "1.0.0");
        query            = new QueryType(typeNames, cust, sortBy, constraint);
        request          = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAbstractRecord().size() == 2);
        assertTrue(result.getSearchResults().getAny().size() == 0);
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 2);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 2);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        obj = result.getSearchResults().getAbstractRecord().get(0);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        assertTrue(obj instanceof RecordType);
        RecordType customResult1 = (RecordType) obj;

        obj = result.getSearchResults().getAbstractRecord().get(1);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        assertTrue(obj instanceof RecordType);
        RecordType customResult2 = (RecordType) obj;

        //because the order of the record can be random we re-order the results
        if (!customResult1.getIdentifier().getContent().get(0).equals("42292_5p_19900609195600")) {
            RecordType temp = customResult1;
            customResult1   = customResult2;
            customResult2   = temp;
        }

        RecordType expCustomResult1 =  ((JAXBElement<RecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1CustomDC.xml"))).getValue();
        RecordType expCustomResult2 =  ((JAXBElement<RecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta2CustomDC.xml"))).getValue();

        assertEquals(expCustomResult1, customResult1);
        assertEquals(expCustomResult2, customResult2);


        /*
         *  TEST 5 : getRecords with RESULTS - DC mode (Custom) - CQL text: Title LIKE *0008411.ctd
         */

        typeNames        = Arrays.asList(TypeNames._Record_QNAME);
        cust             = new ArrayList<QName>();
        cust.add(_BoundingBox_QNAME);
        cust.add(_Modified_QNAME);
        cust.add(_Identifier_QNAME);
        sortBy           = null;
        constraint       = new QueryConstraintType("Title LIKE '%0008411.ctd'", "1.0.0");
        query            = new QueryType(typeNames, cust, sortBy, constraint);
        request          = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAbstractRecord().size() == 2);
        assertTrue(result.getSearchResults().getAny().size() == 0);
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 2);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 2);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        obj = result.getSearchResults().getAbstractRecord().get(0);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        assertTrue(obj instanceof RecordType);
        customResult1 = (RecordType) obj;

        obj = result.getSearchResults().getAbstractRecord().get(1);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        assertTrue(obj instanceof RecordType);
        customResult2 = (RecordType) obj;

        //because the order of the record can be random we re-order the results
        if (!customResult1.getIdentifier().getContent().get(0).equals("42292_5p_19900609195600")) {
            RecordType temp = customResult1;
            customResult1   = customResult2;
            customResult2   = temp;
        }

        expCustomResult1 =  new RecordType();
        expCustomResult1.setIdentifier(new SimpleLiteral("42292_5p_19900609195600"));
        expCustomResult1.setModified(new SimpleLiteral("2009-01-01T00:00:00"));
        expCustomResult1.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));
        expCustomResult2 =  new RecordType();
        expCustomResult2.setIdentifier(new SimpleLiteral("42292_9s_19900610041000"));
        expCustomResult2.setModified(new SimpleLiteral("2009-01-26T12:00:00"));
        expCustomResult2.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.3667, 36.6, 1.3667, 36.6));


        assertEquals(expCustomResult1, customResult1);
        assertEquals(expCustomResult2, customResult2);

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
