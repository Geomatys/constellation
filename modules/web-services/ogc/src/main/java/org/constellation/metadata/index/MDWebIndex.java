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

package org.constellation.metadata.index;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// apache Lucene dependencies
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;

// constellation dependencies
import org.constellation.ws.WebServiceException;
import static org.constellation.metadata.CSWQueryable.*;
import static org.constellation.ows.OWSExceptionCode.*;

// MDweb dependencies
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.CodeList;
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.schemas.Locale;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.schemas.Property;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.sql.Reader;
import org.mdweb.sql.v20.Reader20;

/**
 * A Lucene index handler for an MDWeb Database.
 * 
 * @author Guilhem Legal
 */
public class MDWebIndex extends AbstractIndexer<Form> {
    
    /**
     * The Reader of this lucene index (MDWeb DB mode).
     */
    private Reader MDWebReader;
    
     /**
     * A Map containg all the paths (used when reader is null)
     */
    private Map<String, Path> pathMap;
    
    /**
     * A Map containing some classes (used when reader is null)
     */
    private Map<String, Classe> classeMap;
    
     /**
     * Creates a new Lucene Index into the specified directory with the specified MDweb reader.
     * 
     * @param reader An mdweb reader for read the metadata database.
     * @param configDirectory A directory where the index can write indexation file. 
     */
    public MDWebIndex(Connection MDConnection, File configDirectory, String serviceID) throws WebServiceException {
        super(serviceID, configDirectory);
        try {
            MDWebReader   = new Reader20(Standard.ISO_19115,  MDConnection);
            pathMap       = null;
            classeMap     = null;
            if (create)
                createIndex();
        } catch (SQLException ex) {
            throw new WebServiceException("SQL Exception while creating mdweb reader: " +ex.getMessage(), NO_APPLICABLE_CODE);
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
    protected MDWebIndex(List<Form> forms, List<Classe> classes, List<Path> paths, File configDirectory) throws SQLException {
        super("", configDirectory);
        MDWebReader   = null;

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
        File f = new File(configDirectory, "index");
        
        setFileDirectory(f);
        
        //if the index File exists we don't need to index the documents again.
        if(!getFileDirectory().exists()) {
            logger.info("Creating lucene index for the first time...");
            long time = System.currentTimeMillis();
            IndexWriter writer;
            int nbCatalogs = 0;
            int nbForms    = 0; 
            try {
                writer = new IndexWriter(getFileDirectory(), analyzer, true);
                nbForms    =  forms.size();
                for (Form form : forms) {
                    indexDocument(writer, form);
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
                        "catalogs: " + nbCatalogs + " documents indexed: " + nbForms);
        } else {
            logger.info("Index already created");
        }
    }
    
    /**
     * Create a new Index from the MDweb database.
     * 
     * @throws java.sql.SQLException
     */
    public void createIndex() throws WebServiceException {
        logger.info("Creating lucene index for MDWeb database please wait...");
        
        long time = System.currentTimeMillis();
        IndexWriter writer;
        int nbCatalogs = 0;
        int nbForms = 0;
        try {
            writer = new IndexWriter(getFileDirectory(), analyzer, true);

            // getting the objects list and index avery item in the IndexWriter.
            List<Catalog> cats = MDWebReader.getCatalogs();
            nbCatalogs = cats.size();
            List<Form> results = MDWebReader.getAllForm(cats);
            logger.info("all form read in " + (System.currentTimeMillis() - time) + " ms.");
            nbForms = results.size();
            for (Form form : results) {
                indexDocument(writer, form);
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
        } catch (SQLException ex) {
            logger.severe("SQLException while indexing document: " + ex.getMessage());
            ex.printStackTrace();
        }
        logger.info("Index creation process in " + (System.currentTimeMillis() - time) + " ms" + '\n' +
                "catalogs: " + nbCatalogs + " documents indexed: " + nbForms + ".");
    }
    
    /**
     * This method add to index of lucene a new document based on Form object.
     * (implements AbstractIndex.indexDocument() )
     * 
     * @param writer A lucene Index Writer.
     * @param object A MDweb formular.
     */
    public void indexDocument(IndexWriter writer, Form form) {
        try {
            writer.addDocument(createDocument(form));
            logger.finer("Form: " + form.getTitle() + " indexed");
            
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
     * This method add to index of lucene a new document based on Form object.
     * (implements AbstractIndex.indexDocument() )
     * object must be a Form.
     * 
     * @param object A MDweb formular.
     */
    public void indexDocument(Form form) {
        try {
            IndexWriter writer = new IndexWriter(getFileDirectory(), analyzer, true);
            
            //adding the document in a specific model. in this case we use a MDwebDocument.
            writer.addDocument(createDocument(form));
            logger.info("Form: " + form.getTitle() + " indexed");
        
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
    * Makes a document for a MDWeb formular.
    * 
    * @param Form An MDweb formular to index.
    * @return A Lucene document.
    */
    protected Document createDocument(Form form) throws SQLException {
        
        // make a new, empty document
        Document doc = new Document();
        
        doc.add(new Field("id",      form.getId() + "",            Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("catalog", form.getCatalog().getCode() , Field.Store.YES, Field.Index.TOKENIZED));        
        doc.add(new Field("Title",   form.getTitle(),              Field.Store.YES, Field.Index.TOKENIZED));
        
        Classe identifiable, registryObject;
        if (MDWebReader == null) {
            identifiable   = classeMap.get("Identifiable");
            registryObject = classeMap.get("RegistryObject");
        } else {
            identifiable   = MDWebReader.getClasse("Identifiable", Standard.EBRIM_V3);
            registryObject = MDWebReader.getClasse("RegistryObject", Standard.EBRIM_V2_5);
        }
        
        if (form.getTopValue() == null) {
            logger.severe("unable to index form:" + form.getId() + " top value is null");
            
        } else if (form.getTopValue().getType() == null) {
            logger.severe("unable to index form:" + form.getId() + " top value type is null");
        
        // For an ISO 19115 form
        } else if (form.getTopValue().getType().getName().equals("MD_Metadata")) {
            
            logger.info("indexing ISO 19115 MD_Metadata/FC_FeatureCatalogue");
            //TODO add ANyText
            for (String term :ISO_QUERYABLE.keySet()) {
                doc.add(new Field(term, getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field(term + "_sort", getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.UN_TOKENIZED));
            }
        
           //we add the geometry parts
            String coord = "null";
            try {
                coord = getValues("WestBoundLongitude", form, ISO_QUERYABLE, -1);
                double minx = Double.parseDouble(coord);

                coord = getValues("EastBoundLongitude", form, ISO_QUERYABLE, -1);
                double maxx = Double.parseDouble(coord);
            
                coord = getValues("NorthBoundLatitude", form, ISO_QUERYABLE, -1);
                double maxy = Double.parseDouble(coord);
            
                coord = getValues("SouthBoundLatitude", form, ISO_QUERYABLE, -1);
                double miny = Double.parseDouble(coord);
                
                addBoundingBox(doc, minx, maxx, miny, maxy, "EPSG:4326");
            
            } catch (NumberFormatException e) {
                if (!coord.equals("null"))
                    logger.severe("unable to spatially index form: " + form.getTitle() + '\n' +
                                  "cause:  unable to parse double: " + coord);
            }
            
         // For an ebrim v 3.0 form    
        } else if (form.getTopValue().getType().isSubClassOf(identifiable)) {
            logger.info("indexing Ebrim 3.0 Record");
            
           /* for (String term :EBRIM_QUERYABLE.keySet()) {
                doc.add(new Field(term, getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field(term + "_sort", getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.UN_TOKENIZED));
            }*/
        
             // For an ebrim v 2.5 form    
        } else if (form.getTopValue().getType().isSubClassOf(registryObject)) {
            logger.info("indexing Ebrim 2.5 Record");
            
            /*for (String term :EBRIM_QUERYABLE.keySet()) {
                doc.add(new Field(term, getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field(term + "_sort", getValues(term,  form, ISO_QUERYABLE, -1),   Field.Store.YES, Field.Index.UN_TOKENIZED));
            }*/
            
            
        // For a csw:Record (indexing is made in next generic indexing bloc)
        } else if (form.getTopValue().getType().getName().equals("Record")){
            logger.info("indexing CSW Record");
            
        } else {
            logger.severe("unknow Form classe unable to index: " + form.getTopValue().getType().getName());
        }
        
        
        // All form types must be compatible with dublinCore.
        
        StringBuilder anyText = new StringBuilder();
        for (String term :DUBLIN_CORE_QUERYABLE.keySet()) {
                
            String values = getValues(term,  form, DUBLIN_CORE_QUERYABLE, -1);
            if (!values.equals("null")) {
                logger.finer("put " + term + " values: " + values);
                anyText.append(values).append(" ");
            }
            if (term.equals("date") || term.equals("modified")) {
                values = values.replaceAll("-","");
            }
            doc.add(new Field(term, values,   Field.Store.YES, Field.Index.TOKENIZED));
            doc.add(new Field(term + "_sort", values,   Field.Store.YES, Field.Index.UN_TOKENIZED));
        }
            
        //we add the anyText values
        doc.add(new Field("AnyText", anyText.toString(),   Field.Store.YES, Field.Index.TOKENIZED));
            
        //we add the geometry parts
        String coord = "null";
        try {
            coord = getValues("WestBoundLongitude", form, DUBLIN_CORE_QUERYABLE, 1);
            double minx = Double.parseDouble(coord);
                
            coord = getValues("EastBoundLongitude", form, DUBLIN_CORE_QUERYABLE, 1);
            double maxx = Double.parseDouble(coord);
            
            coord = getValues("NorthBoundLatitude", form, DUBLIN_CORE_QUERYABLE, 2);
            double maxy = Double.parseDouble(coord);
            
            coord = getValues("SouthBoundLatitude", form, DUBLIN_CORE_QUERYABLE, 2);
            double miny = Double.parseDouble(coord);
                
            coord = getValues("SouthBoundLatitude", form, DUBLIN_CORE_QUERYABLE, 2);
            
            String crs = getValues("CRS", form, DUBLIN_CORE_QUERYABLE, -1);
                
            addBoundingBox(doc, minx, maxx, miny, maxy, crs);
            
        } catch (NumberFormatException e) {
            if (!coord.equals("null"))
                logger.severe("unable to spatially index form: " + form.getTitle() + '\n' +
                              "cause:  unable to parse double: " + coord);
        }
        
        
        
        // add a default meta field to make searching all documents easy 
	doc.add(new Field("metafile", "doc",Field.Store.YES, Field.Index.TOKENIZED));
        
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
    private String getValues(String term, Form form, Map<String,List<String>> queryable, int ordinal) throws SQLException {
        StringBuilder response  = new StringBuilder("");
        List<String> paths = queryable.get(term);
        
        if (term.equalsIgnoreCase("Type") && form.getProfile() != null) {
            response.append(form.getProfile().getName()).append(','); 
        }
            
        if (paths != null) {
            for (String fullPathID: paths) {
                Path path;
                String pathID;
                Path conditionalPath     = null;
                String conditionalPathID = null;
                String conditionalValue  = null;
                
                // if the path ID contains a # we have a conditional value (codeList element) next to the searched value.
                int separator = fullPathID.indexOf('#'); 
                if (separator != -1) {
                    pathID            = fullPathID.substring(0, separator);
                    conditionalPathID = pathID.substring(0, pathID.lastIndexOf(':') + 1) + fullPathID.substring(separator + 1, fullPathID.indexOf('='));
                    conditionalValue  = fullPathID.substring(fullPathID.indexOf('=') + 1);
                    logger.finer("pathID           : " + pathID            + '\n' +
                                "conditionalPathID: " + conditionalPathID + '\n' +
                                "conditionalValue : " + conditionalValue); 
                } else {
                    pathID = fullPathID;
                }
                
                if (MDWebReader != null) {
                    path   = MDWebReader.getPath(pathID);
                    if (conditionalPathID != null)
                        conditionalPath = MDWebReader.getPath(conditionalPathID);
                } else {
                    path = pathMap.get(pathID);
                    if (conditionalPathID != null)
                        conditionalPath = pathMap.get(conditionalPathID);
                }
                List<Value> values;
                if (conditionalPath == null) {
                    values = form.getValueFromPath(path);
                } else {
                    values = new ArrayList<Value>();
                    values.add(form.getConditionalValueFromPath(path, conditionalPath, conditionalValue));
                }
                for (Value v: values) {
                    if ( (ordinal == -1 && v instanceof TextValue) || (v instanceof TextValue && v.getOrdinal() == ordinal)) {
                
                        TextValue tv = (TextValue) v;
                        
                        //for a codelist value we don't write the code but the codlistElement value.
                        if (tv.getType() instanceof CodeList) {
                            CodeList cl = (CodeList) tv.getType();
                            
                            // we look if the codelist contains locale element.
                            boolean locale = false;
                            List<Property> props = cl.getProperties();
                            if (props != null && props.size() > 0) {
                                if (props.get(0) instanceof Locale)
                                    locale = true;
                            }
                            
                            if (locale) {
                                response.append(tv.getValue()).append(','); 
                                
                            } else {
                                int code = 1;
                                try {
                                    code = Integer.parseInt(tv.getValue());
                                } catch (NumberFormatException ex) {
                                    logger.severe("NumberFormat Exception while parsing a codelist code: " + tv.getValue());
                                }
                                CodeListElement element = cl.getElementByCode(code); 
                            
                                if (element != null) {
                                    response.append(element.getName()).append((','));
                                } else {
                                    logger.severe("Unable to find a codelistElement for the code: " + code + " in the codelist: " + cl.getName());
                                } 
                            }
                        } else if (tv.getType().getName().equals("Date")) {
                            String value = tv.getValue();
                            value = value.replaceAll("-", "");
                            response.append(value).append(',');
                        // else we write the text value.    
                        } else {
                            response.append(tv.getValue()).append(','); 
                        }
                    } 
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
    
    public void destroy() {
        if (pathMap != null)
            pathMap.clear();
        if (classeMap != null)
            classeMap.clear();
        try {
            MDWebReader.close();
        } catch (SQLException ex) {
            logger.severe("SQL Exception while destroying index");
        }
    }
}
