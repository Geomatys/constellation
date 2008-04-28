/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008, Geomatys
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
package net.seagis.coverage.catalog;

import java.sql.SQLException;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Entry;
import net.seagis.coverage.web.Service;


/**
 *
 * @author guilhem
 */
final class PermissionEntry extends Entry {

    protected final boolean WMS;

    protected final boolean WCS;

    private Object include;

    public PermissionEntry(final String name, final String include,
            final boolean WCS, final boolean WMS, final String remarks)
    {
        super(name, remarks);
        this.WCS     = WCS;
        this.WMS     = WMS;
        this.include = include;
    }

    /**
     * Update the {@link #include} field using the given table.
     */
    final void postCreateEntry(final PermissionTable table)
            throws CatalogException, SQLException
    {
        if (include != null) {
            include = table.getEntry((String) include);
        }
    }

    /**
     *
     */
    public boolean isAccessibleService(final Service service, final String user) {
        if (user.equalsIgnoreCase(getName())) {
            switch (service) {
                case WMS: return WMS;
                case WCS: return WCS;
            }
        }
        if (include != null) {
            return ((PermissionEntry) include).isAccessibleService(service, user);
        }
        return false;
    }
}
