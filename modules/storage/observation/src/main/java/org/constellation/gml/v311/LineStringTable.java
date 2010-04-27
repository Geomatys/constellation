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
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.SingletonTable;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.util.Utilities;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class LineStringTable extends SingletonTable<DirectPositionEntry> {

    /**
     * identifiant secondaire de la table.
     */
    private String idlineString;

    public LineStringTable(final Database database) {
        this(new LineStringQuery(database));
    }

     /**
     * Initialise l'identifiant de la table.
     */
    private LineStringTable(final LineStringQuery query) {
        super(query);
        setIdentifierParameters(query.byIdentifier, null);
    }

    /**
     * Construit une nouvelle table non partagée
     */
    public LineStringTable(final LineStringTable table) {
        super(table);
    }

    @Override
    protected DirectPositionEntry createEntry(ResultSet results) throws CatalogException, SQLException {
        final LineStringQuery query = (LineStringQuery) super.query;
        Double x = results.getDouble(indexOf(query.xValue));
        Double y = results.getDouble(indexOf(query.yValue));
        Double z = results.getDouble(indexOf(query.zValue));
        if (results.wasNull()) {
            z = null;
        }
        DirectPositionType pos;
        if (z != null) {
            pos = new DirectPositionType(x, y, z);
        } else {
            pos = new DirectPositionType(x, y);
        }
        String name = results.getString(indexOf(query.identifier)) + "/" + x + "/" + y + "/" + z;
        return new DirectPositionEntry(name, pos);

    }

    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException, CatalogException {
        super.configure(type, statement);
        final LineStringQuery query = (LineStringQuery) super.query;
        if (! type.equals(QueryType.INSERT))
            statement.setString(indexOf(query.byIdentifier), idlineString);

    }


    public synchronized void getIdentifier(String idline, DirectPositionType position) throws SQLException, CatalogException {

    }
    
    public String getIdLineString() {
        return idlineString;
    }

    public synchronized void setIdLineString(String idlineString) {
        if (!Utilities.equals(this.idlineString, idlineString)) {
            this.idlineString = idlineString;
            fireStateChanged("idlineString");
        }

    }
}
