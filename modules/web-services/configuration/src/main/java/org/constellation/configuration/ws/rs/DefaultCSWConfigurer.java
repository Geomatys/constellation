/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.configuration.ws.rs;

import java.io.File;

// Constellation dependencies
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.ContainerNotifierImpl;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.constellation.configuration.ws.rs.ConfigurationService.*;

/**
 *
 * @author Guilhem Legal
 */
public class DefaultCSWConfigurer extends AbstractCSWConfigurer {

    public DefaultCSWConfigurer(ContainerNotifierImpl cn) throws ConfigurationException {
        super(cn);
    }
    
    /**
     * Update all the vocabularies skos files and the list of contact.
     */
    @Override
    public AcknowlegementType updateVocabularies() throws CstlServiceException {
        throw new CstlServiceException("This method is not supported by the current implementation.", OPERATION_NOT_SUPPORTED);
    }
    
    /**
     * Update all the contact retrieved from files and the list of contact.
     */
    @Override
    public AcknowlegementType updateContacts() throws CstlServiceException {
        throw new CstlServiceException("This method is not supported by the current implementation.", OPERATION_NOT_SUPPORTED);
    }
    
    @Override
    protected File getConfigurationDirectory() {
        return SERVCE_DIRECTORY.get("CSW");
    }
}
