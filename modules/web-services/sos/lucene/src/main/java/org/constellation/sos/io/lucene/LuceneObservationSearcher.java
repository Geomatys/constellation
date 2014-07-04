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

package org.constellation.sos.io.lucene;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.SearchingException;
import org.geotoolkit.lucene.filter.SerialChainFilter;
import org.geotoolkit.lucene.filter.SpatialQuery;
import org.geotoolkit.lucene.index.LuceneIndexSearcher;
import org.geotoolkit.observation.ObservationResult;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.constellation.sos.ws.SOSUtils.unLuceneTimeValue;

// Apache Lucene dependencies
// constellation dependencies
// Geotoolkit dependencies

/**
 *  A Lucene searcher for an index connected to an O&M DataSource.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class LuceneObservationSearcher extends LuceneIndexSearcher {

    /**
     * Build a new index searcher with the index located in the specified directory.
     * The index directory path must be :
     * <configDir path>/<serviceID>index-<some timestamp number>
     *
     * @param configDir A directory containing the lucene index directory.
     * @param serviceID The identifier of the index/service
     * @throws IndexingException
     */
    public LuceneObservationSearcher(final File configDir, final String serviceID) throws IndexingException  {
        super(configDir, serviceID, new WhitespaceAnalyzer(Version.LUCENE_46), false);
    }

    /**
     * This method proceed a lucene search and returns a list of ObservationResult.
     *
     * @param spatialQuery The lucene query string with spatial filters.
     *
     * @return A List of Observation result..
     */
    public List<ObservationResult> doResultSearch(SpatialQuery spatialQuery) throws SearchingException {
        final Query simpleQuery = new TermQuery(new Term("metafile", "doc"));
        try {
            final long start = System.currentTimeMillis();
            final List<ObservationResult> results = new ArrayList<>();

            int maxRecords = (int) searcher.collectionStatistics("id").maxDoc();
            if (maxRecords == 0) {
                LOGGER.severe("The index seems to be empty.");
                maxRecords = 1;
            }

            final String field       = "Title";
            final QueryParser parser = new QueryParser(Version.LUCENE_46, field, analyzer);
            parser.setDefaultOperator(Operator.AND);

            // we enable the leading wildcard mode if the first character of the query is a '*'
            if (spatialQuery.getQuery().indexOf(":*") != -1 || spatialQuery.getQuery().indexOf(":?") != -1 || spatialQuery.getQuery().indexOf(":(*") != -1
             || spatialQuery.getQuery().indexOf(":(+*") != -1 || spatialQuery.getQuery().indexOf(":+*") != -1) {
                parser.setAllowLeadingWildcard(true);
                BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
            }
            LOGGER.log(Level.FINER, "before parse:{0}", spatialQuery.getQuery());
            final Query query   = parser.parse(spatialQuery.getQuery());
            final Filter filter = spatialQuery.getSpatialFilter();
            final int operator  = spatialQuery.getLogicalOperator();
            final Sort sort     = spatialQuery.getSort();
            String sorted       = "no Sorted";
            if (sort != null) {
                sorted = "order by: " + sort.toString();
            }
            String f = "no Filter";
            if (filter != null) {
                f = filter.toString();
            }
            LOGGER.log(logLevel, "Searching for result: " + query.toString(field) + '\n' + SerialChainFilter.valueOf(operator) + '\n' + f + '\n' + sorted + '\n' + "max records: " + maxRecords);

            // simple query with an AND
            if (operator == SerialChainFilter.AND || (operator == SerialChainFilter.OR && filter == null)) {
                final TopDocs docs;
                if (sort != null) {
                    docs = searcher.search(query, filter, maxRecords, sort);
                } else {
                    docs = searcher.search(query, filter, maxRecords);
                }
                for (ScoreDoc doc : docs.scoreDocs) {
                    final ObservationResult or = getObservationResult(searcher.doc(doc.doc));
                    results.add(or);
                }

            // for a OR we need to perform many request
            } else if (operator == SerialChainFilter.OR) {
                final TopDocs hits1;
                final TopDocs hits2;
                if (sort != null) {
                    hits1 = searcher.search(query, null, maxRecords, sort);
                    hits2 = searcher.search(simpleQuery, spatialQuery.getSpatialFilter(), maxRecords, sort);
                } else {
                    hits1 = searcher.search(query, maxRecords);
                    hits2 = searcher.search(simpleQuery, spatialQuery.getSpatialFilter(), maxRecords);
                }
                for (ScoreDoc doc : hits1.scoreDocs) {
                    final ObservationResult or = getObservationResult(searcher.doc(doc.doc));
                    results.add(or);
                }
                for (ScoreDoc doc : hits2.scoreDocs) {
                    final ObservationResult or = getObservationResult(searcher.doc(doc.doc));
                    if (!results.contains(or)) {
                        results.add(or);
                    }
                }

            // for a NOT we need to perform many request
            } else if (operator == SerialChainFilter.NOT) {
                final TopDocs hits1;
                if (sort != null) {
                    hits1 = searcher.search(query, filter, maxRecords, sort);
                } else {
                    hits1 = searcher.search(query, filter, maxRecords);
                }
                final List<ObservationResult> unWanteds = new ArrayList<>();
                for (ScoreDoc doc : hits1.scoreDocs) {
                    final ObservationResult or = getObservationResult(searcher.doc(doc.doc));
                    unWanteds.add(or);
                }

                final TopDocs hits2;
                if (sort != null) {
                    hits2 = searcher.search(simpleQuery, null, maxRecords, sort);
                } else {
                    hits2 = searcher.search(simpleQuery, maxRecords);
                }
                for (ScoreDoc doc : hits2.scoreDocs) {
                    final ObservationResult or = getObservationResult(searcher.doc(doc.doc));
                    if (!unWanteds.contains(or)) {
                        results.add(or);
                    }
                }

            } else {
                throw new IllegalArgumentException("unsupported logical Operator");
            }

            // if we have some subQueries we execute it separely and merge the result
            if (spatialQuery.getSubQueries().size() > 0) {
                final SpatialQuery sub        = spatialQuery.getSubQueries().get(0);
                final List<ObservationResult> subResults = doResultSearch(sub);
                for (ObservationResult r : results) {
                    if (!subResults.contains(r)) {
                        results.remove(r);
                    }
                }
            }

            LOGGER.log(logLevel, results.size() + " total matching documents (" + (System.currentTimeMillis() - start) + "ms)");
            return results;
        } catch (ParseException ex) {
            throw new SearchingException("Parse Exception while performing lucene request", ex);
        } catch (IOException ex) {
           throw new SearchingException("IO Exception while performing lucene request", ex);
        }
    }

    /**
     * Return an observationResult from a Lucene document.
     *
     * @param d A lucene document.
     *
     * @return an observationResult containing the id and the time period of the observation.
     */
    private ObservationResult getObservationResult(final Document d) {
        Timestamp begin = null;
        Timestamp end   = null;
        try {
            final String timeBegin = d.get("sampling_time_begin");
            if (timeBegin != null) {
                begin = Timestamp.valueOf(unLuceneTimeValue(timeBegin));
            }
        } catch (IllegalArgumentException ex) {
            LOGGER.log(logLevel, "unable  to parse the timestamp");
        }
        try {
            final String timeEnd = d.get("sampling_time_end");
            if (timeEnd != null) {
                end = Timestamp.valueOf(unLuceneTimeValue(timeEnd));
            }
        } catch (IllegalArgumentException ex) {
            LOGGER.log(logLevel, "unable  to parse the timestamp");
        }
        return new ObservationResult(d.get("id"), begin, end);
    }
}
