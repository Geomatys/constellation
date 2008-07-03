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

package net.seagis.lucene;

import java.awt.geom.Line2D;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.seagis.lucene.Filter.SerialChainFilter;
import net.seagis.lucene.Filter.SpatialFilter;
import net.seagis.lucene.Filter.SpatialQuery;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.RAMDirectory;

import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.junit.*;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import static org.junit.Assert.*;


/**
 *
 * @author guilhem
 */
public class LuceneTest {
    
    private RAMDirectory  directory;
    private IndexSearcher searcher;
    private Query         simpleQuery;
    private Logger logger = Logger.getLogger("net.seagis.lucene");

   
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        directory = new RAMDirectory();
	IndexWriter writer = new IndexWriter(directory, new WhitespaceAnalyzer(), true);
	fillTestData(writer);
        searcher = new IndexSearcher(directory);
        //create a term query to search against all documents
        simpleQuery = new TermQuery(new Term("metafile", "doc"));
    }

    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * Test the spatial filter BBOX.
     */
    @Test
    public void BBOXTest() throws Exception {

        /*
         * first bbox
         */ 
        double min1[] = {-20, -20};
        double max1[] = { 20,  20};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        bbox.setCoordinateReferenceSystem(crs);
        SpatialQuery bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.BBOX);

        //we perform a lucene query
        Hits hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        int nbResults = hits.length();
        logger.finer("BBOX:BBOX 1 CRS=4326: nb Results: " + nbResults);
        
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results
        assertEquals(nbResults, 7);
        assertTrue(results.contains("point 1"));
        assertTrue(results.contains("point 1 projected"));
        assertTrue(results.contains("point 2"));
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("box 2"));
        assertTrue(results.contains("box 2 projected"));
        assertTrue(results.contains("line 2"));
        
        /*
         * The same box in a diferent crs
         */ 
        double min2[] = {-2226389.8158654715, -2258423.6490963786};
        double max2[] = { 2226389.8158654715,  2258423.6490963805};
        bbox = new GeneralEnvelope(min2, max2);
        bbox.setCoordinateReferenceSystem(CRS.decode("EPSG:3395", true));
        bboxQuery = new SpatialQuery(bbox, "EPSG:3395", SpatialFilter.BBOX);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BBOX:BBOX 1 CRS= 3395: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results
        assertEquals(nbResults, 7);
        assertTrue(results.contains("point 1"));
        assertTrue(results.contains("point 1 projected"));
        assertTrue(results.contains("point 2"));
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("box 2"));
        assertTrue(results.contains("box 2 projected"));
        assertTrue(results.contains("line 2"));

        /*
         * second bbox
         */ 
        double min3[] = { -5, -5};
        double max3[] = { 60,  60};
        bbox = new GeneralEnvelope(min3, max3);
        bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.BBOX);
        
        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());
        
        nbResults = hits.length();
        logger.finer("BBOX:BBOX 2 CRS= 4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
         //we verify that we obtain the correct results
        assertEquals(nbResults, 7);
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("box 2"));
        assertTrue(results.contains("box 2 projected"));
        assertTrue(results.contains("line 1"));
        assertTrue(results.contains("line 1 projected"));
    }
    
    /**
     * Test the spatial filter INTERSECT.
     */
    @Test
    public void intersectTest() throws Exception {

        /*
         * case 1: bbox.
         */ 
        double min1[] = {-20, -20};
        double max1[] = { 20,  20};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        bbox.setCoordinateReferenceSystem(crs);
        SpatialQuery bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.INTERSECT);

        //we perform a lucene query
        Hits hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        int nbResults = hits.length();
        logger.finer("INTER:BBOX 1 CRS=4326: nb Results: " + nbResults);
        
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 10);
        assertTrue(results.contains("point 1"));
        assertTrue(results.contains("point 1 projected"));
        assertTrue(results.contains("point 2"));
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("box 2"  ));
        assertTrue(results.contains("box 2 projected"));
        assertTrue(results.contains("box 4"  ));
        assertTrue(results.contains("line 1" ));
        assertTrue(results.contains("line 1 projected"));
        assertTrue(results.contains("line 2" )); 
        
        /*
         * case 2: The same box in a diferent crs.
         */ 
        double min2[] = {-2226389.8158654715, -2258423.6490963786};
        double max2[] = { 2226389.8158654715,  2258423.6490963805};
        bbox = new GeneralEnvelope(min2, max2);
        bbox.setCoordinateReferenceSystem(CRS.decode("EPSG:3395", true));
        bboxQuery = new SpatialQuery(bbox, "EPSG:3395", SpatialFilter.INTERSECT);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("INTER:BBOX 1 CRS= 3395: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results
        assertEquals(nbResults, 10);
        assertTrue(results.contains("point 1"));
        assertTrue(results.contains("point 1 projected"));
        assertTrue(results.contains("point 2"));
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("box 2"  ));
        assertTrue(results.contains("box 2 projected"));
        assertTrue(results.contains("box 4"  ));
        assertTrue(results.contains("line 1" ));
        assertTrue(results.contains("line 1 projected"));
        assertTrue(results.contains("line 2" ));
        
        /*
         * case 3: line
         */ 
        Line2D line = new Line2D.Double(7, 30, 7,-30);
        bboxQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.INTERSECT);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("INTER:Line 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 4);
        assertTrue(results.contains("box 2"));
        assertTrue(results.contains("box 2 projected"));
        assertTrue(results.contains("line 1" ));
        assertTrue(results.contains("line 1 projected"));
        
        /*
         * case 4: same line diferent CRS
         */ 
        line = new Line2D.Double(775978.5043848383, 3339584.723798207, 775978.5043848383, -3339584.723798207);
        bboxQuery = new SpatialQuery(line, "EPSG:3395", SpatialFilter.INTERSECT);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("INTER:Line 1 CRS=3395: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 4);
        assertTrue(results.contains("box 2"  ));
        assertTrue(results.contains("box 2 projected"));
        assertTrue(results.contains("line 1" ));
        assertTrue(results.contains("line 1 projected"));
        
        /*
         * case 5: another line
         */ 
        line = new Line2D.Double(40, 40, 40, -30);
        bboxQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.INTERSECT);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("INTER:Line 2 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 2);
        assertTrue(results.contains("box 3"  ));
        assertTrue(results.contains("point 4"));
        
        /*
         * case 6: same line another CRS
         */ 
        line = new Line2D.Double(4452779.631730943, 4838471.398061137, 4452779.631730943, -3339584.723798207);
        bboxQuery = new SpatialQuery(line, "EPSG:3395", SpatialFilter.INTERSECT);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("INTER:Line 2 CRS=3395: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 2);
        assertTrue(results.contains("box 3"  ));
        assertTrue(results.contains("point 4"));
    }
    
    /**
     * Test the spatial filter EQUALS.
     */
    @Test
    public void equalsTest() throws Exception {
        
        /*
         * case 1: bbox.
         */ 
        double min1[] = { 30,   0};
        double max1[] = { 50,  15};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        bbox.setCoordinateReferenceSystem(crs);
        SpatialQuery bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.EQUALS);

        //we perform a lucene query
        Hits hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        int nbResults = hits.length();
        logger.finer("EQ:BBOX 1 CRS=4326: nb Results: " + nbResults);
        
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 1);
        assertTrue(results.contains("box 3"));
        
        
        /*
         * case 2: line
         */ 
        Line2D line = new Line2D.Double(0, 0, 25,0);
        bboxQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.EQUALS);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("EQ:Line 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 1);
        assertTrue(results.contains("line 1" ));
        
        //TODO  issue here the projected line does not have the exact same coordinates (this issue happen for all geometry in Equals)
        //assertTrue(results.contains("line 1 projected"));
        
        /*
         * case 3: point
         */ 
        GeneralDirectPosition point = new GeneralDirectPosition(-10, 10);
        point.setCoordinateReferenceSystem(crs);
        bboxQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.EQUALS);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("EQ:Point 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
         //we verify that we obtain the correct results.
        assertEquals(nbResults, 1);
        assertTrue(results.contains("point 1" ));
    }
    
    /**
     * Test the spatial filter CONTAINS.
     */
    @Test
    public void containsTest() throws Exception {
        
        /*
         * case 1: BOX/BOX.
         */ 
        double min1[] = {-30, -47};
        double max1[] = {-26, -42};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        bbox.setCoordinateReferenceSystem(crs);
        SpatialQuery bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.CONTAINS);

        //we perform a lucene query
        Hits hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        int nbResults = hits.length();
        logger.finer("CT:BBOX 1 CRS=4326: nb Results: " + nbResults);
        
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 1);
        assertTrue(results.contains("box 1"));
        
        /*
         * case 2: BOX/Line
         */ 
        Line2D line = new Line2D.Double(-25, 5, -15, 5);
        bboxQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.CONTAINS);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("CT:Line 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 1);
        assertTrue(results.contains("box 4"));
        
        /*
         * case 3: BOX/point
         */ 
        GeneralDirectPosition point = new GeneralDirectPosition(-25, 5);
        point.setCoordinateReferenceSystem(crs);
        bboxQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.CONTAINS);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("CT:Point 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 1);
        assertTrue(results.contains("box 4"));
        
        /*
         * case 4: Line/point
         */ 
        point = new GeneralDirectPosition(20, 0);
        point.setCoordinateReferenceSystem(crs);
        bboxQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.CONTAINS);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("CT:Point 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 2);
        assertTrue(results.contains("line 1"));
        assertTrue(results.contains("line 1 projected"));
        
        /*
         * case 5: Line/Line
         */ 
        line = new Line2D.Double(20, 0, 15, 0);
        point.setCoordinateReferenceSystem(crs);
        bboxQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.CONTAINS);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("CT:Line 2 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 2);
        assertTrue(results.contains("line 1"));
        assertTrue(results.contains("line 1 projected"));
    }
    
    /**
     * Test the spatial filter DISJOINT.
     */
    @Test
    public void disjointTest() throws Exception {
        
        /*
         * case 1: point
         * 
         */ 
        GeneralDirectPosition point = new GeneralDirectPosition(-25, 5);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        point.setCoordinateReferenceSystem(crs);
        SpatialQuery spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.DISJOINT);

        //we perform a lucene query
        Hits hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        int nbResults = hits.length();
        logger.finer("DJ:Point 1 CRS=4326: nb Results: " + nbResults);
        
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 13);
        assertTrue(results.contains("point 1"));
        assertTrue(results.contains("point 1 projected"));
        assertTrue(results.contains("point 2"));
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        assertTrue(results.contains("box 2"));
        assertTrue(results.contains("box 2 projected"));
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        
        /*
         * case 2: another point intersecting with the two registered lines.
         *  (equals to point 3)
         * 
         */ 
        point = new GeneralDirectPosition(0, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.DISJOINT);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DJ:Point 2 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 10);
        assertTrue(results.contains("point 1"));
        assertTrue(results.contains("point 1 projected"));
        assertTrue(results.contains("point 2"));
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        assertTrue(results.contains("box 2"));
        assertTrue(results.contains("box 2 projected"));
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("box 4"));
        
        /*
         * case 3: a line
         * 
         */ 
        Line2D line = new Line2D.Double(-40, 0, 30, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.DISJOINT);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DJ:Line 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 7);
        assertTrue(results.contains("point 1"));
        assertTrue(results.contains("point 1 projected"));
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        assertTrue(results.contains("box 2"));
        assertTrue(results.contains("box 2 projected"));
        
        /*
         * case 4: another line
         * 
         */ 
        line = new Line2D.Double(7, 40, 7, -20);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.DISJOINT);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DJ:Line 2 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 10);
        assertTrue(results.contains("point 1"));
        assertTrue(results.contains("point 1 projected"));
        assertTrue(results.contains("point 2"));
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("box 4"));
        assertTrue(results.contains("line 2"));
        
        /*
         * case 5: a BBOX
         * 
         */ 
        double min1[] = {-20, -20};
        double max1[] = { 20,  20};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        bbox.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.DISJOINT);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DJ:BBox 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 4);
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"  ));
        assertTrue(results.contains("box 3"  ));
        
        /*
         * case 6: another BBOX
         * 
         */ 
        double min2[] = {-50, -60};
        double max2[] = { -5,  60};
        bbox = new GeneralEnvelope(min2, max2);
        bbox.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.DISJOINT);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DJ:BBox 2 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 8);
        assertTrue(results.contains("point 3"));
	assertTrue(results.contains("point 4"));
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
	assertTrue(results.contains("box 3"));
	assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));

        
    }
    
     /**
     * Test the spatial filter TOUCHES.
     */
    @Test
    public void touchesTest() throws Exception {
        
        /*
         * case 1: point (equals to point 3)
         * 
         */ 
        GeneralDirectPosition point = new GeneralDirectPosition(0, 0);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        point.setCoordinateReferenceSystem(crs);
        SpatialQuery spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.TOUCHES);

        //we perform a lucene query
        Hits hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        int nbResults = hits.length();
        logger.finer("TO:Point 1 CRS=4326: nb Results: " + nbResults);
        
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 4);
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        
        /*
         * case 2: another point
         * 
         */ 
        point = new GeneralDirectPosition(-30, 5);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.TOUCHES);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("TO:Point 2 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 1);
	assertTrue(results.contains("box 4"));
        
        /*
         * case 3: another point
         * 
         */ 
        point = new GeneralDirectPosition(-25, -50);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.TOUCHES);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("TO:Point 3 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 1);
	assertTrue(results.contains("box 1"));
        
        /*
         * case 4: another point
         * 
         */ 
        point = new GeneralDirectPosition( 0, -10);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.TOUCHES);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("TO:Point 4 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 1);
	assertTrue(results.contains("line 2"));
        
        /*
         * case 5: another point
         * 
         */ 
        point = new GeneralDirectPosition( 40, 20);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.TOUCHES);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("TO:Point 5 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 1);
	assertTrue(results.contains("point 4"));
        
        /*
         * case 6: a line
         * 
         */ 
        Line2D line = new Line2D.Double(7, 30, 7, 0);
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.TOUCHES);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("TO:Line 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 2);
        assertTrue(results.contains("line 1"));
        assertTrue(results.contains("line 1 projected"));
        
        /*
         * case 7: another line
         * 
         */ 
        line = new Line2D.Double(-15, 3, 30, 4);
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.TOUCHES);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("TO:Line 2 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 2);
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("box 4"));
        
         /*
         * case 8: another line
         * 
         */ 
        line = new Line2D.Double(0, 0, -40, -40);
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.TOUCHES);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("TO:Line 2 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 5);
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("box 1"));
        assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        
        /*
         * case 9: a BBOX
         * 
         */ 
        double min1[] = {-15,   0};
        double max1[] = { 30,  50};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        bbox.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.TOUCHES);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("TO:BBox 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 7);
        assertTrue(results.contains("point 2"));
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("box 3"  ));
        assertTrue(results.contains("box 4"  ));
        assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
    }
    
    /**
     * Test the combinated spatial filter.
     */
    @Test
    public void withinTest() throws Exception {
        
        /*
         * case 1: BBOX  
         */ 
        double min1[] = {-20, -20};
        double max1[] = { 20,  20};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        bbox.setCoordinateReferenceSystem(crs);
        SpatialQuery bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.WITHIN);

        //we perform a lucene query
        Hits hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        int nbResults = hits.length();
        logger.finer("WT:BBOX 1 CRS=4326: nb Results: " + nbResults);
        
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 7);
        assertTrue(results.contains("point 1"));
	assertTrue(results.contains("point 1 projected"));
	assertTrue(results.contains("point 2"));
	assertTrue(results.contains("point 3"));
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
	assertTrue(results.contains("line 2"));
        
        /*
         * case 2: another BBOX.
         */ 
        double min2[] = {  3,   5};
        double max2[] = { 55,  50};
        bbox = new GeneralEnvelope(min2, max2);
        bbox.setCoordinateReferenceSystem(crs);
        bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.WITHIN);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("WT:BBOX 2 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 3);
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
	assertTrue(results.contains("point 4"));

        /*
         * case 6: a line
         * 
         */ 
        Line2D line = new Line2D.Double(-40, 30, 40, 20);
        SpatialQuery spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.WITHIN);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("WT:Line 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 2);
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
    }
    
    /**
     * Test the combinated spatial filter.
     */
    @Test
    public void crossesTest() throws Exception {
    
        /*
         * case 1: a line
         * 
         */ 
        Line2D line = new Line2D.Double(40, 10, 40, 30);
        SpatialQuery spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.CROSSES);

        //we perform a lucene query
        Hits hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        int nbResults = hits.length();
        logger.finer("CR:Line 1 CRS=4326: nb Results: " + nbResults);
        
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 2);
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("point 4"));
        
        /*
         * case 2: another line
         * 
         */ 
        line = new Line2D.Double(40, 10, -5, -5);
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.CROSSES);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("CR:Line 2 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 4);
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("line 1"));
        assertTrue(results.contains("line 1 projected"));
        assertTrue(results.contains("line 2"));
        
        /*
         * case 3: another line
         * 
         */ 
        line = new Line2D.Double(-25, 5, -35, -45);
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.CROSSES);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("CR:Line 2 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 2);
        assertTrue(results.contains("box 4"));
        assertTrue(results.contains("box 1"));
        
        /*
         * case 4: point (equals to point 3)
         * 
         */ 
        GeneralDirectPosition point = new GeneralDirectPosition(0, 0);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.CROSSES);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("CR:Point 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 4);
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        
        /*
         * case 5: another point
         * 
         */ 
        point = new GeneralDirectPosition(5, 13);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.CROSSES);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("CR:Point 2 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 2);
        assertTrue(results.contains("box 2"));
        assertTrue(results.contains("box 2 projected"));
        
        /*
         * case 6: a BBOX
         * 
         */ 
        double min1[] = {-10, -20};
        double max1[] = { 20,   5};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        bbox.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.CROSSES);
        
        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("CR:BBOX 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 3);
        assertTrue(results.contains("line 1"));
        assertTrue(results.contains("line 1 projected"));
        assertTrue(results.contains("point 2"));
    }
    
    /**
     * Test the combinated spatial filter.
     */
    @Test
    public void mulitpleFilterTest() throws Exception {
        
         /*
         * case 1: a BBOX TOUCHES filter OR a BBOX filter
         * 
         */ 
        double min1[] = { 25, -10};
        double max1[] = { 60,  50};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        bbox.setCoordinateReferenceSystem(crs);
        SpatialQuery spatialQuery1 = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.TOUCHES);
        SpatialQuery spatialQuery2 = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.BBOX);
        
        Filter filters[]  = {spatialQuery1.getSpatialFilter(), spatialQuery2.getSpatialFilter()};
        int filterType[]  = {SerialChainFilter.OR};
        SerialChainFilter serialFilter = new SerialChainFilter(filters, filterType); 
        
        
        
        //we perform a lucene query
        Hits hits = searcher.search(simpleQuery, serialFilter);

        int nbResults = hits.length();
        logger.finer("TO || BBOX: BBox 1 CRS=4326: nb Results: " + nbResults);
        
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 3);
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("box 3"  ));
        assertTrue(results.contains("line 1"));
	
        // TODO add precision
        //assertTrue(results.contains("line 1 projected"));
        
        /*
         * case 2: same test with AND instead of OR
         * 
         */ 
        int filterType2[]  = {SerialChainFilter.AND};
        serialFilter = new SerialChainFilter(filters, filterType2); 
        
        //we perform a lucene query
        hits = searcher.search(simpleQuery, serialFilter);

        nbResults = hits.length();
        logger.finer("TO && BBOX: BBox 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 0);
        
        /*
         * case 3: NOT INTERSECT line1
         * 
         */ 
        Line2D line               = new Line2D.Double(7, 40, 6, -40);
        SpatialQuery spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.INTERSECT);
        Filter filters3[]         = {spatialQuery.getSpatialFilter()};
        int filterType3[]         = {SerialChainFilter.NOT};
        serialFilter              = new SerialChainFilter(filters3, filterType3); 
        
        //we perform a lucene query
        hits = searcher.search(simpleQuery, serialFilter);

        nbResults = hits.length();
        logger.finer("NOT INTER:Line 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 10);
        assertTrue(results.contains("point 1"));
	assertTrue(results.contains("point 1 projected"));
	assertTrue(results.contains("point 2"));
	assertTrue(results.contains("point 3"));
	assertTrue(results.contains("point 4"));
	assertTrue(results.contains("point 5"));
	assertTrue(results.contains("box 1"));
	assertTrue(results.contains("box 3"));
	assertTrue(results.contains("box 4"));
	assertTrue(results.contains("line 2"));
        
        
        /*
         * case 4: INTERSECT line AND BBOX 
         * 
         */
        double min2[]          = {-12, -17};
        double max2[]          = { 15,  50};
        GeneralEnvelope bbox2  = new GeneralEnvelope(min2, max2);
        bbox2.setCoordinateReferenceSystem(crs);
        SpatialQuery bboxQuery = new SpatialQuery(bbox2, "EPSG:4326", SpatialFilter.BBOX);
        Filter filters4[]         = {spatialQuery.getSpatialFilter(), bboxQuery.getSpatialFilter()};
        int filterType4[]         = {SerialChainFilter.AND};
        serialFilter              = new SerialChainFilter(filters4, filterType4); 
        
        //we perform a lucene query
        hits = searcher.search(simpleQuery, serialFilter);

        nbResults = hits.length();
        logger.finer("NOT INTER:Line 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 2);
	assertTrue(results.contains("box 2"));
        assertTrue(results.contains("box 2 projected"));
        
        /*
         * case 5: INTERSECT line AND NOT BBOX 
         * 
         */
        int filterType5[] = {SerialChainFilter.AND, SerialChainFilter.NOT};
        serialFilter      = new SerialChainFilter(filters4, filterType5); 
        
        //we perform a lucene query
        hits = searcher.search(simpleQuery, serialFilter);

        nbResults = hits.length();
        logger.finer("NOT INTER:Line 1 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 2);
	assertTrue(results.contains("line 1"));
        assertTrue(results.contains("line 1 projected"));

        
    }
    
    /**
     * Test the distance spatial filter DWithin.
     */
    @Test
    public void dWithinTest() throws Exception {
        
        /*
         * case 1: point distance 5Km
         * 
         */ 
        GeneralDirectPosition point = new GeneralDirectPosition(0, 0);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        point.setCoordinateReferenceSystem(crs);
        SpatialQuery spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.DWITHIN, 5, "kilometers");

        //we perform a lucene query
        Hits hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        int nbResults = hits.length();
        logger.finer("DW:Point 1 dist: 5km CRS=4326: nb Results: " + nbResults);
        
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 4);
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        
        /*
         * case 2: point distance 1500Km
         * 
         */ 
        point = new GeneralDirectPosition(0, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.DWITHIN, 1500, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DW:Point 1 dist: 1500km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 7);
        assertTrue(results.contains("point 2"));
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
        
        /*
         * case 3: point distance 1500000m (same request than 2 in meters)
         * 
         */ 
        point = new GeneralDirectPosition(0, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.DWITHIN, 1500000, "meters");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DW:Point 1 dist: 1500000m CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 7);
        assertTrue(results.contains("point 2"));
        assertTrue(results.contains("point 3"));
        assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
        
        /*
         * case 4: point distance 2000Km
         * 
         */ 
        point = new GeneralDirectPosition(0, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.DWITHIN, 2000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DW:Point 1 dist: 2000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 10);
        assertTrue(results.contains("point 1"));
	assertTrue(results.contains("point 1 projected"));
	assertTrue(results.contains("point 2"));
	assertTrue(results.contains("point 3"));
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
	assertTrue(results.contains("box 4"));
	assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        
        /*
         * case 5: point distance 4000Km
         * 
         */ 
        point = new GeneralDirectPosition(0, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.DWITHIN, 4000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DW:Point 1 dist: 4000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 11);
        assertTrue(results.contains("point 1"));
	assertTrue(results.contains("point 1 projected"));
	assertTrue(results.contains("point 2"));
	assertTrue(results.contains("point 3"));
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
	assertTrue(results.contains("box 4"));
	assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        assertTrue(results.contains("box 3"));
        
        /*
         * case 6: point distance 5000Km
         * 
         */ 
        point = new GeneralDirectPosition(0, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.DWITHIN, 5000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DW:Point 1 dist: 5000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 12);
        assertTrue(results.contains("point 1"));
	assertTrue(results.contains("point 1 projected"));
	assertTrue(results.contains("point 2"));
	assertTrue(results.contains("point 3"));
        assertTrue(results.contains("point 4"));
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
	assertTrue(results.contains("box 4"));
	assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        assertTrue(results.contains("box 3"));
        
        /*
         * case 6: point distance 6000Km
         * 
         */ 
        point = new GeneralDirectPosition(0, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.DWITHIN, 6000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DW:Point 1 dist: 6000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 14);
        assertTrue(results.contains("point 1"));
	assertTrue(results.contains("point 1 projected"));
	assertTrue(results.contains("point 2"));
	assertTrue(results.contains("point 3"));
        assertTrue(results.contains("point 4"));
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
	assertTrue(results.contains("box 4"));
	assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        
        /*
         * case 7: BBOX distance 5km  
         */ 
        double min1[] = {-20, -20};
        double max1[] = { 20,  20};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        bbox.setCoordinateReferenceSystem(crs);
        SpatialQuery bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.DWITHIN, 5, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DW:BBOX 1 dist: 5km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 10);
        assertTrue(results.contains("point 1"));
	assertTrue(results.contains("point 1 projected"));
	assertTrue(results.contains("point 2"));
	assertTrue(results.contains("point 3"));
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
        assertTrue(results.contains("box 4"));
        assertTrue(results.contains("line 1"));
        assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        
        
        /*
         * case 8: BBOX distance 1500km  
         */ 
        bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.DWITHIN, 1500, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DW:BBOX 1 dist: 1500km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 11);
        assertTrue(results.contains("point 1"));
	assertTrue(results.contains("point 1 projected"));
	assertTrue(results.contains("point 2"));
	assertTrue(results.contains("point 3"));
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("box 4"));
        assertTrue(results.contains("line 1"));
        assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        
        /*
         * case 9: BBOX distance 3000km  
         */ 
        bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.DWITHIN, 3000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DW:BBOX 1 dist: 3000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 14);
        assertTrue(results.contains("point 1"));
	assertTrue(results.contains("point 1 projected"));
	assertTrue(results.contains("point 2"));
	assertTrue(results.contains("point 3"));
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("box 4"));
        assertTrue(results.contains("line 1"));
        assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        
        /*
         * case 10: a line distance 5km
         * 
         */ 
        Line2D line = new Line2D.Double(-50, -45, 60, -43);
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.DWITHIN, 5, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DW:Line 1 dist: 5km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 1);
	assertTrue(results.contains("box 1"));
        
         /*
         * case 11: a line distance 4000km
         * 
         */ 
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.DWITHIN, 4000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DW:Line 1 dist: 4000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 3);
	assertTrue(results.contains("box 1"));
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("line 2"));
        
        /*
         * case 12: a line distance 5000km
         * 
         */ 
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.DWITHIN, 5000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DW:Line 1 dist: 5000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 8);
        assertTrue(results.contains("point 2"));
        assertTrue(results.contains("point 3"));
	assertTrue(results.contains("box 1"));
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("box 4"));
        assertTrue(results.contains("line 2"));
        assertTrue(results.contains("line 1"));
        assertTrue(results.contains("line 1 projected"));
        
        /*
         * case 12: a line distance 6000km
         * 
         */ 
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.DWITHIN, 6000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("DW:Line 1 dist: 6000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 13);
        assertTrue(results.contains("point 2"));
        assertTrue(results.contains("point 3"));
	assertTrue(results.contains("box 1"));
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("box 4"));
        assertTrue(results.contains("line 2"));
        assertTrue(results.contains("line 1"));
        assertTrue(results.contains("line 1 projected"));
        assertTrue(results.contains("point 1"));
	assertTrue(results.contains("point 1 projected"));
	assertTrue(results.contains("point 4"));
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));

    }
    
    /**
     * Test the Distance spatial filter BEYOND.
     */
    @Test
    public void beyondTest() throws Exception {
        
        /*
         * case 1: point distance 5Km
         * 
         */ 
        GeneralDirectPosition point = new GeneralDirectPosition(0, 0);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        point.setCoordinateReferenceSystem(crs);
        SpatialQuery spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.BEYOND, 5, "kilometers");

        //we perform a lucene query
        Hits hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        int nbResults = hits.length();
        logger.finer("BY:Point 1 CRS=4326: nb Results: " + nbResults);
        
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 10);
        assertTrue(results.contains("point 1"));
        assertTrue(results.contains("point 1 projected"));
        assertTrue(results.contains("point 2"));
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        assertTrue(results.contains("box 2"));
        assertTrue(results.contains("box 2 projected"));
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("box 4"));
        
        /*
         * case 2: point distance 1500Km
         * 
         */ 
        point = new GeneralDirectPosition(0, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.BEYOND, 1500, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BY:Point 1 dist: 1500km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 7);
        assertTrue(results.contains("point 1"));
        assertTrue(results.contains("point 1 projected"));
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("box 4"));
        
        /*
         * case 3: point distance 1500000m
         * 
         */ 
        point = new GeneralDirectPosition(0, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.BEYOND, 1500000, "meters");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BY:Point 1 dist: 1500000m CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 7);
        assertTrue(results.contains("point 1"));
        assertTrue(results.contains("point 1 projected"));
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        assertTrue(results.contains("box 3"));
        assertTrue(results.contains("box 4"));
        
        
        /*
         * case 4: point distance 2000Km
         * 
         */ 
        point = new GeneralDirectPosition(0, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.BEYOND, 2000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BY:Point 1 dist: 2000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 4);
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        assertTrue(results.contains("box 3"));
        
        /*
         * case 5: point distance 4000Km
         * 
         */ 
        point = new GeneralDirectPosition(0, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.BEYOND, 4000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BY:Point 1 dist: 4000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 3);
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        
        /*
         * case 6: point distance 5000Km
         * 
         */ 
        point = new GeneralDirectPosition(0, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.BEYOND, 5000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BY:Point 1 dist: 5000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 2);
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        
        /*
         * case 7: point distance 6000Km
         * 
         */ 
        point = new GeneralDirectPosition(0, 0);
        point.setCoordinateReferenceSystem(crs);
        spatialQuery = new SpatialQuery(point, "EPSG:4326", SpatialFilter.BEYOND, 6000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BY:Point 1 dist: 6000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 0);
        
        /*
         * case 8: BBOX distance 5km  
         */ 
        double min1[] = {-20, -20};
        double max1[] = { 20,  20};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        bbox.setCoordinateReferenceSystem(crs);
        SpatialQuery bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.BEYOND, 5, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BY:BBOX 1 dist: 5km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 4);
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        assertTrue(results.contains("box 3"));
        
        /*
         * case 8: BBOX distance 1500km  
         */ 
        bbox.setCoordinateReferenceSystem(crs);
        bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.BEYOND, 1500, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BY:BBOX 1 dist: 1500km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 3);
        assertTrue(results.contains("point 4"));
        assertTrue(results.contains("point 5"));
        assertTrue(results.contains("box 1"));
        
        /*
         * case 9: BBOX distance 3000km  
         */ 
        bbox.setCoordinateReferenceSystem(crs);
        bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.BEYOND, 3000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BY:BBOX 1 dist: 3000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 0);
        
         /*
         * case 10: a line distance 5km
         * 
         */ 
        Line2D line = new Line2D.Double(-50, -45, 60, -43);
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.BEYOND, 5, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BY:Line 1 dist: 5km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 13);
        assertTrue(results.contains("point 1"));
	assertTrue(results.contains("point 1 projected"));
	assertTrue(results.contains("point 2"));
	assertTrue(results.contains("point 3"));
	assertTrue(results.contains("point 4"));
	assertTrue(results.contains("point 5"));
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
	assertTrue(results.contains("box 3"));
	assertTrue(results.contains("box 4"));
	assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
	assertTrue(results.contains("line 2"));
        
        /*
         * case 11: a line distance 4000km
         * 
         */ 
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.BEYOND, 4000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BY:Line 1 dist: 4000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 11);
        assertTrue(results.contains("point 1"));
	assertTrue(results.contains("point 1 projected"));
	assertTrue(results.contains("point 2"));
	assertTrue(results.contains("point 3"));
	assertTrue(results.contains("point 4"));
	assertTrue(results.contains("point 5"));
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
        //issue: this box as tha same y value than box 3
        assertTrue(results.contains("box 4"));
	assertTrue(results.contains("line 1"));
	assertTrue(results.contains("line 1 projected"));
        
        /*
         * case 12: a line distance 5000km
         * 
         */ 
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.BEYOND, 5000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BY:Line 1 dist: 5000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 6);
        assertTrue(results.contains("point 1"));
	assertTrue(results.contains("point 1 projected"));
	assertTrue(results.contains("point 4"));
	assertTrue(results.contains("point 5"));
	assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
        
        /*
         * case 13: a line distance 6000km
         * 
         */ 
        spatialQuery = new SpatialQuery(line, "EPSG:4326", SpatialFilter.BEYOND, 6000, "kilometers");

        //we perform a lucene query
        hits = searcher.search(simpleQuery, spatialQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("BY:Line 1 dist: 6000km CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 1);
	assertTrue(results.contains("point 5"));
        
        
    }
    
    /**
     * Test the combinated spatial filter.
     */
    @Test
    public void overlapsTest() throws Exception {
        /*
         * case 1: bbox.
         */ 
        double min1[] = {-20, -20};
        double max1[] = { 20,  20};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        bbox.setCoordinateReferenceSystem(crs);
        SpatialQuery bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.OVERLAPS);

        //we perform a lucene query
        Hits hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        int nbResults = hits.length();
        logger.finer("OL:BBOX 1 CRS=4326: nb Results: " + nbResults);
        
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 1);
        assertTrue(results.contains("box 4"));
        
        /*
         * case 2: another bbox.
         */ 
        double min2[] = {-20, -20};
        double max2[] = {  7,  20};
        bbox = new GeneralEnvelope(min2, max2);
        bbox.setCoordinateReferenceSystem(crs);
        bboxQuery = new SpatialQuery(bbox, "EPSG:4326", SpatialFilter.OVERLAPS);

        //we perform a lucene query
        hits = searcher.search(simpleQuery, bboxQuery.getSpatialFilter());

        nbResults = hits.length();
        logger.finer("OL:BBOX 2 CRS=4326: nb Results: " + nbResults);
        
        results = new ArrayList<String>();
        for (int i = 0; i < nbResults; i++) {
            String name = hits.doc(i).get("name");
            results.add(name);
            logger.finer('\t' + "Name: " +  name);
        }
        
        //we verify that we obtain the correct results.
        assertEquals(nbResults, 3);
        assertTrue(results.contains("box 4"));
        assertTrue(results.contains("box 2"));
	assertTrue(results.contains("box 2 projected"));
        
    }
    
    private void fillTestData(IndexWriter writer) throws Exception {
        Document doc = new Document();
        doc.add(new Field("name", "point 1", Field.Store.YES, Field.Index.TOKENIZED));
        addPoint      (doc,           -10,                10, "EPSG:4326");
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "point 1 projected", Field.Store.YES, Field.Index.TOKENIZED));
        addPoint      (doc,           -1111475.102852225,   1113194.9079327357, "EPSG:3395");
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "point 2", Field.Store.YES, Field.Index.TOKENIZED));
        addPoint      (doc,           -10,                 0, "EPSG:4326");
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "point 3", Field.Store.YES, Field.Index.TOKENIZED));
        addPoint      (doc,             0,                 0, "EPSG:4326");
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "point 4", Field.Store.YES, Field.Index.TOKENIZED));
        addPoint      (doc,            40,                20, "EPSG:4326");
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "point 5", Field.Store.YES, Field.Index.TOKENIZED));
        addPoint      (doc,           -40,                30, "EPSG:4326");
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "box 1", Field.Store.YES, Field.Index.TOKENIZED));
        addBoundingBox(doc,           -40,                -25,           -50,               -40, "EPSG:4326");
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "box 2", Field.Store.YES, Field.Index.TOKENIZED));
        addBoundingBox(doc,             5,                 10,            10,                15, "EPSG:4326");
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "box 2 projected", Field.Store.YES, Field.Index.TOKENIZED));
        addBoundingBox(doc,             556597.4539663679,  1113194.9079327357,  1111475.1028522244, 1678147.5163917788, "EPSG:3395");
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "box 3", Field.Store.YES, Field.Index.TOKENIZED));
        addBoundingBox(doc,            30,                 50,             0,                15, "EPSG:4326");
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "box 4", Field.Store.YES, Field.Index.TOKENIZED));
        addBoundingBox(doc,           -30,                -15 ,             0,                10, "EPSG:4326");
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "line 1", Field.Store.YES, Field.Index.TOKENIZED));
        addLine       (doc,             0,                  0,            25,                 0, "EPSG:4326");
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "line 1 projected", Field.Store.YES, Field.Index.TOKENIZED));
        addLine       (doc,             0,        0,      2857692.6111605316,                 0, "EPSG:3395");
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "line 2", Field.Store.YES, Field.Index.TOKENIZED));
        addLine       (doc,             0,                  0,             0,               -15, "EPSG:4326");
        writer.addDocument(doc);
        writer.flush();
    }

    /**
     *  Add a point geometry to the specified Document.
     * 
     * @param doc     The document to add the geometry
     * @param x       The x coordinate of the point.
     * @param y       The y coordinate of the point.
     * @param crsName The coordinate reference system in witch the coordinates are expressed.
     */
    private void addPoint(Document doc, double y, double x, String crsName) throws IOException, NoSuchAuthorityCodeException, FactoryException, TransformException {

        // convert the lat / long to lucene fields
        doc.add(new Field("geometry" , "point", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("x"        , x + "" , Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("y"        , y + "" , Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("CRS"      , crsName, Field.Store.YES, Field.Index.UN_TOKENIZED));
        
        // add a default meta field to make searching all documents easy 
        doc.add(new Field("metafile", "doc",    Field.Store.YES, Field.Index.TOKENIZED));
    }
    
    /**
     * Add a boundingBox geometry to the specified Document.
     * 
     * @param doc  The document to add the geometry
     * @param minx the minimun X coordinate of the bounding box.
     * @param maxx the maximum X coordinate of the bounding box.
     * @param miny the minimun Y coordinate of the bounding box.
     * @param maxy the maximum Y coordinate of the bounding box.
     * @param crsName The coordinate reference system in witch the coordinates are expressed.
     */
    private void addBoundingBox(Document doc, double minx, double maxx, double miny, double maxy, String crsName) throws NoSuchAuthorityCodeException, FactoryException, TransformException {

        // convert the corner of the box to lucene fields
        doc.add(new Field("geometry" , "boundingbox", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("minx"     , minx + "",     Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("maxx"     , maxx + "",     Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("miny"     , miny + "",     Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("maxy"     , maxy + "",     Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("CRS"      , crsName  ,     Field.Store.YES, Field.Index.UN_TOKENIZED));
        

        // add a default meta field to make searching all documents easy 
        doc.add(new Field("metafile", "doc",          Field.Store.YES, Field.Index.TOKENIZED));
    }
    
    /**
     * Add a Line geometry to the specified Document.
     * 
     * @param doc The document to add the geometry
     * @param x1  the X coordinate of the first point of the line.
     * @param y1  the Y coordinate of the first point of the line.
     * @param x2  the X coordinate of the second point of the line.
     * @param y2  the Y coordinate of the first point of the line.
     * @param crsName The coordinate reference system in witch the coordinates are expressed.
     */
    private void addLine(Document doc, double x1, double y1, double x2, double y2, String crsName) throws IOException, NoSuchAuthorityCodeException, FactoryException, TransformException {

        
        // convert the corner of the box to lucene fields
        doc.add(new Field("geometry" , "line" , Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("x1"       , x1 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("y1"       , y1 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("x2"       , x2 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("y2"       , y2 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("CRS"      , crsName, Field.Store.YES, Field.Index.UN_TOKENIZED));
        
        // add a default meta field to make searching all documents easy 
        doc.add(new Field("metafile", "doc",   Field.Store.YES, Field.Index.TOKENIZED));

    }

}
