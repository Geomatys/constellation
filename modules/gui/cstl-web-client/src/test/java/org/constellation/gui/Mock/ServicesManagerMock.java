/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.gui.Mock;

import org.constellation.dto.Service;
import org.constellation.gui.service.ServicesManager;

import javax.enterprise.inject.Specializes;
import java.util.logging.Level;
import java.util.logging.Logger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import org.constellation.ServiceDef.Specification;

/**
 * ServiceManager mock
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
@Specializes
public class ServicesManagerMock extends ServicesManager{

    private static final Logger LOGGER = Logger.getLogger(ServicesManagerMock.class.getName());

    /**
     * create service with {@link org.constellation.dto.Service} capabilities information
     *
     * @param createdService {@link org.constellation.dto.Service} object which contain capability service information
     * @param service        service type as {@link String}
     * @return <code>true</code> if succeded, <code>false</code> if not succeded
     */
    @Override
    public void createServices(Service createdService, Specification service) {
        LOGGER.log(Level.INFO, createdService.getName());
        LOGGER.log(Level.INFO, createdService.getIdentifier());
        LOGGER.log(Level.INFO, createdService.getDescription());
        LOGGER.log(Level.INFO, createdService.getKeywords().toString());
    }
}
