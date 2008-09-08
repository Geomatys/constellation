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
package net.seagis.coverage.catalog;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.geotools.coverage.grid.GridCoverage2D;

import net.seagis.catalog.Database;
import net.seagis.catalog.SingletonTable;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.IllegalRecordException;


/**
 * Connection to a table of image {@linkplain Format}. Those format are used for reading
 * {@link GridCoverage2D}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class FormatTable extends SingletonTable<Format> {
    /**
     * Connexion vers la table des bandes.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private SampleDimensionTable bands;

    /**
     * Creates a format table.
     *
     * @param database Connection to the database.
     */
    public FormatTable(final Database database) {
        this(new FormatQuery(database));
    }

    /**
     * Constructs a new {@code FormatTable} from the specified query.
     */
    private FormatTable(final FormatQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }

    /**
     * Creates a format from the current row in the specified result set.
     *
     * @param  results The result set to read.
     * @return The entry for current row in the specified result set.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    protected Format createEntry(final ResultSet results) throws CatalogException, SQLException {
        final FormatQuery query = (FormatQuery) super.query;
        final String name     = results.getString(indexOf(query.name));
        final String format   = results.getString(indexOf(query.format));
        final String encoding = results.getString(indexOf(query.encoding));
        if (bands == null) {
            bands = getDatabase().getTable(SampleDimensionTable.class);
        }
        final boolean geophysics;
        final String type = String.valueOf(encoding).toLowerCase();
        if (type.equals("geophysics")) {
            geophysics = true;
        } else if (type.equals("packed") || type.equals("rendered") || type.equals("native")) {
            geophysics = false;
        } else {
            throw new IllegalRecordException("Type d'image inconnu: " + encoding,
                        this, results, indexOf(query.encoding), name);
        }
        return new FormatEntry(name, format, geophysics, bands.getSampleDimensions(name));
    }
}
