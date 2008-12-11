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
import org.constellation.metadata.index.IndexLucene;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.ws.WebServiceException;
import org.geotools.factory.AbstractFactory;

/**
 *
 * @author Guilhem Legal
 */
public abstract class AbstractCSWFactory extends AbstractFactory {

    public AbstractCSWFactory(int priority) {
        super(priority);
    }
    
    public abstract MetadataReader getMetadataReader(Automatic configuration, Connection MDConnection, File dataDir, Unmarshaller unmarshaller) throws SQLException, JAXBException;

    public abstract MetadataWriter getMetadataWriter(int dbType, Connection MDConnection, IndexLucene index, Marshaller marshaller, File dataDirectory) throws SQLException, JAXBException;
    
    public abstract int getProfile(int dbType);
    
    public abstract IndexLucene getIndex(int dbType, MetadataReader reader, Connection MDConnection, File configDir) throws WebServiceException;
    
    public abstract IndexLucene getIndex(int dbType, MetadataReader reader, Connection MDConnection) throws WebServiceException;
}
