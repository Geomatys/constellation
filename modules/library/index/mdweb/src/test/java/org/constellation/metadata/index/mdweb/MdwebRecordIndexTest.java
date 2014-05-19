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
import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//Junit dependencies
import javax.imageio.spi.ServiceRegistry;
import org.junit.*;
import static org.junit.Assert.*;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.Util;

// lucene dependencies
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

// geotoolkit dependencies
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.lucene.filter.LuceneOGCFilter;
import org.geotoolkit.lucene.filter.SerialChainFilter;
import org.geotoolkit.lucene.filter.SpatialQuery;
import org.geotoolkit.lucene.index.LuceneIndexSearcher;
import org.geotoolkit.util.FileUtilities;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;

// MDWeb dependencies
import org.mdweb.io.MD_IOException;
import org.mdweb.io.MD_IOFactory;
import org.mdweb.io.Writer;
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.Obligation;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.schemas.PrimitiveType;
import org.mdweb.model.schemas.Property;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.RecordSet;
import org.mdweb.model.storage.FullRecord;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.model.users.User;

// GeoAPI dependencies
import org.opengis.filter.FilterFactory2;


/**
 * Test class for constellation lucene index
 *
 * @author Guilhem Legal
 */
public class MdwebRecordIndexTest {

    protected static final FilterFactory2 FF = (FilterFactory2)
            FactoryFinder.getFilterFactory(new Hints(Hints.FILTER_FACTORY,FilterFactory2.class));

    private static final Logger LOGGER = Logging.getLogger("org.constellation.metadata");

    private static LuceneIndexSearcher indexSearcher;

    private static File configDirectory;

    private static final Level LOG_LEVEL = Level.FINER;

    private static DefaultDataSource ds;

    @BeforeClass
    public static void setUpClass() throws Exception {
        configDirectory = new File("config-test");
        if (configDirectory.exists()) {
            FileUtilities.deleteDirectory(configDirectory);
        }
        configDirectory.mkdir();

        final String url = "jdbc:derby:memory:ILTest;create=true";
        ds               = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115.sql"));

        MD_IOFactory factory = null;
        final Iterator<MD_IOFactory> ite = ServiceRegistry.lookupProviders(MD_IOFactory.class);
        while (ite.hasNext()) {
            MD_IOFactory currentFactory = ite.next();
            if (currentFactory.matchImplementationType(ds, false)) {
                factory = currentFactory;
            }
        }

        Writer writer = factory.getWriterInstance(ds, false);
        fillTestData(writer);
        writer.close();

        BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
        final Automatic configuration = new Automatic("mdweb", bdd);
        configuration.setConfigurationDirectory(configDirectory);

        final MDWebIndexer indexer    = new MDWebIndexer(configuration, "", true);
        indexSearcher                   = new LuceneIndexSearcher(configDirectory, "", null, true);
        indexSearcher.setLogLevel(LOG_LEVEL);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        configDirectory = new File("config-test");
        if (configDirectory.exists()) {
            FileUtilities.deleteDirectory(configDirectory);
        }
        ds.shutdown();
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
    public void simpleSearchTest() throws Exception {
        Filter nullFilter   = null;
        String resultReport = "";

        /**
         * Test 1 simple search: title = title1
         */
        SpatialQuery spatialQuery = new SpatialQuery("title:title1", nullFilter, SerialChainFilter.AND);
        Set<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 1:\n{0}", resultReport);

        Set<String> expectedResult = new LinkedHashSet<>();
        expectedResult.add("2345-aa453-ade456");

        assertEquals(expectedResult, result);

        /**
         * Test 2 simple search: title like tit*
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("title:tit*", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 2:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        expectedResult.add("test-5");
        expectedResult.add("test-6");

        assertEquals(expectedResult, result);

        /**
         * Test 3 simple search: identifier != 2345-aa453-ade456
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc NOT identifier:\"2345-aa453-ade456\"", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 3:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        expectedResult.add("test-5");
        expectedResult.add("test-6");

        assertEquals(expectedResult, result);

        /**
         * Test 4 simple search: (identifier = 2345-aa453-ade456 AND title = title1 ) OR (NOT title= title4)
         */
        resultReport          = "";
        spatialQuery          = new SpatialQuery("identifier:\"2345-aa453-ade456\" AND title:\"title1\"", nullFilter, SerialChainFilter.OR);
        SpatialQuery subQuery = new SpatialQuery("title:\"title4\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 4:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("test-5");
        expectedResult.add("test-6");

        assertEquals(expectedResult, result);

        /**
         * Test 5 simple search: (identifier = 2345-aa453-ade456 OR title = title2 ) AND (NOT title= title4)
         */
        resultReport          = "";
        spatialQuery          = new SpatialQuery("identifier:\"2345-aa453-ade456\" OR title:\"title2\"", nullFilter, SerialChainFilter.AND);
        subQuery = new SpatialQuery("title:\"title4\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 5:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");

        /**
         * Test 5 simple search: (identifier = 2345-aa453-ade456 OR title = title2 ) AND (NOT type=xirces)
         */
        resultReport          = "";
        spatialQuery          = new SpatialQuery("identifier:\"2345-aa453-ade456\" OR title:\"title2\"", nullFilter, SerialChainFilter.AND);
        subQuery = new SpatialQuery("type:\"xirces\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 5:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");


        assertEquals(expectedResult, result);

        /**
         * Test 6 simple search: (NOT identifier = 2345-aa453-ade456 AND NOT title = title2 )
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        subQuery     = new SpatialQuery("identifier:\"2345-aa453-ade456\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);
        subQuery     = new SpatialQuery("title:\"title2\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);

        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 6:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        expectedResult.add("test-5");
        expectedResult.add("test-6");

        assertEquals(expectedResult, result);

        /**
         * Test 7 simple search: (NOT identifier = 2345-aa453-ade456 OR NOT title = title2 )
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.OR);
        subQuery     = new SpatialQuery("identifier:\"2345-aa453-ade456\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);
        subQuery     = new SpatialQuery("title:\"title2\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);

        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 7:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        expectedResult.add("test-5");
        expectedResult.add("test-6");
        expectedResult.add("2345-aa453-ade456");

        assertEquals(expectedResult, result);

        /**
         * Test 8 simple search: (NOT identifier = 2345-aa453-ade456 OR NOT title = title1 )
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.OR);
        subQuery     = new SpatialQuery("identifier:\"2345-aa453-ade456\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);
        subQuery     = new SpatialQuery("title:\"title1\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);

        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 8:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        expectedResult.add("test-5");
        expectedResult.add("test-6");

        assertEquals(expectedResult, result);

        /**
         * Test 9 simple search: type < silent hill
         */
        spatialQuery = new SpatialQuery("type:[0 TO \"silent hill\"]", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 9:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("test-5");
        expectedResult.add("test-6");
        assertEquals(expectedResult, result);

        /**
         * Test 10 simple search: type > silent hill
         */
        spatialQuery = new SpatialQuery("type:[\"silent hill\" TO z]", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 10:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        assertEquals(expectedResult, result);

        /**
         * Test 11 simple search: 11458.test < type < silent hill
         */
        spatialQuery = new SpatialQuery("type:[\"21958.test\" TO \"silent hill\"]", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 11:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("test-6");
        assertEquals(expectedResult, result);

        /**
         * Test 12 simple search: 9 < type < a
         */
        spatialQuery = new SpatialQuery("type:[ba TO bov]", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 11:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("test-6");
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
         * Test 1 sorted search: all orderBy type ASC
         */
        SpatialQuery spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        SortField sf = new SortField("type_sort", SortField.Type.STRING, false);
        spatialQuery.setSort(new Sort(sf));

        Set<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SortedSearch 1:\n{0}", resultReport);

        Set<String> expectedResult = new LinkedHashSet<>();
        expectedResult.add("test-5");
        expectedResult.add("test-6");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");

        assertEquals(expectedResult, result);

        /**
         * Test 2 sorted search: all orderBy type DSC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        sf           = new SortField("type_sort", SortField.Type.STRING, true);
        spatialQuery.setSort(new Sort(sf));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SortedSearch 2:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("test-6");
        expectedResult.add("test-5");

        assertEquals(expectedResult, result);

        /**
         * Test 3 sorted search: all orderBy identifier ASC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        sf           = new SortField("identifier_sort", SortField.Type.STRING, false);
        spatialQuery.setSort(new Sort(sf));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SortedSearch 3:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("test-5");
        expectedResult.add("test-6");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");

        assertEquals(expectedResult, result);

        /**
         * Test 4 sorted search: all orderBy identifier DSC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        sf           = new SortField("identifier_sort", SortField.Type.STRING, true);
        spatialQuery.setSort(new Sort(sf));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SortedSearch 4:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        expectedResult.add("test-6");
        expectedResult.add("test-5");
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");

        assertEquals(expectedResult, result);
    }

    /**
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
        LuceneOGCFilter sf          = LuceneOGCFilter.wrap(FF.bbox(LuceneOGCFilter.GEOMETRY_PROPERTY, -20, -20, 20, 20, "EPSG:4326"));
        SpatialQuery spatialQuery = new SpatialQuery("metafile:doc", sf, SerialChainFilter.AND);

        Set<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "spatialSearch 1:\n{0}", resultReport);

        Set<String> expectedResult = new LinkedHashSet<>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");

        assertEquals(expectedResult, result);

        /**
         * Test 1 spatial search: NOT BBOX filter
         */
        resultReport = "";
        List<Filter> lf = new ArrayList<>();
        sf           = LuceneOGCFilter.wrap(FF.bbox(LuceneOGCFilter.GEOMETRY_PROPERTY, -20, -20, 20, 20, "EPSG:4326"));
        lf.add(sf);
        int[] op = {SerialChainFilter.NOT};
        SerialChainFilter f = new SerialChainFilter(lf, op);
        spatialQuery = new SpatialQuery("metafile:doc", f, SerialChainFilter.AND);

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "spatialSearch 2:\n{0}", resultReport);

        expectedResult = new LinkedHashSet<>();
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("test-5");
        expectedResult.add("test-6");

        assertEquals(expectedResult, result);
    }


    public static void fillTestData(Writer writer) throws MD_IOException {

        //we create a new Catalog
        RecordSet cat             = new RecordSet("CATEST", "CATEST");
        writer.writeRecordSet(cat);

        //then we build the classes
        Classe sLiteralClass    = new Classe(Standard.DUBLINCORE, "SimpleLiteral", "sl", "no definition", null, false, ' ');
        Property contentProp    = new Property(Standard.DUBLINCORE, "content", "ct", "no definition", sLiteralClass, PrimitiveType.STRING, 0, 1, Obligation.OPTIONNAL, 1, 'V');
        Property schemeProp     = new Property(Standard.DUBLINCORE, "scheme", "sh", "no definition", sLiteralClass, PrimitiveType.STRING, 0, 1, Obligation.OPTIONNAL, 1, 'P');

        writer.writeClasse(sLiteralClass);

        Classe boundingBoxClass = new Classe(Standard.OWS, "BoundingBox", "bbox", "no defintion", null, false, ' ');
        Property lowerProp      = new Property(Standard.OWS, "LowerCorner", "lc", "no definition", boundingBoxClass, PrimitiveType.REAL,0, 1, Obligation.OPTIONNAL, 1, ' ');
        Property upperProp      = new Property(Standard.OWS, "UpperCorner", "uc", "no definition", boundingBoxClass, PrimitiveType.REAL,0, 1, Obligation.OPTIONNAL, 1, ' ');

        writer.writeClasse(boundingBoxClass);

        Classe recordClass      = new Classe(Standard.CSW, "Record", "rec", "no definition", null, false, ' ');
        Property identifierProp = new Property(Standard.DUBLINCORE, "identifier", "id", "no definition", recordClass, sLiteralClass, 0, 1, Obligation.OPTIONNAL, 1, ' ');
        Property titleProp      = new Property(Standard.DUBLINCORE, "title", "ti", "no definition", recordClass, sLiteralClass, 0, 1, Obligation.OPTIONNAL, 2, ' ');
        Property typeProp       = new Property(Standard.DUBLINCORE, "type", "ty", "no definition", recordClass, sLiteralClass, 0, 1, Obligation.OPTIONNAL, 3, ' ');
        Property bboxProp       = new Property(Standard.OWS, "BoundingBox", "box", "no definition", recordClass, boundingBoxClass, 0, 1, Obligation.OPTIONNAL, 4, ' ');

        writer.writeClasse(recordClass);

        Classe identifiable     = new Classe(Standard.EBRIM_V3, "Identifiable", "id", "no definition", null, false, ' ');
        Classe registryObject   = new Classe(Standard.EBRIM_V2_5, "RegsitryObject", "ro", "no definition", null, false, ' ');
        writer.writeClasse(identifiable);
        writer.writeClasse(registryObject);

        //The paths
        writer.writeStandard(Standard.CSW);
        writer.writeStandard(Standard.DUBLINCORE);
        writer.writeStandard(Standard.OWS);

        Path recordPath      = new Path(Standard.CSW, recordClass);

        Path identifierPath  = new Path(recordPath, identifierProp);
        Path idenContentPath = new Path(identifierPath, contentProp);

        Path titlePath       = new Path(recordPath, titleProp);
        Path titContentPath  = new Path(titlePath, contentProp);

        Path typePath        = new Path(recordPath, typeProp);
        Path typContentPath  = new Path(typePath, contentProp);

        Path bboxPath        = new Path(recordPath, bboxProp);
        Path lowerCornPath   = new Path(bboxPath, lowerProp);
        Path upperCornPath   = new Path(bboxPath, upperProp);

        writer.writePath(recordPath);
        writer.writePath(identifierPath);
        writer.writePath(idenContentPath);

        writer.writePath(titlePath);
        writer.writePath(titContentPath);

        writer.writePath(typePath);
        writer.writePath(typContentPath);
        writer.writePath(bboxPath);
        writer.writePath(lowerCornPath);
        writer.writePath(upperCornPath);

        //The records
        Date d = new Date(120);
        User inputUser = new User("admin", null, d, d);
        writer.writeUser(inputUser);

        FullRecord f1 = new FullRecord(1, "2345-aa453-ade456", cat, "title1", inputUser, null, null, d, d, null, true, true, FullRecord.TYPE.NORMALRECORD);
        Value f1_rootValue    = new Value(recordPath, f1, 1, recordClass, null, null);
        Value f1_ident        = new Value(identifierPath, f1, 1, sLiteralClass, f1_rootValue, null);
        TextValue f1_idValue  = new TextValue(idenContentPath, f1, 1, "2345-aa453-ade456", PrimitiveType.STRING, f1_ident, null);
        Value f1_title        = new Value(titlePath, f1, 1, sLiteralClass, f1_rootValue, null);
        TextValue f1_tiValue  = new TextValue(titContentPath, f1, 1, "title1", PrimitiveType.STRING, f1_title, null);
        Value f1_type         = new Value(typePath, f1, 1, sLiteralClass, f1_rootValue, null);
        TextValue f1_tyValue  = new TextValue(typContentPath, f1, 1, "xirces", PrimitiveType.STRING, f1_type, null);
        Value f1_bbox         = new Value(bboxPath, f1, 1, boundingBoxClass, f1_rootValue, null);
        TextValue f1_lcxValue = new TextValue(lowerCornPath, f1, 1, "30", PrimitiveType.STRING, f1_bbox, null);
        TextValue f1_lcyValue = new TextValue(lowerCornPath, f1, 2, "0", PrimitiveType.STRING, f1_bbox, null);
        TextValue f1_ucxValue = new TextValue(upperCornPath, f1, 1, "50", PrimitiveType.STRING, f1_bbox, null);
        TextValue f1_ucyValue = new TextValue(upperCornPath, f1, 2, "15", PrimitiveType.STRING, f1_bbox, null);

        FullRecord f2 = new FullRecord(2, "00180e67-b7cf-40a3-861d-b3a09337b195", cat, "title2", inputUser, null, null, d, d, null,true, true, FullRecord.TYPE.NORMALRECORD);
        Value f2_rootValue    = new Value(recordPath, f2, 1, recordClass, null, null);
        Value f2_ident        = new Value(identifierPath, f2, 1, sLiteralClass, f2_rootValue, null);
        TextValue f2_idValue  = new TextValue(idenContentPath, f2, 1, "00180e67-b7cf-40a3-861d-b3a09337b195", PrimitiveType.STRING, f2_ident, null);
        Value f2_title        = new Value(titlePath, f2, 1, sLiteralClass, f2_rootValue, null);
        TextValue f2_tiValue  = new TextValue(titContentPath, f2, 1, "title2", PrimitiveType.STRING, f2_title, null);
        Value f2_type         = new Value(typePath, f2, 1, sLiteralClass, f2_rootValue, null);
        TextValue f2_tyValue  = new TextValue(typContentPath, f2, 1, "service", PrimitiveType.STRING, f2_type, null);
        Value f2_bbox         = new Value(bboxPath, f2, 1, boundingBoxClass, f2_rootValue, null);
        TextValue f2_lcxValue = new TextValue(lowerCornPath, f2, 1, "-30", PrimitiveType.STRING, f2_bbox, null);
        TextValue f2_lcyValue = new TextValue(lowerCornPath, f2, 2, "0", PrimitiveType.STRING, f2_bbox, null);
        TextValue f2_ucxValue = new TextValue(upperCornPath, f2, 1, "-15", PrimitiveType.STRING, f2_bbox, null);
        TextValue f2_ucyValue = new TextValue(upperCornPath, f2, 2, "10", PrimitiveType.STRING, f2_bbox, null);

        FullRecord f3 = new FullRecord(3, "09844e51-e5cd-52c3-737d-b3a61366d028", cat, "bo", inputUser, null, null, d, d, null,true, true, FullRecord.TYPE.NORMALRECORD);
        Value f3_rootValue    = new Value(recordPath, f3, 1, recordClass, null, null);
        Value f3_ident        = new Value(identifierPath, f3, 1, sLiteralClass, f3_rootValue, null);
        TextValue f3_idValue  = new TextValue(idenContentPath, f3, 1, "09844e51-e5cd-52c3-737d-b3a61366d028", PrimitiveType.STRING, f3_ident, null);
        Value f3_title        = new Value(titlePath, f3, 1, sLiteralClass, f3_rootValue, null);
        TextValue f3_tiValue  = new TextValue(titContentPath, f3, 1, "bo", PrimitiveType.STRING, f3_title, null);
        Value f3_type         = new Value(typePath, f3, 1, sLiteralClass, f3_rootValue, null);
        TextValue f3_tyValue  = new TextValue(typContentPath, f3, 1, "dataset", PrimitiveType.STRING, f3_type, null);
        Value f3_bbox         = new Value(bboxPath, f3, 1, boundingBoxClass, f3_rootValue, null);
        TextValue f3_lcxValue = new TextValue(lowerCornPath, f3, 1, "5", PrimitiveType.STRING, f3_bbox, null);
        TextValue f3_lcyValue = new TextValue(lowerCornPath, f3, 2, "10", PrimitiveType.STRING, f3_bbox, null);
        TextValue f3_ucxValue = new TextValue(upperCornPath, f3, 1, "10", PrimitiveType.STRING, f3_bbox, null);
        TextValue f3_ucyValue = new TextValue(upperCornPath, f3, 2, "15", PrimitiveType.STRING, f3_bbox, null);

        FullRecord f4 = new FullRecord(4, "urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f", cat, "title4", inputUser, null, null, d, d, null,true, true, FullRecord.TYPE.NORMALRECORD);
        Value f4_rootValue    = new Value(recordPath, f4, 1, recordClass, null, null);
        Value f4_ident        = new Value(identifierPath, f4, 1, sLiteralClass, f4_rootValue, null);
        TextValue f4_idValue  = new TextValue(idenContentPath, f4, 1, "urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f", PrimitiveType.STRING, f4_ident, null);
        Value f4_title        = new Value(titlePath, f4, 1, sLiteralClass, f4_rootValue, null);
        TextValue f4_tiValue  = new TextValue(titContentPath, f4, 1, "title4", PrimitiveType.STRING, f4_title, null);
        Value f4_type         = new Value(typePath, f4, 1, sLiteralClass, f4_rootValue, null);
        TextValue f4_tyValue  = new TextValue(typContentPath, f4, 1, "xircos", PrimitiveType.STRING, f4_type, null);
        Value f4_bbox         = new Value(bboxPath, f4, 1, boundingBoxClass, f4_rootValue, null);
        TextValue f4_lcxValue = new TextValue(lowerCornPath, f4, 1, "5", PrimitiveType.STRING, f4_bbox, null);
        TextValue f4_lcyValue = new TextValue(lowerCornPath, f4, 2, "10", PrimitiveType.STRING, f4_bbox, null);
        TextValue f4_ucxValue = new TextValue(upperCornPath, f4, 1, "10", PrimitiveType.STRING, f4_bbox, null);
        TextValue f4_ucyValue = new TextValue(upperCornPath, f4, 2, "15", PrimitiveType.STRING, f4_bbox, null);

        FullRecord f5 = new FullRecord(5, "test-5", cat, "title5", inputUser, null, null, d, d, null,true, true, FullRecord.TYPE.NORMALRECORD);
        Value f5_rootValue    = new Value(recordPath, f5, 1, recordClass, null, null);
        Value f5_title        = new Value(titlePath, f5, 1, sLiteralClass, f5_rootValue, null);
        TextValue f5_tiValue  = new TextValue(titContentPath, f5, 1, "title5", PrimitiveType.STRING, f4_title, null);
        Value f5_ident        = new Value(identifierPath, f5, 1, sLiteralClass, f5_rootValue, null);
        TextValue f5_idValue  = new TextValue(idenContentPath, f5, 1, "test-5", PrimitiveType.STRING, f5_ident, null);
        Value f5_type         = new Value(typePath, f5, 1, sLiteralClass, f5_rootValue, null);
        TextValue f5_tyValue  = new TextValue(typContentPath, f5, 1, "218a", PrimitiveType.STRING, f5_type, null);


        FullRecord f6 = new FullRecord(6, "test-6", cat, "title6", inputUser, null, null, d, d, null,true, true, FullRecord.TYPE.NORMALRECORD);
        Value f6_rootValue    = new Value(recordPath, f6, 1, recordClass, null, null);
        Value f6_ident        = new Value(identifierPath, f6, 1, sLiteralClass, f6_rootValue, null);
        TextValue f6_idValue  = new TextValue(idenContentPath, f6, 1, "test-6", PrimitiveType.STRING, f6_ident, null);
        Value f6_type         = new Value(typePath, f6, 1, sLiteralClass, f6_rootValue, null);
        TextValue f6_tyValue  = new TextValue(typContentPath, f6, 1, "Bou", PrimitiveType.STRING, f6_type, null);
        Value f6_title        = new Value(titlePath, f6, 1, sLiteralClass, f6_rootValue, null);
        TextValue f6_tiValue  = new TextValue(titContentPath, f6, 1, "title6", PrimitiveType.STRING, f6_title, null);


        writer.writeRecord(f1, false, true);
        writer.writeRecord(f2, false, true);
        writer.writeRecord(f3, false, true);
        writer.writeRecord(f4, false, true);
        writer.writeRecord(f5, false, true);
        writer.writeRecord(f6, false, true);


    }
}
