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
package net.sicade.observation;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;

// OpenGis dependencies
import org.opengis.observation.Phenomenon;


/**
 * Connexion vers la table des {@linkplain Phenomenon phénomènes}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 */
public class PhenomenonTable<EntryType extends Phenomenon> extends SingletonTable<PhenomenonEntry> {
   
    /**
     * Construit une table des phénomènes.
     * 
     * @param  database Connexion vers la base de données.
     */
    public PhenomenonTable(final Database database) {
        super(new PhenomenonQuery(database));
        PhenomenonQuery query = new PhenomenonQuery(database);
        setIdentifierParameters(query.byName, null);
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    protected PhenomenonTable(final PhenomenonQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }
    
    /**
     * Construit un phénomène pour l'enregistrement courant.
     */
    protected PhenomenonEntry createEntry(final ResultSet results) throws SQLException, CatalogException {
        final PhenomenonQuery query = (PhenomenonQuery) super.query;
        return new PhenomenonEntry(results.getString(indexOf(query.identifier)),
                                   results.getString(indexOf(query.name)),
                                   results.getString(indexOf(query.remarks)));
    }
}
