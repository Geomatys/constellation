/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

import org.geotoolkit.util.Version;


/**
 * A version applicable to a web {@linkplain ServiceType service}.
 *
 * @version $Id$
 * @author Guilhem Legal
 */
@SuppressWarnings("serial") // Not intented to be serialized at this time.
public class ServiceVersion extends Version {
    /**
     * The first WCS version to be considered as OWS.
     */
    private static final ServiceVersion THRESHOLD = new ServiceVersion(ServiceType.WCS, "1.1");

    /**
     * The service.
     */
    private final ServiceType service;

    /**
     * Builds a new version for the given service.
     */
    public ServiceVersion(final ServiceType service, final String version) {
        super(version);
        this.service = service;
    }

    /**
     * Returns the service.
     */
    public ServiceType getService() {
        return service;
    }

    /**
     * Returns {@code true} if the service is a OWS service.
     */
    public boolean isOWS() {
        switch (service) {
            case OWS: return true;
            case WCS: return compareTo(THRESHOLD) >= 0;
            default:  return false;
        }
    }
}
