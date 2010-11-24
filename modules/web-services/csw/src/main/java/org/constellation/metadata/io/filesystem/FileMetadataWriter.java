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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

// JAXB dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

//geotoolkit dependencies
import org.geotoolkit.csw.xml.v202.RecordPropertyType;
import org.geotoolkit.csw.xml.v202.RecordType;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.metadata.iso.DefaultMetadata;
import org.geotoolkit.util.SimpleInternationalString;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.AbstractCSWMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.utils.Utils;
import org.constellation.util.ReflectionUtilities;

// GeoApi dependencies
import org.opengis.util.InternationalString;


/**
 * A csw Metadata Writer. This writer does not require a database.
 * The csw records are stored XML file in a directory .
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileMetadataWriter extends AbstractCSWMetadataWriter {

    /**
     * A marshaller to store object from harvested resource.
     */
    private final  MarshallerPool marshallerPool;
    
    /**
     * A directory in witch the metadata files are stored.
     */
    private final File dataDirectory;

    /**
     * A string constant frequantly used in error message.
     */
    private static final String IN_CLASS_MSG = " in the class:";
     
    /**
     * Build a new File metadata writer, with the specified indexer.
     *
     * @param index A lucene indexer.
     * @param configuration An object containing all the datasource informations (in this case the data directory).
     *
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    public FileMetadataWriter(Automatic configuration, AbstractIndexer index) throws MetadataIoException {
        super(index);
        File dataDir = configuration.getDataDirectory();
        if (dataDir == null || !dataDir.exists()) {
            final File configDir = configuration.getConfigurationDirectory();
            dataDir = new File(configDir, dataDir.getName());
        }
        dataDirectory = dataDir;
        if (dataDirectory == null || !dataDirectory.exists()) {
            throw new MetadataIoException("Unable to find the data directory", NO_APPLICABLE_CODE);
        }
        marshallerPool = EBRIMMarshallerPool.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean storeMetadata(Object obj) throws MetadataIoException {
        File f = null;
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            final String identifier = Utils.findIdentifier(obj);
            // for windows we avoid to create file with ':'
            if (System.getProperty("os.name", "").startsWith("Windows")) {
                final String windowsIdentifier = identifier.replace(':', '-');
                f = new File(dataDirectory, windowsIdentifier + ".xml");
            } else {
                f = new File(dataDirectory, identifier + ".xml");
            }
            f.createNewFile();
            marshaller.marshal(obj, f);
            indexer.indexDocument(obj);
            
        } catch (JAXBException ex) {
            throw new MetadataIoException("Unable to marshall the object: " + obj, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new MetadataIoException("Unable to write the file: " + f.getPath(), NO_APPLICABLE_CODE);
        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
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
    public boolean deleteMetadata(String metadataID) throws MetadataIoException {
        final File metadataFile = new File (dataDirectory,  metadataID + ".xml");
        if (metadataFile.exists()) {
           final boolean suceed =  metadataFile.delete();
           if (suceed) {
               indexer.removeDocument(metadataID);
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
    public boolean replaceMetadata(String metadataID, Object any) throws MetadataIoException {
        final boolean succeed = deleteMetadata(metadataID);
        if (!succeed)
            return false;
        return storeMetadata(any);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateMetadata(String metadataID, List<RecordPropertyType> properties) throws MetadataIoException {
        final Object metadata = getObjectFromFile(metadataID);
        for (RecordPropertyType property : properties) {
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

                // we look for a know metadata type
                if (typeName.equals("MD_Metadata")) {
                    type = DefaultMetadata.class;
                } else if (typeName.equals("Record")) {
                    type = RecordType.class;
                } else {
                    throw new MetadataIoException("This metadata type is not allowed:" + typeName + "\n Allowed ones are: MD_Metadata or Record", INVALID_PARAMETER_VALUE);
                }
                LOGGER.log(Level.FINER, "update type:{0}", type);
                
                // we verify that the metadata to update has the same type that the Xpath type
                if (!metadata.getClass().equals(type)) {
                    throw new MetadataIoException("The metadata :" + Utils.findIdentifier(metadata) + "is not of the same type that the one describe in Xpath expression", INVALID_PARAMETER_VALUE);
                }

                //we remove the type name from the xpath
                xpath = xpath.substring(xpath.indexOf('/') + 1);

                Object parent = metadata;
                while (xpath.indexOf('/') != -1) {
                    
                    //Then we get the next Property name
                    
                    String propertyName = xpath.substring(0, xpath.indexOf('/'));
                    final int ordinal         = extractOrdinal(propertyName);
                    if (propertyName.indexOf('[') != -1) {
                        propertyName = propertyName.substring(0, propertyName.indexOf('['));
                    }

                    LOGGER.finer("propertyName:" + propertyName + " ordinal=" + ordinal);

                    Class parentClass;
                    if (parent instanceof Collection) {
                        final Collection parentCollection = (Collection) parent;
                        if (parentCollection.size() > 0) {
                            parentClass = parentCollection.iterator().next().getClass();
                        } else  {
                            throw new MetadataIoException("An unresolved programmation issue occurs: TODO find the type of an empty collection", NO_APPLICABLE_CODE);
                        }
                    } else {
                        parentClass = parent.getClass();
                    }

                    //we try to find a getter for this property
                    final Method getter = ReflectionUtilities.getGetterFromName(propertyName, parentClass);
                    if (getter == null) {
                        throw new MetadataIoException("There is no getter for the property:" + propertyName + IN_CLASS_MSG + type.getSimpleName(), INVALID_PARAMETER_VALUE);
                    } else {
                        // we execute the getter
                        if (!(parent instanceof Collection)) {
                            parent = ReflectionUtilities.invokeMethod(parent, getter);
                        } else {
                            final Collection tmp = new ArrayList();
                            for (Object child : (Collection) parent) {
                                tmp.add(ReflectionUtilities.invokeMethod(child, getter));
                            }
                            parent = tmp;
                        }
                    }

                    if (ordinal != -1) {

                        if (!(parent instanceof Collection)) {
                            throw new MetadataIoException("The property:" + propertyName + IN_CLASS_MSG + parentClass + " is not a collection", INVALID_PARAMETER_VALUE);
                        }
                        Object tmp = null;
                        for (Object child : (Collection) parent) {
                            int i = 1;
                            for (Object o : (Collection) child) {
                                if (i == ordinal) {
                                    tmp = o;
                                }
                                i++;
                            }
                        }
                        parent = tmp;
                    }
                    xpath = xpath.substring(xpath.indexOf('/') + 1);
                }

                // we update the metadata
                final Object value = property.getValue();
                
                updateObjects(parent, xpath, value);

                // we finish by updating the metadata.
                deleteMetadata(metadataID);
                storeMetadata(metadata);
                return true;

            }
        }
        return false;
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
                    ordinal = Integer.parseInt(ordinalValue);
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
    private void updateObjects(Object parent, String propertyName, Object value) throws MetadataIoException {

        Class parameterType = value.getClass();
        LOGGER.log(Level.FINER, "parameter type:{0}", parameterType);

        final String fullPropertyName = propertyName;
        final int ordinal             = extractOrdinal(propertyName);
        if (propertyName.indexOf('[') != -1) {
            propertyName = propertyName.substring(0, propertyName.indexOf('['));
        }

        //Special case for language
        if (propertyName.equalsIgnoreCase("language")) {
            parameterType = Locale.class;
            if (value instanceof String) {
                value = new Locale((String) value);
            } else {
                throw new MetadataIoException("The value's type of the recordProperty does not match the specified property type language accept only string type",
                        INVALID_PARAMETER_VALUE);
            }
        }

        //Special case for dateStamp
        if (propertyName.contains("date") && parameterType.equals(String.class)) {
            parameterType = Date.class;
            value = parseDate((String) value);
        }

        if (parent instanceof Collection) {
            for (Object single : (Collection) parent) {
                updateObjects(single, fullPropertyName, value);
            }
        } else {
            updateObject(propertyName, parent, value, parameterType, ordinal);
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
                throw new MetadataIoException("The property:" + propertyName + IN_CLASS_MSG + parent.getClass() + " is not a collection", INVALID_PARAMETER_VALUE);
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
        final File metadataFile = new File (dataDirectory,  identifier + ".xml");
        if (metadataFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                Object metadata = unmarshaller.unmarshal(metadataFile);
                if (metadata instanceof JAXBElement) {
                    metadata = ((JAXBElement) metadata).getValue();
                }
                return metadata;
            } catch (JAXBException ex) {
                throw new MetadataIoException("The metadataFile : " + identifier + ".xml can not be unmarshalled" + "\n" +
                        "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }
        } else {
            throw new MetadataIoException("The metadataFile : " + identifier + ".xml is not present", INVALID_PARAMETER_VALUE);
        }
    }

    /**
     * Try to parse a date in a string.
     * If the string can not be parsed a MetadataIoException will be throw.
     *
     * @param dateValue the string representation of the date.
     * @return a Date object.
     *
     * @throws MetadataIoException if the string can not be parsed.
     */
    private Date parseDate(String dateValue) throws MetadataIoException {
        // in the case of a timezone expressed like this +01:00 we must transform it in +0100
        if (dateValue.indexOf('.') != -1) {
            String msNtz = dateValue.substring(dateValue.indexOf('.'));
            if (msNtz.indexOf(':') != -1) {
                msNtz = msNtz.replace(":", "");
                dateValue = dateValue.substring(0, dateValue.indexOf('.'));
                dateValue = dateValue + msNtz;
            }
        }
        Date result = null;
        boolean success = true;
        try {
            result = DATE_FORMAT.get(0).parse((String) dateValue);
        } catch (ParseException ex) {
            success = false;
        }
        if (!success) {
            try {
               result = DATE_FORMAT.get(1).parse((String) dateValue);
            } catch (ParseException ex) {
                throw new MetadataIoException("There service was unable to parse the date:" + dateValue, INVALID_PARAMETER_VALUE);
            }
        }
        return result;
    }
}
