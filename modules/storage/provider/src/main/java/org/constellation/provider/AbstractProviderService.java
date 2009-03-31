/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.provider;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.constellation.provider.configuration.ProviderConfig;
import org.xml.sax.SAXException;

/**
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 */
public abstract class AbstractProviderService<K, V> implements ProviderService<K, V> {

    private File configFile = null;

    private static final Logger LOGGER = Logger.getLogger(AbstractProviderService.class.getName());

    @Override
    public synchronized void init(File file) {
        if (file == null) {
            throw new NullPointerException("Configuration file can not be null");
        }

        if (configFile != null) {
            throw new IllegalStateException("The "+ getName() +" provider service has already been initialize");
        }

        configFile = file;

        ProviderConfig config = null;
        try {
            config = ProviderConfig.read(file);
        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        if (config == null) {
            return;
        }

        init(config);
    }
}
