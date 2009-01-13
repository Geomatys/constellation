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
import java.sql.SQLException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.constellation.util.Utils;
import org.constellation.ws.WebServiceException;

/**
 * An abstract lucene Indexer used to create and writer lucene index.
 *
 * @author Mehdi Sidhoum
 * @author Guilhem Legal
 */
public abstract class AbstractIndexer<E> extends IndexLucene {

    /**
     * A flag use in child constructor.
     */
    protected boolean create;

    public AbstractIndexer(String serviceID, File configDirectory) {
        super();
        //we look if an index has been pre-generated. if yes, we delete the precedent index and replace it.
        File preGeneratedIndexDirectory = new File(configDirectory, serviceID + "nextIndex");

        // we get the current index directory
        File currentIndexDirectory = new File(configDirectory, serviceID + "index");
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
     * Replace the precedent index directory by another pre-generated.
     */
    private void switchIndexDir(File preGeneratedDirectory, File indexDirectory) {
        if (indexDirectory.exists()) {
            Utils.deleteDirectory(indexDirectory);
        }
        preGeneratedDirectory.renameTo(indexDirectory);
    }
    
    /**
     * Create a new Index.
     *
     * @throws java.sql.SQLException
     */
    public abstract void createIndex() throws WebServiceException;

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
        doc.add(new Field("geometry" , "boundingbox", Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("fullBBOX", minx + "," + maxx + "," + miny + "," + maxy + "," + crsName, Field.Store.YES, Field.Index.NOT_ANALYZED));
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
        doc.add(new Field("geometry" , "point", Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("fullPoint", x + "," + y + "," + crsName, Field.Store.YES, Field.Index.NOT_ANALYZED));


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
        doc.add(new Field("geometry" , "line" , Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("fullLine", x1 + "," + y1 + "," + x2 + "," + y2 + "," + crsName , Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public abstract void destroy();
}

