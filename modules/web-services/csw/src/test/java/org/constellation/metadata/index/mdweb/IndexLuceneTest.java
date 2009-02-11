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
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

//Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

// Constellation dependencies
import org.constellation.lucene.filter.SerialChainFilter;
import org.constellation.lucene.filter.SpatialFilter;
import org.constellation.lucene.filter.SpatialQuery;

// lucene dependencies
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Sort;

// geotools dependencies
import org.constellation.lucene.filter.BBOXFilter;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;

// MDWeb dependencies
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.Obligation;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.schemas.PrimitiveType;
import org.mdweb.model.schemas.Property;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;

// GeoAPI dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Test class for constellation lucene index 
 * 
 * @author Guilhem Legal
 */
public class IndexLuceneTest {
    
    
    private Logger logger = Logger.getLogger("org.constellation.metadata");
   
    private static MDWebIndexSearcher indexSearcher;

    private static File configDirectory;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        configDirectory = new File("config-test");
        if (!configDirectory.exists())
            configDirectory.mkdir();
        else
            deleteIndex(configDirectory);

        List<Form> forms     = new ArrayList<Form>();
        List<Path> paths     = new ArrayList<Path>();
        List<Classe> classes = new ArrayList<Classe>();
        forms                = fillTestData(paths, classes);

        MDWebIndexer indexer = new MDWebIndexer(forms, classes, paths, configDirectory);
        indexSearcher        = new MDWebIndexSearcher(configDirectory, "");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        configDirectory = new File("config-test");
        if (configDirectory.exists()) {
            File indexDirectory = new File(configDirectory, "index");
            if (indexDirectory.exists()) {
                for (File f : indexDirectory.listFiles()) {
                    f.delete();
                }
                indexDirectory.delete();
            }
            configDirectory.delete();
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    public static void deleteIndex(File configDir) {
        if (configDir.exists()) {
            File indexDirectory = new File(configDir, "index");
            if (indexDirectory.exists()) {
                for (File f : indexDirectory.listFiles()) {
                    f.delete();
                }
                indexDirectory.delete();
            }
        }

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
        
        logger.info("SimpleSearch 1:" + '\n' + resultReport);
        
        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("1:catalogTest");
        
        assertEquals(expectedResult, result);
        
        /**
         * Test 2 simple search: title like tit*
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("Title:tit*", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        logger.info("SimpleSearch 2:" + '\n' + resultReport);
        
        expectedResult = new ArrayList<String>();
        expectedResult.add("1:catalogTest");
        expectedResult.add("2:catalogTest");
        expectedResult.add("3:catalogTest");
        expectedResult.add("4:catalogTest");
        
        assertEquals(expectedResult, result);
        
         /**
         * Test 3 simple search: indentifier != 2345-aa453-ade456
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc NOT identifier:\"2345-aa453-ade456\"", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        logger.info("SimpleSearch 3:" + '\n' + resultReport);
        
        expectedResult = new ArrayList<String>();
        expectedResult.add("2:catalogTest");
        expectedResult.add("3:catalogTest");
        expectedResult.add("4:catalogTest");
        
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
        spatialQuery.setSort(new Sort("type_sort", false));
        
        List<String> result = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        logger.info("SortedSearch 1:" + '\n' + resultReport);
        
        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("3:catalogTest");
        expectedResult.add("2:catalogTest");
        expectedResult.add("1:catalogTest");
        expectedResult.add("4:catalogTest");
        
        assertEquals(expectedResult, result);
        
        /**
         * Test 2 sorted search: all orderBy type DSC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        spatialQuery.setSort(new Sort("type_sort", true));
        
        result = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        logger.info("SortedSearch 2:" + '\n' + resultReport);
        
        expectedResult = new ArrayList<String>();
        expectedResult.add("4:catalogTest");
        expectedResult.add("1:catalogTest");
        expectedResult.add("2:catalogTest");
        expectedResult.add("3:catalogTest");
        
        assertEquals(expectedResult, result);
        
        /**
         * Test 3 sorted search: all orderBy identifier ASC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        spatialQuery.setSort(new Sort("identifier_sort", false));
        
        result = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        logger.info("SortedSearch 3:" + '\n' + resultReport);
        
        expectedResult = new ArrayList<String>();
        expectedResult.add("2:catalogTest");
        expectedResult.add("3:catalogTest");
        expectedResult.add("1:catalogTest");
        expectedResult.add("4:catalogTest");
        
        assertEquals(expectedResult, result);
        
        /**
         * Test 4 sorted search: all orderBy identifier DSC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        spatialQuery.setSort(new Sort("identifier_sort", true));
        
        result = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        logger.info("SortedSearch 4:" + '\n' + resultReport);
        
        expectedResult = new ArrayList<String>();
        expectedResult.add("4:catalogTest");
        expectedResult.add("1:catalogTest");
        expectedResult.add("3:catalogTest");
        expectedResult.add("2:catalogTest");
        
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
        double min1[] = {-20, -20};
        double max1[] = { 20,  20};
        GeneralEnvelope bbox = new GeneralEnvelope(min1, max1);
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326", true);
        bbox.setCoordinateReferenceSystem(crs);
        SpatialFilter sf          = new BBOXFilter(bbox, "EPSG:4326");
        SpatialQuery spatialQuery = new SpatialQuery("metafile:doc", sf, SerialChainFilter.AND);
        
        List<String> result = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        logger.info("spatialSearch 1:" + '\n' + resultReport);
        
        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("2:catalogTest");
        expectedResult.add("3:catalogTest");
        expectedResult.add("4:catalogTest");
        
        assertEquals(expectedResult, result);
        
        /**
         * Test 1 spatial search: NOT BBOX filter
         */
        resultReport = "";
        List<Filter> lf = new ArrayList<Filter>();
        sf           = new BBOXFilter(bbox, "urn:x-ogc:def:crs:EPSG:6.11:4326");
        lf.add(sf);
        int[] op = {SerialChainFilter.NOT};
        SerialChainFilter f = new SerialChainFilter(lf, op);
        spatialQuery = new SpatialQuery("metafile:doc", f, SerialChainFilter.AND);
        
        result = indexSearcher.doSearch(spatialQuery);
        
        for (String s: result)
            resultReport = resultReport + s + '\n';
        
        logger.info("spatialSearch 2:" + '\n' + resultReport);
        
        expectedResult = new ArrayList<String>();
        expectedResult.add("1:catalogTest");
        
        assertEquals(expectedResult, result);
    }

    
    public static List<Form> fillTestData(List<Path> paths, List<Classe> classes) {
        List<Form> result       = new ArrayList<Form>();
        
        //we create a new Catalog
        Catalog cat             = new Catalog("catalogTest", "catalogTest");
        
        //then we build the classes
        Classe sLiteralClass    = new Classe(Standard.DUBLINCORE, "SimpleLiteral", "sl", "no definition", null, false, ' ');
        Property contentProp    = new Property(Standard.DUBLINCORE, "content", "ct", "no definition", sLiteralClass, PrimitiveType.STRING, 0, 1, Obligation.OPTIONNAL, 1, 'V');
        Property schemeProp     = new Property(Standard.DUBLINCORE, "scheme", "sh", "no definition", sLiteralClass, PrimitiveType.STRING, 0, 1, Obligation.OPTIONNAL, 1, 'P');
        
        Classe boundingBoxClass = new Classe(Standard.OWS, "BoundingBox", "bbox", "no defintion", null, false, ' ');
        Property lowerProp      = new Property(Standard.OWS, "LowerCorner", "lc", "no definition", boundingBoxClass, PrimitiveType.REAL,0, 1, Obligation.OPTIONNAL, 1, ' ');
        Property upperProp      = new Property(Standard.OWS, "UpperCorner", "uc", "no definition", boundingBoxClass, PrimitiveType.REAL,0, 1, Obligation.OPTIONNAL, 1, ' ');
        
        Classe recordClass      = new Classe(Standard.CSW, "Record", "rec", "no definition", null, false, ' ');
        Property identifierProp = new Property(Standard.DUBLINCORE, "identifier", "id", "no definition", recordClass, sLiteralClass, 0, 1, Obligation.OPTIONNAL, 1, ' ');
        Property titleProp      = new Property(Standard.DUBLINCORE, "title", "ti", "no definition", recordClass, sLiteralClass, 0, 1, Obligation.OPTIONNAL, 2, ' ');
        Property typeProp       = new Property(Standard.DUBLINCORE, "type", "ty", "no definition", recordClass, sLiteralClass, 0, 1, Obligation.OPTIONNAL, 3, ' ');
        Property bboxProp       = new Property(Standard.OWS, "BoundingBox", "box", "no definition", recordClass, boundingBoxClass, 0, 1, Obligation.OPTIONNAL, 4, ' ');
        
        Classe identifiable     = new Classe(Standard.EBRIM_V3, "Identifiable", "id", "no definition", null, false, ' ');
        Classe registryObject   = new Classe(Standard.EBRIM_V2_5, "RegsitryObject", "ro", "no definition", null, false, ' ');
        classes.add(identifiable);
        classes.add(registryObject);
        
        //The paths
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
        
        paths.add(recordPath);
        paths.add(idenContentPath);
        paths.add(identifierPath);
        paths.add(titContentPath);
        paths.add(titlePath);
        paths.add(typePath);
        paths.add(typContentPath);
        paths.add(bboxPath);
        paths.add(lowerCornPath);
        paths.add(upperCornPath);
        
        //The forms
        Date d = new Date(120);
        Form f1 = new Form(1, cat, "title1", null, null, null, d);
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
        
        Form f2 = new Form(2, cat, "title2", null, null, null, d);
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
        
        Form f3 = new Form(3, cat, "title3", null, null, null, d);
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
        
        Form f4 = new Form(4, cat, "title4", null, null, null, d);
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
        
        result.add(f1);
        result.add(f2);
        result.add(f3);
        result.add(f4);
        
        return result;
    }
}
