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

package org.constellation.util;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.citation.DefaultCitationDate;
import org.apache.sis.metadata.iso.citation.DefaultResponsibleParty;
import org.apache.sis.metadata.iso.extent.DefaultExtent;
import org.apache.sis.metadata.iso.extent.DefaultTemporalExtent;
import org.apache.sis.metadata.iso.identification.DefaultDataIdentification;
import org.apache.sis.metadata.iso.identification.DefaultKeywords;
import org.apache.sis.util.iso.DefaultInternationalString;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.geotoolkit.ebrim.xml.v250.ClassificationSchemeType;
import org.geotoolkit.ebrim.xml.v250.NotifyActionType;
import org.geotoolkit.ebrim.xml.v250.UserType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.extent.TemporalExtent;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.identification.KeywordType;
import org.opengis.util.InternationalString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// JUnit dependencies

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ReflectionUtilitiesTest {

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
    public void singleGetConditionalValuesFromPathTest() {

        DefaultMetadata metadata = new DefaultMetadata();
        DefaultDataIdentification identification = new DefaultDataIdentification();
        DefaultCitation citation = new DefaultCitation();
        List<CitationDate> dates = new ArrayList<CitationDate>();
        long randomDate1 = 1245587454;
        DefaultCitationDate date1 = new DefaultCitationDate(new Date(randomDate1), DateType.CREATION);
        long randomDate2 = 1253587454;
        DefaultCitationDate date2 = new DefaultCitationDate(new Date(randomDate2), DateType.PUBLICATION);
        long randomDate3 = 1266687454;
        DefaultCitationDate date3 = new DefaultCitationDate(new Date(randomDate3), DateType.REVISION);
        dates.add(date1);
        dates.add(date2);
        dates.add(date3);
        citation.setDates(dates);
        identification.setCitation(citation);
        List<ResponsibleParty> pocs = new ArrayList<ResponsibleParty>();
        DefaultResponsibleParty poc1 = new DefaultResponsibleParty(Role.ORIGINATOR);
        InternationalString orgName1 = new DefaultInternationalString("orgniz1");
        poc1.setOrganisationName(orgName1);
        pocs.add(poc1);
        DefaultResponsibleParty poc2 = new DefaultResponsibleParty(Role.PUBLISHER);
        InternationalString orgName2 = new DefaultInternationalString("orgniz2");
        poc2.setOrganisationName(orgName2);
        pocs.add(poc2);
        DefaultResponsibleParty poc3 = new DefaultResponsibleParty(Role.AUTHOR);
        InternationalString orgName3 = new DefaultInternationalString("orgniz3");
        poc3.setOrganisationName(orgName3);
        pocs.add(poc3);
        identification.setPointOfContacts(pocs);
        List<DefaultKeywords> keywords = new ArrayList<DefaultKeywords>();
        DefaultKeywords kw1 = new DefaultKeywords();
        InternationalString key1 = new SimpleInternationalString("keyword1");
        kw1.setKeywords(Arrays.asList(key1));
        kw1.setType(KeywordType.valueOf("VariablesCategory"));
        keywords.add(kw1);
        DefaultKeywords kw2 = new DefaultKeywords();
        InternationalString key2 = new SimpleInternationalString("keyword2");
        kw2.setKeywords(Arrays.asList(key2));
        kw2.setType(KeywordType.valueOf("StationType"));
        keywords.add(kw2);
        identification.setDescriptiveKeywords(keywords);

        final List<TemporalExtent> tempExtents = new ArrayList<TemporalExtent>();
        final Date start = new Date(1547845121);
        final Date stop  = new Date(1747845121);
        final TimePositionType startPos = new TimePositionType(start);
        final TimePositionType stopPos = new TimePositionType(stop);
        TimePeriodType allPeriod = new TimePeriodType(startPos, stopPos);
        allPeriod.setId("1-all");

        final DefaultExtent extent = new DefaultExtent();
        final DefaultTemporalExtent stationTempExtent = new DefaultTemporalExtent();
        stationTempExtent.setExtent(allPeriod);
        tempExtents.add(stationTempExtent);
        extent.setTemporalElements(tempExtents);
        identification.setExtents(Arrays.asList(extent));

        metadata.setIdentificationInfo(Arrays.asList(identification));

        /*
         * Test 1 ISO 19115:MD_Metadata:identificationInfo:citation:date:date#dateType=revision
         */
        Object result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:citation:date:date", "dateType", "revision", metadata);
        
        assertTrue(result instanceof Date);
        assertEquals(1266687454, ((Date)result).getTime());
        

        /*
         * Test 2 ISO 19115:MD_Metadata:identificationInfo:citation:date:date#dateType=publication
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:citation:date:date", "dateType", "publication", metadata);

        assertTrue(result instanceof Date);
        assertEquals(1253587454, ((Date)result).getTime());
        

        /*
         * Test 3 ISO 19115:MD_Metadata:identificationInfo:citation:date:date#dateType=creation
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:citation:date:date", "dateType", "creation", metadata);

        assertTrue(result instanceof Date);
        assertEquals(1245587454, ((Date)result).getTime());
        

         /**
         * Test 4 ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName#role=originator
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName", "role", "originator", metadata);

        assertTrue(result instanceof DefaultInternationalString);
        assertEquals(orgName1, result);
        

        /**
         * Test 5 ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName#role=publisher
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName", "role", "publisher", metadata);

        assertTrue(result instanceof DefaultInternationalString);
        assertEquals(orgName2, result);
        

        /**
         * Test 6 ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName#role=author
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName", "role", "author", metadata);

        assertTrue(result instanceof DefaultInternationalString);
        assertEquals(orgName3, result);

        /**
         * Test 7 ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword#type=VariablesCategory
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword", "type", "VariablesCategory", metadata);

        assertTrue("result type was:" + result.getClass().getName(), result instanceof List);
        assertEquals(key1, ((List)result).get(0));
        
        /**
         * Test 8 ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword#type=VariablesCategory
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword", "type", "StationType", metadata);

        assertTrue("result type was:" + result.getClass().getName(), result instanceof List);
        assertEquals(key2, ((List)result).get(0));

        /**
         * Test 8 ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:beginPosition#id=[0-9]+-all
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:extent:temporalElement:extent:beginPosition", "id", "[0-9]+-all", metadata);

        assertTrue("result type was:" + result.getClass().getName(), result instanceof TimePositionType);
        assertEquals(startPos, result);

    }

    @Test
    public void MultipleGetConditionalValuesFromPathTest() {

        DefaultMetadata metadata = new DefaultMetadata();

        List<DataIdentification> identifications = new ArrayList<DataIdentification>();
        DefaultDataIdentification identification = new DefaultDataIdentification();
        DefaultCitation citation = new DefaultCitation();
        List<CitationDate> dates = new ArrayList<CitationDate>();
        long randomDate1 = 1245587454;
        DefaultCitationDate date1 = new DefaultCitationDate(new Date(randomDate1), DateType.CREATION);
        long randomDate2 = 1253587454;
        DefaultCitationDate date2 = new DefaultCitationDate(new Date(randomDate2), DateType.PUBLICATION);
        long randomDate3 = 1266687454;
        DefaultCitationDate date3 = new DefaultCitationDate(new Date(randomDate3), DateType.REVISION);

        long randomDate4 = 1789587454;
        DefaultCitationDate date4 = new DefaultCitationDate(new Date(randomDate4), DateType.CREATION);
        long randomDate5 = 1999587454;
        DefaultCitationDate date5 = new DefaultCitationDate(new Date(randomDate5), DateType.PUBLICATION);
        long randomDate6 = 1888687454;
        DefaultCitationDate date6 = new DefaultCitationDate(new Date(randomDate6), DateType.REVISION);
        dates.add(date1);
        dates.add(date2);
        dates.add(date3);
        dates.add(date4);
        dates.add(date5);
        dates.add(date6);
        citation.setDates(dates);
        identification.setCitation(citation);
        List<ResponsibleParty> pocs = new ArrayList<ResponsibleParty>();
        DefaultResponsibleParty poc1 = new DefaultResponsibleParty(Role.ORIGINATOR);
        InternationalString orgName1 = new DefaultInternationalString("orgniz1");
        poc1.setOrganisationName(orgName1);
        pocs.add(poc1);
        DefaultResponsibleParty poc2 = new DefaultResponsibleParty(Role.PUBLISHER);
        InternationalString orgName2 = new DefaultInternationalString("orgniz2");
        poc2.setOrganisationName(orgName2);
        pocs.add(poc2);
        DefaultResponsibleParty poc3 = new DefaultResponsibleParty(Role.AUTHOR);
        InternationalString orgName3 = new DefaultInternationalString("orgniz3");
        poc3.setOrganisationName(orgName3);
        pocs.add(poc3);
        identification.setPointOfContacts(pocs);
        List<DefaultKeywords> keywords = new ArrayList<DefaultKeywords>();
        DefaultKeywords kw1 = new DefaultKeywords();
        InternationalString key1 = new SimpleInternationalString("keyword1");
        InternationalString key2 = new SimpleInternationalString("keyword2");
        kw1.setKeywords(Arrays.asList(key1, key2));
        kw1.setType(KeywordType.valueOf("VariablesCategory"));
        keywords.add(kw1);
        DefaultKeywords kw2 = new DefaultKeywords();
        InternationalString key3 = new SimpleInternationalString("keyword3");
        InternationalString key4 = new SimpleInternationalString("keyword4");
        kw2.setKeywords(Arrays.asList(key3,key4));
        kw2.setType(KeywordType.valueOf("StationType"));
        keywords.add(kw2);
        identification.setDescriptiveKeywords(keywords);
        identifications.add(identification);

        DefaultDataIdentification identification2 = new DefaultDataIdentification();
        List<ResponsibleParty> pocs2 = new ArrayList<ResponsibleParty>();
        DefaultResponsibleParty poc4 = new DefaultResponsibleParty(Role.ORIGINATOR);
        InternationalString orgName4 = new DefaultInternationalString("orgniz4");
        poc4.setOrganisationName(orgName4);
        pocs2.add(poc4);
        DefaultResponsibleParty poc5 = new DefaultResponsibleParty(Role.PUBLISHER);
        InternationalString orgName5 = new DefaultInternationalString("orgniz5");
        poc5.setOrganisationName(orgName5);
        pocs2.add(poc5);
        DefaultResponsibleParty poc6 = new DefaultResponsibleParty(Role.AUTHOR);
        InternationalString orgName6 = new DefaultInternationalString("orgniz6");
        poc6.setOrganisationName(orgName6);
        pocs2.add(poc6);
        identification2.setPointOfContacts(pocs2);
        List<DefaultKeywords> keywords2 = new ArrayList<DefaultKeywords>();
        DefaultKeywords kw3 = new DefaultKeywords();
        InternationalString key5 = new SimpleInternationalString("keyword5");
        InternationalString key6 = new SimpleInternationalString("keyword6");
        kw3.setKeywords(Arrays.asList(key5, key6));
        kw3.setType(KeywordType.valueOf("VariablesCategory"));
        keywords2.add(kw3);
        DefaultKeywords kw4 = new DefaultKeywords();
        InternationalString key7 = new SimpleInternationalString("keyword7");
        InternationalString key8 = new SimpleInternationalString("keyword8");
        kw4.setKeywords(Arrays.asList(key7,key8));
        kw4.setType(KeywordType.valueOf("StationType"));
        keywords2.add(kw4);
        identification2.setDescriptiveKeywords(keywords2);
        identifications.add(identification2);

        metadata.setIdentificationInfo(identifications);

        /*
         * Test 1 ISO 19115:MD_Metadata:identificationInfo:citation:date:date#dateType=revision
         */
        Object result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:citation:date:date", "dateType", "revision", metadata);

        assertTrue(result instanceof List);
        List collResult = (List) result;
        assertEquals(2, collResult.size());
        assertEquals(1266687454, ((Date)collResult.get(0)).getTime());
        assertEquals(1888687454, ((Date)collResult.get(1)).getTime());

        /*
         * Test 2 ISO 19115:MD_Metadata:identificationInfo:citation:date:date#dateType=publication
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:citation:date:date", "dateType", "publication", metadata);

        assertTrue(result instanceof List);
        collResult = (List) result;
        assertEquals(2, collResult.size());
        assertEquals(1253587454, ((Date)collResult.get(0)).getTime());
        assertEquals(1999587454, ((Date)collResult.get(1)).getTime());
        

        /*
         * Test 3 ISO 19115:MD_Metadata:identificationInfo:citation:date:date#dateType=creation
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:citation:date:date", "dateType", "creation", metadata);

        assertTrue(result instanceof List);
        collResult = (List) result;
        assertEquals(2, collResult.size());
        assertEquals(1245587454, ((Date)collResult.get(0)).getTime());
        assertEquals(1789587454, ((Date)collResult.get(1)).getTime());

        

        /**
         * Test 4 ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName#role=originator
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName", "role", "originator", metadata);

        assertTrue(result instanceof List);
        collResult = (List) result;
        assertTrue(collResult.get(0) instanceof DefaultInternationalString);
        assertTrue(collResult.get(1) instanceof DefaultInternationalString);
        assertEquals(orgName1, collResult.get(0));
        assertEquals(orgName4, collResult.get(1));
        

        /**
         * Test 5 ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName#role=publisher
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName", "role", "publisher", metadata);

        assertTrue(result instanceof List);
        collResult = (List) result;
        assertTrue(collResult.get(0) instanceof DefaultInternationalString);
        assertTrue(collResult.get(1) instanceof DefaultInternationalString);
        assertEquals(orgName2, collResult.get(0));
        assertEquals(orgName5, collResult.get(1));
        

        /**
         * Test 6 ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName#role=author
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:pointOfContact:organisationName", "role", "author", metadata);

        assertTrue(result instanceof List);
        collResult = (List) result;
        assertTrue(collResult.get(0) instanceof DefaultInternationalString);
        assertTrue(collResult.get(1) instanceof DefaultInternationalString);
        assertEquals(orgName3, collResult.get(0));
        assertEquals(orgName6, collResult.get(1));

        /**
         * Test 7 ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword#type=VariablesCategory
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword", "type", "VariablesCategory", metadata);

        assertTrue("result type was:" + result.getClass().getName(), result instanceof List);
        assertEquals(Arrays.asList(key1, key2), ((List)result).get(0));
        assertEquals(Arrays.asList(key5, key6), ((List)result).get(1));

        /**
         * Test 8 ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword#type=StationType
         */
        result = ReflectionUtilities.getConditionalValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:descriptiveKeywords:keyword", "type", "StationType", metadata);

        assertTrue("result type was:" + result.getClass().getName(), result instanceof List);
        assertEquals(Arrays.asList(key3, key4), ((List)result).get(0));
        assertEquals(Arrays.asList(key7, key8), ((List)result).get(1));
    }
    
    @Test
    public void instanceOfTest() {
         assertTrue(ReflectionUtilities.instanceOf("org.geotoolkit.ebrim.xml.v250.RegistryObjectType", UserType.class));
         assertTrue(ReflectionUtilities.instanceOf("org.geotoolkit.ebrim.xml.v250.RegistryObjectType", ClassificationSchemeType.class));
         assertFalse(ReflectionUtilities.instanceOf("org.geotoolkit.ebrim.xml.v250.RegistryObjectType", NotifyActionType.class));
         
    }
}
