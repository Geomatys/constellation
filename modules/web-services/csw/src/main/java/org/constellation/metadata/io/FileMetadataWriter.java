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


package org.constellation.metadata.io;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.cat.csw.v202.RecordPropertyType;
import org.constellation.cat.csw.v202.RecordType;
import org.constellation.generic.database.Automatic;
import org.constellation.lucene.index.AbstractIndexer;
import org.constellation.metadata.CSWClassesContext;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.metadata.iso.DefaultMetaData;
import org.geotoolkit.util.SimpleInternationalString;
import org.geotoolkit.xml.MarshallerPool;
import org.opengis.util.InternationalString;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileMetadataWriter extends MetadataWriter {

    /**
     * The maximum number of elements in a queue of marshallers and unmarshallers.
     */
    private static final int MAX_QUEUE_SIZE = 4;

    /**
     * A marshaller to store object from harvested resource.
     */
    private final  MarshallerPool marshallerPool;
    
    /**
     * A directory in witch the metadata files are stored.
     */
    private final File dataDirectory;
    
    /**
     * 
     * @param index
     * @param marshaller
     * @throws java.sql.SQLException
     */
    public FileMetadataWriter(Automatic configuration, AbstractIndexer index) throws CstlServiceException {
        super(index);
        dataDirectory   = configuration.getdataDirectory();
        if (dataDirectory == null || !dataDirectory.exists()) {
            throw new CstlServiceException("Unable to find the data directory", NO_APPLICABLE_CODE);
        }
        
        try {
            marshallerPool = new MarshallerPool(CSWClassesContext.getAllClasses());
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB excepiton while creating unmarshaller", NO_APPLICABLE_CODE);
        }
        
    }

    @Override
    public boolean storeMetadata(Object obj) throws CstlServiceException {
        File f = null;
        Marshaller marshaller = null;
        try {
            marshaller = marshallerPool.acquireMarshaller();
            String identifier = findIdentifier(obj);
            f = new File(dataDirectory, identifier + ".xml");
            f.createNewFile();
            marshaller.marshal(obj, f);
            indexer.indexDocument(obj);
            
        } catch (JAXBException ex) {
            throw new CstlServiceException("Unable to marshall the object: " + obj, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("Unable to write the file: " + f.getPath(), NO_APPLICABLE_CODE);
        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
        return true;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public boolean deleteSupported() {
        return true;
    }

    @Override
    public boolean updateSupported() {
        return true;
    }

    @Override
    public boolean deleteMetadata(String metadataID) throws CstlServiceException {
        File metadataFile = new File (dataDirectory,  metadataID + ".xml");
        if (metadataFile.exists()) {
           boolean suceed =  metadataFile.delete();
           if (suceed) {
               indexer.removeDocument(metadataID);
           }
           return suceed;
        } else {
            throw new CstlServiceException("The metadataFile : " + metadataID + ".xml is not present", INVALID_PARAMETER_VALUE);
        }
    }

    @Override
    public boolean replaceMetadata(String metadataID, Object any) throws CstlServiceException {
        boolean succeed = deleteMetadata(metadataID);
        if (!succeed)
            return false;
        return storeMetadata(any);
    }

    @Override
    public boolean updateMetadata(String metadataID, List<RecordPropertyType> properties) throws CstlServiceException {
        Object metadata = getObjectFromFile(metadataID);
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
                    type = DefaultMetaData.class;
                } else if (typeName.equals("Record")) {
                    type = RecordType.class;
                } else {
                    throw new CstlServiceException("This metadata type is not allowed:" + typeName + "\n Allowed ones are: MD_Metadata or Record", INVALID_PARAMETER_VALUE);
                }
                LOGGER.finer("update type:" + type);
                
                // we verify that the metadata to update has the same type that the Xpath type
                if (!metadata.getClass().equals(type)) {
                    throw new CstlServiceException("The metadata :" + findIdentifier(metadata) + "is not of the same type that the one describe in Xpath expression", INVALID_PARAMETER_VALUE);
                }

                //we remove the type name from the xpath
                xpath = xpath.substring(xpath.indexOf('/') + 1);

                Object parent = metadata;
                while (xpath.indexOf('/') != -1) {
                    
                    //Then we get the next Property name
                    int ordinal = -1;
                    String propertyName = xpath.substring(0, xpath.indexOf('/'));

                    //we extract the ordinal if there is one
                    if (propertyName.indexOf('[') != -1) {
                        if (propertyName.indexOf(']') != -1) {
                            try {
                                String ordinalValue = propertyName.substring(propertyName.indexOf('[') + 1, propertyName.indexOf(']'));
                                ordinal = Integer.parseInt(ordinalValue);
                            } catch (NumberFormatException ex) {
                                throw new CstlServiceException("The xpath is malformed, the brackets value is not an integer", NO_APPLICABLE_CODE);
                            }
                            propertyName = propertyName.substring(0, propertyName.indexOf('['));
                        } else {
                            throw new CstlServiceException("The xpath is malformed, unclosed bracket", NO_APPLICABLE_CODE);
                        }
                    }
                    LOGGER.finer("propertyName:" + propertyName + " ordinal=" + ordinal);

                    Class parentClass;
                    if (parent instanceof Collection) {
                        Collection parentCollection = (Collection) parent;
                        if (parentCollection.size() > 0) {
                            parentClass = parentCollection.iterator().next().getClass();
                        } else  {
                            throw new CstlServiceException("An unresolved programmation issue occurs: TODO find the type of an empty collection", NO_APPLICABLE_CODE);
                        }
                    } else {
                        parentClass = parent.getClass();
                    }

                    //we try to find a getter for this property
                    Method getter = Util.getGetterFromName(propertyName, parentClass);
                    if (getter == null) {
                        throw new CstlServiceException("There is no getter for the property:" + propertyName + " in the class:" + type.getSimpleName(), INVALID_PARAMETER_VALUE);
                    } else {
                        // we execute the getter
                        if (!(parent instanceof Collection)) {
                            parent = Util.invokeMethod(parent, getter);
                        } else {
                            Collection tmp = new ArrayList();
                            for (Object child : (Collection) parent) {
                                tmp.add(Util.invokeMethod(child, getter));
                            }
                            parent = tmp;
                        }
                    }

                    if (ordinal != -1) {

                        if (!(parent instanceof Collection)) {
                            throw new CstlServiceException("The property:" + propertyName + " in the class:" + parentClass + " is not a collection", INVALID_PARAMETER_VALUE);
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
                Object value = property.getValue();
                
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
     * Update an object by calling the setter of the specified property with the specified value
     * 
     * @param parent
     * @param propertyName
     * @param value
     * @throws org.constellation.ws.CstlServiceException
     */
    private void updateObjects(Object parent, String propertyName, Object value) throws CstlServiceException {

        Class parameterType = value.getClass();
        LOGGER.finer("parameter type:" + parameterType);

        String fullPropertyName = propertyName;
        //we extract the ordinal if there is one
        int ordinal = -1;
        if (propertyName.indexOf('[') != -1) {
            if (propertyName.indexOf(']') != -1) {
                try {
                    String ordinalValue = propertyName.substring(propertyName.indexOf('[') + 1, propertyName.indexOf(']'));
                    ordinal = Integer.parseInt(ordinalValue);
                } catch (NumberFormatException ex) {
                    throw new CstlServiceException("The xpath is malformed, the brackets value is not an integer", NO_APPLICABLE_CODE);
                }
                propertyName = propertyName.substring(0, propertyName.indexOf('['));
            } else {
                throw new CstlServiceException("The xpath is malformed, unclosed bracket", NO_APPLICABLE_CODE);
            }
        }

        //Special case for language
        if (propertyName.equalsIgnoreCase("language")) {
            parameterType = Locale.class;
            if (value instanceof String) {
                value = new Locale((String) value);
            } else {
                throw new CstlServiceException("The value's type of the recordProperty does not match the specified property type language accept only string type",
                        INVALID_PARAMETER_VALUE);
            }
        }

        //Special case for dateStamp
        if (propertyName.contains("date") && parameterType.equals(String.class)) {
            parameterType = Date.class;
            try {
                value = dateFormat.parse((String) value);
            } catch (ParseException ex) {
                throw new CstlServiceException("There service was unable to parse the date:" + value, INVALID_PARAMETER_VALUE);
            }
        }

        if (parent instanceof Collection) {
            for (Object single : (Collection) parent) {
                updateObjects(single, fullPropertyName, value);
            }
        } else {
            updateObject(propertyName, parent, value, parameterType, ordinal);
        }
    }

    private void updateObject(String propertyName, Object parent, Object value, Class parameterType, int ordinal) throws CstlServiceException {
        Method setter = Util.getSetterFromName(propertyName, parameterType, parent.getClass());

        // with the geotools implementation we sometimes have to used InternationalString instead of String.
        if (setter == null && parameterType.equals(String.class)) {
            setter = Util.getSetterFromName(propertyName, InternationalString.class, parent.getClass());
            value = new SimpleInternationalString((String) value);
        }

        //if there is an ordinal we must get the existant Collection
        if (ordinal != -1) {
            Method getter = Util.getGetterFromName(propertyName, parent.getClass());
            if (getter == null) {
                throw new CstlServiceException("There is no getter for the property:" + propertyName + " in the class:" + parent.getClass(), INVALID_PARAMETER_VALUE);
            }
            Object existant = Util.invokeMethod(parent, getter);

            if (!(existant instanceof Collection)) {
                throw new CstlServiceException("The property:" + propertyName + " in the class:" + parent.getClass() + " is not a collection", INVALID_PARAMETER_VALUE);
            } 

            Collection c = (Collection) existant;
            if (c.size() < ordinal) {
                throw new CstlServiceException("The property:" + propertyName + " in the class:" + parent.getClass() + " got only" + c.size() + " elements", INVALID_PARAMETER_VALUE);
            }

            if (parameterType.equals(String.class) && c.iterator().hasNext() && c.iterator().next() instanceof InternationalString) {
                value = new SimpleInternationalString((String) value);
            }

            // ISSUE how to add in a Collection at a predefined index
            if (c instanceof List) {
               List l = (List) c;
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
            throw new CstlServiceException("There is no setter for the property:" + propertyName + " in the class:" + parent.getClass(), INVALID_PARAMETER_VALUE);
        } else {
            String baseMessage = "Unable to invoke the method " + setter + ": ";
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
                        Util.invokeMethodEx(setter, parent, c);
                    }
                } else {
                    Util.invokeMethodEx(setter, parent, value);
                }
            } catch (IllegalAccessException ex) {
                throw new CstlServiceException(baseMessage + "the class is not accessible.", NO_APPLICABLE_CODE);

            } catch (IllegalArgumentException ex) {
                String param = "null";
                if (value != null) {
                    param = value.getClass().getSimpleName();
                }
                throw new CstlServiceException(baseMessage + "the given argument does not match that required by the method.( argument type was " + param + ")");

            } catch (InvocationTargetException ex) {
                String errorMsg = ex.getMessage();
                if (errorMsg == null && ex.getCause() != null) {
                    errorMsg = ex.getCause().getMessage();
                }
                if (errorMsg == null && ex.getTargetException() != null) {
                    errorMsg = ex.getTargetException().getMessage();
                }
                throw new CstlServiceException(baseMessage + "an Exception was thrown in the invoked method:" + errorMsg);
            }
        }
    }

    public static Object invokeMethodStrColl(final Method method, final Object object, final String parameter) throws CstlServiceException {
        String baseMessage = "Unable to invoke the method " + method + ": ";
        Object result = null;
        if (method != null) {
            int i = 0;
            CstlServiceException exe = null;
            while (i < 2) {
                try {
                    Collection c = new ArrayList(1);
                    if (i == 0) {
                        c.add(parameter);
                    } else {
                        c.add(new SimpleInternationalString(parameter));
                    }
                    result = method.invoke(object, c);
                    return result;

                } catch (IllegalAccessException ex) {
                    throw new CstlServiceException(baseMessage + "the class is not accessible.", NO_APPLICABLE_CODE);

                } catch (IllegalArgumentException ex) {

                    throw new CstlServiceException(baseMessage + "the given argument does not match that required by the method.( argument type was String)");

                } catch (InvocationTargetException ex) {
                    String errorMsg = ex.getMessage();
                    if (errorMsg == null && ex.getCause() != null) {
                        errorMsg = ex.getCause().getMessage();
                    }
                    if (errorMsg == null && ex.getTargetException() != null) {
                        errorMsg = ex.getTargetException().getMessage();
                    }
                    if (i == 1) {
                        throw new CstlServiceException(baseMessage + "an Exception was thrown in the invoked method:" + errorMsg);
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
     * @throws org.constellation.ws.CstlServiceException
     */
    private Object getObjectFromFile(String identifier) throws CstlServiceException {
        File metadataFile = new File (dataDirectory,  identifier + ".xml");
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
                throw new CstlServiceException("The metadataFile : " + identifier + ".xml can not be unmarshalled" + "\n" +
                        "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }
        } else {
            throw new CstlServiceException("The metadataFile : " + identifier + ".xml is not present", INVALID_PARAMETER_VALUE);
        }
    }
}
