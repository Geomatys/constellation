/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
package org.constellation.metadata.utils;

import java.util.Arrays;
import org.geotoolkit.util.DefaultInternationalString;
import org.geotoolkit.metadata.iso.citation.DefaultCitation;
import org.geotoolkit.metadata.iso.identification.DefaultDataIdentification;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.ows.xml.v100.BoundingBoxType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.ebrim.xml.v300.RegistryObjectType;
import org.geotoolkit.ebrim.xml.v300.LocalizedStringType;
import org.geotoolkit.ebrim.xml.v300.InternationalStringType;
import org.geotoolkit.feature.catalog.FeatureCatalogueImpl;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author guilhem
 */
public class UtilsTest {

    @BeforeClass
    public static void setUpClass() throws Exception {


    }

    @AfterClass
    public static void tearDownClass() throws Exception {

    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void FindIdentifierTestDC() throws Exception {

        /*
         * DublinCore Record v202 with identifier
         */
        RecordType record = new RecordType();
        record.setIdentifier(new SimpleLiteral("42292_5p_19900609195600"));
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        String expResult = "42292_5p_19900609195600";
        String result = Utils.findIdentifier(record);

        assertEquals(expResult, result);

        /*
         * DublinCore Record v202 without identifier
         */
        record = new RecordType();
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        expResult = "unknow_identifier";
        result = Utils.findIdentifier(record);

        assertEquals(expResult, result);

        /*
         * DublinCore Record v202 with title
         */
        record = new RecordType();
        record.setIdentifier(new SimpleLiteral("42292_5p_19900609195600"));
        record.setTitle(new SimpleLiteral("title1"));
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        expResult = "42292_5p_19900609195600";
        result = Utils.findIdentifier(record);

        assertEquals(expResult, result);
        

        /*
         * DublinCore Record v200 with identifer
         */
        org.geotoolkit.csw.xml.v200.RecordType record200 = new org.geotoolkit.csw.xml.v200.RecordType();
        record200.setIdentifier(new org.geotoolkit.dublincore.xml.v1.elements.SimpleLiteral("42292_5poi_19900609195600"));
        record200.setModified(new org.geotoolkit.dublincore.xml.v1.elements.SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record200.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        expResult = "42292_5poi_19900609195600";
        result = Utils.findIdentifier(record200);

        assertEquals(expResult, result);


        /*
         * DublinCore Record v200 without identifer
         */
        record200 = new org.geotoolkit.csw.xml.v200.RecordType();
        record200.setModified(new org.geotoolkit.dublincore.xml.v1.elements.SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record200.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        expResult = "unknow_identifier";
        result = Utils.findIdentifier(record200);

        assertEquals(expResult, result);


        
    }

    @Test
    public void FindIdentifierTestISO() throws Exception {
        /*
         * ISO 19139 Metadata
         */
        DefaultMetadata metadata = new DefaultMetadata();
        metadata.setFileIdentifier("ident1");
        DefaultDataIdentification identification = new DefaultDataIdentification();
        DefaultCitation citation = new DefaultCitation();
        citation.setTitle(new DefaultInternationalString("titleMeta"));
        identification.setCitation(citation);
        metadata.setIdentificationInfo(Arrays.asList(identification));

        String expResult = "ident1";
        String result = Utils.findIdentifier(metadata);
        assertEquals(expResult, result);

        /*
         * ISO 19139 Metadata with no identifier
         */
        metadata = new DefaultMetadata();
        identification = new DefaultDataIdentification();
        citation = new DefaultCitation();
        citation.setTitle(new DefaultInternationalString("titleMeta"));
        identification.setCitation(citation);
        metadata.setIdentificationInfo(Arrays.asList(identification));

        expResult = "unknow_identifier";
        result = Utils.findIdentifier(metadata);
        assertEquals(expResult, result);
    }

    
    @Test
    public void FindIdentifierTestISO19110() throws Exception {
        FeatureCatalogueImpl catalogue = new FeatureCatalogueImpl();
        catalogue.setId("someid");

        String expResult = "someid";
        String result = Utils.findIdentifier(catalogue);
        assertEquals(expResult, result);


        /*
         * Catalougue with no id
         */
        catalogue = new FeatureCatalogueImpl();

        expResult =  "unknow_identifier";
        result = Utils.findIdentifier(catalogue);
        assertEquals(expResult, result);

    }


    @Test
    public void FindIdentifierTestEbrim() throws Exception {
        /*
         * Ebrim v 3.0
         */
        RegistryObjectType reg = new RegistryObjectType();
        reg.setId("ebrimid-1");

        String expResult = "ebrimid-1";
        String result = Utils.findIdentifier(reg);

        assertEquals(expResult, result);


         /*
         * Ebrim v 2.5
         */
        org.geotoolkit.ebrim.xml.v250.RegistryObjectType reg25 = new org.geotoolkit.ebrim.xml.v250.RegistryObjectType();
        reg25.setId("ebrimid-2");

        expResult = "ebrimid-2";
        result = Utils.findIdentifier(reg25);

        assertEquals(expResult, result);
    }

    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void FindTitleTest() throws Exception {

        RecordType record = new RecordType();
        record.setIdentifier(new SimpleLiteral("42292_5p_19900609195600"));
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        String expResult = "42292_5p_19900609195600";
        String result = Utils.findTitle(record);

        assertEquals(expResult, result);

        record = new RecordType();
        record.setIdentifier(new SimpleLiteral("42292_5p_19900609195600"));
        record.setTitle(new SimpleLiteral("title1"));
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        expResult = "title1";
        result = Utils.findTitle(record);

        assertEquals(expResult, result);

        DefaultMetadata metadata = new DefaultMetadata();
        DefaultDataIdentification identification = new DefaultDataIdentification();
        DefaultCitation citation = new DefaultCitation();
        citation.setTitle(new DefaultInternationalString("titleMeta"));
        identification.setCitation(citation);
        metadata.setIdentificationInfo(Arrays.asList(identification));

        expResult = "titleMeta";
        result = Utils.findTitle(metadata);

        assertEquals(expResult, result);
    }

    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void FindTitleTestDC() throws Exception {

        /*
         * DublinCore Record v202 with identifier and no title
         */
        RecordType record = new RecordType();
        record.setIdentifier(new SimpleLiteral("42292_5p_19900609195600"));
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));

        String expResult = "42292_5p_19900609195600";
        String result = Utils.findTitle(record);

        assertEquals(expResult, result);

        /*
         * DublinCore Record v202 with title
         */
        record = new RecordType();
        record.setIdentifier(new SimpleLiteral("42292_5p_19900609195600"));
        record.setTitle(new SimpleLiteral("title1"));
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));

        expResult = "title1";
        result = Utils.findTitle(record);

        assertEquals(expResult, result);

        /*
         * DublinCore Record v202 with no title, no identifier
         */
        record = new RecordType();
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));

        expResult = "unknow title";
        result = Utils.findTitle(record);

        assertEquals(expResult, result);

        /*
         * DublinCore Record v200 with identifer and no title
         */
        org.geotoolkit.csw.xml.v200.RecordType record200 = new org.geotoolkit.csw.xml.v200.RecordType();
        record200.setIdentifier(new org.geotoolkit.dublincore.xml.v1.elements.SimpleLiteral("42292_5poi_19900609195600"));
        record200.setModified(new org.geotoolkit.dublincore.xml.v1.elements.SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record200.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        expResult = "42292_5poi_19900609195600";
        result = Utils.findTitle(record200);

        assertEquals(expResult, result);


        /*
         * DublinCore Record v200 with title
         */
        record200 = new org.geotoolkit.csw.xml.v200.RecordType();
        record200.setIdentifier(new  org.geotoolkit.dublincore.xml.v1.elements.SimpleLiteral("42292_5p_19900609195600"));
        record200.setTitle(new  org.geotoolkit.dublincore.xml.v1.elements.SimpleLiteral("title200"));
        record200.setModified(new org.geotoolkit.dublincore.xml.v1.elements.SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record200.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        expResult = "title200";
        result = Utils.findTitle(record200);

        assertEquals(expResult, result);

        /*
         * DublinCore Record v200 with no title no identifier
         */
        record200 = new org.geotoolkit.csw.xml.v200.RecordType();
        record200.setModified(new org.geotoolkit.dublincore.xml.v1.elements.SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record200.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        expResult = "unknow title";
        result = Utils.findTitle(record200);

        assertEquals(expResult, result);

    }

    @Test
    public void FindTitleTestISO() throws Exception {
        DefaultMetadata metadata = new DefaultMetadata();
        DefaultDataIdentification identification = new DefaultDataIdentification();
        DefaultCitation citation = new DefaultCitation();
        citation.setTitle(new DefaultInternationalString("titleMeta"));
        identification.setCitation(citation);
        metadata.setIdentificationInfo(Arrays.asList(identification));

        String expResult = "titleMeta";
        String result = Utils.findTitle(metadata);

        assertEquals(expResult, result);

        /**
         * metadate with no title
         */
        metadata = new DefaultMetadata();
        identification = new DefaultDataIdentification();
        citation = new DefaultCitation();
        identification.setCitation(citation);
        metadata.setIdentificationInfo(Arrays.asList(identification));

        expResult = "unknow title";
        result = Utils.findTitle(metadata);

        assertEquals(expResult, result);
    }

    @Test
    public void FindTitleTestEbrim() throws Exception {
        /*
         * Ebrim v 3.0
         */
        RegistryObjectType reg = new RegistryObjectType();
        reg.setId("ebrimid-1");
        InternationalStringType it = new InternationalStringType();
        LocalizedStringType lo = new LocalizedStringType();
        lo.setValue("title1");
        it.setLocalizedString(lo);
        reg.setName(it);

        String expResult = "title1";
        String result = Utils.findTitle(reg);

        assertEquals(expResult, result);

        /*
         * Ebrim v 3.0 no title
         */
        reg = new RegistryObjectType();
        reg.setId("ebrimid-1");


        expResult = "ebrimid-1";
        result = Utils.findTitle(reg);

        assertEquals(expResult, result);

        /*
         * Ebrim v 3.0 nothing
         */
        reg = new RegistryObjectType();


        expResult = "unknow title";
        result = Utils.findTitle(reg);

        assertEquals(expResult, result);


         /*
         * Ebrim v 2.5
         */
        org.geotoolkit.ebrim.xml.v250.RegistryObjectType reg25     = new org.geotoolkit.ebrim.xml.v250.RegistryObjectType();
        org.geotoolkit.ebrim.xml.v250.InternationalStringType it25 = new org.geotoolkit.ebrim.xml.v250.InternationalStringType();
        org.geotoolkit.ebrim.xml.v250.LocalizedStringType lo25     = new org.geotoolkit.ebrim.xml.v250.LocalizedStringType();
        lo25.setValue("title25");
        it25.setLocalizedString(lo25);
        reg25.setName(it25);

        reg25.setId("ebrimid-2");

        expResult = "title25";
        result = Utils.findTitle(reg25);

        assertEquals(expResult, result);

         /*
         * Ebrim v 2.5 no title
         */
        reg25 = new org.geotoolkit.ebrim.xml.v250.RegistryObjectType();
        reg25.setId("ebrimid-2");

        expResult = "ebrimid-2";
        result = Utils.findTitle(reg25);

        assertEquals(expResult, result);

         /*
         * Ebrim v 2.5 nothign
         */
        reg25 = new org.geotoolkit.ebrim.xml.v250.RegistryObjectType();

        expResult = "unknow title";
        result = Utils.findTitle(reg25);

        assertEquals(expResult, result);
    }

}
