
package org.constellation.json.metadata.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.sis.metadata.iso.maintenance.DefaultMaintenanceInformation;
import org.apache.sis.metadata.iso.maintenance.DefaultScope;
import org.apache.sis.metadata.iso.quality.DefaultConformanceResult;
import org.apache.sis.metadata.iso.quality.DefaultDataQuality;
import org.apache.sis.metadata.iso.quality.DefaultDomainConsistency;
import org.apache.sis.metadata.iso.quality.DefaultFormatConsistency;
import org.apache.sis.metadata.iso.quality.DefaultQuantitativeResult;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.json.metadata.binding.RootObj;
import org.constellation.test.utils.MetadataUtilities;
import org.junit.Test;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.constraint.Classification;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.maintenance.MaintenanceFrequency;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.util.FactoryException;
import org.opengis.util.InternationalString;

/**
 *
 * @author guilhem
 */
public class TeamplateReaderUpdateTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * in this test the metadata is the same as the original
     * 
     * @throws IOException
     * @throws FactoryException 
     */
    @Test
    public void testUpdateSame() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata previous = new DefaultMetadata();
        
        previous.setFileIdentifier("metadata-id-0007");
        previous.setLanguage(Locale.FRENCH);
        previous.setCharacterSet(CharacterSet.UTF_8);
        previous.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET));
        previous.setMetadataStandardName("x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster");
        previous.setMetadataStandardVersion("2011.03");
        
        final DefaultDataQuality previousQuality = new DefaultDataQuality(new DefaultScope(ScopeCode.DATASET));
        final DefaultDomainConsistency previousReport = new DefaultDomainConsistency();
        final DefaultCitation previousCit = new DefaultCitation("some title");
        final DefaultCitationDate previousDate = new DefaultCitationDate(new Date(11145600000L), DateType.CREATION);
        previousCit.setDates(Arrays.asList(previousDate));
        final DefaultConformanceResult previousConfResult = new DefaultConformanceResult(previousCit, "some explanation", true);
        previousReport.setResults(Arrays.asList(previousConfResult));
        previousQuality.setReports(Arrays.asList(previousReport));
        previous.setDataQualityInfo(Arrays.asList(previousQuality));
        
        final DefaultDataIdentification previousDataIdent = new DefaultDataIdentification();
        final DefaultKeywords previousKeywords = new DefaultKeywords();
        final InternationalString previousKw1 = new SimpleInternationalString("hello");
        final InternationalString previousKw2 = new SimpleInternationalString("world");
        previousKeywords.setKeywords(Arrays.asList(previousKw1, previousKw2));
        
        final DefaultKeywords previousKeywords2 = new DefaultKeywords();
        final InternationalString previousKw21 = new SimpleInternationalString("this");
        final InternationalString previousKw22 = new SimpleInternationalString("is");
        previousKeywords2.setKeywords(Arrays.asList(previousKw21, previousKw22));
        
        previousDataIdent.setDescriptiveKeywords(Arrays.asList(previousKeywords, previousKeywords2));
        previous.setIdentificationInfo(Arrays.asList(previousDataIdent));
        
        
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        
        Object result = reader.readTemplate(root, previous);
        
        
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
    
    /**
     * in this test the metadata is the same as the original
     * 
     * @throws IOException
     * @throws FactoryException 
     */
    @Test
    public void testUpdateChange() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata previous = new DefaultMetadata();
        
        previous.setFileIdentifier("metadata-id-0008");
        previous.setLanguage(Locale.ENGLISH);
        previous.setCharacterSet(CharacterSet.ISO_8859_1);
        previous.setHierarchyLevels(Arrays.asList(ScopeCode.APPLICATION));
        previous.setMetadataStandardName("x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster2");
        previous.setMetadataStandardVersion("2011.03.21");
        
        final DefaultDataQuality previousQuality = new DefaultDataQuality(new DefaultScope(ScopeCode.AGGREGATE));
        final DefaultDomainConsistency previousReport = new DefaultDomainConsistency();
        final DefaultCitation previousCit = new DefaultCitation("ancient value");
        final DefaultCitationDate previousDate = new DefaultCitationDate(new Date(11145600000L), DateType.PUBLICATION);
        previousCit.setDates(Arrays.asList(previousDate));
        final DefaultConformanceResult previousConfResult = new DefaultConformanceResult(previousCit, "some old explanation", true);
        previousReport.setResults(Arrays.asList(previousConfResult));
        previousQuality.setReports(Arrays.asList(previousReport));
        previous.setDataQualityInfo(Arrays.asList(previousQuality));
        
        final DefaultDataIdentification previousDataIdent = new DefaultDataIdentification();
        final DefaultKeywords previousKeywords = new DefaultKeywords();
        final InternationalString previousKw1 = new SimpleInternationalString("older word");
        previousKeywords.setKeywords(Arrays.asList(previousKw1));
        
        
        previousDataIdent.setDescriptiveKeywords(Arrays.asList(previousKeywords));
        previous.setIdentificationInfo(Arrays.asList(previousDataIdent));
        
        
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        
        Object result = reader.readTemplate(root, previous);
        
        
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
    
    /**
     * in this test the metadata is the same as the original
     * 
     * @throws IOException
     * @throws FactoryException 
     */
    @Test
    public void testUpdateChange2() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata previous = new DefaultMetadata();
        
        previous.setFileIdentifier("metadata-id-0008");
        previous.setLanguage(Locale.ENGLISH);
        previous.setCharacterSet(CharacterSet.ISO_8859_1);
        previous.setHierarchyLevels(Arrays.asList(ScopeCode.APPLICATION, ScopeCode.AGGREGATE));// extra hierarchy level
        previous.setMetadataStandardName("x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster2");
        previous.setMetadataStandardVersion("2011.03.21");
        
        final DefaultDataQuality previousQuality = new DefaultDataQuality(new DefaultScope(ScopeCode.AGGREGATE));
        final DefaultDomainConsistency previousReport = new DefaultDomainConsistency();
        final DefaultCitation previousCit = new DefaultCitation("ancient value");
        final DefaultCitationDate previousDate = new DefaultCitationDate(new Date(11145600000L), DateType.PUBLICATION);
        previousCit.setDates(Arrays.asList(previousDate));
        final DefaultConformanceResult previousConfResult = new DefaultConformanceResult(previousCit, "some old explanation", true);
        previousReport.setResults(Arrays.asList(previousConfResult));
        previousQuality.setReports(Arrays.asList(previousReport));
        
        // extra dataqualityInfo
        final DefaultDataQuality previousQuality2 = new DefaultDataQuality(new DefaultScope(ScopeCode.AGGREGATE));
        final DefaultFormatConsistency previousReport2 = new DefaultFormatConsistency();
        final DefaultQuantitativeResult previousConfResult2 = new DefaultQuantitativeResult();
        previousConfResult2.setErrorStatistic(new SimpleInternationalString("stats error"));
        previousReport2.setResults(Arrays.asList(previousConfResult2));
        previousQuality2.setReports(Arrays.asList(previousReport2));
        
        previous.setDataQualityInfo(Arrays.asList(previousQuality, previousQuality2));
        
        final DefaultDataIdentification previousDataIdent = new DefaultDataIdentification();
        final DefaultKeywords previousKeywords = new DefaultKeywords();
        final InternationalString previousKw1 = new SimpleInternationalString("older word");
        previousKeywords.setKeywords(Arrays.asList(previousKw1));
        
        
        previousDataIdent.setDescriptiveKeywords(Arrays.asList(previousKeywords));
        previous.setIdentificationInfo(Arrays.asList(previousDataIdent));
        
        //out of scope maintenance info
        final DefaultMaintenanceInformation previousMaint = new DefaultMaintenanceInformation();
        previousMaint.setMaintenanceAndUpdateFrequency(MaintenanceFrequency.DAILY);
        previousMaint.setMaintenanceNotes(Arrays.asList(new SimpleInternationalString(" some notes")));
        previous.setMetadataMaintenance(previousMaint);
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        
        Object result = reader.readTemplate(root, previous);
        
        
        final DefaultMetadata expResult = new DefaultMetadata();
        
        expResult.setFileIdentifier("metadata-id-0007");
        expResult.setLanguage(Locale.FRENCH);
        expResult.setCharacterSet(CharacterSet.UTF_8);
        expResult.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET));// extra hierarchy level removed
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
        
        // extra dataqualityInfo
        final DefaultDataQuality quality2 = new DefaultDataQuality(new DefaultScope(ScopeCode.AGGREGATE));
        final DefaultFormatConsistency report2 = new DefaultFormatConsistency();
        final DefaultQuantitativeResult confResult2 = new DefaultQuantitativeResult();
        confResult2.setErrorStatistic(new SimpleInternationalString("stats error"));
        report2.setResults(Arrays.asList(confResult2));
        quality2.setReports(Arrays.asList(report2));
        
        expResult.setDataQualityInfo(Arrays.asList(quality, quality2));
        
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
        
        final DefaultMaintenanceInformation maint = new DefaultMaintenanceInformation();
        maint.setMaintenanceAndUpdateFrequency(MaintenanceFrequency.DAILY);
        maint.setMaintenanceNotes(Arrays.asList(new SimpleInternationalString(" some notes")));
        expResult.setMetadataMaintenance(maint);
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
        
    }
    
    @Test
    public void testUpdateChange3() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata previous = new DefaultMetadata();
        
        previous.setFileIdentifier("metadata-id-0008");
        previous.setLanguage(Locale.ENGLISH);
        previous.setCharacterSet(CharacterSet.ISO_8859_1);
        previous.setHierarchyLevels(Arrays.asList(ScopeCode.APPLICATION, ScopeCode.AGGREGATE));// extra hierarchy level
        previous.setMetadataStandardName("x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster2");
        previous.setMetadataStandardVersion("2011.03.21");
        
        // unexpected type for report
        final DefaultDataQuality previousQuality2 = new DefaultDataQuality(new DefaultScope(ScopeCode.AGGREGATE));
        final DefaultFormatConsistency previousReport2 = new DefaultFormatConsistency();
        final DefaultQuantitativeResult previousConfResult2 = new DefaultQuantitativeResult();
        previousConfResult2.setErrorStatistic(new SimpleInternationalString("stats error"));
        previousReport2.setResults(Arrays.asList(previousConfResult2));
        previousQuality2.setReports(Arrays.asList(previousReport2));
        
        previous.setDataQualityInfo(Arrays.asList(previousQuality2));
        
        final DefaultDataIdentification previousDataIdent = new DefaultDataIdentification();
        final DefaultKeywords previousKeywords = new DefaultKeywords();
        final InternationalString previousKw1 = new SimpleInternationalString("older word");
        previousKeywords.setKeywords(Arrays.asList(previousKw1));
        
        
        previousDataIdent.setDescriptiveKeywords(Arrays.asList(previousKeywords));
        previous.setIdentificationInfo(Arrays.asList(previousDataIdent));
        
        //out of scope maintenance info
        final DefaultMaintenanceInformation previousMaint = new DefaultMaintenanceInformation();
        previousMaint.setMaintenanceAndUpdateFrequency(MaintenanceFrequency.DAILY);
        previousMaint.setMaintenanceNotes(Arrays.asList(new SimpleInternationalString(" some notes")));
        previous.setMetadataMaintenance(previousMaint);
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        
        Object result = reader.readTemplate(root, previous);
        
        
        final DefaultMetadata expResult = new DefaultMetadata();
        
        expResult.setFileIdentifier("metadata-id-0007");
        expResult.setLanguage(Locale.FRENCH);
        expResult.setCharacterSet(CharacterSet.UTF_8);
        expResult.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET));// extra hierarchy level removed
        expResult.setMetadataStandardName("x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster");
        expResult.setMetadataStandardVersion("2011.03");
        
        final DefaultDataQuality quality = new DefaultDataQuality(new DefaultScope(ScopeCode.DATASET));
        
        final DefaultDomainConsistency report = new DefaultDomainConsistency();
        final DefaultCitation cit = new DefaultCitation("some title");
        final DefaultCitationDate date = new DefaultCitationDate(new Date(11145600000L), DateType.CREATION);
        cit.setDates(Arrays.asList(date));
        final DefaultConformanceResult confResult = new DefaultConformanceResult(cit, "some explanation", true);
        report.setResults(Arrays.asList(confResult));
        
        // extra report
        final DefaultFormatConsistency report2 = new DefaultFormatConsistency();
        final DefaultQuantitativeResult confResult2 = new DefaultQuantitativeResult();
        confResult2.setErrorStatistic(new SimpleInternationalString("stats error"));
        report2.setResults(Arrays.asList(confResult2));
        
        quality.setReports(Arrays.asList(report2, report));
        
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
        
        final DefaultMaintenanceInformation maint = new DefaultMaintenanceInformation();
        maint.setMaintenanceAndUpdateFrequency(MaintenanceFrequency.DAILY);
        maint.setMaintenanceNotes(Arrays.asList(new SimpleInternationalString(" some notes")));
        expResult.setMetadataMaintenance(maint);
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }
    
    @Test
    public void testUpdateMultipleBlock() throws IOException, FactoryException {
        
        final DefaultMetadata previous = new DefaultMetadata();
        
        final DefaultDataIdentification previousDataIdent = new DefaultDataIdentification();
        final DefaultKeywords previouskeywords = new DefaultKeywords();
        final InternationalString pkw1 = new SimpleInternationalString("hello");
        final InternationalString pkw2 = new SimpleInternationalString("world");
        previouskeywords.setKeywords(Arrays.asList(pkw1, pkw2));
        final DefaultCitation previousGemet = new DefaultCitation("GEMET");
        previousGemet.setDates(Arrays.asList(new DefaultCitationDate(new Date(1325376000000L), DateType.PUBLICATION)));
        previouskeywords.setThesaurusName(previousGemet);
        
        previousDataIdent.setDescriptiveKeywords(Arrays.asList(previouskeywords));
        previous.setIdentificationInfo(Arrays.asList(previousDataIdent));
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_multiple_block2.json");
        RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        Object result = reader.readTemplate(root, previous);
        
        
        final DefaultMetadata expResult = new DefaultMetadata();
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));
        final DefaultCitation gemet = new DefaultCitation("GEMET");
        gemet.setDates(Arrays.asList(new DefaultCitationDate(new Date(11145600000L), DateType.CREATION), 
                                     new DefaultCitationDate(new Date(1325376000000L), DateType.PUBLICATION)));
        keywords.setThesaurusName(gemet);
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords));
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }
    
    
    @Test
    public void testReadAddBlockInstance() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_keywords_UI.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata previous = new DefaultMetadata();
        
        final DefaultDataIdentification prevDataIdent = new DefaultDataIdentification();
        final DefaultKeywords prevKeywords = new DefaultKeywords();
        final InternationalString pkw1 = new SimpleInternationalString("hello");
        final InternationalString pkw2 = new SimpleInternationalString("world");
        prevKeywords.setKeywords(Arrays.asList(pkw1, pkw2));
        
        prevDataIdent.setDescriptiveKeywords(Arrays.asList(prevKeywords));
        
        previous.setIdentificationInfo(Arrays.asList(prevDataIdent));
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        Object result = reader.readTemplate(root, previous);
        
        
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
        
        final DefaultKeywords keywords3 = new DefaultKeywords();
        final InternationalString kw31 = new SimpleInternationalString("my");
        final InternationalString kw32 = new SimpleInternationalString("test");
        keywords3.setKeywords(Arrays.asList(kw31, kw32));
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2, keywords3));
        
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }
    
    @Test
    public void testReadRemoveBlockInstance() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_keywords6.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata previous = new DefaultMetadata();
        
        final DefaultDataIdentification prevDataIdent = new DefaultDataIdentification();
        final DefaultKeywords prevKeywords = new DefaultKeywords();
        final InternationalString pkw1 = new SimpleInternationalString("hello");
        final InternationalString pkw2 = new SimpleInternationalString("world");
        prevKeywords.setKeywords(Arrays.asList(pkw1, pkw2));
        
        final DefaultKeywords prevKeywords2 = new DefaultKeywords();
        final InternationalString pkw21 = new SimpleInternationalString("this");
        final InternationalString pkw22 = new SimpleInternationalString("is");
        prevKeywords2.setKeywords(Arrays.asList(pkw21, pkw22));
        
        prevDataIdent.setDescriptiveKeywords(Arrays.asList(prevKeywords, prevKeywords2));
        
        previous.setIdentificationInfo(Arrays.asList(prevDataIdent));
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        Object result = reader.readTemplate(root, previous);
        
        
        final DefaultMetadata expResult = new DefaultMetadata();
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords));
        
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }
    
    @Test
    public void testReadRemoveFieldInstance() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_keywords7.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        
        final DefaultMetadata previous = new DefaultMetadata();
        
        final DefaultDataIdentification prevDataIdent = new DefaultDataIdentification();
        final DefaultKeywords prevKeywords = new DefaultKeywords();
        final InternationalString pkw1 = new SimpleInternationalString("hello");
        final InternationalString pkw2 = new SimpleInternationalString("world");
        prevKeywords.setKeywords(Arrays.asList(pkw1, pkw2));
        
        final DefaultKeywords prevKeywords2 = new DefaultKeywords();
        final InternationalString pkw21 = new SimpleInternationalString("this");
        final InternationalString pkw22 = new SimpleInternationalString("is");
        prevKeywords2.setKeywords(Arrays.asList(pkw21, pkw22));
        
        prevDataIdent.setDescriptiveKeywords(Arrays.asList(prevKeywords, prevKeywords2));
        
        previous.setIdentificationInfo(Arrays.asList(prevDataIdent));
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        Object result = reader.readTemplate(root, previous);
        
        
        final DefaultMetadata expResult = new DefaultMetadata();
        
        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        keywords.setKeywords(Arrays.asList(kw1));
        
        final DefaultKeywords keywords2 = new DefaultKeywords();
        final InternationalString kw22 = new SimpleInternationalString("is");
        keywords2.setKeywords(Arrays.asList(kw22));
        
        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));
        
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }
    
    @Test
    public void testReadFromFilledTemplate2() throws IOException, FactoryException {
        
        final DefaultMetadata previous = new DefaultMetadata();
        
        previous.setFileIdentifier("metadata-id-0007");
        previous.setLanguage(Locale.FRENCH);
        previous.setCharacterSet(CharacterSet.UTF_8);
        previous.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET));
        previous.setMetadataStandardName("x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster");
        previous.setMetadataStandardVersion("2011.03");
        
        final DefaultDataQuality prevQuality = new DefaultDataQuality(new DefaultScope(ScopeCode.DATASET));
        final DefaultDomainConsistency prevReport = new DefaultDomainConsistency();
        final DefaultCitation prevCit = new DefaultCitation("some title");
        final DefaultCitationDate prevDate = new DefaultCitationDate(new Date(11145600000L), DateType.CREATION);
        prevCit.setDates(Arrays.asList(prevDate));
        final DefaultConformanceResult prevConfResult = new DefaultConformanceResult(prevCit, "some explanation", true);
        prevReport.setResults(Arrays.asList(prevConfResult));
        prevQuality.setReports(Arrays.asList(prevReport));
        previous.setDataQualityInfo(Arrays.asList(prevQuality));
        
        final DefaultDataIdentification prevDataIdent = new DefaultDataIdentification();
        final DefaultKeywords prevKeywords = new DefaultKeywords();
        final InternationalString pkw1 = new SimpleInternationalString("hello");
        final InternationalString pkw2 = new SimpleInternationalString("world");
        prevKeywords.setKeywords(Arrays.asList(pkw1, pkw2));
        
        final DefaultKeywords prevKeywords2 = new DefaultKeywords();
        final InternationalString pkw21 = new SimpleInternationalString("this");
        final InternationalString pkw22 = new SimpleInternationalString("is");
        prevKeywords2.setKeywords(Arrays.asList(pkw21, pkw22));
        
        prevDataIdent.setDescriptiveKeywords(Arrays.asList(prevKeywords, prevKeywords2));
        
        final DefaultLegalConstraints prevConstraint1 = new DefaultLegalConstraints();
        prevConstraint1.setAccessConstraints(Arrays.asList(Restriction.LICENCE));
        
        final DefaultSecurityConstraints prevConstraint2 = new DefaultSecurityConstraints();
        prevConstraint2.setUseLimitations(Arrays.asList(new SimpleInternationalString("some limitations")));
        prevConstraint2.setClassification(Classification.UNCLASSIFIED);
        
        prevDataIdent.setResourceConstraints(Arrays.asList(prevConstraint1,prevConstraint2));
        
        previous.setIdentificationInfo(Arrays.asList(prevDataIdent));
        
        
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result2.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        Object result = reader.readTemplate(root, previous);
        
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
    public void testRemoveSpecialTypeBlock() throws IOException, FactoryException {
        
        final DefaultMetadata previous = new DefaultMetadata();
        
        previous.setFileIdentifier("metadata-id-0007");
        previous.setLanguage(Locale.FRENCH);
        previous.setCharacterSet(CharacterSet.UTF_8);
        previous.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET));
        previous.setMetadataStandardName("x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster");
        previous.setMetadataStandardVersion("2011.03");
        
        final DefaultDataQuality prevQuality = new DefaultDataQuality(new DefaultScope(ScopeCode.DATASET));
        final DefaultDomainConsistency prevReport = new DefaultDomainConsistency();
        final DefaultCitation prevCit = new DefaultCitation("some title");
        final DefaultCitationDate prevDate = new DefaultCitationDate(new Date(11145600000L), DateType.CREATION);
        prevCit.setDates(Arrays.asList(prevDate));
        final DefaultConformanceResult prevConfResult = new DefaultConformanceResult(prevCit, "some explanation", true);
        prevReport.setResults(Arrays.asList(prevConfResult));
        prevQuality.setReports(Arrays.asList(prevReport));
        previous.setDataQualityInfo(Arrays.asList(prevQuality));
        
        final DefaultDataIdentification prevDataIdent = new DefaultDataIdentification();
        final DefaultKeywords prevKeywords = new DefaultKeywords();
        final InternationalString pkw1 = new SimpleInternationalString("hello");
        final InternationalString pkw2 = new SimpleInternationalString("world");
        prevKeywords.setKeywords(Arrays.asList(pkw1, pkw2));
        
        final DefaultKeywords prevKeywords2 = new DefaultKeywords();
        final InternationalString pkw21 = new SimpleInternationalString("this");
        final InternationalString pkw22 = new SimpleInternationalString("is");
        prevKeywords2.setKeywords(Arrays.asList(pkw21, pkw22));
        
        prevDataIdent.setDescriptiveKeywords(Arrays.asList(prevKeywords, prevKeywords2));
        
        final DefaultLegalConstraints prevConstraint1 = new DefaultLegalConstraints();
        prevConstraint1.setAccessConstraints(Arrays.asList(Restriction.LICENCE));
        
        final DefaultLegalConstraints prevConstraint2 = new DefaultLegalConstraints();
        prevConstraint2.setAccessConstraints(Arrays.asList(Restriction.COPYRIGHT));
        
        
        final DefaultSecurityConstraints prevConstraint3 = new DefaultSecurityConstraints();
        prevConstraint3.setUseLimitations(Arrays.asList(new SimpleInternationalString("some limitations")));
        prevConstraint3.setClassification(Classification.UNCLASSIFIED);
        
        final DefaultSecurityConstraints prevConstraint4 = new DefaultSecurityConstraints();
        prevConstraint4.setUseLimitations(Arrays.asList(new SimpleInternationalString("some limitations second")));
        prevConstraint4.setClassification(Classification.PROTECTED);
        
        prevDataIdent.setResourceConstraints(Arrays.asList(prevConstraint1,prevConstraint2, prevConstraint3, prevConstraint4));
        
        previous.setIdentificationInfo(Arrays.asList(prevDataIdent));
        
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result2.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        Object result = reader.readTemplate(root, previous);
        
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
    public void testRemoveSpecialKeywordBlock() throws IOException, FactoryException {
        
        final DefaultMetadata previous = new DefaultMetadata();
        final DefaultDataIdentification prevDataIdent = new DefaultDataIdentification();
        final DefaultKeywords prevKeywords = new DefaultKeywords();
        final InternationalString pkw1 = new SimpleInternationalString("hello");
        final InternationalString pkw2 = new SimpleInternationalString("world");
        prevKeywords.setKeywords(Arrays.asList(pkw1, pkw2));
        final DefaultCitation prevGemet = new DefaultCitation("GEMET");
        prevGemet.setDates(Arrays.asList(new DefaultCitationDate(new Date(1325376000000L), DateType.PUBLICATION)));
        prevKeywords.setThesaurusName(prevGemet);
        
        final DefaultKeywords prevKeywords2 = new DefaultKeywords();
        final InternationalString pkw21 = new SimpleInternationalString("you shall");
        final InternationalString pkw22 = new SimpleInternationalString("not pass");
        prevKeywords2.setKeywords(Arrays.asList(pkw21, pkw22));
        prevKeywords2.setThesaurusName(prevGemet);
        
        final DefaultKeywords prevKeywords3 = new DefaultKeywords();
        final InternationalString pkw31 = new SimpleInternationalString("this");
        final InternationalString pkw32 = new SimpleInternationalString("is");
        prevKeywords3.setKeywords(Arrays.asList(pkw31, pkw32));
        
        prevDataIdent.setDescriptiveKeywords(Arrays.asList(prevKeywords, prevKeywords2, prevKeywords3));
        previous.setIdentificationInfo(Arrays.asList(prevDataIdent));
        
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_keywords2.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        Object result = reader.readTemplate(root, previous);
        
        final DefaultMetadata expResult = new DefaultMetadata();
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
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));
        
        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    
    }
    
    /**
     */
    @Test
    public void testReadFromFilledTemplate3() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result5.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);

        final DefaultMetadata previous = new DefaultMetadata();
        
        previous.setFileIdentifier("metadata-id-0007");
        previous.setLanguage(Locale.FRENCH);
        previous.setCharacterSet(CharacterSet.UTF_8);
        previous.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET));
        previous.setMetadataStandardName("x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster");
        previous.setMetadataStandardVersion("2011.03");
        
        final DefaultDataQuality pquality = new DefaultDataQuality(new DefaultScope(ScopeCode.DATASET));
        final DefaultDomainConsistency preport = new DefaultDomainConsistency();
        final DefaultCitation pcit = new DefaultCitation("some title");
        final DefaultCitationDate pdate = new DefaultCitationDate(new Date(11145600000L), DateType.CREATION);
        pcit.setDates(Arrays.asList(pdate));
        final DefaultConformanceResult pconfResult = new DefaultConformanceResult(pcit, "some explanation", true);
        preport.setResults(Arrays.asList(pconfResult));
        pquality.setReports(Arrays.asList(preport));
        
        
        final DefaultDataQuality pquality2 = new DefaultDataQuality(new DefaultScope(ScopeCode.AGGREGATE));
        final DefaultDomainConsistency preport2 = new DefaultDomainConsistency();
        final DefaultCitation pcit2 = new DefaultCitation("some second title  before change");
        final DefaultCitationDate pdate2 = new DefaultCitationDate(new Date(11156600000L), DateType.PUBLICATION);
        pcit2.setDates(Arrays.asList(pdate2));
        final DefaultConformanceResult pconfResult2 = new DefaultConformanceResult(pcit2, "some second explanation", true);
        preport2.setResults(Arrays.asList(pconfResult2));
        pquality2.setReports(Arrays.asList(preport2));
        
        previous.setDataQualityInfo(Arrays.asList(pquality, pquality2));
        
        final DefaultDataIdentification pdataIdent = new DefaultDataIdentification();
        final DefaultKeywords pkeywords = new DefaultKeywords();
        final InternationalString pkw1 = new SimpleInternationalString("hello");
        final InternationalString pkw2 = new SimpleInternationalString("world");
        pkeywords.setKeywords(Arrays.asList(pkw1, pkw2));
        
        final DefaultKeywords pkeywords2 = new DefaultKeywords();
        final InternationalString pkw21 = new SimpleInternationalString("this");
        final InternationalString pkw22 = new SimpleInternationalString("is");
        pkeywords2.setKeywords(Arrays.asList(pkw21, pkw22));
        
        pdataIdent.setDescriptiveKeywords(Arrays.asList(pkeywords, pkeywords2));
        
        final DefaultLegalConstraints pconstraint1 = new DefaultLegalConstraints();
        pconstraint1.setAccessConstraints(Arrays.asList(Restriction.LICENCE));
        
        final DefaultSecurityConstraints pconstraint2 = new DefaultSecurityConstraints();
        pconstraint2.setUseLimitations(Arrays.asList(new SimpleInternationalString("some limitations")));
        pconstraint2.setClassification(Classification.UNCLASSIFIED);
        
        pdataIdent.setResourceConstraints(Arrays.asList(pconstraint1,pconstraint2));
        
        previous.setIdentificationInfo(Arrays.asList(pdataIdent));
        
        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        
        Object result = reader.readTemplate(root, previous);
        
        
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
        
        
        final DefaultDataQuality quality2 = new DefaultDataQuality(new DefaultScope(ScopeCode.AGGREGATE));
        final DefaultDomainConsistency report2 = new DefaultDomainConsistency();
        final DefaultCitation cit2 = new DefaultCitation("some second title");
        final DefaultCitationDate date2 = new DefaultCitationDate(new Date(11156600000L), DateType.PUBLICATION);
        cit2.setDates(Arrays.asList(date2));
        final DefaultConformanceResult confResult2 = new DefaultConformanceResult(cit2, "some second explanation", true);
        report2.setResults(Arrays.asList(confResult2));
        quality2.setReports(Arrays.asList(report2));
        
        expResult.setDataQualityInfo(Arrays.asList(quality, quality2));
        
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
}
