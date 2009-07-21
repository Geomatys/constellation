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
import java.awt.geom.Line2D;
import java.util.logging.Logger;

// Constellation dependencies 
import org.constellation.lucene.filter.BBOXFilter;
import org.constellation.lucene.filter.BeyondFilter;
import org.constellation.lucene.filter.ContainsFilter;
import org.constellation.lucene.filter.CrossesFilter;
import org.constellation.lucene.filter.DWithinFilter;
import org.constellation.lucene.filter.DisjointFilter;
import org.constellation.lucene.filter.DistanceFilter;
import org.constellation.lucene.filter.EqualsFilter;
import org.constellation.lucene.filter.IntersectFilter;
import org.constellation.lucene.filter.OverlapsFilter;
import org.constellation.lucene.filter.SerialChainFilter;
import org.constellation.lucene.filter.SpatialFilter;
import org.constellation.lucene.filter.SpatialQuery;
import org.constellation.lucene.filter.TouchesFilter;
import org.constellation.lucene.filter.WithinFilter;

// Geotools dependencies
import org.geotoolkit.filter.FilterFactoryImpl;
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.geometry.GeneralEnvelope;

// JUnit dependencies
import org.geotoolkit.ogc.xml.v110modified.FilterType;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * A suite of test verifying the transformation of an CQL request into a Lucene Query/filter 
 * 
 * @author Guilhem Legal
 */
public class CQLParserTest {
    
    private LuceneFilterParser filterParser;
    private Logger       logger = Logger.getLogger("org.constellation.filter");
    private FilterFactoryImpl filterFactory = new FilterFactoryImpl();
    
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
        
        FilterType filter = filterParser.cqlToFilter("Title LIKE 'VM%'");
        
        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        SpatialQuery spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "Title:VM*");
        
        /**
         *  Test 2: PropertyIsEquals
         */
        
        filter = filterParser.cqlToFilter("Title ='VM'");
        
        
        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "Title:\"VM\"");
        
        /**
         *  Test 3: PropertyIsNotEquals
         */
        
        filter =filterParser.cqlToFilter("Title <>'VM'");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "Title:\"VM\"");
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.NOT);
        
        /**
         * Test 4: PropertyIsNull
         */ 
        filter = filterParser.cqlToFilter("Title IS NULL");
        
        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "Title:null");
    
        /**
         * Test 5: PropertyIsGreaterThan
         */
        filter = filterParser.cqlToFilter("CreationDate AFTER 2007-06-02T00:00:00Z");
        
        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "CreationDate:{20070602  30000101}");
        
         /**
         * Test 6: PropertyIsLessThan
         */
        filter = filterParser.cqlToFilter("CreationDate BEFORE 2007-06-02T00:00:00Z");
        
        assertTrue(filter.getComparisonOps() != null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "CreationDate:{00000101 20070602}");
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
        FilterType filter = filterParser.cqlToFilter("Title = 'starship trooper' AND Author = 'Timothee Gustave'");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        SpatialQuery spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "(Title:\"starship trooper\" AND Author:\"Timothee Gustave\")");
        
        
        /**
         * Test 2: OR between two propertyIsEqualTo 
         */
        
        filter = filterParser.cqlToFilter("Title = 'starship trooper' OR Author = 'Timothee Gustave'");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "(Title:\"starship trooper\" OR Author:\"Timothee Gustave\")");
        
        /**
         * Test 3:  OR between three propertyIsEqualTo 
         */
        filter = filterParser.cqlToFilter("Title = 'starship trooper' OR Author = 'Timothee Gustave' OR Id = '268'");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getSubQueries().size(), 0);
        assertEquals(spaQuery.getQuery(), "(Id:\"268\" OR (Title:\"starship trooper\" OR Author:\"Timothee Gustave\"))");
        
        /**
         * Test 4: Not propertyIsEqualTo 
         */
        
        filter = filterParser.cqlToFilter("NOT Title = 'starship trooper'");
        
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
     * Test simple Spatial CQL query
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void simpleSpatialFilterTest() throws Exception {
        
        /**
         * Test 1: a simple spatial Filter Intersects 
         */
        FilterType filter = filterParser.cqlToFilter("INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.42)) ");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        SpatialQuery spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        SpatialFilter spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof IntersectFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 2: a simple Distance Filter DWithin
         */
        filter = filterParser.cqlToFilter("DWITHIN(BoundingBox, POINT(12.1 28.9), 10, meters)");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof DistanceFilter);
        DistanceFilter Dfilter= (DistanceFilter) spaQuery.getSpatialFilter();
                
        assertTrue(Dfilter instanceof  DWithinFilter);
        assertTrue(Dfilter.getGeometry() instanceof GeneralDirectPosition);
        assertEquals(Dfilter.getDistanceUnit(), "meters");
        assertTrue(Dfilter.getDistance() == 10.0);
        
        /**
         * Test 3: a simple Distance Filter Beyond
         */
        filter = filterParser.cqlToFilter("BEYOND(BoundingBox, POINT(12.1 28.9), 10, meters)");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof DistanceFilter);
        Dfilter = (DistanceFilter) spaQuery.getSpatialFilter();
                
        assertTrue(Dfilter instanceof BeyondFilter);
        assertTrue(Dfilter.getGeometry() instanceof GeneralDirectPosition);
        assertEquals(Dfilter.getDistanceUnit(), "meters");
        assertTrue(Dfilter.getDistance() == 10.0);
        
        /**
         * Test 4: a simple BBOX filter
         */
        filter = filterParser.cqlToFilter("BBOX(BoundingBox, 10,20,30,40)");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof BBOXFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 4: a simple Contains filter
         */
        filter = filterParser.cqlToFilter("CONTAINS(BoundingBox, POINT(14.05 46.46))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof ContainsFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralDirectPosition);
        
        /**
         * Test 5: a simple Contains filter
         */
        filter = filterParser.cqlToFilter("CONTAINS(BoundingBox, LINESTRING(1 2, 10 15))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof  ContainsFilter);
        assertTrue(spatialFilter.getGeometry() instanceof Line2D);
        
        /*
         * Test 6: a simple Contains filter
         */
        filter = filterParser.cqlToFilter("CONTAINS(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof ContainsFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralEnvelope);
        
         /**
         * Test 7: a simple Crosses filter
         */
        filter = filterParser.cqlToFilter("CROSS(BoundingBox, POINT(14.05 46.46))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof CrossesFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralDirectPosition);
        
        /**
         * Test 8: a simple Crosses filter
         */
        filter = filterParser.cqlToFilter("CROSS(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof CrossesFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 9: a simple Disjoint filter
         */
        filter = filterParser.cqlToFilter("DISJOINT(BoundingBox, POINT(14.05 46.46))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof  DisjointFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralDirectPosition);
        
        /**
         * Test 10: a simple Disjoint filter
         */
        filter = filterParser.cqlToFilter("DISJOINT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof DisjointFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 11: a simple Equals filter
         */
        filter = filterParser.cqlToFilter("EQUAL(BoundingBox, POINT(14.05 46.46))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof  EqualsFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralDirectPosition);
        
        /**
         * Test 12: a simple Equals filter
         */
        filter = filterParser.cqlToFilter("EQUAL(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof  EqualsFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 13: a simple Overlaps filter
         */
        filter = filterParser.cqlToFilter("OVERLAP(BoundingBox, POINT(14.05 46.46))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof OverlapsFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralDirectPosition);
        
        /**
         * Test 14: a simple Overlaps filter
         */
        filter = filterParser.cqlToFilter("OVERLAP(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof OverlapsFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 15: a simple Touches filter
         */
        filter = filterParser.cqlToFilter("TOUCH(BoundingBox, POINT(14.05 46.46))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof TouchesFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralDirectPosition);
        
        /**
         * Test 16: a simple Touches filter
         */
        filter = filterParser.cqlToFilter("TOUCH(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof TouchesFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 17: a simple Within filter
         */
        filter = filterParser.cqlToFilter("WITHIN(BoundingBox, POINT(14.05 46.46))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof WithinFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralDirectPosition);
        
        /**
         * Test 18: a simple Within filter
         */
        filter = filterParser.cqlToFilter("WITHIN(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      == null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    != null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spatialFilter = (SpatialFilter) spaQuery.getSpatialFilter();
                
        assertTrue(spatialFilter instanceof WithinFilter);
        assertTrue(spatialFilter.getGeometry() instanceof GeneralEnvelope);
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
        FilterType filter = filterParser.cqlToFilter("INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND OVERLAP(BoundingBox, ENVELOPE(22.07, 60.23, 11.69, 73.48))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        SpatialQuery spaQuery = filterParser.getLuceneQuery(filter);
        
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

        filter = filterParser.cqlToFilter("INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) OR CONTAINS(BoundingBox, POINT(22.07 60.23)) OR BBOX(BoundingBox, 10,20,30,40)");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
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
        
        assertTrue(cf1.getChain().get(0) instanceof SpatialFilter);
        SpatialFilter cf1_1 = (SpatialFilter) cf1.getChain().get(0);
        assertTrue(cf1_1 instanceof IntersectFilter);
        assertTrue(cf1_1.getGeometry() instanceof GeneralEnvelope);
        
        assertTrue(cf1.getChain().get(1) instanceof SpatialFilter);
        SpatialFilter cf1_2 = (SpatialFilter) cf1.getChain().get(1);
        assertTrue(cf1_2 instanceof ContainsFilter);
        assertTrue  (cf1_2.getGeometry() instanceof GeneralDirectPosition);
        
        assertTrue(chainFilter.getChain().get(1) instanceof SpatialFilter);
        SpatialFilter f2 = (SpatialFilter) chainFilter.getChain().get(1);
        assertTrue(f2 instanceof BBOXFilter);
        assertTrue(  f2.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 3: three spatial Filter F1 AND (F2 OR F3)
         */

        filter = filterParser.cqlToFilter("INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND (CONTAINS(BoundingBox, POINT(22.07 60.23)) OR BBOX(BoundingBox, 10,20,30,40))");
        
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "(metafile:doc)");
        assertEquals(spaQuery.getSubQueries().size(), 0);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SerialChainFilter);
        chainFilter = (SerialChainFilter) spaQuery.getSpatialFilter();
                
        assertEquals(chainFilter.getActionType().length,  1);
        assertEquals(chainFilter.getActionType()[0],      SerialChainFilter.AND);
        assertEquals(chainFilter.getChain().size(),       2);
        
        //we verify each filter
        SpatialFilter f1 = (SpatialFilter) chainFilter.getChain().get(1);
        assertTrue(f1 instanceof IntersectFilter);
        assertTrue(f1.getGeometry() instanceof GeneralEnvelope);
        
        SerialChainFilter cf2 = (SerialChainFilter) chainFilter.getChain().get(0);
        assertEquals(cf2.getActionType().length,  1);
        assertEquals(cf2.getActionType()[0],      SerialChainFilter.OR);
        assertEquals(cf2.getChain().size(),       2);
        
        
        SpatialFilter cf2_1 = (SpatialFilter) cf2.getChain().get(0);
        assertTrue(cf2_1 instanceof ContainsFilter);
        assertTrue  (cf2_1.getGeometry() instanceof GeneralDirectPosition);
        
        SpatialFilter cf2_2 = (SpatialFilter) cf2.getChain().get(1);
        assertTrue(cf2_2 instanceof BBOXFilter);
        assertTrue  (cf2_2.getGeometry() instanceof GeneralEnvelope);
        
         /**
         * Test 4: three spatial Filter (NOT F1) AND F2 AND F3
         */
       
        filter = filterParser.cqlToFilter("NOT INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND CONTAINS(BoundingBox, POINT(22.07 60.23)) AND BBOX(BoundingBox, 10,20,30,40)");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);

        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 1);

        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        SpatialFilter f3 = (SpatialFilter) spaQuery.getSpatialFilter();
        assertTrue(f3 instanceof BBOXFilter);
        assertTrue(f3.getGeometry() instanceof GeneralEnvelope);

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

        assertTrue(cf1.getChain().get(0) instanceof SpatialFilter);
        SpatialFilter cf1_cf1_1 = (SpatialFilter) cf1.getChain().get(0);
        assertTrue(cf1_cf1_1 instanceof IntersectFilter);
        assertTrue  (cf1_cf1_1.getGeometry() instanceof GeneralEnvelope);

        assertTrue(chainFilter.getChain().get(1) instanceof SpatialFilter);
        f2 = (SpatialFilter) chainFilter.getChain().get(1);
        assertTrue(f2 instanceof ContainsFilter);
        assertTrue  (f2.getGeometry() instanceof GeneralDirectPosition);

        /**
         * Test 5: three spatial Filter NOT (F1 OR F2) AND F3
         */
        
        filter = filterParser.cqlToFilter("NOT (INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) OR CONTAINS(BoundingBox, POINT(22.07 60.23))) AND BBOX(BoundingBox, 10,20,30,40)");
        
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
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
        
        assertTrue(cf1_cf1.getChain().get(0) instanceof SpatialFilter);
        cf1_cf1_1 = (SpatialFilter) cf1_cf1.getChain().get(0);
        assertTrue(cf1_cf1_1 instanceof IntersectFilter);
        assertTrue  (cf1_cf1_1.getGeometry() instanceof GeneralEnvelope);
        
        assertTrue(cf1_cf1.getChain().get(1) instanceof SpatialFilter);
        SpatialFilter cf1_cf1_2 = (SpatialFilter) cf1_cf1.getChain().get(1);
        assertTrue(cf1_cf1_2 instanceof ContainsFilter);
        assertTrue  (cf1_cf1_2.getGeometry() instanceof GeneralDirectPosition);
        
        f2 = (SpatialFilter) chainFilter.getChain().get(1);
        assertTrue(f2 instanceof BBOXFilter);
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
        
        FilterType filter = filterParser.cqlToFilter("Title LIKE '%VM%' AND INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26))");
        
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
                
        assertTrue(spaFilter instanceof IntersectFilter);
        assertTrue  (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 2: PropertyIsLike AND INTERSECT AND propertyIsEquals
         */
        
        filter = filterParser.cqlToFilter("Title LIKE '%VM%' AND INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND Title = 'VM'");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "(Title:\"VM\")");
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.AND);
        
        SpatialQuery subQuery = spaQuery.getSubQueries().get(0);
        assertTrue(subQuery.getSpatialFilter() != null);
        assertEquals(subQuery.getQuery(), "(Title:*VM*)");
        assertEquals(subQuery.getSubQueries().size(), 0);
        
        assertTrue(subQuery.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) subQuery.getSpatialFilter();
                
        assertTrue(spaFilter instanceof IntersectFilter);
        assertTrue  (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 3:  INTERSECT AND propertyIsEquals AND BBOX
         */
        
        filter = filterParser.cqlToFilter("INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND Title = 'VM' AND BBOX(BoundingBox, 10,20,30,40)");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getSubQueries().size(), 1);

        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        SpatialFilter f1 =  (SpatialFilter) spaQuery.getSpatialFilter();
        assertTrue (f1 instanceof BBOXFilter);
        assertTrue   (f1.getGeometry() instanceof GeneralEnvelope);
        
        subQuery = spaQuery.getSubQueries().get(0);
        assertTrue(subQuery.getSpatialFilter() != null);
        assertTrue(subQuery.getLogicalOperator() == SerialChainFilter.AND);
        
        SpatialFilter f2 = (SpatialFilter) subQuery.getSpatialFilter();
        assertTrue (f2 instanceof IntersectFilter);
        assertTrue   (f2.getGeometry() instanceof GeneralEnvelope);
        assertEquals(subQuery.getQuery(), "(Title:\"VM\")");
        
        /**
         * Test 4: PropertyIsLike OR INTERSECT OR propertyIsEquals
         */
        filter = filterParser.cqlToFilter("Title LIKE '%VM%' OR INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) OR Title = 'VM'");
        
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "(Title:\"VM\")");
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.OR);
        
        subQuery = spaQuery.getSubQueries().get(0);
        assertEquals(subQuery.getQuery(), "(Title:*VM*)");
        assertEquals(subQuery.getSubQueries().size(), 0);
        assertEquals(subQuery.getLogicalOperator(), SerialChainFilter.OR);
        assertTrue(subQuery.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) subQuery.getSpatialFilter();
                
        assertTrue(spaFilter instanceof IntersectFilter);
        assertTrue  (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 5:  INTERSECT OR propertyIsEquals OR BBOX
         */
        
        filter = filterParser.cqlToFilter("INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) OR Title = 'VM' OR BBOX(BoundingBox, 10,20,30,40)");
        
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.OR);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        
        f1 = (SpatialFilter) spaQuery.getSpatialFilter();
        assertTrue (f1 instanceof  BBOXFilter);
        assertTrue   (f1.getGeometry() instanceof GeneralEnvelope);
        
        subQuery = spaQuery.getSubQueries().get(0);
        assertEquals(subQuery.getQuery(), "(Title:\"VM\")");
        assertTrue(subQuery.getSpatialFilter() != null);
        assertEquals(subQuery.getLogicalOperator(), SerialChainFilter.OR);
        
        assertTrue(subQuery.getSpatialFilter() instanceof SpatialFilter);
        f2 = (SpatialFilter) subQuery.getSpatialFilter();
        assertTrue (f2 instanceof  IntersectFilter);
        assertTrue   (f2.getGeometry() instanceof GeneralEnvelope);
        
        /**
         * Test 6:  INTERSECT AND (propertyIsEquals OR BBOX)
         */
        
        filter = filterParser.cqlToFilter("INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND (Title = 'VM' OR BBOX(BoundingBox, 10,20,30,40))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() != null);
        assertEquals(spaQuery.getQuery(), "metafile:doc");
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.AND);
        
        assertTrue(spaQuery.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) spaQuery.getSpatialFilter();
        
        assertTrue (spaFilter instanceof IntersectFilter);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
        SpatialQuery subQuery1 = spaQuery.getSubQueries().get(0);
        assertTrue  (subQuery1.getSpatialFilter() != null);
        assertEquals(subQuery1.getQuery(), "(Title:\"VM\")");
        assertEquals(subQuery1.getSubQueries().size(), 0);
        assertEquals(subQuery1.getLogicalOperator(), SerialChainFilter.OR);
        
        assertTrue(subQuery1.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) subQuery1.getSpatialFilter();
        
        assertTrue (spaFilter instanceof BBOXFilter);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
        
        /**
         * Test 7:  propertyIsEquals OR (propertyIsLike AND BBOX)
         */
        
        filter = filterParser.cqlToFilter("Title = 'VMAI' OR (Title LIKE 'LO?Li' AND DWITHIN(BoundingBox, POINT(12.1 28.9), 10, meters))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
        assertTrue(spaQuery.getSpatialFilter() == null);
        assertEquals(spaQuery.getQuery(), "(Title:\"VMAI\")");
        assertEquals(spaQuery.getSubQueries().size(), 1);
        assertEquals(spaQuery.getLogicalOperator(), SerialChainFilter.OR);
        
        subQuery1 = spaQuery.getSubQueries().get(0);
        assertTrue  (subQuery1.getSpatialFilter() != null);
        assertEquals(subQuery1.getQuery(), "(Title:LO?Li)");
        assertEquals(subQuery1.getSubQueries().size(), 0);
        assertEquals(subQuery1.getLogicalOperator(), SerialChainFilter.AND);
        
        assertTrue(subQuery1.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) subQuery1.getSpatialFilter();
        
        assertTrue (spaFilter instanceof DWithinFilter);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralDirectPosition);
        

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
        
        filter = filterParser.cqlToFilter("Title Like '%VM%' AND INTERSECT(BoundingBox, ENVELOPE(14.05, 46.46, 17.24, 48.26)) AND (Title = 'PLOUF' OR BBOX(BoundingBox, 10,20,30,40)) AND (Title = 'VMAI' OR (BEYOND(BoundingBox, POINT(14.05 46.46), 10, meters) AND Title LIKE 'LO?Li'))");
        
        assertTrue(filter.getComparisonOps() == null);
        assertTrue(filter.getLogicOps()      != null);
        assertTrue(filter.getId().size()     == 0   );
        assertTrue(filter.getSpatialOps()    == null);
        
        spaQuery = filterParser.getLuceneQuery(filter);
        
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
        assertTrue  (subQuery1_1.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) subQuery1_1.getSpatialFilter();
        
        assertTrue (spaFilter instanceof IntersectFilter);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
        SpatialQuery subQuery1_2 = subQuery1.getSubQueries().get(1);
        assertTrue  (subQuery1_2.getSpatialFilter() != null);
        assertEquals(subQuery1_2.getQuery(), "(Title:\"PLOUF\")");
        assertEquals(subQuery1_2.getSubQueries().size(), 0);
        assertEquals(subQuery1_2.getLogicalOperator(), SerialChainFilter.OR);
        
        assertTrue(subQuery1_2.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) subQuery1_2.getSpatialFilter();
        
        assertTrue (spaFilter instanceof  BBOXFilter);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralEnvelope);
        
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
        
        assertTrue(subQuery2_1.getSpatialFilter() instanceof SpatialFilter);
        spaFilter = (SpatialFilter) subQuery2_1.getSpatialFilter();
        
        assertTrue (spaFilter instanceof BeyondFilter);
        assertTrue   (spaFilter.getGeometry() instanceof GeneralDirectPosition);
        
    }
}
