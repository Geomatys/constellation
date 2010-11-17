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
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
import javax.xml.bind.JAXBElement;

// apache Lucene dependencies
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.SimpleFSDirectory;

// constellation dependencies
import org.constellation.concurrent.BoundedCompletionService;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.index.AbstractCSWIndexer;
import org.constellation.metadata.io.AbstractMetadataReader;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.util.ReflectionUtilities;

// geotoolkit dependencies
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.temporal.object.DefaultInstant;
import org.geotoolkit.temporal.object.DefaultPosition;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.ebrim.xml.v250.RegistryObjectType;
import org.geotoolkit.ebrim.xml.v300.IdentifiableType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.util.FileUtilities;

// geoAPI dependencies
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
    private final CSWMetadataReader reader;
    
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    /**
     * A map of getters to avoid to seach the same getters many times.
     */
    private static final Map<String, Method> GETTERS = new HashMap<String, Method>();

    /**
     * Shared Thread Pool for parralele execution
     */
    private ExecutorService pool = Executors.newFixedThreadPool(6);

    /**
     * Creates a new Lucene Index into the specified directory with the specified generic database reader.
     * 
     * @param reader A generic reader to request the metadata datasource.
     * @param configuration  A configuration object containing the directory where the index can write indexation file.
     * @param serviceID The identifier, if there is one, of the index/service.
     */
    public GenericIndexer(CSWMetadataReader reader, Automatic configuration, String serviceID) throws IndexingException {
        super(serviceID, configuration.getConfigurationDirectory(), reader.getAdditionalQueryablePathMap());
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
    public GenericIndexer(List<Object> toIndex, Map<String, List<String>> additionalQueryable, File configDirectory, String serviceID, Analyzer analyzer, Level logLevel) throws IndexingException {
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
    public GenericIndexer(List<Object> toIndex, Map<String, List<String>> additionalQueryable, File configDirectory, String serviceID) throws IndexingException {
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
        IndexWriter writer;
        int nbEntries = 0;
        try {
            writer = new IndexWriter(new SimpleFSDirectory(getFileDirectory()), analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
            final String serviceID = getServiceID();
            
            // TODO getting the objects list and index avery item in the IndexWriter.
            final List<String> ids = reader.getAllIdentifiers();
            nbEntries = ids.size();
            LOGGER.log( Level.INFO, "{0} metadata to index (light memory mode)", nbEntries);
            for (String id : ids) {
                if (!stopIndexing && !indexationToStop.contains(serviceID)) {
                    final Object entry = reader.getMetadata(id, AbstractMetadataReader.ISO_19115, null);
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
        LOGGER.info("Index creation process in " + (System.currentTimeMillis() - time) + " ms\n" +
                " documents indexed: " + nbEntries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createIndex(List<Object> toIndex) throws IndexingException {
        LOGGER.log(logLevel, "Creating lucene index for Generic database please wait...");
        final long time = System.currentTimeMillis();
        IndexWriter writer;
        int nbEntries = 0;
        try {
            writer = new IndexWriter(new SimpleFSDirectory(getFileDirectory()), analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
            final String serviceID = getServiceID();
            
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

    private void stopIndexation(IndexWriter writer, String serviceID) throws IOException {
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
        if (identifier.equals("unknow")) {
            throw new IndexingException("unexpected metadata type.");
        }
        doc.add(new Field("id", identifier,  Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getType(Object metadata) {
        return metadata.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isISO19139(Object meta) {
        return meta instanceof DefaultMetadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isDublinCore(Object meta) {
        return meta instanceof RecordType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEbrim25(Object meta) {
        return meta instanceof RegistryObjectType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEbrim30(Object meta) {
        return meta instanceof IdentifiableType;
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
    private TermValue formatStringValue(TermValue values) {
         if (values.term.equals("date")) {
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
    protected String getIdentifier(Object obj) {
        final String identifier;
        if (obj instanceof DefaultMetadata) {
            identifier = ((DefaultMetadata)obj).getFileIdentifier();
        } else if (obj instanceof RecordType) {
            identifier = ((RecordType)obj).getIdentifier().getContent().get(0);
        } else if (obj instanceof RegistryObjectType) {
            identifier = ((RegistryObjectType)obj).getId();
        } else {
            String type = "null type";
            if (obj != null) {
                type = obj.getClass().getSimpleName();
            }
            LOGGER.log(Level.WARNING, "unexpected metadata type: {0}", type);
            identifier = "unknow";
        }
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getValues(Object metadata, List<String> paths) {
        return extractValues(metadata, paths);
    }

    
    public static String extractValues(Object metadata, List<String> paths) {
        final StringBuilder response  = new StringBuilder("");
        
        if (paths != null) {
            for (String fullPathID: paths) {
               if ((fullPathID.startsWith("ISO 19115:MD_Metadata:") && !(metadata instanceof DefaultMetadata)) ||
                   (fullPathID.startsWith("Catalog Web Service:Record:") && !(metadata instanceof RecordType)) ||
                   (fullPathID.startsWith("Ebrim v2.5:ExtrinsicObject:") && !(metadata instanceof RegistryObjectType))) {
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
                    LOGGER.finer("pathID              : " + pathID               + '\n' +
                                 "conditionalAttribute: " + conditionalAttribute + '\n' +
                                 "conditionalValue    : " + conditionalValue); 
                } else {
                    pathID = fullPathID;
                }
                
                if (conditionalAttribute == null) {
                    final String value = getValuesFromPath(pathID, metadata);
                    if (value != null && !value.isEmpty() && !value.equals(NULL_VALUE))
                        response.append(value).append(',');
                } else {
                    response.append(getConditionalValuesFromPath(pathID, conditionalAttribute, conditionalValue, metadata)).append(',');
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
     * Return a string value extract from the specified object by using the string path specified.
     * example : getValuesFromPath("ISO 19115:MD_Metadata:identificationInfo:citation:title", (MetatadataImpl) obj)
     *           will execute obj.getIdentificationInfo().getTitle() and return the result.
     * The collection attribute are handled :
     * example : if getIdentificationInfo() return a collection of 3 elements the method will execute
     *           getIdentificationInfo().get(0).getTitle()
     *           getIdentificationInfo().get(1).getTitle()
     *           getIdentificationInfo().get(2).getTitle()
     *           The result will be : title1,title2,title3
     * 
     * 
     * @param pathID   A String path using MDWeb pattern.
     * @param metadata An Object.
     * @return A String value.
     */
    private static String getValuesFromPath(String pathID, Object metadata) {
        String result = "";
        if ((pathID.startsWith("ISO 19115:MD_Metadata:") && metadata instanceof DefaultMetadata) ||
            (pathID.startsWith("Catalog Web Service:Record:") && metadata instanceof RecordType) ||
            (pathID.startsWith("Ebrim v2.5:ExtrinsicObject:") && metadata instanceof RegistryObjectType)) {
            
            // we remove the prefix path part
            if (pathID.startsWith("ISO 19115:MD_Metadata:")) {
                pathID = pathID.substring(22);
            } else if (pathID.startsWith("Catalog Web Service:Record:")) {
                pathID = pathID.substring(27);
            } else if (pathID.startsWith("Ebrim v2.5:ExtrinsicObject:")) {
                pathID = pathID.substring(27);
            }
            
            //for each part of the path we execute a (many) getter
            while (!pathID.isEmpty()) {
                String attributeName;
                if (pathID.indexOf(':') != -1) {
                    attributeName = pathID.substring(0, pathID.indexOf(':'));
                    pathID        = pathID.substring(pathID.indexOf(':') + 1);
                } else {
                    attributeName = pathID;
                    pathID = "";
                }
                
                if (metadata instanceof Collection) {
                    final List<Object> tmp = new ArrayList<Object>();
                    for (Object subMeta: (Collection) metadata) {
                        final Object obj = getAttributeValue(subMeta, attributeName);
                        if (obj instanceof Collection) {
                            for (Object o : (Collection)obj) {
                                if (o != null) tmp.add(o);
                            }
                        } else {
                            if (obj != null) tmp.add(obj);
                        }
                    }
                    metadata = tmp;
                } else {
                    metadata = getAttributeValue(metadata, attributeName);
                }
            } 
            
            result = getStringValue(metadata);
        }
        return result;
    }
    
    /**
     * Return a String value from the specified Object.
     * 
     * @param obj
     * @return
     */
    private static String getStringValue(Object obj) {
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
        } else if (obj instanceof Double) {
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
        
        } else if (obj instanceof DefaultPosition) {
            final DefaultPosition pos = (DefaultPosition) obj;
            synchronized(DATE_FORMAT) {
                result = DATE_FORMAT.format(pos.getDate());
            }
            
        } else if (obj instanceof TimePositionType) {
            final TimePositionType pos = (TimePositionType) obj;
            final Date d = pos.getDate();
            if (d != null) {
                synchronized(DATE_FORMAT) {
                    result = DATE_FORMAT.format(d);
                }
            } else {
               result = NULL_VALUE;
            }

        } else if (obj instanceof DefaultInstant) {
            final DefaultInstant inst = (DefaultInstant)obj;
            if (inst.getPosition() != null && inst.getPosition().getDate() != null) {
                synchronized(DATE_FORMAT) {
                    result = DATE_FORMAT.format(inst.getPosition().getDate());
                }
            } else {
                result = NULL_VALUE;
            }
            
        } else if (obj instanceof Date) {
            synchronized (DATE_FORMAT){
                result = DATE_FORMAT.format((Date)obj);
            }
            
        } else {
            throw new IllegalArgumentException("this type is unexpected: " + obj.getClass().getSimpleName());
        }
        return result;
    }
    
    /**
     * TODO 
     * 
     * @param pathID
     * @param conditionalPathID
     * @param conditionalValue
     * @param metadata
     * @return
     */
    private static String getConditionalValuesFromPath(String pathID, String conditionalAttribute, String conditionalValue, Object metadata) {
        String result = "";
        if (pathID.startsWith("ISO 19115:MD_Metadata:")) {
             // we remove the prefix path part 
            pathID = pathID.substring(22);
            
            //for each part of the path we execute a (many) getter
            while (!pathID.isEmpty()) {
                String attributeName;
                if (pathID.indexOf(':') != -1) {
                    attributeName = pathID.substring(0, pathID.indexOf(':'));
                    pathID        = pathID.substring(pathID.indexOf(':') + 1);
                } else {
                    attributeName = pathID;
                    pathID = "";
                }
                
                if (metadata instanceof Collection) {
                    final List<Object> tmp = new ArrayList<Object>();
                    if (pathID.isEmpty()) {
                        for (Object subMeta: (Collection)metadata) {
                            if (matchCondition(subMeta, conditionalAttribute, conditionalValue)) {
                                tmp.add(getAttributeValue(subMeta, attributeName));
                            } 
                        }
                    } else {
                        for (Object subMeta: (Collection)metadata) {
                            final Object obj = getAttributeValue(subMeta, attributeName);
                            if (obj instanceof Collection) {
                                for (Object o : (Collection)obj) {
                                    if (o != null) tmp.add(o);
                                }
                            } else {
                                if (obj != null) tmp.add(obj);
                            }
                        }
                    }
                    
                    if (tmp.size() == 1) metadata = tmp.get(0); 
                    else metadata = tmp;
                    
                } else {
                    if (pathID.isEmpty()) {
                        if (matchCondition(metadata, conditionalAttribute, conditionalValue)) {
                            metadata = getAttributeValue(metadata, attributeName);
                        } else {
                            metadata = null;
                        }
                        
                    } else metadata = getAttributeValue(metadata, attributeName);
                }
            } 
            result = getStringValue(metadata);
        }
        return result;
    }
    
    /**
     * 
     * @param metadata
     * @param conditionalAttribute
     * @param conditionalValue
     * @return
     */
    private static boolean matchCondition(Object metadata, String conditionalAttribute, String conditionalValue) {
        final Object conditionalObj = getAttributeValue(metadata, conditionalAttribute);
        LOGGER.finer("contionalObj: "     + getStringValue(conditionalObj) + '\n' +
                     "conditionalValue: " + conditionalValue               + '\n' +
                     "match? "            + conditionalValue.equals(getStringValue(conditionalObj)));
        return conditionalValue.equalsIgnoreCase(getStringValue(conditionalObj));
    }
    
    /**
     * Call a get method on the specified object named get'AttributeName'() and return the result.
     * 
     * @param object An object.
     * @param attributeName The name of the attribute that you want the value.
     * @return
     */
    private static Object getAttributeValue(Object object, String attributeName) {
        Object result = null;
        int ordinal   = -1;
        if (attributeName.indexOf('[') != -1){
            final String tmp    = attributeName.substring(attributeName.indexOf('[') + 1, attributeName.length() - 1);
            attributeName = attributeName.substring(0, attributeName.indexOf('['));
            try {
                ordinal = Integer.parseInt(tmp);
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "Unable to parse the ordinal {0}", tmp);
            }
        }
        if (object != null) {
            if (object instanceof JAXBElement) {
               object = ((JAXBElement)object).getValue();
            }
            final String getterId = object.getClass().getName() + ':' + attributeName;
            Method getter         = GETTERS.get(getterId);
            if (getter != null) {
                result = ReflectionUtilities.invokeMethod(object, getter);
            } else {
                if (attributeName.equalsIgnoreCase("referenceSystemIdentifier")) {
                    attributeName = "name";
                }
                getter = ReflectionUtilities.getGetterFromName(attributeName, object.getClass());
                if (getter != null) {
                    GETTERS.put(object.getClass().getName() + ':' + attributeName, getter);
                    result = ReflectionUtilities.invokeMethod(object, getter);
                } else {
                    LOGGER.finer("No getter have been found for attribute " + attributeName + " in the class " + object.getClass().getName());
                }
            }
        }
        if (result instanceof JAXBElement) {
            result = ((JAXBElement)result).getValue();
        }
        if (ordinal != -1 && result instanceof Collection) {
            final Collection c = (Collection) result;
            final Iterator t   = c.iterator();
            int i = 0;
            while (t.hasNext()) {
                result = t.next();
                if (i == ordinal) return result;
                i++;
            }

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
