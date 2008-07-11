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
import java.awt.geom.Line2D;
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

// geotools dependencies
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 * A suite of test verifying the transformation of an XML filter into a Lucene Query/filter 
 * 
 * @author Guilhem Legal
 */
public class FilterParserTest {
    
    private FilterParser filterParser;
    private Logger       logger = Logger.getLogger("net.seagis.filter");
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "(Title:\"starship trooper\" OR Author:\"Timothee Gustave\" OR Id:\"268\")");
        
        
        /**
         * Test 4: a simple Filter Not propertyIsEqualTo 
         */
        XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">"                 +
                           "    <ogc:Not>                                        "          +
			   "        <ogc:PropertyIsEqualTo>"                                +
                           "            <ogc:PropertyName>apiso:Title</ogc:PropertyName>"   +
                           "            <ogc:Literal>starship trooper</ogc:Literal>"        +
		           "        </ogc:PropertyIsEqualTo>"                               +
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "Title:\"starship trooper\"");
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.NOT);
    }
    
    
    /**
     * Test simple Spatial filter
     * 
     * @throws java.lang.Exception
     */
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        SpatialFilter spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertEquals(spatialFilter.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 2: a simple Distance Filter DWithin
         */
        XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"          " +
                    "            xmlns:gml=\"http://www.opengis.net/gml\">         " +
                    "    <ogc:DWithin>                                             " +
                    "      <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>  " +
                    "        <gml:Point srsName=\"EPSG:4326\">                     " +
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertEquals(spatialFilter.getFilterType(), SpatialFilter.DWITHIN);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralDirectPosition);
        
        /**
         * Test 3: a simple spatial Filter Intersects 
         */
        XMLrequest =       "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"          "  +
                           "            xmlns:gml=\"http://www.opengis.net/gml\">         "  +
                           "    <ogc:Intersects>                                          "  +
                           "       <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName> "  +
                           "           <gml:LineString srsName=\"EPSG:4326\">             "  +
                           "                <gml:coordinates ts=\" \" decimal=\".\" cs=\",\">1,2 10,15</gml:coordinates>" +
                           "           </gml:LineString>                                  "  + 
			   "    </ogc:Intersects>                                         "  +
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
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertEquals(spatialFilter.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue(spatialFilter.getGeometry() instanceof Line2D);
        
    }
    
    /**
     * Test Multiple Spatial Filter
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void multipleSpatialFilterTest() throws Exception {
        
        /**
         * Test 1: two spatial Filter with AND 
         */
        String XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                "  +
                           "            xmlns:gml=\"http://www.opengis.net/gml\">               "  +
                           "    <ogc:And>                                                       "  +
                           "        <ogc:Intersects>                                            "  +
                           "             <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName> "  +
                           "             <gml:Envelope srsName=\"EPSG:4326\">                   "  +
			   "                 <gml:lowerCorner>7 12</gml:lowerCorner>            "  +
                           "                 <gml:upperCorner>20 20</gml:upperCorner>           "  +
			   "             </gml:Envelope>                                        "  +     
			   "        </ogc:Intersects>                                           "  +
                           "        <ogc:Intersects>                                            "  +
                           "           <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>   "  +
                           "             <gml:Envelope srsName=\"EPSG:4326\">                   "  +
			   "                  <gml:lowerCorner>-2 -4</gml:lowerCorner>          "  +
                           "                  <gml:upperCorner>12 12</gml:upperCorner>          "  +
			   "             </gml:Envelope>                                        "  +     
			   "        </ogc:Intersects>                                           "  +
                           "    </ogc:And>                                                      "  +
                           "</ogc:Filter>";
        
        StringReader reader = new StringReader(XMLrequest);
        
        JAXBElement element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        FilterType filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        SpatialQuery spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SerialChainFilter);
        SerialChainFilter chainFilter = (SerialChainFilter) spaQuery.getSpatialFilter();
                
        assertEquals(chainFilter.getActionType().length,  1);
        assertEquals(chainFilter.getActionType()[0],      SerialChainFilter.AND);
        assertEquals(chainFilter.getChain().size(),       2);
        
        /**
         * Test 2: three spatial Filter with OR 
         */
       XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                "  +
                   "            xmlns:gml=\"http://www.opengis.net/gml\">               "  +
                   "    <ogc:Or>                                                        "  +
                   "        <ogc:Intersects>                                            "  +
                   "             <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName> "  +
                   "             <gml:Envelope srsName=\"EPSG:4326\">                   "  +
                   "                 <gml:lowerCorner>7 12</gml:lowerCorner>            "  +
                   "                 <gml:upperCorner>20 20</gml:upperCorner>           "  +
		   "             </gml:Envelope>                                        "  +     
                   "        </ogc:Intersects>                                           "  +
		   "        <ogc:Contains>                                              "  +
                   "             <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName> "  +
                   "             <gml:Point srsName=\"EPSG:4326\">                      "  +
                   "                 <gml:coordinates>3.4 2.5</gml:coordinates>         "  +
                   "            </gml:Point>                                            "  +  
		   "        </ogc:Contains>                                             "  +
                   "         <ogc:BBOX>                                                 "  +
                   "              <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>"  + 
		   "              <gml:Envelope srsName=\"EPSG:4326\">                  "  +
                   "                   <gml:lowerCorner>-20 -20</gml:lowerCorner>       "  +
		   "                   <gml:upperCorner>20 20</gml:upperCorner>         "  +
		   "              </gml:Envelope>                                       "  +
		   "       </ogc:BBOX>                                                  "  +
                   "    </ogc:Or>                                                       "  +
                   "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SerialChainFilter);
        chainFilter = (SerialChainFilter) spaQuery.getSpatialFilter();
                
        assertEquals(chainFilter.getActionType().length,  2);
        assertEquals(chainFilter.getActionType()[0],      SerialChainFilter.OR);
        assertEquals(chainFilter.getActionType()[1],      SerialChainFilter.OR);
        assertEquals(chainFilter.getChain().size(),       3);
        
        //we verify each filter
        SpatialFilter f1 = (SpatialFilter) chainFilter.getChain().get(0);
        assertEquals(f1.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue(f1.getGeometry() instanceof GeneralEnvelope);
        
        SpatialFilter f2 = (SpatialFilter) chainFilter.getChain().get(1);
        assertEquals(f2.getFilterType(), SpatialFilter.CONTAINS);
        assertTrue  (f2.getGeometry() instanceof GeneralDirectPosition);
        
        SpatialFilter f3 = (SpatialFilter) chainFilter.getChain().get(2);
        assertEquals(f3.getFilterType(), SpatialFilter.BBOX);
        assertTrue(f3.getGeometry() instanceof GeneralEnvelope);
        
         /**
         * Test 3: three spatial Filter F1 AND (F2 OR F3)
         */
       XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                   "  +
                   "            xmlns:gml=\"http://www.opengis.net/gml\">                  "  +
                   "    <ogc:And>                                                          "  +
                   "        <ogc:Intersects>                                               "  +
                   "             <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>    "  +
                   "             <gml:Envelope srsName=\"EPSG:4326\">                      "  +
                   "                 <gml:lowerCorner>7 12</gml:lowerCorner>               "  +
                   "                 <gml:upperCorner>20 20</gml:upperCorner>              "  +
		   "             </gml:Envelope>                                           "  +     
                   "        </ogc:Intersects>                                              "  +
                   "        <ogc:Or>                                                       "  +
		   "            <ogc:Contains>                                             "  +
                   "                <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName> "  +
                   "                <gml:Point srsName=\"EPSG:4326\">                      "  +
                   "                    <gml:coordinates>3.4 2.5</gml:coordinates>         "  +
                   "                </gml:Point>                                           "  +  
		   "            </ogc:Contains>                                            "  +
                   "            <ogc:BBOX>                                                 "  +
                   "                <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName> "  + 
		   "                <gml:Envelope srsName=\"EPSG:4326\">                   "  +
                   "                    <gml:lowerCorner>-20 -20</gml:lowerCorner>         "  +
		   "                    <gml:upperCorner>20 20</gml:upperCorner>           "  +
		   "                </gml:Envelope>                                        "  +
		   "            </ogc:BBOX>                                                "  +
                   "        </ogc:Or>                                                      "  +
                   "    </ogc:And>                                                         "  +
                   "</ogc:Filter>                                                          ";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SerialChainFilter);
        chainFilter = (SerialChainFilter) spaQuery.getSpatialFilter();
                
        assertEquals(chainFilter.getActionType().length,  1);
        assertEquals(chainFilter.getActionType()[0],      SerialChainFilter.AND);
        assertEquals(chainFilter.getChain().size(),       2);
        
        //we verify each filter
        f1 = (SpatialFilter) chainFilter.getChain().get(0);
        assertEquals(f1.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue(f1.getGeometry() instanceof GeneralEnvelope);
        
        SerialChainFilter cf2 = (SerialChainFilter) chainFilter.getChain().get(1);
        assertEquals(cf2.getActionType().length,  1);
        assertEquals(cf2.getActionType()[0],      SerialChainFilter.OR);
        assertEquals(cf2.getChain().size(),       2);
        
        
        SpatialFilter cf2_1 = (SpatialFilter) cf2.getChain().get(0);
        assertEquals(cf2_1.getFilterType(), SpatialFilter.CONTAINS);
        assertTrue  (cf2_1.getGeometry() instanceof GeneralDirectPosition);
        
        SpatialFilter cf2_2 = (SpatialFilter) cf2.getChain().get(1);
        assertEquals(cf2_2.getFilterType(), SpatialFilter.BBOX);
        assertTrue  (cf2_2.getGeometry() instanceof GeneralEnvelope);
        
         /**
         * Test 4: three spatial Filter (NOT F1) AND F2 AND F3
         */
       XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                   "  +
                   "            xmlns:gml=\"http://www.opengis.net/gml\">                  "  +
                   "    <ogc:And>                                                          "  +
                   "        <ogc:Not>                                                      "  +
                   "            <ogc:Intersects>                                           "  +
                   "                <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName> "  +
                   "                <gml:Envelope srsName=\"EPSG:4326\">                   "  +
                   "                    <gml:lowerCorner>7 12</gml:lowerCorner>            "  +
                   "                    <gml:upperCorner>20 20</gml:upperCorner>           "  +
		   "                </gml:Envelope>                                        "  +     
                   "            </ogc:Intersects>                                          "  +
                   "        </ogc:Not>                                                     "  +
		   "        <ogc:Contains>                                                 "  +
                   "             <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>    "  +
                   "             <gml:Point srsName=\"EPSG:4326\">                         "  +
                   "                 <gml:coordinates>3.4 2.5</gml:coordinates>            "  +
                   "            </gml:Point>                                               "  +  
		   "        </ogc:Contains>                                                "  +
                   "         <ogc:BBOX>                                                    "  +
                   "              <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>   "  + 
		   "              <gml:Envelope srsName=\"EPSG:4326\">                     "  +
                   "                   <gml:lowerCorner>-20 -20</gml:lowerCorner>          "  +
		   "                   <gml:upperCorner>20 20</gml:upperCorner>            "  +
		   "              </gml:Envelope>                                          "  +
		   "       </ogc:BBOX>                                                     "  +
                   "    </ogc:And>                                                         "  +
                   "</ogc:Filter>                                                          ";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SerialChainFilter);
        chainFilter = (SerialChainFilter) spaQuery.getSpatialFilter();

        assertEquals(chainFilter.getActionType().length,  2);
        assertEquals(chainFilter.getActionType()[0],      SerialChainFilter.AND);
        assertEquals(chainFilter.getActionType()[1],      SerialChainFilter.AND);
        assertEquals(chainFilter.getChain().size(),       3);
        
        //we verify each filter
        SerialChainFilter cf1 = (SerialChainFilter) chainFilter.getChain().get(0);
        assertEquals(cf1.getChain().size(), 1);
        assertEquals(cf1.getActionType().length,  1);
        assertEquals(cf1.getActionType()[0],      SerialChainFilter.NOT);
        
        SpatialFilter cf1_1 = (SpatialFilter) cf1.getChain().get(0);
        assertEquals(cf1_1.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue  (cf1_1.getGeometry() instanceof GeneralEnvelope);
        
        
        f2 = (SpatialFilter) chainFilter.getChain().get(1);
        assertEquals(f2.getFilterType(), SpatialFilter.CONTAINS);
        assertTrue  (f2.getGeometry() instanceof GeneralDirectPosition);
        
        f3 = (SpatialFilter) chainFilter.getChain().get(2);
        assertEquals(f3.getFilterType(), SpatialFilter.BBOX);
        assertTrue(f3.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 5: three spatial Filter NOT (F1 OR F2) AND F3
         */
       XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                      "  +
                   "            xmlns:gml=\"http://www.opengis.net/gml\">                     "  +
                   "    <ogc:And>                                                             "  +
                   "        <ogc:Not>                                                         "  +
                   "            <ogc:Or>                                                      "  +
                   "                <ogc:Intersects>                                          "  +
                   "                    <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>"  +
                   "                    <gml:Envelope srsName=\"EPSG:4326\">                  "  +
                   "                        <gml:lowerCorner>7 12</gml:lowerCorner>           "  +
                   "                        <gml:upperCorner>20 20</gml:upperCorner>          "  +
		   "                    </gml:Envelope>                                       "  +     
                   "                </ogc:Intersects>                                         "  +
		   "                <ogc:Contains>                                            "  +
                   "                    <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>"  +
                   "                    <gml:Point srsName=\"EPSG:4326\">                     "  +
                   "                        <gml:coordinates>3.4 2.5</gml:coordinates>        "  +
                   "                    </gml:Point>                                          "  +  
		   "                </ogc:Contains>                                           "  +
                   "           </ogc:Or>                                                      "  +
                   "        </ogc:Not>                                                        "  +
                   "         <ogc:BBOX>                                                       "  +
                   "              <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>      "  + 
		   "              <gml:Envelope srsName=\"EPSG:4326\">                        "  +
                   "                   <gml:lowerCorner>-20 -20</gml:lowerCorner>             "  +
		   "                   <gml:upperCorner>20 20</gml:upperCorner>               "  +
		   "              </gml:Envelope>                                             "  +
		   "       </ogc:BBOX>                                                        "  +
                   "    </ogc:And>                                                            "  +
                   "</ogc:Filter>                                                             ";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SerialChainFilter);
        chainFilter = (SerialChainFilter) spaQuery.getSpatialFilter();
            
        assertEquals(chainFilter.getActionType().length,  1);
        assertEquals(chainFilter.getActionType()[0],      SerialChainFilter.AND);
        assertEquals(chainFilter.getChain().size(),       2);
        
        //we verify each filter
        cf1 = (SerialChainFilter) chainFilter.getChain().get(0);
        assertEquals(cf1.getChain().size(), 1);
        assertEquals(cf1.getActionType().length,  1);
        assertEquals(cf1.getActionType()[0],      SerialChainFilter.NOT);
        assertTrue(cf1.getChain().get(0) instanceof SerialChainFilter);
        
        SerialChainFilter cf1_cf1 =  (SerialChainFilter) cf1.getChain().get(0);
        assertEquals(cf1_cf1.getChain().size(),   2);
        assertEquals(cf1_cf1.getActionType().length,  1);
        assertEquals(cf1_cf1.getActionType()[0],      SerialChainFilter.OR);
        
        assertTrue(cf1_cf1.getChain().get(0) instanceof SpatialFilter);
        SpatialFilter cf1_cf1_1 = (SpatialFilter) cf1_cf1.getChain().get(0);
        assertEquals(cf1_cf1_1.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue  (cf1_cf1_1.getGeometry() instanceof GeneralEnvelope);
        
        assertTrue(cf1_cf1.getChain().get(1) instanceof SpatialFilter);
        SpatialFilter cf1_cf1_2 = (SpatialFilter) cf1_cf1.getChain().get(1);
        assertEquals(cf1_cf1_2.getFilterType(), SpatialFilter.CONTAINS);
        assertTrue  (cf1_cf1_2.getGeometry() instanceof GeneralDirectPosition);
        
        f2 = (SpatialFilter) chainFilter.getChain().get(1);
        assertEquals(f2.getFilterType(), SpatialFilter.BBOX);
        assertTrue(f2.getGeometry() instanceof GeneralEnvelope);
        
        
    }

    /**
     * Test complex query with both comparison, logical and spatial query
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void multipleMixedFilterTest() throws Exception {

        /**
         * Test 1: PropertyIsLike AND INTERSECT 
         */
        String XMLrequest ="<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                          " +
                           "            xmlns:gml=\"http://www.opengis.net/gml\">                         " +
                           "    <ogc:And>                                                                 " +
			   "        <ogc:PropertyIsLike escapeChar=\"\\\" singleChar=\"?\" wildCard=\"*\">" +
                           "           <ogc:PropertyName>apiso:Title</ogc:PropertyName>                   " +
			   "           <ogc:Literal>*VM*</ogc:Literal>                                    " +
			   "        </ogc:PropertyIsLike>                                                 " + 
                           "        <ogc:Intersects>                                                      " +
                           "           <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>             " +
                           "             <gml:Envelope srsName=\"EPSG:4326\">                             " +
			   "                  <gml:lowerCorner>-2 -4</gml:lowerCorner>                    " +
                           "                  <gml:upperCorner>12 12</gml:upperCorner>                    " +
			   "             </gml:Envelope>                                                  " +     
			   "        </ogc:Intersects>                                                     " +
                           "    </ogc:And>                                                                " +
                           "</ogc:Filter>";
        
        StringReader reader = new StringReader(XMLrequest);
        
        JAXBElement element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        FilterType filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        SpatialQuery spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "(Title:*VM*)");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        SpatialFilter spaFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertEquals(spaFilter.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue  (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 2: PropertyIsLike AND INTERSECT AND propertyIsEquals
         */
        XMLrequest =       "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                          " +
                           "            xmlns:gml=\"http://www.opengis.net/gml\">                         " +
                           "    <ogc:And>                                                                 " +
			   "        <ogc:PropertyIsLike escapeChar=\"\\\" singleChar=\"?\" wildCard=\"*\">" +
                           "           <ogc:PropertyName>apiso:Title</ogc:PropertyName>                   " +
			   "           <ogc:Literal>*VM*</ogc:Literal>                                    " +
			   "        </ogc:PropertyIsLike>                                                 " + 
                           "        <ogc:Intersects>                                                      " +
                           "           <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>             " +
                           "             <gml:Envelope srsName=\"EPSG:4326\">                             " +
			   "                  <gml:lowerCorner>-2 -4</gml:lowerCorner>                    " +
                           "                  <gml:upperCorner>12 12</gml:upperCorner>                    " +
			   "             </gml:Envelope>                                                  " +     
			   "        </ogc:Intersects>                                                     " +
                           "        <ogc:PropertyIsEqualTo>                                               " +
                           "            <ogc:PropertyName>apiso:Title</ogc:PropertyName>                  " +
                           "            <ogc:Literal>VM</ogc:Literal>                                     " +
                           "        </ogc:PropertyIsEqualTo>                                              " + 
                           "    </ogc:And>                                                                " +
                           "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "(Title:*VM* AND Title:\"VM\")");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertEquals(spaFilter.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue  (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 3:  INTERSECT AND propertyIsEquals AND BBOX
         */
        XMLrequest =       "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                          " +
                           "            xmlns:gml=\"http://www.opengis.net/gml\">                         " +
                           "    <ogc:And>                                                                 " +
                           "        <ogc:Intersects>                                                      " +
                           "           <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>             " +
                           "             <gml:Envelope srsName=\"EPSG:4326\">                             " +
			   "                  <gml:lowerCorner>-2 -4</gml:lowerCorner>                    " +
                           "                  <gml:upperCorner>12 12</gml:upperCorner>                    " +
			   "             </gml:Envelope>                                                  " +     
			   "        </ogc:Intersects>                                                     " +
                           "        <ogc:PropertyIsEqualTo>                                               " +
                           "            <ogc:PropertyName>apiso:Title</ogc:PropertyName>                  " +
                           "            <ogc:Literal>VM</ogc:Literal>                                     " +
                           "        </ogc:PropertyIsEqualTo>                                              " +
                           "         <ogc:BBOX>                                                           " +
                           "              <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>          " + 
                           "              <gml:Envelope srsName=\"EPSG:4326\">                            " +
                           "                   <gml:lowerCorner>-20 -20</gml:lowerCorner>                 " +
                           "                   <gml:upperCorner>20 20</gml:upperCorner>                   " +
                           "              </gml:Envelope>                                                 " +
                           "       </ogc:BBOX>                                                            " +
                           "    </ogc:And>                                                                " +
                           "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "(Title:\"VM\")");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SerialChainFilter);
        SerialChainFilter chainFilter = (SerialChainFilter) spaQuery.getSpatialFilter();
        
        assertEquals(chainFilter.getActionType().length,  1);
        assertEquals(chainFilter.getActionType()[0],      SerialChainFilter.AND);
        assertEquals(chainFilter.getChain().size(),       2);
        
        SpatialFilter f1 = (SpatialFilter) chainFilter.getChain().get(0);
        assertEquals (f1.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue   (f1.getGeometry() instanceof GeneralEnvelope);
        
        SpatialFilter f2 = (SpatialFilter) chainFilter.getChain().get(1);
        assertEquals (f2.getFilterType(), SpatialFilter.BBOX);
        assertTrue   (f2.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 4: PropertyIsLike OR INTERSECT OR propertyIsEquals
         */
        XMLrequest =       "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                          " +
                           "            xmlns:gml=\"http://www.opengis.net/gml\">                         " +
                           "    <ogc:Or>                                                                 " +
			   "        <ogc:PropertyIsLike escapeChar=\"\\\" singleChar=\"?\" wildCard=\"*\">" +
                           "           <ogc:PropertyName>apiso:Title</ogc:PropertyName>                   " +
			   "           <ogc:Literal>*VM*</ogc:Literal>                                    " +
			   "        </ogc:PropertyIsLike>                                                 " + 
                           "        <ogc:Intersects>                                                      " +
                           "           <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>             " +
                           "             <gml:Envelope srsName=\"EPSG:4326\">                             " +
			   "                  <gml:lowerCorner>-2 -4</gml:lowerCorner>                    " +
                           "                  <gml:upperCorner>12 12</gml:upperCorner>                    " +
			   "             </gml:Envelope>                                                  " +     
			   "        </ogc:Intersects>                                                     " +
                           "        <ogc:PropertyIsEqualTo>                                               " +
                           "            <ogc:PropertyName>apiso:Title</ogc:PropertyName>                  " +
                           "            <ogc:Literal>VM</ogc:Literal>                                     " +
                           "        </ogc:PropertyIsEqualTo>                                              " + 
                           "    </ogc:Or>                                                                 " +
                           "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "(Title:*VM* OR Title:\"VM\")");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.OR);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertEquals(spaFilter.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue  (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
         /**
         * Test 5:  INTERSECT OR propertyIsEquals OR BBOX
         */
        XMLrequest =       "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                          " +
                           "            xmlns:gml=\"http://www.opengis.net/gml\">                         " +
                           "    <ogc:Or>                                                                 " +
                           "        <ogc:Intersects>                                                      " +
                           "           <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>             " +
                           "             <gml:Envelope srsName=\"EPSG:4326\">                             " +
			   "                  <gml:lowerCorner>-2 -4</gml:lowerCorner>                    " +
                           "                  <gml:upperCorner>12 12</gml:upperCorner>                    " +
			   "             </gml:Envelope>                                                  " +     
			   "        </ogc:Intersects>                                                     " +
                           "        <ogc:PropertyIsEqualTo>                                               " +
                           "            <ogc:PropertyName>apiso:Title</ogc:PropertyName>                  " +
                           "            <ogc:Literal>VM</ogc:Literal>                                     " +
                           "        </ogc:PropertyIsEqualTo>                                              " +
                           "         <ogc:BBOX>                                                           " +
                           "              <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>          " + 
                           "              <gml:Envelope srsName=\"EPSG:4326\">                            " +
                           "                   <gml:lowerCorner>-20 -20</gml:lowerCorner>                 " +
                           "                   <gml:upperCorner>20 20</gml:upperCorner>                   " +
                           "              </gml:Envelope>                                                 " +
                           "       </ogc:BBOX>                                                            " +
                           "    </ogc:Or>                                                                " +
                           "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "(Title:\"VM\")");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.OR);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SerialChainFilter);
        chainFilter = (SerialChainFilter) spaQuery.getSpatialFilter();
        
        assertEquals(chainFilter.getActionType().length,  1);
        assertEquals(chainFilter.getActionType()[0],      SerialChainFilter.OR);
        assertEquals(chainFilter.getChain().size(),       2);
        
        f1 = (SpatialFilter) chainFilter.getChain().get(0);
        assertEquals (f1.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue   (f1.getGeometry() instanceof GeneralEnvelope);
        
        f2 = (SpatialFilter) chainFilter.getChain().get(1);
        assertEquals (f2.getFilterType(), SpatialFilter.BBOX);
        assertTrue   (f2.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 6:  INTERSECT AND (propertyIsEquals OR BBOX)
         */
        XMLrequest =       "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                          " +
                           "            xmlns:gml=\"http://www.opengis.net/gml\">                         " +
                           "    <ogc:And>                                                                 " +
                           "        <ogc:Intersects>                                                      " +
                           "           <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>             " +
                           "             <gml:Envelope srsName=\"EPSG:4326\">                             " +
			   "                  <gml:lowerCorner>-2 -4</gml:lowerCorner>                    " +
                           "                  <gml:upperCorner>12 12</gml:upperCorner>                    " +
			   "             </gml:Envelope>                                                  " +     
			   "        </ogc:Intersects>                                                     " +
                           "        <ogc:Or>                                                              " +
                           "            <ogc:PropertyIsEqualTo>                                           " +
                           "                <ogc:PropertyName>apiso:Title</ogc:PropertyName>              " +
                           "                <ogc:Literal>VM</ogc:Literal>                                 " +
                           "            </ogc:PropertyIsEqualTo>                                          " +
                           "            <ogc:BBOX>                                                        " +
                           "                <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>        " + 
                           "                <gml:Envelope srsName=\"EPSG:4326\">                          " +
                           "                    <gml:lowerCorner>-20 -20</gml:lowerCorner>                " +
                           "                    <gml:upperCorner>20 20</gml:upperCorner>                  " +
                           "               </gml:Envelope>                                                " +
                           "            </ogc:BBOX>                                                       " +
                           "        </ogc:Or>                                                             " +
                           "    </ogc:And>                                                                " +
                           "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "");
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.AND);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) spaQuery.getSpatialFilter();
        
        assertEquals (spaFilter.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
        SpatialQuery subQuery1 = spaQuery.getSubQueries().get(0);
        assertTrue  (subQuery1.getSpatialFilter() != null);
        assertEquals(subQuery1.getQuery(), "(Title:\"VM\")");
        assertEquals(subQuery1.getSubQueries().size(), 0);
        assertEquals(subQuery1.getLogicalOperator(), SerialChainFilter.OR);
        
        assertTrue(subQuery1.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) subQuery1.getSpatialFilter();
        
        assertEquals (spaFilter.getFilterType(), SpatialFilter.BBOX);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 7:  propertyIsNotEquals OR (propertyIsLike AND DWITHIN)
         */
        XMLrequest =       "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                                  " +
                           "            xmlns:gml=\"http://www.opengis.net/gml\">                                 " +
                           "        <ogc:Or>                                                                      " +
                           "            <ogc:PropertyIsNotEqualTo>                                                " +
                           "                <ogc:PropertyName>apiso:Title</ogc:PropertyName>                      " +
                           "                <ogc:Literal>VMAI</ogc:Literal>                                       " +
                           "            </ogc:PropertyIsNotEqualTo>                                               " +
                           "            <ogc:And>                                                                 " +
                           "                <ogc:PropertyIsLike escapeChar=\"\\\" singleChar=\"?\" wildCard=\"*\">" +
                           "                    <ogc:PropertyName>apiso:Title</ogc:PropertyName>                  " +
			   "                    <ogc:Literal>LO?Li</ogc:Literal>                                  " +
			   "                </ogc:PropertyIsLike>                                                 " + 
                           "                <ogc:DWithin>                                                         " +
                           "                    <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>            " +
                           "                    <gml:Point srsName=\"EPSG:4326\">                                 " +
                           "                        <gml:coordinates>3.4 2.5</gml:coordinates>                    " +
                           "                    </gml:Point>                                                      " +
                           "                    <ogc:Distance units='m'>1000</ogc:Distance>                       " +
                           "                </ogc:DWithin>                                                        " +
                           "            </ogc:And>                                                                " +
                           "        </ogc:Or>                                                                     " +
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
        assertEquals(spaQuery.getQuery(), "(metafile:doc NOT Title:\"VMAI\")");
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.OR);
        
        subQuery1 = spaQuery.getSubQueries().get(0);
        assertTrue  (subQuery1.getSpatialFilter() != null);
        assertEquals(subQuery1.getQuery(), "(Title:LO?Li)");
        assertEquals(subQuery1.getSubQueries().size(), 0);
        assertEquals(subQuery1.getLogicalOperator(), SerialChainFilter.AND);
        
        assertTrue(subQuery1.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) subQuery1.getSpatialFilter();
        
        assertEquals (spaFilter.getFilterType(), SpatialFilter.DWITHIN);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralDirectPosition);
        
        
        /**
         * Test 8:  propertyIsLike AND INTERSECT AND (propertyIsEquals OR BBOX) AND (propertyIsNotEquals OR (Beyond AND propertyIsLike))
         */
        XMLrequest =       "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                                  " +
                           "            xmlns:gml=\"http://www.opengis.net/gml\">                                 " +
                           "    <ogc:And>                                                                         " +
                           "        <ogc:PropertyIsLike escapeChar=\"\\\" singleChar=\"?\" wildCard=\"*\">        " +
                           "           <ogc:PropertyName>apiso:Title</ogc:PropertyName>                           " +
			   "           <ogc:Literal>*VM*</ogc:Literal>                                            " +
			   "        </ogc:PropertyIsLike>                                                         " + 
                           "        <ogc:Intersects>                                                              " +
                           "           <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>                     " +
                           "             <gml:Envelope srsName=\"EPSG:4326\">                                     " +
			   "                  <gml:lowerCorner>-2 -4</gml:lowerCorner>                            " +
                           "                  <gml:upperCorner>12 12</gml:upperCorner>                            " +
			   "             </gml:Envelope>                                                          " +     
			   "        </ogc:Intersects>                                                             " +
                           "        <ogc:Or>                                                                      " +
                           "            <ogc:PropertyIsEqualTo>                                                   " +
                           "                <ogc:PropertyName>apiso:Title</ogc:PropertyName>                      " +
                           "                <ogc:Literal>PLOUF</ogc:Literal>                                      " +
                           "            </ogc:PropertyIsEqualTo>                                                  " +
                           "            <ogc:BBOX>                                                                " +
                           "                <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>                " + 
                           "                <gml:Envelope srsName=\"EPSG:4326\">                                  " +
                           "                    <gml:lowerCorner>-20 -20</gml:lowerCorner>                        " +
                           "                    <gml:upperCorner>20 20</gml:upperCorner>                          " +
                           "               </gml:Envelope>                                                        " +
                           "            </ogc:BBOX>                                                               " +
                           "        </ogc:Or>                                                                     " +
                           "        <ogc:Or>                                                                      " +
                           "            <ogc:PropertyIsNotEqualTo>                                                " +
                           "                <ogc:PropertyName>apiso:Title</ogc:PropertyName>                      " +
                           "                <ogc:Literal>VMAI</ogc:Literal>                                       " +
                           "            </ogc:PropertyIsNotEqualTo>                                               " +
                           "            <ogc:And>                                                                 " +
                           "                <ogc:PropertyIsLike escapeChar=\"\\\" singleChar=\"?\" wildCard=\"*\">" +
                           "                    <ogc:PropertyName>apiso:Title</ogc:PropertyName>                  " +
			   "                    <ogc:Literal>LO?Li</ogc:Literal>                                  " +
			   "                </ogc:PropertyIsLike>                                                 " + 
                           "                <ogc:DWithin>                                                         " +
                           "                    <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>            " +
                           "                    <gml:Point srsName=\"EPSG:4326\">                                 " +
                           "                        <gml:coordinates>3.4 2.5</gml:coordinates>                    " +
                           "                    </gml:Point>                                                      " +
                           "                    <ogc:Distance units='m'>1000</ogc:Distance>                       " +
                           "                </ogc:DWithin>                                                        " +
                           "            </ogc:And>                                                                " +
                           "        </ogc:Or>                                                                     " +
                           "    </ogc:And>                                                                        " +
                           "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "(Title:*VM*)");
        assertEquals(spaQuery.getSubQueries().size(), 2);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.AND);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) spaQuery.getSpatialFilter();
        
        assertEquals (spaFilter.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
        subQuery1 = spaQuery.getSubQueries().get(0);
        assertTrue  (subQuery1.getSpatialFilter() != null);
        assertEquals(subQuery1.getQuery(), "(Title:\"PLOUF\")");
        assertEquals(subQuery1.getSubQueries().size(), 0);
        assertEquals(subQuery1.getLogicalOperator(), SerialChainFilter.OR);
        
        assertTrue(subQuery1.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) subQuery1.getSpatialFilter();
        
        assertEquals (spaFilter.getFilterType(), SpatialFilter.BBOX);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
        SpatialQuery subQuery2 = spaQuery.getSubQueries().get(1);
        assertTrue  (subQuery2.getSpatialFilter() == null);
        assertEquals(subQuery2.getQuery(), "(metafile:doc NOT Title:\"VMAI\")");
        assertEquals(subQuery2.getSubQueries().size(), 1);
        assertEquals(subQuery2.getLogicalOperator(), SerialChainFilter.OR);
        
        SpatialQuery subQuery2_1 = subQuery2.getSubQueries().get(0);
        assertTrue  (subQuery2_1.getSpatialFilter() != null);
        assertEquals(subQuery2_1.getQuery(), "(Title:LO?Li)");
        assertEquals(subQuery2_1.getSubQueries().size(), 0);
        assertEquals(subQuery2_1.getLogicalOperator(), SerialChainFilter.AND);
        
        assertTrue(subQuery2_1.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) subQuery2_1.getSpatialFilter();
        
        assertEquals (spaFilter.getFilterType(), SpatialFilter.DWITHIN);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralDirectPosition);
        
        
        
        /**
         * Test 9:  NOT propertyIsLike AND NOT INTERSECT AND NOT (propertyIsEquals OR BBOX) AND (propertyIsNotEquals OR (Beyond AND propertyIsLike))
         */
        XMLrequest =       "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\"                                  " +
                           "            xmlns:gml=\"http://www.opengis.net/gml\">                                 " +
                           "    <ogc:And>                                                                         " +
                           "        <ogc:Not>                                                                     " +
                           "            <ogc:PropertyIsLike escapeChar=\"\\\" singleChar=\"?\" wildCard=\"*\">    " +
                           "                <ogc:PropertyName>apiso:Title</ogc:PropertyName>                      " +
			   "                <ogc:Literal>*VM*</ogc:Literal>                                       " +
			   "            </ogc:PropertyIsLike>                                                     " + 
                           "        </ogc:Not>                                                                    " +
                           "        <ogc:Not>                                                                     " +
                           "            <ogc:Intersects>                                                          " +
                           "                <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>                " +
                           "                <gml:Envelope srsName=\"EPSG:4326\">                                  " +
			   "                    <gml:lowerCorner>-2 -4</gml:lowerCorner>                          " +
                           "                    <gml:upperCorner>12 12</gml:upperCorner>                          " +
			   "                </gml:Envelope>                                                       " +     
			   "            </ogc:Intersects>                                                         " +
                           "        </ogc:Not>                                                                    " +
                           "        <ogc:Not>                                                                     " +
                           "        <ogc:Or>                                                                      " +
                           "            <ogc:PropertyIsEqualTo>                                                   " +
                           "                <ogc:PropertyName>apiso:Title</ogc:PropertyName>                      " +
                           "                <ogc:Literal>PLOUF</ogc:Literal>                                      " +
                           "            </ogc:PropertyIsEqualTo>                                                  " +
                           "            <ogc:BBOX>                                                                " +
                           "                <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>                " + 
                           "                <gml:Envelope srsName=\"EPSG:4326\">                                  " +
                           "                    <gml:lowerCorner>-20 -20</gml:lowerCorner>                        " +
                           "                    <gml:upperCorner>20 20</gml:upperCorner>                          " +
                           "               </gml:Envelope>                                                        " +
                           "            </ogc:BBOX>                                                               " +
                           "        </ogc:Or>                                                                     " +
                           "        </ogc:Not>                                                                     " +
                           "        <ogc:Or>                                                                      " +
                           "            <ogc:PropertyIsNotEqualTo>                                                " +
                           "                <ogc:PropertyName>apiso:Title</ogc:PropertyName>                      " +
                           "                <ogc:Literal>VMAI</ogc:Literal>                                       " +
                           "            </ogc:PropertyIsNotEqualTo>                                               " +
                           "            <ogc:And>                                                                 " +
                           "                <ogc:PropertyIsLike escapeChar=\"\\\" singleChar=\"?\" wildCard=\"*\">" +
                           "                    <ogc:PropertyName>apiso:Title</ogc:PropertyName>                  " +
			   "                    <ogc:Literal>LO?Li</ogc:Literal>                                  " +
			   "                </ogc:PropertyIsLike>                                                 " + 
                           "                <ogc:DWithin>                                                         " +
                           "                    <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>            " +
                           "                    <gml:Point srsName=\"EPSG:4326\">                                 " +
                           "                        <gml:coordinates>3.4 2.5</gml:coordinates>                    " +
                           "                    </gml:Point>                                                      " +
                           "                    <ogc:Distance units='m'>1000</ogc:Distance>                       " +
                           "                </ogc:DWithin>                                                        " +
                           "            </ogc:And>                                                                " +
                           "        </ogc:Or>                                                                     " +
                           "    </ogc:And>                                                                        " +
                           "</ogc:Filter>";
        
        reader = new StringReader(XMLrequest);
        
        element =  (JAXBElement) filterUnmarshaller.unmarshal(reader);
        filter = (FilterType) element.getValue();
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "");
        assertEquals(spaQuery.getSubQueries().size(), 3);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.AND);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SerialChainFilter);
        chainFilter = (SerialChainFilter) spaQuery.getSpatialFilter();
        
        assertEquals(chainFilter.getActionType().length,  1);
        assertEquals(chainFilter.getActionType()[0],      SerialChainFilter.NOT);
        assertEquals(chainFilter.getChain().size(),       1);
        
        f1 = (SpatialFilter) chainFilter.getChain().get(0);
        
        assertEquals (f1.getFilterType(), SpatialFilter.INTERSECT);
        assertTrue   (f1.getGeometry() instanceof GeneralEnvelope);

        // first sub-query
        subQuery1 = spaQuery.getSubQueries().get(0);
        assertTrue  (subQuery1.getSpatialFilter() == null);
        assertEquals(subQuery1.getQuery(), "Title:*VM*");
        assertEquals(subQuery1.getSubQueries().size(), 0);
        assertEquals(subQuery1.getLogicalOperator(), SerialChainFilter.NOT);
        
        
        // second sub-query
        subQuery2 = spaQuery.getSubQueries().get(1);
        assertTrue  (subQuery2.getSpatialFilter() == null);
        assertEquals(subQuery2.getQuery(), "");
        assertEquals(subQuery2.getSubQueries().size(), 1);
        assertEquals(subQuery2.getLogicalOperator(), SerialChainFilter.NOT);
        
        // second subQuery => first subQuery
        subQuery2_1 = subQuery2.getSubQueries().get(0);
        assertTrue  (subQuery2_1.getSpatialFilter() != null);
        assertEquals(subQuery2_1.getQuery(), "(Title:\"PLOUF\")");
        assertEquals(subQuery2_1.getSubQueries().size(), 0);
        assertEquals(subQuery2_1.getLogicalOperator(), SerialChainFilter.OR);
        
        assertTrue(subQuery2_1.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) subQuery2_1.getSpatialFilter();
        
        assertEquals (spaFilter.getFilterType(), SpatialFilter.BBOX);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
        // third sub-query
        SpatialQuery subQuery3 = spaQuery.getSubQueries().get(2);
        assertTrue  (subQuery3.getSpatialFilter() == null);
        assertEquals(subQuery3.getQuery(), "(metafile:doc NOT Title:\"VMAI\")");
        assertEquals(subQuery3.getSubQueries().size(), 1);
        assertEquals(subQuery3.getLogicalOperator(), SerialChainFilter.OR);
        
        SpatialQuery subQuery3_1 = subQuery3.getSubQueries().get(0);
        assertTrue  (subQuery3_1.getSpatialFilter() != null);
        assertEquals(subQuery3_1.getQuery(), "(Title:LO?Li)");
        assertEquals(subQuery3_1.getSubQueries().size(), 0);
        assertEquals(subQuery3_1.getLogicalOperator(), SerialChainFilter.AND);
        
        assertTrue(subQuery3_1.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) subQuery3_1.getSpatialFilter();
        
        assertEquals (spaFilter.getFilterType(), SpatialFilter.DWITHIN);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralDirectPosition);
        
        
    }
    

}
