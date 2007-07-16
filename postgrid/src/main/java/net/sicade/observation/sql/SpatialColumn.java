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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.StringTokenizer;
import org.opengis.geometry.Envelope;
import org.geotools.geometry.GeneralEnvelope;


/**
 * A column with a spatial type, like {@code BOX3D}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Deprecated
public class SpatialColumn extends Column {
    /**
     * Enable work around what seems to be PostGIS limitations.
     */
    public static final boolean WORKAROUND_POSTGIS = true;

    /**
     * The number of dimensions for spatial column.
     * Our database uses only 3 dimensional boxes or points.
     */
    static final int DIMENSION = 3;

    /**
     * {@code true} if the database is spatial enabled (e.g. PostgreSQL with a PostGIS extension).
     */
    final boolean spatialEnabled;

    /**
     * Creates a column from the specified table with the specified name but no alias.
     *
     * @param query The query for which the column is created.
     * @param table The table name in which this column appears.
     * @param name  The column name.
     */
    public SpatialColumn(final Query query, final String table, final String name) {
        super(query, table, name);
        spatialEnabled = (query.database != null) && query.database.isSpatialEnabled();
    }

    /**
     * Creates a column from the specified table with the specified name but no alias.
     *
     * @param query The query for which the column is created.
     * @param table The table name in which this column appears.
     * @param name  The column name.
     * @param types The query for which to include this column, or {@code null} for all.
     */
    public SpatialColumn(final Query query, final String table, final String name, final QueryType... types) {
        super(query, table, name, types);
        spatialEnabled = (query.database != null) && query.database.isSpatialEnabled();
    }

    /**
     * A column for the {@code BOX3D} type.
     */
    @Deprecated
    public static class Box extends SpatialColumn {
        /**
         * Creates a column from the specified table with the specified name but no alias.
         *
         * @param query The query for which the column is created.
         * @param table The table name in which this column appears.
         * @param name  The column name.
         */
        public Box(final Query query, final String table, final String name) {
            super(query, table, name);
        }

        /**
         * Creates a column from the specified table with the specified name but no alias.
         *
         * @param query The query for which the column is created.
         * @param table The table name in which this column appears.
         * @param name  The column name.
         * @param types The query for which to include this column, or {@code null} for all.
         */
        public Box(final Query query, final String table, final String name, final QueryType... types) {
            super(query, table, name, types);
        }

        /**
         * Creates a spatial envelope from the current row in the specified result sets.
         * If the {@linkplain Database database} is spatial enabled, then this method
         * expects a {@code BOX} or {@code BOX3D} element in a single column. Otherwise
         * this method expects 6 consecutive columns with (<var>xmin</var>, <var>xmax</var>,
         * <var>ymin</var>, <var>ymax</var>, <var>zmin</var>, <var>zmax</var>) values.
         *
         * @param  results The result set in which to fetch the coordinates.
         * @param  type The query type.
         * @return Envelope The envelope created from the result set.
         * @throws SQLException if this column does not contain the expected data.
         */
        public Envelope getEnvelope(final ResultSet results, final QueryType type) throws SQLException {
            final int columnIndex = indexOf(type);
            if (spatialEnabled) {
                return parse(results.getString(columnIndex));
            } else {
                return getEnvelope(results, columnIndex);
            }
        }

        /**
         * Creates a spatial envelope from the current row in the specified result sets.
         * This method expects 6 consecutive columns with (<var>xmin</var>, <var>xmax</var>,
         * <var>ymin</var>, <var>ymax</var>, <var>zmin</var>, <var>zmax</var>) values.
         *
         * @param  results The result set in which to fetch the coordinates.
         * @param  columnIndex the first column to read.
         * @return Envelope The envelope created from the result set.
         * @throws SQLException if the columns do not contain the expected data.
         */
        public static Envelope getEnvelope(final ResultSet results, int columnIndex) throws SQLException {
            final GeneralEnvelope envelope = new GeneralEnvelope(DIMENSION);
            for (int i=0; i<DIMENSION; i++) {
                envelope.setRange(i, getDouble(results, columnIndex++), getDouble(results, columnIndex++));
            }
            return envelope;
        }

        /**
         * Returns the value of the specified column as a double, or NaN if null.
         */
        private static double getDouble(final ResultSet results, final int column) throws SQLException {
            final double v = results.getDouble(column);
            return results.wasNull() ? Double.NaN : v;
        }

        /**
         * Replace infinitie values by pad values.
         *
         * @todo Current implementation use arbitrary values for infinities.
         *       We need to do something better.
         */
        static double toPadValue(double value) {
            if (value == Double.NEGATIVE_INFINITY) {
                value = -1000;
            } else if (value == Double.POSITIVE_INFINITY) {
                value = +1000;
            }
            return value;
        }

        /**
         * Formats a {@code BOX3D} element.
         */
        static String format(final Envelope envelope) {
            final int dimension = envelope.getDimension();
            final StringBuilder buffer = new StringBuilder("BOX").append(dimension).append('D');
            if (WORKAROUND_POSTGIS) {
                // I have been unable to make PostGIS understand BOX3D.
                buffer.setLength(0);
                buffer.append("MULTIPOINT");
            }
            char separator = '(';
            for (int i=0; i<dimension; i++) {
                buffer.append(separator).append(toPadValue(envelope.getMinimum(i)));
                separator = ' ';
            }
            separator = ',';
            for (int i=0; i<dimension; i++) {
                buffer.append(separator).append(toPadValue(envelope.getMaximum(i)));
                separator = ' ';
            }
            return buffer.append(')').toString();
        }

        /**
         * Parses a {@code BOX3D} element.
         */
        static Envelope parse(String bbox) throws SQLException {
            bbox = bbox.trim().toUpperCase(Locale.ENGLISH);
            final int length = bbox.length();
            final int offset = bbox.indexOf('(');
            if (offset>=0 && bbox.charAt(length-1)==')') {
                final int dimension = getDimension(bbox.substring(0, offset).trim());
                if (dimension != 0) {
                    int i = 0;
                    final double[] coordinates = new double[dimension * 2];
                    Arrays.fill(coordinates, Double.NaN);
                    final StringTokenizer tokens = new StringTokenizer(bbox.substring(offset+1, length-1), " ,", true);
                    while (tokens.hasMoreTokens()) {
                        final String t = tokens.nextToken().trim();
                        final int lg = t.length();
                        if (lg == 0) {
                            continue;
                        }
                        if (lg == 1 && t.charAt(0) == ',') {
                            final int m = i % dimension;
                            if (m != 0) {
                                i += (dimension - m);
                            }
                            continue;
                        }
                        try {
                            coordinates[i++] = Double.parseDouble(t);
                        } catch (NumberFormatException exception) {
                            throw new SQLException("Coordonnée invalide: " + t, exception);
                        } catch (ArrayIndexOutOfBoundsException exception) {
                            throw new SQLException("Trop de valeurs dans " + bbox);
                        }
                    }
                    final GeneralEnvelope envelope = new GeneralEnvelope(dimension);
                    for (i=0; i<dimension; i++) {
                        envelope.setRange(i, coordinates[i], coordinates[i+dimension]);
                    }
                    return envelope;
                }
            }
            throw new SQLException("Texte non-reconnu: " + bbox);
        }

        /**
         * Returns the dimension of the specified WKT keyword, or 0 if unknown.
         */
        private static int getDimension(final String prefix) {
            if (prefix.equals("BOX2D")) {
                return 2;
            }
            if (prefix.equals("BOX3D")) {
                return 3;
            }
            return 0;
        }
    }
}
