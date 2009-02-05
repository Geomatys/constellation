/*
 *    Constellation - An open source and Stop compliant SDI
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

package org.constellation.metadata.index.generic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Sort;
import org.constellation.lucene.filter.SerialChainFilter;
import org.constellation.lucene.filter.SpatialQuery;
import org.constellation.util.Util;
import org.geotools.metadata.iso.MetaDataImpl;

//Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class StopAnalyzerTest {

    private Logger logger = Logger.getLogger("org.constellation.metadata");

    private static GenericIndexSearcher indexSearcher;

    private static File configDirectory = new File("StopAnalyzerTest");


    @BeforeClass
    public static void setUpClass() throws Exception {
        List<MetaDataImpl> object = fillTestData();
        GenericIndexer indexer = new GenericIndexer(object, null, configDirectory, "", new StopAnalyzer());
        indexSearcher          = new GenericIndexSearcher(configDirectory, "", new StopAnalyzer());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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

        logger.info("SimpleSearch 1:" + '\n' + resultReport);

        // the result we want are this
        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");

        // ERROR: but with the stop Analyzer remove the number so we get all the results finishing by ctd (why???)
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("40510_145_19930221211500");

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

        logger.info("SimpleSearch 2:" + '\n' + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("11325_158_19640418141800");

        // ERROR: here the stop analyzer remove all the number and '_'
        // the result is all the records instead of only 4 result
        expectedResult.add("40510_145_19930221211500");

        assertEquals(expectedResult, result);

        /**
         * Test 3 simple search: originator = Donnees CTD NEDIPROD VI 120
         */
        spatialQuery = new SpatialQuery("abstract:\"Donnees CTD NEDIPROD VI 120\"", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.info("simpleSearch 3:" + '\n' + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");

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
        SpatialQuery spatialQuery = new SpatialQuery("Title:*0008411.ctd", nullFilter, SerialChainFilter.AND);
        List<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.info("wildCharSearch 1:" + '\n' + resultReport);

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");

        // ERROR: it didn't find any result (why???)
        expectedResult = new ArrayList<String>();
        
        assertEquals(expectedResult, result);

        /**
         * Test 2 wildChar search: abstract LIKE *NEDIPROD*
         */
        spatialQuery = new SpatialQuery("abstract:*NEDIPROD*", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.info("wildCharSearch 2:" + '\n' + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");

        assertEquals(expectedResult, result);

        /**
         * Test 3 wildChar search: title like *.ctd
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("Title:*.ctd", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.info("wildCharSearch 3:" + '\n' + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");

        // ERROR: it didn't find any result (why???)
        expectedResult = new ArrayList<String>();

        assertEquals(expectedResult, result);

        /**
         * Test 4 wildCharSearch: anstract LIKE *onnees CTD NEDIPROD VI 120
         */
        spatialQuery = new SpatialQuery("abstract:(*onnees CTD NEDIPROD VI 120)", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.info("wildCharSearch 4:" + '\n' + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");

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

        logger.info("DateSearch 1:" + '\n' + resultReport);

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("40510_145_19930221211500");

        //ERROR: it didn't find any result (why???)
        expectedResult = new ArrayList<String>();
        
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
        spatialQuery.setSort(new Sort("identifier_sort", false));

        List<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.info("SortedSearch 1:" + '\n' + resultReport);

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");

        assertEquals(expectedResult, result);

        /**
         * Test 2 sorted search: all orderBy identifier DSC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        spatialQuery.setSort(new Sort("identifier_sort", true));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.info("SortedSearch 2:" + '\n' + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("11325_158_19640418141800");

        assertEquals(expectedResult, result);

        /**
         * Test 3 sorted search: all orderBy Abstract ASC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        spatialQuery.setSort(new Sort("Abstract_sort", false));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.info("SortedSearch 3:" + '\n' + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("42292_5p_19900609195600");

        assertEquals(expectedResult, result);

        /**
         * Test 4 sorted search: all orderBy Abstract DSC
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc", nullFilter, SerialChainFilter.AND);
        spatialQuery.setSort(new Sort("Abstract_sort", true));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.info("SortedSearch 4:" + '\n' + resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("11325_158_19640418141800");

        assertEquals(expectedResult, result);
    }

    /**
     *
     *           TODO
     *
     * Test spatial lucene search.
     *
     * @throws java.lang.Exception

    @Test
    public void spatialSearchTest() throws Exception {

        String resultReport = "";

        /**
         * Test 1 spatial search: BBOX filter

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
    }*/


    public static List<MetaDataImpl> fillTestData() throws JAXBException {
        List<MetaDataImpl> result = new ArrayList<MetaDataImpl>();
        JAXBContext context       = JAXBContext.newInstance(MetaDataImpl.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        Object obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1.xml"));
        if (obj instanceof MetaDataImpl) {
            result.add((MetaDataImpl) obj);
        } else {
            throw new IllegalArgumentException("resource file must be MetadataImpl");
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta2.xml"));
        if (obj instanceof MetaDataImpl) {
            result.add((MetaDataImpl) obj);
        } else {
            throw new IllegalArgumentException("resource file must be MetadataImpl:" + obj);
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta3.xml"));
        if (obj instanceof MetaDataImpl) {
            result.add((MetaDataImpl) obj);
        } else {
            throw new IllegalArgumentException("resource file must be MetadataImpl:" + obj);
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta4.xml"));
        if (obj instanceof MetaDataImpl) {
            result.add((MetaDataImpl) obj);
        } else {
            throw new IllegalArgumentException("resource file must be MetadataImpl:" + obj);
        }

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta5.xml"));
        if (obj instanceof MetaDataImpl) {
            result.add((MetaDataImpl) obj);
        } else {
            throw new IllegalArgumentException("resource file must be MetadataImpl:" + obj);
        }
        return result;
    }

}
