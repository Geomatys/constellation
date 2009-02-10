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
package org.constellation.ogc;

// J2SE dependencies
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.EnvelopeEntry;

//Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 * A Test suite verifying that the Record are correctly marshalled/unmarshalled
 * 
 * @author Guilhem Legal
 */
public class FilterMarshallingTest {
    
    private Logger       logger = Logger.getLogger("org.constellation.filter");
    private Unmarshaller unmarshaller;
    private Marshaller   marshaller;
    
   
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        JAXBContext jbcontext  = JAXBContext.newInstance("org.constellation.ogc:org.constellation.gml.v311");
        unmarshaller           = jbcontext.createUnmarshaller();
        marshaller             = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl(""));
        
    }

    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * Test simple Record Marshalling. 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void filterMarshalingTest() throws Exception {
        
        /*
         * Test marshalling spatial filter
         */
        DirectPositionType lowerCorner = new DirectPositionType(10.0, 11.0);
        DirectPositionType upperCorner = new DirectPositionType(10.0, 11.0);
        EnvelopeEntry envelope         = new EnvelopeEntry("env-id", lowerCorner, upperCorner, "EPSG:4326");
        
        OverlapsType filterElement     = new OverlapsType(new PropertyNameType("boundingBox"), envelope);
        FilterType filter              = new FilterType(filterElement); 
        
        StringWriter sw = new StringWriter();
        marshaller.marshal(filter, sw);
        
        String result = sw.toString();
        String expResult = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\">" + '\n' +
        "    <ogc:Overlaps>"                                                                                                                           + '\n' +
        "        <ogc:PropertyName>boundingBox</ogc:PropertyName>"                                                                                     + '\n' +
        "        <gml:Envelope srsName=\"EPSG:4326\">"                                                                                                 + '\n' +
        "            <gml:id>env-id</gml:id>"                                                                                                          + '\n' +
        "            <gml:lowerCorner>10.0 11.0</gml:lowerCorner>"                                                                                     + '\n' +
        "            <gml:upperCorner>10.0 11.0</gml:upperCorner>"                                                                                     + '\n' +
        "        </gml:Envelope>"                                                                                                                      + '\n' +
        "    </ogc:Overlaps>"                                                                                                                          + '\n' +
        "</ogc:Filter>" + '\n';
    
        logger.finer("result" + result);
        logger.finer("expected" + expResult);
        assertEquals(expResult, result);
        
        
    }
    
    /**
     * Test simple Record Marshalling. 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void filterUnmarshalingTest() throws Exception {
        
        /*
         * Test Unmarshalling spatial filter.
         */
        
        String xml = 
       "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\">" + '\n' +
        "    <ogc:Overlaps>"                                                                                                                           + '\n' +
        "        <ogc:PropertyName>boundingBox</ogc:PropertyName>"                                                                                     + '\n' +
        "        <gml:Envelope srsName=\"EPSG:4326\">"                                                                                                 + '\n' +
        "            <gml:id>env-id</gml:id>"                                                                                                          + '\n' +
        "            <gml:lowerCorner>10.0 11.0</gml:lowerCorner>"                                                                                     + '\n' +
        "            <gml:upperCorner>10.0 11.0</gml:upperCorner>"                                                                                     + '\n' +
        "        </gml:Envelope>"                                                                                                                      + '\n' +
        "    </ogc:Overlaps>"                                                                                                                          + '\n' +
        "</ogc:Filter>" + '\n';
        
        StringReader sr = new StringReader(xml);
        
        JAXBElement jb =  (JAXBElement) unmarshaller.unmarshal(sr);
        FilterType result = (FilterType) jb.getValue();
        
        DirectPositionType lowerCorner = new DirectPositionType(10.0, 11.0);
        DirectPositionType upperCorner = new DirectPositionType(10.0, 11.0);
        EnvelopeEntry envelope         = new EnvelopeEntry("env-id", lowerCorner, upperCorner, "EPSG:4326");
        
        OverlapsType filterElement     = new OverlapsType(new PropertyNameType("boundingBox"), envelope);
        FilterType expResult           = new FilterType(filterElement); 
        
        
        assertEquals(expResult.getSpatialOps().getValue(), result.getSpatialOps().getValue());
        assertEquals(expResult, result);
        
    }
    
    class NamespacePrefixMapperImpl extends NamespacePrefixMapper {

        /**
         * if set this namespace will be the root of the document with no prefix.
         */
        private String rootNamespace;

        public NamespacePrefixMapperImpl(String rootNamespace) {
            super();
            this.rootNamespace = rootNamespace;

        }

        /**
         * Returns a preferred prefix for the given namespace URI.
         */
        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            String prefix = null;

            if (rootNamespace != null && rootNamespace.equals(namespaceUri)) {
                prefix = "";
            } else if ("http://www.opengis.net/gml".equals(namespaceUri)) {
                prefix = "gml";
            } else if ("http://www.opengis.net/ogc".equals(namespaceUri)) {
                prefix = "ogc";
            } else if ("http://www.w3.org/1999/xlink".equals(namespaceUri)) {
                prefix = "xlink";
            }
            return prefix;
        }

        /**
         * Returns a list of namespace URIs that should be declared
         * at the root element.
         */
        @Override
        public String[] getPreDeclaredNamespaceUris() {
            return new String[]{};
        }
    }
}

