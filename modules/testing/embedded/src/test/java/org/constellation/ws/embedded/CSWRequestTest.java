/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.ws.embedded;

import org.geotoolkit.csw.xml.v202.GetRecordsResponseType;
import org.geotoolkit.csw.xml.TypeNames;
import org.geotoolkit.csw.xml.ResultType;
import org.geotoolkit.csw.xml.DomainValues;
import java.util.Arrays;
import java.util.List;
import org.geotoolkit.csw.xml.v202.GetDomainType;
import java.net.URLConnection;
import java.net.URL;
import java.io.File;
import java.util.ArrayList;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.csw.xml.v202.DomainValuesType;
import org.geotoolkit.csw.xml.v202.ElementSetNameType;
import org.geotoolkit.csw.xml.v202.GetCapabilitiesType;
import org.geotoolkit.csw.xml.v202.GetDomainResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdType;
import org.geotoolkit.csw.xml.v202.GetRecordsType;
import org.geotoolkit.csw.xml.v202.ListOfValuesType;
import org.geotoolkit.csw.xml.v202.ObjectFactory;
import org.geotoolkit.csw.xml.v202.QueryConstraintType;
import org.geotoolkit.csw.xml.v202.QueryType;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.ows.xml.v100.ExceptionReport;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CSWRequestTest extends AbstractTestRequest {

    private static final String CSW_POST_URL = "http://localhost:9090/csw?";

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initPool() throws JAXBException {
        // Get the list of layers
        pool = EBRIMMarshallerPool.getInstance();
    }

    @AfterClass
    public static void finish() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void testCSWGetCapabilities() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(CSW_POST_URL);


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        final GetCapabilitiesType request = new GetCapabilitiesType("CSW");
        
        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof Capabilities);

    }

    @Test
    public void testCSWError() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(CSW_POST_URL);


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        final GetCapabilitiesType request = new GetCapabilitiesType("SOS");

        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ExceptionReport);

    }

    @Test
    public void testCSWGetDomain() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(CSW_POST_URL);


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        final GetDomainType request = new GetDomainType("CSW", "2.0.2", null, "GetCapabilities.sections");

        postRequestObject(conec, request);
        Object result = unmarshallResponse(conec);

        assertTrue(result instanceof GetDomainResponseType);
        
        List<DomainValues> values = new ArrayList<DomainValues>();
        ListOfValuesType list = new ListOfValuesType(Arrays.asList("All", "ServiceIdentification", "ServiceProvider", "OperationsMetadata","Filter_Capabilities"));
        values.add(new DomainValuesType("GetCapabilities.sections", null, list, new QName("http://www.opengis.net/cat/csw/2.0.2", "Capabilities")));
        GetDomainResponseType expResult = new GetDomainResponseType(values);

        assertEquals(expResult, result);
    }

    @Test
    public void testCSWGetRecordByID() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(CSW_POST_URL);


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        final GetRecordByIdType request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                "text/xml", null, Arrays.asList("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f"));

        final ObjectFactory factory = new ObjectFactory();
        postRequestObject(conec, factory.createGetRecordById(request));
        Object result = unmarshallResponse(conec);

        assertTrue(result instanceof GetRecordByIdResponseType);

        GetRecordByIdResponseType grResult = (GetRecordByIdResponseType) result;
        assertEquals(1, grResult.getAbstractRecord().size());

    }

    @Test
    public void testCSWGetRecords() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(CSW_POST_URL);


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        final QueryConstraintType constraint = new QueryConstraintType("identifier='urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f'", "1.1.0");
        final QueryType query = new QueryType(Arrays.asList(TypeNames.RECORD_QNAME), new ElementSetNameType(ElementSetType.FULL), null, constraint);
        final GetRecordsType request = new GetRecordsType("CSW", "2.0.2", ResultType.RESULTS, null, null, null, 1, 10, query, null);
                
        postRequestObject(conec, request);
        Object result = unmarshallResponse(conec);

        assertTrue(result instanceof GetRecordsResponseType);

        GetRecordsResponseType grResult = (GetRecordsResponseType) result;
        assertEquals(1, grResult.getSearchResults().getAbstractRecord().size());

    }
}
