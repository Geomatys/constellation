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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

// constyellation dependencies
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.GetDomainResponse;
import org.geotoolkit.csw.xml.v202.AcknowledgementType;
import org.geotoolkit.csw.xml.v202.GetRecordsResponseType;
import org.geotoolkit.csw.xml.v202.BriefRecordType;
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.csw.xml.v202.DeleteType;
import org.geotoolkit.csw.xml.v202.DomainValuesType;
import org.geotoolkit.csw.xml.v202.ElementSetNameType;
import org.geotoolkit.csw.xml.v202.ElementSetType;
import org.geotoolkit.csw.xml.v202.GetCapabilitiesType;
import org.geotoolkit.csw.xml.v202.GetDomainResponseType;
import org.geotoolkit.csw.xml.v202.GetDomainType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdType;
import org.geotoolkit.csw.xml.v202.GetRecordsType;
import org.geotoolkit.csw.xml.v202.InsertType;
import org.geotoolkit.csw.xml.v202.ListOfValuesType;
import org.geotoolkit.csw.xml.v202.QueryConstraintType;
import org.geotoolkit.csw.xml.v202.QueryType;
import org.geotoolkit.csw.xml.v202.RecordPropertyType;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.csw.xml.v202.ResultType;
import org.geotoolkit.csw.xml.v202.SummaryRecordType;
import org.geotoolkit.csw.xml.v202.TransactionResponseType;
import org.geotoolkit.csw.xml.v202.TransactionType;
import org.geotoolkit.csw.xml.v202.UpdateType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import org.constellation.generic.database.Automatic;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.metadata.iso.DefaultExtendedElementInformation;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory.*;
import static org.geotoolkit.dublincore.xml.v2.terms.ObjectFactory.*;
import static org.constellation.metadata.TypeNames.*;
import static org.geotoolkit.ows.xml.v100.ObjectFactory._BoundingBox_QNAME;

// geotools dependencies
import org.geotoolkit.metadata.iso.DefaultMetaData;
import org.geotoolkit.metadata.iso.citation.DefaultResponsibleParty;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;

// JUnit dependencies
import org.geotoolkit.ogc.xml.v110modified.SortByType;
import org.geotoolkit.ogc.xml.v110modified.SortOrderType;
import org.geotoolkit.ogc.xml.v110modified.SortPropertyType;
import org.geotoolkit.ows.xml.v100.AcceptFormatsType;
import org.geotoolkit.ows.xml.v100.AcceptVersionsType;
import org.geotoolkit.ows.xml.v100.BoundingBoxType;
import org.geotoolkit.ows.xml.v100.SectionsType;
import org.geotoolkit.util.SimpleInternationalString;
import org.geotoolkit.xml.MarshallerPool;
import org.junit.*;
import org.opengis.metadata.Datatype;
import org.opengis.metadata.ExtendedElementInformation;
import org.opengis.metadata.citation.Role;
import static org.junit.Assert.*;

/**
 * Test the differents methods of CSWWorker with a FileSystem reader/writer.
 * 
 * @author Guilhem Legal (geomatys)
 */
public class CSWworkerTest {

    private CSWworker worker;

    private static MarshallerPool pool;
    private static Unmarshaller unmarshaller;

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
        GetRecordByIdResponseType result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 0);
        assertTrue(result.getAny().size() == 1);
        Object obj = result.getAny().get(0);
        assertTrue(obj instanceof DefaultMetaData);

        DefaultMetaData isoResult = (DefaultMetaData) obj;

        DefaultMetaData ExpResult1 = (DefaultMetaData) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1.xml"));

        assertEquals(ExpResult1, isoResult);

        /*
         *  TEST 2 : getRecordById with the first metadata in DC mode (BRIEF).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.BRIEF),
                "application/xml", "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

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
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

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
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

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
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

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
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

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
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

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
     * Tests the getRecords method
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getRecordsTest() throws Exception {
        
        /*
         *  TEST 1 : getRecords with HITS - DC mode (FULL) - CQL text: Title LIKE *0008411.ctd
         */
        
        List<QName> typeNames             = Arrays.asList(TypeNames.RECORD_QNAME);
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

        typeNames      = Arrays.asList(TypeNames.RECORD_QNAME);
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

        typeNames      = Arrays.asList(TypeNames.RECORD_QNAME);
        elementSetName = new ElementSetNameType(ElementSetType.FULL);
        sortBy         = null;
        constraint     = new QueryConstraintType("Title LIKE '%0008411.ctd'", "1.0.0");
        query          = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request        = new GetRecordsType("CSW", "2.0.2", ResultType.VALIDATE, null, "application/xml", "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        assertTrue(worker.getRecords(request) instanceof AcknowledgementType);

        /*
         *  TEST 4 : getRecords with RESULTS - DC mode (BRIEF) - CQL text: Title LIKE *0008411.ctd
         */

        typeNames      = Arrays.asList(TypeNames.RECORD_QNAME);
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

        typeNames        = Arrays.asList(TypeNames.RECORD_QNAME);
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

        typeNames        = Arrays.asList(TypeNames.RECORD_QNAME);
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

    /**
     * Tests the getDomain method
     *
     * @throws java.lang.Exception
     */
    @Test
    public void getDomainTest() throws Exception {

        /*
         *  TEST 1 : getDomain 2.0.2 parameterName = GetCapabilities.sections
         */
        GetDomainType request = new GetDomainType("CSW", "2.0.2", null, "GetCapabilities.sections");

        GetDomainResponse result = worker.getDomain(request);

        assertTrue(result instanceof GetDomainResponseType);

        List<DomainValues> domainValues = new ArrayList<DomainValues>();
        ListOfValuesType values = new  ListOfValuesType(Arrays.asList("All", "ServiceIdentification", "ServiceProvider", "OperationsMetadata", "Filter_Capabilities"));
        DomainValuesType value  = new DomainValuesType("GetCapabilities.sections", null, values, CAPABILITIES_QNAME);
        domainValues.add(value);
        GetDomainResponse expResult = new GetDomainResponseType(domainValues);

        assertEquals(expResult, result);
        
        
        /*
         *  TEST 1 : getDomain 2.0.0 parameterName = GetCapabilities.sections
         */
        org.geotoolkit.csw.xml.v200.GetDomainType request200 = new org.geotoolkit.csw.xml.v200.GetDomainType("CSW", "2.0.0", null, "GetCapabilities.sections");

        GetDomainResponse result200 = worker.getDomain(request200);

        assertTrue(result200 instanceof org.geotoolkit.csw.xml.v200.GetDomainResponseType);

        List<DomainValues> domainValues200 = new ArrayList<DomainValues>();
        List<String> list = new ArrayList<String>();
        list.add("All");
        list.add("ServiceIdentification");
        list.add("ServiceProvider");
        list.add("OperationsMetadata");
        list.add("Filter_Capabilities");
        org.geotoolkit.csw.xml.v200.ListOfValuesType values200 = new org.geotoolkit.csw.xml.v200.ListOfValuesType(list);
        org.geotoolkit.csw.xml.v200.DomainValuesType value200  = new org.geotoolkit.csw.xml.v200.DomainValuesType("GetCapabilities.sections", null, values200, CAPABILITIES_QNAME);
        domainValues200.add(value200);
        GetDomainResponse expResult200 = new org.geotoolkit.csw.xml.v200.GetDomainResponseType(domainValues200);

        assertEquals(expResult200, result200);


    }

    /**
     * Tests the transaction method
     *
     * @throws java.lang.Exception
     */
    @Test
    public void transactionDeleteTest() throws Exception {
        
        /*
         *  TEST 1 : we delete the metadata 42292_5p_19900609195600
         */

        // first we must be sure that the metadata is present
        GetRecordByIdType requestGRBI = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "application/xml", "http://www.isotc211.org/2005/gmd", Arrays.asList("42292_5p_19900609195600"));
        GetRecordByIdResponseType GRresult = (GetRecordByIdResponseType) worker.getRecordById(requestGRBI);

        assertTrue(GRresult != null);
        assertTrue(GRresult.getAbstractRecord().size() == 0);
        assertTrue(GRresult.getAny().size() == 1);
        Object obj = GRresult.getAny().get(0);
        assertTrue(obj instanceof DefaultMetaData);

        DefaultMetaData isoResult = (DefaultMetaData) obj;
        DefaultMetaData ExpResult1 = (DefaultMetaData) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1.xml"));
        assertEquals(ExpResult1, isoResult);

        // we delete the metadata
        QueryConstraintType constraint = new QueryConstraintType("identifier='42292_5p_19900609195600'", "1.1.0");
        DeleteType delete = new DeleteType(null, constraint);
        TransactionType request = new TransactionType("CSW", "2.0.2", delete);

        TransactionResponseType result = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalDeleted(), 1);

        // we try to request the deleted metadata
        CstlServiceException exe = null;
        try {
            GRresult = (GetRecordByIdResponseType) worker.getRecordById(requestGRBI);
        } catch (CstlServiceException ex) {
            exe = ex;
        }

        // we must receive an exception saying that the metadata is not present.
        assertTrue(exe != null);
        assertEquals(exe.getExceptionCode() , INVALID_PARAMETER_VALUE);
        assertEquals(exe.getLocator() , "id");
    }

    /**
     * Tests the transaction method
     *
     * @throws java.lang.Exception
     */
    @Test
    public void transactionInsertTest() throws Exception {

        /*
         *  TEST 1 : we add the metadata 42292_5p_19900609195600
         */
        DefaultMetaData ExpResult1 = (DefaultMetaData) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1.xml"));

        InsertType insert       = new InsertType(ExpResult1);
        TransactionType request = new TransactionType("CSW", "2.0.2", insert);
        TransactionResponseType result  = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalInserted(), 1);


        // then we must be sure that the metadata is present
        GetRecordByIdType requestGRBI = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "application/xml", "http://www.isotc211.org/2005/gmd", Arrays.asList("42292_5p_19900609195600"));
        GetRecordByIdResponseType GRresult = (GetRecordByIdResponseType) worker.getRecordById(requestGRBI);

        assertTrue(GRresult != null);
        assertTrue(GRresult.getAbstractRecord().size() == 0);
        assertTrue(GRresult.getAny().size() == 1);
        Object obj = GRresult.getAny().get(0);
        assertTrue(obj instanceof DefaultMetaData);

        DefaultMetaData isoResult = (DefaultMetaData) obj;
        assertEquals(ExpResult1, isoResult);

    }

    /**
     * Tests the transaction method
     *
     * @throws java.lang.Exception
     */
    @Test
    public void transactionUpdateTest() throws Exception {
        
        /*
         *  TEST 1 : we update the metadata 42292_5p_19900609195600 by replacing it by another metadata
         */

        DefaultMetaData replacement        = (DefaultMetaData) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta6.xml"));
        QueryConstraintType constraint  = new QueryConstraintType("identifier='42292_5p_19900609195600'", "1.1.0");
        UpdateType update               = new UpdateType(replacement, constraint);
        TransactionType request         = new TransactionType("CSW", "2.0.2", update);
        TransactionResponseType result  = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);


        // we try to request the updated metadata
        CstlServiceException exe = null;
        try {
            GetRecordByIdType requestGRBI = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "application/xml", "http://www.isotc211.org/2005/gmd", Arrays.asList("42292_5p_19900609195600"));
            GetRecordByIdResponseType GRresult = (GetRecordByIdResponseType) worker.getRecordById(requestGRBI);
        } catch (CstlServiceException ex) {
            exe = ex;
        }

        // we must receive an exception saying that the metadata is not present.
        assertTrue(exe != null);
        assertEquals(exe.getExceptionCode() , INVALID_PARAMETER_VALUE);
        assertEquals(exe.getLocator() , "id");
        
        
        // then we must be sure that the replacement metadata is present
        GetRecordByIdType  requestGRBI = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "application/xml", "http://www.isotc211.org/2005/gmd", Arrays.asList("CTDF02"));
        GetRecordByIdResponseType GRresult = (GetRecordByIdResponseType) worker.getRecordById(requestGRBI);

        assertTrue(GRresult != null);
        assertTrue(GRresult.getAbstractRecord().size() == 0);
        assertTrue(GRresult.getAny().size() == 1);
        Object obj = GRresult.getAny().get(0);
        assertTrue(obj instanceof DefaultMetaData);

        DefaultMetaData isoResult = (DefaultMetaData) obj;
        assertEquals(replacement, isoResult);


        /*
         *  TEST 2 : we update the metadata 11325_158_19640418141800 by replacing a single Property
         *  we replace the property MD_Metadata.language from en to fr.
         */

        // we perform a request to get the list of metadata matching language = en
        constraint        = new QueryConstraintType("Language = 'eng'", "1.0.0");
        SortPropertyType sp = new SortPropertyType("Identifier", SortOrderType.ASC);
        SortByType sort   = new SortByType(Arrays.asList(sp));
        QueryType query   = new QueryType(TypeNames.ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), sort, constraint);
        GetRecordsType gr = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        GetRecordsResponseType response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 5);

        List<String> results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        List<String> expResult = new ArrayList<String>();
        expResult.add("11325_158_19640418141800");
        expResult.add("39727_22_19750113062500");
        expResult.add("40510_145_19930221211500");
        expResult.add("42292_9s_19900610041000");
        expResult.add("CTDF02");

        assertEquals(expResult, results);


        // we update the metadata 11325_158_19640418141800 by replacing the language eng by fr
        constraint = new QueryConstraintType("identifier='11325_158_19640418141800'", "1.1.0");
        List<RecordPropertyType> properties = new ArrayList<RecordPropertyType>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/language", "fr"));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);

        // we perform again the getRecord request the modified metadata must not appears in the list
        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 4);

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("39727_22_19750113062500");
        expResult.add("40510_145_19930221211500");
        expResult.add("42292_9s_19900610041000");
        expResult.add("CTDF02");

        assertEquals(expResult, results);


         // we make a getRecords request with language=fr to verify that the modified metadata is well indexed
        constraint = new QueryConstraintType("Language = 'fra'", "1.0.0");
        query      = new QueryType(TypeNames.ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.isotc211.org/2005/gmd", 1, 10, query, null);
        
        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 1);

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("11325_158_19640418141800");

        assertEquals(expResult, results);

        /*
         *  TEST 3 : we update the metadata 39727_22_19750113062500 by replacing a single Property
         *  we replace the property MD_Metadata.identificationInfo.abstract from "Donnees CTD ANGOLA CAP 7501 78" to "Modified datas by CSW-T".
         */

        // first we make a getRecords request to verify that the metadata match the request on the Abstract field
        constraint = new QueryConstraintType("Abstract = 'Donnees CTD ANGOLA CAP 7501 78'", "1.0.0");
        query      = new QueryType(TypeNames.ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 1);

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("39727_22_19750113062500");

        assertEquals(expResult, results);


        // we update the metadata 11325_158_19640418141800 by replacing the abstract field from "Donnees CTD ANGOLA CAP 7501 78" to "Modified datas by CSW-T".
        constraint = new QueryConstraintType("identifier='39727_22_19750113062500'", "1.1.0");
        properties = new ArrayList<RecordPropertyType>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/identificationInfo/abstract", "Modified datas by CSW-T"));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);


        // we verify that the metadata does not appears anymore in the precedent getRecords request
        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 0);

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();

        assertEquals(expResult, results);

        // then we verify that the modified metadata is well modified and indexed
        constraint = new QueryConstraintType("Abstract = 'Modified datas by CSW-T'", "1.0.0");
        query      = new QueryType(TypeNames.ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 1);

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("39727_22_19750113062500");

        assertEquals(expResult, results);

        /*
         *  TEST 4 : we update the metadata 39727_22_19750113062500 by replacing a single Property
         *  we replace the property MD_Metadata.dateStamp with "2009-03-31T12:00:00.000+01:00".
         */

        // we update the metadata 39727_22_19750113062500 by replacing the dateStamp field with "2009-03-31T12:00:00.000+01:00".
        constraint = new QueryConstraintType("identifier='39727_22_19750113062500'", "1.1.0");
        properties = new ArrayList<RecordPropertyType>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/dateStamp", "2009-03-31T12:00:00.000+01:00"));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);

        // then we verify that the modified metadata is well modified and indexed
        constraint = new QueryConstraintType("Modified after 2009-03-30T00:00:00Z", "1.0.0");
        query      = new QueryType(TypeNames.ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 1);

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("39727_22_19750113062500");

        assertEquals(expResult, results);

        /*
         *  TEST 5 : we update the metadata 39727_22_19750113062500 by replacing a complex Property
         *  we replace the property MD_Metadata.identificationInfo.extent.geographicElement by a new Geographic bounding box".
         */

        // we update the metadata 11325_158_19640418141800 by replacing the geographicElement.
        constraint = new QueryConstraintType("identifier='39727_22_19750113062500'", "1.1.0");
        properties = new ArrayList<RecordPropertyType>();
        DefaultGeographicBoundingBox geographicElement = new DefaultGeographicBoundingBox(1.1, 1.1, 1.1, 1.1);
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/identificationInfo/extent/geographicElement", geographicElement));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);

        
        // then we verify that the modified metadata is well modified and indexed
        constraint = new QueryConstraintType("WestBoundLongitude = '1.1'", "1.0.0");
        query      = new QueryType(TypeNames.ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 1);

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("39727_22_19750113062500");

        assertEquals(expResult, results);

        /*
         *  TEST 6 : we try to update the metadata 11325_158_19640418141800 by replacing a single Property
         *  we try to replace the property MD_Metadata.language from en to a complex type CI_ResponsibleParty.
         * we must receive an exception saying that is not the good type.
         */

        // we perform a request to get the list of metadata matching language = fra
        constraint = new QueryConstraintType("Language = 'fra'", "1.0.0");
        sp         = new SortPropertyType("Identifier", SortOrderType.ASC);
        sort       = new SortByType(Arrays.asList(sp));
        query      = new QueryType(TypeNames.ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), sort, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(1, response.getSearchResults().getAny().size());

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("11325_158_19640418141800");

        assertEquals(expResult, results);


        // we update the metadata 11325_158_19640418141800 by replacing the language eng by a responsibleParty
        constraint = new QueryConstraintType("identifier='11325_158_19640418141800'", "1.1.0");
        DefaultResponsibleParty value = new DefaultResponsibleParty(Role.AUTHOR);
        properties = new ArrayList<RecordPropertyType>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/language",value));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);

        exe = null;
        try {
            result     = worker.transaction(request);
        } catch (CstlServiceException ex) {
            exe = ex;
        }

        assertTrue(exe != null);
        assertEquals(exe.getExceptionCode(), INVALID_PARAMETER_VALUE);

        // we perform again the getRecord request the modified metadata must appears again in the list
        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 1);

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("11325_158_19640418141800");
        assertEquals(expResult, results);

        /*
         *  TEST 7 : we update the metadata 39727_22_19750113062500 by replacing a single Property
         *  we replace the property MD_Metadata.dateStamp with "hello world".
         *  we must receive an exception saying that is not the good type.
         */

        // we update the metadata 39727_22_19750113062500 by replacing the dateStamp field with "hello world".
        constraint = new QueryConstraintType("identifier='39727_22_19750113062500'", "1.1.0");
        properties = new ArrayList<RecordPropertyType>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/dateStamp", "hello world"));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);

        exe = null;
        try {
            result     = worker.transaction(request);
        } catch (CstlServiceException ex) {
            exe = ex;
        }

        assertTrue(exe != null);

        // then we verify that the metadata is not modified
        constraint = new QueryConstraintType("Modified after 2009-03-30T00:00:00Z", "1.0.0");
        query      = new QueryType(TypeNames.ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 1);

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("39727_22_19750113062500");

        assertEquals(expResult, results);

        /*
         *  TEST 8 : we update the metadata 39727_22_19750113062500 by replacing a complex Property
         *  we replace the property MD_Metadata.identificationInfo.extent.geographicElement by a responsible party".
         *  we must receive an exception
         */

        // we update the metadata 11325_158_19640418141800 by replacing the geographicElement.
        constraint = new QueryConstraintType("identifier='39727_22_19750113062500'", "1.1.0");
        properties = new ArrayList<RecordPropertyType>();
        value = new DefaultResponsibleParty(Role.AUTHOR);
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/identificationInfo/extent/geographicElement", value));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);

        exe = null;
        try {
            result     = worker.transaction(request);
        } catch (CstlServiceException ex) {
            exe = ex;
        }
        assertTrue(exe != null);


        // then we verify that the metadata is not modified
        constraint = new QueryConstraintType("WestBoundLongitude = '1.1'", "1.0.0");
        query      = new QueryType(TypeNames.ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 1);

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("39727_22_19750113062500");

        assertEquals(expResult, results);


        /*
         *  TEST 9 : we update the metadata 42292_9s_1990061004100 by replacing a numeroted single Property
         *  we replace the property MD_Metadata.identificationInfo.descriptiveKeywords[3].keyword from "research vessel" to "Modified datas by CSW-T".
         */

        // first we make a getRecords request to verify that the metadata match the request on the Subject field
        constraint = new QueryConstraintType("Subject = 'research vessel' AND Subject = 'CTD profilers'", "1.0.0");
        query      = new QueryType(TypeNames.ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), sort, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(3, response.getSearchResults().getAny().size());

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("39727_22_19750113062500");
        expResult.add("40510_145_19930221211500");
        expResult.add("42292_9s_19900610041000");

        assertEquals(expResult, results);


        // we update the metadata 42292_9s_1990061004100 by replacing the third descriptive field from "research vessel" to "Modified datas by CSW-T".
        constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.1.0");
        properties = new ArrayList<RecordPropertyType>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/identificationInfo/descriptiveKeywords[3]/keyword", "Modified datas by CSW-T"));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);


        // we verify that the metadata does not appears anymore in the precedent getRecords request
        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(2, response.getSearchResults().getAny().size());

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("39727_22_19750113062500");
        expResult.add("40510_145_19930221211500");

        assertEquals(expResult, results);

        // then we verify that the modified metadata is well modified and indexed
        constraint = new QueryConstraintType("Subject = 'Modified datas by CSW-T' AND Subject = 'CTD profilers'", "1.0.0");
        query      = new QueryType(TypeNames.ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(1 , response.getSearchResults().getAny().size());

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("42292_9s_19900610041000");

        assertEquals(expResult, results);

         /*
         *  TEST 10 : we update the metadata 42292_9s_1990061004100 by replacing a numeroted single Property
         *  we replace the property MD_Metadata.identificationInfo.descriptiveKeywords[1].keyword[7] from "Salinity of the water column" to "something".
         */

        // first we make a getRecords request to verify that the metadata match the request on the Subject field
        constraint = new QueryConstraintType("Subject = 'Salinity of the water column' AND Subject = 'CTD profilers'", "1.0.0");
        query      = new QueryType(TypeNames.ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), sort, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(3, response.getSearchResults().getAny().size());

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("39727_22_19750113062500");
        expResult.add("40510_145_19930221211500");
        expResult.add("42292_9s_19900610041000");

        assertEquals(expResult, results);


        // we update the metadata 42292_9s_1990061004100 by replacing the abstract field from "Salinity of the water column" to "something".
        constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.1.0");
        properties = new ArrayList<RecordPropertyType>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/identificationInfo/descriptiveKeywords[1]/keyword[7]", "something"));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);


        // we verify that the metadata does not appears anymore in the precedent getRecords request
        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(2, response.getSearchResults().getAny().size());

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("39727_22_19750113062500");
        expResult.add("40510_145_19930221211500");

        assertEquals(expResult, results);

        // then we verify that the modified metadata is well modified and indexed
        constraint = new QueryConstraintType("Subject = 'something' AND Subject = 'CTD profilers'", "1.0.0");
        query      = new QueryType(TypeNames.ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "application/xml", "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(1 , response.getSearchResults().getAny().size());

        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetaData meta = (DefaultMetaData) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("42292_9s_19900610041000");

        assertEquals(expResult, results);

         /*
         *  TEST 11 : we try to update the metadata 42292_9s_1990061004100 by replacing a numeroted single Property
         *  we try to replace the property MD_Metadata.identificationInfo.abstract[2] but abstract is not a list so we must receive an exception
         */

        // we try to update the metadata 42292_9s_1990061004100 by replacing the abstract field with "wathever".
        constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.1.0");
        properties = new ArrayList<RecordPropertyType>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/identificationInfo/abstract[2]", "whatever"));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);

        exe = null;
        try {
            result     = worker.transaction(request);
        } catch (CstlServiceException ex) {
            exe = ex;
        }

        assertTrue(exe != null);
        assertEquals("The property:abstract in the class:class org.geotoolkit.metadata.iso.identification.DefaultDataIdentification is not a collection", exe.getMessage());

        /*
         *  TEST 12 : we try to update the metadata 42292_9s_1990061004100 by replacing a numeroted single Property
         *  we try to replace the property MD_Metadata.distributionInfo[3]/distributionFormat/name but distributionInfo is not a list so we must receive an exception
         */

        // we try to update the metadata 42292_9s_1990061004100 by replacing the name field with "wathever".
        constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.1.0");
        properties = new ArrayList<RecordPropertyType>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/distributionInfo[3]/distributionFormat/name", "whatever"));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);

        exe = null;
        try {
            result     = worker.transaction(request);
        } catch (CstlServiceException ex) {
            exe = ex;
        }

        assertTrue(exe != null);
        assertEquals("The property:distributionInfo in the class:class org.geotoolkit.metadata.iso.DefaultMetaData is not a collection", exe.getMessage());


        /*
         *  TEST 13 : we update the metadata 42292_9s_1990061004100 by replacing a numeroted complex Property
         *  we replace the property MD_Metadata.metadataExtensionInfo.extendedElementInformation[3] with a new MD_ExtendedElementInformation.
         */

        // we update the metadata 42292_9s_1990061004100 by replacing the abstract field from "Salinity of the water column" to "something".
        constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.1.0");
        properties = new ArrayList<RecordPropertyType>();
        DefaultExtendedElementInformation ext = new DefaultExtendedElementInformation("extendedName",
                                                                                new SimpleInternationalString("some definition"),
                                                                                new SimpleInternationalString("some condition"),
                                                                                Datatype.ABSTRACT_CLASS, null, null, null);
        
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/metadataExtensionInfo/extendedElementInformation[3]", ext));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);

        // then we must be sure that the metadata is modified
        requestGRBI = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "application/xml", "http://www.isotc211.org/2005/gmd", Arrays.asList("42292_9s_19900610041000"));
        GRresult = (GetRecordByIdResponseType) worker.getRecordById(requestGRBI);

        assertTrue(GRresult != null);
        assertTrue(GRresult.getAbstractRecord().size() == 0);
        assertTrue(GRresult.getAny().size() == 1);
        obj = GRresult.getAny().get(0);
        assertTrue(obj instanceof DefaultMetaData);

        isoResult = (DefaultMetaData) obj;
        DefaultExtendedElementInformation extResult = null;
        boolean removed = true;
        for (ExtendedElementInformation ex : isoResult.getMetadataExtensionInfo().iterator().next().getExtendedElementInformation()) {
            if (ex.getName().equals("extendedName")) {
                extResult = (DefaultExtendedElementInformation) ex;
            } else if (ex.getName().equals("SDN:L031:2:")) {
                removed = false;
            }
        }

        assertEquals(ext, extResult);
        assertTrue(removed);


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
