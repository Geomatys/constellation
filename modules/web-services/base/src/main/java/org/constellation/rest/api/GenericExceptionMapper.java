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

import org.apache.sis.util.NullArgumentException;
import org.constellation.configuration.NoSuchInstanceException;
import org.constellation.configuration.NotRunningServiceException;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(final Exception exception) {
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }
        if (exception instanceof IllegalArgumentException || exception instanceof NullArgumentException) {
            return badRequest(exception.getLocalizedMessage());
        }
        if (exception instanceof NotRunningServiceException || exception instanceof NoSuchInstanceException) {
            return notFound(exception.getLocalizedMessage());
        }
        return internalError(exception.getLocalizedMessage());
    }
}
