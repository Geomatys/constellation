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
import org.geotoolkit.metadata.iso.identification.DefaultDataIdentification;
import org.geotoolkit.util.logging.Logging;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import org.constellation.util.Util;

// lucene dependencies
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

// geotoolkit dependencies
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.lucene.filter.LuceneOGCFilter;
import org.geotoolkit.lucene.filter.SerialChainFilter;
import org.geotoolkit.lucene.filter.SpatialQuery;
import org.geotoolkit.lucene.index.AbstractIndexSearcher;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.metadata.iso.citation.DefaultCitation;
import org.geotoolkit.metadata.iso.citation.DefaultCitationDate;
import org.geotoolkit.util.FileUtilities;

// GeoAPI dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;

//Junit dependencies
import org.junit.*;
import org.opengis.filter.FilterFactory2;
import org.opengis.metadata.citation.DateType;
import static org.junit.Assert.*;

/**
 * Test class for constellation lucene index
 *
 * @author Guilhem Legal (Geomatys)
 */
public class GenericindexTest {

    protected static final FilterFactory2 FF = (FilterFactory2)
            FactoryFinder.getFilterFactory(new Hints(Hints.FILTER_FACTORY,FilterFactory2.class));


    private static final Logger logger = Logging.getLogger("org.constellation.metadata");

    private static AbstractIndexSearcher indexSearcher;

    private static GenericIndexer indexer;
    
    private static final File configDirectory  = new File("GenericIndexTest");

    @BeforeClass
    public static void setUpClass() throws Exception {
        
        FileUtilities.deleteDirectory(configDirectory);
        List<Object> object       = fillTestData();
        indexer                    = new GenericIndexer(object, null, configDirectory, "");
        indexSearcher               = new AbstractIndexSearcher(configDirectory, "");
        //indexer.setLogLevel(Level.FINER);
        //indexSearcher.setLogLevel(Level.FINER);
        
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FileUtilities.deleteDirectory(configDirectory);
        indexer.destroy();
        indexSearcher.destroy();
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
        SpatialQuery spatialQuery = new SpatialQuery("Title:\"90008411.ctd\"", nullFilter, SerialChainFilter.AND);
        List<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "SimpleSearch 1:\n{0}", resultReport);

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");

        assertEquals(expectedResult, result);

         /**
         * Test 2 simple search: identifier != 40510_145_19930221211500
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("metafile:doc NOT identifier:\"40510_145_19930221211500\"", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "SimpleSearch 2:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("CTDF02");
        expectedResult.add("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");
        expectedResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        

        assertEquals(expectedResult, result);

        /**
         * Test 3 simple search: originator = UNIVERSITE DE LA MEDITERRANNEE (U2) / COM - LAB. OCEANOG. BIOGEOCHIMIE - LUMINY
         */
        spatialQuery = new SpatialQuery("abstract:\"Donnees CTD NEDIPROD VI 120\"", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "simpleSearch 3:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");

        assertEquals(expectedResult, result);
        
        /**
         * Test 4 simple search: Title = 92005711.ctd
         */
        spatialQuery = new SpatialQuery("Title:\"92005711.ctd\"", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "SimpleSearch 4:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("40510_145_19930221211500");
        

        assertEquals(expectedResult, result);
        
        /**
         * Test 5 simple search: creator = IFREMER / IDM/SISMER
         */
        spatialQuery = new SpatialQuery("creator:\"IFREMER / IDM/SISMER\"", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "SimpleSearch 5:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("40510_145_19930221211500");
        

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

        logger.log(Level.FINER, "wildCharSearch 1:\n{0}", resultReport);

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");

        assertEquals(expectedResult, result);

        /**
         * Test 2 wildChar search: originator LIKE *UNIVER....
         */                              
        spatialQuery = new SpatialQuery("abstract:*NEDIPROD*", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "wildCharSearch 2:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");


        assertEquals(expectedResult, result);

        /**
         * Test 3 wildChar search: Title like *.ctd
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("Title:*.ctd", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "wilCharSearch 3:\n{0}", resultReport);

        assertTrue(result.contains("39727_22_19750113062500"));
        assertTrue(result.contains("40510_145_19930221211500"));
        assertTrue(result.contains("42292_5p_19900609195600"));
        assertTrue(result.contains("42292_9s_19900610041000"));
        
        assertEquals(4, result.size());
        
        /**
         * Test 4 wildChar search: title like *.ctd
         */
        resultReport = "";
        spatialQuery = new SpatialQuery("title:*.ctd", nullFilter, SerialChainFilter.AND);
        result       = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "wilCharSearch 4:\n{0}", resultReport);

        assertTrue(result.contains("39727_22_19750113062500"));
        assertTrue(result.contains("40510_145_19930221211500"));
        assertTrue(result.contains("42292_5p_19900609195600"));
        assertTrue(result.contains("42292_9s_19900610041000"));

        assertEquals(4, result.size());
        
         /**
         * Test 5 wildCharSearch: abstract LIKE *onnees CTD NEDIPROD VI 120
         */
        spatialQuery = new SpatialQuery("abstract:(*onnees CTD NEDIPROD VI 120)", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        resultReport = "";
        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "wildCharSearch 5:\n{0}", resultReport);

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
        SpatialQuery spatialQuery = new SpatialQuery("date:{\"20090125\" 30000101}", nullFilter, SerialChainFilter.AND);
        List<String> result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "DateSearch 1:\n{0}", resultReport);

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("CTDF02");
        
        assertEquals(expectedResult, result);
        
        /**
         * Test 2 date search: TempExtent_begin before 01/01/1985
         */
        spatialQuery = new SpatialQuery("TempExtent_begin:{00000101 \"19850101\"}", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "DateSearch 2:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("CTDF02");
        
        assertEquals(expectedResult, result);

        /**
         * Test 3 date search: TempExtent_end after 01/01/1991
         */
        spatialQuery = new SpatialQuery("TempExtent_end:{\"19910101\" 30000101}", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "DateSearch 3:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("CTDF02");

        assertEquals(expectedResult, result);

        /**
         * Test 4 date search: date = 26/01/2009
         */
        spatialQuery = new SpatialQuery("date:\"20090126\"", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "DateSearch 4:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("11325_158_19640418141800");

        assertEquals(expectedResult, result);

        /**
         * Test 5 date search: date LIKE 26/01/200*
         */
        spatialQuery = new SpatialQuery("date:(200*0126)", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "DateSearch 4:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("40510_145_19930221211500");

        assertEquals(expectedResult, result);

        /**
         * Test 6 date search: CreationDate between 01/01/1800 and 01/01/2000
         */
        spatialQuery = new SpatialQuery("CreationDate:[18000101  30000101]CreationDate:[00000101 20000101]", nullFilter, SerialChainFilter.AND);
        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "DateSearch 6:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");

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

        logger.log(Level.FINER, "SortedSearch 1:\n{0}", resultReport);

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("CTDF02");
        expectedResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        expectedResult.add("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");
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

        logger.log(Level.FINER, "SortedSearch 2:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");
        expectedResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        expectedResult.add("CTDF02");
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
        sf = new SortField("Abstract_sort", SortField.STRING, false);
        spatialQuery.setSort(new Sort(sf));

        result = indexSearcher.doSearch(spatialQuery);

        for (String s: result)
            resultReport = resultReport + s + '\n';

        logger.log(Level.FINER, "SortedSearch 3:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");
        expectedResult.add("CTDF02");
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");

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

        logger.log(Level.FINER, "SortedSearch 4:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("CTDF02");
        expectedResult.add("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");
        
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

        logger.log(Level.FINER, "spatialSearch 1:\n{0}", resultReport);

        List<String> expectedResult = new ArrayList<String>();
        expectedResult.add("39727_22_19750113062500");
        expectedResult.add("11325_158_19640418141800");
        expectedResult.add("CTDF02");

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

        logger.log(Level.FINER, "spatialSearch 2:\n{0}", resultReport);

        expectedResult = new ArrayList<String>();
        expectedResult.add("42292_5p_19900609195600");
        expectedResult.add("42292_9s_19900610041000");
        expectedResult.add("40510_145_19930221211500");
        expectedResult.add("urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd");
        expectedResult.add("gov.noaa.nodc.ncddc. MODXXYYYYJJJ.L3_Mosaic_NOAA_GMX or MODXXYYYYJJJHHMMSS.L3_NOAA_GMX");

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

        logger.log(Level.FINER, "identifier query 1:\n{0}", result);

        String expectedResult = "39727_22_19750113062500";

        assertEquals(expectedResult, result);

        /**
         * Test 2
         */

        identifier = "CTDF02";
        result = indexSearcher.identifierQuery(identifier);

        logger.log(Level.FINER, "identifier query 2:\n{0}", result);

        expectedResult = "CTDF02";

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
        indexer.removeDocument("CTDF02");

        indexSearcher.refresh();

        /**
         * Test 1
         */

        String identifier = "39727_22_19750113062500";
        String result = indexSearcher.identifierQuery(identifier);

        logger.log(Level.FINER, "identifier query 1:\n{0}", result);

        String expectedResult = "39727_22_19750113062500";

        assertEquals(expectedResult, result);

        /**
         * Test 2
         */

        identifier = "CTDF02";
        result = indexSearcher.identifierQuery(identifier);

        logger.log(Level.FINER, "identifier query 2:\n{0}", result);

        expectedResult = null;

        assertEquals(expectedResult, result);
    }
    
    
    @Test
    public void extractValuesTest() throws Exception {
        DefaultMetadata meta = new DefaultMetadata();
        DefaultDataIdentification ident = new DefaultDataIdentification();
        DefaultCitation citation = new DefaultCitation();
        Date d = new Date(0);
        DefaultCitationDate date = new DefaultCitationDate(d, DateType.CREATION);
        citation.setDates(Arrays.asList(date));
        ident.setCitation(citation);
        meta.setIdentificationInfo(Arrays.asList(ident));
        String result = GenericIndexer.extractValues(meta, Arrays.asList("ISO 19115:MD_Metadata:identificationInfo:citation:date#dateType=creation:date"));
        assertEquals("19700101", result);
        
        DefaultMetadata meta2 = new DefaultMetadata();
        DefaultDataIdentification ident2 = new DefaultDataIdentification();
        DefaultCitation citation2 = new DefaultCitation();
        Date d2 = new Date(0);
        DefaultCitationDate date2 = new DefaultCitationDate(d2, DateType.REVISION);
        citation2.setDates(Arrays.asList(date2));
        ident2.setCitation(citation2);
        meta2.setIdentificationInfo(Arrays.asList(ident2));
        result = GenericIndexer.extractValues(meta2, Arrays.asList("ISO 19115:MD_Metadata:identificationInfo:citation:date#dateType=creation:date"));
        assertEquals("null", result);
        
        Unmarshaller unmarshaller    = CSWMarshallerPool.getInstance().acquireUnmarshaller();
        DefaultMetadata meta3 = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"));
        CSWMarshallerPool.getInstance().release(unmarshaller);
        
        List<String> paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:beginPosition");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:position");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:temporalElement:extent:beginPosition");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:temporalElement:extent:position");
        result = GenericIndexer.extractValues(meta3, paths);
        
        assertEquals("19900605", result);
        
        
        paths = new ArrayList<String>();
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:endPosition");
        paths.add("ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:position");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:temporalElement:extent:endPosition");
        paths.add("ISO 19115-2:MI_Metadata:identificationInfo:extent:temporalElement:extent:position");
        result = GenericIndexer.extractValues(meta3, paths);
        
        assertEquals("19900702", result);
        
    }

    public static List<Object> fillTestData() throws JAXBException {
        List<Object> result = new ArrayList<Object>();
        Unmarshaller unmarshaller    = CSWMarshallerPool.getInstance().acquireUnmarshaller();

        Object obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"));
        result.add(obj);

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta2.xml"));
        result.add(obj);

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta3.xml"));
        result.add(obj);

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta4.xml"));
        result.add(obj);

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta5.xml"));
        result.add(obj);

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta6.xml"));
        result.add(obj);

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta8.xml"));
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement)obj).getValue();
        }
        result.add(obj);

        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/imageMetadata.xml"));
        result.add(obj);

        CSWMarshallerPool.getInstance().release(unmarshaller);
        
        return result;
    }
}

