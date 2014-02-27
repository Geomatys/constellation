/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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

package org.constellation.metadata.io.filesystem.sql;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.util.collection.CloseableIterator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class RecordIterator implements CloseableIterator<Node> {

    private static final Logger LOGGER = Logging.getLogger(IdentifierIterator.class);

    private final DocumentBuilderFactory dbf;

    private final Session session;

    private final ResultSet rs;

    private Node current = null;

    public RecordIterator(final Session session) {
        this.session = session;
        try {
            session.setReadOnly(true);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "error while setting cconenction to read only", ex);
        }
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        this.rs = session.getPathIterator();
    }

    @Override
    public boolean hasNext() {
        findNext();
        return current != null;
    }

    @Override
    public Node next() {
       findNext();
       final Node entry = current;
       current = null;
       return entry;
    }

    private void findNext(){
        if (current!=null) return;
        try {
            if (rs.next()) {
                final String path = rs.getString(1);
                final File metadataFile = new File(path);
                current = getNodeFromFile(metadataFile);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error while iterating resultSet", e);
        }
    }

    private Node getNodeFromFile(final File metadataFile) throws SQLException {
        try {
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.parse(metadataFile);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new SQLException("The metadata file : " + metadataFile.getName() + ".xml can not be read", null, ex);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported in this iterator.");
    }

    @Override
    public void close() {
        try {
            rs.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "error while closing resultSet", ex);
        }
        session.close();
    }
}
