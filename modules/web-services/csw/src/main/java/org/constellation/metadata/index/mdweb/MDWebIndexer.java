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
import javax.sql.DataSource;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

// constellation dependencies
import org.apache.lucene.store.SimpleFSDirectory;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.index.AbstractCSWIndexer;

import org.geotoolkit.lucene.IndexingException;
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
public class MDWebIndexer extends AbstractCSWIndexer<Form> {

    /**
     * The Reader of this lucene index (MDWeb DB mode).
     */
    private Reader mdWebReader;

    private Classe identifiable;

    private Classe registryObject;

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
            initEbrimClasses();
            if (create)
                createIndex();
        } catch (SQLException ex) {
            throw new IndexingException("SQL Exception while creating mdweb indexer: " + ex.getMessage());
        } catch (MD_IOException ex) {
            throw new IndexingException("MD_IO Exception while creating mdweb indexer(during Ebrim classes reading): " + ex.getMessage());
        }
    }

    private void initEbrimClasses() throws MD_IOException {
        identifiable   = mdWebReader.getClasse("Identifiable", Standard.EBRIM_V3);
        registryObject = mdWebReader.getClasse("RegistryObject", Standard.EBRIM_V2_5);
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
    public void createIndex(List<Form> forms) throws IndexingException {
        LOGGER.log(logLevel, "Creating lucene index for MDWeb database please wait...");

        final long time = System.currentTimeMillis();
        IndexWriter writer;
        final int nbRecordSets = 0;
        int nbForms = 0;
        try {
            writer = new IndexWriter(new SimpleFSDirectory(getFileDirectory()), analyzer, true,IndexWriter.MaxFieldLength.UNLIMITED);

            nbForms = forms.size();
            for (Form form : forms) {
                if (form.isPublished()) {
                    indexDocument(writer, form);
                } else {
                   LOGGER.log(logLevel, "The form " + form.getId() + "is not published we don't index it");
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
     * Find the identifier of the metadata
     *
     * @param obj
     * @return
     */
    @Override
    protected String getIdentifier(Form obj) {
        return obj.getTitle();
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
        
        doc.add(new Field("id",        Integer.toString(form.getId()),   Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("recordSet", form.getRecordSet().getCode() , Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("Title",     form.getTitle(),                Field.Store.YES, Field.Index.ANALYZED));

         if (form.getTopValue() == null) {
            throw new IndexingException("unable to index form:" + form.getId() + " top value is null");

        } else if (form.getTopValue().getType() == null) {
            throw new IndexingException("unable to index form:" + form.getId() + " top value type is null");
        }

        final StringBuilder anyText     = new StringBuilder();
        boolean alreadySpatiallyIndexed = false;

        if (isISO19139(form)) {
            alreadySpatiallyIndexed = indexISO19139(doc, form, ISO_QUERYABLE, anyText, false);
        } else if (isEbrim30(form)) {
           // TODO
        } else if (isEbrim25(form)) {
            // TODO
        } else if (!isDublinCore(form)) {
            LOGGER.warning("unknow Form classe unable to index: " + form.getTopValue().getType().getName());
        }


        // All form types must be compatible with dublinCore.
        indexDublinCore(doc, form, DUBLIN_CORE_QUERYABLE, anyText, alreadySpatiallyIndexed);

        // add a default meta field to make searching all documents easy
        doc.add(new Field("metafile", "doc",Field.Store.YES, Field.Index.ANALYZED));

        //we add the anyText values
        doc.add(new Field("AnyText", anyText.toString(),   Field.Store.YES, Field.Index.ANALYZED));


        return doc;
    }

    @Override
    protected boolean isISO19139(Form form) {
       return form.getTopValue().getType().getName().equals("MD_Metadata");
    }

    @Override
    protected boolean isDublinCore(Form form) {
        return form.getTopValue().getType().getName().equals("Record");
    }

    @Override
    protected boolean isEbrim25(Form form) {
        return form.getTopValue().getType().isSubClassOf(registryObject);
    }

    @Override
    protected boolean isEbrim30(Form form) {
        return form.getTopValue().getType().isSubClassOf(identifiable);
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
    @Override
    protected boolean indexISO19139(final Document doc, final Form form, Map<String, List<String>> queryableSet, final StringBuilder anyText, boolean alreadySpatiallyIndexed) throws IndexingException {
        LOGGER.finer("indexing ISO 19115 MD_Metadata/FC_FeatureCatalogue");

        try {
            for (String term :queryableSet.keySet()) {
                final String values = getValues(form, queryableSet.get(term));
                if (!values.equals("null")) {
                    anyText.append(values).append(" ");
                }
                doc.add(new Field(term,           values,   Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field(term + "_sort", values,   Field.Store.YES, Field.Index.NOT_ANALYZED));
            }

            // add special INSPIRE queryable
            for (String term :INSPIRE_QUERYABLE.keySet()) {
                final String values = getValues(form, INSPIRE_QUERYABLE.get(term));
                if (!values.equals("null")) {
                    anyText.append(values).append(" ");
                }
                doc.add(new Field(term,           values,   Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field(term + "_sort", values,   Field.Store.YES, Field.Index.NOT_ANALYZED));
            }

           //we add the geometry parts
            if (!alreadySpatiallyIndexed) {
                return indexSpatialPart(doc, form, queryableSet);
            }
            return false;
        } catch (MD_IOException ex) {
            throw new IndexingException("error while indexing ISO metadata", ex);
        }
    }

    /**
     * Index a form of all type with the common queryable element of Dublin Core.
     *
     * @param doc The Lucene document to write on.
     * @param form The mdweb Form.
     * @param alreadySpatiallyIndexed a flag indicating if the document already contains spatial information.
     * @throws MD_IOException
     */
    @Override
    protected boolean indexDublinCore(Document doc, Form form, Map<String, List<String>> queryableSet, final StringBuilder anyText, boolean alreadySpatiallyIndexed) throws IndexingException {
        try {
            for (String term :queryableSet.keySet()) {
                final String values = getValues(form, queryableSet.get(term));
                if (!values.equals("null")) {
                    anyText.append(values).append(" ");
                }
                doc.add(new Field(term,           values, Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field(term + "_sort", values, Field.Store.YES, Field.Index.NOT_ANALYZED));
            }

            //we add the geometry parts
            if (!alreadySpatiallyIndexed) {
                return indexSpatialPart(doc, form, queryableSet);
            }
            return false;
        } catch (MD_IOException ex) {
            throw new IndexingException("error while indexing DublinCore metadata", ex);
        }
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
    private String getValues(final Form form, final List<String> paths) throws MD_IOException {
        final StringBuilder response  = new StringBuilder();

        if (paths != null) {
            for (String fullPathID: paths) {
                final List<Value> values = getValuesFromPathID(fullPathID, form);
                for (final Value v: values) {
                    //only handle textvalue
                    if (!(v instanceof TextValue)) continue;
                    
                    final TextValue tv = (TextValue) v;
                    response.append(getTextValueStringDescription(tv)).append(',');
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
     * @param form An MDWeb formular from whitch we extract the values correspounding to the specified term.
     * @param ordinal If we want only one value for a path we add an ordinal to select the value we want. else put -1.
     *
     * @return A string concataining the differents values correspounding to the specified term, coma separated.
     */
    @Override
    protected List<Double> extractPositions(final Form form, final List<String> paths) throws IndexingException {
        final List<Double> response   = new ArrayList<Double>();
        if (paths != null) {
            for (String fullPathID: paths) {
                try {
                    final List<Value> values = getValuesFromPathID(fullPathID, form);
                    for (final Value v: values) {
                        //only handle textvalue
                        if (!(v instanceof TextValue)) continue;
                        final TextValue tv = (TextValue) v;
                        final String value = getTextValueStringDescription(tv);
                        if (value != null) {
                            try {
                                response.add(Double.parseDouble(value));
                            } catch (NumberFormatException e) {
                                LOGGER.warning("unable to spatially index form: " + form.getTitle() + "\n cause:  unable to parse double: " + value);
                            }
                        }
                    }
                } catch (MD_IOException ex) {
                    throw new IndexingException("MD_IO exception while getValue from pathID", ex);
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
        int ordinal              = -1;
        
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
                String StringOrdinal = fullPathID.substring(fullPathID.indexOf('[') + 1, fullPathID.indexOf(']'));
                try {
                    ordinal = Integer.parseInt(StringOrdinal);
                    // mdweb ordinal start at 1
                    ordinal++;
                } catch (NumberFormatException ex) {
                    LOGGER.warning("unable to parse the ordinal:" + StringOrdinal);
                    ordinal = -1;
                }
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
            if (ordinal != -1) {
                List<Value> toRemove = new ArrayList<Value>();
                for (Value v : values) {
                    if (v.getOrdinal() != ordinal) {
                        toRemove.add(v);
                    }
                }
                values.removeAll(toRemove);
            }

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
