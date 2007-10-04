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
package net.sicade.gml;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.QueryType;
import net.sicade.catalog.SingletonTable;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class ReferenceTable extends SingletonTable<ReferenceEntry>{
    
    /**
     * Construit une table des reference.
     *
     * @param  database Connexion vers la base de données.
     */
    public ReferenceTable(final Database database) {
        this(new ReferenceQuery(database));
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private ReferenceTable(final ReferenceQuery query) {
        super(query);
        setIdentifierParameters(query.byIdReference, null);
    }
    
    /**
     * Construit une reference pour l'enregistrement courant.
     */
    protected ReferenceEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final ReferenceQuery query = (ReferenceQuery) super.query;
        return new ReferenceEntry(results.getString(indexOf(query.idReference)),
                results.getString(indexOf(query.href)));
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier de la reference passée en parametre si non-null)
     * et enregistre la nouvelle reference dans la base de donnée.
     *
     * @param result le resultat a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final ReferenceEntry ref) throws SQLException, CatalogException {
        final ReferenceQuery query  = (ReferenceQuery) super.query;
        String id;
        if (ref.getId() != null) {
            PreparedStatement statement = getStatement(QueryType.EXISTS);
            statement.setString(indexOf(query.idReference), ref.getId());
            ResultSet result = statement.executeQuery();
            if(result.next())
                return ref.getId();
            else
                id = ref.getId();
        } else {
            id = searchFreeIdentifier("reference");
        }
        
        PreparedStatement statement = getStatement(QueryType.INSERT);
        statement.setString(indexOf(query.idReference), id);
        
        if (ref.getActuate() != null) {
            statement.setString(indexOf(query.actuate), ref.getActuate());
        } else {
            statement.setNull(indexOf(query.actuate), java.sql.Types.VARCHAR);
        }
        if (ref.getArcrole() != null) {
            statement.setString(indexOf(query.arcrole), ref.getArcrole());
        } else {
            statement.setNull(indexOf(query.arcrole), java.sql.Types.VARCHAR);
        }
        if (ref.getHref() != null) {
            statement.setString(indexOf(query.href), ref.getHref());
        } else {
            statement.setNull(indexOf(query.href), java.sql.Types.VARCHAR);
        }
        if (ref.getRole() != null) {
            statement.setString(indexOf(query.role), ref.getRole());
        } else {
            statement.setNull(indexOf(query.role), java.sql.Types.VARCHAR);
        }
        if (ref.getShow() != null) {
            statement.setString(indexOf(query.show), ref.getShow());
        } else {
            statement.setNull(indexOf(query.show), java.sql.Types.VARCHAR);
        }
        if (ref.getTitle() != null) {
            statement.setString(indexOf(query.title), ref.getTitle());
        } else {
            statement.setNull(indexOf(query.title), java.sql.Types.VARCHAR);
        }
        if (ref.getOwns() != null) {
            statement.setBoolean(indexOf(query.owns), ref.getOwns());
        } else {
            statement.setNull(indexOf(query.owns), java.sql.Types.BOOLEAN);
        }
        if (ref.getType() != null) {
            statement.setString(indexOf(query.type), ref.getType());
        } else {
            statement.setNull(indexOf(query.type), java.sql.Types.VARCHAR);
        }
        insertSingleton(statement); 
        return id;
    }
}
