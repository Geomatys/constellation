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
package org.constellation.wps.ws.rs;

import java.io.File;

import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.Processes;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.ServiceConfiguration;
import org.constellation.ws.rs.AbstractServiceConfiguration;

/**
 * WPS {@link org.constellation.ws.rs.ServiceConfiguration} implementation
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class WPSServiceConfiguration extends AbstractServiceConfiguration implements ServiceConfiguration {

    public WPSServiceConfiguration(final Class workerClass) {
        super(workerClass, ProcessContext.class, "processContext.xml");
    }

    @Override
    public void basicConfigure(File instanceDirectory, Object capabilitiesConfiguration, String serviceType) throws CstlServiceException {
        configureInstance(instanceDirectory, new ProcessContext(new Processes(true)), capabilitiesConfiguration, serviceType);
    }
}
