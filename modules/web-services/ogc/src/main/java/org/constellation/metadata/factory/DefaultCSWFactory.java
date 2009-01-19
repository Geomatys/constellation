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

package org.constellation.metadata.factory;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.CSWworker;
import org.constellation.metadata.index.AbstractIndexSearcher;
import org.constellation.metadata.index.AbstractIndexer;
import org.constellation.metadata.index.generic.GenericIndexer;
import org.constellation.metadata.index.generic.GenericIndexSearcher;
import org.constellation.metadata.index.mdweb.MDWebIndexer;
import org.constellation.metadata.index.mdweb.MDWebIndexSearcher;
import org.constellation.metadata.io.FileMetadataReader;
import org.constellation.metadata.io.FileMetadataWriter;
import org.constellation.metadata.io.MDWebMetadataReader;
import org.constellation.metadata.io.MDWebMetadataWriter;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.metadata.io.MetadataWriter;
import static org.constellation.generic.database.Automatic.*;
import org.constellation.ws.WebServiceException;

/**
 *
 * @author Guilhem Legal
 */
public class DefaultCSWFactory extends AbstractCSWFactory {

    public DefaultCSWFactory() {
        super(4);
    }
    /**
     * Return a Metadata reader for the specified database type.
     * 
     * @param configuration
     * @param MDConnection
     * @return
     * @throws java.sql.SQLException
     * @throws javax.xml.bind.JAXBException
     */
    public MetadataReader getMetadataReader(Automatic configuration, Connection MDConnection, File dataDirectory, Unmarshaller unmarshaller, File configDir) throws SQLException, JAXBException {
        switch (configuration.getType()) {
            case MDWEB:
                return new MDWebMetadataReader(MDConnection, configDir);
            case FILESYSTEM:
                if (MDConnection != null)
                    MDConnection.close();
                return new FileMetadataReader(dataDirectory, unmarshaller);
            default:
                throw new IllegalArgumentException("Unknow database type: " + configuration.getType());
        }
    }
    
    /**
     * Return a Metadata Writer for the specified database type.
     * 
     * @param configuration
     * @param MDConnection
     * @return
     * @throws java.sql.SQLException
     * @throws javax.xml.bind.JAXBException
     */
    public MetadataWriter getMetadataWriter(int dbType, Connection MDConnection, AbstractIndexer indexer, Marshaller marshaller, File dataDirectory) throws SQLException, JAXBException {
        switch (dbType) {
            case MDWEB:
                return new MDWebMetadataWriter(MDConnection, indexer);
            case FILESYSTEM:
                if (MDConnection != null)
                    MDConnection.close();
                return new FileMetadataWriter(indexer, marshaller, dataDirectory);
            default:
                throw new IllegalArgumentException("Unknow database type: " + dbType);
        }
    }
    
    /**
     * Return a profile (discovery or transactionnal) for the specified databaseType
     * @param dbType The type of the database.
     * 
     * @return DISCOVERY or TRANSACTIONAL 
     */
    public int getProfile(int dbType) {
        switch (dbType) {
            case MDWEB:
                return CSWworker.TRANSACTIONAL;
            case FILESYSTEM:
                return CSWworker.TRANSACTIONAL;
            default:
                throw new IllegalArgumentException("Unknow database type: " + dbType);
        }
    }
    
    /**
     * Return a Lucene index for the specified database type.
     * 
     * @param dbType The type of the database.
     * @param reader A metadataReader (unused for MDWeb database);
     * @param MDConnection A connecton to the database (used only for MDWeb database).
     * @param configDir
     * @return
     * @throws org.constellation.ws.WebServiceException
     */
    public AbstractIndexer getIndexer(int dbType, MetadataReader reader, Connection MDConnection, File configDir, String serviceID) throws WebServiceException {
        switch (dbType) {
            case MDWEB:
                return new MDWebIndexer(MDConnection, configDir, serviceID);
            case FILESYSTEM:
                return new GenericIndexer(reader, configDir, serviceID);
            default:
                throw new IllegalArgumentException("Unknow database type: " + dbType);
        }
    }
    
     @Override
    public AbstractIndexSearcher getIndexSearcher(int dbType, File configDir, String serviceID) throws WebServiceException {
        switch (dbType) {
            case MDWEB:
                return new MDWebIndexSearcher(configDir, serviceID);
            case FILESYSTEM:
                return new GenericIndexSearcher(configDir, serviceID);
            default:
                throw new IllegalArgumentException("Unknow database type: " + dbType);
        }
    }
}
