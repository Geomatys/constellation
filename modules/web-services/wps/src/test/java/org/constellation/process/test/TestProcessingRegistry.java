package org.constellation.process.test;

import java.util.Collections;
import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.constellation.process.test.testprocess.TestDescriptor;
import org.geotoolkit.processing.AbstractProcessingRegistry;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.identification.Identification;

/**
 *
 * @author Zozime Theo
 */
public class TestProcessingRegistry extends AbstractProcessingRegistry {
    
    /** Factory name **/
    public static final String NAME = "test";
    public static final DefaultServiceIdentification IDENTIFICATION;
    
    static {
        IDENTIFICATION = new DefaultServiceIdentification();
        final Identifier id = new DefaultIdentifier(NAME);
        final DefaultCitation citation = new DefaultCitation(NAME);
        citation.setIdentifiers(Collections.singleton(id));
        IDENTIFICATION.setCitation(citation);
    }
    
    public TestProcessingRegistry() {
        super(TestDescriptor.INSTANCE);
    }

    @Override
    public Identification getIdentification() {
        return IDENTIFICATION;
    }
}
