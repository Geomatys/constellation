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

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;

/**
 *
 * @author Mehdi Sidhoum
 * @author Guilhem Legal
 */
public abstract class AbstractIndexer<E> {

    protected static final Logger logger = Logger.getLogger("org.constellation.metadata");

    /**
     * This is the RAM Directory if you would like to store the index in the RAM memory.
     */
    private RAMDirectory RAMdirectory = new RAMDirectory();

    /**
     * This the File Directory if you would like to store the index in a File directory.
     */
    private File FileDirectory = new File("index");

    /**
     * This is the index searcher of Lucene.
     */
    private IndexSearcher searcher;


    /**
     * Creates a new instance of AbstractIndex
     */
    public AbstractIndexer() {}

    /**
     * This method add documents into the index, the model's object is specified in the childrens classes.
     */
    public abstract void indexDocument(IndexWriter writer, E object);

    /**
     * Returns a RAMdirectory of this Index Object.
     */
    public RAMDirectory getRAMdirectory() {
        return RAMdirectory;
    }

    /**
     * The RAMdirectory setter for this Index object.
     *
     * @param RAMDirectory a RAMDirectory object.
     */
    public void setRAMdirectory(RAMDirectory RAMdirectory) {
        this.RAMdirectory = RAMdirectory;
    }

    /**
     * Returns a file directory of this index.
     */
    public File getFileDirectory() {
        return FileDirectory;
    }

    /**
     * The FileDirectory setter of this index.
     *
     * @param aFileDirectory a FileDirectory object.
     */
    public void setFileDirectory(File aFileDirectory) {
        FileDirectory = aFileDirectory;
    }

    /**
     * Returns the IndexSearcher of this index.
     */
    public IndexSearcher getSearcher() throws CorruptIndexException, IOException {
       if (searcher == null) {
            File indexDirectory = getFileDirectory();
            logger.info("Creating new Index Searcher with index directory:" + indexDirectory.getPath());
            IndexReader ireader = IndexReader.open(indexDirectory);
            searcher   = new IndexSearcher(ireader);
        }
        return searcher;
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

