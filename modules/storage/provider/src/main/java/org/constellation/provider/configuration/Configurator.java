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
import javax.xml.parsers.ParserConfigurationException;

import org.constellation.configuration.ConfigDirectory;

import org.geotoolkit.util.logging.Logging;

import org.xml.sax.SAXException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public interface Configurator {

    public static final Configurator DEFAULT = new DefaultConfigurator();

    ProviderConfig getConfiguration(String serviceName);


    static class DefaultConfigurator implements Configurator{

        private static final Logger LOGGER = Logging.getLogger("org.constellation.provider");

        private DefaultConfigurator(){}

        @Override
        public ProviderConfig getConfiguration(String serviceName) {

            final String fileName = serviceName + ".xml";
            final File configFile = ConfigDirectory.getProviderConfigFile(fileName);

            if(configFile == null || !configFile.exists()){
                //return an empty configuration
                return new ProviderConfig();
            }

            //parse the configuration
            ProviderConfig config = null;
            try {
                config = ProviderConfig.read(configFile);
            } catch (ParserConfigurationException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }

            return config;
        }

    }

}
