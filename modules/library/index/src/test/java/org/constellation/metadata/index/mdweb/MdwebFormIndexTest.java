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
package org.constellation.metadata.index.mdweb;

// J2SE dependencies
import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//Junit dependencies
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
import org.geotoolkit.lucene.index.AbstractIndexSearcher;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.logging.Logging;
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
import org.mdweb.model.storage.Form;
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
public class MdwebFormIndexTest {

    protected static final FilterFactory2 FF = (FilterFactory2)
            FactoryFinder.getFilterFactory(new Hints(Hints.FILTER_FACTORY,FilterFactory2.class));
    
    private static final Logger LOGGER = Logging.getLogger("org.constellation.metadata");
   
    private static AbstractIndexSearcher indexSearcher;

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
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/model/mdw_schema_2.1(derby).sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19115.sql"));

        Writer writer = MD_IOFactory.getWriterInstance(ds, false);
        fillTestData(writer);
        writer.close();

        BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
        final Automatic configuration = new Automatic("mdweb", bdd);
        configuration.setConfigurationDirectory(configDirectory);
        
        final MDWebIndexer indexer    = new MDWebIndexer(configuration, "");
        indexer.destroy();
        indexSearcher                   = new AbstractIndexSearcher(configDirectory, "");
        indexSearcher.setLogLevel(LOG_LEVEL);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        configDirectory = new File("config-test");
        if (configDirectory.exists()) {
            FileUtilities.deleteDirectory(configDirectory);
        }
        ds.shutdown();
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
        SpatialQuery spatialQuery = new SpatialQuery("Title:title1", nullFilter, SerialChainFilter.AND);
        List<String> result = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        LOGGER.log(LOG_LEVEL, "SimpleSearch 1:\n{0}", resultReport);
        
        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("2345-aa453-ade456");
        
        assertEquals(expectedResult, result);
        
        /**
         * Test 2 simple search: title like tit*
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("Title:tit*", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        LOGGER.log(LOG_LEVEL, "SimpleSearch 2:\n{0}", resultReport);
        
        expectedResult = new ArrayList<String>();
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        
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
        
        expectedResult = new ArrayList<String>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        
        assertEquals(expectedResult, result);

        /**
         * Test 4 simple search: (identifier = 2345-aa453-ade456 AND title = title1 ) OR (NOT title= title4)
         */
        resultReport          = "";
        spatialQuery          = new SpatialQuery("identifier:\"2345-aa453-ade456\" AND Title:\"title1\"", nullFilter, SerialChainFilter.OR);
        SpatialQuery subQuery = new SpatialQuery("Title:\"title4\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 4:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        

        assertEquals(expectedResult, result);

        /**
         * Test 5 simple search: (identifier = 2345-aa453-ade456 OR title = title2 ) AND (NOT title= title4)
         */
        resultReport          = "";
        spatialQuery          = new SpatialQuery("identifier:\"2345-aa453-ade456\" OR Title:\"title2\"", nullFilter, SerialChainFilter.AND);
        subQuery = new SpatialQuery("Title:\"title4\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 5:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");

        /**
         * Test 5 simple search: (identifier = 2345-aa453-ade456 OR title = title2 ) AND (NOT type=xirces)
         */
        resultReport          = "";
        spatialQuery          = new SpatialQuery("identifier:\"2345-aa453-ade456\" OR Title:\"title2\"", nullFilter, SerialChainFilter.AND);
        subQuery = new SpatialQuery("type:\"xirces\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 5:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");


        assertEquals(expectedResult, result);

        /**
         * Test 6 simple search: (NOT identifier = 2345-aa453-ade456 AND NOT title = title2 )
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        subQuery     = new SpatialQuery("identifier:\"2345-aa453-ade456\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);
        subQuery     = new SpatialQuery("Title:\"title2\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);

        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 6:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");


        assertEquals(expectedResult, result);

        /**
         * Test 7 simple search: (NOT identifier = 2345-aa453-ade456 OR NOT title = title2 )
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.OR);
        subQuery     = new SpatialQuery("identifier:\"2345-aa453-ade456\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);
        subQuery     = new SpatialQuery("Title:\"title2\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);

        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 7:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        expectedResult.add("2345-aa453-ade456");



        assertEquals(expectedResult, result);

        /**
         * Test 8 simple search: (NOT identifier = 2345-aa453-ade456 OR NOT title = title1 )
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.OR);
        subQuery     = new SpatialQuery("identifier:\"2345-aa453-ade456\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);
        subQuery     = new SpatialQuery("Title:\"title1\"", nullFilter, SerialChainFilter.NOT);
        spatialQuery.addSubQuery(subQuery);

        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        LOGGER.log(LOG_LEVEL, "SimpleSearch 8:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");



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
        SortField sf = new SortField("type_sort", SortField.STRING, false);
        spatialQuery.setSort(new Sort(sf));
        
        List<String> result = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        LOGGER.log(LOG_LEVEL, "SortedSearch 1:\n{0}", resultReport);
        
        List<String> expectedResult = new ArrayList<String>();
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
        sf           = new SortField("type_sort", SortField.STRING, true);
        spatialQuery.setSort(new Sort(sf));
        
        result = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        LOGGER.log(LOG_LEVEL, "SortedSearch 2:\n{0}", resultReport);
        
        expectedResult = new ArrayList<String>();
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        
        assertEquals(expectedResult, result);
        
        /**
         * Test 3 sorted search: all orderBy identifier ASC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        sf           = new SortField("identifier_sort", SortField.STRING, false);
        spatialQuery.setSort(new Sort(sf));
        
        result = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        LOGGER.log(LOG_LEVEL, "SortedSearch 3:\n{0}", resultReport);
        
        expectedResult = new ArrayList<String>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("2345-aa453-ade456");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        
        assertEquals(expectedResult, result);
        
        /**
         * Test 4 sorted search: all orderBy identifier DSC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        sf           = new SortField("identifier_sort", SortField.STRING, true);
        spatialQuery.setSort(new Sort(sf));
        
        result = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        LOGGER.log(LOG_LEVEL, "SortedSearch 4:\n{0}", resultReport);
        
        expectedResult = new ArrayList<String>();
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
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
        
        List<String> result = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        LOGGER.log(LOG_LEVEL, "spatialSearch 1:\n{0}", resultReport);
        
        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("00180e67-b7cf-40a3-861d-b3a09337b195");
        expectedResult.add("09844e51-e5cd-52c3-737d-b3a61366d028");
        expectedResult.add("urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        
        assertEquals(expectedResult, result);
        
        /**
         * Test 1 spatial search: NOT BBOX filter
         */
        resultReport = "";
        List<Filter> lf = new ArrayList<Filter>();
        sf           = LuceneOGCFilter.wrap(FF.bbox(LuceneOGCFilter.GEOMETRY_PROPERTY, -20, -20, 20, 20, "EPSG:4326"));
        lf.add(sf);
        int[] op = {SerialChainFilter.NOT};
        SerialChainFilter f = new SerialChainFilter(lf, op);
        spatialQuery = new SpatialQuery("metafile:doc", f, SerialChainFilter.AND);
        
        result = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        LOGGER.log(LOG_LEVEL, "spatialSearch 2:\n{0}", resultReport);
        
        expectedResult = new ArrayList<String>();
        expectedResult.add("2345-aa453-ade456");
        
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
        
        //The forms
        Date d = new Date(120);
        User inputUser = new User("admin", "adminadmin", null, d, d);
        writer.writeUser(inputUser);
        
        Form f1 = new Form(1, "2345-aa453-ade456", cat, "title1", inputUser, null, null, d, d, null, true, true, Form.TYPE.NORMALFORM);
        Value f1_rootValue    = new Value(recordPath, f1, 1, recordClass, null);
        Value f1_ident        = new Value(identifierPath, f1, 1, sLiteralClass, f1_rootValue);
        TextValue f1_idValue  = new TextValue(idenContentPath, f1, 1, "2345-aa453-ade456", PrimitiveType.STRING, f1_ident);
        Value f1_title        = new Value(titlePath, f1, 1, sLiteralClass, f1_rootValue);
        TextValue f1_tiValue  = new TextValue(titContentPath, f1, 1, "title1", PrimitiveType.STRING, f1_title);
        Value f1_type         = new Value(typePath, f1, 1, sLiteralClass, f1_rootValue);
        TextValue f1_tyValue  = new TextValue(typContentPath, f1, 1, "xirces", PrimitiveType.STRING, f1_type);
        Value f1_bbox         = new Value(bboxPath, f1, 1, boundingBoxClass, f1_rootValue);
        TextValue f1_lcxValue = new TextValue(lowerCornPath, f1, 1, "30", PrimitiveType.STRING, f1_bbox);
        TextValue f1_lcyValue = new TextValue(lowerCornPath, f1, 2, "0", PrimitiveType.STRING, f1_bbox);
        TextValue f1_ucxValue = new TextValue(upperCornPath, f1, 1, "50", PrimitiveType.STRING, f1_bbox);
        TextValue f1_ucyValue = new TextValue(upperCornPath, f1, 2, "15", PrimitiveType.STRING, f1_bbox);
        
        Form f2 = new Form(2, "00180e67-b7cf-40a3-861d-b3a09337b195", cat, "title2", inputUser, null, null, d, d, null,true, true, Form.TYPE.NORMALFORM);
        Value f2_rootValue    = new Value(recordPath, f2, 1, recordClass, null);
        Value f2_ident        = new Value(identifierPath, f2, 1, sLiteralClass, f2_rootValue);
        TextValue f2_idValue  = new TextValue(idenContentPath, f2, 1, "00180e67-b7cf-40a3-861d-b3a09337b195", PrimitiveType.STRING, f2_ident);
        Value f2_title        = new Value(titlePath, f2, 1, sLiteralClass, f2_rootValue);
        TextValue f2_tiValue  = new TextValue(titContentPath, f2, 1, "title2", PrimitiveType.STRING, f2_title);
        Value f2_type         = new Value(typePath, f2, 1, sLiteralClass, f2_rootValue);
        TextValue f2_tyValue  = new TextValue(typContentPath, f2, 1, "service", PrimitiveType.STRING, f2_type);
        Value f2_bbox         = new Value(bboxPath, f2, 1, boundingBoxClass, f2_rootValue);
        TextValue f2_lcxValue = new TextValue(lowerCornPath, f2, 1, "-30", PrimitiveType.STRING, f2_bbox);
        TextValue f2_lcyValue = new TextValue(lowerCornPath, f2, 2, "0", PrimitiveType.STRING, f2_bbox);
        TextValue f2_ucxValue = new TextValue(upperCornPath, f2, 1, "-15", PrimitiveType.STRING, f2_bbox);
        TextValue f2_ucyValue = new TextValue(upperCornPath, f2, 2, "10", PrimitiveType.STRING, f2_bbox);
        
        Form f3 = new Form(3, "09844e51-e5cd-52c3-737d-b3a61366d028", cat, "title3", inputUser, null, null, d, d, null,true, true, Form.TYPE.NORMALFORM);
        Value f3_rootValue    = new Value(recordPath, f3, 1, recordClass, null);
        Value f3_ident        = new Value(identifierPath, f3, 1, sLiteralClass, f3_rootValue);
        TextValue f3_idValue  = new TextValue(idenContentPath, f3, 1, "09844e51-e5cd-52c3-737d-b3a61366d028", PrimitiveType.STRING, f3_ident);
        Value f3_title        = new Value(titlePath, f3, 1, sLiteralClass, f3_rootValue);
        TextValue f3_tiValue  = new TextValue(titContentPath, f3, 1, "bo", PrimitiveType.STRING, f3_title);
        Value f3_type         = new Value(typePath, f3, 1, sLiteralClass, f3_rootValue);
        TextValue f3_tyValue  = new TextValue(typContentPath, f3, 1, "dataset", PrimitiveType.STRING, f3_type);
        Value f3_bbox         = new Value(bboxPath, f3, 1, boundingBoxClass, f3_rootValue);
        TextValue f3_lcxValue = new TextValue(lowerCornPath, f3, 1, "5", PrimitiveType.STRING, f3_bbox);
        TextValue f3_lcyValue = new TextValue(lowerCornPath, f3, 2, "10", PrimitiveType.STRING, f3_bbox);
        TextValue f3_ucxValue = new TextValue(upperCornPath, f3, 1, "10", PrimitiveType.STRING, f3_bbox);
        TextValue f3_ucyValue = new TextValue(upperCornPath, f3, 2, "15", PrimitiveType.STRING, f3_bbox);
        
        Form f4 = new Form(4, "urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f", cat, "title4", inputUser, null, null, d, d, null,true, true, Form.TYPE.NORMALFORM);
        Value f4_rootValue    = new Value(recordPath, f4, 1, recordClass, null);
        Value f4_ident        = new Value(identifierPath, f4, 1, sLiteralClass, f4_rootValue);
        TextValue f4_idValue  = new TextValue(idenContentPath, f4, 1, "urn:uuid:19887a8a-f6b0-4a63-ae56-7fba0e17801f", PrimitiveType.STRING, f4_ident);
        Value f4_title        = new Value(titlePath, f4, 1, sLiteralClass, f4_rootValue);
        TextValue f4_tiValue  = new TextValue(titContentPath, f4, 1, "title4", PrimitiveType.STRING, f4_title);
        Value f4_type         = new Value(typePath, f4, 1, sLiteralClass, f4_rootValue);
        TextValue f4_tyValue  = new TextValue(typContentPath, f4, 1, "xircos", PrimitiveType.STRING, f4_type);
        Value f4_bbox         = new Value(bboxPath, f4, 1, boundingBoxClass, f4_rootValue);
        TextValue f4_lcxValue = new TextValue(lowerCornPath, f4, 1, "5", PrimitiveType.STRING, f4_bbox);
        TextValue f4_lcyValue = new TextValue(lowerCornPath, f4, 2, "10", PrimitiveType.STRING, f4_bbox);
        TextValue f4_ucxValue = new TextValue(upperCornPath, f4, 1, "10", PrimitiveType.STRING, f4_bbox);
        TextValue f4_ucyValue = new TextValue(upperCornPath, f4, 2, "15", PrimitiveType.STRING, f4_bbox);
        
        writer.writeForm(f1, false, true);
        writer.writeForm(f2, false, true);
        writer.writeForm(f3, false, true);
        writer.writeForm(f4, false, true);
        
        
    }
}
