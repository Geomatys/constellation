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

package org.constellation.rest.api;

import static org.constellation.utils.RESTfulUtilities.badRequest;
import static org.constellation.utils.RESTfulUtilities.internalError;
import static org.constellation.utils.RESTfulUtilities.notFound;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.sis.util.NullArgumentException;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigProcessException;
import org.constellation.configuration.CstlConfigurationRuntimeException;
import org.constellation.configuration.TargetNotFoundException;

/**
 * Custom {@link ExceptionMapper} to transform an {@link Exception} into an
 * appropriate {@link Response} instance.
 *
 * @author Bernard Fabien (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    
    public static final Logger LOGGER = Logging.getLogger(GenericExceptionMapper.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(final Exception exception) {
    
        if(exception instanceof CstlConfigurationRuntimeException) {
            //This exception hold an error code, will be handled by client.
            CstlConfigurationRuntimeException registryRuntimeException = (CstlConfigurationRuntimeException) exception;
            LOGGER.log(Level.WARNING, exception.getMessage() + '(' + registryRuntimeException.getErrorCode() + ')');
            return internalError(AcknowlegementType.failure(exception.getLocalizedMessage(), registryRuntimeException.getErrorCode()));
        }
        
        LOGGER.log(Level.WARNING, exception.getMessage(), exception);
        /*
         * Runtime exception that defines the response to be returned.
         */
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }
        
        /*
         * Others. Simply return the response message with an appropriate HTTP status code.
         */
        if (exception instanceof IllegalArgumentException || exception instanceof NullArgumentException) {
            return badRequest(AcknowlegementType.failure(exception.getLocalizedMessage()));
        }
        if (exception instanceof TargetNotFoundException) {
            return notFound(AcknowlegementType.failure(exception.getLocalizedMessage()));
        }
        if (exception instanceof ConfigProcessException) {
            return internalError(AcknowlegementType.failure(exception.toString())); // use toString() to see message + cause
        }
        return internalError(AcknowlegementType.failure(exception.getLocalizedMessage()));
    }
}
