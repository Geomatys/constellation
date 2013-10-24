/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process;

import java.io.IOException;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.LayerContext;
import org.constellation.process.service.DeleteServiceTest;
import org.constellation.wfs.ws.DefaultWFSWorker;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class DeleteWFSServiceTest extends DeleteServiceTest {

    public DeleteWFSServiceTest() {
        super("WFS", DefaultWFSWorker.class);
    }

    /** {@inheritDoc} */
    @Override
    protected void createInstance(final String identifier) {
        try {
            final LayerContext configuration = new LayerContext();
            ConfigurationEngine.createConfiguration(serviceName, identifier, "layerContext.xml", configuration, null);
        } catch (JAXBException | IOException ex) {
            LOGGER.log(Level.SEVERE, "Error while creating instance", ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean checkInstanceExist(final String identifier) {
        return ConfigurationEngine.getServiceConfigurationIds(serviceName).contains(identifier);
    }

}
