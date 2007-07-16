/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import java.util.Locale;

import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.crs.DefaultCompoundCRS;
import static org.geotools.referencing.CRS.getTemporalCRS;

import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.CRS;
import net.sicade.observation.sql.Table;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.CatalogException;
import net.sicade.observation.sql.Query;


/**
 * Connection to a source of {@linkplain CoordinateReferenceSystem coordinate reference system}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@UsedBy(GridCoverageTable.class)
public class CoordinateReferenceSystemTable extends Table implements Shareable {
    /**
     * La fabrique de CRS à utiliser. Ne sera créée que la première fois où elle sera nécessaire.
     */
    private CRSFactory factory;

    /**
     * Ensemble des systèmes de références qui ont déjà été créés.
     */
    private final Map<String,CoordinateReferenceSystem> pool = new HashMap<String,CoordinateReferenceSystem>();

    /**
     * Creates a CRS table.
     * 
     * @param database Connection to the database.
     */
    public CoordinateReferenceSystemTable(final Database database) {
        super(new Query(database));
    }

    /**
     * Returns the CRS for the specified name.
     *
     * @param  name CRS identifier.
     * @return The CRS.
     * @throws CatalogException if the requested CRS can not be obtained.
     * @throws SQLException if an error occured while reading the database.
     *
     * @todo Not yet fully implemented.
     */
    public synchronized CoordinateReferenceSystem getEntry(final String name)
            throws CatalogException, SQLException
    {
        /*
         * Special processing for the "xy", "xyz", "xyt" and "xyzt" codes.
         */
        final String authority = "IRD:WGS84(";
        final String asUpper = name.trim().toUpperCase(Locale.ENGLISH);
        if (asUpper.startsWith(authority)) {
            final int last = asUpper.lastIndexOf(')');
            if (last >= 0) {
                final String code = asUpper.substring(authority.length(), last);
                try {
                    return CRS.valueOf(code).getCoordinateReferenceSystem();
                } catch (IllegalArgumentException cause) {
                    // Ignore. We will try the authority below.
                }
            }
        }
        CoordinateReferenceSystem entry = pool.get(name);
        if (entry != null) {
            return entry;
        }
        if (true) {
            // TODO
            throw new CatalogException("Not yet implemented.");
        }
        /*
         * Ajoute une dimension temporelle (s'il n'y en avait pas déjà) et sauvegarde le
         * résultat dans la cache pour réutilisation.
         */
        TemporalCRS temporal = getTemporalCRS(entry);
        if (temporal == null) {
            temporal = getTemporalCRS(CRS.XYT.getCoordinateReferenceSystem());
            entry = new DefaultCompoundCRS(name, entry, temporal);
        }
        if (pool.put(name, entry) != null) {
            throw new AssertionError(name); // Should never happen.
        }
        return entry;
    }
}
