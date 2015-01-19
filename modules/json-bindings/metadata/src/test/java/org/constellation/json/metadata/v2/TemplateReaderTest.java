
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
import org.apache.sis.metadata.iso.maintenance.DefaultScope;
import org.apache.sis.metadata.iso.quality.DefaultConformanceResult;
import org.apache.sis.metadata.iso.quality.DefaultDataQuality;
import org.apache.sis.metadata.iso.quality.DefaultDomainConsistency;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.json.metadata.binding.RootObj;
import org.constellation.test.utils.MetadataUtilities;
import org.junit.Test;
import org.opengis.metadata.citation.DateType;
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
}
