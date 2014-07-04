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

// J2SE dependencies

import org.apache.sis.metadata.iso.DefaultExtendedElementInformation;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.test.XMLComparator;
import org.apache.sis.util.ComparisonMode;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.xml.Namespaces;
import org.apache.sis.xml.XML;
import org.constellation.util.NodeUtilities;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.geotoolkit.csw.xml.AbstractCapabilities;
import org.geotoolkit.csw.xml.DescribeRecordResponse;
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.GetDomainResponse;
import org.geotoolkit.csw.xml.GetRecordByIdResponse;
import org.geotoolkit.csw.xml.ResultType;
import org.geotoolkit.csw.xml.TransactionResponse;
import org.geotoolkit.csw.xml.v202.AcknowledgementType;
import org.geotoolkit.csw.xml.v202.BriefRecordType;
import org.geotoolkit.csw.xml.v202.DeleteType;
import org.geotoolkit.csw.xml.v202.DescribeRecordType;
import org.geotoolkit.csw.xml.v202.DomainValuesType;
import org.geotoolkit.csw.xml.v202.ElementSetNameType;
import org.geotoolkit.csw.xml.v202.GetCapabilitiesType;
import org.geotoolkit.csw.xml.v202.GetDomainResponseType;
import org.geotoolkit.csw.xml.v202.GetDomainType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdType;
import org.geotoolkit.csw.xml.v202.GetRecordsResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordsType;
import org.geotoolkit.csw.xml.v202.InsertType;
import org.geotoolkit.csw.xml.v202.ListOfValuesType;
import org.geotoolkit.csw.xml.v202.QueryConstraintType;
import org.geotoolkit.csw.xml.v202.QueryType;
import org.geotoolkit.csw.xml.v202.RecordPropertyType;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.csw.xml.v202.SummaryRecordType;
import org.geotoolkit.csw.xml.v202.TransactionType;
import org.geotoolkit.csw.xml.v202.UpdateType;
import org.geotoolkit.ebrim.xml.v250.ExtrinsicObjectType;
import org.geotoolkit.ebrim.xml.v300.RegistryPackageType;
import org.geotoolkit.metadata.iso.citation.DefaultResponsibleParty;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.LiteralType;
import org.geotoolkit.ogc.xml.v110.PropertyIsEqualToType;
import org.geotoolkit.ogc.xml.v110.PropertyIsLessThanOrEqualToType;
import org.geotoolkit.ogc.xml.v110.PropertyNameType;
import org.geotoolkit.ogc.xml.v110.SortByType;
import org.geotoolkit.ogc.xml.v110.SortOrderType;
import org.geotoolkit.ogc.xml.v110.SortPropertyType;
import org.geotoolkit.ows.xml.v100.AcceptFormatsType;
import org.geotoolkit.ows.xml.v100.AcceptVersionsType;
import org.geotoolkit.ows.xml.v100.SectionsType;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.geotoolkit.xml.AnchoredMarshallerPool;
import org.junit.Ignore;
import org.opengis.metadata.Datatype;
import org.opengis.metadata.ExtendedElementInformation;
import org.opengis.metadata.citation.Role;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.metadata.CSWConstants.OUTPUT_SCHEMA;
import static org.constellation.metadata.CSWConstants.PARAMETERNAME;
import static org.constellation.metadata.CSWConstants.TYPENAMES;
import static org.constellation.test.utils.MetadataUtilities.ebrimEquals;
import static org.constellation.test.utils.MetadataUtilities.metadataEquals;
import static org.geotoolkit.csw.xml.TypeNames.CAPABILITIES_QNAME;
import static org.geotoolkit.csw.xml.TypeNames.EXTRINSIC_OBJECT_25_QNAME;
import static org.geotoolkit.csw.xml.TypeNames.EXTRINSIC_OBJECT_QNAME;
import static org.geotoolkit.csw.xml.TypeNames.ISO_TYPE_NAMES;
import static org.geotoolkit.csw.xml.TypeNames.METADATA_QNAME;
import static org.geotoolkit.csw.xml.TypeNames.RECORD_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Date_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Format_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Identifier_QNAME;
import static org.geotoolkit.dublincore.xml.v2.elements.ObjectFactory._Subject_QNAME;
import static org.geotoolkit.dublincore.xml.v2.terms.ObjectFactory._Modified_QNAME;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.VERSION_NEGOTIATION_FAILED;
import static org.geotoolkit.ows.xml.v100.ObjectFactory._BoundingBox_QNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// JAXB dependencies
// constellation dependencies
// geotoolkit dependencies
// GeoAPI dependencies
// JUnit dependencies

/**
 * Test the different methods of CSWWorker with a FileSystem reader/writer.
 *
 * @author Guilhem Legal (geomatys)
 */
@Ignore
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
public class CSWworkerTest implements ApplicationContextAware {

    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected static CSWworker worker;

    protected static MarshallerPool pool;

    protected static final Logger LOGGER = Logging.getLogger("org.constellation.metadata");

    protected boolean typeCheckUpdate = true;
    
    protected static boolean onlyIso = false;

    public static void fillPoolAnchor(AnchoredMarshallerPool pool) {
        try {
            pool.addAnchor("Common Data Index record", new URI("SDN:L231:3:CDI"));
            pool.addAnchor("France", new URI("SDN:C320:2:FR"));
            pool.addAnchor("EPSG:4326", new URI("SDN:L101:2:4326"));
            pool.addAnchor("2", new URI("SDN:C371:1:2"));
            pool.addAnchor("35", new URI("SDN:C371:1:35"));
            pool.addAnchor("Transmittance and attenuance of the water column", new URI("SDN:P021:35:ATTN"));
            pool.addAnchor("Electrical conductivity of the water column", new URI("SDN:P021:35:CNDC"));
            pool.addAnchor("Dissolved oxygen parameters in the water column", new URI("SDN:P021:35:DOXY"));
            pool.addAnchor("Light extinction and diffusion coefficients", new URI("SDN:P021:35:EXCO"));
            pool.addAnchor("Dissolved noble gas concentration parameters in the water column", new URI("SDN:P021:35:HEXC"));
            pool.addAnchor("Optical backscatter", new URI("SDN:P021:35:OPBS"));
            pool.addAnchor("Salinity of the water column", new URI("SDN:P021:35:PSAL"));
            pool.addAnchor("Dissolved concentration parameters for 'other' gases in the water column", new URI("SDN:P021:35:SCOX"));
            pool.addAnchor("Temperature of the water column", new URI("SDN:P021:35:TEMP"));
            pool.addAnchor("Visible waveband radiance and irradiance measurements in the atmosphere", new URI("SDN:P021:35:VSRA"));
            pool.addAnchor("Visible waveband radiance and irradiance measurements in the water column", new URI("SDN:P021:35:VSRW"));
            pool.addAnchor("MEDATLAS ASCII", new URI("SDN:L241:1:MEDATLAS"));
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IllegalStateException ex) {
            // this exception happen when we try to put 2 twice the same anchor.
            // for this test we call many times this method in a static instance (MarshallerPool)
            // so for now we do bnothing here
            // TODO find a way to call this only one time in the CSW test
        }
    }
    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    public void getCapabilitiesTest() throws Exception {

        /*
         *  TEST 1 : minimal getCapabilities
         */
        GetCapabilitiesType request = new GetCapabilitiesType("CSW");
        AbstractCapabilities result = worker.getCapabilities(request);

        assertTrue(result != null);
        assertTrue(result.getVersion().equals("2.0.2"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() != null);

        /*
         *  TEST 2 : full get capabilities
         */
        AcceptVersionsType acceptVersions = new AcceptVersionsType("2.0.2");
        SectionsType sections             = new SectionsType("All");
        AcceptFormatsType acceptFormats   = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, null, "CSW");

        result = worker.getCapabilities(request);

        assertTrue(result != null);
        assertTrue(result.getVersion().equals("2.0.2"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() != null);

        /*
         *  TEST 3 : get capabilities section Operation metadata
         */
        acceptVersions = new AcceptVersionsType("2.0.2");
        sections       = new SectionsType("OperationsMetadata");
        acceptFormats  = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request        = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, null, "CSW");

        result         = worker.getCapabilities(request);

        assertTrue(result != null);
        assertTrue(result.getVersion().equals("2.0.2"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() == null);

        /*
         *  TEST 4 : get capabilities section Service provider
         */
        acceptVersions = new AcceptVersionsType("2.0.2");
        sections       = new SectionsType("ServiceProvider");
        acceptFormats  = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, null, "CSW");

        result = worker.getCapabilities(request);

        assertTrue(result != null);
        assertTrue(result.getVersion().equals("2.0.2"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() != null);

        /*
         *  TEST 5 : get capabilities section Service Identification
         */
        acceptVersions = new AcceptVersionsType("2.0.2");
        sections       = new SectionsType("ServiceIdentification");
        acceptFormats  = new AcceptFormatsType(MimeType.APPLICATION_XML);
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, null, "CSW");

        result = worker.getCapabilities(request);

        assertTrue(result != null);
        assertTrue(result.getVersion().equals("2.0.2"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() == null);

        /*
         *  TEST 6 : get capabilities with wrong version (waiting for an exception)
         */
        acceptVersions = new AcceptVersionsType("2.0.4");
        sections       = new SectionsType("All");
        acceptFormats  = new AcceptFormatsType(MimeType.TEXT_XML);
        request = new GetCapabilitiesType(acceptVersions, sections, acceptFormats, null, "CSW");

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
    public void getRecordByIdTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        /*
         *  TEST 1 : getRecordById with the first metadata in ISO mode.
         */
        GetRecordByIdType request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", Arrays.asList("42292_5p_19900609195600"));
        GetRecordByIdResponseType result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAny().size() == 1);
        Object obj = result.getAny().get(0);
        if (obj instanceof DefaultMetadata) {
            DefaultMetadata isoResult = (DefaultMetadata) obj;
            DefaultMetadata ExpResult1 = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"));
            metadataEquals(ExpResult1, isoResult, ComparisonMode.BY_CONTRACT);
        } else if (obj instanceof Node) {
            Node resultNode = (Node) obj;
            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta1.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        } else {
            fail("unexpected record type:" + obj);
        }

        /*
         *  TEST 2 : getRecordById with the first metadata in DC mode (BRIEF).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.BRIEF),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAny().size() == 1);

        obj = result.getAny().get(0);
        if (obj instanceof BriefRecordType) {
            BriefRecordType briefResult =  (BriefRecordType) obj;
            BriefRecordType expBriefResult1 =  (BriefRecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1BDC.xml"));

            assertEquals(expBriefResult1, briefResult);
        } else {
            Node resultNode = (Node) obj;
            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta1BDC.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        }

        /*
         *  TEST 3 : getRecordById with the first metadata in DC mode (SUMMARY).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.SUMMARY),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAny().size() == 1);

        obj = result.getAny().get(0);

        if (obj instanceof SummaryRecordType) {
           SummaryRecordType sumResult =  (SummaryRecordType) obj;
           SummaryRecordType expSumResult1 =  (SummaryRecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1SDC.xml"));

           assertEquals(expSumResult1.getFormat(), sumResult.getFormat());
           assertEquals(expSumResult1, sumResult);
        } else {
            Node resultNode = (Node) obj;
            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta1SDC.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        }

        /*
         *  TEST 4 : getRecordById with the first metadata in DC mode (FULL).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAny().size() == 1);

        obj = result.getAny().get(0);

        if (obj instanceof RecordType) {
            RecordType recordResult = (RecordType) obj;
            RecordType expRecordResult1 = (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1FDC.xml"));

            assertEquals(expRecordResult1.getFormat(), recordResult.getFormat());
            assertEquals(expRecordResult1, recordResult);
        } else {
            Node resultNode = (Node) obj;
            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta1FDC.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        }

        /*
         *  TEST 5 : getRecordById with the a metadata in DC mode (FULL).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("39727_22_19750113062500"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAny().size() == 1);

        obj = result.getAny().get(0);

        if (obj instanceof RecordType) {
            RecordType recordResult = (RecordType) obj;
            RecordType expRecordResult3 =  (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta3FDC.xml"));

            assertEquals(expRecordResult3.getFormat(), recordResult.getFormat());
            assertEquals(expRecordResult3, recordResult);
        } else {
            Node resultNode = (Node) obj;
            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta3FDC.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        }

        /*
         *  TEST 6 : getRecordById with two metadata in DC mode (FULL).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600","42292_9s_19900610041000"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAny().size() == 2);

        obj = result.getAny().get(0);

        if (obj instanceof RecordType) {

            RecordType recordResult1 = (RecordType) obj;
            RecordType expRecordResult1 = (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1FDC.xml"));
            assertEquals(expRecordResult1, recordResult1);

            RecordType recordResult2 = (RecordType) result.getAny().get(1);
            RecordType expRecordResult2 =  (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta2FDC.xml"));
            assertEquals(expRecordResult2, recordResult2);

        } else {
            Node resultNode = (Node) obj;
            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta1FDC.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();

            resultNode = (Node)result.getAny().get(1);
            expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta2FDC.xml");
            comparator = new XMLComparator(expResultNode, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        }


        /*
         *  TEST 7 : getRecordById with the first metadata with no outputSchema.
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.SUMMARY),
                MimeType.APPLICATION_XML, null, Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAny().size() == 1);

        obj = result.getAny().get(0);

        if (obj instanceof SummaryRecordType) {
            SummaryRecordType sumResult = (SummaryRecordType) obj;
            SummaryRecordType expSumResult1 = (SummaryRecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1SDC.xml"));

            assertEquals(expSumResult1.getFormat(), sumResult.getFormat());
            assertEquals(expSumResult1, sumResult);
        } else {
            Node resultNode = (Node) obj;
            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta1SDC.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        }

        /*
         *  TEST 8 : getRecordById with the first metadata with no outputSchema and no ElementSetName.
         */
        request = new GetRecordByIdType("CSW", "2.0.2", null,
                MimeType.APPLICATION_XML, null, Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAny().size() == 1);

        obj = result.getAny().get(0);

         if (obj instanceof SummaryRecordType) {
            SummaryRecordType sumResult = (SummaryRecordType) obj;
            SummaryRecordType expSumResult1 = (SummaryRecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1SDC.xml"));

            assertEquals(expSumResult1.getFormat(), sumResult.getFormat());
            assertEquals(expSumResult1, sumResult);
        } else {
            Node resultNode = (Node) obj;
            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta1SDC.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        }

        /*
         *  TEST 9 : getRecordById with ebrim 2.5 etadata.
         */
        if (!onlyIso) {
            request = new GetRecordByIdType("CSW", "2.0.2", null,
                    MimeType.APPLICATION_XML, "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", Arrays.asList("000068C3-3B49-C671-89CF-10A39BB1B652"));
            result = (GetRecordByIdResponseType) worker.getRecordById(request);

            assertTrue(result != null);
            assertTrue(result.getAny().size() == 1);

            obj = result.getAny().get(0);

            if (obj instanceof ExtrinsicObjectType) {
                ExtrinsicObjectType eoResult =  (ExtrinsicObjectType) obj;
                ExtrinsicObjectType expEoResult =  ((JAXBElement<ExtrinsicObjectType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim1.xml"))).getValue();

                assertEquals(expEoResult, eoResult);
            } else {
                Node resultNode = (Node) obj;
                Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/ebrim1.xml");
                XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
                comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
                comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
                comparator.compare();
            }

            /*
             *  TEST 10 : getRecordById with ebrim 3.0 metadata.
             */
            request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                    MimeType.APPLICATION_XML, "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", Arrays.asList("urn:motiive:csw-ebrim"));
            result = (GetRecordByIdResponseType) worker.getRecordById(request);

            assertTrue(result != null);
            assertTrue(result.getAny().size() == 1);

            obj = result.getAny().get(0);

            if (obj instanceof RegistryPackageType) {
                RegistryPackageType rpResult =  (RegistryPackageType) obj;

                RegistryPackageType expRpResult =  ((JAXBElement<RegistryPackageType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim3.xml"))).getValue();

                ebrimEquals(expRpResult, rpResult);
            } else {
                Node resultNode = (Node) obj;
                Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/ebrim3.xml");
                XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
                comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
                comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
                comparator.compare();
            }

            /*
             *  TEST 11 : getRecordById with native DC metadata.
             */
            request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                    MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("urn:uuid:1ef30a8b-876d-4828-9246-dcbbyyiioo"));
            result = (GetRecordByIdResponseType) worker.getRecordById(request);

            assertTrue(result != null);
            assertTrue(result.getAny().size() == 1);

            obj = result.getAny().get(0);

            if (obj instanceof RecordType) {
                RecordType dcResult =  (RecordType) obj;
                RecordType dcexpResult =  (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta13.xml"));
                assertEquals(dcexpResult, dcResult);
            } else if (obj instanceof Node) {
                Node resultNode = (Node) obj;
                Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta13.xml");
                XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
                comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
                comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
                comparator.compare();
            } else {
                fail("unexpected record type:" + obj);
            }

            /*
             *  TEST 12 : getRecordById with native DC metadata applying a ElementSet Summary.
             */
            request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.SUMMARY),
                    MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("urn:uuid:1ef30a8b-876d-4828-9246-dcbbyyiioo"));
            result = (GetRecordByIdResponseType) worker.getRecordById(request);

            assertTrue(result != null);
            assertTrue(result.getAny().size() == 1);

            obj = result.getAny().get(0);

            if (obj instanceof SummaryRecordType) {
                SummaryRecordType dcResult =  (SummaryRecordType) obj;
                SummaryRecordType dcexpResult =  (SummaryRecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta13SDC.xml"));
                assertEquals(dcexpResult, dcResult);
            } else if (obj instanceof Node) {
                Node resultNode = (Node) obj;
                Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta13SDC.xml");
                XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
                comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
                comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
                comparator.compare();
            } else {
                fail("unexpected record type:" + obj);
            }
        }

        pool.recycle(unmarshaller);
    }

    public void getRecordByIdErrorTest() throws Exception {

        /*
         *  TEST 1 : getRecordById with no identifier (waiting an exception).
         */
         GetRecordByIdType request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", null);
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
         *  TEST 2 : getRecordById with an unvalid identifier (waiting an exception).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2",Arrays.asList("whatever"));
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
         *  TEST 3 : getRecordById with an unvalid outputSchema (waiting an exception).
         */
        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.opengis.net/whatever",Arrays.asList("42292_5p_19900609195600"));
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
         *  TEST 4 : getRecordById with an unvalid outputFormat (waiting an exception).
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
    }

    /**
     * Tests the getRecords method
     *
     * @throws java.lang.Exception
     */
    public void getRecordsTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        /*
         *  TEST 1 : getRecords with HITS - DC mode (FULL) - CQL text: Title LIKE 90008411%
         */

        List<QName> typeNames             = Arrays.asList(RECORD_QNAME);
        ElementSetNameType elementSetName = new ElementSetNameType(ElementSetType.FULL);
        SortByType sortBy                 = null;
        QueryConstraintType constraint    = new QueryConstraintType("Title LIKE '90008411%'", "1.0.0");
        QueryType query = new QueryType(typeNames, elementSetName, sortBy, constraint);
        GetRecordsType request = new GetRecordsType("CSW", "2.0.2", ResultType.HITS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        GetRecordsResponseType result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAny().isEmpty());
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 2);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 0);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        /*
         *  TEST 2 : getRecords with RESULTS - DC mode (FULL) - CQL text: Title LIKE 90008411%
         */

        typeNames      = Arrays.asList(RECORD_QNAME);
        elementSetName = new ElementSetNameType(ElementSetType.FULL);
        sortBy         = null;
        constraint     = new QueryConstraintType("Title LIKE '90008411%'", "1.0.0");
        query          = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request        = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAny().size() == 2);
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 2);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 2);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        Object obj = result.getSearchResults().getAny().get(0);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }

        if (obj instanceof RecordType) {
            RecordType recordResult1 = (RecordType) obj;

            obj = result.getSearchResults().getAny().get(1);
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

            RecordType expRecordResult1 =  (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1FDC.xml"));
            RecordType expRecordResult2 =  (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta2FDC.xml"));

            assertEquals(expRecordResult1, recordResult1);
            assertEquals(expRecordResult2, recordResult2);
        } else {
            Node resultNode1 = (Node) obj;
            Node resultNode2 = (Node) result.getSearchResults().getAny().get(1);

            final List<String> identifierValues = NodeUtilities.getValuesFromPath(resultNode1, "/csw:Record/dc:identifier");
            if (!identifierValues.get(0).equals("42292_5p_19900609195600")) {
                Node temp = resultNode1;
                resultNode1   = resultNode2;
                resultNode2   = temp;
            }

            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta1FDC.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, resultNode1);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();

            expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta2FDC.xml");
            comparator = new XMLComparator(expResultNode, resultNode2);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        }


        /*
         *  TEST 3 : getRecords with VALIDATE - DC mode (FULL) - CQL text: Title LIKE 90008411%
         */

        typeNames      = Arrays.asList(RECORD_QNAME);
        elementSetName = new ElementSetNameType(ElementSetType.FULL);
        sortBy         = null;
        constraint     = new QueryConstraintType("Title LIKE '90008411%'", "1.0.0");
        query          = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request        = new GetRecordsType("CSW", "2.0.2", ResultType.VALIDATE, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        assertTrue(worker.getRecords(request) instanceof AcknowledgementType);

        /*
         *  TEST 4 : getRecords with RESULTS - DC mode (BRIEF) - CQL text: Title LIKE 90008411%
         */

        typeNames      = Arrays.asList(RECORD_QNAME);
        elementSetName = new ElementSetNameType(ElementSetType.BRIEF);
        sortBy         = null;
        constraint     = new QueryConstraintType("Title LIKE '90008411%'", "1.0.0");
        query          = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request        = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAny().size() == 2);
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.BRIEF));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 2);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 2);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        obj = result.getSearchResults().getAny().get(0);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }

        if (obj instanceof BriefRecordType) {
            BriefRecordType briefResult1 = (BriefRecordType) obj;

            obj = result.getSearchResults().getAny().get(1);
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

            BriefRecordType expBriefResult1 =  (BriefRecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1BDC.xml"));
            BriefRecordType expBriefResult2 =  (BriefRecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta2BDC.xml"));

            assertEquals(expBriefResult1, briefResult1);
            assertEquals(expBriefResult2, briefResult2);
        } else {
            Node resultNode1 = (Node) obj;
            Node resultNode2 = (Node) result.getSearchResults().getAny().get(1);

            final List<String> identifierValues = NodeUtilities.getValuesFromPath(resultNode1, "/csw:BriefRecord/dc:identifier");
            if (!identifierValues.get(0).equals("42292_5p_19900609195600")) {
                Node temp = resultNode1;
                resultNode1   = resultNode2;
                resultNode2   = temp;
            }

            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta1BDC.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, resultNode1);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();

            expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta2BDC.xml");
            comparator = new XMLComparator(expResultNode, resultNode2);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        }

        /*
         *  TEST 5 : getRecords with RESULTS - DC mode (Custom) - CQL text: Title LIKE 90008411%
         */
        LOGGER.finer("TEST - 5 begin");

        typeNames        = Arrays.asList(RECORD_QNAME);
        List<QName> cust = new ArrayList<>();
        cust.add(_Identifier_QNAME);
        cust.add(_Subject_QNAME);
        cust.add(_Date_QNAME);
        cust.add(_Format_QNAME);
        sortBy           = null;
        constraint       = new QueryConstraintType("Title LIKE '90008411%'", "1.0.0");
        query            = new QueryType(typeNames, cust, sortBy, constraint);
        request          = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAny().size() == 2);
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 2);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 2);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        obj = result.getSearchResults().getAny().get(0);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }

        if (obj instanceof RecordType) {
            RecordType customResult1 = (RecordType) obj;

            obj = result.getSearchResults().getAny().get(1);
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

            RecordType expCustomResult1 =  (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1CustomDC.xml"));
            RecordType expCustomResult2 =  (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta2CustomDC.xml"));

            assertEquals(expCustomResult1, customResult1);
            assertEquals(expCustomResult2, customResult2);
        } else {
            Node resultNode1 = (Node) obj;
            Node resultNode2 = (Node) result.getSearchResults().getAny().get(1);

            final List<String> identifierValues = NodeUtilities.getValuesFromPath(resultNode1, "/csw:Record/dc:identifier");
            if (!identifierValues.get(0).equals("42292_5p_19900609195600")) {
                Node temp = resultNode1;
                resultNode1   = resultNode2;
                resultNode2   = temp;
            }

            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta1CustomDC.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, resultNode1);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();

            expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta2CustomDC.xml");
            comparator = new XMLComparator(expResultNode, resultNode2);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        }

        /*
         *  TEST 6 : getRecords with RESULTS - DC mode (Custom) - CQL text: Title LIKE 90008411%
         */

        typeNames        = Arrays.asList(RECORD_QNAME);
        cust             = new ArrayList<>();
        cust.add(_BoundingBox_QNAME);
        cust.add(_Modified_QNAME);
        cust.add(_Identifier_QNAME);
        sortBy           = null;
        constraint       = new QueryConstraintType("Title LIKE '90008411%'", "1.0.0");
        query            = new QueryType(typeNames, cust, sortBy, constraint);
        request          = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAny().size() == 2);
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 2);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 2);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        obj = result.getSearchResults().getAny().get(0);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }

        if (obj instanceof RecordType) {
            RecordType customResult1 = (RecordType) obj;

            obj = result.getSearchResults().getAny().get(1);
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

            RecordType expCustomResult1 =  (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1CustomDC2.xml"));
            RecordType expCustomResult2 =  (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta2CustomDC2.xml"));

            assertEquals(expCustomResult1, customResult1);
            assertEquals(expCustomResult2, customResult2);
        } else {
            Node resultNode1 = (Node) obj;
            Node resultNode2 = (Node) result.getSearchResults().getAny().get(1);

            final List<String> identifierValues = NodeUtilities.getValuesFromPath(resultNode1, "/csw:Record/dc:identifier");
            if (!identifierValues.get(0).equals("42292_5p_19900609195600")) {
                Node temp = resultNode1;
                resultNode1   = resultNode2;
                resultNode2   = temp;
            }

            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta1CustomDC2.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, resultNode1);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();

            expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta2CustomDC2.xml");
            comparator = new XMLComparator(expResultNode, resultNode2);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        }

        /*
         *  TEST 7 : getRecords with RESULTS - DC mode (Custom) - CQL text: Modified BETWEEN 2009-01-10 AND 2009-01-30
         */

        typeNames        = Arrays.asList(RECORD_QNAME);
        cust             = new ArrayList<>();
        cust.add(_Modified_QNAME);
        cust.add(_Identifier_QNAME);
        cust.add(_BoundingBox_QNAME);
        sortBy           = null;
        constraint       = new QueryConstraintType("Modified BETWEEN '2009-01-10' AND '2009-01-30'", "1.0.0");
        query            = new QueryType(typeNames, cust, sortBy, constraint);
        request          = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 20, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        assertEquals(3, result.getSearchResults().getAny().size());
        assertEquals(3, result.getSearchResults().getNumberOfRecordsMatched());
        assertEquals(3, result.getSearchResults().getNumberOfRecordsReturned());
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        if (result.getSearchResults().getAny().get(0) instanceof RecordType) {
            RecordType customResult2 = null;
            RecordType customResult3 = null;
            RecordType customResult4 = null;

            List<Object> records = result.getSearchResults().getAny();
            for (Object rec : records) {

                assertTrue(rec instanceof RecordType);
                RecordType r = (RecordType)rec;
                switch (r.getIdentifier().getContent().get(0)) {
                    case "42292_9s_19900610041000":
                        customResult2 = r;
                        break;
                    case "39727_22_19750113062500":
                        customResult3 = r;
                        break;
                    case "11325_158_19640418141800":
                        customResult4 = r;
                        break;
                    default:
                        fail("unexpected metadata:" + r.getIdentifier().getContent().get(0));
                        break;
                }
            }

            RecordType expCustomResult2 =  (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta2CustomDC2.xml"));
            RecordType expCustomResult3 =  (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta3CustomDC.xml"));
            RecordType expCustomResult4 =  (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta4CustomDC.xml"));

            assertEquals(expCustomResult2, customResult2);
            assertEquals(expCustomResult3, customResult3);
            assertEquals(expCustomResult4, customResult4);
        } else {

            Node customResult2 = null;
            Node customResult3 = null;
            Node customResult4 = null;

            List<Object> records = result.getSearchResults().getAny();
            for (Object rec : records) {

                Node r = (Node)rec;
                switch (NodeUtilities.getValuesFromPath(r, "/csw:Record/dc:identifier").get(0)) {
                    case "42292_9s_19900610041000":
                        customResult2 = r;
                        break;
                    case "39727_22_19750113062500":
                        customResult3 = r;
                        break;
                    case "11325_158_19640418141800":
                        customResult4 = r;
                        break;
                    default:
                        fail("unexpected metadata:" + NodeUtilities.getValuesFromPath(r, "/csw:Record/dc:identifier").get(0));
                        break;
                }
            }


            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta2CustomDC2.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, customResult2);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();

            expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta3CustomDC.xml");
            comparator = new XMLComparator(expResultNode, customResult3);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();

            expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta4CustomDC.xml");
            comparator = new XMLComparator(expResultNode, customResult4);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        }


        /*
         *  TEST 8 : getRecords with HITS - DC mode (FULL) - CQL text: identifier LIKE %42292_9s%
         */

        typeNames             = Arrays.asList(RECORD_QNAME);
        elementSetName = new ElementSetNameType(ElementSetType.FULL);
        sortBy                 = null;
        constraint    = new QueryConstraintType("identifier LIKE '%42292_9s%'", "1.0.0");
        query = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request = new GetRecordsType("CSW", "2.0.2", ResultType.HITS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAny().isEmpty());
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 1);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 0);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        /*
         *  TEST 8 : getRecords with HITS - DC mode (FULL) - CQL text: identifier LIKE %42292_9s%
         */

        typeNames             = Arrays.asList(RECORD_QNAME);
        elementSetName = new ElementSetNameType(ElementSetType.FULL);
        sortBy                 = null;
        constraint    = new QueryConstraintType("identifier LIKE '%2292_9s_19900%'", "1.0.0");
        query = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request = new GetRecordsType("CSW", "2.0.2", ResultType.HITS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAny().isEmpty());
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 1);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 0);
        assertTrue(result.getSearchResults().getNextRecord() == 0);


        /*
         *  TEST 9 : getRecords with HITS - DC mode (FULL) - CQL text: DWITHIN(geometry, POINT(1 2), 10, kilometers)
         */

        typeNames      = Arrays.asList(RECORD_QNAME);
        elementSetName = new ElementSetNameType(ElementSetType.FULL);
        sortBy         = null;
        constraint    = new QueryConstraintType("DWITHIN(geometry, POINT(1 2), 10, kilometers)", "1.0.0");
        query = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request = new GetRecordsType("CSW", "2.0.2", ResultType.HITS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAny().isEmpty());
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 0);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 0);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        pool.recycle(unmarshaller);
    }

     /**
     * Tests the getRecords method
     *
     * @throws java.lang.Exception
     */
    public void getRecordsSpatialTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
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

        /*
         *  TEST 1 : getRecords with HITS - DC mode (FULL) - CQL text: BBOX
         */

        constraint    = new QueryConstraintType("BBOX(ows:BoundingBox, 13, 60, 18,69)", "1.0.0");
        query = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        if (!onlyIso) {
            result = (GetRecordsResponseType) worker.getRecords(request);

            assertTrue(result.getSearchResults() != null);
            assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
            assertEquals(1, result.getSearchResults().getAny().size());
            assertEquals(1, result.getSearchResults().getNumberOfRecordsMatched());
            assertEquals(1, result.getSearchResults().getNumberOfRecordsReturned());
            assertEquals(0, result.getSearchResults().getNextRecord());

            obj = result.getSearchResults().getAny().get(0);
            if (obj instanceof JAXBElement) {
                obj = ((JAXBElement) obj).getValue();
            }

            if (obj instanceof RecordType) {
                RecordType recordResult = (RecordType) obj;
                assertEquals(recordResult.getIdentifier().getContent().get(0), "urn:uuid:1ef30a8b-876d-4828-9246-dcbbyyiioo");
            } else {
                Node recordResult = (Node) obj;
                assertEquals(NodeUtilities.getValuesFromPath(recordResult, "/csw:Record/dc:identifier").get(0), "urn:uuid:1ef30a8b-876d-4828-9246-dcbbyyiioo");
            }
        }
        pool.recycle(unmarshaller);
    }

    /**
     * Tests the getRecords on ISO 19115-2 method
     *
     * @throws java.lang.Exception
     */
    public void getRecords191152Test() throws Exception {

        /*
         *  TEST 1 : getRecords with RESULT - DC mode (FULL) - CQL text: Instrument='Instrument 007'
         */
        List<QName> typeNames             = Arrays.asList(RECORD_QNAME);
        ElementSetNameType elementSetName = new ElementSetNameType(ElementSetType.FULL);
        SortByType sortBy                 = null;
        QueryConstraintType constraint    = new QueryConstraintType("Instrument='Instrument 007'", "1.0.0");
        QueryType query = new QueryType(typeNames, elementSetName, sortBy, constraint);
        GetRecordsType request = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        GetRecordsResponseType result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertEquals(1, result.getSearchResults().getAny().size());
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 1);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 1);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        Object obj = result.getSearchResults().getAny().get(0);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }

        if (obj instanceof RecordType) {
            RecordType recordResult = (RecordType) obj;
            assertEquals(recordResult.getIdentifier().getContent().get(0), "gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        } else {
            Node recordResult = (Node) obj;
            assertEquals(NodeUtilities.getValuesFromPath(recordResult, "/csw:Record/dc:identifier").get(0), "gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        }

        /*
         *  TEST 2 : getRecords with RESULTS - DC mode (FULL) - CQL text: Platform='Platform 007'
         */

        typeNames      = Arrays.asList(RECORD_QNAME);
        elementSetName = new ElementSetNameType(ElementSetType.FULL);
        sortBy         = null;
        constraint     = new QueryConstraintType("Platform='Platform 007'", "1.0.0");
        query          = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request        = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAny().size() == 1);
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 1);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 1);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        obj = result.getSearchResults().getAny().get(0);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }

        if (obj instanceof RecordType) {
            RecordType recordResult = (RecordType) obj;
            assertEquals(recordResult.getIdentifier().getContent().get(0), "gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        } else {
            Node recordResult = (Node) obj;
            assertEquals(NodeUtilities.getValuesFromPath(recordResult, "/csw:Record/dc:identifier").get(0), "gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        }

        /*
         *  TEST 3 : getRecords with RESULTS - DC mode (FULL) - CQL text: Operation='Earth Observing System'
         */

        typeNames      = Arrays.asList(RECORD_QNAME);
        elementSetName = new ElementSetNameType(ElementSetType.FULL);
        sortBy         = null;
        constraint     = new QueryConstraintType("Operation='Earth Observing System'", "1.0.0");
        query          = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request        = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);
        //assertTrue(result.getSearchResults().getRecordSchema().equals("http://www.opengis.net/cat/csw/2.0.2"));
        assertTrue(result.getSearchResults().getAny().size() == 1);
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 1);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 1);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        obj = result.getSearchResults().getAny().get(0);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }

        if (obj instanceof RecordType) {
            RecordType recordResult = (RecordType) obj;
            assertEquals(recordResult.getIdentifier().getContent().get(0), "gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        } else {
            Node recordResult = (Node) obj;
            assertEquals(NodeUtilities.getValuesFromPath(recordResult, "/csw:Record/dc:identifier").get(0), "gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        }

    }

    /**
     * Tests the getRecords method
     *
     * @throws java.lang.Exception
     */
    public void getRecordsEbrimTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        /*
         *  TEST 1 : getRecords with RESULTS- Ebrim mode (FULL) - Filter: rim:ExtrinsicObject/@stability = Static
         */

        QName t                           = new QName(EXTRINSIC_OBJECT_25_QNAME.getNamespaceURI(), EXTRINSIC_OBJECT_25_QNAME.getLocalPart(), "rim");
        List<QName> typeNames             = Arrays.asList(t);
        ElementSetNameType elementSetName = new ElementSetNameType(ElementSetType.FULL);
        SortByType sortBy                 = null;
        PropertyIsEqualToType propEq      = new PropertyIsEqualToType(new LiteralType("Static"), new PropertyNameType("rim:ExtrinsicObject/@stability"), Boolean.TRUE);
        FilterType filter                 = new FilterType(propEq);
        QueryConstraintType constraint    = new QueryConstraintType(filter, "1.0.0");
        QueryType query = new QueryType(typeNames, elementSetName, sortBy, constraint);
        GetRecordsType request = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", 1, 5, query, null);

        GetRecordsResponseType result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);

        assertTrue(result.getSearchResults().getAny().size() == 1);
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 1);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 1);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        Object obj = result.getSearchResults().getAny().get(0);

        if (obj instanceof Node) {
             Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/ebrim2.xml");
             Node resultNode = (Node) obj;

            XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();

        } else {
            assertTrue(obj instanceof ExtrinsicObjectType);
            ExtrinsicObjectType eoResult =  (ExtrinsicObjectType) obj;
            ExtrinsicObjectType expEoResult =  ((JAXBElement<ExtrinsicObjectType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/ebrim2.xml"))).getValue();
            assertEquals(eoResult, expEoResult);
        }
        
        /*
         *  TEST 2 : getRecords with RESULTS- Ebrim mode (FULL) - Filter: rim:ExtrinsicObject/@minorVersion <= 1
         */

        PropertyIsLessThanOrEqualToType propLe = new PropertyIsLessThanOrEqualToType(new LiteralType("1"), new PropertyNameType("rim:ExtrinsicObject/@minorVersion"), Boolean.TRUE);
        filter                                 = new FilterType(propLe);
        constraint                             = new QueryConstraintType(filter, "1.0.0");
        query                                  = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request                               = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5", 1, 5, query, null);

        result = (GetRecordsResponseType) worker.getRecords(request);

        assertTrue(result.getSearchResults() != null);

        assertTrue(result.getSearchResults().getAny().size() == 2);
        assertTrue(result.getSearchResults().getElementSet().equals(ElementSetType.FULL));
        assertTrue(result.getSearchResults().getNumberOfRecordsMatched() == 2);
        assertTrue(result.getSearchResults().getNumberOfRecordsReturned() == 2);
        assertTrue(result.getSearchResults().getNextRecord() == 0);

        pool.recycle(unmarshaller);
    }

    public void getRecordsErrorTest() throws Exception {

        /*
         * Test 1 : getRecord with bad outputFormat
         */
        ElementSetNameType elementSetName = new ElementSetNameType(ElementSetType.FULL);
        List<QName> typeNames           = Arrays.asList(RECORD_QNAME);
        SortByType sortBy               = null;
        QueryConstraintType constraint  = new QueryConstraintType("Title LIKE '90008411%'", "1.0.0");
        QueryType query                 = new QueryType(typeNames, elementSetName, sortBy, constraint);
        GetRecordsType request          = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, "something", "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        boolean exLaunched = false;
        try {
            worker.getRecords(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(), "outputFormat");
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

        /*
         * Test 2 : getRecord with no typeNames
         */
        sortBy           = null;
        constraint       = new QueryConstraintType("Title LIKE '90008411%'", "1.0.0");
        query            = new QueryType(null, elementSetName, sortBy, constraint);
        request          = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        exLaunched = false;
        try {
            worker.getRecords(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(), TYPENAMES);
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

        /*
         * Test 3 : getRecord with bad typeNames
         */
        typeNames        = Arrays.asList(new QName("http://www.badnamespace.com", "something"));
        sortBy           = null;
        constraint       = new QueryConstraintType("Title LIKE '90008411%'", "1.0.0");
        query            = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request          = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, query, null);

        exLaunched = false;
        try {
            worker.getRecords(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(), TYPENAMES);
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

         /*
         * Test 4 : getRecord with bad outputSchema
         */
        typeNames        = Arrays.asList(RECORD_QNAME);
        sortBy           = null;
        constraint       = new QueryConstraintType("Title LIKE '90008411%'", "1.0.0");
        query            = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request          = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/4.5.8", 1, 5, query, null);

        exLaunched = false;
        try {
            worker.getRecords(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(), OUTPUT_SCHEMA);
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

         /*
         * Test 5 : getRecord with no query
         */
        request          = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 1, 5, null, null);

        exLaunched = false;
        try {
            worker.getRecords(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(), "Query");
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

        /*
         * Test 7 : getRecord with bad start position
         */
        typeNames        = Arrays.asList(RECORD_QNAME);
        sortBy           = null;
        constraint       = new QueryConstraintType("Title LIKE '90008411%'", "1.0.0");
        query            = new QueryType(typeNames, elementSetName, sortBy, constraint);
        request          = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", 0, 5, query, null);

        exLaunched = false;
        try {
            worker.getRecords(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(), "startPosition");
        }
        assertTrue(exLaunched);
    }

    /**
     * Tests the getDomain method
     *
     * @throws java.lang.Exception
     */
    public void getDomainTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        /*
         *  TEST 1 : getDomain 2.0.2 parameterName = GetCapabilities.sections
         */
        GetDomainType request = new GetDomainType("CSW", "2.0.2", null, "GetCapabilities.sections");

        GetDomainResponse result = worker.getDomain(request);

        assertTrue(result instanceof GetDomainResponseType);

        List<DomainValues> domainValues = new ArrayList<>();
        ListOfValuesType values = new  ListOfValuesType(Arrays.asList("All", "ServiceIdentification", "ServiceProvider", "OperationsMetadata", "Filter_Capabilities"));
        DomainValuesType value  = new DomainValuesType("GetCapabilities.sections", null, values, CAPABILITIES_QNAME);
        domainValues.add(value);
        GetDomainResponse expResult = new GetDomainResponseType(domainValues);

        assertEquals(expResult, result);


        /*
         *  TEST 2 : getDomain 2.0.0 parameterName = GetCapabilities.sections
         */
        org.geotoolkit.csw.xml.v200.GetDomainType request200 = new org.geotoolkit.csw.xml.v200.GetDomainType("CSW", "2.0.0", null, "GetCapabilities.sections");

        GetDomainResponse result200 = worker.getDomain(request200);

        assertTrue(result200 instanceof org.geotoolkit.csw.xml.v200.GetDomainResponseType);

        List<DomainValues> domainValues200 = new ArrayList<>();
        List<String> list = new ArrayList<>();
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

        /*
         *  TEST 3 : getDomain 2.0.2 propertyName = "identifier"
         */
        request = new GetDomainType("CSW", "2.0.2", "identifier", null);

        result = worker.getDomain(request);

        assertTrue(result instanceof GetDomainResponseType);

        domainValues = new ArrayList<>();
        list = new ArrayList<>();
        if (!onlyIso) {
            list.add("000068C3-3B49-C671-89CF-10A39BB1B652");
        }
        list.add("11325_158_19640418141800");
        list.add("39727_22_19750113062500");
        list.add("40510_145_19930221211500");
        list.add("42292_5p_19900609195600");
        list.add("42292_9s_19900610041000");
        list.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        list.add("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4");
        if (!onlyIso) {
            list.add("urn:motiive:csw-ebrim");
            list.add("urn:uuid:1ef30a8b-876d-4828-9246-dcbbyyiioo");
            list.add("urn:uuid:3e195454-42e8-11dd-8329-00e08157d076");
        }
        values = new ListOfValuesType(list);
        value  = new DomainValuesType(null, "identifier", values, METADATA_QNAME);
        domainValues.add(value);
        expResult = new GetDomainResponseType(domainValues);

        assertEquals(expResult, result);

        /*
         *  TEST 4 : getDomain 2.0.2 propertyName = "Identifier"
         */
        request = new GetDomainType("CSW", "2.0.2", "Identifier", null);

        result = worker.getDomain(request);

        assertTrue(result instanceof GetDomainResponseType);

        domainValues = new ArrayList<>();
        list = new ArrayList<>();
        // no ebrim list.add("000068C3-3B49-C671-89CF-10A39BB1B652");
        list.add("11325_158_19640418141800");
        list.add("39727_22_19750113062500");
        list.add("40510_145_19930221211500");
        list.add("42292_5p_19900609195600");
        list.add("42292_9s_19900610041000");
        list.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        list.add("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4");
        // no ebrim list.add("urn:uuid:3e195454-42e8-11dd-8329-00e08157d076");
        values = new ListOfValuesType(list);
        value  = new DomainValuesType(null, "Identifier", values, METADATA_QNAME);
        domainValues.add(value);
        expResult = new GetDomainResponseType(domainValues);

        assertEquals(expResult, result);

        /*
         *  TEST 5 : getDomain 2.0.2 propertyName = "title"
         */
        request = new GetDomainType("CSW", "2.0.2", "title", null);

        result = worker.getDomain(request);

        assertTrue(result instanceof GetDomainResponseType);

        domainValues = new ArrayList<>();
        list = new ArrayList<>();
        list.add("64061411.bot");
        list.add("75000111.ctd");
        list.add("90008411-2.ctd");
        list.add("90008411.ctd");
        list.add("92005711.ctd");
        if (!onlyIso) {
            list.add("Feature Type Catalogue Extension Package");
        }
        list.add("Sea surface temperature and history derived from an analysis of MODIS Level 3 data for the Gulf of Mexico");
        list.add("WMS Server for CORINE Land Cover France");
        if (!onlyIso) {
            list.add("dcbbyyiioo");
            list.add("ebrim1Title");
            list.add("ebrim2Title");
        }
        values = new ListOfValuesType(list);
        value  = new DomainValuesType(null, "title", values, METADATA_QNAME);
        domainValues.add(value);
        expResult = new GetDomainResponseType(domainValues);

        assertEquals(expResult, result);

        /*
         *  TEST 6 : getDomain 2.0.2 propertyName = "Title"
         */
        request = new GetDomainType("CSW", "2.0.2", "Title", null);

        result = worker.getDomain(request);

        assertTrue(result instanceof GetDomainResponseType);

        domainValues = new ArrayList<>();
        list = new ArrayList<>();
        list.add("64061411.bot");
        list.add("75000111.ctd");
        list.add("90008411-2.ctd");
        list.add("90008411.ctd");
        list.add("92005711.ctd");
        list.add("Sea surface temperature and history derived from an analysis of MODIS Level 3 data for the Gulf of Mexico");
        list.add("WMS Server for CORINE Land Cover France");
        values = new ListOfValuesType(list);
        value  = new DomainValuesType(null, "Title", values, METADATA_QNAME);
        domainValues.add(value);
        expResult = new GetDomainResponseType(domainValues);

        assertEquals(expResult, result);

        /*
         *  TEST 7 : getDomain 2.0.2 propertyName = "Identifier" and parameterName = GetCapabilities.sections => error

        request = new GetDomainType("CSW", "2.0.2", "Identifier", "GetCapabilities.sections");

        boolean exLaunched = false;
        try {
            result = worker.getDomain(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(), PARAMETERNAME);
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);
         */

        /*
         *  TEST 8 : getDomain 2.0.2 with no propertyName or parameterName
         */
        request = new GetDomainType("CSW", "2.0.2", null, null);

        boolean exLaunched = false;
        try {
            worker.getDomain(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(), "parameterName, propertyName");
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

        /*
         *  TEST 9 : getDomain 2.0.2 with a bad parameterName (missing '.')
         */
        request = new GetDomainType("CSW", "2.0.2", null, "GetCapabilities sections");

        exLaunched = false;
        try {
            worker.getDomain(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(), PARAMETERNAME);
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

        /*
         *  TEST 10 : getDomain 2.0.2 with a bad parameterName (bad parameter)
         */
        request = new GetDomainType("CSW", "2.0.2", null, "GetCapabilities.whatever");

        exLaunched = false;
        try {
            worker.getDomain(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(), PARAMETERNAME);
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

        /*
         *  TEST 11 : getDomain 2.0.2 with a bad parameterName (bad request name)
         */
        request = new GetDomainType("CSW", "2.0.2", null, "GetCapabilitos.sections");

        exLaunched = false;
        try {
            worker.getDomain(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(), PARAMETERNAME);
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
        assertTrue(exLaunched);

        pool.recycle(unmarshaller);

    }

    /**
     * Tests the transaction method
     *
     * @throws java.lang.Exception
     */
    public void DescribeRecordTest() throws Exception {

        /**
         * Test 1 : bad schema language
         */
        DescribeRecordType request = new DescribeRecordType("CSW", "2.0.2", Arrays.asList(RECORD_QNAME), "text/xml", "wathever");

        boolean exLaunched = false;
        try {
            worker.describeRecord(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "schemaLanguage");
        }

        assertTrue(exLaunched);

        /**
         * Test 2 : good request with no schema language
         */
        request = new DescribeRecordType("CSW", "2.0.2", Arrays.asList(RECORD_QNAME, METADATA_QNAME), "text/xml", null);
        DescribeRecordResponse result = worker.describeRecord(request);

        assertEquals(result.getSchemaComponent().size(), 2);
        assertEquals(result.getSchemaComponent().get(0).getTargetNamespace(), Namespaces.CSW);
        assertEquals(result.getSchemaComponent().get(1).getTargetNamespace(), Namespaces.GMD);

        /**
         * Test 2 : good request with ebrim QNames
         */
        request = new DescribeRecordType("CSW", "2.0.2", Arrays.asList(EXTRINSIC_OBJECT_25_QNAME, EXTRINSIC_OBJECT_QNAME), "text/xml", null);
        result = worker.describeRecord(request);

        assertEquals(result.getSchemaComponent().size(), 2);
        assertEquals(result.getSchemaComponent().get(0).getTargetNamespace(), "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
        assertEquals(result.getSchemaComponent().get(1).getTargetNamespace(), "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5");
    }

    /**
     * Tests the transaction method
     *
     * @throws java.lang.Exception
     */
    public void transactionDeleteInsertTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();

        LOGGER.finer("\n\n--- TRANSACTION DELETE TEST --- \n\n");

        /*
         *  TEST 1 : we delete the metadata 42292_5p_19900609195600
         */

        // first we must be sure that the metadata is present
        GetRecordByIdType requestGRBI = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", Arrays.asList("42292_5p_19900609195600"));
        GetRecordByIdResponseType GRresult = (GetRecordByIdResponseType) worker.getRecordById(requestGRBI);

        assertTrue(GRresult != null);
        assertTrue(GRresult.getAny().size() == 1);
        Object obj = GRresult.getAny().get(0);

        if (obj instanceof DefaultMetadata) {
            DefaultMetadata isoResult = (DefaultMetadata) obj;
            DefaultMetadata ExpResult1 = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"));
            metadataEquals(ExpResult1, isoResult, ComparisonMode.BY_CONTRACT);
        } else if (obj instanceof Node) {
            Node resultNode = (Node) obj;
            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/meta1.xml");
            XMLComparator comparator = new XMLComparator(expResultNode, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        } else {
            fail("unexpected record type:" + obj);
        }

        // we delete the metadata
        QueryConstraintType constraint = new QueryConstraintType("identifier='42292_5p_19900609195600'", "1.1.0");
        DeleteType delete = new DeleteType(null, constraint);
        TransactionType request = new TransactionType("CSW", "2.0.2", delete);

        TransactionResponse result = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalDeleted(), 1);

        // we try to request the deleted metadata
        CstlServiceException exe = null;
        try {
            worker.getRecordById(requestGRBI);
        } catch (CstlServiceException ex) {
            exe = ex;
        }

        // we must receive an exception saying that the metadata is not present.
        assertNotNull(exe);
        assertEquals(exe.getExceptionCode() , INVALID_PARAMETER_VALUE);
        assertEquals(exe.getLocator() , "id");


        LOGGER.finer("\n\n--- TRANSACTION INSERT TEST --- \n\n");

        unmarshaller = pool.acquireUnmarshaller();
        /*
         *  TEST 1 : we add the metadata 42292_5p_19900609195600
         */
        DefaultMetadata ExpResult1    = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"));
        Node original =  getOriginalMetadata("org/constellation/xml/metadata/meta1.xml");

        InsertType insert       = new InsertType(original);
        request = new TransactionType("CSW", "2.0.2", insert);
        result  = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalInserted(), 1);


        // then we must be sure that the metadata is present
        requestGRBI = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", Arrays.asList("42292_5p_19900609195600"));
        GRresult = (GetRecordByIdResponseType) worker.getRecordById(requestGRBI);

        assertTrue(GRresult != null);
        assertTrue(GRresult.getAny().size() == 1);
        obj = GRresult.getAny().get(0);

        if (obj instanceof DefaultMetadata) {
            DefaultMetadata isoResult = (DefaultMetadata) obj;
            metadataEquals(ExpResult1, isoResult, ComparisonMode.BY_CONTRACT);
        } else if (obj instanceof Node) {
            Node resultNode = (Node) obj;
            XMLComparator comparator = new XMLComparator(original, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        } else {
            fail("unexpected record type:" + obj);
        }

        /*
         *  TEST 2 : we add the metadata urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd (DC Record)
         */
        RecordType ExpResult2 = (RecordType) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta8.xml"));
        Node       oriExpResult2 = getOriginalMetadata("org/constellation/xml/metadata/meta8.xml");

        insert  = new InsertType(oriExpResult2);
        request = new TransactionType("CSW", "2.0.2", insert);
        result  = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalInserted(), 1);


        // then we must be sure that the metadata is present
        requestGRBI = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd"));
        GRresult = (GetRecordByIdResponseType) worker.getRecordById(requestGRBI);

        assertTrue(GRresult != null);
        assertEquals(1, GRresult.getAny().size());

        if (!(GRresult.getAny().get(0) instanceof Node)) {
            obj = GRresult.getAny().get(0);
            assertTrue(obj instanceof RecordType);

            RecordType dcResult =  (RecordType) obj;
            assertEquals(ExpResult2, dcResult);
        } else {
            obj = GRresult.getAny().get(0);
            assertTrue(obj instanceof Node);

            Node resultNode = (Node) obj;
            XMLComparator comparator = new XMLComparator(oriExpResult2, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.compare();
        }
        pool.recycle(unmarshaller);
    }

    /**
     * Tests the transaction method
     *
     * @throws java.lang.Exception
     */
    public void transactionUpdateTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();

        LOGGER.finer("\n\n--- TRANSACTION UPDATE TEST ---\n\n");
        /*
         *  TEST 1 : we update the metadata 42292_5p_19900609195600 by replacing it by another metadata
         */

        DefaultMetadata replacement     = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta6.xml"));
        Node replacementOriginal        = getOriginalMetadata("org/constellation/xml/metadata/meta6.xml");
        QueryConstraintType constraint  = new QueryConstraintType("identifier='42292_5p_19900609195600'", "1.1.0");
        UpdateType update               = new UpdateType(replacementOriginal, constraint);
        TransactionType request         = new TransactionType("CSW", "2.0.2", update);
        TransactionResponse result      = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);


        // we try to request the updated metadata
        CstlServiceException exe = null;
        try {
            GetRecordByIdType requestGRBI = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", Arrays.asList("42292_5p_19900609195600"));
             worker.getRecordById(requestGRBI);
        } catch (CstlServiceException ex) {
            exe = ex;
        }

        // we must receive an exception saying that the metadata is not present.
        assertNotNull(exe);
        assertEquals(exe.getExceptionCode() , INVALID_PARAMETER_VALUE);
        assertEquals(exe.getLocator() , "id");


        // then we must be sure that the replacement metadata is present
        GetRecordByIdType  requestGRBI = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", Arrays.asList("CTDF02"));
        GetRecordByIdResponseType GRresult = (GetRecordByIdResponseType) worker.getRecordById(requestGRBI);

        assertTrue(GRresult != null);
        assertTrue(GRresult.getAny().size() == 1);
        Object obj = GRresult.getAny().get(0);

        if (obj instanceof DefaultMetadata) {
            DefaultMetadata isoResult = (DefaultMetadata) obj;
            metadataEquals(replacement, isoResult);
        } else if (obj instanceof Node) {
            Node resultNode = (Node) obj;
            XMLComparator comparator = new XMLComparator(replacementOriginal, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.ignoredAttributes.add("codeList");
            comparator.compare();
        } else {
            fail("unexpected record type:" + obj);
        }


        /*
         *  TEST 2 : we update the metadata 11325_158_19640418141800 by replacing a single Property
         *  we replace the property MD_Metadata.language from en to fr.
         */

        // we perform a request to get the list of metadata matching language = en
        constraint        = new QueryConstraintType("Language = 'eng'", "1.0.0");
        SortPropertyType sp = new SortPropertyType("Identifier", SortOrderType.ASC);
        SortByType sort   = new SortByType(Arrays.asList(sp));
        QueryType query   = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), sort, constraint);
        GetRecordsType gr = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        GetRecordsResponseType response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(6, response.getSearchResults().getAny().size());

        List<String> results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        List<String> expResult = new ArrayList<>();
        expResult.add("11325_158_19640418141800");
        expResult.add("39727_22_19750113062500");
        expResult.add("40510_145_19930221211500");
        expResult.add("42292_9s_19900610041000");
        expResult.add("CTDF02");
        expResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        assertEquals(expResult, results);


        // we update the metadata 11325_158_19640418141800 by replacing the language eng by fr
        constraint = new QueryConstraintType("identifier='11325_158_19640418141800'", "1.1.0");
        List<RecordPropertyType> properties = new ArrayList<>();

        final Node languageNode = buildNode("http://www.isotc211.org/2005/gmd", "LanguageCode");
        final Node valueNode = languageNode.getOwnerDocument().createAttribute("codeListValue");
        valueNode.setNodeValue("fra");
        final Node clNode = languageNode.getOwnerDocument().createAttribute("codeList");
        clNode.setNodeValue("http://schemas.opengis.net/iso/19139/20070417/resources/Codelist/ML_gmxCodelists.xml#LanguageCode");
        languageNode.getAttributes().setNamedItem(valueNode);
        languageNode.getAttributes().setNamedItem(clNode);
        languageNode.setTextContent("French");

        properties.add(new RecordPropertyType("/gmd:MD_Metadata/language", languageNode));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);

        // we perform again the getRecord request the modified metadata must not appears in the list
        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(5, response.getSearchResults().getAny().size());

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("39727_22_19750113062500");
        expResult.add("40510_145_19930221211500");
        expResult.add("42292_9s_19900610041000");
        expResult.add("CTDF02");
        expResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");

        assertEquals(expResult, results);


         // we make a getRecords request with language=fr to verify that the modified metadata is well indexed
        constraint = new QueryConstraintType("Language = 'fra'", "1.0.0");
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(2, response.getSearchResults().getAny().size());

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4");
        expResult.add("11325_158_19640418141800");

        assertEquals(expResult, results);

        /*
         *  TEST 3 : we update the metadata 39727_22_19750113062500 by replacing a single Property
         *  we replace the property MD_Metadata.identificationInfo.abstract from "Donnees CTD ANGOLA CAP 7501 78" to "Modified datas by CSW-T".
         */

        // first we make a getRecords request to verify that the metadata match the request on the Abstract field
        constraint = new QueryConstraintType("Abstract = 'Donnees CTD ANGOLA CAP 7501 78'", "1.0.0");
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 1);

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("39727_22_19750113062500");

        assertEquals(expResult, results);


        // we update the metadata 11325_158_19640418141800 by replacing the abstract field from "Donnees CTD ANGOLA CAP 7501 78" to "Modified datas by CSW-T".
        constraint = new QueryConstraintType("identifier='39727_22_19750113062500'", "1.1.0");
        properties = new ArrayList<>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/identificationInfo/MD_DataIdentification/abstract/CharacterString", "Modified datas by CSW-T"));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);


        // we verify that the metadata does not appears anymore in the precedent getRecords request
        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().isEmpty());

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();

        assertEquals(expResult, results);

        // then we verify that the modified metadata is well modified and indexed
        constraint = new QueryConstraintType("Abstract = 'Modified datas by CSW-T'", "1.0.0");
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 1);

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("39727_22_19750113062500");

        assertEquals(expResult, results);

        /*
         *  TEST 4 : we update the metadata 39727_22_19750113062500 by replacing a single Property
         *  we replace the property MD_Metadata.dateStamp with "2009-03-31T12:00:00.000+01:00".
         */

        // we update the metadata 39727_22_19750113062500 by replacing the dateStamp field with "2009-03-31T12:00:00.000+01:00".
        constraint = new QueryConstraintType("identifier='39727_22_19750113062500'", "1.1.0");
        properties = new ArrayList<>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/dateStamp/DateTime", "2009-03-31T12:00:00.000+01:00"));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);

        // then we verify that the modified metadata is well modified and indexed
        constraint = new QueryConstraintType("Modified after 2009-03-30T00:00:00Z", "1.0.0");
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertNotNull(response);
        assertNotNull(response.getSearchResults());
        assertNotNull(response.getSearchResults().getAny());
        assertEquals(2, response.getSearchResults().getAny().size());

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4");
        expResult.add("39727_22_19750113062500");

        assertEquals(expResult, results);

        /*
         *  TEST 5 : we update the metadata 39727_22_19750113062500 by replacing a complex Property
         *  we replace the property MD_Metadata.identificationInfo.extent.geographicElement by a new Geographic bounding box".
         */

        // we update the metadata 11325_158_19640418141800 by replacing the geographicElement.
        constraint = new QueryConstraintType("identifier='39727_22_19750113062500'", "1.1.0");
        properties = new ArrayList<>();
        DefaultGeographicBoundingBox geographicElement = new DefaultGeographicBoundingBox(1.1, 1.1, 1.1, 1.1);
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement", geographicElement));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);


        // then we verify that the modified metadata is well modified and indexed
        constraint = new QueryConstraintType("WestBoundLongitude = 1.1", "1.0.0");
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertNotNull(response);
        assertNotNull(response.getSearchResults());
        assertNotNull(response.getSearchResults().getAny());
        assertEquals(1, response.getSearchResults().getAny().size());

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
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
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), sort, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertNotNull(response);
        assertNotNull(response.getSearchResults());
        assertNotNull(response.getSearchResults().getAny());
        assertEquals(2, response.getSearchResults().getAny().size());

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("11325_158_19640418141800");
        expResult.add("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4");

        assertEquals(expResult, results);


        if (typeCheckUpdate) {
            // we update the metadata 11325_158_19640418141800 by replacing the language eng by a responsibleParty
            constraint = new QueryConstraintType("identifier='11325_158_19640418141800'", "1.1.0");
            DefaultResponsibleParty value = new DefaultResponsibleParty(Role.AUTHOR);
            properties = new ArrayList<>();
            properties.add(new RecordPropertyType("/gmd:MD_Metadata/language",value));
            update     = new UpdateType(properties, constraint);
            request    = new TransactionType("CSW", "2.0.2", update);

            exe = null;
            try {
                worker.transaction(request);
            } catch (CstlServiceException ex) {
                exe = ex;
            }

            assertNotNull(exe);
            assertEquals(exe.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }

        // we perform again the getRecord request the modified metadata must appears again in the list
        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertNotNull(response);
        assertNotNull(response.getSearchResults());
        assertNotNull(response.getSearchResults().getAny());
        assertEquals(2, response.getSearchResults().getAny().size());

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("11325_158_19640418141800");
        expResult.add("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4");
        assertEquals(expResult, results);

        /*
         *  TEST 7 : we update the metadata 39727_22_19750113062500 by replacing a single Property
         *  we replace the property MD_Metadata.dateStamp with "hello world".
         *  we must receive an exception saying that is not the good type.
         */

        if (typeCheckUpdate) {
            // we update the metadata 39727_22_19750113062500 by replacing the dateStamp field with "hello world".
            constraint = new QueryConstraintType("identifier='39727_22_19750113062500'", "1.1.0");
            properties = new ArrayList<>();
            properties.add(new RecordPropertyType("/gmd:MD_Metadata/dateStamp", "hello world"));
            update     = new UpdateType(properties, constraint);
            request    = new TransactionType("CSW", "2.0.2", update);

            exe = null;
            try {
                worker.transaction(request);
            } catch (CstlServiceException ex) {
                exe = ex;
            }

            assertTrue(exe != null);
        }

        // then we verify that the metadata is not modified
        constraint = new QueryConstraintType("Modified after 2009-03-30T00:00:00Z", "1.0.0");
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 2);

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("mdweb_2_catalog_CSW Data Catalog_profile_inspire_core_service_4");
        expResult.add("39727_22_19750113062500");

        assertEquals(expResult, results);

        /*
         *  TEST 8 : we update the metadata 39727_22_19750113062500 by replacing a complex Property
         *  we replace the property MD_Metadata.identificationInfo.extent.geographicElement by a responsible party".
         *  we must receive an exception
         */

        if (typeCheckUpdate) {
            // we update the metadata 11325_158_19640418141800 by replacing the geographicElement.
            constraint = new QueryConstraintType("identifier='39727_22_19750113062500'", "1.1.0");
            properties = new ArrayList<>();
            DefaultResponsibleParty value = new DefaultResponsibleParty(Role.AUTHOR);
            properties.add(new RecordPropertyType("/gmd:MD_Metadata/identificationInfo/MD_DataIdentification/extent/EX_Extent/geographicElement", value));
            update     = new UpdateType(properties, constraint);
            request    = new TransactionType("CSW", "2.0.2", update);

            exe = null;
            try {
                worker.transaction(request);
            } catch (CstlServiceException ex) {
                exe = ex;
            }
            assertTrue(exe != null);
        }


        // then we verify that the metadata is not modified
        constraint = new QueryConstraintType("WestBoundLongitude = 1.1", "1.0.0");
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertTrue(response.getSearchResults().getAny().size() == 1);

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("39727_22_19750113062500");

        assertEquals(expResult, results);


        /*
         *  TEST 9 : we update the metadata 42292_9s_1990061004100 by replacing a numeroted single Property
         *  we replace the property MD_Metadata.identificationInfo.descriptiveKeywords[3].keyword from "research vessel" to "Modified datas by CSW-T".
         */

        // first we make a getRecords request to verify that the metadata match the request on the Subject field
        constraint = new QueryConstraintType("Subject = 'research vessel' AND Subject = 'CTD profilers'", "1.0.0");
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), sort, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(3, response.getSearchResults().getAny().size());

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("39727_22_19750113062500");
        expResult.add("40510_145_19930221211500");
        expResult.add("42292_9s_19900610041000");

        assertEquals(expResult, results);


        // we update the metadata 42292_9s_1990061004100 by replacing the third descriptive field from "research vessel" to "Modified datas by CSW-T".
        constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.1.0");
        properties = new ArrayList<>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/identificationInfo/MD_DataIdentification/descriptiveKeywords[3]/MD_Keywords/keyword/Anchor", "Modified datas by CSW-T"));
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

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("39727_22_19750113062500");
        expResult.add("40510_145_19930221211500");

        assertEquals(expResult, results);

        // then we verify that the modified metadata is well modified and indexed
        constraint = new QueryConstraintType("Subject = 'Modified datas by CSW-T' AND Subject = 'CTD profilers'", "1.0.0");
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(1 , response.getSearchResults().getAny().size());

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("42292_9s_19900610041000");

        assertEquals(expResult, results);

         /*
         *  TEST 10 : we update the metadata 42292_9s_1990061004100 by replacing a numeroted single Property
         *  we replace the property MD_Metadata.identificationInfo.descriptiveKeywords[1].keyword[7] from "Salinity of the water column" to "something".
         */

        // first we make a getRecords request to verify that the metadata match the request on the Subject field
        constraint = new QueryConstraintType("Subject = 'Salinity of the water column' AND Subject = 'CTD profilers'", "1.0.0");
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), sort, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(3, response.getSearchResults().getAny().size());

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("39727_22_19750113062500");
        expResult.add("40510_145_19930221211500");
        expResult.add("42292_9s_19900610041000");

        assertEquals(expResult, results);


        // we update the metadata 42292_9s_1990061004100 by replacing the abstract field from "Salinity of the water column" to "something".
        constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.1.0");
        properties = new ArrayList<>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/identificationInfo/MD_DataIdentification/descriptiveKeywords[1]/MD_Keywords/keyword[7]/Anchor", "something"));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);


        // we verify that the metadata does not appears anymore in the precedent getRecords request
        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        /*
         *
        assertEquals(2, response.getSearchResults().getAny().size());


          TODO FIX this test
        results = new ArrayList<String>();
        for (Object objRec : response.getSearchResults().getAny()) {
            DefaultMetadata meta = (DefaultMetadata) objRec;
            results.add(meta.getFileIdentifier());
        }

        expResult = new ArrayList<String>();
        expResult.add("39727_22_19750113062500");
        expResult.add("40510_145_19930221211500");

         */

        assertEquals(expResult, results);

        // then we verify that the modified metadata is well modified and indexed
        constraint = new QueryConstraintType("Subject = 'something' AND Subject = 'CTD profilers'", "1.0.0");
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(1 , response.getSearchResults().getAny().size());

        results = new ArrayList<>();
        for (Object objRec : response.getSearchResults().getAny()) {
            if (objRec instanceof DefaultMetadata) {
                final DefaultMetadata meta = (DefaultMetadata) objRec;
                results.add(meta.getFileIdentifier());
            } else if (objRec instanceof Node) {
                final Node isoNode = (Node) objRec;
                final List<Node> idNodes = getNodes("fileIdentifier/CharacterString", isoNode);
                assertEquals(1, idNodes.size());
                Node n =  idNodes.get(0);
                results.add(n.getTextContent());
            }  else {
                fail("unexpected record type:" + obj);
            }
        }

        expResult = new ArrayList<>();
        expResult.add("42292_9s_19900610041000");

        assertEquals(expResult, results);

         /*
         *  TEST 11 : we try to update the metadata 42292_9s_1990061004100 by replacing a numeroted single Property
         *  we try to replace the property MD_Metadata.identificationInfo.abstract[2] but abstract is not a list so we must receive an exception
         */

        if (typeCheckUpdate) {
            // we try to update the metadata 42292_9s_1990061004100 by replacing the abstract field with "wathever".
            constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.1.0");
            properties = new ArrayList<>();
            properties.add(new RecordPropertyType("/gmd:MD_Metadata/identificationInfo/MD_DataIdentification/abstract[2]/CharacterString", "whatever"));
            update     = new UpdateType(properties, constraint);
            request    = new TransactionType("CSW", "2.0.2", update);

            exe = null;
            try {
                worker.transaction(request);
            } catch (CstlServiceException ex) {
                exe = ex;
                assertTrue(ex.getMessage(), ex.getMessage().contains("The property: abstract"));
                assertTrue(ex.getMessage(), ex.getMessage().contains("is not a collection"));
            }

            assertTrue(exe != null);
        }

        /*
         *  TEST 12 : we try to update the metadata 42292_9s_1990061004100 by replacing a numeroted single Property
         *  we try to replace the property MD_Metadata.distributionInfo[3]/distributionFormat/name but distributionInfo is not a list so we must receive an exception
         */

        if (typeCheckUpdate) {
            // we try to update the metadata 42292_9s_1990061004100 by replacing the name field with "wathever".
            constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.1.0");
            properties = new ArrayList<>();
            properties.add(new RecordPropertyType("/gmd:MD_Metadata/distributionInfo[3]/MD_Distribution/distributionFormat/MD_Format/name/Anchor", "whatever"));
            update     = new UpdateType(properties, constraint);
            request    = new TransactionType("CSW", "2.0.2", update);

            exe = null;
            try {
                worker.transaction(request);
            } catch (CstlServiceException ex) {
                exe = ex;
            }

            assertTrue(exe != null);
        }


        /*
         *  TEST 13 : we update the metadata 42292_9s_1990061004100 by replacing a numeroted complex Property
         *  we replace the property MD_Metadata.metadataExtensionInfo.extendedElementInformation[3] with a new MD_ExtendedElementInformation.
         */

        // we update the metadata 42292_9s_1990061004100 by replacing the abstract field from "Salinity of the water column" to "something".
        constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.1.0");
        properties = new ArrayList<>();
        DefaultExtendedElementInformation ext = new DefaultExtendedElementInformation("extendedName",
                                                                                new SimpleInternationalString("some definition"),
                                                                                new SimpleInternationalString("some condition"),
                                                                                Datatype.ABSTRACT_CLASS, null, null, null);

        properties.add(new RecordPropertyType("/gmd:MD_Metadata/metadataExtensionInfo/MD_MetadataExtensionInformation/extendedElementInformation[3]", ext));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);

        // then we must be sure that the metadata is modified
        requestGRBI = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", Arrays.asList("42292_9s_19900610041000"));
        GRresult = (GetRecordByIdResponseType) worker.getRecordById(requestGRBI);

        assertTrue(GRresult != null);
        assertTrue(GRresult.getAny().size() == 1);
        obj = GRresult.getAny().get(0);

        boolean removed = true;
        if (obj instanceof DefaultMetadata) {

            DefaultMetadata isoResult = (DefaultMetadata) obj;
            DefaultExtendedElementInformation extResult = null;

            for (ExtendedElementInformation ex : isoResult.getMetadataExtensionInfo().iterator().next().getExtendedElementInformation()) {
                switch (ex.getName()) {
                    case "extendedName":
                        extResult = (DefaultExtendedElementInformation) ex;
                        break;
                    case "SDN:L031:2:":
                        removed = false;
                        break;
                }
            }
            assertEquals(ext, extResult);
        } else if (obj instanceof Node) {

           DefaultExtendedElementInformation extResult = null;

            Node isoResult = (Node) obj;
            final List<Node> nodes = getNodes("metadataExtensionInfo/MD_MetadataExtensionInformation/extendedElementInformation/MD_ExtendedElementInformation", isoResult);
            for (Node extNode : nodes) {
                DefaultExtendedElementInformation ex = (DefaultExtendedElementInformation) unmarshaller.unmarshal(extNode);
                switch (ex.getName()) {
                    case "extendedName":
                        extResult = (DefaultExtendedElementInformation) ex;
                        break;
                    case "SDN:L031:2:":
                        removed = false;
                        break;
                }
            }
            assertEquals(ext, extResult);

        } else {
            fail("unexpected record type:" + obj);
        }

        // TODO fix this test assertTrue(removed);


         // TEST 14 we update the metadata 42292_9s_1990061004100 by adding a property datasetURI.
        // this value is not yet present in the metadata.
        constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.1.0");
        properties = new ArrayList<>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/gmd:dataSetURI/gco:CharacterString", "someURI"));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);


        // then we verify that the modified metadata is well modified and indexed
        constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.0.0");
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(1 , response.getSearchResults().getAny().size());
        obj = response.getSearchResults().getAny().get(0);
        if (obj instanceof DefaultMetadata) {
            DefaultMetadata meta = (DefaultMetadata) obj;
            assertEquals("someURI", meta.getDataSetUri());
        } else if (obj instanceof Node) {
            Node isoNode = (Node) obj;
            final List<Node> uriNodes = getNodes("dataSetURI/CharacterString", isoNode);
            assertEquals(1, uriNodes.size());
            Node n =  uriNodes.get(0);
            assertEquals(n.getTextContent(), "someURI");
        } else {
            fail("unexpected record type:" + obj);
        }

        // TEST 15 we update the metadata 42292_9s_1990061004100 by updating the datestamp.


        constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.0.0");
        query      = new QueryType(ISO_TYPE_NAMES, new ElementSetNameType(ElementSetType.FULL), null, constraint);
        gr         = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", 1, 10, query, null);

        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(1 , response.getSearchResults().getAny().size());

        obj = response.getSearchResults().getAny().get(0);
        if (obj instanceof DefaultMetadata) {
            DefaultMetadata meta = (DefaultMetadata) obj;
            assertEquals(TemporalUtilities.parseDateSafe("2009-01-26T13:00:00+02:00",true, true), meta.getDateStamp());
        } else if (obj instanceof Node) {
           Node isoNode = (Node) obj;
            final List<Node> dateNodes = getNodes("dateStamp/DateTime", isoNode);
            assertEquals(1, dateNodes.size());
            Node n =  dateNodes.get(0);
            assertEquals(n.getTextContent(), "2009-01-26T13:00:00+02:00");
        } else {
            fail("unexpected record type:" + obj);
        }

        constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.1.0");
        properties = new ArrayList<>();
        properties.add(new RecordPropertyType("/gmd:MD_Metadata/dateStamp/DateTime", "2009-01-18T14:00:00+02:00"));
        update     = new UpdateType(properties, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);


        // then we verify that the modified metadata is well modified and indexed
        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(1 , response.getSearchResults().getAny().size());

        obj = response.getSearchResults().getAny().get(0);
        if (obj instanceof DefaultMetadata) {
            DefaultMetadata meta = (DefaultMetadata) obj;
            assertEquals(TemporalUtilities.parseDateSafe("2009-01-18T14:00:00+02:00",true, true), meta.getDateStamp());
        } else if (obj instanceof Node) {
           Node isoNode = (Node) obj;
            final List<Node> dateNodes = getNodes("dateStamp/DateTime", isoNode);
            assertEquals(1, dateNodes.size());
            Node n =  dateNodes.get(0);
            assertEquals(n.getTextContent(), "2009-01-18T14:00:00+02:00");
        } else {
            fail("unexpected record type:" + obj);
        }

        // TEST 16 we replace totaly the metadata 42292_9s_1990061004100 .

        final DefaultMetadata newMeta = new DefaultMetadata();
        newMeta.setFileIdentifier("42292_9s_19900610041000");
        newMeta.setDateStamp(TemporalUtilities.parseDateSafe("2012-01-01T15:00:00+02:00",true, true));

        final Node originalnewMeta = writeMetadataInDom(newMeta);

        constraint = new QueryConstraintType("identifier='42292_9s_19900610041000'", "1.1.0");
        update     = new UpdateType(originalnewMeta, constraint);
        request    = new TransactionType("CSW", "2.0.2", update);
        result     = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);


        // then we verify that the modified metadata is well modified and indexed
        response = (GetRecordsResponseType) worker.getRecords(gr);
        assertTrue(response != null);
        assertTrue(response.getSearchResults() != null);
        assertTrue(response.getSearchResults().getAny() != null);
        assertEquals(1 , response.getSearchResults().getAny().size());

        obj = response.getSearchResults().getAny().get(0);
        if (obj instanceof DefaultMetadata) {
            DefaultMetadata meta = (DefaultMetadata) obj;
            assertEquals(TemporalUtilities.parseDateSafe("2012-01-01T15:00:00+02:00",true, true), meta.getDateStamp());
            assertEquals(newMeta, meta);

        } else if (obj instanceof Node) {
           Node isoNode = (Node) obj;
            final List<Node> dateNodes = getNodes("dateStamp/DateTime", isoNode);
            assertEquals(1, dateNodes.size());
            Node n =  dateNodes.get(0);
            assertEquals(n.getTextContent(), "2012-01-01T15:00:00+02:00");

            XMLComparator comparator = new XMLComparator(originalnewMeta, isoNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();

        } else {
            fail("unexpected record type:" + obj);
        }

        final GetRecordByIdType grbi = new GetRecordByIdType("CSW", "2.0.2",  new ElementSetNameType(ElementSetType.FULL), "text/xml", "http://www.isotc211.org/2005/gmd", Arrays.asList("42292_9s_19900610041000"));
        final GetRecordByIdResponse resp = worker.getRecordById(grbi);
        assertEquals(1, resp.getAny().size());
        obj = resp.getAny().get(0);
        if (obj instanceof DefaultMetadata) {
            DefaultMetadata meta = (DefaultMetadata) resp.getAny().get(0);

            assertEquals(TemporalUtilities.parseDateSafe("2012-01-01T15:00:00+02:00",true, true), meta.getDateStamp());
            assertEquals(newMeta, meta);
        } else if (obj instanceof Node) {
            Node isoNode = (Node) obj;
            final List<Node> dateNodes = getNodes("dateStamp/DateTime", isoNode);
            assertEquals(1, dateNodes.size());
            Node n =  dateNodes.get(0);
            assertEquals(n.getTextContent(), "2012-01-01T15:00:00+02:00");
        } else {
            fail("unexpected record type:" + obj);
        }

        /*
         * restore context by replacing CTD02 by 42292_5p_19900609195600
         */


        replacement         = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"));
        replacementOriginal = getOriginalMetadata("org/constellation/xml/metadata/meta1.xml");
        constraint          = new QueryConstraintType("identifier='CTDF02'", "1.1.0");
        update              = new UpdateType(replacementOriginal, constraint);
        request             = new TransactionType("CSW", "2.0.2", update);
        result              = worker.transaction(request);

        assertEquals(result.getTransactionSummary().getTotalUpdated(), 1);


        // we try to request the updated metadata
        exe = null;
        try {
            requestGRBI = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", Arrays.asList("CTDF02"));
             worker.getRecordById(requestGRBI);
        } catch (CstlServiceException ex) {
            exe = ex;
        }

        // we must receive an exception saying that the metadata is not present.
        assertNotNull(exe);
        assertEquals(exe.getExceptionCode() , INVALID_PARAMETER_VALUE);
        assertEquals(exe.getLocator() , "id");


        // then we must be sure that the replacement metadata is present
        requestGRBI = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", Arrays.asList("42292_5p_19900609195600"));
        GRresult = (GetRecordByIdResponseType) worker.getRecordById(requestGRBI);

        assertTrue(GRresult != null);
        assertTrue(GRresult.getAny().size() == 1);
        obj = GRresult.getAny().get(0);

        if (obj instanceof DefaultMetadata) {
            DefaultMetadata isoResult = (DefaultMetadata) obj;
            metadataEquals(replacement, isoResult);
        } else if (obj instanceof Node) {
            Node resultNode = (Node) obj;
            XMLComparator comparator = new XMLComparator(replacementOriginal, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.compare();
        } else {
            fail("unexpected record type:" + obj);
        }

        pool.recycle(unmarshaller);
    }

    protected Node getOriginalMetadata(final String fileName) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        Document document = docBuilder.parse(Util.getResourceAsStream(fileName));

        return document.getDocumentElement();
    }

    private Node writeMetadataInDom(final DefaultMetadata meta) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        Document document = docBuilder.newDocument();
        Marshaller m = pool.acquireMarshaller();
        m.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
        m.marshal(meta, document);
        pool.recycle(m);
        return document.getDocumentElement();
    }

    private Node buildNode(final String ns, String localName) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        Document document = docBuilder.newDocument();
        return document.createElementNS(ns, localName);
    }

    private List<Node> getNodes(String XPath, final Node isoNode) {
        final String[] parts = XPath.split("/");
        List<Node> nodes = Arrays.asList(isoNode);
        for (String part : parts) {
            nodes = getChildNodes(nodes, part);
        }
        return nodes;
    }

    private List<Node> getChildNodes(final List<Node> nodes, String childName) {
        final List<Node> result = new ArrayList<>();

        for (Node node : nodes) {
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                final Node n = node.getChildNodes().item(i);
                final String nodeName = n.getLocalName();
                if (nodeName != null && nodeName.equals(childName)) {
                    result.add(n);
                }
            }
        }
        return result;
    }

    /**
     * used for debug
     * @param n
     * @return
     * @throws Exception
     */
    private static String getStringFromNode(final Node n) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(n), new StreamResult(writer));
        String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
        return output;
    }
}
