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
package org.constellation.ws;


/**
 * Type kind of service.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @deprecated use ServiceDef.Specification
 */
@Deprecated
public enum ServiceType {
    /**
     * Web Map ServiceType.
     */
    WMS,

    /**
     * Web Coverage ServiceType.
     */
    WCS,

    /**
     * Web Feature ServiceType.
     */
    WFS,

    /**
     * Catalog ServiceType-Web.
     */
    CSW,

    /**
     * Open Web service.
     */
    OWS,

    /**
     * Sensor Observation ServiceType.
     */
    SOS,

    /**
     * GetFeatureInfo WMS request, part of Web Map ServiceType.
     */
    GETINFO,

    /**
     * Any other ServiceType.
     */
    OTHER
}
