/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.metadata.index.generic;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

// Apache Lucene dependencies
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.SimpleFSDirectory;

// constellation dependencies
import org.apache.lucene.util.Version;
import org.constellation.concurrent.BoundedCompletionService;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.index.AbstractCSWIndexer;
import org.constellation.metadata.io.AbstractMetadataReader;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.utils.Utils;
import org.constellation.util.ReflectionUtilities;

// geotoolkit dependencies
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.util.FileUtilities;

// GeoAPI dependencies
import org.opengis.temporal.Instant;
import org.opengis.temporal.Position;
import org.opengis.util.InternationalString;
import org.opengis.util.LocalName;


/**
 * A Lucene Index Handler for a generic Database.
 * @author Guilhem Legal
 */
public class GenericIndexer extends AbstractCSWIndexer<Object> {
    
    /**
     * The Reader of this lucene index (generic DB mode).
     */
    private final MetadataReader reader;
    
    private static final DateFormat LUCENE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    /**
     * Shared Thread Pool for parallel execution
     */
    private final ExecutorService pool = Executors.newFixedThreadPool(6);

    /**
     * Creates a new Lucene Index into the specified directory with the specified generic database reader.
     * 
     * @param reader A generic reader to request the metadata dataSource.
     * @param configuration  A configuration object containing the directory where the index can write indexation file.
     * @param serviceID The identifier, if there is one, of the index/service.
     * @param additionalQueryable A map of additional queryable element.
     */
    public GenericIndexer(final MetadataReader reader, final Automatic configuration, final String serviceID, final Map<String, List<String>> additionalQueryable) throws IndexingException {
        super(serviceID, configuration.getConfigurationDirectory(), additionalQueryable);
        this.reader = reader;
        if (create) {
            createIndex();
        }
    }

    /**
     * Creates a new Lucene Index into the specified directory with the specified list of object to index.
     *
     * @param configDirectory A directory where the index can write indexation file.
     */
    public GenericIndexer(final List<Object> toIndex, final Map<String, List<String>> additionalQueryable, final File configDirectory,
            final String serviceID, final Analyzer analyzer, final Level logLevel) throws IndexingException {
        super(serviceID, configDirectory, analyzer, additionalQueryable);
        this.logLevel            = logLevel;
        this.reader              = null;
        if (create) {
            createIndex(toIndex);
        }
    }

    /**
     * Creates a new Lucene Index into the specified directory with the specified list of object to index.
     *
     * @param configDirectory A directory where the index can write indexation file.
     */
    public GenericIndexer(final List<Object> toIndex, final Map<String, List<String>> additionalQueryable, final File configDirectory, 
            final String serviceID) throws IndexingException {
        super(serviceID, configDirectory, additionalQueryable);
        this.reader = null;
        if (create) {
            createIndex(toIndex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void createIndex() throws IndexingException {
        LOGGER.log(logLevel, "(light memory) Creating lucene index for Generic database please wait...");
        final long time = System.currentTimeMillis();
        int nbEntries = 0;
        try {
            final IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_32, analyzer);
            final IndexWriter writer     = new IndexWriter(new SimpleFSDirectory(getFileDirectory()), conf);
            final String serviceID       = getServiceID();
            
            // TODO getting the objects list and index avery item in the IndexWriter.
            final List<String> ids = reader.getAllIdentifiers();
            nbEntries = ids.size();
            LOGGER.log( Level.INFO, "{0} metadata to index (light memory mode)", nbEntries);
            for (String id : ids) {
                if (!stopIndexing && !indexationToStop.contains(serviceID)) {
                    final Object entry = reader.getMetadata(id, AbstractMetadataReader.ISO_19115);
                    indexDocument(writer, entry);
                } else {
                     LOGGER.info("Index creation stopped after " + (System.currentTimeMillis() - time) + " ms for service:" + serviceID);
                     stopIndexation(writer, serviceID);
                     return;
                }
            }
            writer.optimize();
            writer.close();

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "IOException while indexing document: {0}", ex.getMessage());
            throw new IndexingException("IOException while indexing documents.", ex);
        } catch (MetadataIoException ex) {
            LOGGER.log(Level.SEVERE, "CstlServiceException while indexing document: {0}", ex.getMessage());
            throw new IndexingException("CstlServiceException while indexing documents.", ex);
        }
        LOGGER.info("Index creation process in " + (System.currentTimeMillis() - time) + " ms\nDocuments indexed: " + nbEntries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createIndex(final List<Object> toIndex) throws IndexingException {
        LOGGER.log(logLevel, "Creating lucene index for Generic database please wait...");
        final long time = System.currentTimeMillis();
        int nbEntries = 0;
        try {
            final IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_32, analyzer);
            final IndexWriter writer     = new IndexWriter(new SimpleFSDirectory(getFileDirectory()), conf);
            final String serviceID       = getServiceID();
            
            nbEntries = toIndex.size();
            for (Object entry : toIndex) {
                if (!stopIndexing && !indexationToStop.contains(serviceID)) {
                    indexDocument(writer, entry);
                } else {
                     LOGGER.info("Index creation stopped after " + (System.currentTimeMillis() - time) + " ms for service:" + serviceID);
                     stopIndexation(writer, serviceID);
                     return;
                }
            }
            writer.optimize();
            writer.close();

        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, IO_SINGLE_MSG, ex);
        }
        LOGGER.log(logLevel, "Index creation process in " + (System.currentTimeMillis() - time) + " ms\n" +
                " documents indexed: " + nbEntries);
    }

    private void stopIndexation(final IndexWriter writer, final String serviceID) throws IOException {
        writer.optimize();
        writer.close();
        FileUtilities.deleteDirectory(getFileDirectory());
        if (indexationToStop.contains(serviceID)) {
            indexationToStop.remove(serviceID);
        }
        if (indexationToStop.isEmpty()) {
            stopIndexing = false;
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
        doc.add(new Field("id", identifier,  Field.Store.YES, Field.Index.NOT_ANALYZED));
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
        return meta instanceof DefaultMetadata;
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
    protected void indexQueryableSet(final Document doc, final Object metadata,final  Map<String, List<String>> queryableSet, final StringBuilder anyText) throws IndexingException {
        final CompletionService<TermValue> cs = new BoundedCompletionService<TermValue>(this.pool, 5);
        for (final String term :queryableSet.keySet()) {
            cs.submit(new Callable<TermValue>() {

                @Override
                public TermValue call() {
                    return new TermValue(term, getValues(metadata, queryableSet.get(term)));
                }
            });
        }

        for (int i = 0; i < queryableSet.size(); i++) {
            try {
                final TermValue values = formatStringValue(cs.take().get());
                doc.add(new Field(values.term,           values.value, Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field(values.term + "_sort", values.value, Field.Store.YES, Field.Index.NOT_ANALYZED));
                if (values.value != null && !values.value.equals(NULL_VALUE) && anyText.indexOf(values.value) == -1) {
                    anyText.append(values.value).append(" ");
                }

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
             String value = values.value;
             if (value.endsWith("z") || value.endsWith("Z")) {
                 value = value.substring(0, value.length() - 1);
             }
             value = value.replace("-", "");
             values.value = value;
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
    protected String getValues(final Object metadata, final List<String> paths) {
        return extractValues(metadata, paths);
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
    public static String extractValues(final Object metadata, final List<String> paths) {
        final StringBuilder response  = new StringBuilder("");
        
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
                    final Object brutValue = ReflectionUtilities.getValuesFromPath(pathID, metadata);
                    final String value     = getStringValue(brutValue);
                    if (value != null && !value.isEmpty() && !value.equals(NULL_VALUE))
                        response.append(value).append(',');
                } else {
                    final Object brutValue = ReflectionUtilities.getConditionalValuesFromPath(pathID, conditionalAttribute, conditionalValue, metadata);
                    final String value     = getStringValue(brutValue);
                    response.append(value).append(',');
                }
            }
        }
        if (response.toString().isEmpty()) {
            response.append(NULL_VALUE);
        } else {
            // we remove the last ','
            response.delete(response.length() - 1, response.length()); 
        }
        return response.toString();
    }


    /**
     * Return a String value from the specified Object.
     * 
     * @param obj
     * @return
     */
    private static String getStringValue(final Object obj) {
        String result = "";
        if (obj == null) {
            return NULL_VALUE;
        } else if (obj instanceof String) {
            result = (String) obj;
        } else if (obj instanceof InternationalString) {
            final InternationalString is = (InternationalString) obj;
            result = is.toString();
        } else if (obj instanceof LocalName) {
            final LocalName ln = (LocalName) obj;
            result = ln.toString();
        } else if (obj instanceof Double || obj instanceof Long) {
            result = obj.toString();
        } else if (obj instanceof java.util.Locale) {
            try {
                result = ((java.util.Locale)obj).getISO3Language();
            } catch (MissingResourceException ex) {
                result = ((java.util.Locale)obj).getLanguage();
            }
        } else if (obj instanceof Collection) {
            final StringBuilder sb = new StringBuilder();
            for (Object o : (Collection) obj) {
                sb.append(getStringValue(o)).append(',');
            }
            result = sb.toString();
            if (result.indexOf(',') != -1)
            result = result.substring(0, result.length() - 1);
            if (result.length() == 0)
                result = NULL_VALUE;
        } else if (obj instanceof org.opengis.util.CodeList) {
            result = ((org.opengis.util.CodeList)obj).name();
        
        } else if (obj instanceof Position) {
            final Position pos = (Position) obj;
            final Date d = pos.getDate();
            if (d != null) {
                synchronized(LUCENE_DATE_FORMAT) {
                    result = LUCENE_DATE_FORMAT.format(d);
                }
            } else {
               result = NULL_VALUE;
            }

        } else if (obj instanceof Instant) {
            final Instant inst = (Instant)obj;
            if (inst.getPosition() != null && inst.getPosition().getDate() != null) {
                synchronized(LUCENE_DATE_FORMAT) {
                    result = LUCENE_DATE_FORMAT.format(inst.getPosition().getDate());
                }
            } else {
                result = NULL_VALUE;
            }
            
        } else if (obj instanceof Date) {
            synchronized (LUCENE_DATE_FORMAT){
                result = LUCENE_DATE_FORMAT.format((Date)obj);
            }
            
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

        public String value;

        public TermValue(String term, String value) {
            this.term  = term;
            this.value = value;
        }
    }
}
