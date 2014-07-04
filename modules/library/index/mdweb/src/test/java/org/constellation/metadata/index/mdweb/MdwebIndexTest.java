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
package org.constellation.metadata.index.mdweb;

// J2SE dependencies

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.util.logging.Logging;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.TestRunner;
import org.constellation.util.Util;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.lucene.filter.LuceneOGCFilter;
import org.geotoolkit.lucene.filter.SerialChainFilter;
import org.geotoolkit.lucene.filter.SpatialQuery;
import org.geotoolkit.lucene.index.LuceneIndexSearcher;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.junit.runner.RunWith;
import org.mdweb.io.MD_IOFactory;
import org.mdweb.io.Reader;
import org.mdweb.model.storage.FullRecord;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.imageio.spi.ServiceRegistry;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

// MDWeb dependencies
// Constellation dependencies
// lucene dependencies
// geotoolkit dependencies
// GeoAPI dependencies
//Junit dependencies

/**
 * Test class for constellation lucene index
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
public class MdwebIndexTest {

    protected static final FilterFactory2 FF = (FilterFactory2)
            FactoryFinder.getFilterFactory(new Hints(Hints.FILTER_FACTORY,FilterFactory2.class));


    private static DefaultDataSource ds;

    private static final Logger LOGGER = Logging.getLogger("org.constellation.metadata");

    private static LuceneIndexSearcher indexSearcher;

    private static MDWebIndexer indexer;

    @BeforeClass
    public static void setUpClass() throws Exception {
        File configDirectory      = new File("MDwebIndexTest");
        FileUtilities.deleteDirectory(configDirectory);

        final String url = "jdbc:derby:memory:MDITest;create=true";
        ds = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19119.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19108.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115-2.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19110.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/data/defaultRecordSets.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/users/creation_user.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/catalog_web_service.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ebrimv2.5.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ebrimv3.0.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-2.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-6.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/csw-data-6.5.sql"));
        sr.close(false);
        con.close();

        //we write the configuration file
        BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
        Automatic configuration = new Automatic("mdweb", bdd);
        configuration.setConfigurationDirectory(configDirectory);
        indexer                 = new MDWebIndexer(configuration, "", true);
        indexSearcher           = new LuceneIndexSearcher(configDirectory, "", null, true);
        indexer.setLogLevel(Level.FINER);
        indexSearcher.setLogLevel(Level.FINER);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (ds != null) {
            ds.shutdown();
        }
        FileUtilities.deleteDirectory(new File("MDwebIndexTest"));
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
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
    @Order(order = 1)
    public void simpleSearchTest() throws Exception {
        Filter nullFilter   = null;
        String resultReport = "";

        /**
         * Test 1 simple search: title = 90008411.ctd
         */
        SpatialQuery spatialQuery = new SpatialQuery("Title:\"90008411.ctd\"", nullFilter, SerialChainFilter.AND);
        Set<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "SimpleSearch 1:\n{0}", resultReport);

        Set<String> expectedResult = new LinkedHashSet<>();
        expectedResult.add("42292_5p_19900609195600");
        //expectedResult.add("42292_9s_19900610041000");

        assertEquals(expectedResult, result);

         /**
         * Test 2 simple search: identifier != 40510_145_19930221211500
         */
        spatialQuery = new SpatialQuery("metafile:doc NOT identifier:\"40510_145_19930221211500\"", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "SimpleSearch 2:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("CTDF02");
        expectedResult.add("cat-1");
        expectedResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        expectedResult.add("484fc4d9-8d11-48a5-a386-65c19398f7c3");
        expectedResult.add("484fc4d9-8d11-48a5-a386-65c19398f7k7");
        expectedResult.add("28644bf0-5d9d-4ebd-bef0-f2b0b2067b26");
        expectedResult.add("937491cd-4bc4-43e4-9509-f6cc606f906e");
        expectedResult.add("666-999-666");
        expectedResult.add("999-666-999");
        expectedResult.add("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");
        expectedResult.add("meta_NaN_id");

        assertEquals(expectedResult, result);

        /**
         * Test 3 simple search: originator = UNIVERSITE DE LA MEDITERRANNEE (U2) / COM - LAB. OCEANOG. BIOGEOCHIMIE - LUMINY
         */
        spatialQuery = new SpatialQuery("abstract:\"Donnees CTD NEDIPROD VI 120\"", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "simpleSearch 3:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("42292_5p_19900609195600");

        assertEquals(expectedResult, result);

        /**
         * Test 4 simple search: Title = 92005711.ctd
         */
        spatialQuery = new SpatialQuery("Title:\"92005711.ctd\"", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "SimpleSearch 4:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("40510_145_19930221211500");


        assertEquals(expectedResult, result);

        /**
         * Test 5 simple search: creator = IFREMER / IDM/SISMER
         */
        spatialQuery = new SpatialQuery("creator:\"IFREMER / IDM/SISMER\"", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "SimpleSearch 5:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("40510_145_19930221211500");


        assertEquals(expectedResult, result);

        /**
         * Test 6 simple search: identifier = 40510_145_19930221211500
         */
        spatialQuery = new SpatialQuery("identifier:\"40510_145_19930221211500\"", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "SimpleSearch 2:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("40510_145_19930221211500");

        assertEquals(expectedResult, result);
    }

    /**
     * Test simple lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order = 2)
    public void numericComparisonSearchTest() throws Exception {
        Filter nullFilter   = null;
        String resultReport = "";

        /**
         * Test 1 numeric search: CloudCover < 60
         */
        SpatialQuery spatialQuery = new SpatialQuery("CloudCover:{-2147483648 TO 60}", nullFilter, SerialChainFilter.AND);
        Set<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "numericComparisonSearch 1:\n{0}", resultReport);

        Set<String> expectedResult = new LinkedHashSet<>();
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("42292_5p_19900609195600");

        assertEquals(expectedResult, result);

        /**
         * Test 2 numeric search: CloudCover <= 25
         */
        spatialQuery = new SpatialQuery("CloudCover:[-2147483648 TO 25]", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "numericComparisonSearch 2:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("42292_9s_19900610041000");


        assertEquals(expectedResult, result);

        /**
         * Test 3 numeric search: CloudCover => 25
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("CloudCover:[25 TO 2147483648]", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "numericComparisonSearch 3:\n{0}", resultReport);

        assertEquals(2, result.size());
        assertTrue(result.contains("42292_5p_19900609195600"));
        assertTrue(result.contains("39727_22_19750113062500"));

        /**
         * Test 4 numeric search: CloudCover => 60
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("CloudCover:[210 TO 2147483648]", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "numericComparisonSearch 4:\n{0}", resultReport);

        assertEquals(0, result.size());

         /**
         * Test 5 numeric search: CloudCover => 50
         */
        spatialQuery = new SpatialQuery("CloudCover:[50 TO 2147483648]", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "numericComparisonSearch 5:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("42292_5p_19900609195600");

        //issues here it found
        assertEquals(expectedResult, result);

        /**
         * Test 6 numeric search: CloudCover = 50.0
         */
        spatialQuery = new SpatialQuery("CloudCover:[50 TO 50]", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "numericComparisonSearch 6:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("42292_5p_19900609195600");

        //issues here it found
        assertEquals(expectedResult, result);

        /**
         * Test 7 numeric search: CloudCover = 50.0
         */
        spatialQuery = new SpatialQuery("CloudCover:50.0", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "numericComparisonSearch 7:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        //expectedResult.add("42292_5p_19900609195600");

        //issues here it didn't find
        assertEquals(expectedResult, result);

    }

     /**
     * Test wildChar lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order = 3)
    public void wildCharSearchTest() throws Exception {
        Filter nullFilter   = null;
        String resultReport = "";

        /**
         * Test 1 simple search: title = title1
         */
        SpatialQuery spatialQuery = new SpatialQuery("Title:90008411*", nullFilter, SerialChainFilter.AND);
        Set<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "wildCharSearch 1:\n{0}", resultReport);

        Set<String> expectedResult = new LinkedHashSet<>();
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("42292_5p_19900609195600");


        assertEquals(expectedResult, result);

        /**
         * Test 2 wildChar search: originator LIKE *UNIVER....
         */
        spatialQuery = new SpatialQuery("abstract:*NEDIPROD*", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "wildCharSearch 2:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("42292_5p_19900609195600");


        assertEquals(expectedResult, result);

        /**
         * Test 3 wildChar search: Title like *.ctd
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("Title:*.ctd", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "wilCharSearch 3:\n{0}", resultReport);

        assertTrue(result.contains("42292_5p_19900609195600"));
        assertTrue(result.contains("42292_9s_19900610041000"));
        assertTrue(result.contains("39727_22_19750113062500"));
        assertTrue(result.contains("40510_145_19930221211500"));

        assertEquals(4, result.size());

        /**
         * Test 4 wildChar search: title like *.ctd
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("title:*.ctd", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "wilCharSearch 4:\n{0}", resultReport);

        assertTrue(result.contains("42292_5p_19900609195600"));
        assertTrue(result.contains("42292_9s_19900610041000"));
        assertTrue(result.contains("39727_22_19750113062500"));
        assertTrue(result.contains("40510_145_19930221211500"));

        assertEquals(4, result.size());


        /**
         * Test 5 wildCharSearch: abstract LIKE *onnees CTD NEDIPROD VI 120
         */
        spatialQuery = new SpatialQuery("abstract:(*onnees CTD NEDIPROD VI 120)", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "wildCharSearch 5:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("42292_5p_19900609195600");

        //issues here it found
        assertEquals(expectedResult, result);

        /**
         * Test 6 wildCharSearch: identifier LIKE 40510_145_*
         */
        spatialQuery = new SpatialQuery("identifier:40510_145_*", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "wildCharSearch 6:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("40510_145_19930221211500");

        assertEquals(expectedResult, result);

        /**
         * Test 7 wildCharSearch: identifier LIKE *40510_145_*
         */
        spatialQuery = new SpatialQuery("identifier:*40510_145_*", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "wildCharSearch 7:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("40510_145_19930221211500");

        assertEquals(expectedResult, result);

    }

     /**
     * Test simple lucene date search.
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order = 4)
    public void dateSearchTest() throws Exception {
        Filter nullFilter   = null;
        String resultReport = "";

        /**
         * Test 1 date search: date after 25/01/2009
         */
        SpatialQuery spatialQuery = new SpatialQuery("date:{\"20090125000000\" 30000101000000}", nullFilter, SerialChainFilter.AND);
        Set<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "DateSearch 1:\n{0}", resultReport);

        Set<String> expectedResult = new LinkedHashSet<>();
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("CTDF02");

        assertEquals(expectedResult, result);

        /**
         * Test 2 date search: TempExtent_begin before 01/01/1985
         */
        spatialQuery = new SpatialQuery("TempExtent_begin:{00000101000000 \"19850101000000\"}", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "DateSearch 2:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("CTDF02");

        assertEquals(expectedResult, result);

        /**
         * Test 3 date search: TempExtent_end after 01/01/1991
         */
        spatialQuery = new SpatialQuery("TempExtent_end:{\"19910101000000\" 30000101000000}", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "DateSearch 3:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("CTDF02");
        expectedResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");

        assertEquals(expectedResult, result);

        /**
         * Test 4 date search: date = 26/01/2009
         */
        spatialQuery = new SpatialQuery("date:\"20090126122224\"", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "DateSearch 4:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("11325_158_19640418141800");
        //expectedResult.add("39727_22_19750113062500"); exclude since date time is handled
        //expectedResult.add("42292_9s_19900610041000"); exclude since date time is handled

        assertEquals(expectedResult, result);

        /**
         * Test 5 date search: date LIKE 26/01/200*
         */
        spatialQuery = new SpatialQuery("date:(200*0126*)", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "DateSearch 5:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("40510_145_19930221211500");

        assertEquals(expectedResult, result);

        /**
         * Test 6 date search: CreationDate between 01/01/1800 and 01/01/2000
         */
        spatialQuery = new SpatialQuery("CreationDate:[18000101000000  30000101000000]CreationDate:[00000101000000 20000101000000]", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "DateSearch 6:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("42292_5p_19900609195600");

        assertEquals(expectedResult, result);

        /**
         * Test 7 date time search: CreationDate after 1970-02-04T06:00:00
         */
        spatialQuery = new SpatialQuery("CreationDate:[19700204060000  30000101000000]", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "DateSearch 7:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("42292_5p_19900609195600");

        assertEquals(expectedResult, result);

    }

    /**
     * Test sorted lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order = 5)
    public void sortedSearchTest() throws Exception {

        Filter nullFilter   = null;
        String resultReport = "";

        /**
         * Test 1 sorted search: all orderBy identifier ASC
         */
        SpatialQuery spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        SortField sf = new SortField("identifier_sort", SortField.Type.STRING, false);
        spatialQuery.setSort(new Sort(sf));

        Set<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "SortedSearch 1:\n{0}", resultReport);

        Set<String> expectedResult = new LinkedHashSet<>();
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("28644bf0-5d9d-4ebd-bef0-f2b0b2067b26");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("484fc4d9-8d11-48a5-a386-65c19398f7c3");
        expectedResult.add("484fc4d9-8d11-48a5-a386-65c19398f7k7");
        expectedResult.add("666-999-666");
        expectedResult.add("937491cd-4bc4-43e4-9509-f6cc606f906e");
        expectedResult.add("999-666-999");
        expectedResult.add("CTDF02");
        expectedResult.add("cat-1");
        expectedResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        expectedResult.add("meta_NaN_id");
        expectedResult.add("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");
        assertEquals(expectedResult, result);

        /**
         * Test 2 sorted search: all orderBy identifier DSC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        sf = new SortField("identifier_sort", SortField.Type.STRING, true);
        spatialQuery.setSort(new Sort(sf));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "SortedSearch 2:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");
        expectedResult.add("meta_NaN_id");
        expectedResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        expectedResult.add("cat-1");
        expectedResult.add("CTDF02");
        expectedResult.add("999-666-999");
        expectedResult.add("937491cd-4bc4-43e4-9509-f6cc606f906e");
        expectedResult.add("666-999-666");
        expectedResult.add("484fc4d9-8d11-48a5-a386-65c19398f7k7");
        expectedResult.add("484fc4d9-8d11-48a5-a386-65c19398f7c3");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("28644bf0-5d9d-4ebd-bef0-f2b0b2067b26");
        expectedResult.add("11325_158_19640418141800");

        assertEquals(expectedResult, result);

        /**
         * Test 3 sorted search: all orderBy Abstract ASC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        sf = new SortField("Abstract_sort", SortField.Type.STRING, false);
        spatialQuery.setSort(new Sort(sf));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "SortedSearch 3:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("cat-1"); // TODO why cat-1 in first he is not indexable
        expectedResult.add("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");
        expectedResult.add("CTDF02");
        expectedResult.add("meta_NaN_id");
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        expectedResult.add("484fc4d9-8d11-48a5-a386-65c19398f7c3");
        expectedResult.add("484fc4d9-8d11-48a5-a386-65c19398f7k7");
        expectedResult.add("28644bf0-5d9d-4ebd-bef0-f2b0b2067b26");
        expectedResult.add("937491cd-4bc4-43e4-9509-f6cc606f906e");
        expectedResult.add("666-999-666");
        expectedResult.add("999-666-999");

        assertEquals(expectedResult, result);

        /**
         * Test 4 sorted search: all orderBy Abstract DSC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        sf = new SortField("Abstract_sort", SortField.Type.STRING, true);
        spatialQuery.setSort(new Sort(sf));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "SortedSearch 4:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("484fc4d9-8d11-48a5-a386-65c19398f7c3");
        expectedResult.add("484fc4d9-8d11-48a5-a386-65c19398f7k7");
        expectedResult.add("28644bf0-5d9d-4ebd-bef0-f2b0b2067b26");
        expectedResult.add("937491cd-4bc4-43e4-9509-f6cc606f906e");
        expectedResult.add("666-999-666");
        expectedResult.add("999-666-999");
        expectedResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("meta_NaN_id");
        expectedResult.add("CTDF02");
        expectedResult.add("cat-1");
        expectedResult.add("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");

        assertEquals(expectedResult, result);

        /**
         * Test 5 sorted search: orderBy CloudCover ASC with SortField.STRING => bad order
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("CloudCover:[0 TO 2147483648]", nullFilter, SerialChainFilter.AND);
        sf = new SortField("CloudCover_sort", SortField.Type.STRING, true);
        spatialQuery.setSort(new Sort(sf));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "SortedSearch 5:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();

        // i don't why we have the good order here
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");


        assertEquals(expectedResult, result);

        /**
         * Test 5 sorted search: orderBy CloudCover ASC with SortField.DOUBLE => good order
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("CloudCover:[0 TO 2147483648]", nullFilter, SerialChainFilter.AND);
        sf = new SortField("CloudCover_sort", SortField.Type.DOUBLE, true);
        spatialQuery.setSort(new Sort(sf));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "SortedSearch 5:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();

        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");


        assertEquals(expectedResult, result);
    }

    /**
     *
     * Test spatial lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order = 5)
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

        Set<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "spatialSearch 1:\n{0}", resultReport);

        Set<String> expectedResult = new LinkedHashSet<>();
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("CTDF02");

        assertEquals(expectedResult, result);

        /**
         * Test 1 spatial search: NOT BBOX filter
         */
        resultReport = "";
        List<Filter> lf = new ArrayList<>();
        //sf           = new BBOXFilter(bbox, "urn:x-ogc:def:crs:EPSG:6.11:4326");
        sf           = LuceneOGCFilter.wrap(FF.bbox(LuceneOGCFilter.GEOMETRY_PROPERTY, -20, -20, 20, 20, "EPSG:4326"));

        lf.add(sf);
        int[] op = {SerialChainFilter.NOT};
        SerialChainFilter f = new SerialChainFilter(lf, op);
        spatialQuery = new SpatialQuery("metafile:doc", f, SerialChainFilter.AND);

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result) {
            resultReport = resultReport + s + '\n';
        }

        LOGGER.log(Level.FINER, "spatialSearch 2:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("meta_NaN_id");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("cat-1");
        expectedResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        expectedResult.add("484fc4d9-8d11-48a5-a386-65c19398f7c3");
        expectedResult.add("484fc4d9-8d11-48a5-a386-65c19398f7k7");
        expectedResult.add("28644bf0-5d9d-4ebd-bef0-f2b0b2067b26");
        expectedResult.add("937491cd-4bc4-43e4-9509-f6cc606f906e");
        expectedResult.add("666-999-666");
        expectedResult.add("999-666-999");
        expectedResult.add("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");

        assertEquals("CRS URN are not working", expectedResult, result);
    }

    /**
     *
     * Test spatial lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order = 6)
    public void TermQueryTest() throws Exception {

        /**
         * Test 1
         */

        String identifier = "39727_22_19750113062500";
        String result = indexSearcher.identifierQuery(identifier);

        LOGGER.log(Level.FINER, "identifier query 1:\n{0}", result);

        String expectedResult = "39727_22_19750113062500";

        assertEquals(expectedResult, result);

        /**
         * Test 2
         */

        identifier = "CTDF02";
        result = indexSearcher.identifierQuery(identifier);

        LOGGER.log(Level.FINER, "identifier query 2:\n{0}", result);

        expectedResult = "CTDF02";

        assertEquals(expectedResult, result);

        /**
         * Test 3
         */

        identifier = "urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd";
        result = indexSearcher.identifierQuery(identifier);

        LOGGER.log(Level.FINER, "identifier query 3:\n{0}", result);

        expectedResult = "urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd";

        assertEquals(expectedResult, result);
    }

    /**
     *
     * Test spatial lucene search.
     *
     * @throws java.lang.Exception
     */
    @Test
    @Order(order = 7)
    public void DeleteDocumentTest() throws Exception {
        indexer.removeDocument("CTDF02");

        indexSearcher.refresh();

        /**
         * Test 1
         */

        String identifier = "39727_22_19750113062500";
        String result = indexSearcher.identifierQuery(identifier);

        LOGGER.log(Level.FINER, "identifier query 1:\n{0}", result);

        String expectedResult = "39727_22_19750113062500";

        assertEquals(expectedResult, result);

        /**
         * Test 2
         */

        identifier = "CTDF02";
        result = indexSearcher.identifierQuery(identifier);

        LOGGER.log(Level.FINER, "identifier query 2:\n{0}", result);

        expectedResult = null;

        assertEquals(expectedResult, result);
    }

    @Test
    @Order(order = 8)
    public void extractValuesTest() throws Exception {

        MD_IOFactory factory = null;
        final Iterator<MD_IOFactory> ite = ServiceRegistry.lookupProviders(MD_IOFactory.class);
        while (ite.hasNext()) {
            MD_IOFactory currentFactory = ite.next();
            if (currentFactory.matchImplementationType(ds, false)) {
                factory = currentFactory;
            }
        }

        Reader reader = factory.getReaderInstance(ds, false);
        FullRecord record = reader.getRecord("40510_145_19930221211500");

        assertNotNull(record);

        List<Value> result = MDWebIndexer.getValuesFromPathID("ISO 19115:MD_Metadata:identificationInfo:citation:date#dateType=creation:date", record);
        assertEquals(0, result.size());


        result = MDWebIndexer.getValuesFromPathID("ISO 19115:MD_Metadata:identificationInfo:citation:date#dateType=revision:date", record);
        assertEquals(1, result.size());

        result = MDWebIndexer.getValuesFromPathID("ISO 19115:MD_Metadata:identificationInfo:pointOfContact#role=originator:organisationName:value", record);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof TextValue);
        TextValue resultValue = (TextValue)result.get(0);
        assertEquals("IFREMER / IDM/SISMER", resultValue.getValue());

        result = MDWebIndexer.getValuesFromPathID("ISO 19115:MD_Metadata:identificationInfo:pointOfContact#role=custodian:organisationName:value", record);
        assertEquals(0, result.size());
    }
}

