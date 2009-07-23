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

// J2SE dependencies
import java.io.File;

// Lucene dependencies
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
        
// Constellation dependencies
import org.apache.lucene.store.RAMDirectory;

/**
 *
 * @author Guilhem Legal
 */
public abstract class IndexLucene {

    protected static final Logger LOGGER = Logger.getLogger("org.constellation.metadata.index");

    /**
     * A lucene analyser.
     */
    protected final Analyzer analyzer;

    /**
     * This is the RAM Directory if you would like to store the index in the RAM memory.
     */
    private RAMDirectory ramDirectory = new RAMDirectory();

    /**
     * This the File Directory if you would like to store the index in a File directory.
     */
    private File fileDirectory;
    
   /**
     * Creates a new Lucene Index.
     * 
     * @param configDirectory A directory where the index can write indexation file. 
     */
    public IndexLucene() {
        analyzer = new StandardAnalyzer();
    }

    /**
     * Creates a new Lucene Index with the specified Analyzer.
     *
     * @param configDirectory A directory where the index can write indexation file.
     */
    public IndexLucene(final Analyzer analyzer) {
        if (analyzer == null)
            this.analyzer = new StandardAnalyzer();
        else
            this.analyzer = analyzer;
    }

    /**
     * Returns a RAMdirectory of this Index Object.
     */
    public RAMDirectory getRAMdirectory() {
        return ramDirectory;
    }

    /**
     * The RAMdirectory setter for this Index object.
     *
     * @param RAMDirectory a RAMDirectory object.
     */
    public void setRAMdirectory(final RAMDirectory ramDirectory) {
        this.ramDirectory = ramDirectory;
    }

    /**
     * Returns a file directory of this index.
     */
    public File getFileDirectory() {
        return fileDirectory;
    }

    /**
     * The FileDirectory setter of this index.
     *
     * @param aFileDirectory a FileDirectory object.
     */
    public void setFileDirectory(final File aFileDirectory) {
        fileDirectory = aFileDirectory;
    }

}
