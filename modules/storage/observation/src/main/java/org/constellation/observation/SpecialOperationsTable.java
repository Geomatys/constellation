/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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

package org.constellation.observation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.Table;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SpecialOperationsTable extends Table implements Cloneable {

     public SpecialOperationsTable(final Database database) {
        this(new SpecialOperationsQuery(database));
    }

     /**
     * Initialise l'identifiant de la table.
     */
    private SpecialOperationsTable(final SpecialOperationsQuery query) {
        super(query);
    }

    /**
     * Construit une nouvelle table non partagée
     */
    private SpecialOperationsTable(final SpecialOperationsTable table) {
        super(table);
    }

    @Override
    protected Table clone() {
        return new SpecialOperationsTable(this);
    }


    public boolean observationExists(final String template) throws SQLException {
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            final PreparedStatement stmt = lc.connection().prepareStatement(" SELECT \"name\" FROM \"observation\".\"observations\"" +
                                                                           " WHERE \"name\"=? " +
                                                                           " UNION " +
                                                                           " SELECT \"name\" FROM \"observation\".\"measurements\" " +
                                                                           " WHERE \"name\"=?");
            stmt.setString(1, template);
            stmt.setString(2, template);

            final ResultSet r = stmt.executeQuery();
            final boolean result = r.next();
            r.close();
            stmt.close();
            return result;
        }
    }

    public int observationCount(final String observationIdBase) throws SQLException {
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            final Statement stmt     = lc.connection().createStatement();
            final ResultSet r        = stmt.executeQuery("SELECT Count(*) FROM \"observation\".\"observations\" WHERE \"name\" LIKE '%" + observationIdBase + "%' ");

            int result = 0;
            if (r.next()) {
                result = r.getInt(1);
            }
            r.close();
            stmt.close();
            return result;
        }
    }

    public int measureCount(final String observationIdBase) throws SQLException {
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            final Statement stmt     = lc.connection().createStatement();
            final ResultSet r        = stmt.executeQuery("SELECT Count(*) FROM \"observation\".\"measurements\" WHERE \"name\" LIKE '%" + observationIdBase + "%'  ");

            int result = 0;
            if (r.next()) {
                result = r.getInt(1);
            }
            r.close();
            stmt.close();
            return result;
        }
    }

    public List<Date> getTimeForStation(final String samplingfeatureId) throws SQLException {
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            final Statement stmt = lc.connection().createStatement();
            final ResultSet r    = stmt.executeQuery("SELECT min(sampling_time_begin), max(sampling_time_end) "
                                                   + "FROM \"observation\".\"observations\" "
                                                   + "WHERE \"feature_of_interest\" ='" + samplingfeatureId + "' "
                                                   + "OR \"feature_of_interest_point\" ='" + samplingfeatureId + "' "
                                                   + "OR \"feature_of_interest_curve\" ='" + samplingfeatureId + "' ");
            final List<Date> result;
            if (r.next()) {
                final Date begin = r.getDate(1);
                final Date end   = r.getDate(2);
                result = Arrays.asList(begin, end);
            } else {
                result = new ArrayList<Date>();
            }
            r.close();
            stmt.close();
            return result;
        }
    }

    public Timestamp getMinTimeOffering() throws SQLException {
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            final Statement stmt     = lc.connection().createStatement();
            final ResultSet r        = stmt.executeQuery("select MIN(\"event_time_begin\") from \"sos\".\"observation_offerings\"");

            final Timestamp result;
            if (r.next()) {
                result = r.getTimestamp(1);
            } else {
                result = null;
            }
            r.close();
            stmt.close();
            return result;
        }
    }

}
