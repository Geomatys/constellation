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
public abstract class WebServiceException extends CatalogException {
    
    public WebServiceException() {
        super();
    }
    
    public WebServiceException(String message) {
        super(message);
    }
    
    public WebServiceException(Exception cause) {
        super(cause);
    }
    
    public WebServiceException(String message, Exception cause) {
        super(message, cause);
    }

    public abstract WMSExceptionCode getExceptionCode();
}
