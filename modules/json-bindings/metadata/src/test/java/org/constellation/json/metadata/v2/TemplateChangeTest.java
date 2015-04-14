package org.constellation.json.metadata.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.identification.DefaultKeywords;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.json.metadata.binding.RootObj;
import org.geotoolkit.util.FileUtilities;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.CouplingType;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.util.FactoryException;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class TemplateChangeTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void testChangeTemplate() throws IOException, FactoryException {
        final DefaultMetadata metadata = new DefaultMetadata();
        final DefaultServiceIdentification servIdent = new DefaultServiceIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));
        servIdent.setDescriptiveKeywords(Arrays.asList(keywords));
        servIdent.setCouplingType(CouplingType.LOOSE);
        metadata.setIdentificationInfo(Arrays.asList(servIdent));
        metadata.setMetadataStandardName("some unvalid standard name");
        metadata.setMetadataStandardVersion(" wrong version");
        metadata.setCharacterSet(CharacterSet.UTF_16);
        metadata.setLanguage(Locale.ENGLISH);
        metadata.setHierarchyLevels(Arrays.asList(ScopeCode.FEATURE));
        
        
        final InputStream stream = TemplateWriterTest.class.getResourceAsStream("profile_default_raster3.json");
        final RootObj root = objectMapper.readValue(stream, RootObj.class);
        TemplateWriter writer = new TemplateWriter(MetadataStandard.ISO_19115);
        final RootObj rootFilled = writer.writeTemplate(root, metadata, false, true);
        
        final InputStream resStream = TemplateWriterTest.class.getResourceAsStream("result_change.json");
        String expectedJson = FileUtilities.getStringFromStream(resStream);
        
        File resultFile = File.createTempFile("test", ".json");
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new FileWriter(resultFile), rootFilled);
        
        String resultJson = FileUtilities.getStringFromFile(resultFile);
        
        assertEquals(expectedJson, resultJson);
        
        final TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);
        
        final DefaultMetadata changedMetadata = (DefaultMetadata) reader.readTemplate(rootFilled, metadata);
        
        //System.out.println("changed:\n" + changedMetadata + "\n\n");
        
        final DefaultMetadata newMetadata = (DefaultMetadata) reader.readTemplate(rootFilled, new DefaultMetadata());
        
        //System.out.println("new:\n" + newMetadata + "\n\n");
    }
}
