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

package org.constellation.ws.rs;

import org.constellation.ServiceDef;
import org.geotoolkit.image.jai.Registry;

/**
 * A Super class for WMS and WCS Webservice.
 * The point is to remove the hard-coded dependency to JAI.
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.5
 */
public abstract class GridWebService extends OGCWebService {

    static {
        Registry.setDefaultCodecPreferences();
    }

    public GridWebService(final ServiceDef... supportedVersions) {
        super(supportedVersions);
    }
}
