/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
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
package net.sicade.observation.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.opengis.geometry.Envelope;
import static net.sicade.observation.sql.SpatialColumn.Box.*;


/**
 * A parameter with a spatial type, like {@code BOX3D}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Deprecated
public class SpatialParameter extends Parameter {
    /**
     * {@code true} if the database is spatial enabled (e.g. PostgreSQL with a PostGIS extension).
     */
    final boolean spatialEnabled;

    /**
     * Creates a new parameter for the specified query.
     *
     * @param  query  The query for which the parameter is created.
     * @param  column The column on which the parameter is applied.
     * @param  types  The query types for which the parameter applies.
     * @throws SQLException if an error occured while reading the database.
     */
    public SpatialParameter(final Query query, final Column column, final QueryType... types)
            throws SQLException
    {
        super(query, column, types);
        spatialEnabled = (query.database != null) && query.database.isSpatialEnabled();
    }

    /**
     * A parameter for the {@code BOX3D} type.
     */
    @Deprecated
    public static class Box extends SpatialParameter {
        /**
         * Creates a new parameter for the specified query.
         *
         * @param query  The query for which the parameter is created.
         * @param column The column on which the parameter is applied.
         * @param types  The query types for which the parameter applies.
         * @throws SQLException if an error occured while reading the database.
         */
        public Box(final Query query, final Column column, final QueryType... types)
                throws SQLException
        {
            super(query, column, types);
        }

        /**
         * Set the parameter to an envelope. If the {@linkplain Database database} is spatial
         * enabled, then this method sets a single {@code BOX} or {@code BOX3D} parameter.
         * Otherwise this method sets up to 6 consecutive parameters with (<var>xmin</var>,
         * <var>xmax</var>, <var>ymin</var>, <var>ymax</var>, <var>zmin</var>, <var>zmax</var>)
         * values.
         *
         * @param  statement The statement in which to set the parameter.
         * @param  type      The query type.
         * @param  envelope  The three-dimensional envelope to set as parameter.
         * @throws SQLException if the parameters can not be set.
         */
        public void setEnvelope(final PreparedStatement statement, final QueryType type,
                                final Envelope envelope) throws SQLException
        {
            final int index = indexOf(type);
            if (spatialEnabled) {
                statement.setString(index, format(envelope));
            } else {
                setEnvelope(statement, index, envelope);
            }
        }

        /**
         * Set the parameter to an envelope. This method sets up to 6 consecutive parameters with
         * (<var>xmin</var>, <var>xmax</var>, <var>ymin</var>, <var>ymax</var>, <var>zmin</var>,
         * <var>zmax</var>) values.
         *
         * @param  statement The statement in which to set the parameter.
         * @param  index     Index of the first parameter to set.
         * @param  envelope  The three-dimensional envelope to set as parameter.
         * @throws SQLException if the parameters can not be set.
         */
        public static void setEnvelope(final PreparedStatement statement, int index,
                                       final Envelope envelope) throws SQLException
        {
            final int dimension = Math.min(envelope.getDimension(), DIMENSION);
            for (int i=0; i<dimension; i++) {
                statement.setDouble(index++, envelope.getMinimum(i));
                statement.setDouble(index++, envelope.getMaximum(i));
            }
        }
    }
}
