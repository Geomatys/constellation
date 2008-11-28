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
package org.constellation.metadata.index;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// Lucene dependencies
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;

// MDWeb dependencies
import org.mdweb.lucene.AbstractIndex;

// Constellation dependencies
import org.constellation.lucene.filter.SerialChainFilter;
import org.constellation.lucene.filter.SpatialQuery;
import org.constellation.metadata.Utils;

/**
 *
 * @author Guilehm Legal
 */
public abstract class IndexLucene<E> extends AbstractIndex<E> {

    protected final Logger logger = Logger.getLogger("org.constellation.metadata");
    
    /**
     * A lucene analyser.
     */
    protected final Analyzer analyzer;
    
    /**
     * A default Query requesting all the document
     */
    private final Query simpleQuery = new TermQuery(new Term("metafile", "doc"));
    
    /**
     * A flag use in child constructor.
     */
    protected boolean create;
    
   /**
     * Creates a new Lucene Index.
     * 
     * @param configDirectory A directory where the index can write indexation file. 
     */
    public IndexLucene(File configDirectory) {
        
        analyzer      = new WhitespaceAnalyzer();
        
        //we look if an index has been pre-generated. if yes, we delete the precedent index and replace it.
        File preGeneratedIndexDirectory = new File(configDirectory, "nextIndex");
        
        // we get the current index directory
        File currentIndexDirectory = new File(configDirectory, "index");
        setFileDirectory(currentIndexDirectory);
        
        if (preGeneratedIndexDirectory.exists()) {
            switchIndexDir(preGeneratedIndexDirectory, currentIndexDirectory);
            logger.info("using pre-created index.");
            
        } else {
            //if the index File exists we don't need to index the documents again.
            if(!currentIndexDirectory.exists()) {
                create = true;
            } else {
                logger.info("Index already created.");
                create = false;
            }
        }
    }
    
    /**
     * Creates a new Lucene Index.
     */
    public IndexLucene() {
        analyzer      = new WhitespaceAnalyzer();
    }
    
    /** 
     * Create a new Index.
     * 
     * @throws java.sql.SQLException
     */
    public abstract void createIndex() throws SQLException;

    /**
     * Replace the precedent index directory by another pre-generated.
     */
    private void switchIndexDir(File preGeneratedDirectory, File indexDirectory) {
        if (indexDirectory.exists()) {
            Utils.deleteDirectory(indexDirectory);
        }
        preGeneratedDirectory.renameTo(indexDirectory);
    }
    
    /**
     * Index a document from the specified object with the specified index writer.
     * Used when indexing in line many document.
     * 
     * @param writer An Lucene index writer.
     * @param object The object to index.
     */
    public abstract void indexDocument(IndexWriter writer, E object);
    
    /**
     * This method add to index of lucene a new document.
     * (implements AbstractIndex.indexDocument() )
     * 
     * @param object The object to index.
     */
    public abstract void indexDocument(E object);
    
    
    /**
    * Makes a document from the specified object.
    * 
    * @param Form An MDweb formular to index.
    * @return A Lucene document.
    */
    protected abstract Document createDocument(E object) throws SQLException;
    
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
        if (spatialQuery.getQuery().indexOf(":*") != -1 || spatialQuery.getQuery().indexOf(":?") != -1 ) {
            parser.setAllowLeadingWildcard(true);
            BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        }
        
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
    public abstract List<String> identifierQuery(String id) throws CorruptIndexException, IOException, ParseException;
    
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
    protected void addBoundingBox(Document doc, double minx, double maxx, double miny, double maxy, String crsName) {

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
    protected void addPoint(Document doc, double y, double x, String crsName) {

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
    protected void addLine(Document doc, double x1, double y1, double x2, double y2, String crsName) {

        
        // convert the corner of the box to lucene fields
        doc.add(new Field("geometry" , "line" , Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("x1"       , x1 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("y1"       , y1 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("x2"       , x2 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("y2"       , y2 + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("CRS"      , crsName, Field.Store.YES, Field.Index.UN_TOKENIZED));
    }
   
}
