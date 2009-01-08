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

package org.constellation.configuration.ws.rs;

// Constellation dependencies
import java.io.File;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.metadata.index.AbstractIndexer;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.ws.WebServiceException;
import org.constellation.ws.rs.ContainerNotifierImpl;
import static org.constellation.ows.OWSExceptionCode.*;
import static org.constellation.configuration.ws.rs.ConfigurationService.*;

/**
 *
 * @author Guilhem Legal
 */
public class DefaultCSWConfigurer extends AbstractCSWConfigurer {

    public DefaultCSWConfigurer(ContainerNotifierImpl cn) throws ConfigurationException {
        super(cn);
    }
    
    /**
     * Update all the vocabularies skos files and the list of contact.
     */
    public AcknowlegementType updateVocabularies() throws WebServiceException {
        throw new WebServiceException("This method is not supported by the current implementation.", OPERATION_NOT_SUPPORTED);
    }
    
    /**
     * Update all the contact retrieved from files and the list of contact.
     */
    public AcknowlegementType updateContacts() throws WebServiceException {
        throw new WebServiceException("This method is not supported by the current implementation.", OPERATION_NOT_SUPPORTED);
    }
    
    public void destroy() {
        for (AbstractIndexer indexer : indexers.values()) {
            indexer.destroy();
        }
        for (MetadataReader reader: readers) {
            reader.destroy();
        }
    }

    @Override
    protected File getConfigurationDirectory() {
        return serviceDirectory.get("CSW");
    }
}
