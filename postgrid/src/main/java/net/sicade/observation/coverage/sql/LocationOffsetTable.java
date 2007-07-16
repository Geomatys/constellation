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

import java.sql.ResultSet;
import java.sql.SQLException;

import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.LocationOffset;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.SingletonTable;
import net.sicade.observation.sql.Column;
import net.sicade.observation.sql.Parameter;
import net.sicade.observation.sql.Role;
import static net.sicade.observation.sql.QueryType.*;


/**
 * Connexion vers la table des {@linkplain LocationOffset décalage spatio-temporels} relatifs aux
 * positions des {@linkplain net.sicade.observation.Observation observations}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
@UsedBy(DescriptorTable.class)
public class LocationOffsetTable extends SingletonTable<LocationOffset> implements Shareable {
    /**
     * Column name declared in the {@linkplain #query query}.
     */
    private final Column name, dx, dy, dz, dt;

    /**
     * Parameter declared in the {@linkplain #query query}.
     */
    private final Parameter byName;

    /**
     * Construit une table en utilisant la connexion spécifiée.
     *
     * @param  database Connexion vers la base de données d'observations.
     */
    public LocationOffsetTable(final Database database) {
        super(database);
        final QueryType[] usage = {SELECT, LIST};
        name = new Column   (query, "LocationOffsets", "name", usage);
        dx   = new Column   (query, "LocationOffsets", "dx",   usage);
        dy   = new Column   (query, "LocationOffsets", "dy",   usage);
        dz   = new Column   (query, "LocationOffsets", "dz",   usage);
        dt   = new Column   (query, "LocationOffsets", "dt",   usage);
        byName  = new Parameter(query, name,  SELECT);
        name.setRole(Role.NAME);
        dt.setOrdering("DESC");
        dz.setOrdering("DESC");
        dy.setOrdering("DESC");
        dx.setOrdering("DESC");
    }

    /**
     * Construit un décalage spatio-temporel pour l'enregistrement courant.
     */
    protected LocationOffset createEntry(final ResultSet results) throws SQLException, CatalogException {
        return new LocationOffsetEntry(
                results.getString(indexOf(name)),
                results.getDouble(indexOf(dx  )),
                results.getDouble(indexOf(dy  )),
                results.getDouble(indexOf(dz  )),
                Math.round(results.getDouble(indexOf(dt)) * LocationOffsetEntry.DAY));
    }
}
