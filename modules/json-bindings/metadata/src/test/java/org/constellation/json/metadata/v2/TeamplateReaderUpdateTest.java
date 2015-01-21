
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
        previous.setHierarchyLevels(Arrays.asList(ScopeCode.APPLICATION, ScopeCode.AGGREGATE));
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
        expResult.setHierarchyLevels(Arrays.asList(ScopeCode.AGGREGATE, ScopeCode.DATASET));
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
        previous.setHierarchyLevels(Arrays.asList(ScopeCode.APPLICATION, ScopeCode.AGGREGATE));
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
        expResult.setHierarchyLevels(Arrays.asList(ScopeCode.AGGREGATE, ScopeCode.DATASET));
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
}
