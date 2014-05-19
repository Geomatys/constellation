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
package org.constellation.wmts.ws.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.ws.WebServiceException;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.apache.sis.util.Version;


/**
 * Exception thrown by all SOAP operations when failing.
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SOAPServiceException")
public class SOAPServiceException extends WebServiceException {
    /*
     *
     */
    private static final long serialVersionUID = -2695672978653373664L;

    /**
     * An OGC Web ServiceType exception report
     */
    private final ExceptionReport exception;

    public SOAPServiceException() {
        super();
        exception = null;
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
