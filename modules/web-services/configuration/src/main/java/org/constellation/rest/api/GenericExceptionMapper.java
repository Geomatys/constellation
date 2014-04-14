/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.rest.api;

import com.sun.istack.logging.Logger;
import java.util.logging.Level;
import org.apache.sis.util.NullArgumentException;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigProcessException;
import org.constellation.configuration.NotRunningServiceException;
import org.constellation.configuration.TargetNotFoundException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static org.constellation.utils.RESTfulUtilities.badRequest;
import static org.constellation.utils.RESTfulUtilities.internalError;
import static org.constellation.utils.RESTfulUtilities.notFound;

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
    
    public static Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class);
    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(final Exception exception) {
    
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
