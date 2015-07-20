
package org.constellation.json.metadata.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import org.apache.sis.internal.jaxb.metadata.replace.ReferenceSystemMetadata;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.citation.DefaultCitationDate;
import org.apache.sis.metadata.iso.constraint.DefaultLegalConstraints;
import org.apache.sis.metadata.iso.constraint.DefaultSecurityConstraints;
import org.apache.sis.metadata.iso.extent.DefaultExtent;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.metadata.iso.extent.DefaultGeographicDescription;
import org.apache.sis.metadata.iso.extent.DefaultTemporalExtent;
import org.apache.sis.metadata.iso.identification.DefaultDataIdentification;
import org.apache.sis.metadata.iso.identification.DefaultKeywords;
import org.apache.sis.metadata.iso.maintenance.DefaultScope;
import org.apache.sis.metadata.iso.quality.DefaultConformanceResult;
import org.apache.sis.metadata.iso.quality.DefaultDataQuality;
import org.apache.sis.metadata.iso.quality.DefaultDomainConsistency;
import org.apache.sis.metadata.iso.quality.DefaultFormatConsistency;
import org.apache.sis.metadata.iso.quality.DefaultQuantitativeResult;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.json.metadata.binding.RootObj;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.util.FileUtilities;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.util.InternationalString;

/**
 *
 * @author guilhem
 */
public class TemplateWriterTest {
 
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void testWriteFilledMetadata() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_default_raster.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        // case value into a block with no path
        metadata.setFileIdentifier("metadata-id-0007");
        metadata.setLanguage(Locale.FRENCH);
        
        // the second instance will be ignored int the result because the fields is not multiple
        metadata.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET, ScopeCode.APPLICATION));
        
        final DefaultDataQuality quality = new DefaultDataQuality(new DefaultScope(ScopeCode.DATASET));
        final DefaultDomainConsistency report = new DefaultDomainConsistency();
        final DefaultCitation cit = new DefaultCitation("some title");
        final DefaultCitationDate date = new DefaultCitationDate(new Date(11145603000L), DateType.CREATION);
        cit.setDates(Arrays.asList(date));
        final DefaultConformanceResult result = new DefaultConformanceResult(cit, "some explanation", true);
        report.setResults(Arrays.asList(result));
        quality.setReports(Arrays.asList(report));
        metadata.setDataQualityInfo(Arrays.asList(quality));
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));
        final DefaultKeywords keywords2 = new DefaultKeywords();
        final InternationalString kw21 = new SimpleInternationalString("this");
        final InternationalString kw22 = new SimpleInternationalString("is");
        keywords2.setKeywords(Arrays.asList(kw21, kw22));
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));
        metadata.setIdentificationInfo(Arrays.asList(dataIdent));
        
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        final RootObj rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        
        final InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
    }
    
    /**
     * This test add a security constraint object
     * 
     */
    @Test
    public void testWriteFilledMetadata2() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_default_raster2.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        // case value into a block with no path
        metadata.setFileIdentifier("metadata-id-0007");
        metadata.setLanguage(Locale.FRENCH);
        
        final DefaultDataQuality quality = new DefaultDataQuality(new DefaultScope(ScopeCode.DATASET));
        final DefaultDomainConsistency report = new DefaultDomainConsistency();
        final DefaultCitation cit = new DefaultCitation("some title");
        final DefaultCitationDate date = new DefaultCitationDate(new Date(11145600000L), DateType.CREATION);
        cit.setDates(Arrays.asList(date));
        final DefaultConformanceResult result = new DefaultConformanceResult(cit, "some explanation", true);
        report.setResults(Arrays.asList(result));
        quality.setReports(Arrays.asList(report));
        metadata.setDataQualityInfo(Arrays.asList(quality));
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));
        final DefaultKeywords keywords2 = new DefaultKeywords();
        final InternationalString kw21 = new SimpleInternationalString("this");
        final InternationalString kw22 = new SimpleInternationalString("is");
        keywords2.setKeywords(Arrays.asList(kw21, kw22));
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));
        metadata.setIdentificationInfo(Arrays.asList(dataIdent));
        
        final DefaultSecurityConstraints constraint = new DefaultSecurityConstraints();
        constraint.setUseLimitations(Arrays.asList(new SimpleInternationalString("some limitations")));
        
        dataIdent.setResourceConstraints(Arrays.asList(constraint));
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        RootObj rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        
        InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result2.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
        /**
         * Add a legal constraint block in first position
         */
        final DefaultLegalConstraints constraint2 = new DefaultLegalConstraints();
        constraint2.setUseLimitations(Arrays.asList(new SimpleInternationalString("legal limitations")));
        
        dataIdent.setResourceConstraints(Arrays.asList(constraint2, constraint));
        
        rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        resStream = TemplateWriterTest.class.getResourceAsStream("result3.json");
        expectedJson = FileUtilities.getStringFromStream(resStream);

        
        resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
        /**
         * invert the 2 constraint blocks
         */
        dataIdent.setResourceConstraints(Arrays.asList(constraint, constraint2));
        
        rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        resStream = TemplateWriterTest.class.getResourceAsStream("result3.json");
        expectedJson = FileUtilities.getStringFromStream(resStream);

        
        resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
    }
    
    @Test
    public void testWriteFilledMetadataKeyword() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_keywords.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        /*
        * KEYWORDS
        */
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));
        final DefaultKeywords keywords2 = new DefaultKeywords();
        final InternationalString kw21 = new SimpleInternationalString("this");
        final InternationalString kw22 = new SimpleInternationalString("is");
        keywords2.setKeywords(Arrays.asList(kw21, kw22));
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));
        metadata.setIdentificationInfo(Arrays.asList(dataIdent));
        
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        final RootObj rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        
        
        final InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result_keywords.json");
        final String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
    }
    
    @Test
    public void testWriteFilledMetadataKeyword2() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_keywords2.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        /*
        * TEST 1 : one instance for gemet block, one for free block
        */
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));
        final DefaultCitation gemet = new DefaultCitation("GEMET");
        gemet.setDates(Arrays.asList(new DefaultCitationDate(new Date(1325376000000L), DateType.PUBLICATION)));
        keywords.setThesaurusName(gemet);
        
        final DefaultKeywords keywords2 = new DefaultKeywords();
        final InternationalString kw21 = new SimpleInternationalString("this");
        final InternationalString kw22 = new SimpleInternationalString("is");
        keywords2.setKeywords(Arrays.asList(kw21, kw22));
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));
        metadata.setIdentificationInfo(Arrays.asList(dataIdent));
        
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        RootObj rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result_keywords2.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
        /*
        * TEST 2 : one instance for gemet block, one for free block but inversed in metadata
        */
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords2, keywords));
        
        rootFilled = writer.writeTemplate(root, metadata, false, false);
        resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
       /*
        * TEST 3 : two instance for gemet block, zero for free block
        */
        
        keywords2.setThesaurusName(gemet);
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));
        
        rootFilled = writer.writeTemplate(root, metadata, false, false);
        resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        resultJson = FileUtilities.getStringFromFile(resultFile);
        
        resStream = TemplateWriterTest.class.getResourceAsStream("result_keywords3.json");
        expectedJson = FileUtilities.getStringFromStream(resStream);
        
        assertEquals(expectedJson, resultJson);
        
        /*
        * TEST 4 : zero for gemet block, two instance for free block
        */
        keywords.setThesaurusName(null);
        keywords2.setThesaurusName(null);
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));
        
        rootFilled = writer.writeTemplate(root, metadata, false, false);
        resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        resultJson = FileUtilities.getStringFromFile(resultFile);
        
        resStream = TemplateWriterTest.class.getResourceAsStream("result_keywords4.json");
        expectedJson = FileUtilities.getStringFromStream(resStream);
        
        assertEquals(expectedJson, resultJson);
        
         /*
        * TEST 5 : two instance for gemet block, two instance for free block
        */
        keywords.setThesaurusName(gemet);
        keywords2.setThesaurusName(gemet);
        
        final DefaultKeywords keywords3 = new DefaultKeywords();
        final InternationalString kw31 = new SimpleInternationalString("you");
        final InternationalString kw32 = new SimpleInternationalString("shall");
        keywords3.setKeywords(Arrays.asList(kw31, kw32));
        final DefaultCitation agro = new DefaultCitation("AGRO");
        agro.setDates(Arrays.asList(new DefaultCitationDate(new Date(1325376000000L), DateType.CREATION)));
        keywords3.setThesaurusName(agro);
        final DefaultKeywords keywords4 = new DefaultKeywords();
        final InternationalString kw41 = new SimpleInternationalString("not pass");
        keywords4.setKeywords(Arrays.asList(kw41));
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2, keywords3, keywords4));
        
        rootFilled = writer.writeTemplate(root, metadata, false, false);
        resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        resultJson = FileUtilities.getStringFromFile(resultFile);
        
        resStream = TemplateWriterTest.class.getResourceAsStream("result_keywords5.json");
        expectedJson = FileUtilities.getStringFromStream(resStream);
        
        assertEquals(expectedJson, resultJson);
        
    }
    
    @Test
    public void testWriteFilledMetadataBadType() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_default_raster.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        // case value into a block with no path
        metadata.setFileIdentifier("metadata-id-0007");
        metadata.setLanguage(Locale.FRENCH);
        
        // the second instance will be ignored int the result because the fields is not multiple
        metadata.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET, ScopeCode.APPLICATION));
        
        
        // unexpected type for report element
        final DefaultDataQuality quality2 = new DefaultDataQuality(new DefaultScope(ScopeCode.AGGREGATE));
        final DefaultFormatConsistency report2 = new DefaultFormatConsistency();
        final DefaultQuantitativeResult confResult2 = new DefaultQuantitativeResult();
        confResult2.setErrorStatistic(new SimpleInternationalString("stats error"));
        report2.setResults(Arrays.asList(confResult2));
        quality2.setReports(Arrays.asList(report2));
        metadata.setDataQualityInfo(Arrays.asList(quality2));
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));
        final DefaultKeywords keywords2 = new DefaultKeywords();
        final InternationalString kw21 = new SimpleInternationalString("this");
        final InternationalString kw22 = new SimpleInternationalString("is");
        keywords2.setKeywords(Arrays.asList(kw21, kw22));
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));
        metadata.setIdentificationInfo(Arrays.asList(dataIdent));
        
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        final RootObj rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        
        final InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result4.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
    }
    
    @Test
    public void testWriteFilledMetadataMultipleFieldNonBlock() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_default_raster.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        // case value into a block with no path
        metadata.setFileIdentifier("metadata-id-0007");
        metadata.setLanguage(Locale.FRENCH);
        
        // the second instance will be ignored int the result because the fields is not multiple
        metadata.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET, ScopeCode.APPLICATION));
        
        final DefaultDataQuality quality = new DefaultDataQuality(new DefaultScope(ScopeCode.DATASET));
        final DefaultDomainConsistency report = new DefaultDomainConsistency();
        final DefaultCitation cit = new DefaultCitation("some title");
        final DefaultCitationDate date = new DefaultCitationDate(new Date(11145600000L), DateType.CREATION);
        cit.setDates(Arrays.asList(date));
        final DefaultConformanceResult result = new DefaultConformanceResult(cit, "some explanation", true);
        report.setResults(Arrays.asList(result));
        quality.setReports(Arrays.asList(report));
        
        // unexpected type for report element
        final DefaultDataQuality quality2 = new DefaultDataQuality(new DefaultScope(ScopeCode.AGGREGATE));
        final DefaultDomainConsistency report2 = new DefaultDomainConsistency();
        final DefaultCitation cit2 = new DefaultCitation("some second title");
        final DefaultCitationDate date2 = new DefaultCitationDate(new Date(11156600000L), DateType.PUBLICATION);
        cit2.setDates(Arrays.asList(date2));
        final DefaultConformanceResult confResult2 = new DefaultConformanceResult(cit2, "some second explanation", true);
        report2.setResults(Arrays.asList(confResult2));
        quality2.setReports(Arrays.asList(report2));
        
        
        metadata.setDataQualityInfo(Arrays.asList(quality, quality2));
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));
        final DefaultKeywords keywords2 = new DefaultKeywords();
        final InternationalString kw21 = new SimpleInternationalString("this");
        final InternationalString kw22 = new SimpleInternationalString("is");
        keywords2.setKeywords(Arrays.asList(kw21, kw22));
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));
        metadata.setIdentificationInfo(Arrays.asList(dataIdent));
        
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        final RootObj rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        
        final InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result5.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
    }
    
    @Test
    public void testWriteFilledMetadataMultipleBlock() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_multiple_block.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        /*
        * TEST 1 : one keyword, one thesaurus date
        */
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));
        final DefaultCitation gemet = new DefaultCitation("GEMET");
        gemet.setDates(Arrays.asList(new DefaultCitationDate(new Date(1325376000000L), DateType.PUBLICATION)));
        keywords.setThesaurusName(gemet);
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords));
        metadata.setIdentificationInfo(Arrays.asList(dataIdent));
        
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        RootObj rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result_multiple_block.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
        /*
        * TEST 2 : one keyword with two thesaurus date
        */
        gemet.setDates(Arrays.asList(new DefaultCitationDate(new Date(11156600000L), DateType.CREATION), new DefaultCitationDate(new Date(1325376000000L), DateType.PUBLICATION)));
        
        rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        resStream = TemplateWriterTest.class.getResourceAsStream("result_multiple_block2.json");
        expectedJson = FileUtilities.getStringFromStream(resStream);
        
        resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
    }
    
    @Test
    public void testWriteSpecialType() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_special_type.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        final ReferenceSystemMetadata rs = new ReferenceSystemMetadata(new DefaultIdentifier("EPSG:4326"));
        metadata.setReferenceSystemInfo(Arrays.asList(rs));
        
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        
        final DefaultExtent ex = new DefaultExtent();
        final DefaultTemporalExtent tex = new DefaultTemporalExtent();
        tex.setBounds(new Date(11142000000L), new Date(1325372400000L));
        ex.setTemporalElements(Arrays.asList(tex));
        dataIdent.setExtents(Arrays.asList(ex));
        metadata.setIdentificationInfo(Arrays.asList(dataIdent));
        
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        final RootObj rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        
        final InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result_special_type.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
    }
    
    @Test
    public void testWriteFilledMetadataPrune() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_default_raster.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        final DefaultDataQuality quality = new DefaultDataQuality(new DefaultScope(ScopeCode.DATASET));
        final DefaultDomainConsistency report = new DefaultDomainConsistency();
        final DefaultCitation cit = new DefaultCitation("some title");
        final DefaultCitationDate date = new DefaultCitationDate(new Date(11145600000L), DateType.CREATION);
        cit.setDates(Arrays.asList(date));
        final DefaultConformanceResult result = new DefaultConformanceResult(cit, "some explanation", true);
        report.setResults(Arrays.asList(result));
        quality.setReports(Arrays.asList(report));
        metadata.setDataQualityInfo(Arrays.asList(quality));
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));
        final DefaultKeywords keywords2 = new DefaultKeywords();
        final InternationalString kw21 = new SimpleInternationalString("this");
        final InternationalString kw22 = new SimpleInternationalString("is");
        keywords2.setKeywords(Arrays.asList(kw21, kw22));
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));
        metadata.setIdentificationInfo(Arrays.asList(dataIdent));
        
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        final RootObj rootFilled = writer.writeTemplate(root, metadata, true, false);
        
        
        final InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result_prune.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
    }
    
    @Test
    public void testWriteFilledMetadataPrune2() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_default_raster.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        // case value into a block with no path
        metadata.setFileIdentifier("metadata-id-0007");
        metadata.setLanguage(Locale.FRENCH);
        
        // the second instance will be ignored int the result because the fields is not multiple
        metadata.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET, ScopeCode.APPLICATION));
        
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        final RootObj rootFilled = writer.writeTemplate(root, metadata, true, false);
        
        
        final InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result_prune2.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
    }
    
    @Test
    public void testWriteSpecialType2() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_special_type.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        final ReferenceSystemMetadata rs = new ReferenceSystemMetadata(new DefaultIdentifier("EPSG:4326"));
        metadata.setReferenceSystemInfo(Arrays.asList(rs));
        
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        
        final DefaultExtent ex = new DefaultExtent();
        final DefaultTemporalExtent tex = new DefaultTemporalExtent();
        tex.setExtent(new TimeInstantType(new Date(System.currentTimeMillis())));
        ex.setTemporalElements(Arrays.asList(tex));
        dataIdent.setExtents(Arrays.asList(ex));
        metadata.setIdentificationInfo(Arrays.asList(dataIdent));
        
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        // just verify there is no error
        writer.writeTemplate(root, metadata, false, false);
    }
    
    @Test
    public void testWriteExtent2() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_extent.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        final ReferenceSystemMetadata rs = new ReferenceSystemMetadata(new DefaultIdentifier("EPSG:4326"));
        metadata.setReferenceSystemInfo(Arrays.asList(rs));
        
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        
        final DefaultExtent ex = new DefaultExtent();
        final DefaultGeographicBoundingBox bbox = new DefaultGeographicBoundingBox(-10, 10, -10, 10);
        ex.setGeographicElements(Arrays.asList(bbox));
        dataIdent.setExtents(Arrays.asList(ex));
        metadata.setIdentificationInfo(Arrays.asList(dataIdent));
        
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        final RootObj rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        
        final InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result_extent.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
    }
    
    @Test
    public void testWriteExtent3() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_extent.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        final ReferenceSystemMetadata rs = new ReferenceSystemMetadata(new DefaultIdentifier("EPSG:4326"));
        metadata.setReferenceSystemInfo(Arrays.asList(rs));
        
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        
        final DefaultExtent ex = new DefaultExtent();
        final DefaultGeographicBoundingBox bbox = new DefaultGeographicBoundingBox(-10, 10, -10, 10);
        
        final DefaultGeographicDescription desc = new DefaultGeographicDescription();
        final DefaultIdentifier id = new DefaultIdentifier("Gard");
        id.setCodeSpace("departement");
        desc.setGeographicIdentifier(id);
        
        
        ex.setGeographicElements(Arrays.asList(bbox, desc));
        dataIdent.setExtents(Arrays.asList(ex));
        metadata.setIdentificationInfo(Arrays.asList(dataIdent));
        
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        final RootObj rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        
        final InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result_extent2.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
    }
    
    @Test
    public void testWriteKeywords3() throws IOException {
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_keywords3.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        final DefaultMetadata metadata = new DefaultMetadata();
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("value1");
        final InternationalString kw3 = new SimpleInternationalString("world");
        final InternationalString kw4 = new SimpleInternationalString("value2");
        keywords.setKeywords(Arrays.asList(kw1, kw2, kw3, kw4));
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords));
        metadata.setIdentificationInfo(Arrays.asList(dataIdent));
        
        
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        
        final RootObj rootFilled = writer.writeTemplate(root, metadata, false, false);
        
        
        final InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result_keywords8.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
    
    
    }
}
