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
package org.constellation.json.metadata;

import java.util.Set;
import java.util.Locale;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import org.opengis.metadata.citation.Role;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.citation.DefaultResponsibility;
import org.apache.sis.metadata.iso.distribution.DefaultDistribution;
import org.apache.sis.metadata.iso.distribution.DefaultDistributor;
import org.apache.sis.metadata.iso.extent.DefaultExtent;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.metadata.iso.identification.AbstractIdentification;
import org.apache.sis.util.CharSequences;
import org.junit.Test;

import static org.junit.Assert.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;


/**
 * Tests the {@link Template} class.
 *
 * @author Martin Desruisseaux (Geomatys)
 */
public final strictfp class TemplateTest {
    /**
     * Verifies the validity of templates returned by {@link Template#getInstance(String)}.
     *
     * @throws ParseException if a predefined template is not valid.
     */
    @Test
    public void validateInstances() throws ParseException {
        final Set<String> names = Template.getAvailableNames();
        assertTrue("profile_inspire_vector", names.contains("profile_inspire_vector"));
        assertTrue("profile_inspire_raster", names.contains("profile_inspire_raster"));
        for (final String name : names) {
            final Template template = Template.getInstance(name);
            assertNotNull(name, template);
            template.root.validatePath(null);
        }
    }

    /**
     * Creates the metadata object corresponding to the {@link #JSON} string.
     */
    private static DefaultMetadata createMetadata() {
        final AbstractIdentification identification = new AbstractIdentification();
        identification.setCitation(new DefaultCitation("Data \"title\""));
        identification.setExtents(singleton(new DefaultExtent(null,
                new DefaultGeographicBoundingBox(-11.4865013, -4.615912, 43.165467, 49.9990223), null, null)));

        final DefaultDistribution distribution = new DefaultDistribution();
        distribution.setDistributors(asList(
                new DefaultDistributor(new DefaultResponsibility(Role.AUTHOR, null, null)),
                new DefaultDistributor(new DefaultResponsibility(Role.COLLABORATOR, null, null))));

        final DefaultMetadata metadata = new DefaultMetadata();
        metadata.setFileIdentifier("An archive");
        metadata.setLanguage(Locale.ENGLISH);
        metadata.setCharacterSet(StandardCharsets.UTF_16);
        metadata.setMetadataStandardName("ISO19115");
        metadata.setMetadataStandardVersion("2003/Cor.1:2006");
        metadata.setIdentificationInfo(singleton(identification));
        metadata.setDistributionInfo(distribution);
        return metadata;
    }

    /**
     * Test writing of a simple metadata while pruning the empty nodes.
     *
     * @throws IOException if an error occurred while applying the template.
     * @throws URISyntaxException should never occurs.
     */
    @Test
    public void testWritePrune() throws IOException, URISyntaxException {
        final DefaultMetadata metadata = createMetadata();
        final StringBuilder buffer = new StringBuilder(5000);
        Template.getInstance("profile_inspire_vector").write(metadata, buffer, true);
        /*
         * Compares with expected lines.
         */
        final CharSequence[] lines = CharSequences.splitOnEOL(buffer);
        final URI expected = TemplateTest.class.getResource("vector_test.json").toURI();
        int lineNumber = 0;
        for (final String expectedLine : Files.readAllLines(Paths.get(expected), StandardCharsets.UTF_8)) {
            final CharSequence actualLine = lines[lineNumber++];
            if (!expectedLine.equals(actualLine)) {
                fail("Comparison failure at line " + lineNumber + ".\n" +
                     "Expected: " + expectedLine + "\n" +
                     "Actual:   " + actualLine + '\n');
            }
        }
    }
}
