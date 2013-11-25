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

package org.constellation.metadata.utils;

import static org.constellation.metadata.CSWConstants.ACCEPTED_OUTPUT_FORMATS;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.geotoolkit.csw.xml.AbstractCswRequest;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CSWUtils {

     /**
     * Return the request (or default) outputFormat (MIME type) of the response.
     * if the format is not supported it throws a WebService Exception.
     *
     * @param request
     * @return the outputFormat (MIME type) of the response.
     * @throws CstlServiceException
     */
    public static String getOutputFormat(final AbstractCswRequest request) throws CstlServiceException {

        // we initialize the output format of the response
        final String format = request.getOutputFormat();
        if (format != null && ACCEPTED_OUTPUT_FORMATS.contains(format)) {
            return format;
        } else if (format != null && !ACCEPTED_OUTPUT_FORMATS.contains(format)) {
            final StringBuilder supportedFormat = new StringBuilder();
            for (String s: ACCEPTED_OUTPUT_FORMATS) {
                supportedFormat.append(s).append('\n');
            }
            throw new CstlServiceException("The server does not support this output format: " + format + '\n' +
                                             " supported ones are: " + '\n' + supportedFormat.toString(),
                                             INVALID_PARAMETER_VALUE, "outputFormat");
        } else {
            return MimeType.APPLICATION_XML;
        }
    }
}
