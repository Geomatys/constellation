/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.metadata;

// J2SE dependencies
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.List;

// JAXB dependencies
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;

// Constellation dependencies
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.ws.CstlServiceException;

// Geotoolkit dependencies
import org.geotoolkit.csw.xml.GetRecordsRequest;
import org.geotoolkit.ebrim.xml.EBRIMClassesContext;
import org.geotoolkit.xml.MarshallerPool;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class CatalogueHarvester {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.metadata");
    
    /**
     * A Marshaller / unMarshaller pool to send request to another CSW services / to get object from harvested resource.
     */
    protected final MarshallerPool marshallerPool;

    /**
     * A writer for the database
     */
    protected final MetadataWriter metadataWriter;

    public CatalogueHarvester(MetadataWriter metadataWriter) throws MetadataIoException {
        try {
            this.marshallerPool = EBRIMClassesContext.getMarshallerPool();
            this.metadataWriter = metadataWriter;
        } catch (JAXBException ex) {
           throw new MetadataIoException(ex);
        }
    }
    
    /**
     * Harvest another CSW service by getting all this records ans storing it into the database
     * 
     * @param sourceURL The URL of the distant CSW service
     * 
     * @return An array containing: the number of inserted records, the number of updated records and the number of deleted records.
     */
    public abstract int[] harvestCatalogue(String sourceURL) throws MalformedURLException, IOException, CstlServiceException, SQLException;
    
    /**
     * Transfer The request to all the servers specified in distributedServers.
     * 
     * @return
     */
    public abstract DistributedResults transferGetRecordsRequest(GetRecordsRequest request, List<String> distributedServers, int startPosition, int maxRecords);
        
    /**
     * Harvest a single record and storing it into the database
     *
     * @param sourceURL The URL of the resource.
     * @param resourceType The record schema of the document to harvest.
     * 
     * @return An array containing: the number of inserted records, the number of updated records and the number of deleted records.
     */
    public abstract int[] harvestSingle(String sourceURL, String resourceType) throws MalformedURLException, IOException, CstlServiceException, JAXBException;

    
    public abstract void destroy();
}
