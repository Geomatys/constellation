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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.opengis.metadata.citation.Role;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.citation.DefaultIndividual;
import org.apache.sis.metadata.iso.citation.DefaultResponsibility;
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

        identification.setPointOfContacts(asList(
                new DefaultResponsibility(Role.AUTHOR,       null, new DefaultIndividual("An author",      null, null)),
                new DefaultResponsibility(Role.COLLABORATOR, null, new DefaultIndividual("A collaborator", null, null))));

        final DefaultMetadata metadata = new DefaultMetadata();
        metadata.setFileIdentifier("An archive");
        metadata.setLanguage(Locale.ENGLISH);
        metadata.setCharacterSet(StandardCharsets.UTF_16);
        metadata.setMetadataStandardName("ISO19115");
        metadata.setMetadataStandardVersion("2003/Cor.1:2006");
        metadata.setIdentificationInfo(singleton(identification));
        return metadata;
    }

    /**
     * Asserts that a JSON output is equals to the expected one.
     *
     * @param  expectedFile The filename (without directory) of the test resource containing the expected JSON content.
     * @param  actual The JSON content produced by {@link Template}.
     * @throws IOException if an error occurred while reading the expected JSON file.
     */
    private static void assertJsonEquals(final String expectedFile, final CharSequence actual) throws IOException {
        int lineNumber = 0;
        final CharSequence[] lines = CharSequences.splitOnEOL(actual);
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(
                Template.class.getResourceAsStream(expectedFile), "UTF-8")))
        {
            String expectedLine;
            while ((expectedLine = in.readLine()) != null) {
                final CharSequence actualLine = lines[lineNumber++];
                if (!expectedLine.equals(actualLine)) {
                    fail("Comparison failure at line " + lineNumber + ".\n" +
                         "Expected: " + expectedLine + "\n" +
                         "Actual:   " + actualLine + '\n');
                }
            }
        }

    }

    /**
     * Test writing of a simple metadata while pruning the empty nodes.
     *
     * @throws IOException if an error occurred while applying the template.
     */
    @Test
    public void testWritePrune() throws IOException {
        final DefaultMetadata metadata = createMetadata();
        final StringBuilder buffer = new StringBuilder(5000);
        Template.getInstance("profile_inspire_vector").write(metadata, buffer, true);
        assertJsonEquals("vector_prune.json", buffer);
    }

    /**
     * Test writing of a simple metadata without pruning the empty nodes.
     *
     * @throws IOException if an error occurred while applying the template.
     */
    @Test
    public void testWriteFull() throws IOException {
        final DefaultMetadata metadata = createMetadata();
        final StringBuilder buffer = new StringBuilder(32000);
        Template.getInstance("profile_inspire_vector").write(metadata, buffer, false);
        assertJsonEquals("vector_test.json", buffer);
    }
}
