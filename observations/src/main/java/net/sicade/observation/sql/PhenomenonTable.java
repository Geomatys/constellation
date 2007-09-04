/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
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

import java.sql.ResultSet;
import java.sql.SQLException;

// Sicade dependencies
import net.sicade.catalog.ConfigurationKey;
import net.sicade.catalog.Database;
import net.sicade.catalog.Query;
import net.sicade.catalog.SingletonTable;
import net.sicade.observation.PhenomenonEntry;

// OpenGis dependencies
import org.opengis.observation.Phenomenon;


/**
 * Connexion vers la table des {@linkplain Phenomenon phénomènes}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Deprecated
public class PhenomenonTable extends SingletonTable<Phenomenon> {
    /**
     * Construit une table des phénomènes.
     * 
     * @param  database Connexion vers la base de données.
     */
    public PhenomenonTable(final Database database) {
        super(new PhenomenonQuery(database));
    }

    /**
     * Construit un phénomène pour l'enregistrement courant.
     */
    protected Phenomenon createEntry(final ResultSet results) throws SQLException {
        final PhenomenonQuery query = (PhenomenonQuery) super.query;
        return new PhenomenonEntry(results.getString(indexOf(query.name   )),
                                   results.getString(indexOf(query.remarks)),
                                   results.getString(indexOf(query.identifier)));
    }
}
