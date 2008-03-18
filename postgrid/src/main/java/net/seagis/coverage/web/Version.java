/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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


/**
 * A version applicable to a web {@linkplain Service service}.
 *
 * @version $Id$
 * @author Guilhem Legal
 */
@SuppressWarnings("serial") // Not intented to be serialized at this time.
public class Version extends org.geotools.util.Version {
    /**
     * The service.
     */
    private final Service service;

    /**
     * Indicate if this version of the service implement the OWS specification.
     */
    private final boolean isOWS;

    /**
     * Build a new version.
     */
    public Version(final String versionNumber, final boolean isOWS, final Service service) {
       super(versionNumber);
        this.isOWS   = isOWS;
        this.service = service;
    }

    public boolean isOWS() {
        return isOWS;
    }

    public Service getService() {
        return service;
    }
}
