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
package org.constellation.metadata.ws.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.ws.WebServiceException;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.util.Version;


/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SOAPServiceException")
public class SOAPServiceException extends WebServiceException {
    private static final long serialVersionUID = 7201924662570181781L;
    
    /**
     * An OGC Web ServiceType exception report
     */
    private ExceptionReport exception;
    
    SOAPServiceException() {
        super();
    }
            
    public SOAPServiceException(String message, String code, String v) {
        super(message);
        this.exception = new ExceptionReport(message, code, null,  v);
        
        this.setStackTrace(new StackTraceElement[0]);
    }

    public SOAPServiceException(String message, String code, Version v) {
        super(message);
        String version = null;
        if (v != null) {
            version = v.toString();
        }
        this.exception = new ExceptionReport(message, code, null,  version);

        this.setStackTrace(new StackTraceElement[0]);
    }
    
    public ExceptionReport getException() {
        return exception;
    }
}
