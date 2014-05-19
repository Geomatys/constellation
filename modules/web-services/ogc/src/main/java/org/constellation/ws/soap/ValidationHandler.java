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
package org.constellation.ws.soap;

import com.sun.xml.ws.developer.ValidationErrorHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
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
