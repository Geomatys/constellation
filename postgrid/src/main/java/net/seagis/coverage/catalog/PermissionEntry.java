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
 * The restrictions to be applied on coverage data.
 *
 * @author Guilhem Legal
 * @author Martin Desruisseaux
 * @version $Id: FormatEntry.java 538 2008-04-22 20:05:01Z desruisseaux $
 */
final class PermissionEntry extends Entry {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -8336985120346671214L;

    /**
     * {@code true} if the data can be displayed in a Web Map Server (WMS).
     */
    protected final boolean WMS;

    /**
     * {@code true} if the data can be displayed in a Web Coverage Server (WCS).
     */
    protected final boolean WCS;

    /**
     * If this permission includes the data of an other permission, the other permission.
     * Otherwise {@code null}. This field is a {@link String} on construction and changed
     * to a {@link PermissionEntry} on {@link #postCreateEntry}.
     */
    private Object include;

    /**
     * Creates a new entry.
     */
    public PermissionEntry(final String name, final String include,
            final boolean WCS, final boolean WMS, final String remarks)
    {
        super(name, remarks);
        this.WCS     = WCS;
        this.WMS     = WMS;
        this.include = include;
    }

    /**
     * Updates the {@link #include} field using the given table.
     */
    final void postCreateEntry(final PermissionTable table)
            throws CatalogException, SQLException
    {
        if (include != null) {
            include = table.getEntry((String) include);
        }
    }

    /**
     * Returns {@code true} if the given user is allowed to obtain data through the given service.
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
