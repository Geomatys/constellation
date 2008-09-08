/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package net.seagis.coverage.web;

import net.seagis.coverage.wms.WMSExceptionCode;
import org.geotools.util.Version;


/**
 * An error occuring in Web Map Service.
 *
 * @version $Id$
 * @author Guilhem Legal
 * @author Martin Desruisseaux
 */
public class WMSWebServiceException extends WebServiceException {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 169942429240747383L;

    /**
     * An OGC Web Service exception report.
     */
    private ServiceExceptionReport exception;

    /**
     * Creates a new exception with the specified details.
     *
     * @param message The message for this exception.
     * @param code    The OGC code that describes the error.
     * @param version The version of the web service that produced the error.
     */
    public WMSWebServiceException(final String message, final WMSExceptionCode code, final Version version) {
        super(message);
        setExceptionReport(message, code, version);
    }

    /**
     * Creates a new exception with the specified cause.
     *
     * @param cause   The cause for this exception.
     * @param code    The OGC code that describes the error.
     * @param version The version of the web service that produced the error.
     */
    public WMSWebServiceException(final Exception cause, final WMSExceptionCode code, final Version version) {
        super(cause);
        setExceptionReport(cause.getLocalizedMessage(), code, version);
    }

    /**
     * Creates a new exception with the specified details.
     *
     * @param message The message for this exception.
     * @param cause   The cause for this exception.
     * @param code    The OGC code that describes the error.
     * @param version The version of the web service that produced the error.
     */
    public WMSWebServiceException(final String message, final Exception cause,
                               final WMSExceptionCode code, final Version version)
    {
        super(message, cause);
        setExceptionReport(message, code, version);
    }

    /**
     * Set the exception. Used by constructors only.
     *
     * @param message The message for this exception.
     * @param code    The OGC code that describes the error.
     * @param version The version of the web service that produced the error.
     */
    private void setExceptionReport(final String message, final WMSExceptionCode code, final Version version) {
        final ServiceExceptionType details = new ServiceExceptionType(message, code);
        exception = new ServiceExceptionReport(version, details);
    }

    /**
     * Returns the OGC Web Service exception report.
     */
    public ServiceExceptionReport getExceptionReport() {
        return exception;
    }

    /**
     * Returns the code of the first exception in the report.
     * or {@code null} if there is no exception report.
     */
    public WMSExceptionCode getExceptionCode() {
        if (exception != null && !exception.getServiceExceptions().isEmpty()) {
            return exception.getServiceExceptions().get(0).getCode();
        } else {
            return null;
        }
    }

    /**
     * Return the service version who launch this exception.
     */
    public String getVersion(){
        if (exception != null ) {
            return exception.getVersion();
        }
        return null;
    }
}
