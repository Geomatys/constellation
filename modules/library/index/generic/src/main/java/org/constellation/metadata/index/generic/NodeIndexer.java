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

package org.constellation.metadata.index.generic;

// J2SE dependencies

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.constellation.metadata.index.AbstractCSWIndexer;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.metadata.io.MetadataType;
import org.constellation.metadata.utils.Utils;
import org.constellation.util.NodeUtilities;
import org.geotoolkit.lucene.IndexingException;
import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

// Apache Lucene dependencies
// constellation dependencies
// geotoolkit dependencies
// GeoAPI dependencies


/**
 * A Lucene Index Handler for a generic Database.
 * @author Guilhem Legal
 */
public class NodeIndexer extends AbstractCSWIndexer<Node> {

    /**
     * The Reader of this lucene index (generic DB mode).
     */
    protected final MetadataReader reader;

    /**
     * Creates a new Lucene Index into the specified directory with the specified generic database reader.
     *
     * @param reader A node reader to request the metadata dataSource.
     * @param configurationDirectory The directory where the index can write indexation file.
     * @param indexID The identifier, if there is one, of the index/service.
     * @param additionalQueryable A map of additional queryable element.
     * @param create {@code true} if the index need to be created.
     *
     * @throws org.geotoolkit.lucene.IndexingException If an erro roccurs during the index creation.
     */
    public NodeIndexer(final MetadataReader reader, final File configurationDirectory, final String indexID,
            final Map<String, List<String>> additionalQueryable, final boolean create) throws IndexingException {
        super(indexID, configurationDirectory, additionalQueryable);
        this.reader = reader;
        if (create && needCreation()) {
            createIndex();
        }
    }

    /**
     * Creates a new Lucene Index into the specified directory with the specified list of object to index.
     *
     * @param toIndex A list of Object
     * @param additionalQueryable A Map of additionable queryable to add to the index (name - List of Xpath)
     * @param configDirectory A directory where the index can write indexation file.
     * @param indexID The identifier, if there is one, of the index.
     * @param analyzer The lucene analyzer used.
     * @param logLevel A log level for info information.
     * @param create {@code true} if the index need to be created.
     *
     * @throws org.geotoolkit.lucene.IndexingException If an erro roccurs during the index creation.
     */
    public NodeIndexer(final List<Node> toIndex, final Map<String, List<String>> additionalQueryable, final File configDirectory,
            final String indexID, final Analyzer analyzer, final Level logLevel, final boolean create) throws IndexingException {
        super(indexID, configDirectory, analyzer, additionalQueryable);
        this.logLevel            = logLevel;
        this.reader              = null;
        if (create && needCreation()) {
            createIndex(toIndex);
        }
    }

    /**
     * Creates a new Lucene Index into the specified directory with the specified list of object to index.
     *
     * @param toIndex A list of Object
     * @param additionalQueryable A Map of additionable queryable to add to the index (name - List of Xpath)
     * @param configDirectory A directory where the index can write indexation file.
     * @param indexID The identifier, if there is one, of the index.
     * @param create {@code true} if the index need to be created.
     *
     * @throws org.geotoolkit.lucene.IndexingException If an erro roccurs during the index creation.
     */
    public NodeIndexer(final List<Node> toIndex, final Map<String, List<String>> additionalQueryable, final File configDirectory,
            final String indexID, final boolean create) throws IndexingException {
        super(indexID, configDirectory, additionalQueryable);
        this.reader = null;
        if (create && needCreation()) {
            createIndex(toIndex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getAllIdentifiers() throws IndexingException {
        try {
            return reader.getAllIdentifiers();
        } catch (MetadataIoException ex) {
            throw new IndexingException("Metadata_IOException while reading all identifiers", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterator<String> getIdentifierIterator() throws IndexingException {
        try {
            return reader.getIdentifierIterator();
        } catch (MetadataIoException ex) {
            throw new IndexingException("Metadata_IOException while reading identifier iterator", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node getEntry(final String identifier) throws IndexingException {
        try {
            return (Node) reader.getMetadata(identifier, MetadataType.NATIVE);
        } catch (MetadataIoException ex) {
            throw new IndexingException("Metadata_IOException while reading entry for:" + identifier, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void indexSpecialField(final Node metadata, final Document doc) throws IndexingException {
        final String identifier = getIdentifier(metadata);
        if ("unknow".equals(identifier)) {
            throw new IndexingException("unexpected metadata type.");
        }
        doc.add(new Field("id", identifier,  ID_TYPE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getType(final Node metadata) {
        return metadata.getLocalName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isISO19139(final Node meta) {
        return "MD_Metadata".equals(meta.getLocalName()) ||
               "MI_Metadata".equals(meta.getLocalName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isDublinCore(final Node meta) {
        return "Record".equals(meta.getLocalName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEbrim25(final Node meta) {
        // TODO list rootElement
        return "RegistryObject".equals(meta.getLocalName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEbrim30(final Node meta) {
        // TODO list rootElement
        return "Identifiable".equals(meta.getLocalName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isFeatureCatalogue(Node meta) {
        return "FC_FeatureCatalogue".equals(meta.getLocalName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void indexQueryableSet(final Document doc, final Node metadata, final  Map<String, List<String>> queryableSet, final StringBuilder anyText) throws IndexingException {
        for (final String term : queryableSet.keySet()) {
            final TermValue tm = new TermValue(term, NodeUtilities.extractValues(metadata, queryableSet.get(term)));

            final NodeIndexer.TermValue values = formatStringValue(tm);
            indexFields(values.value, values.term, anyText, doc);
        }
    }

    /**
     * Format the value part in case of a "date" term.
     * @param values
     * @return
     */
    private TermValue formatStringValue(final TermValue values) {
         if ("date".equals(values.term)) {
             final List<Object> newValues = new ArrayList<>();
             for (Object value : values.value) {
                 if (value instanceof String) {
                     String stringValue = (String) value;
                     if (stringValue.endsWith("z") || stringValue.endsWith("Z")) {
                         stringValue = stringValue.substring(0, stringValue.length() - 1);
                     }
                     if (stringValue != null) {
                        stringValue = stringValue.replace("-", "");
                        //add time if there is no
                        if (stringValue.length() == 8) {
                            stringValue = stringValue + "000000";
                        }
                        value = stringValue;
                     }
                 }
                newValues.add(value);
             }
             values.value = newValues;
         }
         return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIdentifier(final Node metadata) {
        return Utils.findIdentifier(metadata);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    protected String getValues(final Node metadata, final List<String> paths) {
        final List<Object> values =  NodeUtilities.extractValues(metadata, paths);
        final StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append(value).append(',');
        }
        if (!sb.toString().isEmpty()) {
            // we remove the last ','
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    @Override
    protected Iterator<Node> getEntryIterator() throws IndexingException {
        try {
            return (Iterator<Node>) reader.getEntryIterator();
        } catch (MetadataIoException ex) {
            throw new IndexingException("Error while getting entry iterator", ex);
        }
    }

    @Override
    protected boolean useEntryIterator() {
        return reader.useEntryIterator();
    }

    @Override
    public void destroy() {
        LOGGER.log(logLevel, "shutting down Node indexer");
        super.destroy();
    }

    private static class TermValue {
        public String term;

        public List<Object> value;

        public TermValue(String term, List<Object> value) {
            this.term  = term;
            this.value = value;
        }
    }
}
