/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.metadata;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

// Lucene dependencies
import org.constellation.lucene.filter.SerialChainFilter;
import org.constellation.lucene.filter.SpatialQuery;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.LockObtainFailedException;

// MDWeb dependencies
import org.mdweb.lucene.AbstractIndex;
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.sql.Reader;

// Constellation dependencies
import static org.constellation.metadata.CSWQueryable.*;


/**
 *
 * @author Guilehm Legal
 */
public class IndexLucene extends AbstractIndex {

    private final Logger logger = Logger.getLogger("org.constellation.metadata");
    
    /**
     * The Reader of this lucene index.
     */
    private final Reader reader;
    
    /**
     * A lucene analyser.
     */
    private final Analyzer analyzer;
    
    /**
     * A default Query requesting all the document
     */
    private final Query simpleQuery = new TermQuery(new Term("metafile", "doc"));
    
    /**
     * A Map containg all the paths (used when reader is null)
     */
    private final Map<String, Path> pathMap;
    
    /**
     * A Map containing some classes (used when reader is null)
     */
    private final  Map<String, Classe> classeMap;
    
    /**
     * Creates a new Lucene Index with the specified MDweb reader.
     * 
     * @param reader An mdweb reader for read the metadata database.
     * @param configDirectory A directory where the index can write indexation file. 
     */
    public IndexLucene(Reader reader, File configDirectory) throws SQLException {
        
        this.reader = reader;
        analyzer    = new StandardAnalyzer();
        pathMap     = null;
        classeMap   = null;
        
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
                
                // getting the objects list and index avery item in the IndexWriter.
                List<Catalog> cats = reader.getCatalogs();
                nbCatalogs = cats.size();
                List<Form> results = reader.getAllForm(cats);
                logger.info("end read all form");
                nbForms    =  results.size();
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
            }
            logger.info("Index creation process in " + (System.currentTimeMillis() - time) + " ms" + '\n' + 
                        "catalogs: " + nbCatalogs + " documents indexed: " + nbForms);
        } else {
            logger.info("Index already created");
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
    protected IndexLucene(List<Form> forms, List<Classe> classes, List<Path> paths, File configDirectory) throws SQLException {
        
        analyzer     = new StandardAnalyzer();
        reader       = null;
        
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
     * This method add to index of lucene a new document based on Form object.
     * (implements AbstractIndex.indexDocument() )
     * object must be a Form.
     * 
     * @param writer A lucene Index Writer.
     * @param object A MDweb formular.
     */
    public void indexDocument(IndexWriter writer, Object object) {
        Form r;
        if (object instanceof Form) {
            r = (Form) object;
        } else {
            throw new IllegalArgumentException("Unexpected type, supported one is: org.mdweb.model.storage.Form");
        }
        try {
            //adding the document in a specific model. in this case we use a MDwebDocument.
            writer.addDocument(createDocument(r));
            logger.finer("Form: " + r.getTitle() + " indexed");
            
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
    public void indexDocument(Object object) {
        Form r;
        if (object instanceof Form) {
            r = (Form) object;
        } else {
            throw new IllegalArgumentException("Unexpected type, supported one is: org.mdweb.model.storage.Form");
        }
        try {
            IndexWriter writer = new IndexWriter(getFileDirectory(), analyzer, true);
            
            //adding the document in a specific model. in this case we use a MDwebDocument.
            writer.addDocument(createDocument(r));
            logger.info("Form: " + r.getTitle() + " indexed");
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
     * Return a string description for the specified terms
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
        
        if (paths != null) {
            for (String pathID: paths) {
                Path path;
                if (reader != null) {
                    path   = reader.getPath(pathID);
                } else {
                    path = pathMap.get(pathID);
                }
                List<Value> values = form.getValueFromPath(path);
                for (Value v: values) {
                    if ( (ordinal == -1 && v instanceof TextValue) || (v instanceof TextValue && v.getOrdinal() == ordinal)) {
                
                        TextValue tv = (TextValue) v;
                        response.append(tv.getValue()).append(','); 
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
    
   /**
    * Makes a document for a MDWeb formular.
    * 
    * @param Form An MDweb formular to index.
    * @return A Lucene document.
    */
    private Document createDocument(Form form) throws SQLException {
        
        // make a new, empty document
        Document doc = new Document();
        
        doc.add(new Field("id",      form.getId() + "",            Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("catalog", form.getCatalog().getCode() , Field.Store.YES, Field.Index.TOKENIZED));        
        doc.add(new Field("Title",   form.getTitle(),              Field.Store.YES, Field.Index.TOKENIZED));
        
        Classe identifiable, registryObject;
        if (reader == null) {
            identifiable   = classeMap.get("Identifiable");
            registryObject = classeMap.get("RegistryObject");
        } else {
            identifiable   = reader.getClasse("Identifiable", Standard.EBRIM_V3);
            registryObject = reader.getClasse("RegistryObject", Standard.EBRIM_V2_5);
        }
        
        // For an ISO 19115 form
        if (form.getTopValue().getType().getName().equals("MD_Metadata")) {
            
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
                logger.info("put " + term + " values: " + values);
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
     * This method proceed a lucene search and returns a list of ID.
     *
     * @param query The lucene query string with spatials filters.
     * 
     * @return      A List of id.
     */
    public List<String> doSearch(SpatialQuery spatialQuery) throws CorruptIndexException, IOException, ParseException {
        
        List<String> results = new ArrayList<String>();
        File indexDirectory = getFileDirectory();
        logger.info("index directory:" + indexDirectory.getPath());
        IndexReader ireader = IndexReader.open(indexDirectory);
        Searcher searcher   = new IndexSearcher(ireader);
        String field        = "Title";
        QueryParser parser  = new QueryParser(field, analyzer);
        if (spatialQuery.getQuery().indexOf(":*") != -1 || spatialQuery.getQuery().indexOf(":?") != -1 )
            parser.setAllowLeadingWildcard(true);
        
        Query query   = parser.parse(spatialQuery.getQuery());
        Filter f      = spatialQuery.getSpatialFilter();
        int operator  = spatialQuery.getLogicalOperator();
        Sort sort     = spatialQuery.getSort();
        String sorted = "";
        if (sort != null)
            sorted = "order by: " + sort.toString();
        
        logger.info("Searching for: "    + query.toString(field) + '\n' +
                    SerialChainFilter.ValueOf(operator)          + '\n' +
                    f                                            + '\n' +
                    sorted                                       + '\n');
        
        // simple query with an AND
        if (operator == SerialChainFilter.AND || (operator == SerialChainFilter.OR && f == null)) {
            Hits hits = searcher.search(query, f, sort);
        
            for (int i = 0; i < hits.length(); i ++) {
            
                results.add( hits.doc(i).get("id") + ':' + hits.doc(i).get("catalog"));
            }
        
        // for a OR we need to perform many request 
        } else if (operator == SerialChainFilter.OR) {
            Hits hits1 = searcher.search(query, sort);
            Hits hits2 = searcher.search(simpleQuery, spatialQuery.getSpatialFilter(), sort);
            
            for (int i = 0; i < hits1.length(); i++) {
                results.add(hits1.doc(i).get("id") + ':' + hits1.doc(i).get("catalog"));
            }
            
            for (int i = 0; i < hits2.length(); i++) {
                String id = hits2.doc(i).get("id") + ':' + hits2.doc(i).get("catalog");
                if (!results.contains(id)) {
                    results.add(id);
                }
            }
            
        // for a NOT we need to perform many request 
        } else if (operator == SerialChainFilter.NOT) {
            Hits hits1 = searcher.search(query, f, sort);
            
            List<String> unWanteds = new ArrayList<String>();
            for (int i = 0; i < hits1.length(); i++) {
                unWanteds.add(hits1.doc(i).get("id") + ':' + hits1.doc(i).get("catalog"));
            }
            
            Hits hits2 = searcher.search(simpleQuery, sort);
            for (int i = 0; i < hits2.length(); i++) {
                String id = hits2.doc(i).get("id") + ':' + hits2.doc(i).get("catalog");
                if (!unWanteds.contains(id)) {
                    results.add(id);
                }
            }
            
        } else {
            throw new IllegalArgumentException("unsupported logical Operator");
        }
        
        // if we have some subQueries we execute it separely and merge the result
        if (spatialQuery.getSubQueries().size() > 0) {
            SpatialQuery sub = spatialQuery.getSubQueries().get(0);
            List<String> subResults =  doSearch(sub);
            for (String r: results) {
                if (!subResults.contains(r)) {
                    results.remove(r);
                } 
            }
        }
        
        logger.info(results.size() + " total matching documents");
        
        ireader.close();
        searcher.close();
        return results;
    } 
    
    /**
     * This method proceed a lucene search and returns a list of ID.
     *
     * @param query A simple Term query.
     * 
     * @return      A List of id.
     */
    public List<String> doSearch(TermQuery query) throws CorruptIndexException, IOException, ParseException {
        
        List<String> results = new ArrayList<String>();
        
        IndexReader ireader = IndexReader.open(getFileDirectory());
        Searcher searcher   = new IndexSearcher(ireader);
        logger.info("TermQuery:" + query.toString());
        Hits hits = searcher.search(query);
        
        for (int i = 0; i < hits.length(); i ++) {
            results.add( hits.doc(i).get("id") + ':' + hits.doc(i).get("catalog"));
        }
        ireader.close();
        searcher.close();
        logger.info(results.size() + " total matching documents");
        
        return results;
    }
    
    /**
     * Add a boundingBox geometry to the specified Document.
     * 
     * @param doc  The document to add the geometry
     * @param minx the minimun X coordinate of the bounding box.
     * @param maxx the maximum X coordinate of the bounding box.
     * @param miny the minimun Y coordinate of the bounding box.
     * @param maxy the maximum Y coordinate of the bounding box.
     * @param crsName The coordinate reference system in witch the coordinates are expressed.
     */
    private void addBoundingBox(Document doc, double minx, double maxx, double miny, double maxy, String crsName) {

        // convert the corner of the box to lucene fields
        doc.add(new Field("geometry" , "boundingbox", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("minx"     , minx + "",     Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("maxx"     , maxx + "",     Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("miny"     , miny + "",     Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("maxy"     , maxy + "",     Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("CRS"      , crsName  ,     Field.Store.YES, Field.Index.UN_TOKENIZED));
        logger.finer("added boundingBox: minx=" + minx + " miny=" + miny + " maxx=" + maxx +  " maxy=" + maxy);
    }
    
    /**
     *  Add a point geometry to the specified Document.
     * 
     * @param doc     The document to add the geometry
     * @param x       The x coordinate of the point.
     * @param y       The y coordinate of the point.
     * @param crsName The coordinate reference system in witch the coordinates are expressed.
     */
    private void addPoint(Document doc, double y, double x, String crsName) {

        // convert the lat / long to lucene fields
        doc.add(new Field("geometry" , "point", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("x"        , x + "" , Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("y"        , y + "" , Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("CRS"      , crsName, Field.Store.YES, Field.Index.UN_TOKENIZED));
       
    }
    
    /**
     * Add a Line geometry to the specified Document.
     * 
     * @param doc The document to add the geometry
     * @param x1  the X coordinate of the first point of the line.
     * @param y1  the Y coordinate of the first point of the line.
     * @param x2  the X coordinate of the second point of the line.
     * @param y2  the Y coordinate of the first point of the line.
     * @param crsName The coordinate reference system in witch the coordinates are expressed.
     */
    private void addLine(Document doc, double x1, double y1, double x2, double y2, String crsName) {

        
        // convert the corner of the box to lucene fields
        doc.add(new Field("geometry" , "line" , Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("x1"       , x1 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("y1"       , y1 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("x2"       , x2 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("y2"       , y2 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("CRS"      , crsName, Field.Store.YES, Field.Index.UN_TOKENIZED));
    }
   
}
