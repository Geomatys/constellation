/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.metadata.index.generic;

// J2SE dependencies
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


// Constellation dependencies
import org.constellation.util.Util;

// lucene dependencies
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

// geotools dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.index.mdweb.MDWebIndexSearcher;
import org.constellation.metadata.index.mdweb.MDWebIndexer;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.internal.sql.ScriptRunner;
import org.geotoolkit.lucene.filter.LuceneOGCFilter;
import org.geotoolkit.lucene.filter.SerialChainFilter;
import org.geotoolkit.lucene.filter.SpatialQuery;
import org.geotoolkit.lucene.index.AbstractIndexSearcher;
import org.geotoolkit.lucene.index.AbstractIndexer;

// GeoAPI dependencies
import org.geotoolkit.resources.NIOUtilities;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

//Junit dependencies
import org.junit.*;
import org.opengis.filter.FilterFactory2;
import static org.junit.Assert.*;

/**
 * Test class for constellation lucene index
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MdwebIndexTest {

    protected static final FilterFactory2 FF = (FilterFactory2)
            FactoryFinder.getFilterFactory(new Hints(Hints.FILTER_FACTORY,FilterFactory2.class));


    private static DefaultDataSource ds;
    
    private static final Logger logger = Logger.getLogger("org.constellation.metadata");

    private static AbstractIndexSearcher indexSearcher;

    private static AbstractIndexer indexer;

    @BeforeClass
    public static void setUpClass() throws Exception {
        File configDirectory      = new File("MDwebIndexTest");
        NIOUtilities.deleteDirectory(configDirectory);

        final String url = "jdbc:derby:memory:MDITest;create=true";
        ds = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        ScriptRunner sr = new ScriptRunner(con);
        sr.run(Util.getResourceAsStream("org/constellation/sql/structure-mdweb.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/mdweb-base-data.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/ISO19115-base-data.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/ISO19115-data.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/ISO19119-data.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/mdweb-user-data.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/DC-schema.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/ebrim-schema.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/metadata/sql/csw-data.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/metadata/sql/csw-data-2.sql"));

        //we write the configuration file
        BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
        Automatic configuration = new Automatic("mdweb", bdd);
        configuration.setConfigurationDirectory(configDirectory);
        indexer                 = new MDWebIndexer(configuration, "");
        indexSearcher           = new MDWebIndexSearcher(configDirectory, "");
        indexer.setLogLevel(Level.FINER);
        indexSearcher.setLogLevel(Level.FINER);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (ds != null) {
            ds.shutdown();
        }
        NIOUtilities.deleteDirectory(new File("MDwebIndexTest"));
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test simple lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void simpleSearchTest() throws Exception {
        Filter nullFilter   = null;
        String resultReport = "";

        /**
         * Test 1 simple search: title = 90008411.ctd
         */
        SpatialQuery spatialQuery = new SpatialQuery("Title:90008411.ctd", nullFilter, SerialChainFilter.AND);
        List<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("SimpleSearch 1:\n" + resultReport);

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("2:CSWCat");
        //expectedResult.add("3:CSWCat");

        assertEquals(expectedResult, result);

         /**
         * Test 2 simple search: indentifier != 40510_145_19930221211500
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc NOT identifier:\"40510_145_19930221211500\"", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("SimpleSearch 2:\n" + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("5:CSWCat");
        expectedResult.add("4:CSWCat");
        expectedResult.add("3:CSWCat");
        expectedResult.add("2:CSWCat");
        expectedResult.add("8:CSWCat");
        expectedResult.add("7:CSWCat");


        assertEquals(expectedResult, result);

        /**
         * Test 3 simple search: originator = UNIVERSITE DE LA MEDITERRANNEE (U2) / COM - LAB. OCEANOG. BIOGEOCHIMIE - LUMINY
         */
        spatialQuery = new SpatialQuery("abstract:\"Donnees CTD NEDIPROD VI 120\"", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("simpleSearch 3:\n" + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("2:CSWCat");

        assertEquals(expectedResult, result);
    }

     /**
     * Test simple lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void wildCharSearchTest() throws Exception {
        Filter nullFilter   = null;
        String resultReport = "";

        /**
         * Test 1 simple search: title = title1
         */
        SpatialQuery spatialQuery = new SpatialQuery("Title:90008411*", nullFilter, SerialChainFilter.AND);
        List<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("wildCharSearch 1:\n" + resultReport);

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("3:CSWCat");
        expectedResult.add("2:CSWCat");

        assertEquals(expectedResult, result);

        /**
         * Test 2 wildChar search: originator LIKE *UNIVER....
         */
        spatialQuery = new SpatialQuery("abstract:*NEDIPROD*", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("wildCharSearch 2:\n" + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("2:CSWCat");


        assertEquals(expectedResult, result);

        /**
         * Test 3 wildChar search: title like *.ctd
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("Title:*.ctd", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("wilCharSearch 3:\n" + resultReport);

        assertTrue(result.contains("4:CSWCat"));
        assertTrue(result.contains("6:CSWCat"));
        assertTrue(result.contains("2:CSWCat"));
        assertTrue(result.contains("3:CSWCat"));

         /**
         * Test 4 wildCharSearch: anstract LIKE *onnees CTD NEDIPROD VI 120
         */
        spatialQuery = new SpatialQuery("abstract:(*onnees CTD NEDIPROD VI 120)", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("wildCharSearch 4:\n" + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("2:CSWCat");

        //issues here it found
        assertEquals(expectedResult, result);

    }

     /**
     * Test simple lucene date search.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void dateSearchTest() throws Exception {
        Filter nullFilter   = null;
        String resultReport = "";

        /**
         * Test 1 date search: date after 25/01/2009
         */
        SpatialQuery spatialQuery = new SpatialQuery("date:{20090125 30000101}", nullFilter, SerialChainFilter.AND);
        List<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("DateSearch 1:\n" + resultReport);

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("5:CSWCat");
        expectedResult.add("4:CSWCat");
        expectedResult.add("3:CSWCat");
        expectedResult.add("6:CSWCat");
        expectedResult.add("8:CSWCat");

        assertEquals(expectedResult, result);

        /**
         * Test 2 date search: TempExtent_begin before 01/01/1985
         */
        spatialQuery = new SpatialQuery("TempExtent_begin:{00000101 19850101}", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("DateSearch 2:\n" + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("5:CSWCat");
        expectedResult.add("4:CSWCat");
        expectedResult.add("8:CSWCat");

        assertEquals(expectedResult, result);

        /**
         * Test 3 date search: TempExtent_end after 01/01/1991
         */
        spatialQuery = new SpatialQuery("TempExtent_end:{19910101 30000101}", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("DateSearch 3:\n" + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("6:CSWCat");
        expectedResult.add("8:CSWCat");

        assertEquals(expectedResult, result);


    }

    /**
     * Test sorted lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void sortedSearchTest() throws Exception {

        Filter nullFilter   = null;
        String resultReport = "";

        /**
         * Test 1 sorted search: all orderBy identifier ASC
         */
        SpatialQuery spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        SortField sf = new SortField("identifier_sort", SortField.STRING, false);
        spatialQuery.setSort(new Sort(sf));

        List<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("SortedSearch 1:\n" + resultReport);

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("5:CSWCat");
        expectedResult.add("4:CSWCat");
        expectedResult.add("6:CSWCat");
        expectedResult.add("2:CSWCat");
        expectedResult.add("3:CSWCat");
        expectedResult.add("8:CSWCat");
        expectedResult.add("7:CSWCat");
        assertEquals(expectedResult, result);

        /**
         * Test 2 sorted search: all orderBy identifier DSC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        sf = new SortField("identifier_sort", SortField.STRING, true);
        spatialQuery.setSort(new Sort(sf));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("SortedSearch 2:\n" + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("7:CSWCat");
        expectedResult.add("8:CSWCat");
        expectedResult.add("3:CSWCat");
        expectedResult.add("2:CSWCat");
        expectedResult.add("6:CSWCat");
        expectedResult.add("4:CSWCat");
        expectedResult.add("5:CSWCat");

        assertEquals(expectedResult, result);

        /**
         * Test 3 sorted search: all orderBy Abstract ASC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        sf = new SortField("Abstract_sort", SortField.STRING, false);
        spatialQuery.setSort(new Sort(sf));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("SortedSearch 3:\n" + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("7:CSWCat");
        expectedResult.add("8:CSWCat");
        expectedResult.add("5:CSWCat");
        expectedResult.add("4:CSWCat");
        expectedResult.add("6:CSWCat");
        expectedResult.add("3:CSWCat");
        expectedResult.add("2:CSWCat");

        assertEquals(expectedResult, result);

        /**
         * Test 4 sorted search: all orderBy Abstract DSC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        sf = new SortField("Abstract_sort", SortField.STRING, true);
        spatialQuery.setSort(new Sort(sf));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("SortedSearch 4:\n" + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("2:CSWCat");
        expectedResult.add("3:CSWCat");
        expectedResult.add("6:CSWCat");
        expectedResult.add("4:CSWCat");
        expectedResult.add("5:CSWCat");
        expectedResult.add("8:CSWCat");
        expectedResult.add("7:CSWCat");

        assertEquals(expectedResult, result);
    }

    /**
     *
     * Test spatial lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void spatialSearchTest() throws Exception {

        String resultReport = "";

        /**
         * Test 1 spatial search: BBOX filter
         */
        double min1[] = {-20, -20};
        double max1[] = { 20,  20};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        bbox.setCoordinateReferenceSystem(crs);
        LuceneOGCFilter sf = LuceneOGCFilter.wrap(FF.bbox(LuceneOGCFilter.GEOMETRY_PROPERTY, -20, -20, 20, 20, "EPSG:4326"));
        SpatialQuery spatialQuery = new SpatialQuery("metafile:doc", sf, SerialChainFilter.AND);

        List<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("spatialSearch 1:\n" + resultReport);

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("5:CSWCat");
        expectedResult.add("4:CSWCat");
        expectedResult.add("8:CSWCat");

        assertEquals(expectedResult, result);

        /**
         * Test 1 spatial search: NOT BBOX filter
         */
        resultReport = "";
        List<Filter> lf = new ArrayList<Filter>();
        //sf           = new BBOXFilter(bbox, "urn:x-ogc:def:crs:EPSG:6.11:4326");
        sf           = LuceneOGCFilter.wrap(FF.bbox(LuceneOGCFilter.GEOMETRY_PROPERTY, -20, -20, 20, 20, "EPSG:4326"));

        lf.add(sf);
        int[] op = {SerialChainFilter.NOT};
        SerialChainFilter f = new SerialChainFilter(lf, op);
        spatialQuery = new SpatialQuery("metafile:doc", f, SerialChainFilter.AND);

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.finer("spatialSearch 2:\n" + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("3:CSWCat");
        expectedResult.add("2:CSWCat");
        expectedResult.add("6:CSWCat");
        expectedResult.add("7:CSWCat");

        assertEquals("CRS URN are not working", expectedResult, result);
    }

    /**
     *
     * Test spatial lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void TermQueryTest() throws Exception {

        /**
         * Test 1
         */

        String identifier = "39727_22_19750113062500";
        String result = indexSearcher.identifierQuery(identifier);

        logger.finer("identifier query 1:\n" + result);

        String expectedResult = "4:CSWCat";

        assertEquals(expectedResult, result);

        /**
         * Test 2
         */

        identifier = "CTDF02";
        result = indexSearcher.identifierQuery(identifier);

        logger.finer("identifier query 2:\n" + result);

        expectedResult = "8:CSWCat";

        assertEquals(expectedResult, result);

        /**
         * Test 3
         */

        identifier = "urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd";
        result = indexSearcher.identifierQuery(identifier);

        logger.finer("identifier query 3:\n" + result);

        expectedResult = "7:CSWCat";

        assertEquals(expectedResult, result);
    }

    /**
     *
     * Test spatial lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void DeleteDocumentTest() throws Exception {
        indexer.removeDocument("8");

        indexSearcher.refresh();

        /**
         * Test 1
         */

        String identifier = "39727_22_19750113062500";
        String result = indexSearcher.identifierQuery(identifier);

        logger.finer("identifier query 1:\n" + result);

        String expectedResult = "4:CSWCat";

        assertEquals(expectedResult, result);

        /**
         * Test 2
         */

        identifier = "CTDF02";
        result = indexSearcher.identifierQuery(identifier);

        logger.finer("identifier query 2:\n" + result);

        expectedResult = null;

        assertEquals(expectedResult, result);
    }

}

