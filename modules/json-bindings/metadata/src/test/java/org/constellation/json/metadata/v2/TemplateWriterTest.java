
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
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.citation.DefaultCitationDate;
import org.apache.sis.metadata.iso.constraint.DefaultLegalConstraints;
import org.apache.sis.metadata.iso.constraint.DefaultSecurityConstraints;
import org.apache.sis.metadata.iso.identification.DefaultDataIdentification;
import org.apache.sis.metadata.iso.identification.DefaultKeywords;
import org.apache.sis.metadata.iso.maintenance.DefaultScope;
import org.apache.sis.metadata.iso.quality.DefaultConformanceResult;
import org.apache.sis.metadata.iso.quality.DefaultDataQuality;
import org.apache.sis.metadata.iso.quality.DefaultDomainConsistency;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.json.metadata.binding.RootObj;
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
        
        final RootObj rootFilled = writer.writeTemplate(root, metadata);
        
        
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
        
        InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_default_raster2.json");
        RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
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
        
        RootObj rootFilled = writer.writeTemplate(root, metadata);
        
        
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
        
        stream     = TemplateWriterTest.class.getResourceAsStream("profile_default_raster2.json"); // TODO we should not have to do that. root is modified but writeTemplate
        root       =  objectMapper.readValue(stream, RootObj.class);
        rootFilled = writer.writeTemplate(root, metadata);
        
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
        
        stream     = TemplateWriterTest.class.getResourceAsStream("profile_default_raster2.json"); // TODO we should not have to do that. root is modified but writeTemplate
        root       =  objectMapper.readValue(stream, RootObj.class);
        rootFilled = writer.writeTemplate(root, metadata);
        
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
        
        final RootObj rootFilled = writer.writeTemplate(root, metadata);
        
        
        
        InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result_keywords.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);

        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
    }
    
    @Test
    public void testWriteFilledMetadataKeyword2() throws IOException {
        
        InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_keywords2.json");
        RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
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
        
        RootObj rootFilled = writer.writeTemplate(root, metadata);
        
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
        stream = TemplateWriterTest.class.getResourceAsStream("profile_keywords2.json"); // TODO we should not have to do that. root is modified but writeTemplate
        root       =  objectMapper.readValue(stream, RootObj.class);
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords2, keywords));
        
        rootFilled = writer.writeTemplate(root, metadata);
        resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
       /*
        * TEST 3 : two instance for gemet block, zero for free block
        */
        stream = TemplateWriterTest.class.getResourceAsStream("profile_keywords2.json"); // TODO we should not have to do that. root is modified but writeTemplate
        root       =  objectMapper.readValue(stream, RootObj.class);
        
        keywords2.setThesaurusName(gemet);
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));
        
        rootFilled = writer.writeTemplate(root, metadata);
        resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        resultJson = FileUtilities.getStringFromFile(resultFile);
        
        resStream = TemplateWriterTest.class.getResourceAsStream("result_keywords3.json");
        expectedJson = FileUtilities.getStringFromStream(resStream);
        
        assertEquals(expectedJson, resultJson);
        
    }
    
}
