/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

// Constellation dependencies
import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.sampling.SamplingFeatureTable;
import org.geotoolkit.util.DateRange;



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
