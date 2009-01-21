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
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// apache Lucene dependencies
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;

// constellation dependencies
import org.constellation.concurrent.BoundedCompletionService;
import org.constellation.lucene.index.AbstractIndexer;
import org.constellation.util.Utils;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.ws.CstlServiceException;
import static org.constellation.metadata.CSWQueryable.*;

// geotools dependencies
import org.geotools.metadata.iso.MetaDataImpl;
import org.geotools.temporal.object.DefaultInstant;

// geoAPI dependencies
import org.geotools.temporal.object.DefaultPosition;
import org.opengis.util.InternationalString;


/**
 * A Lucene Index Handler for a generic Database.
 * @author Guilhem Legal
 */
public class GenericIndexer extends AbstractIndexer<Object> {
    
    /**
     * The Reader of this lucene index (generic DB mode).
     */
    private final MetadataReader reader;
    
    private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private Map<String, Method> getters = new HashMap<String, Method>();

    /**
     * Shared Thread Pool for parralele execution
     */
    private ExecutorService pool = Executors.newFixedThreadPool(6);

    /**
     * Creates a new Lucene Index into the specified directory with the specified generic database reader.
     * 
     * @param reader A generic reader for read the metadata database.
     * @param configDirectory A directory where the index can write indexation file. 
     */
    public GenericIndexer(MetadataReader reader, File configDirectory, String serviceID) throws CstlServiceException {
        super(serviceID, configDirectory);
        this.reader = reader;
        if (create)
            createIndex();
    }
    
    /** 
     * Create a new Index from a generic database.
     * 
     * @throws java.sql.SQLException
     */
    public void createIndex() throws CstlServiceException {
        logger.info("Creating lucene index for Generic database please wait...");
        long time = System.currentTimeMillis();
        IndexWriter writer;
        int nbEntries = 0;
        try {
            writer = new IndexWriter(getFileDirectory(), analyzer, true);

            // TODO getting the objects list and index avery item in the IndexWriter.
            List<? extends Object> ids = reader.getAllEntries();
            logger.info("all entries read in " + (System.currentTimeMillis() - time) + " ms.");
            nbEntries = ids.size();
            for (Object entry : ids) {
                indexDocument(writer, entry);
            }
            writer.optimize();
            writer.close();

        } catch (CorruptIndexException ex) {
            logger.severe("CorruptIndexException while indexing document: " + ex.getMessage());
            ex.printStackTrace();
        } catch (LockObtainFailedException ex) {
            logger.severe("LockObtainException while indexing document: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            logger.severe("IOException while indexing document: " + ex.getMessage());
            ex.printStackTrace();
        }
        logger.info("Index creation process in " + (System.currentTimeMillis() - time) + " ms" + '\n' +
                " documents indexed: " + nbEntries);
    }

    /**
     * This method add to index of lucene a new document based on a geotools Metadata object.
     * (implements AbstractIndex.indexDocument() )
     * 
     * @param writer A lucene Index Writer.
     * @param meta A geotools Metadata object.
     */
    public void indexDocument(IndexWriter writer, Object meta) {
        try {
            //adding the document in a specific model. in this case we use a MDwebDocument.
            writer.addDocument(createDocument(meta));
            logger.finer("Metadata: " + ((MetaDataImpl)meta).getFileIdentifier() + " indexed");

        } catch (SQLException ex) {
            logger.severe("SQLException " + ex.getMessage());
            ex.printStackTrace();
        } catch (CorruptIndexException ex) {
            logger.severe("CorruptIndexException while indexing document: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            logger.severe("IOException while indexing document: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * This method add to index of lucene a new document based on a geotools Metadata object.
     * (implements AbstractIndex.indexDocument() )
     * 
     * @param meta A geotools Metadata object.
     */
    public void indexDocument(Object meta) {
        try {
            IndexWriter writer = new IndexWriter(getFileDirectory(), analyzer, true);
            
            //adding the document in a specific model. in this case we use a MDwebDocument.
            writer.addDocument(createDocument(meta));
            logger.finer("Metadata: " + ((MetaDataImpl)meta).getFileIdentifier() + " indexed");
            
            writer.optimize();
            writer.close();
            
        } catch (SQLException ex) {
            logger.severe("SQLException " + ex.getMessage());
            ex.printStackTrace();
        } catch (CorruptIndexException ex) {
            logger.severe("CorruptIndexException while indexing document: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            logger.severe("IOException while indexing document: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
    * Makes a document for a geotools MetaData Object.
    * 
    * @param metadata.
    * @return A Lucene document.
    */
    protected Document createDocument(final Object metadata) throws SQLException {
        
        // make a new, empty document
        final Document doc = new Document();
        CompletionService<String> cs = new BoundedCompletionService<String>(this.pool, 5);

        doc.add(new Field("id", ((MetaDataImpl)metadata).getFileIdentifier(),  Field.Store.YES, Field.Index.ANALYZED));
        //doc.add(new Field("Title",   metadata.,               Field.Store.YES, Field.Index.ANALYZED));
        
        logger.finer("indexing ISO 19119 MD_Metadata");
        //TODO add ANyText
        for (final String term : ISO_QUERYABLE.keySet()) {
             cs.submit(new Callable<String>() {
                public String call() {
                    return getValues(metadata, ISO_QUERYABLE.get(term));
                }
           });
        }

        for (String term : ISO_QUERYABLE.keySet()) {
            try {
                String values = cs.take().get();
                doc.add(new Field(term, values, Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field(term + "_sort", values, Field.Store.YES, Field.Index.NOT_ANALYZED));
            } catch (InterruptedException ex) {
               logger.severe("InterruptedException in parralele create document:" + '\n' + ex.getMessage());
            } catch (ExecutionException ex) {
               logger.severe("ExecutionException in parralele create document:" + '\n' + ex.getCause());
            }
        }
        
        //we add the geometry parts
        String coord = "null";
        try {
            coord = getValues(metadata, ISO_QUERYABLE.get("WestBoundLongitude"));
            StringTokenizer tokens = new StringTokenizer(coord, ",;");
            final double[] minx = new double[tokens.countTokens()];
            int i = 0;
            while (tokens.hasMoreTokens()) {
                minx[i] = Double.parseDouble(tokens.nextToken());
                i++;
            }

            coord = getValues(metadata, ISO_QUERYABLE.get("EastBoundLongitude"));
            tokens = new StringTokenizer(coord, ",;");
            final double[] maxx = new double[tokens.countTokens()];
            i = 0;
            while (tokens.hasMoreTokens()) {
                maxx[i] = Double.parseDouble(tokens.nextToken());
                i++;
            }

            coord = getValues(metadata, ISO_QUERYABLE.get("NorthBoundLatitude"));
            tokens = new StringTokenizer(coord, ",;");
            final double[] maxy = new double[tokens.countTokens()];
            i = 0;
            while (tokens.hasMoreTokens()) {
                maxy[i] = Double.parseDouble(tokens.nextToken());
                i++;
            }

            coord = getValues(metadata, ISO_QUERYABLE.get("SouthBoundLatitude"));
            tokens = new StringTokenizer(coord, ",;");
            final double[] miny = new double[tokens.countTokens()];
            i = 0;
            while (tokens.hasMoreTokens()) {
                miny[i] = Double.parseDouble(tokens.nextToken());
                i++;
            }

            if (minx.length == maxx.length && maxx.length == miny.length && miny.length == maxy.length) {
                for (int j = 0; j < minx.length; j++)  {
                    addBoundingBox(doc, minx[j], maxx[j], miny[j], maxy[j], "EPSG:4326");
                }
            } else {
                logger.severe("unable to spatially index form: " + ((MetaDataImpl)metadata).getFileIdentifier() + '\n' +
                        "cause: missing coordinates.: " + coord);
            }

        } catch (NumberFormatException e) {
            if (!coord.equals("null")) {
                logger.severe("unable to spatially index form: " + ((MetaDataImpl)metadata).getFileIdentifier() + '\n' +
                        "cause:  unable to parse double: " + coord);
            }
        }
            
         // All metadata types must be compatible with dublinCore.
        cs = new BoundedCompletionService<String>(this.pool, 5);
        final StringBuilder anyText = new StringBuilder();
        for (final String term :DUBLIN_CORE_QUERYABLE.keySet()) {
            cs.submit(new Callable<String>() {

                public String call() {
                    return getValues(metadata, DUBLIN_CORE_QUERYABLE.get(term));
                }
            });
        }

        for (String term : DUBLIN_CORE_QUERYABLE.keySet()) {
            try {
                String values = cs.take().get();
                if (!values.equals("null")) {
                    anyText.append(values).append(" ");
                }
                if (term.equals("date") || term.equals("modified")) {
                    values = values.replaceAll("-", "");
                }
                doc.add(new Field(term, values, Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field(term + "_sort", values, Field.Store.YES, Field.Index.NOT_ANALYZED));

            } catch (InterruptedException ex) {
               logger.severe("InterruptedException in parralele create document:" + '\n' + ex.getMessage());
            } catch (ExecutionException ex) {
               logger.severe("ExecutionException in parralele create document:" + '\n' + ex.getMessage());
            }
        }
            
        //we add the anyText values
        doc.add(new Field("AnyText", anyText.toString(),   Field.Store.YES, Field.Index.ANALYZED));
            
        //we add the geometry parts
        coord = "null";
        try {
            coord = getValues(metadata, DUBLIN_CORE_QUERYABLE.get("WestBoundLongitude"));
            StringTokenizer tokens = new StringTokenizer(coord, ",;");
            final double[] minx = new double[tokens.countTokens()];
            int i = 0;
            while (tokens.hasMoreTokens()) {
                minx[i] = Double.parseDouble(tokens.nextToken());
                i++;
            }
                
            coord = getValues(metadata, DUBLIN_CORE_QUERYABLE.get("EastBoundLongitude"));
            tokens = new StringTokenizer(coord, ",;");
            final double[] maxx = new double[tokens.countTokens()];
            i = 0;
            while (tokens.hasMoreTokens()) {
                maxx[i] = Double.parseDouble(tokens.nextToken());
                i++;
            }
            
            coord = getValues(metadata, DUBLIN_CORE_QUERYABLE.get("NorthBoundLatitude"));
            tokens = new StringTokenizer(coord, ",;");
            final double[] maxy = new double[tokens.countTokens()];
            i = 0;
            while (tokens.hasMoreTokens()) {
                maxy[i] = Double.parseDouble(tokens.nextToken());
                i++;
            }
            
            coord = getValues(metadata, DUBLIN_CORE_QUERYABLE.get("SouthBoundLatitude"));
            tokens = new StringTokenizer(coord, ",;");
            final double[] miny = new double[tokens.countTokens()];
            i = 0;
            while (tokens.hasMoreTokens()) {
                miny[i] = Double.parseDouble(tokens.nextToken());
                i++;
            }
                
            String crs = getValues(metadata, DUBLIN_CORE_QUERYABLE.get("CRS"));
                
            if (minx.length == maxx.length && maxx.length == miny.length && miny.length == maxy.length) {
                for (int j = 0; j < minx.length; j++)  {
                    addBoundingBox(doc, minx[j], maxx[j], miny[j], maxy[j], "EPSG:4326");
                }
            } else {
                logger.severe("unable to spatially index form: " + ((MetaDataImpl)metadata).getFileIdentifier() + '\n' +
                        "cause: missing coordinates.: " + coord);
            }
            
        } catch (NumberFormatException e) {
            if (!coord.equals("null"))
                logger.severe("unable to spatially index metadata: " + ((MetaDataImpl)metadata).getFileIdentifier() + '\n' +
                              "cause:  unable to parse double: " + coord);
        }

        // we add to the index the special queryable element of the metadata reader
        Map<String, List<String>> additionalQueryable = reader.getAdditionalQueryablePathMap();
        for (String term : additionalQueryable.keySet()) {

            String values = getValues(metadata, additionalQueryable.get(term));
            if (!values.equals("null")) {
                logger.finer("put " + term + " values: " + values);
                anyText.append(values).append(" ");
            }
            if (term.equals("date") || term.equals("modified")) {
                values = values.replaceAll("-","");
            }
            doc.add(new Field(term, values,   Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field(term + "_sort", values,   Field.Store.YES, Field.Index.NOT_ANALYZED));
        }

        // add a default meta field to make searching all documents easy 
        doc.add(new Field("metafile", "doc",Field.Store.YES, Field.Index.ANALYZED));
        
        return doc;
    }
    
    /**
     * Return a string description for the specified terms
     * 
     * @param term An ISO queryable term defined in CSWWorker (like Title, Subject, Abstract,...)
     * @param form An getools metadata from whitch we extract the values correspounding to the specified term.
     * 
     * @return A string concataining the differents values correspounding to the specified term, coma separated.
     */
    private String getValues(Object metadata, List<String> paths) {
        StringBuilder response  = new StringBuilder("");
        
        if (paths != null) {
            for (String fullPathID: paths) {
                String pathID;
                String conditionalAttribute = null;
                String conditionalValue     = null;
                
                // if the path ID contains a # we have a conditional value (codeList element) next to the searched value.
                int separator = fullPathID.indexOf('#'); 
                if (separator != -1) {
                    pathID               = fullPathID.substring(0, separator);
                    conditionalAttribute = fullPathID.substring(separator + 1, fullPathID.indexOf('='));
                    conditionalValue     = fullPathID.substring(fullPathID.indexOf('=') + 1);
                    logger.finer("pathID              : " + pathID               + '\n' +
                                 "conditionalAttribute: " + conditionalAttribute + '\n' +
                                 "conditionalValue    : " + conditionalValue); 
                } else {
                    pathID = fullPathID;
                }
                
                if (conditionalAttribute == null) {
                    String value = getValuesFromPath(pathID, metadata);
                    if (value != null && !value.equals(""))
                        response.append(value).append(',');
                } else {
                    response.append(getConditionalValuesFromPath(pathID, conditionalAttribute, conditionalValue, metadata)).append(',');
                }
            }
        }
        if (response.toString().equals("")) {
            response.append("null");
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
    private String getValuesFromPath(String pathID, Object metadata) {
        String result = "";
        if (pathID.startsWith("ISO 19115:MD_Metadata:")) {
            // we remove the prefix path part 
            pathID = pathID.substring(22);
            
            //for each part of the path we execute a (many) getter
            while (!pathID.equals("")) {
                String attributeName;
                if (pathID.indexOf(':') != -1) {
                    attributeName = pathID.substring(0, pathID.indexOf(':'));
                    pathID = pathID.substring(pathID.indexOf(':') + 1);
                } else {
                    attributeName = pathID;
                    pathID = "";
                }
                
                if (metadata instanceof Collection) {
                    List<Object> tmp = new ArrayList<Object>();
                    for (Object subMeta: (Collection) metadata) {
                        Object obj = getAttributeValue(subMeta, attributeName);
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
    private String getStringValue(Object obj) {
        String result = "";
        if (obj == null) {
            return "null";
        } else if (obj instanceof String) {
            result = (String) obj;
        } else if (obj instanceof InternationalString) {
            InternationalString is = (InternationalString) obj;
            result = is.toString();
        } else if (obj instanceof Double) {
            result = obj + "";
        } else if (obj instanceof java.util.Locale) {
            result = ((java.util.Locale)obj).getISO3Language();
        } else if (obj instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            for (Object o : (Collection) obj) {
                sb.append(getStringValue(o)).append(',');
            }
            result = sb.toString();
            if (result.indexOf(',') != -1)
            result = result.substring(0, result.length() - 1);
            if (result.length() == 0)
                result = "null";
        } else if (obj instanceof org.opengis.util.CodeList) {
            result = ((org.opengis.util.CodeList)obj).name();
        
        } else if (obj instanceof DefaultPosition) {
            result = obj.toString();
            
        } else if (obj instanceof DefaultInstant) {
            result = obj.toString();
            
        } else if (obj instanceof Date) {
            result = dateFormat.format((Date)obj);
            
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
    private String getConditionalValuesFromPath(String pathID, String conditionalAttribute, String conditionalValue, Object metadata) {
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
                    List<Object> tmp = new ArrayList<Object>();
                    if (pathID.equals("")) {
                        for (Object subMeta: (Collection)metadata) {
                            if (matchCondition(subMeta, conditionalAttribute, conditionalValue)) {
                                tmp.add(getAttributeValue(subMeta, attributeName));
                            } 
                        }
                    } else {
                        for (Object subMeta: (Collection)metadata) {
                            Object obj = getAttributeValue(subMeta, attributeName);
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
    private boolean matchCondition(Object metadata, String conditionalAttribute, String conditionalValue) {
        Object conditionalObj = getAttributeValue(metadata, conditionalAttribute);
        logger.finer("contionalObj: "     + getStringValue(conditionalObj) + '\n' +
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
    private Object getAttributeValue(Object object, String attributeName) {
        Object result = null;
        if (object != null) {
            String getterId = object.getClass().getName() + ':' + attributeName;
            Method getter = getters.get(getterId);
            if (getter != null) {
                result = Utils.invokeMethod(getter, object);
            } else {
                getter = Utils.getGetterFromName(attributeName, object.getClass());
                if (getter != null) {
                    getters.put(object.getClass().getName() + ':' + attributeName, getter);
                    result = Utils.invokeMethod(getter, object);
                }
            }
        }
        return result;
    }

    public void destroy() {
        if (reader != null)
            reader.destroy();
        pool.shutdown();
    }
}
