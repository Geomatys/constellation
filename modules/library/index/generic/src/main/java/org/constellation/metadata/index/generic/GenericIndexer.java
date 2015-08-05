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
import org.constellation.concurrent.BoundedCompletionService;
import org.constellation.metadata.index.AbstractCSWIndexer;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.metadata.io.MetadataType;
import org.constellation.metadata.utils.Utils;
import org.constellation.util.ReflectionUtilities;
import org.constellation.util.Util;
import org.constellation.util.XpathUtils;
import org.geotoolkit.lucene.IndexingException;
import org.opengis.metadata.Metadata;
import org.opengis.temporal.Instant;
import org.opengis.util.InternationalString;
import org.opengis.util.LocalName;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.geotoolkit.gml.xml.AbstractTimePosition;

// Apache Lucene dependencies
// constellation dependencies
// geotoolkit dependencies
// GeoAPI dependencies


/**
 * A Lucene Index Handler for a generic Database.
 * @author Guilhem Legal
 */
public class GenericIndexer extends AbstractCSWIndexer<Object> {

    /**
     * The Reader of this lucene index (generic DB mode).
     */
    private final MetadataReader reader;

    /**
     * Shared Thread Pool for parallel execution
     */
    private final ExecutorService pool = Executors.newFixedThreadPool(6);

    /**
     * Creates a new Lucene Index into the specified directory with the specified generic database reader.
     *
     * @param reader A generic reader to request the metadata dataSource.
     * @param configurationDirectory The directory where the index can write indexation file.
     * @param indexID The identifier, if there is one, of the index.
     * @param additionalQueryable A map of additional queryable element.
     * @param create {@code true} if the index need to be created.
     *
     * @throws org.geotoolkit.lucene.IndexingException If an erro roccurs during the index creation.
     */
    public GenericIndexer(final MetadataReader reader, final File configurationDirectory, final String indexID,
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
    public GenericIndexer(final List<Object> toIndex, final Map<String, List<String>> additionalQueryable, final File configDirectory,
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
    public GenericIndexer(final List<Object> toIndex, final Map<String, List<String>> additionalQueryable, final File configDirectory,
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
    protected Object getEntry(final String identifier) throws IndexingException {
        try {
            return reader.getMetadata(identifier, MetadataType.ISO_19115);
        } catch (MetadataIoException ex) {
            throw new IndexingException("Metadata_IOException while reading entry for:" + identifier, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void indexSpecialField(final Object metadata, final Document doc) throws IndexingException {
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
    protected String getType(final Object metadata) {
        return metadata.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isISO19139(final Object meta) {
        return meta instanceof Metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isDublinCore(final Object meta) {
        return ReflectionUtilities.instanceOf("org.geotoolkit.csw.xml.v202.RecordType", meta.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEbrim25(final Object meta) {
        return ReflectionUtilities.instanceOf("org.geotoolkit.ebrim.xml.v250.RegistryObjectType", meta.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEbrim30(final Object meta) {
        return ReflectionUtilities.instanceOf("org.geotoolkit.ebrim.xml.v300.IdentifiableType", meta.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isFeatureCatalogue(Object meta) {
        return ReflectionUtilities.instanceOf("org.geotoolkit.feature.catalog.FeatureCatalogueImpl", meta.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void indexQueryableSet(final Document doc, final Object metadata,final  Map<String, List<String>> queryableSet, final StringBuilder anyText) throws IndexingException {
        final CompletionService<TermValue> cs = new BoundedCompletionService<>(this.pool, 5);
        for (final String term :queryableSet.keySet()) {
            cs.submit(new Callable<TermValue>() {

                @Override
                public TermValue call() {
                    final List<String> paths = XpathUtils.xpathToMDPath(queryableSet.get(term));
                    return new TermValue(term, extractValues(metadata, paths));
                }
            });
        }

        for (int i = 0; i < queryableSet.size(); i++) {
            try {
                final TermValue values = formatStringValue(cs.take().get());
                indexFields(values.value, values.term, anyText, doc);

            } catch (InterruptedException ex) {
               LOGGER.log(Level.WARNING, "InterruptedException in parralele create document:\n{0}", ex.getMessage());
            } catch (ExecutionException ex) {
               LOGGER.log(Level.WARNING, "ExecutionException in parralele create document:\n" + ex.getMessage(), ex);
            }
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
    protected String getIdentifier(final Object obj) {
        return Utils.findIdentifier(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    protected String getValues(final Object metadata, final List<String> paths) {
        final List<String> mdpaths = XpathUtils.xpathToMDPath(paths);
        final List<Object> values =  extractValues(metadata, mdpaths);
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

    /**
     * Extract the String values denoted by the specified paths
     * and return the values as a String values1,values2,....
     * if there is no values corresponding to the paths the method return "null" (the string)
     *
     * @param metadata
     * @param paths
     * @return
     */
    public static List<Object> extractValues(final Object metadata, final List<String> paths) {
        final List<Object> response  = new ArrayList<>();

        if (paths != null) {
            for (String fullPathID: paths) {
               if (!ReflectionUtilities.pathMatchObjectType(metadata, fullPathID)) {
                   continue;
               }
                String pathID;
                String conditionalAttribute = null;
                String conditionalValue     = null;

                // if the path ID contains a # we have a conditional value (codeList element) next to the searched value.
                final int separator = fullPathID.indexOf('#');
                if (separator != -1) {
                    pathID               = fullPathID.substring(0, separator);
                    conditionalAttribute = fullPathID.substring(separator + 1, fullPathID.indexOf('='));
                    conditionalValue     = fullPathID.substring(fullPathID.indexOf('=') + 1);
                    int nextSeparator    = conditionalValue.indexOf(':');
                    if (nextSeparator == -1) {
                        throw new IllegalArgumentException("A conditionnal path must be in the form ....:attribute#attibuteconditional=value:otherattribute");
                    } else {
                        pathID = pathID + conditionalValue.substring(nextSeparator);
                        conditionalValue = conditionalValue.substring(0, nextSeparator);
                    }
                    LOGGER.finer("pathID              : " + pathID               + '\n' +
                                 "conditionalAttribute: " + conditionalAttribute + '\n' +
                                 "conditionalValue    : " + conditionalValue);
                } else {
                    pathID = fullPathID;
                }

                if (conditionalAttribute == null) {
                    final Object brutValue   = ReflectionUtilities.getValuesFromPath(pathID, metadata);
                    final List<Object> value = getStringValue(brutValue);
                    if (value != null && !value.isEmpty() && !value.equals(Arrays.asList(NULL_VALUE))) {
                        response.addAll(value);
                    }
                } else {
                    final Object brutValue   = ReflectionUtilities.getConditionalValuesFromPath(pathID, conditionalAttribute, conditionalValue, metadata);
                    final List<Object> value = getStringValue(brutValue);
                    response.addAll(value);
                }
            }
        }
        if (response.isEmpty()) {
            response.add(NULL_VALUE);
        }
        return response;
    }


    /**
     * Return a String value from the specified Object.
     * Let the number object as Number
     *
     * @param obj
     * @return
     */
    private static List<Object> getStringValue(final Object obj) {
        final List<Object> result = new ArrayList<>();
        if (obj == null) {
            result.add(NULL_VALUE);
        } else if (obj instanceof String) {
            result.add(obj);
        } else if (obj instanceof Number) {
            result.add(obj);
        } else if (obj instanceof InternationalString) {
            final InternationalString is = (InternationalString) obj;
            result.add(is.toString());
        } else if (obj instanceof LocalName) {
            final LocalName ln = (LocalName) obj;
            result.add(ln.toString());
        } else if (obj instanceof Double || obj instanceof Long) {
            result.add(obj.toString());
        } else if (obj instanceof java.util.Locale) {
            try {
                result.add(((java.util.Locale)obj).getISO3Language());
            } catch (MissingResourceException ex) {
                result.add(((java.util.Locale)obj).getLanguage());
            }
        } else if (obj instanceof Collection) {
            for (Object o : (Collection) obj) {
                result.addAll(getStringValue(o));
            }
            if (result.isEmpty()) {
                result.add(NULL_VALUE);
            }
        } else if (obj instanceof org.opengis.util.CodeList) {
            result.add(((org.opengis.util.CodeList)obj).name());

        } else if (obj instanceof AbstractTimePosition) {
            final AbstractTimePosition pos = (AbstractTimePosition) obj;
            final Date d = pos.getDate();
            if (d != null) {
                synchronized(Util.LUCENE_DATE_FORMAT) {
                    result.add(Util.LUCENE_DATE_FORMAT.format(d));
                }
            } else {
               result.add(NULL_VALUE);
            }

        } else if (obj instanceof Instant) {
            final Instant inst = (Instant)obj;
            if (inst != null && inst.getDate() != null) {
                synchronized(Util.LUCENE_DATE_FORMAT) {
                    result.add(Util.LUCENE_DATE_FORMAT.format(inst.getDate()));
                }
            } else {
                result.add(NULL_VALUE);
            }
        } else if (obj instanceof Date) {
            synchronized (Util.LUCENE_DATE_FORMAT){
                result.add(Util.LUCENE_DATE_FORMAT.format((Date)obj));
            }

        } else if (obj instanceof Enum) {
            result.add(((Enum)obj).name());

        } else {
            throw new IllegalArgumentException("this type is unexpected: " + obj.getClass().getSimpleName());
        }
        return result;
    }

    @Override
    public void destroy() {
        LOGGER.info("shutting down generic indexer");
        super.destroy();
        pool.shutdown();
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
