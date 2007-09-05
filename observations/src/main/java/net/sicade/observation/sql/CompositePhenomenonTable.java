/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;
import net.sicade.observation.CompositePhenomenonEntry;
import org.opengis.observation.CompositePhenomenon;

/**
 * Connexion vers la table des {@linkplain CompositePhenomenon ph�nom�nes compos�}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class CompositePhenomenonTable extends SingletonTable<CompositePhenomenon>{
    
   /**
     * Construit une table des ph�nom�nes compos�.
     * 
     * @param  database Connexion vers la base de donn�es.
     */
    public CompositePhenomenonTable(final Database database) {
        super(new CompositePhenomenonQuery(database));
    }
    
    /**
     * Construit un ph�nom�ne pour l'enregistrement courant.
     */
    protected CompositePhenomenon createEntry(final ResultSet results) throws SQLException {
        final CompositePhenomenonQuery query = (CompositePhenomenonQuery) super.query;
        return new CompositePhenomenonEntry(results.getString(indexOf(query.name   )),
                                   results.getString(indexOf(query.remarks)),
                                   results.getString(indexOf(query.identifier)));
    }
    
}
