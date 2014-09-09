package org.constellation.process.provider;

import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.opengis.metadata.Identifier;

import java.util.Collections;

/**
 *
 */
public final class ProviderDescriptorConstant {
    public static final String NAME_CSTl = "constellation";
    public static final DefaultServiceIdentification IDENTIFICATION_CSTL;

    static {
        IDENTIFICATION_CSTL = new DefaultServiceIdentification();
        final Identifier id = new DefaultIdentifier(NAME_CSTl);
        final DefaultCitation citation = new DefaultCitation(NAME_CSTl);
        citation.setIdentifiers(Collections.singleton(id));
        IDENTIFICATION_CSTL.setCitation(citation);
    }
}
