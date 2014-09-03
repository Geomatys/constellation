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
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
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
import org.apache.sis.metadata.iso.identification.DefaultKeywords;
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
        assertTrue("profile_import",             names.contains("profile_import"));
        assertTrue("profile_inspire_vector",     names.contains("profile_inspire_vector"));
        assertTrue("profile_inspire_raster",     names.contains("profile_inspire_raster"));
        assertTrue("profile_sensorml_component", names.contains("profile_sensorml_component"));
        assertTrue("profile_sensorml_system",    names.contains("profile_sensorml_system"));
        for (final String name : names) {
            final Template template = Template.getInstance(name);
            assertNotNull(name, template);
            assertEquals(name, template.root.validatePath(null), template.depth);
        }
    }

    /**
     * Creates the metadata object corresponding to the {@link #JSON} string.
     */
    private static DefaultMetadata createMetadata() {
        final AbstractIdentification identification = new AbstractIdentification();
        identification.setCitation(new DefaultCitation("Data \"title\""));
        identification.setExtents(singleton(new DefaultExtent(null,
                new DefaultGeographicBoundingBox(-11.4865013, -4.615912, 43.165467, 49.9990223), null, null)
        ));
        identification.setDescriptiveKeywords(asList(
                new DefaultKeywords("keyword 1", "keyword 2", "keyword 3"),
                new DefaultKeywords("keyword 4", "keyword 5")
        ));
        identification.setPointOfContacts(asList(
                new DefaultResponsibility(Role.AUTHOR,       null, new DefaultIndividual("An author",      null, null)),
                new DefaultResponsibility(Role.COLLABORATOR, null, new DefaultIndividual("A collaborator", null, null))
        ));

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
     * Returns a reader for the test resource of the given name.
     */
    private static BufferedReader open(final String file) throws IOException {
        return new BufferedReader(new InputStreamReader(Template.class.getResourceAsStream(file), "UTF-8"));
    }

    /**
     * Returns all lines read from the test resource of the given name.
     */
    private static List<String> readAllLines(final String file) throws IOException {
        final List<String> lines = new ArrayList<>();
        try (final BufferedReader in = open(file)) {
            String line;
            while ((line = in.readLine()) != null) {
                assertTrue(lines.add(line));
            }
        }
        return lines;
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
        try (final BufferedReader in = open(expectedFile)) {
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
        final StringBuilder buffer = new StringBuilder(10000);
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
        final StringBuilder buffer = new StringBuilder(40000);
        Template.getInstance("profile_inspire_vector").write(metadata, buffer, false);
        assertJsonEquals("vector_test.json", buffer);
    }

    /**
     * Tests {@link Template#read(Iterable, Object)} when storing in an initially empty {@link DefaultMetadata}.
     *
     * @throws IOException if an error occurred while reading the test JSON file.
     */
    @Test
    public void testRead() throws IOException {
        final DefaultMetadata expected = createMetadata();
        final DefaultMetadata metadata = new DefaultMetadata();
        Template.getInstance("profile_inspire_vector").read(readAllLines("vector_prune.json"), metadata, true);
        metadata.prune();
// TODO assertEquals(expected, metadata);
    }
}
