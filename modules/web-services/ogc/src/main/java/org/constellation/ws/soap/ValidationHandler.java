/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.ws.soap;

import com.sun.xml.ws.developer.ValidationErrorHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.util.logging.Logging;
import org.xml.sax.SAXParseException;

/**
 * use with @SchemaValidation(handler = ValidationHandler.class) annotations a JAX-WS service classes.
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class ValidationHandler extends ValidationErrorHandler {

    private static final Logger LOGGER = Logging.getLogger(ValidationHandler.class);
    
    public static final String ERROR       = "SchemaValidationError";
  
    @Override
    public void warning(SAXParseException exception) {
        LOGGER.log(Level.FINER, ERROR, exception);
    }

    @Override
    public void error(SAXParseException exception) {
        packet.invocationProperties.put(ERROR, exception);
    }

    @Override
    public void fatalError(SAXParseException exception) {
        packet.invocationProperties.put(ERROR, exception);
    }

}
