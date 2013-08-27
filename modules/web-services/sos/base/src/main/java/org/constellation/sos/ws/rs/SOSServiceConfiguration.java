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
package org.constellation.sos.ws.rs;

import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.ServiceConfiguration;
import org.constellation.ws.rs.AbstractServiceConfiguration;

import java.io.File;

/**
 * SOS {@link org.constellation.ws.rs.ServiceConfiguration} implementation
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class SOSServiceConfiguration extends AbstractServiceConfiguration implements ServiceConfiguration {

    public SOSServiceConfiguration(final Class workerClass) {
        super(workerClass, SOSConfiguration.class, "config.xml");
    }

    @Override
    public void basicConfigure(File instanceDirectory, Object capabilitiesConfiguration, String serviceType) throws CstlServiceException {
        final SOSConfiguration baseConfig = new SOSConfiguration(new Automatic(null, new BDD()), new Automatic(null, new BDD()));
        baseConfig.setObservationReaderType(DataSourceType.FILESYSTEM);
        baseConfig.setObservationFilterType(DataSourceType.LUCENE);
        baseConfig.setObservationWriterType(DataSourceType.FILESYSTEM);
        baseConfig.setSMLType(DataSourceType.FILESYSTEM);
        configureInstance(instanceDirectory, baseConfig, capabilitiesConfiguration, serviceType);
    }
}
