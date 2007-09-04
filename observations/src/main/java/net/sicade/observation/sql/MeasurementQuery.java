/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la d�cision en environnement
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

package net.sicade.observation.sql;

import net.sicade.catalog.Database;

/**
 * The query to execute for a {@link MeasurementTable}.
 *
 * @author Guilhem Legal
 */
public class MeasurementQuery extends ObservationQuery{
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public MeasurementQuery(final Database database) {
        super(database);
    }
    
}
