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
import java.util.StringTokenizer;
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
import org.constellation.metadata.io.AbstractMetadataReader;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.util.ReflectionUtilities;
import static org.constellation.metadata.CSWQueryable.*;

// geotoolkit dependencies
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.temporal.object.DefaultInstant;
import org.geotoolkit.temporal.object.DefaultPosition;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.ebrim.xml.v250.RegistryObjectType;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.resources.NIOUtilities;

// geoAPI dependencies
import org.opengis.util.InternationalString;


/**
 * A Lucene Index Handler for a generic Database.
 * @author Guilhem Legal
 */
public class GenericIndexer extends AbstractIndexer<Object> {
    
    /**
     * The Reader of this lucene index (generic DB mode).
     */
    private final CSWMetadataReader reader;
    
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    private static final Map<String, Method> GETTERS = new HashMap<String, Method>();

    private final Map<String, List<String>> additionalQueryable;
    /**
     * Shared Thread Pool for parralele execution
     */
    private ExecutorService pool = Executors.newFixedThreadPool(6);

    private static final String NULL_VALUE = "null";

    private static final String NOT_SPATIALLY_INDEXABLE = "unable to spatially index metadata: ";

    /**
     * Creates a new Lucene Index into the specified directory with the specified generic database reader.
     * 
     * @param reader A generic reader for read the metadata database.
     * @param configDirectory A directory where the index can write indexation file. 
     */
    public GenericIndexer(CSWMetadataReader reader, Automatic configuration, String serviceID) throws IndexingException {
        super(serviceID, configuration.getConfigurationDirectory());
        this.reader = reader;
        if (reader != null)
            additionalQueryable = reader.getAdditionalQueryablePathMap();
        else
            additionalQueryable = null;
        if (create)
            createIndex();
    }

    /**
     * Creates a new Lucene Index into the specified directory with the specified list of object to index.
     *
     * @param configDirectory A directory where the index can write indexation file.
     */
    public GenericIndexer(List<? extends Object> toIndex, Map<String, List<String>> additionalQueryable, File configDirectory, String serviceID, Analyzer analyzer) throws IndexingException {
        super(serviceID, configDirectory, analyzer);
        this.reader = null;
        this.additionalQueryable = additionalQueryable;
        if (create)
            createIndex(toIndex);
    }

    /**
     * Creates a new Lucene Index into the specified directory with the specified list of object to index.
     *
     * @param configDirectory A directory where the index can write indexation file.
     */
    public GenericIndexer(List<? extends Object> toIndex, Map<String, List<String>> additionalQueryable, File configDirectory, String serviceID, Analyzer analyzer, Level logLevel) throws IndexingException {
        super(serviceID, configDirectory, analyzer);
        this.logLevel            = logLevel;
        this.reader              = null;
        this.additionalQueryable = additionalQueryable;
        if (create)
            createIndex(toIndex);
    }

    /**
     * Creates a new Lucene Index into the specified directory with the specified list of object to index.
     *
     * @param configDirectory A directory where the index can write indexation file.
     */
    public GenericIndexer(List<? extends Object> toIndex, Map<String, List<String>> additionalQueryable, File configDirectory, String serviceID) throws IndexingException {
        super(serviceID, configDirectory);
        this.reader = null;
        this.additionalQueryable = additionalQueryable;
        if (create)
            createIndex(toIndex);
    }
    
    /** 
     * Create a new Index from a generic database.
     * 
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
            LOGGER.info( nbEntries + " metadata to index (light memory mode)");
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
            LOGGER.severe("IOException while indexing document: " + ex.getMessage());
            throw new IndexingException("IOException while indexing documents.", ex);
        } catch (MetadataIoException ex) {
            LOGGER.severe("CstlServiceException while indexing document: " + ex.getMessage());
            throw new IndexingException("CstlServiceException while indexing documents.", ex);
        }
        LOGGER.info("Index creation process in " + (System.currentTimeMillis() - time) + " ms" + '\n' +
                " documents indexed: " + nbEntries);
    }

    /**
     * Create a new Index from a generic database.
     *
     */
    @Override
    public void createIndex(List<? extends Object> toIndex) throws IndexingException {
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
            LOGGER.severe(IO_SINGLE_MSG + ex.getMessage());
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        LOGGER.log(logLevel, "Index creation process in " + (System.currentTimeMillis() - time) + " ms" + '\n' +
                " documents indexed: " + nbEntries);
    }

    private void stopIndexation(IndexWriter writer, String serviceID) throws IOException {
        writer.optimize();
        writer.close();
        NIOUtilities.deleteDirectory(getFileDirectory());
        if (indexationToStop.contains(serviceID)) {
            indexationToStop.remove(serviceID);
        }
        if (indexationToStop.size() == 0) {
            stopIndexing = false;
        }
    }
    
    /**
     * This method add to index of lucene a new document based on a geotoolkit Metadata object.
     * (implements AbstractIndex.indexDocument() )
     * 
     * @param writer A lucene Index Writer.
     * @param meta A geotoolkit Metadata object.
     */
    @Override
    public void indexDocument(IndexWriter writer, Object meta) {
        try {
            //adding the document in a specific model. in this case we use a MDwebDocument.
            writer.addDocument(createDocument(meta));
            if (meta instanceof DefaultMetadata) {
                LOGGER.finer("Metadata: " + ((DefaultMetadata)meta).getFileIdentifier() + " indexed");
            } else if (meta instanceof RecordType) {
                LOGGER.finer("Metadata: " + ((RecordType)meta).getIdentifier() + " indexed");
            } else {
                LOGGER.finer("Unexpected metadata type");
            }
        } catch (IndexingException ex) {
            LOGGER.severe("indexingException " + ex.getMessage());
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (IOException ex) {
            LOGGER.severe(IO_SINGLE_MSG + ex.getMessage());
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

   /**
     * This method add to index of lucene a new document based on a geotoolkit Metadata object.
     * (implements AbstractIndex.indexDocument() )
     *
     * @param meta A geotoolkit Metadata object.
     */
    @Override
    public void indexDocument(Object meta) {
        try {
            final IndexWriter writer = new IndexWriter(new SimpleFSDirectory(getFileDirectory()), analyzer, false,IndexWriter.MaxFieldLength.UNLIMITED);

            //adding the document in a specific model. in this case we use a MDwebDocument.
            writer.addDocument(createDocument(meta));
            if (meta instanceof DefaultMetadata) {
                LOGGER.finer("Metadata: " + ((DefaultMetadata)meta).getFileIdentifier() + " indexed");
            } else if (meta instanceof RecordType) {
                LOGGER.finer("Metadata: " + ((RecordType)meta).getIdentifier() + " indexed");
            } else {
                LOGGER.finer("Unexpected metadata type");
            }

            writer.optimize();
            writer.close();

        } catch (IndexingException ex) {
            LOGGER.severe("IndexingException " + ex.getMessage());
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (IOException ex) {
            LOGGER.severe(IO_SINGLE_MSG + ex.getMessage());
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
    * Makes a document for a geotoolkit Metadata Object.
    * 
    * @param metadata.
    * @return A Lucene document.
    */
    @Override
    protected Document createDocument(final Object metadata) throws IndexingException {
        
        // make a new, empty document
        final Document doc = new Document();
        CompletionService<TermValue> cs = new BoundedCompletionService<TermValue>(this.pool, 5);

        String identifier;
        if (metadata instanceof DefaultMetadata) {
            identifier = ((DefaultMetadata)metadata).getFileIdentifier();
        } else if (metadata instanceof RecordType) {
            identifier = ((RecordType)metadata).getIdentifier().getContent().get(0);
        } else if (metadata instanceof RegistryObjectType) {
            identifier = ((RegistryObjectType)metadata).getId();
        } else {
            String type = "null type";
            if (metadata != null) {
                type = metadata.getClass().getSimpleName();
            }
            throw new IllegalArgumentException("unexpected metadata type: " + type);
        }
        //doc.add(new Field("Title",   metadata.,               Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("id", identifier,  Field.Store.YES, Field.Index.NOT_ANALYZED));

        final StringBuilder anyText = new StringBuilder();

        if (metadata instanceof DefaultMetadata) {
            LOGGER.finer("indexing ISO 19119 MD_Metadata");
            for (final String term : ISO_QUERYABLE.keySet()) {
                 cs.submit(new Callable<TermValue>() {
                    @Override
                    public TermValue call() {
                        return new TermValue(term, getValues(metadata, ISO_QUERYABLE.get(term)));
                    }
               });
            }

            for (int i = 0; i < ISO_QUERYABLE.size(); i++) {
                try {
                    final TermValue values = cs.take().get();
                    if (values.term != null && !values.term.equals("AnyText")) {
                        doc.add(new Field(values.term, values.value, Field.Store.YES, Field.Index.ANALYZED));
                        doc.add(new Field(values.term + "_sort", values.value, Field.Store.YES, Field.Index.NOT_ANALYZED));
                        if (values.value != null && !values.value.equals(NULL_VALUE) && anyText.indexOf(values.value) == -1) {
                            anyText.append(values.value).append(" ");
                        }
                    }
                } catch (InterruptedException ex) {
                   LOGGER.severe("InterruptedException in parralele create document:" + '\n' + ex.getMessage());
                } catch (ExecutionException ex) {
                   LOGGER.severe("ExecutionException in parralele create document:" + '\n' + ex.getCause());
                   LOGGER.log(Level.SEVERE, ex.getCause().getMessage(), ex.getCause());
                }
            }

            //we add the geometry parts
            String coord = NULL_VALUE;
            try {
                coord = getValues(metadata, ISO_QUERYABLE.get("WestBoundLongitude"));
                final double[] minx = extractPositions(coord);

                coord = getValues(metadata, ISO_QUERYABLE.get("EastBoundLongitude"));
                final double[] maxx = extractPositions(coord);

                coord = getValues(metadata, ISO_QUERYABLE.get("NorthBoundLatitude"));
                final double[] maxy = extractPositions(coord);

                coord = getValues(metadata, ISO_QUERYABLE.get("SouthBoundLatitude"));
                final double[] miny = extractPositions(coord);

                if (minx.length == maxx.length && maxx.length == miny.length && miny.length == maxy.length) {
                    for (int j = 0; j < minx.length; j++)  {
                        addBoundingBox(doc, minx[j], maxx[j], miny[j], maxy[j], SRID_4326);
                    }
                } else {
                    LOGGER.warning(NOT_SPATIALLY_INDEXABLE + ((DefaultMetadata)metadata).getFileIdentifier() + '\n' +
                            "cause: missing coordinates.: " + coord);
                }

            } catch (NumberFormatException e) {
                if (!coord.equals(NULL_VALUE)) {
                    LOGGER.warning(NOT_SPATIALLY_INDEXABLE + ((DefaultMetadata)metadata).getFileIdentifier() + '\n' +
                            "cause: unable to parse double: " + coord);
                }
            }
        }

         // All metadata types must be compatible with dublinCore.
        cs = new BoundedCompletionService<TermValue>(this.pool, 5);
        for (final String term :DUBLIN_CORE_QUERYABLE.keySet()) {
            cs.submit(new Callable<TermValue>() {

                @Override
                public TermValue call() {
                    return new TermValue(term, getValues(metadata, DUBLIN_CORE_QUERYABLE.get(term)));
                }
            });
        }

        for (int i = 0; i < DUBLIN_CORE_QUERYABLE.size(); i++) {
            try {
                final TermValue values = cs.take().get();
                if (!values.term.equals("date")) {
                    doc.add(new Field(values.term, values.value, Field.Store.YES, Field.Index.ANALYZED));
                } else {
                    // we format the date to lucene format
                    String value = values.value;
                    if (value.endsWith("z") || value.endsWith("Z")) {
                        value = value.substring(0, value.length() - 1);
                    }
                    value = value.replace("-", "");
                    doc.add(new Field(values.term, value, Field.Store.YES, Field.Index.ANALYZED));
                }
                doc.add(new Field(values.term + "_sort", values.value, Field.Store.YES, Field.Index.NOT_ANALYZED));
                if (values.value != null && !values.value.equals(NULL_VALUE) && anyText.indexOf(values.value) == -1) {
                    anyText.append(values.value).append(" ");
                }

            } catch (InterruptedException ex) {
               LOGGER.severe("InterruptedException in parralele create document:" + '\n' + ex.getMessage());
            } catch (ExecutionException ex) {
               LOGGER.severe("ExecutionException in parralele create document:" + '\n' + ex.getMessage());
            }
        }

        //we add the geometry parts
        String coord = NULL_VALUE;
        try {
            coord = getValues(metadata, DUBLIN_CORE_QUERYABLE.get("WestBoundLongitude"));
            final double[] minx = extractPositions(coord);

            coord = getValues(metadata, DUBLIN_CORE_QUERYABLE.get("EastBoundLongitude"));
            final double[] maxx = extractPositions(coord);

            coord = getValues(metadata, DUBLIN_CORE_QUERYABLE.get("NorthBoundLatitude"));
            final double[] maxy = extractPositions(coord);

            coord = getValues(metadata, DUBLIN_CORE_QUERYABLE.get("SouthBoundLatitude"));
            final double[] miny = extractPositions(coord);

            // String crs = getValues(metadata, DUBLIN_CORE_QUERYABLE.get("CRS"));

            if (minx.length == maxx.length && maxx.length == miny.length && miny.length == maxy.length) {
                for (int j = 0; j < minx.length; j++)  {
                    LOGGER.finer("added new bbox:" + minx[j] + " " + maxx[j] + " " + miny[j] + " " + maxy[j] + " for identifier:" + identifier);
                    addBoundingBox(doc, minx[j], maxx[j], miny[j], maxy[j], SRID_4326);
                }
            } else {
                if (metadata instanceof DefaultMetadata) {
                    LOGGER.warning(NOT_SPATIALLY_INDEXABLE + ((DefaultMetadata)metadata).getFileIdentifier() + '\n' +
                        "cause: missing coordinates.: " + coord);
                } else if (metadata instanceof RecordType) {
                    LOGGER.warning(NOT_SPATIALLY_INDEXABLE + ((RecordType)metadata).getIdentifier() + '\n' +
                        "cause: missing coordinates.: " + coord);
                } else {
                    LOGGER.finer("Unexpected metadata type");
                }
            }

        } catch (NumberFormatException e) {
            if (!coord.equals(NULL_VALUE)) {
                if (metadata instanceof DefaultMetadata) {
                    LOGGER.warning(NOT_SPATIALLY_INDEXABLE + ((DefaultMetadata)metadata).getFileIdentifier() + '\n' +
                        "cause: unable to parse double: " + coord);
                } else if (metadata instanceof RecordType) {
                    LOGGER.warning(NOT_SPATIALLY_INDEXABLE + ((RecordType)metadata).getIdentifier() + '\n' +
                        "cause: unable to parse double: " + coord);
                } else {
                    LOGGER.finer("Unexpected metadata type");
                }
            }
        }

        // we add to the index the special queryable element of the metadata reader
        if (additionalQueryable != null) {
            for (String term : additionalQueryable.keySet()) {

                String values = getValues(metadata, additionalQueryable.get(term));
                if (!values.equals(NULL_VALUE)) {
                    LOGGER.finer("put " + term + " values: " + values);
                    anyText.append(values).append(" ");
                }
                if (term.equals("date") || term.equals("modified")) {
                    values = values.replaceAll("-","");
                }
                doc.add(new Field(term, values,   Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field(term + "_sort", values,   Field.Store.YES, Field.Index.NOT_ANALYZED));
            }
        }

        // add a default meta field to make searching all documents easy
        doc.add(new Field("metafile", "doc",Field.Store.YES, Field.Index.ANALYZED));

        //we add the anyText values
        doc.add(new Field("AnyText", anyText.toString(),   Field.Store.YES, Field.Index.ANALYZED));

        return doc;
    }

    /**
     * Extract the double coordinate in a comma separated String.
     *
     * @param coord
     * @return
     */
    private double[] extractPositions(final String coord) {
        final StringTokenizer tokens = new StringTokenizer(coord, ",;");
        final double[] coordinate = new double[tokens.countTokens()];
        int i = 0;
        while (tokens.hasMoreTokens()) {
            coordinate[i] = Double.parseDouble(tokens.nextToken());
            i++;
        }
        return coordinate;
    }
    
    /**
     * Return a string description for the specified terms
     * 
     * @param term An ISO queryable term defined in CSWWorker (like Title, Subject, Abstract,...)
     * @param form An getools metadata from whitch we extract the values correspounding to the specified term.
     * 
     * @return A string concataining the differents values correspounding to the specified term, coma separated.
     */
    public static String getValues(Object metadata, List<String> paths) {
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
                    if (value != null && !value.equals("") && !value.equals(NULL_VALUE))
                        response.append(value).append(',');
                } else {
                    response.append(getConditionalValuesFromPath(pathID, conditionalAttribute, conditionalValue, metadata)).append(',');
                }
            }
        }
        if (response.toString().equals("")) {
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
            while (!pathID.equals("")) {
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
        } else if (obj instanceof Double) {
            result = obj + "";
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
            while (!pathID.equals("")) {
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
                    if (pathID.equals("")) {
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
                    if (pathID.equals("")) {
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
                     "match? " +conditionalValue.equals(getStringValue(conditionalObj)));
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
                LOGGER.severe("Unable to parse the ordinal " + tmp);
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
        if (reader != null)
            reader.destroy();
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
