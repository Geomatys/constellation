/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2007 - 2010, Geomatys
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
package org.constellation.admin;

import java.io.StringReader;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.constellation.ServiceDef;
import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.jooq.tables.pojos.Service;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.ICSWConfigurer;
import org.constellation.ws.Refreshable;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.ws.WSEngine;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class InternalCSWSynchronizer {
    /**
     * Injected service repository.
     */
    @Inject
    protected ServiceRepository serviceRepository;
    
    protected void updateInternalCSWIndex(final String metadataID, final int domainId, final boolean update) throws ConfigurationException {
        try {
            final List<Service> services = serviceRepository.findByDomainAndType(domainId, "csw");
            for (Service service : services) {
            
                final Unmarshaller um = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                // read config to determine CSW type
                final Automatic conf = (Automatic) um.unmarshal(new StringReader(service.getConfig()));
                if (conf.getFormat().equals("internal")) {
                    final ICSWConfigurer configurer = (ICSWConfigurer) ServiceConfigurer.newInstance(ServiceDef.Specification.CSW);
                    configurer.removeFromIndex(service.getIdentifier(), metadataID);
                    if (update) {
                        configurer.addToIndex(service.getIdentifier(), metadataID);
                    }
                    final Refreshable worker = (Refreshable) WSEngine.getInstance("CSW", service.getIdentifier());
                    worker.refresh();
                }
            }
        } catch (JAXBException | CstlServiceException ex) {
            throw new ConfigurationException("Error while updating internal CSW index", ex);
        }
    }
    
}
