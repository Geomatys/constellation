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
package org.constellation.coverage.web;


/**
 * Type kind of service provided by {@link WebServiceWorker}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum Service {
    /**
     * Web Map Service.
     */
    WMS,

    /**
     * Web Coverage Service.
     */
    WCS,

    /**
     * Catalog Service-Web.
     */
    CSW,

    /**
     * Observation Web service.
     */
    OWS,

    /**
     * Sensor Observation Service.
     */
    SOS
}
