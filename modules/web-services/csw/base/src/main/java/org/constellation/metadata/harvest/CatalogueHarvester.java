/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.metadata.harvest;

// J2SE dependencies
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.xml.Namespaces;
import org.constellation.metadata.DistributedResults;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.csw.xml.GetRecordsRequest;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.w3c.dom.Node;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class CatalogueHarvester {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.metadata");
    
    /**
     * A Marshaller / unMarshaller pool to send request to another CSW services / to get object from harvested resource.
     */
    protected final MarshallerPool marshallerPool;

    /**
     * A writer for the database
     */
    protected final MetadataWriter metadataWriter;

    /**
     * Build a new Catalog harvester with the specified metadataWriter.
     *
     * @param metadataWriter A writer to store metadata in the dataSource.
     */
    public CatalogueHarvester(final MetadataWriter metadataWriter) {
        this.marshallerPool = CSWMarshallerPool.getInstanceCswOnly();
        this.metadataWriter = metadataWriter;
    }
    
    /**
     * Harvest another CSW service by getting all this records and storing it into the database
     * 
     * @param sourceURL The URL of the distant CSW service
     * 
     * @return An array containing: the number of inserted records, the number of updated records and the number of deleted records.
     */
    public abstract int[] harvestCatalogue(final String sourceURL) throws MalformedURLException, IOException, CstlServiceException, SQLException;
    
    /**
     * Transfer The request to all the servers specified in distributedServers.
     * 
     * @return
     */
    public abstract DistributedResults transferGetRecordsRequest(final GetRecordsRequest request, final List<String> distributedServers,
            final int startPosition, final int maxRecords);
        
    /**
     * Harvest a single record and storing it into the database
     *
     * @param sourceURL The URL of the resource.
     * @param resourceType The record schema of the document to harvest.
     * 
     * @return An array containing: the number of inserted records, the number of updated records and the number of deleted records.
     */
    public int[] harvestSingle(final String sourceURL, final String resourceType) throws MalformedURLException, IOException, CstlServiceException, JAXBException {
        final int[] result = new int[3];
        result[0] = 0;
        result[1] = 0;
        result[2] = 0;
        
        final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        if (Namespaces.GMD.equals(resourceType) ||
            Namespaces.CSW.equals(resourceType) ||
           "http://www.isotc211.org/2005/gfc".equals(resourceType)) {

            final InputStream in      = getSingleMetadata(sourceURL);
            final Object harvestedObj = unmarshaller.unmarshal(in);
            marshallerPool.recycle(unmarshaller);

            if (harvestedObj == null) {
                throw new CstlServiceException("The resource can not be parsed.",
                        INVALID_PARAMETER_VALUE, "Source");
            } else if (!(harvestedObj instanceof Node))  {
                throw new CstlServiceException("object has been unmarshalled.");
            }
            final Node harvested = (Node) harvestedObj;

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
        return result;
    }

    protected abstract InputStream getSingleMetadata(final String sourceURL) throws CstlServiceException;
    
    public void destroy() {
        if (metadataWriter != null) {
            metadataWriter.destroy();
        }
    }
}
