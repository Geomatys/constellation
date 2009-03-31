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
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import javax.xml.bind.JAXBContext;
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
import org.geotools.metadata.iso.MetaDataImpl;
import org.geotools.util.SimpleInternationalString;
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
    private final  LinkedBlockingQueue<Marshaller> marshallers;

    /**
     * A marshaller to store object from harvested resource.
     */
    private final  LinkedBlockingQueue<Unmarshaller> unmarshallers;
    
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
        marshallers   = new LinkedBlockingQueue<Marshaller>(MAX_QUEUE_SIZE);
        unmarshallers = new LinkedBlockingQueue<Unmarshaller>(MAX_QUEUE_SIZE);
        try {
            JAXBContext context = JAXBContext.newInstance(CSWClassesContext.getAllClasses());
            for (int i = 0; i < MAX_QUEUE_SIZE; i++) {
                  marshallers.add(context.createMarshaller());
                  unmarshallers.add(context.createUnmarshaller());
            }
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXB excepiton while creating unmarshaller", NO_APPLICABLE_CODE);
        }
        
    }

    @Override
    public boolean storeMetadata(Object obj) throws CstlServiceException {
        File f = null;
        Marshaller marshaller = null;
        try {
            marshaller = marshallers.take();
            String identifier = findIdentifier(obj);
            f = new File(dataDirectory, identifier + ".xml");
            f.createNewFile();
            marshaller.marshal(obj, f);
            indexer.indexDocument(obj);
            
        } catch (InterruptedException ex) {
            throw new CstlServiceException("interruptedException while marshalling the object: " + obj, NO_APPLICABLE_CODE);
        } catch (JAXBException ex) {
            throw new CstlServiceException("Unable to marshall the object: " + obj, NO_APPLICABLE_CODE);
        } catch (IOException ex) {
            throw new CstlServiceException("Unable to write the file: " + f.getPath(), NO_APPLICABLE_CODE);
        } finally {
            if (marshaller != null) {
                marshallers.add(marshaller);
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
                    type = MetaDataImpl.class;
                } else if (typeName.equals("Record")) {
                    type = RecordType.class;
                } else {
                    throw new CstlServiceException("This metadata type is not allowed:" + typeName + "\n Allowed ones are: MD_Metadata or Record", INVALID_PARAMETER_VALUE);
                }
                LOGGER.info("update type:" + type);
                
                // we verify that the metadata to update has the same type that the Xpath type
                if (!metadata.getClass().equals(type)) {
                    throw new CstlServiceException("The metadata :" + findIdentifier(metadata) + "is not of the same type that the one describe in Xpath expression", INVALID_PARAMETER_VALUE);
                }

                //we remove the type name from the xpath
                xpath = xpath.substring(xpath.indexOf('/') + 1);

                Object parent = metadata;
                while (xpath.indexOf('/') != -1) {
                    
                    //Then we get the next Property name
                    String propertyName = xpath.substring(0, xpath.indexOf('/'));
                    LOGGER.info("propertyName:" + propertyName);

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
                    Method m = Util.getGetterFromName(propertyName, parentClass);
                    if (m == null) {
                        throw new CstlServiceException("There is no getter for the property:" + propertyName + " in the class:" + type.getSimpleName(), INVALID_PARAMETER_VALUE);
                    } else {
                        // we execute the setter
                        if (!(parent instanceof Collection)) {
                            parent = Util.invokeMethod(parent, m);
                        } else {
                            Collection tmp = new ArrayList();
                            for (Object child : (Collection)parent) {
                                tmp.add(Util.invokeMethod(child, m));
                            }
                            parent = tmp;
                        }
                    }

                    xpath = xpath.substring(xpath.indexOf('/') + 1);
                }

                // we try to find a setter for this propertie
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
        LOGGER.info("parameter type:" + parameterType);

        //Special case for language
        if (propertyName.equalsIgnoreCase("language")) {
            parameterType = Locale.class;
            value = new Locale((String) value);
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
                updateObjects(single, propertyName, value);
            }
        } else {
            updateObject(propertyName, parent, value, parameterType);
        }
    }

    private void updateObject(String propertyName, Object parent, Object value, Class parameterType) throws CstlServiceException {
        Method m = Util.getSetterFromName(propertyName, parameterType, parent.getClass());

        // with the geotools implementation we sometimes have to used InternationalString instead of String.
        if (m == null && parameterType.equals(String.class)) {
            m = Util.getSetterFromName(propertyName, InternationalString.class, parent.getClass());
            value = new SimpleInternationalString((String) value);
        }

        if (m == null) {
            throw new CstlServiceException("There is no setter for the property:" + propertyName + " in the class:" + parent.getClass(), INVALID_PARAMETER_VALUE);
        } else {
            // we execute the setter
            if (m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == Collection.class) {
                Collection c = new ArrayList(1);
                c.add(value);
                Util.invokeMethod(m, parent, c);
            } else {
                Util.invokeMethod(m, parent, value);
            }

        }
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
                unmarshaller = unmarshallers.take();
                Object metadata = unmarshaller.unmarshal(metadataFile);
                if (metadata instanceof JAXBElement) {
                    metadata = ((JAXBElement) metadata).getValue();
                }
                return metadata;
            } catch (InterruptedException ex) {
                throw new CstlServiceException("InterruptedException while unnmarshalling the metadataFile : " + identifier + ".xml" + "\n" +
                        "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
            } catch (JAXBException ex) {
                throw new CstlServiceException("The metadataFile : " + identifier + ".xml can not be unmarshalled" + "\n" +
                        "cause: " + ex.getMessage(), INVALID_PARAMETER_VALUE);
            } finally {
                if (unmarshaller != null) {
                    unmarshallers.add(unmarshaller);
                }
            }
        } else {
            throw new CstlServiceException("The metadataFile : " + identifier + ".xml is not present", INVALID_PARAMETER_VALUE);
        }
    }
}
