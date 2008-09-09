/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2006, Institut de Recherche pour le Développement
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
package org.constellation.observation.fishery.sql;

// J2SE dependencies
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.observation.sampling.SamplingFeatureCollection;

// Sicade dependencies
import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.sampling.SamplingFeatureTable;
import org.geotools.util.DateRange;



/**
 * Table des {@linkplain LongLine ligne de palangre}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Deprecated
public class LongLineTable extends SamplingFeatureTable {
    /**
     * Requête SQL pour obtenir une station à partir de son identifiant.
     */
    private static final ConfigurationKey SELECT = null; // new ConfigurationKey("LongLines:SELECT",
//            "SELECT identifier AS name, identifier, platform, quality, provider, \"startTime\", \"endTime\", x, y\n" +
//            "  FROM \"LongLinesLocations\"\n" +
//            " WHERE name LIKE ?\n"            +
//            " ORDER BY identifier");

    /**
     * Construit une nouvelle connexion vers la table des lignes de palangres.
     */
    public LongLineTable(final Database database) {
        super(database);
    }

    /**
     * Construit une station à partir des informations spécifiées.
     *
     * @todo A mettre à jour lorsque l'on aura implémenté une classe {@code LongLineEntry}.
     */
    protected SamplingFeature createEntry(final int          identifier,
                                  final String       name,
                                  final Point2D      coordinate,
                                  final DateRange    timeRange,
                                  final SamplingFeatureCollection     platform,
                                  final Citation     provider,
                                  final ResultSet    result)
            throws SQLException
    {
        return null;//super.createEntry(identifier, name, coordinate, timeRange, platform, provider, result);
    }
}
