/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

package org.constellation.metadata.index;

// J2SE dependencies
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.document.NumericField;
import java.util.Properties;
import org.geotoolkit.util.FileUtilities;
import org.apache.lucene.index.IndexWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

// Apache Lucene dependencies
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

// geotoolkit dependencies
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexer;

import static org.constellation.metadata.CSWQueryable.*;
/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractCSWIndexer<A> extends AbstractIndexer<A> {

    protected static final String NOT_SPATIALLY_INDEXABLE = "unable to spatially index metadata: ";

    protected static final String NULL_VALUE = "null";

    private final Map<String, List<String>> additionalQueryable;
    
    private final Map<String, String> numericFields = new HashMap<String, String>();

    /**
     * Build a new CSW metadata indexer.
     *
     * @param serviceID The identifier, if there is one, of the index/service.
     * @param configDirectory The directory where the files of the index will be stored.
     * @param additionalQueryable A map of additional queryable elements.
     */
    public AbstractCSWIndexer(String serviceID, File configDirectory, Map<String, List<String>> additionalQueryable) {
        super(serviceID, configDirectory);
        if (additionalQueryable != null) {
            this.additionalQueryable = additionalQueryable;
        } else {
            this.additionalQueryable = new HashMap<String, List<String>>();
        }
    }

    /**
     * Build a new CSW metadata indexer, with the specified lucene analyzer.
     * 
     * @param serviceID The identifier, if there is one, of the index/service.
     * @param configDirectory The directory where the files of the index will be stored.
     * @param analyzer A lucene analyzer used in text values indexation (default is ClassicAnalyzer).
     * @param additionalQueryable  A map of additional queryable elements.
     */
    public AbstractCSWIndexer(String serviceID, File configDirectory, Analyzer analyzer, Map<String, List<String>> additionalQueryable) {
        super(serviceID, configDirectory, analyzer);
        if (additionalQueryable != null) {
            this.additionalQueryable = additionalQueryable;
        } else {
            this.additionalQueryable = new HashMap<String, List<String>>();
        }
    }

    /**
    * Makes a document for a A Metadata Object.
    *
    * @param metadata The metadata to index.
    * @return A Lucene document.
    */
    @Override
    protected Document createDocument(final A metadata) throws IndexingException {
        // make a new, empty document
        final Document doc = new Document();

        indexSpecialField(metadata, doc);

        final StringBuilder anyText     = new StringBuilder();
        boolean alreadySpatiallyIndexed = false;

        // For an ISO 19139 object
        if (isISO19139(metadata)) {
            final Map<String, List<String>> isoQueryable = removeOverridenField(ISO_QUERYABLE);
            indexQueryableSet(doc, metadata, isoQueryable, anyText);

            //we add the geometry parts
            alreadySpatiallyIndexed = indexSpatialPart(doc, metadata, isoQueryable);

        } else if (isEbrim30(metadata)) {
           // TODO
        } else if (isEbrim25(metadata)) {
            // TODO
        } else if (!isDublinCore(metadata)) {
            LOGGER.log(Level.WARNING, "unknow Object classe unable to index: {0}", getType(metadata));
        }

        // All metadata types must be compatible with dublinCore.
        final Map<String, List<String>> dcQueryable = removeOverridenField(DUBLIN_CORE_QUERYABLE);
        indexQueryableSet(doc, metadata, dcQueryable, anyText);

        //we add the geometry parts if its nor already indexed
        if (!alreadySpatiallyIndexed) {
            indexSpatialPart(doc, metadata, dcQueryable);
        }

        // we add to the index the special queryable elements
        indexQueryableSet(doc, metadata, additionalQueryable, anyText);

        // add a default meta field to make searching all documents easy
        doc.add(new Field("metafile", "doc",Field.Store.YES, Field.Index.ANALYZED));

        //we add the anyText values
        doc.add(new Field("AnyText", anyText.toString(),   Field.Store.YES, Field.Index.ANALYZED));

        return doc;
    }

    /**
     * Remove the mapping of the specified Queryable set if it is overridden by one in the additional Queryable set.
     * 
     * @param queryableSet
     */
    private Map<String, List<String>> removeOverridenField(Map<String, List<String>> queryableSet) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for (Entry<String, List<String>> entry : queryableSet.entrySet()) {
            if (!additionalQueryable.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    /**
     * Index the values for the specified Field
     * 
     * @param values
     * @param fieldName
     * @param anyText
     * @param doc 
     */
    protected void indexFields(final List<Object> values, final String fieldName, final StringBuilder anyText, final Document doc) {
        for (Object value : values) {
            if (value instanceof String) {
                indexField(fieldName, (String) value, anyText, doc);
            } else if (value instanceof Number) {
                indexNumericField(fieldName, (Number) value, doc);
            } else if (value != null){
                LOGGER.warning("unexpected type for field:" + value.getClass());
            }
        }
    }
    
    /**
     * Index a single String field.
     * Add this value to the anyText builder if its not equals to "null".
     * 
     * @param fieldName
     * @param stringValue
     * @param anyText
     * @param doc 
     */
    protected void indexField(final String fieldName, final String stringValue, final StringBuilder anyText, final Document doc) {
        final Field field        = new Field(fieldName, stringValue, Field.Store.YES, Field.Index.ANALYZED);
        final Field fieldSort    = new Field(fieldName + "_sort", stringValue, Field.Store.YES, Field.Index.NOT_ANALYZED);
        if (!stringValue.equals(NULL_VALUE) && anyText.indexOf(stringValue) == -1) {
            anyText.append(stringValue).append(" ");
        }
        doc.add(field);
        doc.add(fieldSort);
    }
    
    /**
     * Inex a numeric field.
     * 
     * @param fieldName
     * @param numValue
     * @param doc 
     */
    protected void indexNumericField(final String fieldName, final Number numValue, final Document doc) {
         
        final NumericField numField     = new NumericField(fieldName, NumericUtils.PRECISION_STEP_DEFAULT, Field.Store.YES, true);
        final NumericField numSortField = new NumericField(fieldName + "_sort", NumericUtils.PRECISION_STEP_DEFAULT, Field.Store.YES, true);
        final Character fieldType;
        if (numValue instanceof Integer) {
            numField.setIntValue((Integer) numValue);
            numSortField.setIntValue((Integer) numValue);
            fieldType = 'i';
        } else if (numValue instanceof Double) {
            numField.setDoubleValue((Double) numValue);
            numSortField.setDoubleValue((Double) numValue);
            fieldType = 'd';
        } else if (numValue instanceof Float) {
            numField.setFloatValue((Float) numValue);
            numSortField.setFloatValue((Float) numValue);
            fieldType = 'f';
        } else if (numValue instanceof Long) {
            numField.setLongValue((Long) numValue);
            numSortField.setLongValue((Long) numValue);
            fieldType = 'l';
        } else {
            fieldType = 'u';
            LOGGER.severe("Unexpected Number type:" + numValue.getClass().getName());
        }
        addNumericField(fieldName, fieldType);
        addNumericField(fieldName + "_sort", fieldType);
        doc.add(numField);
        doc.add(numSortField);
    }
    
    /**
     * Add the specifics implementation field to the document.
     *
     * @param metadata The metadata to index.
     * @param doc The lucene document currently building.
     * @throws IndexingException
     */
    protected abstract void indexSpecialField(final A metadata, final Document doc) throws IndexingException;

    /**
     * Return a String description of the type of the metadata.
     *
     * @param metadata The metadata currently indexed
     * @return A string description (name of the class, name of the top value type, ...)
     */
    protected abstract String getType(final A metadata);

    /**
     * Index a set of properties contained in the queryableSet.
     *
     * @param doc The lucene document currently building.
     * @param metadata The metadata to index.
     * @param queryableSet A set of queryable properties and their relative path in the metadata.
     * @param anyText A {@link StringBuilder} in which are concatened all the text values.
     * @throws IndexingException
     */
    protected abstract void indexQueryableSet(final Document doc, final A metadata, Map<String, List<String>> queryableSet, final StringBuilder anyText) throws IndexingException;

    /**
     * Spatially index the form extracting the BBOX values with the specified queryable set.
     *
     * @param doc The current Lucene document.
     * @param form The metadata records to spatially index.
     * @param queryableSet A set of queryable Term.
     * @param ordinal
     *
     * @return true if the indexation succeed
     * @throws MD_IOException
     */
    protected boolean indexSpatialPart(Document doc, A form, Map<String, List<String>> queryableSet) throws IndexingException {

        final List<Double> minxs = extractPositions(form, queryableSet.get("WestBoundLongitude"));
        final List<Double> maxxs = extractPositions(form, queryableSet.get("EastBoundLongitude"));
        final List<Double> maxys = extractPositions(form, queryableSet.get("NorthBoundLatitude"));
        final List<Double> minys = extractPositions(form, queryableSet.get("SouthBoundLatitude"));

        if (minxs.size() == minys.size() && minys.size() == maxxs.size() && maxxs.size() == maxys.size()) {
            if (minxs.size() == 1) {
                addBoundingBox(doc, minxs.get(0), maxxs.get(0), minys.get(0), maxys.get(0), SRID_4326);
                return true;
            } else if (minxs.size() > 0) {
                addMultipleBoundingBox(doc, minxs, maxxs, minys, maxys, SRID_4326);
                return true;
            }
        } else {
            LOGGER.warning(NOT_SPATIALLY_INDEXABLE + getIdentifier(form) + "\n cause: missing coordinates."
                    + minxs.size() + " " + minys.size() + " " +  maxxs.size() + " " +  maxys.size());
        }
        return false;
    }

     /**
      * Extract the double coordinate from a metadata object using a list of paths to find the data.
      * 
      * @param metadata The metadata to spatially index.
      * @param paths A list of paths where to find the information within the metadata.
      * @return A list of Double coordinates.
      *
      * @throws IndexingException
      */
    private List<Double> extractPositions(A metadata, List<String> paths) throws IndexingException {
        final String coord            = getValues(metadata, paths);
        final StringTokenizer tokens  = new StringTokenizer(coord, ",;");
        final List<Double> coordinate = new ArrayList<Double>(tokens.countTokens());
        try {
            while (tokens.hasMoreTokens()) {
                coordinate.add(Double.parseDouble(tokens.nextToken()));
            }
        } catch (NumberFormatException e) {
            if (!coord.equals(NULL_VALUE)) {
                LOGGER.warning(NOT_SPATIALLY_INDEXABLE + getIdentifier(metadata) +
                        "\ncause: unable to parse double: " + coord);
            }
        }
        return coordinate;
    }

    /**
     * Add a numeric fields to the current list.
     * 
     * @param fieldName
     * @param numberType 
     */
    protected void addNumericField(final String fieldName, final Character numberType) {
        if (numericFields.get(fieldName) == null) {
            numericFields.put(fieldName, numberType.toString());
        }
    }
    
    /**
     * Store the numeric fields in a properties file int the index directory
     */
    protected void storeNumericFieldsFile() {
        final File indexDirectory   = getFileDirectory();
        final File numericFieldFile = new File(indexDirectory, "numericFields.properties");
        final Properties prop       = new Properties();
        prop.putAll(numericFields);
        try {
            FileUtilities.storeProperties(prop, numericFieldFile);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Unable to store the numeric fields properties file.", ex);
        }
        
    }
    
    protected void stopIndexation(final IndexWriter writer, final String serviceID) throws IOException {
        // writer.optimize(); no longer justified
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
     * Extract some values from a metadata object using  the list of paths.
     * 
     * @param meta The object to index.
     * @param paths A list of paths where to find the information within the metadata.
     *
     * @Deprecated
     * 
     * @return A String containing one or more informations (comma separated) find in the metadata.
     * @throws IndexingException
     */
    @Deprecated
    protected abstract String getValues(final A meta, final List<String> paths) throws IndexingException;

    /**
     * Return true if the metadata object is a ISO19139 object.
     *
     * @param meta The object to index
     * @return true if the metadata object is a ISO19139 object.
     */
    protected abstract boolean isISO19139(A meta);

    /**
     * Return true if the metadata object is a DublinCore object.
     *
     * @param meta The object to index
     * @return true if the metadata object is a DublinCore object.
     */
    protected abstract boolean isDublinCore(A meta);

    /**
     * Return true if the metadata object is a Ebrim version 2.5 object.
     *
     * @param meta The object to index
     * @return true if the metadata object is a Ebrim version 2.5 object.
     */
    protected abstract boolean isEbrim25(A meta);

    /**
     * Return true if the metadata object is a Ebrim version 3.0 object.
     *
     * @param meta The object to index
     * @return true if the metadata object is a Ebrim version 3.0 object.
     */
    protected abstract boolean isEbrim30(A meta);

}
