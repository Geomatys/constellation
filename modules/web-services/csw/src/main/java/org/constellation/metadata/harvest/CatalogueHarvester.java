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
package org.constellation.metadata.harvest;

// J2SE dependencies
import org.constellation.metadata.io.MetadataIoException;
import java.util.logging.Level;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.List;

// JAXB dependencies
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;

// Constellation dependencies
import javax.xml.bind.Unmarshaller;
import org.constellation.metadata.DistributedResults;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.ws.CstlServiceException;

// Geotoolkit dependencies
import org.geotoolkit.csw.xml.GetRecordsRequest;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.xml.Namespaces;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

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

    /**
     * Build a new Catalogue harvester xith the specified metadataWriter.
     *
     * @param metadataWriter A writer to store matadata in the datasource.
     */
    public CatalogueHarvester(MetadataWriter metadataWriter) {
        this.marshallerPool = EBRIMMarshallerPool.getInstance();
        this.metadataWriter = metadataWriter;
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
    public int[] harvestSingle(String sourceURL, String resourceType) throws MalformedURLException, IOException, CstlServiceException, JAXBException {
        final int[] result = new int[3];
        result[0] = 0;
        result[1] = 0;
        result[2] = 0;
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = marshallerPool.acquireUnmarshaller();

            if (resourceType.equals(Namespaces.GMD) ||
                resourceType.equals(Namespaces.CSW_202) ||
                resourceType.equals("http://www.isotc211.org/2005/gfc")) {

                final InputStream in      = getSingleMetadata(sourceURL);
                final Object harvested    = unmarshaller.unmarshal(in);

                if (harvested == null) {
                    throw new CstlServiceException("The resource can not be parsed.",
                            INVALID_PARAMETER_VALUE, "Source");
                }

                LOGGER.log(Level.INFO, "Object Type of the harvested Resource: {0}", harvested.getClass().getName());

                // ugly patch TODO handle update in mdweb
                try {
                    if (metadataWriter.storeMetadata(harvested)) {
                        result[0] = 1;
                    }
                } catch (IllegalArgumentException e) {
                    result[1] = 1;
                }  catch (MetadataIoException ex) {
                    throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                }
            } else {
                throw new CstlServiceException("unexpected resourceType: " + resourceType, NO_APPLICABLE_CODE);
            }
        } finally {
            if (unmarshaller != null) {
                marshallerPool.release(unmarshaller);
            }
        }
        return result;
    }

    protected abstract InputStream getSingleMetadata(String sourceURL) throws CstlServiceException;
    
    public void destroy() {
        if (metadataWriter != null) {
            metadataWriter.destroy();
        }
    }
}
