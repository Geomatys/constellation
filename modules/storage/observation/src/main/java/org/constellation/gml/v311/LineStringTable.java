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

package org.constellation.gml.v311;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.Table;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class LineStringTable extends Table {

    public LineStringTable(final Database database) {
        this(new LineStringQuery(database));
    }

     /**
     * Initialise l'identifiant de la table.
     */
    private LineStringTable(final LineStringQuery query) {
        super(query);
    }

    /**
     * Construit une nouvelle table non partagée
     */
    private LineStringTable(final LineStringTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected LineStringTable clone() {
        return new LineStringTable(this);
    }

    public List<DirectPositionEntry> getEntries(final String idLineString) throws CatalogException, SQLException {
        final LineStringQuery query = (LineStringQuery) this.query;
        final List<DirectPositionEntry> positions = new ArrayList<DirectPositionEntry>();
        synchronized (getLock()) {
            final LocalCache.Stmt ce = getStatement(QueryType.LIST);
            final PreparedStatement statement = ce.statement;
            statement.setString(indexOf(query.byIdentifier), idLineString);
            final int  xIndex = indexOf(query.xValue);
            final int  yIndex = indexOf(query.yValue);
            final int  zIndex = indexOf(query.zValue);
            final int idIndex = indexOf(query.identifier);
            final ResultSet results = statement.executeQuery();
            while (results.next()) {
                final DirectPositionType pos;
                final double x = results.getDouble(xIndex);
                final double y = results.getDouble(yIndex);
                final double z = results.getDouble(zIndex);
                if (results.wasNull()) {
                    pos = new DirectPositionType(x, y);
                } else {
                    pos = new DirectPositionType(x, y, z);
                }
                String name = results.getString(idIndex) + '/' + x + '/' + y + '/' + z;
                positions.add(new DirectPositionEntry(name, pos));
            }
            results.close();
            release(ce);
        }
        return positions;
    }

    public void getIdentifier(String idline, DirectPositionType position) throws SQLException, CatalogException {
        System.out.println("Attention implementation manquante getIdentifer LineString");
    }

}
