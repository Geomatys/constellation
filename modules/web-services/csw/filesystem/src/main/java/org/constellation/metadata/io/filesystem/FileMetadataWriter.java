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
package org.constellation.metadata.io.filesystem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

// JAXB dependencies
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//geotoolkit dependencies
import org.geotoolkit.lucene.index.AbstractIndexer;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.AbstractMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.utils.Utils;
import org.constellation.util.NodeUtilities;

import static org.constellation.metadata.io.filesystem.FileMetadataUtils.*;
import org.constellation.metadata.io.filesystem.sql.MetadataDatasource;
import org.constellation.metadata.io.filesystem.sql.Session;


/**
 * A CSW Metadata Writer. This writer does not require a database.
 * The CSW records are stored XML file in a directory .
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileMetadataWriter extends AbstractMetadataWriter {

    /**
     * An indexer lucene to add object into the index.
     */
    private final AbstractIndexer indexer;

    /**
     * A directory in witch the metadata files are stored.
     */
    private final File dataDirectory;

    private final String serviceID;
    
    /**
     * Build a new File metadata writer, with the specified indexer.
     *
     * @param indexer A lucene indexer.
     * @param configuration An object containing all the dataSource informations (in this case the data directory).
     *
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    public FileMetadataWriter(final Automatic configuration, final AbstractIndexer indexer, final String serviceID) throws MetadataIoException {
        this.indexer = indexer;
        this.serviceID = serviceID;
        dataDirectory = configuration.getDataDirectory();
        if (dataDirectory == null || !dataDirectory.isDirectory()) {
            throw new MetadataIoException("Unable to find the data directory", NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean storeMetadata(final Node original) throws MetadataIoException {
        try {

            final String identifier = Utils.findIdentifier(original);
            final File f;
            // for windows we avoid to create file with ':'
            if (System.getProperty("os.name", "").startsWith("Windows")) {
                final String windowsIdentifier = identifier.replace(':', '-');
                f = new File(dataDirectory, windowsIdentifier + ".xml");
            } else {
                f = new File(dataDirectory, identifier + ".xml");
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            FileWriter writer = new FileWriter(f);
            transformer.transform(new DOMSource(original), new StreamResult(writer));
            
            
            if (indexer != null) {
                indexer.indexDocument(original);
            }

            Session session = null;
            try {
                session = MetadataDatasource.createSession(serviceID);
                session.putRecord(identifier, f.getPath());
            } catch (SQLException ex) {
                throw new MetadataIoException("SQL Exception while reading path for record", ex, NO_APPLICABLE_CODE);
            } finally {
                if (session != null) {
                    session.close();
                }
            }
            
        } catch (IOException | TransformerException ex) {
            throw new MetadataIoException("Unable to write the file.", ex, NO_APPLICABLE_CODE);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteSupported() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateSupported() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteMetadata(final String metadataID) throws MetadataIoException {
        final File metadataFile = getFileFromIdentifier(metadataID);
        if (metadataFile.exists()) {
           final boolean suceed =  metadataFile.delete();
           if (suceed) {
               if (indexer != null) {
                   indexer.removeDocument(metadataID);
               }
               Session session = null;
                try {
                    session = MetadataDatasource.createSession(serviceID);
                    session.removeRecord(metadataID);
                } catch (SQLException ex) {
                    throw new MetadataIoException("SQL Exception while reading path for record", ex, NO_APPLICABLE_CODE);
                } finally {
                    if (session != null) {
                        session.close();
                    }
                }
           } else {
               LOGGER.severe("unable to delete the matadata file");
           }
           return suceed;
        } else {
            throw new MetadataIoException("The metadataFile : " + metadataID + ".xml is not present", INVALID_PARAMETER_VALUE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean replaceMetadata(final String metadataID, final Node any) throws MetadataIoException {
        final boolean succeed = deleteMetadata(metadataID);
        if (!succeed) {
            return false;
        }
        return storeMetadata(any);
    }

    @Override
    public boolean isAlreadyUsedIdentifier(String metadataID) throws MetadataIoException {
        return getFileFromIdentifier(metadataID) != null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateMetadata(final String metadataID, final Map<String , Object> properties) throws MetadataIoException {
        final Document metadataDoc = getDocumentFromFile(metadataID);
        for (Entry<String, Object> property : properties.entrySet()) {
            String xpath = property.getKey();
            // we remove the first / before the type declaration
            if (xpath.startsWith("/")) {
                xpath = xpath.substring(1);
            }
            if (xpath.indexOf('/') != -1) {

                //we get the type of the metadata (first part of the Xpath
                String typeName = xpath.substring(0, xpath.indexOf('/'));
                if (typeName.contains(":")) {
                    typeName = typeName.substring(typeName.indexOf(':') + 1);
                }

                Node parent = metadataDoc.getDocumentElement();

                // we verify that the metadata to update has the same type that the Xpath type
                if (!parent.getLocalName().equals(typeName)) {
                    throw new MetadataIoException("The metadata :" + metadataID + " is not of the same type that the one describe in Xpath expression", INVALID_PARAMETER_VALUE);
                }

                //we remove the type name from the xpath
                xpath = xpath.substring(xpath.indexOf('/') + 1);

                List<Node> nodes = Arrays.asList(parent);
                while (xpath.indexOf('/') != -1) {

                    //Then we get the next Property name

                    String propertyName = xpath.substring(0, xpath.indexOf('/'));
                    final int ordinal   = NodeUtilities.extractOrdinal(propertyName);
                    final int braceIndex = propertyName.indexOf('[');
                    if (braceIndex != -1) {
                        propertyName = propertyName.substring(0,braceIndex);
                    }

                    //remove namespace on propertyName
                    final int separatorIndex = propertyName.indexOf(':');
                    if (separatorIndex != -1) {
                        propertyName = propertyName.substring(separatorIndex + 1);
                    }

                    nodes = NodeUtilities.getNodes(propertyName, nodes, ordinal, true);

                    xpath = xpath.substring(xpath.indexOf('/') + 1);
                }

                // we update the metadata
                final Node value = (Node) property.getValue();

                //remove namespace on propertyName
                final int separatorIndex = xpath.indexOf(':');
                if (separatorIndex != -1) {
                    xpath = xpath.substring(separatorIndex + 1);
                }
                
                updateObjects(nodes, xpath, value);

                // we finish by updating the metadata.
                deleteMetadata(metadataID);
                storeMetadata(metadataDoc.getDocumentElement());
                return true;

            }
        }
        return false;
    }

    /**
     * Update an object by calling the setter of the specified property with the specified value.
     * 
     * @param parent The parent object on witch call the setters.
     * @param propertyName The name of the property to update on the parent (can contain an ordinal).
     * @param value The new value to update.
     * 
     * @throws org.constellation.ws.MetadataIoException
     */
    private void updateObjects(List<Node> nodes, String propertyName, Node value) throws MetadataIoException {

        Class parameterType = value.getClass();
        LOGGER.log(Level.FINER, "parameter type:{0}", parameterType);

        final String fullPropertyName = propertyName;
        final int ordinal             = NodeUtilities.extractOrdinal(propertyName);
        if (propertyName.indexOf('[') != -1) {
            propertyName = propertyName.substring(0, propertyName.indexOf('['));
        }

        for (Node e : nodes) {
            final List<Node> toUpdate = NodeUtilities.getChilds(e, propertyName);

            // ADD
            if (toUpdate.isEmpty()) {
                final Node newNode = e.getOwnerDocument().createElementNS("TODO", propertyName);
                final Node clone   = e.getOwnerDocument().importNode(value, true);
                newNode.appendChild(clone);
                e.appendChild(newNode);

            // UPDATE
            } else {
                for (int i = 0; i < toUpdate.size(); i++) {
                    if (ordinal == -1 || i == ordinal) {
                        Node n = toUpdate.get(i);
                        final Node firstChild = getFirstChild(n, value instanceof Text);
                        if (firstChild != null) {
                            final Node clone = n.getOwnerDocument().importNode(value, true);
                            n.replaceChild(clone, firstChild);
                        }
                    }
                }
            }
        }
    }

    private Document getDocumentFromFile(String identifier) throws MetadataIoException {
        final File metadataFile = getFileFromIdentifier(identifier);
        if (metadataFile.exists()) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                Document document = docBuilder.parse(metadataFile);
                return document;
            } catch (SAXException | IOException | ParserConfigurationException ex) {
                throw new MetadataIoException("The metadataFile : " + identifier + ".xml can not be read\ncause: " + ex.getMessage(), ex, INVALID_PARAMETER_VALUE);
            }
        } else {
            throw new MetadataIoException("The metadataFile : " + identifier + ".xml is not present", INVALID_PARAMETER_VALUE);
        }
    }

    private Node getFirstChild(final Node n, final boolean isText) {
        final NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            final Node child = nl.item(i);
            if (isText || (!(child instanceof Text) && !(child instanceof Comment))) {
                return child;
            }
        }
        return null;
    }

    private File getFileFromIdentifier(final String identifier) throws MetadataIoException {
        Session session = null;
        try {
            session = MetadataDatasource.createSession(serviceID);
            final String path = session.getPathForRecord(identifier);

            return new File(path);
        } catch (SQLException ex) {
            throw new MetadataIoException("SQL Exception while reading path for record", ex, NO_APPLICABLE_CODE);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Destoy all the resource and close connection.
     */
    @Override
    public void destroy() {
        if (indexer != null) {
            indexer.destroy();
        }
        MetadataDatasource.close(serviceID);
    }
}
