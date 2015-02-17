
package org.constellation.json.metadata.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.sis.metadata.iso.extent.DefaultTemporalExtent;
import org.apache.sis.metadata.iso.identification.DefaultDataIdentification;
import org.apache.sis.metadata.iso.identification.DefaultKeywords;
import org.apache.sis.metadata.iso.maintenance.DefaultScope;
import org.apache.sis.metadata.iso.quality.DefaultConformanceResult;
import org.apache.sis.metadata.iso.quality.DefaultDataQuality;
import org.apache.sis.metadata.iso.quality.DefaultDomainConsistency;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.json.metadata.binding.RootObj;
import org.constellation.test.utils.MetadataUtilities;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.constraint.Classification;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.util.FactoryException;
import org.opengis.util.InternationalString;

/**
 *
 * @author guilhem
 */
public class TemplateReaderTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void testReadFromFilledTemplate() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        
        Object result = reader.readTemplate(root, new DefaultMetadata());
        
        
        final DefaultMetadata expResult = new DefaultMetadata();
        
        expResult.setFileIdentifier("metadata-id-0007");
        expResult.setLanguage(Locale.FRENCH);
        expResult.setCharacterSet(CharacterSet.UTF_8);
        expResult.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET));
        expResult.setMetadataStandardName("x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster");
        expResult.setMetadataStandardVersion("2011.03");
        
        final DefaultDataQuality quality = new DefaultDataQuality(new DefaultScope(ScopeCode.DATASET));
        final DefaultDomainConsistency report = new DefaultDomainConsistency();
        final DefaultCitation cit = new DefaultCitation("some title");
        final DefaultCitationDate date = new DefaultCitationDate(new Date(11145600000L), DateType.CREATION);
        cit.setDates(Arrays.asList(date));
        final DefaultConformanceResult confResult = new DefaultConformanceResult(cit, "some explanation", true);
        report.setResults(Arrays.asList(confResult));
        quality.setReports(Arrays.asList(report));
        expResult.setDataQualityInfo(Arrays.asList(quality));
        
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
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
        
    }
    
    @Test
    public void testReadFromFilledTemplate2() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result2.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        
        Object result = reader.readTemplate(root, new DefaultMetadata());
        
        
        final DefaultMetadata expResult = new DefaultMetadata();
        
        expResult.setFileIdentifier("metadata-id-0007");
        expResult.setLanguage(Locale.FRENCH);
        expResult.setCharacterSet(CharacterSet.UTF_8);
        expResult.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET));
        expResult.setMetadataStandardName("x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster");
        expResult.setMetadataStandardVersion("2011.03");
        
        final DefaultDataQuality quality = new DefaultDataQuality(new DefaultScope(ScopeCode.DATASET));
        final DefaultDomainConsistency report = new DefaultDomainConsistency();
        final DefaultCitation cit = new DefaultCitation("some title");
        final DefaultCitationDate date = new DefaultCitationDate(new Date(11145600000L), DateType.CREATION);
        cit.setDates(Arrays.asList(date));
        final DefaultConformanceResult confResult = new DefaultConformanceResult(cit, "some explanation", true);
        report.setResults(Arrays.asList(confResult));
        quality.setReports(Arrays.asList(report));
        expResult.setDataQualityInfo(Arrays.asList(quality));
        
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
        
        final DefaultLegalConstraints constraint1 = new DefaultLegalConstraints();
        constraint1.setAccessConstraints(Arrays.asList(Restriction.LICENCE));
        
        final DefaultSecurityConstraints constraint2 = new DefaultSecurityConstraints();
        constraint2.setUseLimitations(Arrays.asList(new SimpleInternationalString("some limitations")));
        constraint2.setClassification(Classification.UNCLASSIFIED);
        
        dataIdent.setResourceConstraints(Arrays.asList(constraint1,constraint2));
        
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));
        
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
        
    }
    
    @Test
    public void testReadFromFilledTemplateKeywords() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_keywords.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        
        Object result = reader.readTemplate(root, new DefaultMetadata());
        
        
        final DefaultMetadata expResult = new DefaultMetadata();
        
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
        
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }
    
    @Test
    public void testReadFromFilledTemplateMultipleBlock() throws IOException, FactoryException {
        InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_multiple_block.json");
        RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        
        Object result = reader.readTemplate(root, new DefaultMetadata());
        
        
        final DefaultMetadata expResult = new DefaultMetadata();
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));
        final DefaultCitation gemet = new DefaultCitation("GEMET");
        gemet.setDates(Arrays.asList(new DefaultCitationDate(new Date(1325376000000L), DateType.PUBLICATION)));
        keywords.setThesaurusName(gemet);
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords));
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
        
        /*
        * TEST 2 : one keyword with two thesaurus date
        */
        gemet.setDates(Arrays.asList(new DefaultCitationDate(new Date(11145600000L), DateType.CREATION), 
                                     new DefaultCitationDate(new Date(1325376000000L), DateType.PUBLICATION)));
        
        stream = TemplateReaderTest.class.getResourceAsStream("result_multiple_block2.json");
        root   =  objectMapper.readValue(stream, RootObj.class);
        
        result = reader.readTemplate(root, new DefaultMetadata());
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }
    
    @Test
    public void testReadFromFilledTemplateSpecialType() throws IOException, FactoryException {
        InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_special_type.json");
        RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        
        Object result = reader.readTemplate(root, new DefaultMetadata());
        
        final DefaultMetadata expResult = new DefaultMetadata();
        
        final ReferenceSystemMetadata rs = new ReferenceSystemMetadata(new DefaultIdentifier("EPSG:4326"));
        expResult.setReferenceSystemInfo(Arrays.asList(rs));
        
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        
        final DefaultExtent ex = new DefaultExtent();
        final DefaultTemporalExtent tex = new DefaultTemporalExtent();
                                                       
        tex.setBounds(new Date(11142000000L), new Date(1325372400000L));
        ex.setTemporalElements(Arrays.asList(tex));
        dataIdent.setExtents(Arrays.asList(ex));
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }
    
    @Test
    public void testReadFromFilledTemplateExtent() throws IOException, FactoryException {
        InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_extent.json");
        RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        
        Object result = reader.readTemplate(root, new DefaultMetadata());
        
        final DefaultMetadata expResult = new DefaultMetadata();
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        
        final DefaultExtent ex = new DefaultExtent();
        final DefaultGeographicBoundingBox bbox = new DefaultGeographicBoundingBox(-10, 10, -10, 10);
        bbox.setInclusion(null);
        ex.setGeographicElements(Arrays.asList(bbox));
        
        dataIdent.setExtents(Arrays.asList(ex));
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }
}
