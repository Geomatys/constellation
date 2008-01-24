/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.web;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import net.seagis.ogc.ServiceExceptionReport;
import net.seagis.ogc.ServiceExceptionType;
import org.geotools.util.Version;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.wms.WMSExceptionCode;


/**
 * Reports a failure in {@link WebService}.
 *
 * @author Guihlem Legal
 * @author Martin Desruisseaux
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "WebServiceException", namespace="http://wms.geomatys.fr/")
public class WebServiceException extends CatalogException {
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
    public WebServiceException(final String message, final WMSExceptionCode code, final Version version) {
        super(message);
        setServiceExceptionReport(message, code, version);
    }

    /**
     * Creates a new exception with the specified cause.
     *
     * @param cause   The cause for this exception.
     * @param code    The OGC code that describes the error.
     * @param version The version of the web service that produced the error.
     */
    public WebServiceException(final Exception cause, final WMSExceptionCode code, final Version version) {
        super(cause);
        setServiceExceptionReport(cause.getLocalizedMessage(), code, version);
    }

    /**
     * Creates a new exception with the specified details.
     *
     * @param message The message for this exception.
     * @param cause   The cause for this exception.
     * @param code    The OGC code that describes the error.
     * @param version The version of the web service that produced the error.
     */
    public WebServiceException(final String message, final Exception cause,
                               final WMSExceptionCode code, final Version version)
    {
        super(message, cause);
        setServiceExceptionReport(message, code, version);
    }

    /**
     * Set the exception. Used by constructors only.
     *
     * @param message The message for this exception.
     * @param code    The OGC code that describes the error.
     * @param version The version of the web service that produced the error.
     */
    private void setServiceExceptionReport(final String message, final WMSExceptionCode code, final Version version) {
        final ServiceExceptionType details = new ServiceExceptionType(message, code);
        exception = new ServiceExceptionReport(version, details);
    }

    /**
     * Returns the OGC Web Service exception report.
     */
    public ServiceExceptionReport getServiceExceptionReport() {
        return exception;
    }
    
    /**
     * Returns the code of the first exception in the report.
     * or {@code null} if there is no exception report.
     */
    public WMSExceptionCode getExceptionCode() {
        if (exception != null && exception.getServiceExceptions().size() > 0) {
            return exception.getServiceExceptions().get(0).getCode();
        } else {
            return null;
        }
    }
}
