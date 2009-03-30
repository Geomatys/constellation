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

package org.constellation.lucene.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Apache Lucene dependencies
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

// constellation dependencies
import org.constellation.lucene.IndexingException;
import org.constellation.lucene.SearchingException;
import org.constellation.lucene.filter.SerialChainFilter;
import org.constellation.lucene.filter.SpatialQuery;

/**
 *
 * @author Guilhem legal (Geomatys)
 */
public abstract class AbstractIndexSearcher extends IndexLucene {

    /**
     * This is the index searcher of Lucene.
     */
    protected IndexSearcher searcher;

    /**
     * A default Query requesting all the document
     */
    private final Query simpleQuery = new TermQuery(new Term("metafile", "doc"));

    /**
     * A map of cached request
     */
    private final Map<SpatialQuery, List<String>> cachedQueries = new HashMap<SpatialQuery, List<String>>();

    /**
     * The maximum size of the map of queries.
     */
    private final static int MaxCachedQueriesSize = 50;

    /**
     * A flag indicating if the cache system for query is enabled.
     */
    private final boolean isCacheEnabled;

    /**
     * A list of metadata ID ordered by DocID.
     */
    private  List<String> identifiers;
    
    /**
     * Build a new index searcher.
     *
     * @param configDir The configuration Directory where to build the indexDirectory.
     * @param serviceID the "ID" of the service (allow multiple index in the same directory). The value "" is allowed.
     * @param analizer  A lucene Analyzer
     */
    public AbstractIndexSearcher(File configDir, String serviceID, Analyzer analyzer) throws IndexingException {
        super(analyzer);
        try {
            setFileDirectory(new File(configDir, serviceID + "index"));
            isCacheEnabled = true;
            initSearcher();
            initIdentifiersList();

        } catch (CorruptIndexException ex) {
            throw new IndexingException("Corruption encountered during indexing", ex);
        } catch (ParseException ex) {
            throw new IndexingException("Failure to parse during indexing", ex);
        } catch (IOException ex) {
            throw new IndexingException("IO Exception during indexing", ex);
        }  catch (SearchingException ex) {
            throw new IndexingException("Searching Exception during indexing", ex);
        }
        
    }

    public AbstractIndexSearcher(File configDir, String serviceID) throws IndexingException {
        this(configDir, serviceID, null);
    }

    /**
     * Returns the IndexSearcher of this index.
     */
    private void initSearcher() throws CorruptIndexException, IOException {
        File indexDirectory = getFileDirectory();
        IndexReader ireader = IndexReader.open(indexDirectory);
        searcher   = new IndexSearcher(ireader);
        logger.info("Creating new Index Searcher with index directory:" + indexDirectory.getPath());
       
    }

    /**
     * Fill the list of identifiers ordered by doc ID
     */
    private final void initIdentifiersList() throws IOException, CorruptIndexException, ParseException, SearchingException {
        identifiers = new ArrayList<String>();
        for (int i = 0; i < searcher.maxDoc(); i++) {
            String metadataID = getMatchingID(searcher.doc(i));
            identifiers.add(i, metadataID);
        }
        logger.info(identifiers.size() + " records founded.");
    }

    /**
     * Refresh the searcher (must be call after deleting document from the index for example)
     *
     * @throws org.constellation.lucene.IndexingException
     */
    public void refresh() throws IndexingException {
        try {
            initSearcher();
            logger.info("refreshing index searcher");
        } catch (CorruptIndexException ex) {
            throw new IndexingException("Corruption encountered during indexing", ex);
        } catch (IOException ex) {
            throw new IndexingException("IO Exception during indexing", ex);
        }
    }

    /**
     * This method proceed a lucene search and to verify that the identifier exist.
     * If it exist it return the database ID.
     *
     * @param query A simple Term query on "indentifier field".
     *
     * @return A database id.
     */
    public abstract String identifierQuery(String id) throws SearchingException;

    /**
     * This method return the database ID of a matching Document
     *
     * @param doc A matching document.
     *
     * @return A database id.
     */
    public abstract String getMatchingID(Document doc) throws SearchingException;

    /**
     * This method proceed a lucene search and returns a list of ID.
     *
     * @param query The lucene query string with spatials filters.
     *
     * @return      A List of id.
     */
    public List<String> doSearch(SpatialQuery spatialQuery) throws SearchingException {
        try {
            long start = System.currentTimeMillis();
            List<String> results = new ArrayList<String>();

            //we look for a cached Query
            if (isCacheEnabled && cachedQueries.containsKey(spatialQuery)) {
                results = cachedQueries.get(spatialQuery);
                logger.info("returning result from cache (" + results.size() + " matching documents)");
                return results;
            }

            int maxRecords = searcher.maxDoc();
            if (maxRecords == 0) {
                logger.severe("The index seems to be empty.");
                maxRecords = 1;
            }

            String field = "Title";
            QueryParser parser = new QueryParser(field, analyzer);
            parser.setDefaultOperator(Operator.AND);

            // we enable the leading wildcard mode if the first character of the query is a '*'
            if (spatialQuery.getQuery().indexOf(":*") != -1 || spatialQuery.getQuery().indexOf(":?") != -1 || spatialQuery.getQuery().indexOf(":(*") != -1
             || spatialQuery.getQuery().indexOf(":(+*") != -1 || spatialQuery.getQuery().indexOf(":+*") != -1) {
                parser.setAllowLeadingWildcard(true);
                BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
            }
            Query query = parser.parse(spatialQuery.getQuery());
            Filter filter = spatialQuery.getSpatialFilter();
            int operator = spatialQuery.getLogicalOperator();
            Sort sort = spatialQuery.getSort();
            String sorted = "no Sorted";
            if (sort != null) {
                sorted = "order by: " + sort.toString();
            }
            String f = "no Filter";
            if (filter != null) {
                f = filter.toString();
            }
            logger.info("Searching for: " + query.toString(field) + '\n' + SerialChainFilter.ValueOf(operator) + '\n' + f + '\n' + sorted + '\n' + "max records: " + maxRecords);

            // simple query with an AND
            if (operator == SerialChainFilter.AND || (operator == SerialChainFilter.OR && filter == null)) {
                TopDocs docs;
                if (sort != null) {
                    docs = searcher.search(query, filter, maxRecords, sort);
                } else {
                    docs = searcher.search(query, filter, maxRecords);
                }
                for (ScoreDoc doc : docs.scoreDocs) {
                    results.add(identifiers.get(doc.doc));
                }

            // for a OR we need to perform many request
            } else if (operator == SerialChainFilter.OR) {
                TopDocs hits1;
                TopDocs hits2;
                if (sort != null) {
                    hits1 = searcher.search(query, null, maxRecords, sort);
                    hits2 = searcher.search(simpleQuery, spatialQuery.getSpatialFilter(), maxRecords, sort);
                } else {
                    hits1 = searcher.search(query, maxRecords);
                    hits2 = searcher.search(simpleQuery, spatialQuery.getSpatialFilter(), maxRecords);
                }
                for (ScoreDoc doc : hits1.scoreDocs) {
                    results.add(identifiers.get(doc.doc));
                }
                for (ScoreDoc doc : hits2.scoreDocs) {
                    String id = identifiers.get(doc.doc);
                    if (!results.contains(id)) {
                        results.add(id);
                    }
                }

            // for a NOT we need to perform many request
            } else if (operator == SerialChainFilter.NOT) {
                TopDocs hits1;
                if (sort != null) {
                    hits1 = searcher.search(query, filter, maxRecords, sort);
                } else {
                    hits1 = searcher.search(query, filter, maxRecords);
                }
                List<String> unWanteds = new ArrayList<String>();
                for (ScoreDoc doc : hits1.scoreDocs) {
                    unWanteds.add(identifiers.get(doc.doc));
                }

                TopDocs hits2;
                if (sort != null) {
                    hits2 = searcher.search(simpleQuery, null, maxRecords, sort);
                } else {
                    hits2 = searcher.search(simpleQuery, maxRecords);
                }
                for (ScoreDoc doc : hits2.scoreDocs) {
                    String id = identifiers.get(doc.doc);
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
                List<String> subResults = doSearch(sub);
                for (String r : results) {
                    if (!subResults.contains(r)) {
                        results.remove(r);
                    }
                }
            }

            //we put the query in cache
            putInCache(spatialQuery, results);
            
            logger.info(results.size() + " total matching documents (" + (System.currentTimeMillis() - start) + "ms)");
            return results;
        } catch (ParseException ex) {
            throw new SearchingException("Parse Exception while performing lucene request", ex);
        } catch (IOException ex) {
           throw new SearchingException("IO Exception while performing lucene request", ex);
        }
    }

    /**
     * Add a query and its results to the cache.
     * if the map has reach the maximum size the older query is rtemoved from the cache.
     * @param query
     * @param results
     */
    private void putInCache(SpatialQuery query, List<String> results) {
        if (isCacheEnabled) {
            // if we had reach the maximum cache size we remove the first request
            if (cachedQueries.size() >= MaxCachedQueriesSize) {
                cachedQueries.remove(cachedQueries.keySet().iterator().next());
            }
            cachedQueries.put(query, results);
        }
    }

    public void destroy() {
        try {
            if (searcher != null)
                searcher.close();
        } catch (IOException ex) {
            logger.info("IOException while closing the indexer");
        }
    }

}
