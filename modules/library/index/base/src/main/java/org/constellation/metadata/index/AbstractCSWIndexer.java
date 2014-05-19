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

package org.constellation.metadata.index;

// J2SE dependencies
import java.util.Map.Entry;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

// Apache Lucene dependencies
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;

// geotoolkit dependencies
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.apache.sis.util.NullArgumentException;

import static org.constellation.metadata.CSWQueryable.*;
/**
 *
 * @author Guilhem Legal (Geomatys)
 * 
 * @param <A> the type of indexed Object
 */
public abstract class AbstractCSWIndexer<A> extends AbstractIndexer<A> {

    protected static final String NOT_SPATIALLY_INDEXABLE = "unable to spatially index metadata: ";

    protected static final String NULL_VALUE = "null";

    private final Map<String, List<String>> additionalQueryable;

    protected static final FieldType ID_TYPE = new FieldType();
    static {
        ID_TYPE.setTokenized(false);
        ID_TYPE.setStored(true);
        ID_TYPE.setIndexed(true);
    }

    protected static final FieldType SORT_TYPE = new FieldType();
    static {
        SORT_TYPE.setTokenized(false);
        SORT_TYPE.setStored(false);
        SORT_TYPE.setIndexed(true);
    }

    protected static final FieldType TEXT_TYPE = new FieldType();
    static {
        TEXT_TYPE.setTokenized(true);
        TEXT_TYPE.setStored(true);
        TEXT_TYPE.setIndexed(true);
    }

    protected static final FieldType SEARCH_TYPE = new FieldType();
    static {
        SEARCH_TYPE.setTokenized(true);
        SEARCH_TYPE.setStored(false);
        SEARCH_TYPE.setIndexed(true);
    }

    /**
     * Build a new CSW metadata indexer.
     *
     * @param indexID The identifier, if there is one, of the index.
     * @param configDirectory The directory where the files of the index will be stored.
     * @param additionalQueryable A map of additional queryable elements.
     */
    public AbstractCSWIndexer(final String indexID, final File configDirectory, final Map<String, List<String>> additionalQueryable) {
        super(indexID, configDirectory);
        if (additionalQueryable != null) {
            this.additionalQueryable = additionalQueryable;
        } else {
            this.additionalQueryable = new HashMap<>();
        }
    }

    /**
     * Build a new CSW metadata indexer, with the specified lucene analyzer.
     *
     * @param indexID The identifier, if there is one, of the index.
     * @param configDirectory The directory where the files of the index will be stored.
     * @param analyzer A lucene analyzer used in text values indexation (default is ClassicAnalyzer).
     * @param additionalQueryable  A map of additional queryable elements.
     */
    public AbstractCSWIndexer(String indexID, File configDirectory, Analyzer analyzer, Map<String, List<String>> additionalQueryable) {
        super(indexID, configDirectory, analyzer);
        if (additionalQueryable != null) {
            this.additionalQueryable = additionalQueryable;
        } else {
            this.additionalQueryable = new HashMap<>();
        }
    }

    /**
    * Makes a document for a A Metadata Object.
    *
    * @param metadata The metadata to index.
    * @param docId the document identifier.
    * @return A Lucene document.
    */
    @Override
    protected Document createDocument(final A metadata, final int docId) throws IndexingException {
        // make a new, empty document
        final Document doc = new Document();
        doc.add(new Field("docid", docId + "", ID_TYPE));

        indexSpecialField(metadata, doc);

        final StringBuilder anyText     = new StringBuilder();
        boolean alreadySpatiallyIndexed = false;

        // For an ISO 19139 object
        if (isISO19139(metadata)) {
            final Map<String, List<String>> isoQueryable = removeOverridenField(ISO_QUERYABLE);
            indexQueryableSet(doc, metadata, isoQueryable, anyText);

            //we add the geometry parts
            alreadySpatiallyIndexed = indexSpatialPart(doc, metadata, isoQueryable, 268435540);
            
            doc.add(new Field("objectType", "MD_Metadata", SEARCH_TYPE));

        } else if (isEbrim30(metadata)) {
           // TODO
            doc.add(new Field("objectType", "Ebrim", SEARCH_TYPE));
        } else if (isEbrim25(metadata)) {
            // TODO
            doc.add(new Field("objectType", "Ebrim", SEARCH_TYPE));
        } else if (isFeatureCatalogue(metadata)) {
            final Map<String, List<String>> fcQueryable = removeOverridenField(ISO_FC_QUERYABLE);
            indexQueryableSet(doc, metadata, fcQueryable, anyText);
            
            doc.add(new Field("objectType", "FC_FeatureCatalogue", SEARCH_TYPE));
        } else if (isDublinCore(metadata)) {
            
            doc.add(new Field("objectType", "Record", SEARCH_TYPE));
        } else {
            LOGGER.log(Level.WARNING, "unknow Object classe unable to index: {0}", getType(metadata));
        }

        // All metadata types must be compatible with dublinCore.
        final Map<String, List<String>> dcQueryable = removeOverridenField(DUBLIN_CORE_QUERYABLE);
        indexQueryableSet(doc, metadata, dcQueryable, anyText);

        //we add the geometry parts if its nor already indexed
        if (!alreadySpatiallyIndexed) {
            //TODO find the real srid instead of static 4326
            indexSpatialPart(doc, metadata, dcQueryable, 4326);
        }

        // we add to the index the special queryable elements
        indexQueryableSet(doc, metadata, additionalQueryable, anyText);

        // add a default meta field to make searching all documents easy
        doc.add(new Field("metafile", "doc",SEARCH_TYPE));

        //we add the anyText values
        doc.add(new Field("AnyText", anyText.toString(),   SEARCH_TYPE));

        return doc;
    }

    /**
     * Remove the mapping of the specified Queryable set if it is overridden by one in the additional Queryable set.
     *
     * @param queryableSet
     */
    private Map<String, List<String>> removeOverridenField(Map<String, List<String>> queryableSet) {
        Map<String, List<String>> result = new HashMap<>();
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
                LOGGER.log(Level.WARNING, "unexpected type for field:{0}", value.getClass());
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
        final Field field        = new Field(fieldName, stringValue, SEARCH_TYPE);
        final Field fieldSort    = new Field(fieldName + "_sort", stringValue, SORT_TYPE);
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

        final Field numField;
        final Field numSortField;
        final Character fieldType;
        if (numValue instanceof Integer) {
            numField     = new IntField(fieldName,           (Integer) numValue, Field.Store.NO);
            numSortField = new IntField(fieldName + "_sort", (Integer) numValue, Field.Store.NO);
            fieldType = 'i';
        } else if (numValue instanceof Double) {
            numField     = new DoubleField(fieldName,           (Double) numValue, Field.Store.NO);
            numSortField = new DoubleField(fieldName + "_sort", (Double) numValue, Field.Store.NO);
            fieldType = 'd';
        } else if (numValue instanceof Float) {
            numField     = new FloatField(fieldName,           (Float) numValue, Field.Store.NO);
            numSortField = new FloatField(fieldName + "_sort", (Float) numValue, Field.Store.NO);
            fieldType = 'f';
        } else if (numValue instanceof Long) {
            numField     = new LongField(fieldName,           (Long) numValue, Field.Store.NO);
            numSortField = new LongField(fieldName + "_sort", (Long) numValue, Field.Store.NO);
            fieldType = 'l';
        } else {
            numField     = new StringField(fieldName,           numValue + "", Field.Store.NO);
            numSortField = new StringField(fieldName + "_sort", numValue + "", Field.Store.NO);
            fieldType = 'u';
            LOGGER.log(Level.WARNING, "Unexpected Number type:{0}", numValue.getClass().getName());
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
     * @param srid the coordinate reference system SRID
     *
     * @return true if the indexation succeed
     * @throws MD_IOException
     */
    private boolean indexSpatialPart(Document doc, A form, Map<String, List<String>> queryableSet, int srid) throws IndexingException {

        final List<Double> minxs = extractPositions(form, queryableSet.get("WestBoundLongitude"));
        final List<Double> maxxs = extractPositions(form, queryableSet.get("EastBoundLongitude"));
        final List<Double> maxys = extractPositions(form, queryableSet.get("NorthBoundLatitude"));
        final List<Double> minys = extractPositions(form, queryableSet.get("SouthBoundLatitude"));
        try {
            if (minxs.size() == minys.size() && minys.size() == maxxs.size() && maxxs.size() == maxys.size()) {
                addBoundingBox(doc, minxs, maxxs, minys, maxys, srid);
                return true;
            } else {
                LOGGER.log(Level.WARNING,NOT_SPATIALLY_INDEXABLE + "{0}\n cause: missing coordinates.", getIdentifier(form));
            }
        } catch (NullArgumentException ex) {
            throw new IndexingException("error while spatially indexing:" + doc.get("id"), ex);
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
        final List<Double> coordinate = new ArrayList<>(tokens.countTokens());
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

    @Override
    protected Iterator<A> getEntryIterator() throws IndexingException {
        throw new UnsupportedOperationException("Not supported by this implementation");
    }

    @Override
    protected boolean useEntryIterator() {
        return false;
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
    
    /**
     * Return true if the metadata object is a FeatureCatalogue object.
     *
     * @param meta The object to index
     * @return true if the metadata object is a FeatureCatalogue object.
     */
    protected abstract boolean isFeatureCatalogue(A meta);

}
