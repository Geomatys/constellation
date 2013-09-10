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

package org.constellation.sos.configuration;

import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.dto.Service;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.ogc.configuration.OGCConfigurer;

/**
 * {@link org.constellation.configuration.ServiceConfigurer} implementation for SOS service.
 *
 * TODO: implement specific configuration methods
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class SOSConfigurer extends OGCConfigurer {

    /**
     * Create a new {@link SOSConfigurer} instance.
     */
    public SOSConfigurer() {
        super(Specification.SOS, SOSConfiguration.class, "config.xml");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createInstance(final String identifier, final Service metadata, Object configuration) throws ConfigurationException {
        if (configuration == null) {
            final SOSConfiguration baseConfig = new SOSConfiguration(new Automatic(null, new BDD()), new Automatic(null, new BDD()));
            baseConfig.setObservationReaderType(DataSourceType.FILESYSTEM);
            baseConfig.setObservationFilterType(DataSourceType.LUCENE);
            baseConfig.setObservationWriterType(DataSourceType.FILESYSTEM);
            baseConfig.setSMLType(DataSourceType.FILESYSTEM);
            configuration = baseConfig;
        }
        super.createInstance(identifier, metadata, configuration);
    }
}
