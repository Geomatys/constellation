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
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.csw.xml.RecordProperty;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.AbstractCSWMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.utils.Utils;
import org.constellation.util.ReflectionUtilities;

import static org.constellation.metadata.io.filesystem.FileMetadataUtils.*;

// GeoApi dependencies
import org.opengis.util.InternationalString;

import org.apache.sis.util.iso.SimpleInternationalString;
import org.apache.sis.xml.MarshallerPool;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * A CSW Metadata Writer. This writer does not require a database.
 * The CSW records are stored XML file in a directory .
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileMetadataWriter extends AbstractCSWMetadataWriter {

    /**
     * A marshaller to store object from harvested resource.
     */
    private final static MarshallerPool marshallerPool = EBRIMMarshallerPool.getInstance();
    
    /**
     * A directory in witch the metadata files are stored.
     */
    private final File dataDirectory;

    /**
     * A string constant frequently used in error message.
     */
    private static final String IN_CLASS_MSG = " in the class:";
     
    /**
     * Build a new File metadata writer, with the specified indexer.
     *
     * @param index A lucene indexer.
     * @param configuration An object containing all the dataSource informations (in this case the data directory).
     *
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    public FileMetadataWriter(Automatic configuration, AbstractIndexer index) throws MetadataIoException {
        super(index);
        File dataDir = configuration.getDataDirectory();
        if (!dataDir.exists()) {
            final File configDir = configuration.getConfigurationDirectory();
            dataDir = new File(configDir, dataDir.getName());
        }
        dataDirectory = dataDir;
        if (dataDirectory == null || !dataDirectory.isDirectory()) {
            throw new MetadataIoException("Unable to find the data directory", NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean storeMetadata(final Object original) throws MetadataIoException {
        try {
            Object obj;
            if (original instanceof Node) {
                final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
                obj = unmarshaller.unmarshal((Node)original);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                marshallerPool.recycle(unmarshaller);
            } else {
                obj = original;
            }

            final String identifier = Utils.findIdentifier(obj);
            final File f;
            // for windows we avoid to create file with ':'
            if (System.getProperty("os.name", "").startsWith("Windows")) {
                final String windowsIdentifier = identifier.replace(':', '-');
                f = new File(dataDirectory, windowsIdentifier + ".xml");
            } else {
                f = new File(dataDirectory, identifier + ".xml");
            }

            if (original instanceof Node) {
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                FileWriter writer = new FileWriter(f);
                transformer.transform(new DOMSource((Node)original), new StreamResult(writer));
            } else {
                final Marshaller marshaller = marshallerPool.acquireMarshaller();
                marshaller.marshal(obj, f);
                marshallerPool.recycle(marshaller);
            }
            
            if (indexer != null) {
                indexer.indexDocument(obj);
            }
            
        } catch (JAXBException | IOException | TransformerException ex) {
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
        final File metadataFile = getFileFromIdentifier(metadataID, dataDirectory);
        if (metadataFile.exists()) {
           final boolean suceed =  metadataFile.delete();
           if (suceed) {
               if (indexer != null) {
                   indexer.removeDocument(metadataID);
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
    public boolean replaceMetadata(final String metadataID, final Object any) throws MetadataIoException {
        final boolean succeed = deleteMetadata(metadataID);
        if (!succeed) {
            return false;
        }
        return storeMetadata(any);
    }

    @Override
    public boolean isAlreadyUsedIdentifier(String metadataID) throws MetadataIoException {
        return getFileFromIdentifier(metadataID, dataDirectory) != null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateMetadata(final String metadataID, final List<? extends RecordProperty> properties) throws MetadataIoException {
        final Document metadataDoc = getDocumentFromFile(metadataID);
        for (RecordProperty property : properties) {
            String xpath = property.getName();
            // we remove the first / before the type declaration
            if (xpath.startsWith("/")) {
                xpath = xpath.substring(1);
            }
            if (xpath.indexOf('/') != -1) {

                //we get the type of the metadata (first part of the Xpath
                Class type;
                String typeName = xpath.substring(0, xpath.indexOf('/'));
                if (typeName.contains(":")) {
                    typeName = typeName.substring(typeName.indexOf(':') + 1);
                }

                Element parent = metadataDoc.getDocumentElement();

                // we verify that the metadata to update has the same type that the Xpath type
                if (!parent.getLocalName().equals(typeName)) {
                    throw new MetadataIoException("The metadata :" + metadataID + " is not of the same type that the one describe in Xpath expression", INVALID_PARAMETER_VALUE);
                }

                //we remove the type name from the xpath
                xpath = xpath.substring(xpath.indexOf('/') + 1);

                List<Element> nodes = Arrays.asList(parent);
                while (xpath.indexOf('/') != -1) {

                    //Then we get the next Property name

                    String propertyName = xpath.substring(0, xpath.indexOf('/'));
                    final int ordinal         = extractOrdinal(propertyName);
                    final int braceIndex = propertyName.indexOf('[');
                    if (braceIndex != -1) {
                        propertyName = propertyName.substring(0,braceIndex);
                    }

                    //remove namespace on propertyName
                    final int separatorIndex = propertyName.indexOf(':');
                    if (separatorIndex != -1) {
                        propertyName = propertyName.substring(separatorIndex + 1);
                    }

                    LOGGER.finer("propertyName:" + propertyName + " ordinal=" + ordinal);

                    
                    nodes = getNodes(propertyName, nodes, ordinal);

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

    private List<Element> getNodes(final String propertyName, List<Element> nodes, int ordinal) {
        final List<Element> result = new ArrayList<>();
        for (Element e : nodes) {
            final List<Node> nl = getChilds(e, propertyName);
            // add new node
            if (nl.isEmpty()) {
                final Element newNode = e.getOwnerDocument().createElementNS("TODO", propertyName);
                e.appendChild(newNode);
                result.add(newNode);
                
            // Select the node to update
            } else {
                for (int i = 0 ; i < nl.size(); i++) {
                    if (ordinal == -1) {
                        result.add((Element) nl.get(i));
                    } else if (i == ordinal) {
                        result.add((Element) nl.get(i));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Return an ordinal if there is one in the propertyName specified else return -1.
     * example : name[1] return  1
     *           name    return -1
     * @param propertyName A property name extract from an Xpath
     * @return an ordinal if there is one, -1 else.
     * @throws MetadataIoException
     */
    private int extractOrdinal(String propertyName) throws MetadataIoException {
        int ordinal = -1;

        //we extract the ordinal if there is one
        if (propertyName.indexOf('[') != -1) {
            if (propertyName.indexOf(']') != -1) {
                try {
                    final String ordinalValue = propertyName.substring(propertyName.indexOf('[') + 1, propertyName.indexOf(']'));
                    ordinal = Integer.parseInt(ordinalValue) - 1;
                } catch (NumberFormatException ex) {
                    throw new MetadataIoException("The xpath is malformed, the brackets value is not an integer", NO_APPLICABLE_CODE);
                }
            } else {
                throw new MetadataIoException("The xpath is malformed, unclosed bracket", NO_APPLICABLE_CODE);
            }
        }
        return ordinal;
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
    private void updateObjects(List<Element> nodes, String propertyName, Node value) throws MetadataIoException {

        Class parameterType = value.getClass();
        LOGGER.log(Level.FINER, "parameter type:{0}", parameterType);

        final String fullPropertyName = propertyName;
        final int ordinal             = extractOrdinal(propertyName);
        if (propertyName.indexOf('[') != -1) {
            propertyName = propertyName.substring(0, propertyName.indexOf('['));
        }

        for (Element e : nodes) {
            final List<Node> toUpdate = getChilds(e, propertyName);

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

    /**
     * Update a single  object by calling the setter of the specified property with the specified value.
     *
     * @param propertyName  The name of the property to update on the parent (can't contain an ordinal).
     * @param parent The parent object on witch call the setters.
     * @param value The new value to update.
     * @param parameterType The class of the parameter
     * @param ordinal The ordinal of the value in a collection.
     *
     * @throws MetadataIoException
     */
    private void updateObject(String propertyName, Object parent, Object value, Class parameterType, int ordinal) throws MetadataIoException {
        Method setter = ReflectionUtilities.getSetterFromName(propertyName, parameterType, parent.getClass());

        // with the geotoolkit implementation we sometimes have to used InternationalString instead of String.
        if (setter == null && parameterType.equals(String.class)) {
            setter = ReflectionUtilities.getSetterFromName(propertyName, InternationalString.class, parent.getClass());
            value = new SimpleInternationalString((String) value);
        }

        //if there is an ordinal we must get the existant Collection
        if (ordinal != -1) {
            final Method getter = ReflectionUtilities.getGetterFromName(propertyName, parent.getClass());
            if (getter == null) {
                throw new MetadataIoException("There is no getter for the property:" + propertyName + IN_CLASS_MSG + parent.getClass(), INVALID_PARAMETER_VALUE);
            }
            final Object existant = ReflectionUtilities.invokeMethod(parent, getter);

            if (!(existant instanceof Collection)) {
                throw new MetadataIoException("The property: " + propertyName + IN_CLASS_MSG + parent.getClass() + " is not a collection", INVALID_PARAMETER_VALUE);
            } 

            final Collection c = (Collection) existant;
            if (c.size() < ordinal) {
                throw new MetadataIoException("The property:" + propertyName + IN_CLASS_MSG + parent.getClass() + " got only" + c.size() + " elements", INVALID_PARAMETER_VALUE);
            }

            if (parameterType.equals(String.class) && c.iterator().hasNext() && c.iterator().next() instanceof InternationalString) {
                value = new SimpleInternationalString((String) value);
            }

            // ISSUE how to add in a Collection at a predefined index
            if (c instanceof List) {
               final List l = (List) c;
               l.remove(ordinal);
               l.add(ordinal, value);
            } else {
                int i = 1;
                Object toDelete = null;
                for (Object o : c) {
                    if (i == ordinal) {
                        toDelete = o;
                    }
                    i++;
                }
                c.remove(toDelete);
                c.add(value);
            }
            value = existant;
        }

        if (setter == null) {
            throw new MetadataIoException("There is no setter for the property:" + propertyName + IN_CLASS_MSG + parent.getClass(), INVALID_PARAMETER_VALUE);
        } else {
            final String baseMessage = "Unable to invoke the method " + setter + ": ";
            try {
                // we execute the setter
                if (setter.getParameterTypes().length == 1 && setter.getParameterTypes()[0] == Collection.class) {
                    if (value instanceof String) {
                        invokeMethodStrColl(setter, parent, (String) value);
                    } else {
                        Collection c;
                        if (value instanceof Collection) {
                            c = (Collection) value;
                        } else {
                            c = new ArrayList(1);
                            c.add(value);
                        }
                        ReflectionUtilities.invokeMethodEx(setter, parent, c);
                    }
                } else {
                    ReflectionUtilities.invokeMethodEx(setter, parent, value);
                }
            } catch (IllegalAccessException ex) {
                throw new MetadataIoException(baseMessage + "the class is not accessible.", NO_APPLICABLE_CODE);

            } catch (IllegalArgumentException ex) {
                String param = "null";
                if (value != null) {
                    param = value.getClass().getSimpleName();
                }
                throw new MetadataIoException(baseMessage + "the given argument does not match that required by the method.( argument type was " + param + ")");

            } catch (InvocationTargetException ex) {
                String errorMsg = ex.getMessage();
                if (errorMsg == null && ex.getCause() != null) {
                    errorMsg = ex.getCause().getMessage();
                }
                if (errorMsg == null && ex.getTargetException() != null) {
                    errorMsg = ex.getTargetException().getMessage();
                }
                throw new MetadataIoException(baseMessage + "an Exception was thrown in the invoked method:" + errorMsg);
            }
        }
    }

    /**
     * Try to set a collection of String or International string on a parent object.
     *
     * @param method A setter.
     * @param object An object to set.
     * @param parameter A string value to add to object.
     *
     * @throws MetadataIoException
     */
    public static Object invokeMethodStrColl(final Method method, final Object object, final String parameter) throws MetadataIoException {
        final String baseMessage = "Unable to invoke the method " + method + ": ";
        Object result = null;
        if (method != null) {
            int i = 0;
            while (i < 2) {
                try {
                    final Collection c = new ArrayList(1);
                    if (i == 0) {
                        c.add(parameter);
                    } else {
                        c.add(new SimpleInternationalString(parameter));
                    }
                    result = method.invoke(object, c);
                    return result;

                } catch (IllegalAccessException ex) {
                    throw new MetadataIoException(baseMessage + "the class is not accessible.", NO_APPLICABLE_CODE);

                } catch (IllegalArgumentException ex) {

                    throw new MetadataIoException(baseMessage + "the given argument does not match that required by the method.( argument type was String)");

                } catch (InvocationTargetException ex) {
                    String errorMsg = ex.getMessage();
                    if (errorMsg == null && ex.getCause() != null) {
                        errorMsg = ex.getCause().getMessage();
                    }
                    if (errorMsg == null && ex.getTargetException() != null) {
                        errorMsg = ex.getTargetException().getMessage();
                    }
                    if (i == 1) {
                        throw new MetadataIoException(baseMessage + "an Exception was thrown in the invoked method:" + errorMsg);
                    }
                    i++;
                }
            }
        } else {
            LOGGER.severe("Unable to invoke the method reference is null.");
        }
        return result;
    }

    /**
     * Unmarshall The file designed by the path dataDirectory/identifier.xml
     * If the file is not present or if it is impossible to unmarshall it it return an exception.
     *
     * @param identifier
     * @return
     * @throws org.constellation.ws.MetadataIoException
     */
    private Object getObjectFromFile(String identifier) throws MetadataIoException {
        final File metadataFile = getFileFromIdentifier(identifier, dataDirectory);
        if (metadataFile.exists()) {
            try {
                final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
                Object metadata = unmarshaller.unmarshal(metadataFile);
                marshallerPool.recycle(unmarshaller);
                if (metadata instanceof JAXBElement) {
                    metadata = ((JAXBElement) metadata).getValue();
                }
                return metadata;
            } catch (JAXBException ex) {
                throw new MetadataIoException("The metadataFile : " + identifier + ".xml can not be unmarshalled" + "\n" +
                        "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
            }
        } else {
            throw new MetadataIoException("The metadataFile : " + identifier + ".xml is not present", INVALID_PARAMETER_VALUE);
        }
    }

    private Document getDocumentFromFile(String identifier) throws MetadataIoException {
        final File metadataFile = getFileFromIdentifier(identifier, dataDirectory);
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

    private List<Node> getChilds(final Node n, final String propertyName) {
        final List<Node> results = new ArrayList<>();
        final NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            final Node child = nl.item(i);
            if (propertyName.equals(child.getLocalName())) {
                results.add(child);
            } /*else {
                // we go down for one more level, to escape the typeNode
                final NodeList nl2 = child.getChildNodes();
                for (int j = 0; j < nl2.getLength(); j++) {
                    final Node child2 = nl2.item(j);
                    if (propertyName.equals(child2.getLocalName())) {
                        results.add(child2);
                    }
                }
            }*/
        }
        return results;
    }

    private static String getStringFromNode(final Node n) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(n), new StreamResult(writer));
        String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
        return output;
    }
}
