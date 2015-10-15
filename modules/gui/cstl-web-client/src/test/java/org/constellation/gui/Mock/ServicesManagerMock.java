/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.gui.Mock;

import org.constellation.ServiceDef.Specification;
import org.constellation.dto.Service;
import org.constellation.gui.service.ServicesManager;

import javax.enterprise.inject.Specializes;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;

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

    private static final Logger LOGGER = Logging.getLogger("org.constellation.gui.Mock");

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
