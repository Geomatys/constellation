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
package org.constellation.metadata.utils;

// J2SE dependencies
import org.apache.sis.internal.jaxb.gmi.MI_Metadata;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.citation.DefaultResponsibleParty;
import org.apache.sis.metadata.iso.identification.DefaultDataIdentification;
import org.apache.sis.util.iso.DefaultInternationalString;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.geotoolkit.sml.xml.v100.ComponentType;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.constellation.util.Util;

// geotoolkit dependencies
import org.geotoolkit.ows.xml.v100.BoundingBoxType;
import org.geotoolkit.dublincore.xml.v2.elements.SimpleLiteral;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.ebrim.xml.v300.RegistryObjectType;
import org.geotoolkit.ebrim.xml.v300.LocalizedStringType;
import org.geotoolkit.ebrim.xml.v300.InternationalStringType;
import org.geotoolkit.sml.xml.v100.Member;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.sml.xml.v100.SystemType;
import org.geotoolkit.feature.catalog.FeatureCatalogueImpl;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Test the utilities class org.constellation.metadata.utils.utils from the storage-metadata.
 * 
 * This class is tested here because it use reflection and the storage metadata has no dependencies with the tested object.
 *
 * @author Guilhem Legal (Geomatys)
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
    public void findIdentifierDCTest() throws Exception {

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
    public void findIdentifierISO19115Test() throws Exception {
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
        
        /*
         * Responsible party
         */
        DefaultResponsibleParty party = new DefaultResponsibleParty();
        party.setOrganisationName(new SimpleInternationalString("partyIdent"));
        
        expResult = "partyIdent";
        result = Utils.findIdentifier(party);
        assertEquals(expResult, result);
        
        
        party = new DefaultResponsibleParty();
        party.setOrganisationName(new SimpleInternationalString("partyIdent"));
        party.setIndividualName("myName");
        
        expResult = "myName";
        result = Utils.findIdentifier(party);
        assertEquals(expResult, result);
    }

    
    @Test
    public void findIdentifierISO19110Test() throws Exception {
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
    public void findIdentifierEbrimTest() throws Exception {
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

    @Test
    public void findIdentifierSensorMLTest() throws Exception {
        /*
         * SensorML 1.0.0
         */
        SystemType system = new SystemType();
        system.setId("sml-id-1");
        Member member = new Member(system);
        SensorML sml = new SensorML("1.0", Arrays.asList(member));

        String expResult = "sml-id-1";
        String result = Utils.findIdentifier(sml);

        assertEquals(expResult, result);

        ComponentType component = new ComponentType();
        component.setId("sml-id-1-compo");
        Member memberC = new Member(component);
        SensorML smlC = new SensorML("1.0", Arrays.asList(memberC));

        expResult = "sml-id-1-compo";
        result = Utils.findIdentifier(smlC);

        assertEquals(expResult, result);


        /*
         * SensorML 1.0.1
         */
        org.geotoolkit.sml.xml.v101.SystemType system1 = new org.geotoolkit.sml.xml.v101.SystemType();
        system1.setId("sml-id-101");
        org.geotoolkit.sml.xml.v101.SensorML.Member member1 = new org.geotoolkit.sml.xml.v101.SensorML.Member(system1);
        org.geotoolkit.sml.xml.v101.SensorML sml1 = new org.geotoolkit.sml.xml.v101.SensorML("1.0.1", Arrays.asList(member1));

        expResult = "sml-id-101";
        result = Utils.findIdentifier(sml1);

        assertEquals(expResult, result);

        /*
         * SensorML 1.0.1
         */
        org.geotoolkit.sml.xml.v101.ComponentType component1 = new org.geotoolkit.sml.xml.v101.ComponentType();
        component1.setId("sml-id-101-compo");
        org.geotoolkit.sml.xml.v101.SensorML.Member memberC1 = new org.geotoolkit.sml.xml.v101.SensorML.Member(component1);
        org.geotoolkit.sml.xml.v101.SensorML smlC1 = new org.geotoolkit.sml.xml.v101.SensorML("1.0.1", Arrays.asList(memberC1));

        expResult = "sml-id-101-compo";
        result = Utils.findIdentifier(smlC1);

        assertEquals(expResult, result);
    }


    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void findTitleTest() throws Exception {

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
        
        MI_Metadata metadataMI = new MI_Metadata();
        DefaultDataIdentification identificationMI = new DefaultDataIdentification();
        DefaultCitation citationMI = new DefaultCitation();
        citationMI.setTitle(new DefaultInternationalString("titleMetaMI"));
        identificationMI.setCitation(citationMI);
        metadataMI.setIdentificationInfo(Arrays.asList(identificationMI));

        expResult = "titleMetaMI";
        result = Utils.findTitle(metadataMI);

        assertEquals(expResult, result);
    }

    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void findTitleTestDC() throws Exception {

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
    public void findTitleTestISO() throws Exception {
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
    public void findTitleTestEbrim() throws Exception {
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
         * Ebrim v 2.5 nothing
         */
        reg25 = new org.geotoolkit.ebrim.xml.v250.RegistryObjectType();

        expResult = "unknow title";
        result = Utils.findTitle(reg25);

        assertEquals(expResult, result);
    }

    @Test
    public void setIdentifierTestDC() throws Exception {
        /*
         * DublinCore Record v202 with identifier (replace)
         */
        RecordType record = new RecordType();
        record.setIdentifier(new SimpleLiteral("42292_5p_19900609195600"));
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));

        Utils.setIdentifier("new-ident", record);

        assertEquals(1, record.getIdentifier().getContent().size());
        assertEquals("new-ident", record.getIdentifier().getContent().get(0));


        /*
         * DublinCore Record v202 without identifier (insert)
         */
        record = new RecordType();
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        Utils.setIdentifier("new-ident", record);

        assertEquals("new-ident", record.getIdentifier().getContent().get(0));
    }
    
    @Test
    public void setTitleTestDC() throws Exception {
        /*
         * DublinCore Record v202 with identifier (replace)
         */
        RecordType record = new RecordType();
        record.setTitle(new SimpleLiteral("42292_5p_19900609195600"));
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));

        Utils.setTitle("new-ident", record);

        assertEquals(1, record.getTitle().getContent().size());
        assertEquals("new-ident", record.getTitle().getContent().get(0));


        /*
         * DublinCore Record v202 without identifier (insert)
         */
        record = new RecordType();
        record.setModified(new SimpleLiteral("2009-01-01T06:00:00+01:00"));
        record.setBoundingBox(new BoundingBoxType("EPSG:4326", 1.1667, 36.6, 1.1667, 36.6));


        Utils.setTitle("new-ident", record);

        assertEquals("new-ident", record.getTitle().getContent().get(0));
    }

    @Test
    public void setIdentifierEbrimTest() throws Exception {
        /*
         * Ebrim v 3.0 with identifier (replace)
         */
        RegistryObjectType reg = new RegistryObjectType();
        reg.setId("ebrimid-1");

        Utils.setIdentifier("id1", reg);

        assertEquals("id1", reg.getId());

        /*
         * Ebrim v 3.0 with no identifier (insert)
         */
        reg = new RegistryObjectType();

        Utils.setIdentifier("id12", reg);

        assertEquals("id12", reg.getId());


        /*
         * Ebrim v 2.5 with identifier (replace)
         */
        org.geotoolkit.ebrim.xml.v250.RegistryObjectType reg25 = new org.geotoolkit.ebrim.xml.v250.RegistryObjectType();
        reg25.setId("ebrimid-2");

        Utils.setIdentifier("id2", reg25);

        assertEquals("id2", reg25.getId());

         /*
         * Ebrim v 2.5 with no identifier (insert)
         */
        reg25 = new org.geotoolkit.ebrim.xml.v250.RegistryObjectType();
        

        Utils.setIdentifier("id22", reg25);

        assertEquals("id22", reg25.getId());
    }
    
    @Test
    public void setTitleEbrimTest() throws Exception {
        /*
         * Ebrim v 3.0 with identifier (replace)
         */
        RegistryObjectType reg = new RegistryObjectType();
        reg.setId("ebrimid-1");

        Utils.setTitle("id1", reg);

        assertEquals("id1", reg.getName().getLocalizedString().get(0).getValue());

        /*
         * Ebrim v 3.0 with no identifier (insert)
         */
        reg = new RegistryObjectType();

        Utils.setTitle("id12", reg);

        assertEquals("id12", reg.getName().getLocalizedString().get(0).getValue());


        /*
         * Ebrim v 2.5 with identifier (replace)
         */
        org.geotoolkit.ebrim.xml.v250.RegistryObjectType reg25 = new org.geotoolkit.ebrim.xml.v250.RegistryObjectType();
        reg25.setId("ebrimid-2");

        Utils.setTitle("id2", reg25);

        assertEquals("id2", reg25.getName().getLocalizedString().get(0).getValue());

        /*
         * Ebrim v 2.5 with no identifier (insert)
         */
        reg25 = new org.geotoolkit.ebrim.xml.v250.RegistryObjectType();
        

        Utils.setTitle("id22", reg25);

        assertEquals("id22", reg25.getName().getLocalizedString().get(0).getValue());
    }

    @Test
    public void setIdentifierISO19115Test() throws Exception {
        /*
         * ISO 19139 Metadata with identifier (replace)
         */
        DefaultMetadata metadata = new DefaultMetadata();
        metadata.setFileIdentifier("ident1");
        
        Utils.setIdentifier("ident-2", metadata);
        assertEquals("ident-2", metadata.getFileIdentifier());

        /*
         * ISO 19139 Metadata with no identifier (insert)
         */
        metadata = new DefaultMetadata();

        Utils.setIdentifier("ident-3", metadata);
        assertEquals("ident-3", metadata.getFileIdentifier());
        
        /*
         * Responsible party
         */
        DefaultResponsibleParty party = new DefaultResponsibleParty();
        party.setOrganisationName(new SimpleInternationalString("partyIdent"));
        
        Utils.setIdentifier("party-ident-2", party);
        
        String expResult = "party-ident-2";
        String result = Utils.findIdentifier(party);
        assertEquals(expResult, result);

    }
    
    @Test
    public void setTitleISO19115Test() throws Exception {
        /*
         * ISO 19139 Metadata with title (replace)
         */
        DefaultMetadata metadata = new DefaultMetadata();
        DefaultDataIdentification identification = new DefaultDataIdentification();
        DefaultCitation citation = new DefaultCitation();
        citation.setTitle(new DefaultInternationalString("titleMeta"));
        identification.setCitation(citation);
        metadata.setIdentificationInfo(Arrays.asList(identification));
        
        Utils.setTitle("titleMeta-2", metadata);
        assertEquals("titleMeta-2", metadata.getIdentificationInfo().iterator().next().getCitation().getTitle().toString());

        /*
         * ISO 19139 Metadata with no title (insert)
         */
        metadata = new DefaultMetadata();

        Utils.setTitle("titleMeta-2", metadata);
        assertEquals("titleMeta-2", metadata.getIdentificationInfo().iterator().next().getCitation().getTitle().toString());
        
        /*
         * Responsible party with title (replace)
         */
        DefaultResponsibleParty party = new DefaultResponsibleParty();
        party.setOrganisationName(new SimpleInternationalString("partyIdent"));
        
        Utils.setTitle("party-ident-2", party);
        
        String expResult = "party-ident-2";
        assertEquals(expResult, party.getOrganisationName().toString());

        /*
         * Responsible party with title (replace)
         */
        DefaultResponsibleParty party2 = new DefaultResponsibleParty();
        
        Utils.setTitle("party-ident-3", party2);
        
        assertEquals("party-ident-3", party2.getOrganisationName().toString());
    }

    @Test
    public void setIdentifierISO19110Test() throws Exception {

        /*
         * ISO 19110 Metadata with identifier (replace)
         */
        FeatureCatalogueImpl catalogue = new FeatureCatalogueImpl();
        catalogue.setId("someid");

        Utils.setIdentifier("fcat", catalogue);
        assertEquals("fcat", catalogue.getId());

        /*
         * ISO 19110 Metadata with no id (insert)
         */
        catalogue = new FeatureCatalogueImpl();

        Utils.setIdentifier("fcat2", catalogue);
        assertEquals("fcat2", catalogue.getId());
    }
    
    @Test
    public void setTitleISO19110Test() throws Exception {

        /*
         * ISO 19110 Metadata with identifier (replace)
         */
        FeatureCatalogueImpl catalogue = new FeatureCatalogueImpl();
        catalogue.setName("somename");

        Utils.setTitle("fcat", catalogue);
        assertEquals("fcat", catalogue.getName());

        /*
         * ISO 19110 Metadata with no id (insert)
         */
        catalogue = new FeatureCatalogueImpl();

        Utils.setTitle("fcat2", catalogue);
        assertEquals("fcat2", catalogue.getName());
    }

    @Test
    public void setIdentifierSensorMLTest() throws Exception {
        /*
         * SensorML 1.0.0 with identifier (replace)
         */
        SystemType system = new SystemType();
        system.setId("sml-id-1");
        Member member = new Member(system);
        SensorML sml = new SensorML("1.0", Arrays.asList(member));

        Utils.setIdentifier("newid", sml);

        assertEquals("newid", sml.getMember().get(0).getProcess().getValue().getId());

        ComponentType component = new ComponentType();
        component.setId("sml-id-1-compo");
        Member memberC = new Member(component);
        SensorML smlC = new SensorML("1.0", Arrays.asList(memberC));

        Utils.setIdentifier("newidC", smlC);

        assertEquals("newidC", smlC.getMember().get(0).getProcess().getValue().getId());


        /*
         * SensorML 1.0.1 with identifier (replace)
         */
        org.geotoolkit.sml.xml.v101.SystemType system1 = new org.geotoolkit.sml.xml.v101.SystemType();
        system1.setId("sml-id-101");
        org.geotoolkit.sml.xml.v101.SensorML.Member member1 = new org.geotoolkit.sml.xml.v101.SensorML.Member(system1);
        org.geotoolkit.sml.xml.v101.SensorML sml1 = new org.geotoolkit.sml.xml.v101.SensorML("1.0.1", Arrays.asList(member1));

        Utils.setIdentifier("newid-101", sml1);

        assertEquals("newid-101", sml1.getMember().get(0).getProcess().getValue().getId());

        /*
         * SensorML 1.0.1
         */
        org.geotoolkit.sml.xml.v101.ComponentType component1 = new org.geotoolkit.sml.xml.v101.ComponentType();
        component1.setId("sml-id-101-compo");
        org.geotoolkit.sml.xml.v101.SensorML.Member memberC1 = new org.geotoolkit.sml.xml.v101.SensorML.Member(component1);
        org.geotoolkit.sml.xml.v101.SensorML smlC1 = new org.geotoolkit.sml.xml.v101.SensorML("1.0.1", Arrays.asList(memberC1));

        Utils.setIdentifier("newidC-101", smlC1);

        assertEquals("newidC-101", smlC1.getMember().get(0).getProcess().getValue().getId());
    }
    
    @Test
    public void setTitleSensorMLTest() throws Exception {
        /*
         * SensorML 1.0.0 with title (replace)
         */
        SystemType system = new SystemType();
        system.setId("sml-id-1");
        Member member = new Member(system);
        SensorML sml = new SensorML("1.0", Arrays.asList(member));

        Utils.setTitle("newid", sml);

        assertEquals("newid", sml.getMember().get(0).getProcess().getValue().getId());

        ComponentType component = new ComponentType();
        component.setId("sml-id-1-compo");
        Member memberC = new Member(component);
        SensorML smlC = new SensorML("1.0", Arrays.asList(memberC));

        Utils.setTitle("newidC", smlC);

        assertEquals("newidC", smlC.getMember().get(0).getProcess().getValue().getId());


        /*
         * SensorML 1.0.1 system with title (replace)
         */
        org.geotoolkit.sml.xml.v101.SystemType system1 = new org.geotoolkit.sml.xml.v101.SystemType();
        system1.setId("sml-id-101");
        org.geotoolkit.sml.xml.v101.SensorML.Member member1 = new org.geotoolkit.sml.xml.v101.SensorML.Member(system1);
        org.geotoolkit.sml.xml.v101.SensorML sml1 = new org.geotoolkit.sml.xml.v101.SensorML("1.0.1", Arrays.asList(member1));

        Utils.setTitle("newid-101", sml1);

        assertEquals("newid-101", sml1.getMember().get(0).getProcess().getValue().getId());

        /*
         * SensorML 1.0.1 component with title (replace)
         */
        org.geotoolkit.sml.xml.v101.ComponentType component1 = new org.geotoolkit.sml.xml.v101.ComponentType();
        component1.setId("sml-id-101-compo");
        org.geotoolkit.sml.xml.v101.SensorML.Member memberC1 = new org.geotoolkit.sml.xml.v101.SensorML.Member(component1);
        org.geotoolkit.sml.xml.v101.SensorML smlC1 = new org.geotoolkit.sml.xml.v101.SensorML("1.0.1", Arrays.asList(memberC1));

        Utils.setTitle("newidC-101", smlC1);

        assertEquals("newidC-101", smlC1.getMember().get(0).getProcess().getValue().getId());
    }

    @Test
    public void findIdentifierDCNodeTest() throws Exception {

        /*
         * DublinCore Record v202 with identifier
         */
        Node n = getOriginalMetadata("org/constellation/xml/metadata/meta8.xml");

        String expResult = "urn:uuid:1ef30a8b-876d-4828-9246-c37ab4510bbd";
        String result = Utils.findIdentifier(n);

        assertEquals(expResult, result);

        /*
         * DublinCore Record v202 without identifier
         */
        n = getOriginalMetadata("org/constellation/xml/metadata/dcNoIdent.xml");


        expResult = "unknow_identifier";
        result = Utils.findIdentifier(n);

        assertEquals(expResult, result);
    }

    @Test
    public void findIdentifierISO19115NodeTest() throws Exception {
        /*
         * ISO 19139 Metadata
         */
        Node n = getOriginalMetadata("org/constellation/xml/metadata/meta1.xml");

        String expResult = "42292_5p_19900609195600";
        String result = Utils.findIdentifier(n);
        assertEquals(expResult, result);

        /*
         * ISO 19139 Metadata with no identifier
         */
        n = getOriginalMetadata("org/constellation/xml/metadata/isoNoIdent.xml");

        expResult = "unknow_identifier";
        result = Utils.findIdentifier(n);
        assertEquals(expResult, result);

        /*
         * Responsible party
         */
        n = getOriginalMetadata("org/constellation/xml/metadata/contact1.xml");

        expResult = "IFREMER / IDM/SISMER";
        result = Utils.findIdentifier(n);
        assertEquals(expResult, result);


        n = getOriginalMetadata("org/constellation/xml/metadata/contact2.xml");

        expResult = "michel";
        result = Utils.findIdentifier(n);
        assertEquals(expResult, result);
    }


    @Test
    public void findIdentifierISO19110NodeTest() throws Exception {
        Node n = getOriginalMetadata("org/constellation/xml/metadata/featcatalog1.xml");

        String expResult = "cat-1";
        String result = Utils.findIdentifier(n);
        assertEquals(expResult, result);

    }


    @Test
    public void findIdentifierEbrimNodeTest() throws Exception {
        /*
         * Ebrim v 3.0
         */
        Node n = getOriginalMetadata("org/constellation/xml/metadata/ebrim3.xml");

        String expResult = "urn:motiive:csw-ebrim";
        String result = Utils.findIdentifier(n);

        assertEquals(expResult, result);


         /*
         * Ebrim v 2.5
         */
       n = getOriginalMetadata("org/constellation/xml/metadata/ebrim1.xml");

        expResult = "000068C3-3B49-C671-89CF-10A39BB1B652";
        result = Utils.findIdentifier(n);

        assertEquals(expResult, result);
    }

    @Test
    public void findIdentifierSensorMLNodeTest() throws Exception {
        /*
         * SensorML 1.0.0
         */
        Node n = getOriginalMetadata("org/constellation/xml/sml/system.xml");

        String expResult = "sensor-system";
        String result = Utils.findIdentifier(n);

        assertEquals(expResult, result);

        n = getOriginalMetadata("org/constellation/xml/sml/component2.xml");

        expResult = "component2";
        result = Utils.findIdentifier(n);

        assertEquals(expResult, result);

    }

    private Node getOriginalMetadata(final String fileName) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        Document document = docBuilder.parse(Util.getResourceAsStream(fileName));

        return document.getDocumentElement();
    }
}
