/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.provider.configuration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;

import org.constellation.configuration.ConfigDirectory;
import org.apache.sis.util.logging.Logging;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public interface Configurator {

    public static final Configurator DEFAULT = new DefaultConfigurator();

    ParameterValueGroup getConfiguration(String serviceName, ParameterDescriptorGroup desc);

    void saveConfiguration(String serviceName, ParameterValueGroup params);

    static final class DefaultConfigurator implements Configurator{

        private static final Logger LOGGER = Logging.getLogger("org.constellation.provider");

        private DefaultConfigurator(){}

        @Override
        public ParameterValueGroup getConfiguration(final String serviceName, final ParameterDescriptorGroup desc) {

            final String fileName = serviceName + ".xml";
            final File configFile = ConfigDirectory.getProviderConfigFile(fileName);

            if(configFile == null || !configFile.exists()){
                //return an empty configuration
                return desc.createValue();
            }

            //parse the configuration
            ParameterValueGroup config = null;
            try {
                config = ProviderParameters.read(configFile, desc);
            } catch (XMLStreamException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }

            return config;
        }

        @Override
        public void saveConfiguration(final String serviceName, final ParameterValueGroup params) {
            final String fileName = serviceName + ".xml";
            final File configFile = ConfigDirectory.getProviderConfigFile(fileName);

            if(configFile.exists()){
                //make a backup
                configFile.delete();
            }

            //write the configuration
            try {
                ProviderParameters.write(configFile, params);
            } catch (XMLStreamException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

    }

}
