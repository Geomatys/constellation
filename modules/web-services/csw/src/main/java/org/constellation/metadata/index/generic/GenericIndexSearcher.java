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

package org.constellation.metadata.index.generic;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Apache Lucene dependencies
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

// constellation dependencies
import org.constellation.lucene.index.AbstractIndexSearcher;
import org.constellation.ws.CstlServiceException;

/**
 * A Lucene searcher for a Generic index.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class GenericIndexSearcher extends AbstractIndexSearcher {

    public GenericIndexSearcher(File configDir, String serviceID) throws CstlServiceException  {
        super(configDir, serviceID);
    }
    
    /**
     * In generic index we don't need to perform a lucene query for the identifier.
     *
     * @param id
     * @return
     * @throws org.apache.lucene.index.CorruptIndexException
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryParser.ParseException
     */
    @Override
    public String identifierQuery(String id) throws CorruptIndexException, IOException, ParseException {
        TermQuery query      = new TermQuery(new Term("id", id));
        List<String> results = new ArrayList<String>();
        int maxRecords       = searcher.maxDoc();

        logger.info("TermQuery: " + query.toString());
        TopDocs hits = searcher.search(query, maxRecords);

        for (ScoreDoc doc :hits.scoreDocs) {
            results.add(searcher.doc(doc.doc, new IDFieldSelector()).get("id"));
        }
        if (results.size() > 1)
            logger.warning("multiple record in lucene index for identifier: " + id);

        if (results.size() > 0)
            return results.get(0);
        else
            return null;
    }

    @Override
    public String getMatchingID(Document doc) throws CorruptIndexException, IOException, ParseException {
        return doc.get("id");
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    private class IDFieldSelector implements FieldSelector {

        public FieldSelectorResult accept(String fieldName) {
            if (fieldName != null) {
                if (fieldName.equals("id")) {
                    return FieldSelectorResult.LOAD_AND_BREAK;
                } else {
                    return FieldSelectorResult.NO_LOAD;
                }
            }
            return FieldSelectorResult.NO_LOAD;
        }
    }
}
