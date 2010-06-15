/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.filter;

// J2SE dependencies
import java.util.logging.Logger;

// Geotools dependencies

// JUnit dependencies
import org.geotoolkit.csw.xml.v202.QueryConstraintType;
import org.geotoolkit.lucene.filter.LuceneOGCFilter;
import org.geotoolkit.lucene.filter.SerialChainFilter;
import org.geotoolkit.lucene.filter.SpatialQuery;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.junit.*;
import org.opengis.filter.Filter;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import static org.junit.Assert.*;

/**
 * A suite of test verifying the transformation of an CQL request into a Lucene Query/filter 
 * 
 * @author Guilhem Legal
 */
public class CQLParserTest {
    
    private LuceneFilterParser filterParser;
    private Logger       logger = Logger.getLogger("org.constellation.filter");
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        filterParser = new LuceneFilterParser();
    }

    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * Test simple comparison CQL query. 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void simpleComparisonFilterTest() throws Exception {
        
        /**
         * Test 1: PropertyIsLike 
         */
        String cql = "Title LIKE 'VM%'";
        FilterType filter = FilterParser.cqlToFilter(cql);
        
        assertNotNull(filter.getComparisonOps());
        assertNull(filter.getLogicOps());
        assertEquals(0, filter.getId().size() );
        assertNull(filter.getSpatialOps());
        
        SpatialQuery spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "Title:VM*");
        
        /**
         *  Test 2: PropertyIsEquals
         */

        cql = "Title ='VM'";
        filter = FilterParser.cqlToFilter(cql);
        
        
        assertNotNull(filter.getComparisonOps());
        assertNull(filter.getLogicOps());
        assertEquals(0, filter.getId().size() );
        assertNull(filter.getSpatialOps());
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "Title:\"VM\"");
        
        /**
         *  Test 3: PropertyIsNotEquals
         */
        cql = "Title <>'VM'";
        filter =FilterParser.cqlToFilter(cql);
        
        assertNull(filter.getComparisonOps());
        assertNotNull(filter.getLogicOps());
        assertEquals(0, filter.getId().size() );
        assertNull(filter.getSpatialOps());
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "Title:\"VM\"");
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.NOT);
        
        /**
         * Test 4: PropertyIsNull
         */
        cql = "Title IS NULL";
        filter = FilterParser.cqlToFilter(cql);
        
        assertNotNull(filter.getComparisonOps());
        assertNull(filter.getLogicOps());
        assertEquals(0, filter.getId().size() );
        assertNull(filter.getSpatialOps());
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "Title:null");
    
        /**
         * Test 5: PropertyIsGreaterThan
         */
        cql = "CreationDate AFTER 2007-06-02T00:00:00Z";
        filter = FilterParser.cqlToFilter(cql);
        
        assertNotNull(filter.getComparisonOps());
        assertNull(filter.getLogicOps());
        assertEquals(0, filter.getId().size() );
        assertNull(filter.getSpatialOps());
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertNull(spaQuery.getSpatialFilter());
        assertEquals(0, spaQuery.getSubQueries().size());
        assertEquals("CreationDate:{20070602  30000101}", spaQuery.getQuery());
        
         /**
         * Test 6: PropertyIsLessThan
         */
        cql = "CreationDate BEFORE 2007-06-02T00:00:00Z";
        filter = FilterParser.cqlToFilter(cql);
        
        assertNotNull(filter.getComparisonOps());
        assertNull(filter.getLogicOps());
        assertEquals(0, filter.getId().size() );
        assertNull(filter.getSpatialOps());
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertNull(spaQuery.getSpatialFilter());
        assertEquals(0, spaQuery.getSubQueries().size());
        assertEquals("CreationDate:{00000101 20070602}", spaQuery.getQuery());
    }
    
    /**
     * Test simple logical CQL query (unary and binary). 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void simpleLogicalFilterTest() throws Exception {
       
        /**
         * Test 1: AND between two propertyIsEqualTo 
         */
        String cql = "Title = 'starship trooper' AND Author = 'Timothee Gustave'";
        FilterType filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        SpatialQuery spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "(Title:\"starship trooper\" AND Author:\"Timothee Gustave\")");
        
        
        /**
         * Test 2: OR between two propertyIsEqualTo 
         */
        cql = "Title = 'starship trooper' OR Author = 'Timothee Gustave'";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "(Title:\"starship trooper\" OR Author:\"Timothee Gustave\")");
        
        /**
         * Test 3:  OR between three propertyIsEqualTo 
         */
        cql = "Title = 'starship trooper' OR Author = 'Timothee Gustave' OR Id = '268'";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "(Id:\"268\" OR (Title:\"starship trooper\" OR Author:\"Timothee Gustave\"))");
        
        /**
         * Test 4: Not propertyIsEqualTo 
         */
        cql = "NOT Title = 'starship trooper'";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "Title:\"starship trooper\"");
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.NOT);

        /**
         * Test 5: AND between two propertyIsEqualTo and OR NOT with a third propertyIsEqualsTo
         */
        cql = "(Title = 'starship trooper' AND Author = 'Timothee Gustave') OR NOT Title = 'pedro'";
        filter = FilterParser.cqlToFilter(cql);

        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);

        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);

        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getQuery(), "((Title:\"starship trooper\" AND Author:\"Timothee Gustave\"))");
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.OR);
        assertEquals(spaQuery.getSubQueries().get(0).getQuery(), "Title:\"pedro\"");
        assertEquals(spaQuery.getSubQueries().get(0).getLogicalOperator(), SerialChainFilter.NOT);

        /**
         * Test 6: OR between two propertyIsEqualTo and AND NOT with a third propertyIsEqualsTo
         */
        cql = "(Title = 'starship trooper' OR Author = 'Timothee Gustave') AND NOT Title = 'pedro'";
        filter = FilterParser.cqlToFilter(cql);

        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);

        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);

        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getQuery(), "((Title:\"starship trooper\" OR Author:\"Timothee Gustave\"))");
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.AND);
        assertEquals(spaQuery.getSubQueries().get(0).getQuery(), "Title:\"pedro\"");
        assertEquals(spaQuery.getSubQueries().get(0).getLogicalOperator(), SerialChainFilter.NOT);

        /**
         * Test 7: AND between two NOT propertyIsEqualTo
         */
        cql = "NOT Title = 'starship trooper' AND NOT Author = 'Timothee Gustave'";
        filter = FilterParser.cqlToFilter(cql);

        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);

        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);

        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 2);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.AND);
        assertEquals(spaQuery.getSubQueries().get(0).getQuery(), "Title:\"starship trooper\"");
        assertEquals(spaQuery.getSubQueries().get(0).getLogicalOperator(), SerialChainFilter.NOT);
        assertEquals(spaQuery.getSubQueries().get(1).getQuery(), "Author:\"Timothee Gustave\"");
        assertEquals(spaQuery.getSubQueries().get(1).getLogicalOperator(), SerialChainFilter.NOT);

        /**
         * Test 8: OR between two NOT propertyIsEqualTo
         */
        cql = "NOT Title = 'starship trooper' OR NOT Author = 'Timothee Gustave'";
        filter = FilterParser.cqlToFilter(cql);

        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);

        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);

        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 2);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.OR);
        assertEquals(spaQuery.getSubQueries().get(0).getQuery(), "Title:\"starship trooper\"");
        assertEquals(spaQuery.getSubQueries().get(0).getLogicalOperator(), SerialChainFilter.NOT);
        assertEquals(spaQuery.getSubQueries().get(1).getQuery(), "Author:\"Timothee Gustave\"");
        assertEquals(spaQuery.getSubQueries().get(1).getLogicalOperator(), SerialChainFilter.NOT);

    }
    
    /**
     * Test simple Spatial CQL query
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void simpleSpatialFilterTest() throws Exception {
        
        /**
         * Test 1: a simple spatial Filter Intersects 
         */
        String cql = "INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.42)) ";
        FilterType filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        SpatialQuery spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        Filter spatialFilter = (Filter) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spatialFilter instanceof Intersects);
        
        /**
         * Test 2: a simple Distance Filter DWithin
         */
        cql = "DWITHIN(BoundingBox, POINT(12.1 28.9), 10, meters)";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        DWithin Dfilter = (DWithin) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();

                
        assertTrue(Dfilter instanceof  DWithin);
        assertEquals(Dfilter.getDistanceUnits(), "meters");
        assertTrue(Dfilter.getDistance() == 10.0);
        
        /**
         * Test 3: a simple Distance Filter Beyond
         */
        cql = "BEYOND(BoundingBox, POINT(12.1 28.9), 10, meters)";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        Beyond Bfilter = (Beyond) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(Bfilter instanceof Beyond);
        assertEquals(Bfilter.getDistanceUnits(), "meters");
        assertTrue(Bfilter.getDistance() == 10.0);
        
        /**
         * Test 4: a simple BBOX filter
         */
        cql = "BBOX(BoundingBox, 10,20,30,40)";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        BBOX spabbox = (BBOX) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();

        //System.out.println("spabbox:" + spabbox.getSRS());
        assertTrue(spabbox instanceof BBOX);
        
        /**
         * Test 4: a simple Contains filter
         */
        cql = "CONTAINS(BoundingBox, POINT(14.05 46.46))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        Contains spaC = (Contains) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spaC instanceof Contains);
        
        /**
         * Test 5: a simple Contains filter
         */
        cql = "CONTAINS(BoundingBox, LINESTRING(1 2, 10 15))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        spaC = (Contains) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spaC instanceof  Contains);
        
        /*
         * Test 6: a simple Contains filter
         */
        cql = "CONTAINS(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        spaC = (Contains) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spaC instanceof Contains);
        
         /**
         * Test 7: a simple Crosses filter
         */
        cql = "CROSS(BoundingBox, POINT(14.05 46.46))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        Crosses spaCr = (Crosses) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spaCr instanceof Crosses);
        
        /**
         * Test 8: a simple Crosses filter
         */
        cql = "CROSS(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        spaCr = (Crosses) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spaCr instanceof Crosses);
        
        /**
         * Test 9: a simple Disjoint filter
         */
        cql = "DISJOINT(BoundingBox, POINT(14.05 46.46))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        Disjoint spaDis = (Disjoint) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spaDis instanceof  Disjoint);
        
        /**
         * Test 10: a simple Disjoint filter
         */
        cql = "DISJOINT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        spaDis = (Disjoint) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spaDis instanceof Disjoint);
        
        /**
         * Test 11: a simple Equals filter
         */
        cql = "EQUAL(BoundingBox, POINT(14.05 46.46))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        Equals spaEq = (Equals) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spaEq instanceof  Equals);
        
        /**
         * Test 12: a simple Equals filter
         */
        cql = "EQUAL(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        spaEq = (Equals) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spaEq instanceof  Equals);
        
        /**
         * Test 13: a simple Overlaps filter
         */
        cql = "OVERLAP(BoundingBox, POINT(14.05 46.46))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        Overlaps spaOver = (Overlaps) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spaOver instanceof Overlaps);
        
        /**
         * Test 14: a simple Overlaps filter
         */
        cql = "OVERLAP(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        spaOver = (Overlaps) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spaOver instanceof Overlaps);
        
        /**
         * Test 15: a simple Touches filter
         */
        cql = "TOUCH(BoundingBox, POINT(14.05 46.46))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        Touches spaTou = (Touches) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spaTou instanceof Touches);
        
        /**
         * Test 16: a simple Touches filter
         */
        cql = "TOUCH(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        spaTou = (Touches) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spaTou instanceof Touches);
        
        /**
         * Test 17: a simple Within filter
         */
        cql = "WITHIN(BoundingBox, POINT(14.05 46.46))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        spatialFilter = (Filter) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spatialFilter instanceof Within);
        
        /**
         * Test 18: a simple Within filter
         */
        cql = "WITHIN(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        spatialFilter = (Filter) ((LuceneOGCFilter) spaQuery.getSpatialFilter()).getOGCFilter();
                
        assertTrue(spatialFilter instanceof Within);
    }
    
    /**
     * Test multiple spatial CQL query 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void multipleSpatialFilterTest() throws Exception {
        
        /**
         * Test 1: two spatial Filter with AND 
         */
        String cql = "INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND OVERLAP(BoundingBox, ENVELOPE(22.07, 60.23, 11.69, 73.48))";
        FilterType filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        SpatialQuery spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SerialChainFilter);
        SerialChainFilter chainFilter = (SerialChainFilter) spaQuery.getSpatialFilter();
                
        assertEquals(chainFilter.getActionType().length,  1);
        assertEquals(chainFilter.getActionType()[0],      SerialChainFilter.AND);
        assertEquals(chainFilter.getChain().size(),       2);
        
        /**
         * Test 2: three spatial Filter with OR 
         */
        cql = "INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) OR CONTAINS(BoundingBox, POINT(22.07 60.23)) OR BBOX(BoundingBox, 10,20,30,40)";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "(metafile:doc)");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SerialChainFilter);
        chainFilter = (SerialChainFilter) spaQuery.getSpatialFilter();
                
        assertEquals(chainFilter.getActionType().length,  1);
        assertEquals(chainFilter.getActionType()[0],      SerialChainFilter.OR);
        assertEquals(chainFilter.getChain().size(),       2);
        
        //we verify each filter
        assertTrue(chainFilter.getChain().get(0) instanceof SerialChainFilter);
        SerialChainFilter cf1 = (SerialChainFilter) chainFilter.getChain().get(0);
        
        assertEquals(cf1.getActionType().length,  1);
        assertEquals(cf1.getActionType()[0],      SerialChainFilter.OR);
        assertEquals(chainFilter.getChain().size(),       2);
        
        assertTrue(cf1.getChain().get(0) instanceof LuceneOGCFilter);
        LuceneOGCFilter cf1_1 = (LuceneOGCFilter) cf1.getChain().get(0);
        assertTrue(cf1_1.getOGCFilter() instanceof Intersects);
        
        assertTrue(cf1.getChain().get(1) instanceof LuceneOGCFilter);
        LuceneOGCFilter cf1_2 = (LuceneOGCFilter) cf1.getChain().get(1);
        assertTrue(cf1_2.getOGCFilter() instanceof Contains);
        
        assertTrue(chainFilter.getChain().get(1) instanceof LuceneOGCFilter);
        LuceneOGCFilter f2 = (LuceneOGCFilter) chainFilter.getChain().get(1);
        assertTrue(f2.getOGCFilter() instanceof BBOX);
        
        /**
         * Test 3: three spatial Filter F1 AND (F2 OR F3)
         */
        cql = "INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND (CONTAINS(BoundingBox, POINT(22.07 60.23)) OR BBOX(BoundingBox, 10,20,30,40))";
        filter = FilterParser.cqlToFilter(cql);
        
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "(metafile:doc)");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SerialChainFilter);
        chainFilter = (SerialChainFilter) spaQuery.getSpatialFilter();
                
        assertEquals(chainFilter.getActionType().length,  1);
        assertEquals(chainFilter.getActionType()[0],      SerialChainFilter.AND);
        assertEquals(chainFilter.getChain().size(),       2);
        
        //we verify each filter
        LuceneOGCFilter f1 = (LuceneOGCFilter) chainFilter.getChain().get(1);
        assertTrue(f1.getOGCFilter() instanceof Intersects);
        
        SerialChainFilter cf2 = (SerialChainFilter) chainFilter.getChain().get(0);
        assertEquals(cf2.getActionType().length,  1);
        assertEquals(cf2.getActionType()[0],      SerialChainFilter.OR);
        assertEquals(cf2.getChain().size(),       2);
        
        
        LuceneOGCFilter cf2_1 = (LuceneOGCFilter) cf2.getChain().get(0);
        assertTrue(cf2_1.getOGCFilter() instanceof Contains);
        
        LuceneOGCFilter cf2_2 = (LuceneOGCFilter) cf2.getChain().get(1);
        assertTrue(cf2_2.getOGCFilter() instanceof BBOX);
        
         /**
         * Test 4: three spatial Filter (NOT F1) AND F2 AND F3
         */
        cql = "NOT INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND CONTAINS(BoundingBox, POINT(22.07 60.23)) AND BBOX(BoundingBox, 10,20,30,40)";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);

        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 1);

        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        LuceneOGCFilter f3 = (LuceneOGCFilter) ((LuceneOGCFilter) spaQuery.getSpatialFilter());
        assertTrue(f3.getOGCFilter() instanceof BBOX);

        assertTrue(spaQuery.getSubQueries().get(0).getSpatialFilter() instanceof SerialChainFilter);
        chainFilter = (SerialChainFilter) spaQuery.getSubQueries().get(0).getSpatialFilter();

        assertEquals(chainFilter.getActionType().length,  1);
        assertEquals(chainFilter.getActionType()[0],      SerialChainFilter.AND);
        assertEquals(chainFilter.getChain().size(),       2);
        
        //we verify each filter
        assertTrue(chainFilter.getChain().get(0) instanceof SerialChainFilter);
        cf1 = (SerialChainFilter) chainFilter.getChain().get(0);
        assertEquals(cf1.getChain().size(), 1);
        assertEquals(cf1.getActionType().length,  1);
        assertEquals(cf1.getActionType()[0],    SerialChainFilter.NOT);

        assertTrue(cf1.getChain().get(0) instanceof LuceneOGCFilter);
        LuceneOGCFilter cf1_cf1_1 = (LuceneOGCFilter) cf1.getChain().get(0);
        assertTrue(cf1_cf1_1.getOGCFilter() instanceof Intersects);

        assertTrue(chainFilter.getChain().get(1) instanceof LuceneOGCFilter);
        f2 = (LuceneOGCFilter) chainFilter.getChain().get(1);
        assertTrue(f2.getOGCFilter() instanceof Contains);

        /**
         * Test 5: three spatial Filter NOT (F1 OR F2) AND F3
         */
        cql = "NOT (INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) OR CONTAINS(BoundingBox, POINT(22.07 60.23))) AND BBOX(BoundingBox, 10,20,30,40)";
        filter = FilterParser.cqlToFilter(cql);
        
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "(metafile:doc)");
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
        
        assertTrue(cf1_cf1.getChain().get(0) instanceof LuceneOGCFilter);
        cf1_cf1_1 = (LuceneOGCFilter) cf1_cf1.getChain().get(0);
        assertTrue(cf1_cf1_1.getOGCFilter() instanceof Intersects);
        
        assertTrue(cf1_cf1.getChain().get(1) instanceof LuceneOGCFilter);
        LuceneOGCFilter cf1_cf1_2 = (LuceneOGCFilter) cf1_cf1.getChain().get(1);
        assertTrue(cf1_cf1_2.getOGCFilter() instanceof Contains);
        
        f2 = (LuceneOGCFilter) chainFilter.getChain().get(1);
        assertTrue(f2.getOGCFilter() instanceof BBOX);
   
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
        String cql = "Title LIKE '%VM%' AND INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))";
        FilterType filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        SpatialQuery spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "(Title:*VM*)");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        LuceneOGCFilter spaFilter = (LuceneOGCFilter) ((LuceneOGCFilter) spaQuery.getSpatialFilter());
                
        assertTrue(spaFilter.getOGCFilter() instanceof Intersects);
        
        /**
         * Test 2: PropertyIsLike AND INTERSECT AND propertyIsEquals
         */
        cql = "Title LIKE '%VM%' AND INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND Title = 'VM'";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "(Title:\"VM\")");
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.AND);
        
        SpatialQuery subQuery = spaQuery.getSubQueries().get(0);
        assertTrue(subQuery.getSpatialFilter() != null);
        assertEquals(subQuery.getQuery(), "(Title:*VM*)");
        assertEquals(subQuery.getSubQueries().size(), 0);
        
        assertTrue(subQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        spaFilter = (LuceneOGCFilter) subQuery.getSpatialFilter();
                
        assertTrue(spaFilter.getOGCFilter() instanceof Intersects);
        
        /**
         * Test 3:  INTERSECT AND propertyIsEquals AND BBOX
         */
        cql =  "INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND Title = 'VM' AND BBOX(BoundingBox, 10,20,30,40)";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getSubQueries().size(), 1);

        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        LuceneOGCFilter f1 =  (LuceneOGCFilter) ((LuceneOGCFilter) spaQuery.getSpatialFilter());
        assertTrue (f1.getOGCFilter() instanceof BBOX);
        
        subQuery = spaQuery.getSubQueries().get(0);
        assertTrue(subQuery.getSpatialFilter() != null);
        assertTrue(subQuery.getLogicalOperator() == SerialChainFilter.AND);
        
        LuceneOGCFilter f2 = (LuceneOGCFilter) subQuery.getSpatialFilter();
        assertTrue (f2.getOGCFilter() instanceof Intersects);
        assertEquals(subQuery.getQuery(), "(Title:\"VM\")");
        
        /**
         * Test 4: PropertyIsLike OR INTERSECT OR propertyIsEquals
         */
        cql = "Title LIKE '%VM%' OR INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) OR Title = 'VM'";
        filter = FilterParser.cqlToFilter(cql);
        
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "(Title:\"VM\")");
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.OR);
        
        subQuery = spaQuery.getSubQueries().get(0);
        assertEquals(subQuery.getQuery(), "(Title:*VM*)");
        assertEquals(subQuery.getSubQueries().size(), 0);
        assertEquals(subQuery.getLogicalOperator(), SerialChainFilter.OR);
        assertTrue(subQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        spaFilter = (LuceneOGCFilter) subQuery.getSpatialFilter();
                
        assertTrue(spaFilter.getOGCFilter() instanceof Intersects);
        
        /**
         * Test 5:  INTERSECT OR propertyIsEquals OR BBOX
         */
        cql = "INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) OR Title = 'VM' OR BBOX(BoundingBox, 10,20,30,40)";
        filter = FilterParser.cqlToFilter(cql);
        
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.OR);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        
        f1 = (LuceneOGCFilter) ((LuceneOGCFilter) spaQuery.getSpatialFilter());
        assertTrue (f1.getOGCFilter() instanceof  BBOX);
        
        subQuery = spaQuery.getSubQueries().get(0);
        assertEquals(subQuery.getQuery(), "(Title:\"VM\")");
        assertTrue(subQuery.getSpatialFilter() != null);
        assertEquals(subQuery.getLogicalOperator(), SerialChainFilter.OR);
        
        assertTrue(subQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        f2 = (LuceneOGCFilter) subQuery.getSpatialFilter();
        assertTrue (f2.getOGCFilter() instanceof  Intersects);
        
        /**
         * Test 6:  INTERSECT AND (propertyIsEquals OR BBOX)
         */
        cql = "INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND (Title = 'VM' OR BBOX(BoundingBox, 10,20,30,40))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.AND);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof LuceneOGCFilter);
        spaFilter = (LuceneOGCFilter) ((LuceneOGCFilter) spaQuery.getSpatialFilter());
        
        assertTrue (spaFilter.getOGCFilter() instanceof Intersects);
        
        SpatialQuery subQuery1 = spaQuery.getSubQueries().get(0);
        assertTrue  (subQuery1.getSpatialFilter() != null);
        assertEquals(subQuery1.getQuery(), "(Title:\"VM\")");
        assertEquals(subQuery1.getSubQueries().size(), 0);
        assertEquals(subQuery1.getLogicalOperator(), SerialChainFilter.OR);
        
        assertTrue(subQuery1.getSpatialFilter() instanceof LuceneOGCFilter);
        spaFilter = (LuceneOGCFilter) subQuery1.getSpatialFilter();
        
        assertTrue (spaFilter.getOGCFilter() instanceof BBOX);
        
        
        /**
         * Test 7:  propertyIsEquals OR (propertyIsLike AND BBOX)
         */
        cql = "Title = 'VMAI' OR (Title LIKE 'LO?Li' AND DWITHIN(BoundingBox, POINT(12.1 28.9), 10, meters))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "(Title:\"VMAI\")");
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.OR);
        
        subQuery1 = spaQuery.getSubQueries().get(0);
        assertTrue  (subQuery1.getSpatialFilter() != null);
        assertEquals(subQuery1.getQuery(), "(Title:LO?Li)");
        assertEquals(subQuery1.getSubQueries().size(), 0);
        assertEquals(subQuery1.getLogicalOperator(), SerialChainFilter.AND);
        
        assertTrue(subQuery1.getSpatialFilter() instanceof LuceneOGCFilter);
        spaFilter = (LuceneOGCFilter) subQuery1.getSpatialFilter();
        
        assertTrue (spaFilter.getOGCFilter() instanceof DWithin);
        

        /**
         * Test 8:  propertyIsLike AND INTERSECT AND (propertyIsEquals OR BBOX) AND (propertyIsEquals OR (Beyond AND propertyIsLike))
         * 
         * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Filter xmlns="http://www.opengis.net/ogc" xmlns:ns2="http://www.opengis.net/gml" xmlns:ns3="http://www.w3.org/1999/xlink">
    <And>
        <And>
            <And>
                <PropertyIsLike wildCard="%" singleChar="_" escapeChar="\">
                    <PropertyName>Title</PropertyName>
                    <Literal>%VM%</Literal>
                </PropertyIsLike>
                <Intersects>
                    <PropertyName>BoundingBox</PropertyName>
                    <ns2:Envelope srsName="EPSG:4326">
                        <ns2:lowerCorner>14.05 17.24</ns2:lowerCorner>
                        <ns2:upperCorner>46.46 48.26</ns2:upperCorner>
                    </ns2:Envelope>
                </Intersects>
            </And>
            <Or>
                <PropertyIsEqualTo>
                    <Literal>PLOUF</Literal>
                    <PropertyName>Title</PropertyName>
                </PropertyIsEqualTo>
                <BBOX>
                    <PropertyName>BoundingBox</PropertyName>
                    <ns2:Envelope srsName="EPSG:4326">
                        <ns2:lowerCorner>10.0 20.0</ns2:lowerCorner>
                        <ns2:upperCorner>30.0 40.0</ns2:upperCorner>
                    </ns2:Envelope>
                </BBOX>
            </Or>
        </And>
        <Or>
            <PropertyIsEqualTo>
                <Literal>VMAI</Literal>
                <PropertyName>Title</PropertyName>
            </PropertyIsEqualTo>
            <And>
                <Beyond>
                    <PropertyName>BoundingBox</PropertyName>
                    <ns2:Point srsName="EPSG:4326">
                        <ns2:pos>14.05 46.46</ns2:pos>
                    </ns2:Point>
                    <Distance units="meters">10.0</Distance>
                </Beyond>
                <PropertyIsLike wildCard="%" singleChar="_" escapeChar="\">
                    <PropertyName>Title</PropertyName>
                    <Literal>LO?Li</Literal>
                </PropertyIsLike>
            </And>
        </Or>
    </And>
</Filter>
         */
        cql = "Title Like '%VM%' AND INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND (Title = 'PLOUF' OR BBOX(BoundingBox, 10,20,30,40)) AND (Title = 'VMAI' OR (BEYOND(BoundingBox, POINT(14.05 46.46), 10, meters) AND Title LIKE 'LO?Li'))";
        filter = FilterParser.cqlToFilter(cql);
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = (SpatialQuery) filterParser.getQuery(new QueryConstraintType(cql, "1.1.0"), null, null);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 2);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.AND);
        
        subQuery1 = spaQuery.getSubQueries().get(0);
        assertEquals(subQuery1.getQuery(), "metafile:doc");
        assertTrue  (subQuery1.getSpatialFilter() == null);
        assertEquals(subQuery1.getSubQueries().size(), 2);
        assertEquals(subQuery1.getLogicalOperator(), SerialChainFilter.AND);
        
        SpatialQuery subQuery1_1 = subQuery1.getSubQueries().get(0);
        assertEquals(subQuery1_1.getLogicalOperator(), SerialChainFilter.AND);
        assertEquals(subQuery1_1.getQuery(), "(Title:*VM*)");
        assertTrue  (subQuery1_1.getSpatialFilter() != null);
        assertTrue  (subQuery1_1.getSpatialFilter() instanceof LuceneOGCFilter);
        spaFilter = (LuceneOGCFilter) subQuery1_1.getSpatialFilter();
        
        assertTrue (spaFilter.getOGCFilter() instanceof Intersects);
        
        SpatialQuery subQuery1_2 = subQuery1.getSubQueries().get(1);
        assertTrue  (subQuery1_2.getSpatialFilter() != null);
        assertEquals(subQuery1_2.getQuery(), "(Title:\"PLOUF\")");
        assertEquals(subQuery1_2.getSubQueries().size(), 0);
        assertEquals(subQuery1_2.getLogicalOperator(), SerialChainFilter.OR);
        
        assertTrue(subQuery1_2.getSpatialFilter() instanceof LuceneOGCFilter);
        spaFilter = (LuceneOGCFilter) subQuery1_2.getSpatialFilter();
        
        assertTrue (spaFilter.getOGCFilter() instanceof  BBOX);
        
        SpatialQuery subQuery2 = spaQuery.getSubQueries().get(1);
        assertTrue  (subQuery2.getSpatialFilter() == null);
        assertEquals(subQuery2.getQuery(), "(Title:\"VMAI\")");
        assertEquals(subQuery2.getSubQueries().size(), 1);
        assertEquals(subQuery2.getLogicalOperator(), SerialChainFilter.OR);
        
        SpatialQuery subQuery2_1 = subQuery2.getSubQueries().get(0);
        assertTrue  (subQuery2_1.getSpatialFilter() != null);
        assertEquals(subQuery2_1.getQuery(), "(Title:LO?Li)");
        assertEquals(subQuery2_1.getSubQueries().size(), 0);
        assertEquals(subQuery2_1.getLogicalOperator(), SerialChainFilter.AND);
        
        assertTrue(subQuery2_1.getSpatialFilter() instanceof LuceneOGCFilter);
        spaFilter = (LuceneOGCFilter) subQuery2_1.getSpatialFilter();
        
        assertTrue (spaFilter.getOGCFilter() instanceof Beyond);
        
    }
}
