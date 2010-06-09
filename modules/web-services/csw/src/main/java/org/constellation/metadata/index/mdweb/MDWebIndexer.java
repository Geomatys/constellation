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

package org.constellation.metadata.index.mdweb;

// J2SE dependencies
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.sql.DataSource;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

// constellation dependencies
import org.apache.lucene.store.SimpleFSDirectory;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;

import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexer;
import static org.constellation.metadata.CSWQueryable.*;

// MDweb dependencies
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.CodeList;
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.schemas.Locale;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.schemas.Property;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.RecordSet;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.io.Reader;
import org.mdweb.io.MD_IOException;
import org.mdweb.io.sql.v20.Reader20;
import org.mdweb.io.sql.v21.Reader21;
import org.mdweb.model.storage.RecordSet.EXPOSURE;

/**
 * A Lucene index handler for an MDWeb Database.
 *
 * @author Guilhem Legal
 */
public class MDWebIndexer extends AbstractIndexer<Form> {

    /**
     * The Reader of this lucene index (MDWeb DB mode).
     */
    private Reader mdWebReader;

    /**
     * Creates a new CSW indexer for a MDWeb database.
     *
     * @param configuration A configuration object containing the database informations.
     * @param serviceID
     */
    public MDWebIndexer(Automatic configuration, String serviceID) throws IndexingException {
        super(serviceID, configuration.getConfigurationDirectory());
        if (configuration == null) {
            throw new IndexingException("The configuration object is null");
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new IndexingException("The configuration file does not contains a BDD object");
        }
        try {
            final DataSource dataSource   = db.getDataSource();
            final boolean isPostgres      = db.getClassName().equals("org.postgresql.Driver");
            String version                = null;
            final Connection mdConnection = dataSource.getConnection();
            final Statement versionStmt   = mdConnection.createStatement();
            final ResultSet result        = versionStmt.executeQuery("Select * FROM \"version\"");
            if (result.next()) {
                version = result.getString(1);
            }
            result.close();
            versionStmt.close();
            mdConnection.close();

            if (version != null && version.startsWith("2.0")) {
                mdWebReader = new Reader20(dataSource, isPostgres);
            } else if (version != null && version.startsWith("2.1")) {
                mdWebReader = new Reader21(dataSource, isPostgres);
            } else {
                throw new IndexingException("unexpected database version:" + version);
            }
            if (create)
                createIndex();
        } catch (SQLException ex) {
            throw new IndexingException("SQL Exception while creating mdweb indexer: " + ex.getMessage());
        } 
    }

    /**
     * Create a new Index from the MDweb database.
     *
     * @throws java.sql.SQLException
     */
    @Override
    public void createIndex() throws IndexingException {
        LOGGER.log(logLevel, "Creating lucene index for MDWeb database please wait...");

        final long time = System.currentTimeMillis();
        IndexWriter writer;
        int nbRecordSets = 0;
        int nbForms = 0;
        try {
            writer = new IndexWriter(new SimpleFSDirectory(getFileDirectory()), analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);

            // getting the objects list and index avery item in the IndexWriter.
            final List<RecordSet> cats       = mdWebReader.getRecordSets();
            final List<RecordSet> catToIndex = new ArrayList<RecordSet>();
            for (RecordSet r : cats) {
                if (r.getExposure() != EXPOSURE.INTERNAL) {
                    catToIndex.add(r);
                } else {
                    LOGGER.log(logLevel, "RecordSet:" + r.getCode() + " is internal we exclude it.");
                }
            }
            nbRecordSets = cats.size();
            final List<Form> results = mdWebReader.getAllForm(catToIndex);
            LOGGER.log(logLevel, results.size() + " forms read in " + (System.currentTimeMillis() - time) + " ms.");
            for (Form form : results) {
                if ((form.getType() == null || form.getType().equals(Form.TYPE.NORMALFORM)) && form.isPublished()) {
                    indexDocument(writer, form);
                    nbForms++;
                } else {
                     LOGGER.log(logLevel, "The form " + form.getTitle() + '(' + form.getId() + ") is a context (or is not published) so we don't index it");
                }
            }
            writer.optimize();
            writer.close();

        } catch (IOException ex) {
            LOGGER.severe(IO_SINGLE_MSG + ex.getMessage());
            throw new IndexingException("IOException while indexing documents:" + ex.getMessage(), ex);
        } catch (MD_IOException ex) {
            LOGGER.severe("SQLException while indexing document: " + ex.getMessage());
            throw new IndexingException("SQLException while indexing documents.", ex);
        }
        LOGGER.log(logLevel, "Index creation process in " + (System.currentTimeMillis() - time) + " ms" + '\n' +
                "RecordSets: " + nbRecordSets + " documents indexed: " + nbForms + ".");
    }

    /**
     * Create a new Index from a list of Form object.
     *
     * @throws java.sql.SQLException
     */
    @Override
    public void createIndex(List<? extends Object> forms) throws IndexingException {
        LOGGER.log(logLevel, "Creating lucene index for MDWeb database please wait...");

        final long time = System.currentTimeMillis();
        IndexWriter writer;
        final int nbRecordSets = 0;
        int nbForms = 0;
        try {
            writer = new IndexWriter(new SimpleFSDirectory(getFileDirectory()), analyzer, true,IndexWriter.MaxFieldLength.UNLIMITED);

            nbForms = forms.size();
            for (Object form : forms) {
                if (form instanceof Form) {
                    final Form ff = (Form) form;
                    if (ff.isPublished()) {
                        indexDocument(writer, ff);
                    } else {
                       LOGGER.log(logLevel, "The form " + ff.getId() + "is not published we don't index it");
                    }
                } else {
                    throw new IllegalArgumentException("The objects must be forms");
                }
            }
            writer.optimize();
            writer.close();

        } catch (IOException ex) {
            LOGGER.severe(IO_SINGLE_MSG + ex.getMessage());
            throw new IndexingException("IOException while indexing documents:" + ex.getMessage(), ex);
        }
        LOGGER.log(logLevel, "Index creation process in " + (System.currentTimeMillis() - time) + " ms" + '\n' +
                "RecordSets: " + nbRecordSets + " documents indexed: " + nbForms + ".");
    }

    /**
     * This method add to index of lucene a new document based on Form object.
     * (implements AbstractIndex.indexDocument() )
     *
     * @param writer A lucene Index Writer.
     * @param object A MDweb formular.
     */
    @Override
    public void indexDocument(IndexWriter writer, Form form) {
        try {
            writer.addDocument(createDocument(form));
            LOGGER.finer("Form: " + form.getTitle() + " indexed");

        } catch (IndexingException ex) {
            LOGGER.severe("IndexingException " + ex.getMessage());
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (IOException ex) {
            LOGGER.severe(IO_SINGLE_MSG + ex.getMessage());
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * This method add to index of lucene a new document based on Form object.
     * (implements AbstractIndex.indexDocument() )
     * object must be a Form.
     *
     * @param object A MDweb formular.
     */
    @Override
    public void indexDocument(Form form) {
        try {
            final IndexWriter writer = new IndexWriter(new SimpleFSDirectory(getFileDirectory()), analyzer, false,IndexWriter.MaxFieldLength.UNLIMITED);

            //adding the document in a specific model. in this case we use a MDwebDocument.
            writer.addDocument(createDocument(form));
            LOGGER.finer("Form: " + form.getTitle() + " indexed");

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
    * Makes a document for a MDWeb formular.
    *
    * @param Form An MDweb formular to index.
    * @return A Lucene document.
    */
    @Override
    protected Document createDocument(Form form) throws IndexingException {
        // make a new, empty document
        final Document doc = new Document();
        try {

            doc.add(new Field("id",        Integer.toString(form.getId()),             Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("recordSet", form.getRecordSet().getCode() , Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("Title",     form.getTitle(),                Field.Store.YES, Field.Index.ANALYZED));

            final Classe identifiable   = mdWebReader.getClasse("Identifiable", Standard.EBRIM_V3);
            final Classe registryObject = mdWebReader.getClasse("RegistryObject", Standard.EBRIM_V2_5);
            

            boolean alreadySpatiallyIndexed = false;
            
            if (form.getTopValue() == null) {
                LOGGER.warning("unable to index form:" + form.getId() + " top value is null");

            } else if (form.getTopValue().getType() == null) {
                LOGGER.warning("unable to index form:" + form.getId() + " top value type is null");

            // For an ISO 19115 form
            } else if (form.getTopValue().getType().getName().equals("MD_Metadata")) {

                alreadySpatiallyIndexed = indexMetadata(doc, form, ISO_QUERYABLE, false);

             // For an ebrim v 3.0 form
            } else if (form.getTopValue().getType().isSubClassOf(identifiable)) {
                LOGGER.finer("indexing Ebrim 3.0 Record");

               /*  TODO
                  for (String term :EBRIM_QUERYABLE.keySet()) {
                    doc.add(new Field(term, getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.TOKENIZED));
                    doc.add(new Field(term + "_sort", getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.UN_TOKENIZED));
                }*/

                 // For an ebrim v 2.5 form
            } else if (form.getTopValue().getType().isSubClassOf(registryObject)) {
                LOGGER.finer("indexing Ebrim 2.5 Record");

                /* TODO
                 for (String term :EBRIM_QUERYABLE.keySet()) {
                    doc.add(new Field(term, getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.TOKENIZED));
                    doc.add(new Field(term + "_sort", getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.UN_TOKENIZED));
                }*/


            // For a csw:Record (indexing is made in next generic indexing bloc)
            } else if (form.getTopValue().getType().getName().equals("Record")){
                LOGGER.finer("indexing CSW Record");

            } else {
                LOGGER.warning("unknow Form classe unable to index: " + form.getTopValue().getType().getName());
            }


            // All form types must be compatible with dublinCore.
            indexDublinCore(doc, form, DUBLIN_CORE_QUERYABLE, alreadySpatiallyIndexed);

            // add a default meta field to make searching all documents easy
            doc.add(new Field("metafile", "doc",Field.Store.YES, Field.Index.ANALYZED));
            
        } catch (MD_IOException ex) {
            throw new IndexingException(ex.getMessage());
        }
        return doc;
    }

    /**
     * Index a form of type MD_Metadata.
     *
     * @param doc The Lucene document to write on.
     * @param form The mdweb Form.
     *
     * @return true is the document already contains spatial information.
     * @throws MD_IOException
     */
    private boolean indexMetadata(Document doc, Form form, Map<String, List<String>> queryableSet, boolean alreadySpatiallyIndexed) throws MD_IOException {
        LOGGER.finer("indexing ISO 19115 MD_Metadata/FC_FeatureCatalogue");

        //TODO add ANyText
        for (String term :queryableSet.keySet()) {
            doc.add(new Field(term,           getValues(term,  form, queryableSet.get(term), -1),   Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field(term + "_sort", getValues(term,  form, queryableSet.get(term), -1),   Field.Store.YES, Field.Index.NOT_ANALYZED));
        }

        // add special INSPIRE queryable
        for (String term :INSPIRE_QUERYABLE.keySet()) {
            doc.add(new Field(term,           getValues(term,  form, INSPIRE_QUERYABLE.get(term), -1),   Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field(term + "_sort", getValues(term,  form, INSPIRE_QUERYABLE.get(term), -1),   Field.Store.YES, Field.Index.NOT_ANALYZED));
        }
        
       //we add the geometry parts
        if (!alreadySpatiallyIndexed) {
            return IndexSpatialPart(doc, form, queryableSet, -1);
        }
        return false;
    }

    /**
     * Index a form of all type with the common queryable element of Dublin Core.
     *
     * @param doc The Lucene document to write on.
     * @param form The mdweb Form.
     * @param alreadySpatiallyIndexed a flag indicating if the document already contains spatial information.
     * @throws MD_IOException
     */
    private boolean indexDublinCore(Document doc, Form form, Map<String, List<String>> queryableSet, boolean alreadySpatiallyIndexed) throws MD_IOException {
        final StringBuilder anyText = new StringBuilder();

        for (String term :queryableSet.keySet()) {
            final String values = getValues(term,  form, queryableSet.get(term), -1);
            if (!values.equals("null")) {
                anyText.append(values).append(" ");
            }
            doc.add(new Field(term,           values, Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field(term + "_sort", values, Field.Store.YES, Field.Index.NOT_ANALYZED));
        }

        //we add the anyText values
        doc.add(new Field("AnyText", anyText.toString(),   Field.Store.YES, Field.Index.ANALYZED));

        if (!alreadySpatiallyIndexed) {
            //we add the geometry parts
            return IndexSpatialPart(doc, form, queryableSet, 1);
        }
        return false;
    }

    private boolean IndexSpatialPart(Document doc, Form form, Map<String, List<String>> queryableSet, int ordinal) throws MD_IOException {
         List<String> coord = null;
         try {
            coord = getValueList(form, queryableSet.get("WestBoundLongitude"), ordinal);
            final List<Double> minxs = new ArrayList<Double>();
            for (String minx : coord) {
                minxs.add(Double.parseDouble(minx));
            }

            coord = getValueList(form, queryableSet.get("EastBoundLongitude"), ordinal);
            final List<Double> maxxs = new ArrayList<Double>();
            for (String maxx : coord) {
                maxxs.add(Double.parseDouble(maxx));
            }

            coord = getValueList(form, queryableSet.get("NorthBoundLatitude"), ordinal);
            final List<Double> maxys = new ArrayList<Double>();
            for (String maxy : coord) {
                maxys.add(Double.parseDouble(maxy));
            }

            coord = getValueList(form, queryableSet.get("SouthBoundLatitude"), ordinal);
            final List<Double> minys = new ArrayList<Double>();
            for (String miny : coord) {
                minys.add(Double.parseDouble(miny));
            }

            if (minxs.size() == minys.size() && minys.size() == maxxs.size() && maxxs.size() == maxys.size()) {
                if (minxs.size() == 1) {
                    addBoundingBox(doc, minxs.get(0), maxxs.get(0), minys.get(0), maxys.get(0), SRID_4326);
                    return true;
                } else if (minxs.size() > 0) {
                    addMultipleBoundingBox(doc, minxs, maxxs, minys, maxys, SRID_4326);
                    return true;
                }
            } else {
                LOGGER.warning("There is not the same number of coordinate: " + minxs.size() + " " + minys.size() + " " +  maxxs.size() + " " +  maxys.size());
            }
        } catch (NumberFormatException e) {
            if (coord != null) {
                LOGGER.warning("unable to spatially index form: " + form.getTitle() + '\n' +
                               "cause:  unable to parse double: " + coord);
            }
        }
        return false;
    }
    
    /**
     * Return a string description for the specified term.
     *
     * @param term An ISO queryable term defined in CSWQueryable (like Title, Subject, Abstract,...)
     * @param form An MDWeb formular from whitch we extract the values correspounding to the specified term.
     * @param queryable A map of queryable term and their correspounding paths.
     * @param ordinal If we want only one value for a path we add an ordinal to select the value we want. else put -1.
     *
     * @return A string concataining the differents values correspounding to the specified term, coma separated.
     */
    private String getValues(final String term, final Form form, final List<String> paths, final int ordinal) throws MD_IOException {
        final StringBuilder response  = new StringBuilder();

        // todo remove this i don't think we need it
        if (term.equalsIgnoreCase("Type") && form.getProfile() != null) {
            response.append(form.getProfile().getName()).append(',');
        }

        if (paths != null) {
            for (String fullPathID: paths) {
                final List<Value> values = getValuesFromPathID(fullPathID, form);
                for (final Value v: values) {
                    //only handle textvalue
                    if (!(v instanceof TextValue)) continue;
                    
                    final TextValue tv = (TextValue) v;

                    if (ordinal == -1 || ordinal == tv.getOrdinal()) {
                        response.append(getTextValueStringDescription(tv)).append(',');
                    }
                }
            }
        }

        if (response.length() == 0) {
            response.append("null");
        } else {
            // we remove the last ','
            response.deleteCharAt(response.length()-1);
        }

        return response.toString();
    }

    /**
     * Return a string description for the specified terms.
     *
     * @param term An ISO queryable term defined in CSWWorker (like Title, Subject, Abstract,...)
     * @param form An MDWeb formular from whitch we extract the values correspounding to the specified term.
     * @param ordinal If we want only one value for a path we add an ordinal to select the value we want. else put -1.
     *
     * @return A string concataining the differents values correspounding to the specified term, coma separated.
     */
    private List<String> getValueList(final Form form, final List<String> paths, final int ordinal) throws MD_IOException {
        final List<String> response   = new ArrayList<String>();
        if (paths != null) {
            for (String fullPathID: paths) {
                final List<Value> values = getValuesFromPathID(fullPathID, form);
                for (final Value v: values) {
                    //only handle textvalue
                    if (!(v instanceof TextValue)) continue;
                    final TextValue tv = (TextValue) v;

                    if (ordinal == -1 || ordinal == tv.getOrdinal()) {

                        final String value = getTextValueStringDescription(tv);
                        if (value != null) {
                            response.add(value);
                        }
                    }
                }
            }
        }
        return response;
    }


    /**
     * Return the String description of An MDWeb textValue :
     * For almost all the value it return TextValue.getValue()
     * but for the value with codeList type it return the label of the codelist element
     * instead of the code.
     * 
     * @param tv A TextValue
     *
     * @return S String label
     */
    private String getTextValueStringDescription(final TextValue tv) {
        final String value;

        if (tv.getType() instanceof CodeList) {
            value = getCodeListValue(tv);
        } else if (tv.getType().getName().equals("Date")) {
            value = toLuceneDateSyntax(tv.getValue());
        } else {
            value = tv.getValue();
        }
        return value;
    }
    
    /**
     *  Return a List of MDWeb Value from the specified path.
     * 
     * @param fullPathID
     * @param form
     * @return A list of Values.
     *
     * @throws MD_IOException
     */
    private List<Value> getValuesFromPathID(String fullPathID, final Form form) throws MD_IOException {
        final String pathID;
        Path conditionalPath     = null;
        String conditionalPathID = null;
        String conditionalValue  = null;

        // if the path ID contains a # we have a conditional value (codeList element) next to the searched value.
        final int separator = fullPathID.indexOf('#');
        if (separator != -1) {
            pathID            = fullPathID.substring(0, separator);
            conditionalPathID = pathID.substring(0, pathID.lastIndexOf(':') + 1) + fullPathID.substring(separator + 1, fullPathID.indexOf('='));
            conditionalValue  = fullPathID.substring(fullPathID.indexOf('=') + 1);
            LOGGER.finer("pathID           : " + pathID + '\n'
                    + "conditionalPathID: " + conditionalPathID + '\n'
                    + "conditionalValue : " + conditionalValue);
        } else {
            if (fullPathID.indexOf('[') != -1) {
                fullPathID = fullPathID.substring(0, fullPathID.indexOf('['));
            }
            pathID = fullPathID;
        }
        final Path path = mdWebReader.getPath(pathID);
        if (conditionalPathID != null) {
            conditionalPath = mdWebReader.getPath(conditionalPathID);
        }

        final List<Value> values;
        if (conditionalPath == null) {
            values = form.getValueFromPath(path);
        } else {
            values = Collections.singletonList(form.getConditionalValueFromPath(path, conditionalPath, conditionalValue));
        }
        return values;
    }

    /**
     * Return a Date representation in ISO syntax into Lucene date format
     * 
     * @param value
     * @return
     */
    private String toLuceneDateSyntax(String value) {
        if (value != null) {
            value = value.replaceAll("-", "");

            // TODO use time
            if (value.indexOf('T') != -1) {
                value = value.substring(0, value.indexOf('T'));
            }
        }
        return value;
    }

    /**
     * Return true if the specified Codelist contains locale Element
     *
     * @param cl An MDWeb CodeList
     *
     * @return True is the specified CodeList conatins Locale element.
     */
    private boolean isLocale(CodeList cl) {
        boolean locale = false;
        final List<Property> props = cl.getProperties();
        if (props != null && props.size() > 0) {
            locale = props.get(0) instanceof Locale;
        }
        return locale;
    }

    /**
     * Return the text associed with a codeList textValue.
     * 
     * @param tv A TextValue with a type instanceof CodeList.
     *
     * @return A text description of the codeList element.
     */
    private String getCodeListValue(TextValue tv) {
        //for a codelist value we don't write the code but the codelistElement value.
        final CodeList cl = (CodeList) tv.getType();
        final String result;

        // we look if the codelist contains locale element.
        final boolean locale = isLocale(cl);

        if (locale) {
            result = tv.getValue();

        } else {
            int code = 1;
            try {
                code = Integer.parseInt(tv.getValue());
            } catch (NumberFormatException ex) {
                LOGGER.warning("NumberFormat Exception while parsing a codelist code: " + tv.getValue());
            }
            final CodeListElement element = cl.getElementByCode(code);

            if (element != null) {
                result = element.getName();
            } else {
                LOGGER.warning("Unable to find a codelistElement for the code: " + code + " in the codelist: " + cl.getName());
                return null;
            }
        }
        return result;
    }

    @Override
    public void destroy() {
        try {
            mdWebReader.close();
        } catch (MD_IOException ex) {
            LOGGER.warning("MD IO Exception during destroying index while closing MDW reader:" + ex.getMessage());
        }
    }
}
