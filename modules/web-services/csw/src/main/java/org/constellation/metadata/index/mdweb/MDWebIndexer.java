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
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;

// constellation dependencies
import org.apache.lucene.store.SimpleFSDirectory;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;

import org.geotoolkit.geometry.jts.SRIDGenerator;
import org.geotoolkit.geometry.jts.SRIDGenerator.Version;
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
     * A Map containg all the paths (used when reader is null)
     */
    private Map<String, Path> pathMap;

    /**
     * A Map containing some classes (used when reader is null)
     */
    private Map<String, Classe> classeMap;

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
            final Connection mdConnection = db.getConnection();
            boolean isPostgres = db.getClassName().equals("org.postgresql.Driver");
            mdWebReader   = new Reader20(mdConnection, isPostgres);
            pathMap       = null;
            classeMap     = null;
            if (create)
                createIndex();
        } catch (SQLException ex) {
            throw new IndexingException("SQL Exception while creating mdweb indexer: " + ex.getMessage());
        } 
    }

    /**
     * Creates a new Lucene Index with the specified list of Form.
     * This mode of lucene index don't use a MDweb reader (he don't need a database).
     * for now it is used only in JUnit test.
     *
     * @param forms The list of MDweb formular to index
     * @param paths The list of path used in the forms (necesary because of there is no reader)
     * @param configDirectory A directory where the index can write indexation file.
     */
    protected MDWebIndexer(List<Form> forms, List<Classe> classes, List<Path> paths, File configDirectory) throws IndexingException {
        super("", configDirectory);
        mdWebReader   = null;

        //we fill the map of classe
        classeMap = new HashMap<String, Classe>();
        for (Classe c: classes) {
            classeMap.put(c.getName(), c);
        }

        //we fill the map of path
        pathMap = new HashMap<String, Path>();
        for (Path p: paths) {
            pathMap.put(p.getId(), p);
        }

        // we get the configuration file
        final File f = new File(configDirectory, "index");

        setFileDirectory(f);

        //if the index File exists we don't need to index the documents again.
        if(!getFileDirectory().exists()) {
            LOGGER.info("Creating lucene index for the first time...");
            final long time = System.currentTimeMillis();
            IndexWriter writer;
            final int nbRecordSets = 0;
            int nbForms    = 0;
            try {
                writer  = new IndexWriter(new SimpleFSDirectory(getFileDirectory()), analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
                nbForms =  forms.size();
                for (Form form : forms) {
                   // if (form.isFullyValidated()) {
                        indexDocument(writer, form);
                   // } else {
                   //     logger.info("The form " + form.getId() + "is not validated we don't index it");
                   // }
                }
                writer.optimize();
                writer.close();

            } catch (CorruptIndexException ex) {
                LOGGER.severe(CORRUPTED_SINGLE_MSG + ex.getMessage());
                throw new IndexingException(CORRUPTED_MULTI_MSG, ex);
            } catch (LockObtainFailedException ex) {
                LOGGER.severe(LOCK_SINGLE_MSG + ex.getMessage());
                throw new IndexingException(LOCK_MULTI_MSG, ex);
            } catch (IOException ex) {
                LOGGER.severe(IO_SINGLE_MSG + ex.getMessage());
                throw new IndexingException("IOException while indexing documents:" + ex.getMessage(), ex);
            }
            LOGGER.info("Index creation process in " + (System.currentTimeMillis() - time) + " ms" + '\n' +
                        "RecordSets: " + nbRecordSets + " documents indexed: " + nbForms);
        } else {
            LOGGER.info("Index already created");
        }
    }

    /**
     * Create a new Index from the MDweb database.
     *
     * @throws java.sql.SQLException
     */
    @Override
    public void createIndex() throws IndexingException {
        LOGGER.info("Creating lucene index for MDWeb database please wait...");

        final long time = System.currentTimeMillis();
        IndexWriter writer;
        int nbRecordSets = 0;
        int nbForms = 0;
        try {
            writer = new IndexWriter(new SimpleFSDirectory(getFileDirectory()), analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);

            // getting the objects list and index avery item in the IndexWriter.
            final List<RecordSet> cats = mdWebReader.getRecordSets();
            nbRecordSets = cats.size();
            final List<Form> results = mdWebReader.getAllForm(cats);
            LOGGER.info(results.size() + " forms read in " + (System.currentTimeMillis() - time) + " ms.");
            for (Form form : results) {
                if ((form.getType() == null || !form.getType().equals("templateForm")) && form.isPublished()) {
                    indexDocument(writer, form);
                    nbForms++;
                } else {
                     LOGGER.info("The form " + form.getId() + " is a context (or is not published) so we don't index it");
                }
            }
            writer.optimize();
            writer.close();

        } catch (CorruptIndexException ex) {
            LOGGER.severe(CORRUPTED_SINGLE_MSG + ex.getMessage());
            throw new IndexingException(CORRUPTED_MULTI_MSG, ex);
        } catch (LockObtainFailedException ex) {
            LOGGER.severe(LOCK_SINGLE_MSG + ex.getMessage());
            throw new IndexingException(LOCK_MULTI_MSG, ex);
        } catch (IOException ex) {
            LOGGER.severe(IO_SINGLE_MSG + ex.getMessage());
            throw new IndexingException("IOException while indexing documents:" + ex.getMessage(), ex);
        } catch (MD_IOException ex) {
            LOGGER.severe("SQLException while indexing document: " + ex.getMessage());
            throw new IndexingException("SQLException while indexing documents.", ex);
        }
        LOGGER.info("Index creation process in " + (System.currentTimeMillis() - time) + " ms" + '\n' +
                "RecordSets: " + nbRecordSets + " documents indexed: " + nbForms + ".");
    }

    /**
     * Create a new Index from a list of Form object.
     *
     * @throws java.sql.SQLException
     */
    @Override
    public void createIndex(List<? extends Object> forms) throws IndexingException {
        LOGGER.info("Creating lucene index for MDWeb database please wait...");

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
                    //if (ff.isFullyValidated()) {
                        indexDocument(writer, ff);
                    //} else {
                    //   logger.info("The form " + ff.getId() + "is not validated we don't index it");
                    //}
                } else {
                    throw new IllegalArgumentException("The objects must be forms");
                }
            }
            writer.optimize();
            writer.close();

        } catch (CorruptIndexException ex) {
            LOGGER.severe(CORRUPTED_SINGLE_MSG + ex.getMessage());
            throw new IndexingException(CORRUPTED_MULTI_MSG, ex);
        } catch (LockObtainFailedException ex) {
            LOGGER.severe(LOCK_SINGLE_MSG + ex.getMessage());
            throw new IndexingException(LOCK_MULTI_MSG, ex);
        } catch (IOException ex) {
            LOGGER.severe(IO_SINGLE_MSG + ex.getMessage());
            throw new IndexingException("IOException while indexing documents:" + ex.getMessage(), ex);
        }
        LOGGER.info("Index creation process in " + (System.currentTimeMillis() - time) + " ms" + '\n' +
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
        } catch (CorruptIndexException ex) {
            LOGGER.severe(CORRUPTED_SINGLE_MSG + ex.getMessage());
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
        } catch (CorruptIndexException ex) {
            LOGGER.severe(CORRUPTED_SINGLE_MSG + ex.getMessage());
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

            doc.add(new Field("id",        form.getId() + "",             Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("recordSet", form.getRecordSet().getCode() , Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("Title",     form.getTitle(),                Field.Store.YES, Field.Index.ANALYZED));

            Classe identifiable, registryObject;
            if (mdWebReader == null) {
                identifiable   = classeMap.get("Identifiable");
                registryObject = classeMap.get("RegistryObject");
            } else {
                identifiable   = mdWebReader.getClasse("Identifiable", Standard.EBRIM_V3);
                registryObject = mdWebReader.getClasse("RegistryObject", Standard.EBRIM_V2_5);
            }

            if (form.getTopValue() == null) {
                LOGGER.severe("unable to index form:" + form.getId() + " top value is null");

            } else if (form.getTopValue().getType() == null) {
                LOGGER.severe("unable to index form:" + form.getId() + " top value type is null");

            // For an ISO 19115 form
            } else if (form.getTopValue().getType().getName().equals("MD_Metadata")) {

                LOGGER.finer("indexing ISO 19115 MD_Metadata/FC_FeatureCatalogue");
                //TODO add ANyText
                for (String term :ISO_QUERYABLE.keySet()) {
                    doc.add(new Field(term, getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.ANALYZED));
                    doc.add(new Field(term + "_sort", getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.NOT_ANALYZED));
                }

               //we add the geometry parts
                String coord = "null";
                try {
                    coord = getValues("WestBoundLongitude", form, ISO_QUERYABLE, -1);
                    final double minx = Double.parseDouble(coord);

                    coord = getValues("EastBoundLongitude", form, ISO_QUERYABLE, -1);
                    final double maxx = Double.parseDouble(coord);

                    coord = getValues("NorthBoundLatitude", form, ISO_QUERYABLE, -1);
                    final double maxy = Double.parseDouble(coord);

                    coord = getValues("SouthBoundLatitude", form, ISO_QUERYABLE, -1);
                    final double miny = Double.parseDouble(coord);

                    addBoundingBox(doc, minx, maxx, miny, maxy, SRID_4326);

                } catch (NumberFormatException e) {
                    if (!coord.equals("null"))
                        LOGGER.severe("unable to spatially index form: " + form.getTitle() + '\n' +
                                      "cause:  unable to parse double: " + coord);
                }

                for (String term :INSPIRE_QUERYABLE.keySet()) {
                    doc.add(new Field(term, getValues(term,  form, INSPIRE_QUERYABLE, -1),   Field.Store.YES, Field.Index.ANALYZED));
                    doc.add(new Field(term + "_sort", getValues(term,  form, INSPIRE_QUERYABLE, -1),   Field.Store.YES, Field.Index.NOT_ANALYZED));
                }

             // For an ebrim v 3.0 form
            } else if (form.getTopValue().getType().isSubClassOf(identifiable)) {
                LOGGER.finer("indexing Ebrim 3.0 Record");

               /* for (String term :EBRIM_QUERYABLE.keySet()) {
                    doc.add(new Field(term, getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.TOKENIZED));
                    doc.add(new Field(term + "_sort", getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.UN_TOKENIZED));
                }*/

                 // For an ebrim v 2.5 form
            } else if (form.getTopValue().getType().isSubClassOf(registryObject)) {
                LOGGER.finer("indexing Ebrim 2.5 Record");

                /*for (String term :EBRIM_QUERYABLE.keySet()) {
                    doc.add(new Field(term, getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.TOKENIZED));
                    doc.add(new Field(term + "_sort", getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.UN_TOKENIZED));
                }*/


            // For a csw:Record (indexing is made in next generic indexing bloc)
            } else if (form.getTopValue().getType().getName().equals("Record")){
                LOGGER.finer("indexing CSW Record");

            } else {
                LOGGER.severe("unknow Form classe unable to index: " + form.getTopValue().getType().getName());
            }


            // All form types must be compatible with dublinCore.

            final StringBuilder anyText = new StringBuilder();
            for (String term :DUBLIN_CORE_QUERYABLE.keySet()) {

                String values = getValues(term,  form, DUBLIN_CORE_QUERYABLE, -1);
                if (!values.equals("null")) {
                    LOGGER.finer("put " + term + " values: " + values);
                    anyText.append(values).append(" ");
                }
    
                doc.add(new Field(term, values,   Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field(term + "_sort", values,   Field.Store.YES, Field.Index.NOT_ANALYZED));
            }

            //we add the anyText values
            doc.add(new Field("AnyText", anyText.toString(),   Field.Store.YES, Field.Index.ANALYZED));

            //we add the geometry parts
            String coord = "null";
            try {
                coord = getValues("WestBoundLongitude", form, DUBLIN_CORE_QUERYABLE, 1);
                final double minx = Double.parseDouble(coord);

                coord = getValues("EastBoundLongitude", form, DUBLIN_CORE_QUERYABLE, 1);
                final double maxx = Double.parseDouble(coord);

                coord = getValues("NorthBoundLatitude", form, DUBLIN_CORE_QUERYABLE, 2);
                final double maxy = Double.parseDouble(coord);

                coord = getValues("SouthBoundLatitude", form, DUBLIN_CORE_QUERYABLE, 2);
                final double miny = Double.parseDouble(coord);

                coord = getValues("SouthBoundLatitude", form, DUBLIN_CORE_QUERYABLE, 2);

                String crs = getValues("CRS", form, DUBLIN_CORE_QUERYABLE, -1);

                if(crs == null || "null".equalsIgnoreCase(crs)){
                    crs = "CRS:84";
                }
                int srid = 4326;
                try {
                    srid = SRIDGenerator.toSRID(crs, Version.V1);
                } catch(IllegalArgumentException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }
                
                addBoundingBox(doc, minx, maxx, miny, maxy, srid);

            } catch (NumberFormatException e) {
                if (!coord.equals("null"))
                    LOGGER.severe("unable to spatially index form: " + form.getTitle() + '\n' +
                                  "cause:  unable to parse double: " + coord);
            }

            // add a default meta field to make searching all documents easy
            doc.add(new Field("metafile", "doc",Field.Store.YES, Field.Index.ANALYZED));
            
        } catch (MD_IOException ex) {
            throw new IndexingException(ex.getMessage());
        }
        return doc;
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
    private String getValues(final String term, final Form form, final Map<String,List<String>> queryable,
            final int ordinal) throws MD_IOException {

        final StringBuilder response  = new StringBuilder();
        final List<String> paths      = queryable.get(term);

        if (term.equalsIgnoreCase("Type") && form.getProfile() != null) {
            response.append(form.getProfile().getName()).append(',');
        }

        if (paths != null) {
            for (String fullPathID: paths) {
                final Path path;
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
                    LOGGER.finer("pathID           : " + pathID            + '\n' +
                                "conditionalPathID: " + conditionalPathID + '\n' +
                                "conditionalValue : " + conditionalValue);
                } else {
                    if (fullPathID.indexOf('[') != -1) {
                        fullPathID = fullPathID.substring(0, fullPathID.indexOf('['));
                    }
                    pathID = fullPathID;
                }

                if (mdWebReader != null) {
                    path = mdWebReader.getPath(pathID);
                    if (conditionalPathID != null)
                        conditionalPath = mdWebReader.getPath(conditionalPathID);
                } else {
                    path = pathMap.get(pathID);
                    if (conditionalPathID != null)
                        conditionalPath = pathMap.get(conditionalPathID);
                }

                final List<Value> values;
                if (conditionalPath == null) {
                    values = form.getValueFromPath(path);
                } else {
                    values = Collections.singletonList(
                            form.getConditionalValueFromPath(path, conditionalPath, conditionalValue));
                }

                for (final Value v: values) {
                    //only handle textvalue
                    if (!(v instanceof TextValue)) continue;
                    final TextValue tv = (TextValue) v;

                    if (ordinal == -1 || ordinal == tv.getOrdinal()) {

                        //for a codelist value we don't write the code but the codelistElement value.
                        if (tv.getType() instanceof CodeList) {
                            final CodeList cl = (CodeList) tv.getType();

                            // we look if the codelist contains locale element.
                            boolean locale = false;
                            final List<Property> props = cl.getProperties();
                            if (props != null && props.size() > 0) {
                                locale = props.get(0) instanceof Locale;
                            }

                            if (locale) {
                                response.append(tv.getValue()).append(',');

                            } else {
                                int code = 1;
                                try {
                                    code = Integer.parseInt(tv.getValue());
                                } catch (NumberFormatException ex) {
                                    LOGGER.severe("NumberFormat Exception while parsing a codelist code: " + tv.getValue());
                                }
                                final CodeListElement element = cl.getElementByCode(code);

                                if (element != null) {
                                    response.append(element.getName()).append(',');
                                } else {
                                    LOGGER.severe("Unable to find a codelistElement for the code: " + code + " in the codelist: " + cl.getName());
                                }
                            }
                        } else if (tv.getType().getName().equals("Date")) {
                            String value = tv.getValue();
                            if (value != null) {
                                value = value.replaceAll("-", "");
                            
                                // TODO use time
                                if (value.indexOf('T') != -1) {
                                    value = value.substring(0, value.indexOf('T'));
                                }
                            }
                            response.append(value).append(',');
                        // else we write the text value.
                        } else {
                            response.append(tv.getValue()).append(',');
                        }
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

    @Override
    public void destroy() {
        if (pathMap != null)
            pathMap.clear();
        if (classeMap != null)
            classeMap.clear();
        try {
            mdWebReader.close();
        } catch (MD_IOException ex) {
            LOGGER.severe("SQL Exception while destroying index:" + ex.getMessage());
        }
    }
}
