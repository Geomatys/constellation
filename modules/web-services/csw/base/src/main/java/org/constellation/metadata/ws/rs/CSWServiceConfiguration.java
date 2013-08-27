/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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
package org.constellation.metadata.ws.rs;

import java.io.File;

import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.ServiceConfiguration;
import org.constellation.ws.rs.AbstractServiceConfiguration;

/**
 * CSW {@link org.constellation.ws.rs.ServiceConfiguration} implementation
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class CSWServiceConfiguration extends AbstractServiceConfiguration implements ServiceConfiguration {

    public CSWServiceConfiguration(final Class workerClass) {
        super(workerClass, Automatic.class, "config.xml");
    }

    @Override
    public void basicConfigure(File instanceDirectory, Object capabilitiesConfiguration, String serviceType) throws CstlServiceException {
        configureInstance(instanceDirectory, new Automatic("filesystem", new BDD()), null, serviceType);
    }
}
