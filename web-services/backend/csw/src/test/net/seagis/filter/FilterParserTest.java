/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.filter;

// J2SE dependencies
import java.io.StringReader;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

// Seagis dependencies
import net.seagis.coverage.web.Service;
import net.seagis.coverage.web.ServiceVersion;
import net.seagis.lucene.Filter.SerialChainFilter;
import net.seagis.lucene.Filter.SpatialFilter;
import net.seagis.lucene.Filter.SpatialQuery;
import net.seagis.ogc.FilterType;

// JUnit dependencies
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal
 */
public class FilterParserTest {
    
    private FilterParser filterParser;
    private Logger       logger = Logger.getLogger("net.seagis.metadata");
    private Unmarshaller filterUnmarshaller;
   
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        filterParser = new FilterParser(new ServiceVersion(Service.OWS, "2.0.2"));
        JAXBContext jbcontext = JAXBContext.newInstance("net.seagis.ogc:net.seagis.gml.v311");
        filterUnmarshaller = jbcontext.createUnmarshaller();
    }

    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * Test simple comparison filter. 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void simpleComparisonFilterTest() throws Exception {
        
        /**
         * Test 1: a simple Filter propertyIsLike 
         */
        String XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">"                                                               + 
			   "    <ogc:PropertyIsLike escapeChar=\"\\\" singleChar=\"?\" wildCard=\"*\">" +
                           "        <ogc:PropertyName>apiso:Title</ogc:PropertyName>"                   +
			   "        <ogc:Literal>*VM*</ogc:Literal>"                                    +
			   "    </ogc:PropertyIsLike>"                                                  + 
                           "</ogc:Filter>";
        StringReader reader = new StringReader(XMLrequest);
        
        JAXBElement element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        FilterType filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        SpatialQuery spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "Title:*VM*");
        
        
        /**
         * Test 2: a simple Filter PropertyIsEqualTo
         */
        XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">"    + 
	            "    <ogc:PropertyIsEqualTo>"                              +
                    "        <ogc:PropertyName>apiso:Title</ogc:PropertyName>" +
                    "        <ogc:Literal>VM</ogc:Literal>"                    +
		    "    </ogc:PropertyIsEqualTo>"                             + 
                    "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "Title:\"VM\"");
        
        /**
         * Test 3: a simple Filter PropertyIsNotEqualTo
         */
        XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">"    + 
	            "    <ogc:PropertyIsNotEqualTo>"                           +
                    "        <ogc:PropertyName>apiso:Title</ogc:PropertyName>" +
                    "        <ogc:Literal>VM</ogc:Literal>"                    +
		    "    </ogc:PropertyIsNotEqualTo>"                          + 
                    "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "metafile:doc NOT Title:\"VM\"");
        
        
        /**
         * Test 4: a simple Filter PropertyIsNull
         */
        XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">"    + 
	            "    <ogc:PropertyIsNull>"                           +
                    "        <ogc:PropertyName>apiso:Title</ogc:PropertyName>" +
                    "    </ogc:PropertyIsNull>"                          + 
                    "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "Title:null");
        
        /**
         * Test 5: a simple Filter PropertyIsGreaterThanOrEqualTo
         */
        XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">"           + 
	            "    <ogc:PropertyIsGreaterThanOrEqualTo>"                        +
                    "        <ogc:PropertyName>apiso:CreationDate</ogc:PropertyName>" +
                    "        <ogc:Literal>2007-06-02</ogc:Literal>"                   +
                    "    </ogc:PropertyIsGreaterThanOrEqualTo>"                       + 
                    "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "CreationDate:[20070602  30000101]");
        
        /**
         * Test 6: a simple Filter PropertyIsGreaterThan
         */
        XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">"           + 
	            "    <ogc:PropertyIsGreaterThan>"                                 +
                    "        <ogc:PropertyName>apiso:CreationDate</ogc:PropertyName>" +
                    "        <ogc:Literal>2007-06-02</ogc:Literal>"                   +
                    "    </ogc:PropertyIsGreaterThan>"                                + 
                    "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "CreationDate:{20070602  30000101}");
        
        /**
         * Test 7: a simple Filter PropertyIsLessThan
         */
        XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">"           + 
	            "    <ogc:PropertyIsLessThan>"                                 +
                    "        <ogc:PropertyName>apiso:CreationDate</ogc:PropertyName>" +
                    "        <ogc:Literal>2007-06-02</ogc:Literal>"                   +
                    "    </ogc:PropertyIsLessThan>"                                + 
                    "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "CreationDate:{00000101 20070602}");
        
        
         /**
         * Test 8: a simple Filter PropertyIsLessThanOrEqualTo
         */
        XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">"           + 
	            "    <ogc:PropertyIsLessThanOrEqualTo>"                                 +
                    "        <ogc:PropertyName>apiso:CreationDate</ogc:PropertyName>" +
                    "        <ogc:Literal>2007-06-02</ogc:Literal>"                   +
                    "    </ogc:PropertyIsLessThanOrEqualTo>"                                + 
                    "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "CreationDate:[00000101 20070602]");
        
        
        
    }
    
    /**
     * Test simple logical filter (unary and binary). 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void simpleLogicalFilterTest() throws Exception {
        
        /**
         * Test 1: a simple Filter AND between two propertyIsEqualTo 
         */
        String XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">"         +
                           "    <ogc:And>                                        "         +
			   "        <ogc:PropertyIsEqualTo>"                               +
                           "            <ogc:PropertyName>apiso:Title</ogc:PropertyName>"  +
                           "            <ogc:Literal>starship trooper</ogc:Literal>"       +
		           "        </ogc:PropertyIsEqualTo>"                              +
                           "        <ogc:PropertyIsEqualTo>"                               +
                           "            <ogc:PropertyName>apiso:Author</ogc:PropertyName>" +
                           "            <ogc:Literal>Timothee Gustave</ogc:Literal>"       +
		           "        </ogc:PropertyIsEqualTo>"                              +
                           "    </ogc:And>"                                                +
                           "</ogc:Filter>";
        StringReader reader = new StringReader(XMLrequest);
        
        JAXBElement element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        FilterType filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        SpatialQuery spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "(Title:\"starship trooper\" AND Author:\"Timothee Gustave\")");
        
        /**
         * Test 2: a simple Filter OR between two propertyIsEqualTo 
         */
        XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">"                +
                           "    <ogc:Or>                                        "         +
			   "        <ogc:PropertyIsEqualTo>"                               +
                           "            <ogc:PropertyName>apiso:Title</ogc:PropertyName>"  +
                           "            <ogc:Literal>starship trooper</ogc:Literal>"       +
		           "        </ogc:PropertyIsEqualTo>"                              +
                           "        <ogc:PropertyIsEqualTo>"                               +
                           "            <ogc:PropertyName>apiso:Author</ogc:PropertyName>" +
                           "            <ogc:Literal>Timothee Gustave</ogc:Literal>"       +
		           "        </ogc:PropertyIsEqualTo>"                              +
                           "    </ogc:Or>"                                                +
                           "</ogc:Filter>";
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "(Title:\"starship trooper\" OR Author:\"Timothee Gustave\")");
        
        
        /**
         * Test 3: a simple Filter OR between three propertyIsEqualTo 
         */
        XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">"                +
                           "    <ogc:Or>                                        "          +
			   "        <ogc:PropertyIsEqualTo>"                               +
                           "            <ogc:PropertyName>apiso:Title</ogc:PropertyName>"  +
                           "            <ogc:Literal>starship trooper</ogc:Literal>"       +
		           "        </ogc:PropertyIsEqualTo>"                              +
                           "        <ogc:PropertyIsEqualTo>"                               +
                           "            <ogc:PropertyName>apiso:Author</ogc:PropertyName>" +
                           "            <ogc:Literal>Timothee Gustave</ogc:Literal>"       +
		           "        </ogc:PropertyIsEqualTo>"                              +
                           "        <ogc:PropertyIsEqualTo>"                               +
                           "            <ogc:PropertyName>apiso:Id</ogc:PropertyName>"     +
                           "            <ogc:Literal>268</ogc:Literal>"                    +
		           "        </ogc:PropertyIsEqualTo>"                              +
                           "    </ogc:Or> "                                                +
                           "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "(Title:\"starship trooper\" OR Author:\"Timothee Gustave\" OR Id:\"268\")");
        
        
        /**
         * Test 4: a simple Filter Not propertyIsEqualTo 
         */
        XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">"                +
                           "    <ogc:Not>                                        "          +
			   "        <ogc:PropertyIsEqualTo>"                               +
                           "            <ogc:PropertyName>apiso:Title</ogc:PropertyName>"  +
                           "            <ogc:Literal>starship trooper</ogc:Literal>"       +
		           "        </ogc:PropertyIsEqualTo>"                              +
                           "    </ogc:Not>"                                                 +
                           "</ogc:Filter>";
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "Title:\"starship trooper\"");
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.NOT);
    }
    
    
    @Test
    public void simpleSpatialFilterTest() throws Exception {
        
        /**
         * Test 1: a simple spatial Filter Intersects 
         */
        String XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"          "  +
                           "            xmlns:gml=\"http://www.opengis.net/gml\">         "  +
                           "    <ogc:Intersects>                                          "  +
                           "       <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName> "  +
                           "         <gml:Envelope srsName=\"EPSG:4326\">                 "  +
			   "             <gml:lowerCorner>7 12</gml:lowerCorner>          "  +
                           "             <gml:upperCorner>20 20</gml:upperCorner>         "  +
			   "        </gml:Envelope>                                       "  +     
			   "    </ogc:Intersects>                                         "  +
                           "</ogc:Filter>";
        StringReader reader = new StringReader(XMLrequest);
        
        JAXBElement element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        FilterType filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        SpatialQuery spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "");
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        SpatialFilter spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertEquals(spatialFilter.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 2: a simple Distance Filter Beyond 
         */
        XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"          " +
                    "            xmlns:gml=\"http://www.opengis.net/gml\">         " +
                    "    <ogc:DWithin>                                             " +
                    "      <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>  " +
                    "        <gml:Point>                                           " +
                    "           <gml:coordinates>3.4 2.5</gml:coordinates>         " +
                    "        </gml:Point>                                          " +
                    "        <ogc:Distance units='m'>1000</ogc:Distance>           " +
                    "    </ogc:DWithin>                                            " +
                    "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "");
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertEquals(spatialFilter.getFilterType(), SpatialFilter.DWITHIN);
        //assertTrue(spatialFilter.getGeometry() instanceof GeneralDirectPosition);
    }
    

}
