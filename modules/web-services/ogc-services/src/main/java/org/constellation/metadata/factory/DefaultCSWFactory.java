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
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.CSWworker;
import org.constellation.metadata.index.GenericIndex;
import org.constellation.metadata.index.IndexLucene;
import org.constellation.metadata.index.MDWebIndex;
import org.constellation.metadata.io.CDIReader;
import org.constellation.metadata.io.CSRReader;
import org.constellation.metadata.io.EDMEDReader;
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
    
    /**
     * Return a Metadata reader for the specified database type.
     * 
     * @param configuration
     * @param MDConnection
     * @return
     * @throws java.sql.SQLException
     * @throws javax.xml.bind.JAXBException
     */
    public MetadataReader getMetadataReader(Automatic configuration, Connection MDConnection) throws SQLException, JAXBException {
        switch (configuration.getType()) {
            case CDI:
                return new CDIReader(configuration, MDConnection);
            case CSR:
                return new CSRReader(configuration, MDConnection);
            case EDMED:
                return new EDMEDReader(configuration, MDConnection);
            case MDWEB:
                return new MDWebMetadataReader(MDConnection);
            case FILESYSTEM:
                return new FileMetadataReader();
            default:
                throw new IllegalArgumentException("Unknow database type");
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
    public MetadataWriter getMetadataWriter(int dbType, Connection MDConnection, IndexLucene index) throws SQLException, JAXBException {
        switch (dbType) {
            case CDI:
                return null;
            case CSR:
                return null;
            case EDMED:
                return null;
            case MDWEB:
                return new MDWebMetadataWriter(MDConnection, index);
            case FILESYSTEM:
                return new FileMetadataWriter(index);
            default:
                throw new IllegalArgumentException("Unknow database type");
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
            case CDI:
                return CSWworker.DISCOVERY;
            case CSR:
                return CSWworker.DISCOVERY;
            case EDMED:
                return CSWworker.DISCOVERY;
            case MDWEB:
                return CSWworker.TRANSACTIONAL;
            case FILESYSTEM:
                return CSWworker.TRANSACTIONAL;
            default:
                throw new IllegalArgumentException("Unknow database type");
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
    public IndexLucene getIndex(int dbType, MetadataReader reader, Connection MDConnection, File configDir) throws WebServiceException {
        switch (dbType) {
            case CDI:
                return new GenericIndex(reader, configDir);
            case CSR:
                return new GenericIndex(reader, configDir);
            case EDMED:
                return new GenericIndex(reader, configDir);
            case MDWEB:
                return new MDWebIndex(MDConnection, configDir);
            case FILESYSTEM:
                return new GenericIndex(reader, configDir);
            default:
                throw new IllegalArgumentException("Unknow database type");
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
    public IndexLucene getIndex(int dbType, MetadataReader reader, Connection MDConnection) throws WebServiceException {
        switch (dbType) {
            case CDI:
                return new GenericIndex(reader);
            case CSR:
                return new GenericIndex(reader);
            case EDMED:
                return new GenericIndex(reader);
            case MDWEB:
                return new MDWebIndex(MDConnection);
            case FILESYSTEM:
                return new GenericIndex(reader);
            default:
                throw new IllegalArgumentException("Unknow database type");
        }
    }
}
