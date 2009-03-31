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
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.cat.csw.v202.RecordPropertyType;
import org.constellation.generic.database.Automatic;
import org.constellation.lucene.index.AbstractIndexer;
import org.constellation.metadata.CSWClassesContext;
import org.constellation.ws.CstlServiceException;
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
            // TODO
        }
        return false;
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
