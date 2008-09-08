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
package net.seagis.coverage.catalog;

import net.seagis.catalog.Entry;
import net.seagis.coverage.web.Service;
import org.geotools.util.Utilities;


/**
 * The restrictions to be applied on coverage data.
 *
 * @author Guilhem Legal
 * @author Martin Desruisseaux
 * @author Cédric Briançon
 * @version $Id$
 */
final class PermissionEntry extends Entry {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -8336985120346671214L;

    /**
     * {@code true} if the data can be displayed in a Web Map Server (WMS).
     */
    private final boolean WMS;

    /**
     * {@code true} if the data can be displayed in a Web Coverage Server (WCS).
     */
    private final boolean WCS;

    /**
     * User for who the permission will apply. By default the user is {@code Anonymous}.
     */
    private final String user;

    /**
     * Creates a new entry.
     */
    public PermissionEntry(final String name, final String user,
            final boolean WCS, final boolean WMS, final String remarks)
    {
        super(name, remarks);
        this.WCS  = WCS;
        this.WMS  = WMS;
        this.user = user;
    }

    /**
     * Returns {@code true} if the user can obtain data of at least one service.
     */
    public boolean isVisible() {
        return WCS | WMS;
    }

    /**
     * Returns {@code true} if the user is allowed to obtain data through the given service.
     */
    public boolean isAccessibleService(final Service service) {
        switch (service) {
            case WMS: return WMS;
            case WCS: return WCS;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final PermissionEntry that = (PermissionEntry) object;
            return this.WMS == that.WMS && this.WCS == that.WCS &&
                   Utilities.equals(this.user, that.user);
        }
        return false;
    }
}
