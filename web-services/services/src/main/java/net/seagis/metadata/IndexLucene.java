/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.metadata;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// Lucene dependencies
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.LockObtainFailedException;

// MDWeb dependencies
import org.mdweb.lucene.AbstractIndex;
import org.mdweb.model.schemas.Path;
import org.mdweb.model.storage.Catalog;
import org.mdweb.model.storage.Form;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.sql.Reader;

// SeaGIS dependencies
import static net.seagis.metadata.CSWworker.*;

/**
 *
 * @author legal
 */
public class IndexLucene extends AbstractIndex {

    private final Logger logger = Logger.getLogger("net.seagis.coverage");
    
    /**
     * The Reader of this lucene index.
     */
    private final Reader reader;
    
    /**
     * A lucene analyser.
     */
    private final Analyzer analyzer;
    
    /** 
     * Creates a new Lucene Index.
     * 
     * @param reader An mdweb reader for read the metadata database.
     * @param configDirectory A directory where the index can write indexation file. 
     */
    public IndexLucene(Reader reader, File configDirectory) throws SQLException {
        
        this.reader = reader;
        analyzer    = new StandardAnalyzer();
        
        // we get the configuration file
        File f = new File(configDirectory, "index");
        
        setFileDirectory(f);
        
        //if the index File exists we don't need to index the documents again.
        if(!getFileDirectory().exists()){
            logger.info("Creating lucene index for the first time...");
            long time = System.currentTimeMillis();
            IndexWriter writer;

            try {
                writer = new IndexWriter(getFileDirectory(), analyzer, true);
                
                // getting the objects list and index avery item in the IndexWriter.
                List<Catalog> cats = new ArrayList<Catalog>();
                cats.add(reader.getCatalog("FR_SY"));
                List<Form> results = reader.getAllForm(cats);
                for (Form form : results)
                    indexDocument(writer, form);
                
                writer.optimize();
                writer.close();
                
            } catch (CorruptIndexException ex) {
                ex.printStackTrace();
            } catch (LockObtainFailedException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            logger.info("Index creation process in " + (System.currentTimeMillis() - time) + " ms");
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
        } catch (SQLException ex) {
            logger.severe("SQLException " + ex.getMessage());
            ex.printStackTrace();
        } catch (CorruptIndexException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Return a string description for the specified terms
     * 
     * @param term An ISO queryable term defined in CSWWorker (like Title, Subject, Abstract,...)
     * @param form An MDWeb formular from whitch we extract the values correspounding to the specified term.
     * 
     * @return A string concataining the differents values correspounding to the specified term, coma separated.
     */
    private String getValues(String term, Form form) throws SQLException {
        StringBuilder response  = new StringBuilder("");
        for (String pathID: ISO_QUERYABLE.get(term)) {
            Path path   = reader.getPath(pathID);
            List<Value> values = form.getValueFromPath(path);
            for (Value v: values) {
                if (v instanceof TextValue) {
                    TextValue tv = (TextValue) v;
                    response.append(tv.getValue()).append(','); 
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
        
        doc.add(new Field("id",    form.getId() + "", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("Title", form.getTitle(),   Field.Store.YES, Field.Index.TOKENIZED));
        
        //TODO add ANyText
        for (String term :ISO_QUERYABLE.keySet()) {
            doc.add(new Field(term, getValues(term,  form),   Field.Store.YES, Field.Index.TOKENIZED));
        }
        
        // add a default meta field to make searching all documents easy 
	doc.add(new Field("metafile", "doc",Field.Store.YES, Field.Index.TOKENIZED));
        
        return doc;
    }
    
    /**
     * This method proceed a lucene search and returns a list of ID.
     *
     * @param queryString the lucene query string.
     * @param filter a lucene filter (here its essentialy use for spatial filter)
     * @return a List of id.
     */
    public List<String> doSearch(String queryString, Filter filter) throws CorruptIndexException, IOException, ParseException {
        
        List<String> results = new ArrayList<String>();
        
        IndexReader ireader = IndexReader.open(getFileDirectory());
        Searcher searcher   = new IndexSearcher(ireader);
        String field        = "Title";
        QueryParser parser  = new QueryParser(field, analyzer);
        
        if (queryString == null)
            queryString = " ";
        
        Query query = parser.parse(queryString);
        logger.info("Searching for: " + query.toString(field));
        
        Hits hits = searcher.search(query, filter);
        
        logger.info(hits.length() + " total matching documents");
        
        for (int i = 0; i < hits.length(); i ++) {
            
            Document doc = hits.doc(i);
            results.add(doc.get("Identifier"));
        }
        ireader.close();
        
        return results;
    }  
    
   
}
