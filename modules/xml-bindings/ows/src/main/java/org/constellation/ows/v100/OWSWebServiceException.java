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
package org.constellation.ows.v100;

import org.constellation.coverage.web.ServiceVersion;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.ows.OWSExceptionCode;

/**
 *
 * @author Guilhem Legal
 */
public class OWSWebServiceException extends WebServiceException {
    
    /**
     * An OWS Web Service exception report
     */
    private ExceptionReport exception;
         
    /**
     * Build a new OWS 1.0.0 web service exception.
     * 
     * @param message A text explaining why this exception has been launched.
     * @param code    A OWS error code.
     * @param locator The parameter or location which provocate the exception.
     * @param v       The version.
     */
    public OWSWebServiceException(String message, OWSExceptionCode code, String locator, ServiceVersion v) {
        super(message, code, v);
        this.exception = new ExceptionReport(message, code.name(), locator, v);
    }
    
    /**
     * Build an OWS v1.0.0 web service exception from an OWS v 1.1.0 exception
     */
    public OWSWebServiceException(org.constellation.ows.v110.OWSWebServiceException exc, ServiceVersion v) {
        super(exc, exc.getExceptionCode(), v);
        this.exception = new ExceptionReport(exc.getMessage(), exc.getExceptionCode().name(), exc.getExceptionReport().getFirstLocator(), v);
    }
    
    public ExceptionReport getExceptionReport() {
        return exception;
    }
    
    /**
     * Returns the code of the first exception in the report.
     * or {@code null} if there is no exception report.
     */
    public OWSExceptionCode getExceptionCode() {
        if (exception != null && !exception.getException().isEmpty()) {
            String code = exception.getException().get(0).getExceptionCode();
            return OWSExceptionCode.valueOf(code);
        } else {
            return null;
        }
    }
    
    /**
     * Returns the locator of the first exception in the report.
     * or an empty String if there is no exception report.
     */
    public String getLocator() {
        String locator = "";
        if (exception != null && !exception.getException().isEmpty() &&  exception.getException().get(0).getLocator() != null) {
            locator = exception.getException().get(0).getLocator();
        } 
        return locator;
    }
}
