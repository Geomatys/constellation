/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
        boolean success = false;
        transactionBegin();
        try {
            if (ref.getId() == null) {
                PreparedStatement statement = getStatement(QueryType.FILTERED_LIST);
                if (ref.getActuate() != null) {
                    statement.setString(indexOf(query.byActuate), ref.getActuate());
                } else {
                    statement.setString(indexOf(query.byActuate), "");
                }
                if (ref.getArcrole() != null) {
                    statement.setString(indexOf(query.byArcrole), ref.getArcrole());
                } else {
                    statement.setString(indexOf(query.byArcrole), "");
                }
                if (ref.getHref() != null) {
                    statement.setString(indexOf(query.byHref), ref.getHref());
                } else {
                    statement.setString(indexOf(query.byHref), "");
                }
                if (ref.getRole() != null) {
                    statement.setString(indexOf(query.byRole), ref.getRole());
                } else {
                    statement.setString(indexOf(query.byRole), "");
                }
                if (ref.getShow() != null) {
                    statement.setString(indexOf(query.byShow), ref.getShow());
                } else {
                    statement.setString(indexOf(query.byShow), "");
                }
                if (ref.getTitle() != null) {
                    statement.setString(indexOf(query.byTitle), ref.getTitle());
                } else {
                    statement.setString(indexOf(query.byTitle), "");
                }
                if (ref.getOwns() != null) {
                    statement.setBoolean(indexOf(query.byOwns), ref.getOwns());
                } else {
                    statement.setBoolean(indexOf(query.byOwns), false);
                }
                if (ref.getType() != null) {
                    statement.setString(indexOf(query.byType), ref.getType());
                } else {
                    statement.setString(indexOf(query.byType), "");
                }

                ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return result.getString("id_reference");
                } else {
                    id = searchFreeIdentifier("reference");
                }
            } else {
                id = ref.getId();
                return id;
            }
        
            PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.idReference), id);
        
            if (ref.getActuate() != null) {
                statement.setString(indexOf(query.actuate), ref.getActuate());
            } else {
                statement.setString(indexOf(query.actuate), "");
            }
            if (ref.getArcrole() != null) {
                statement.setString(indexOf(query.arcrole), ref.getArcrole());
            } else {
                statement.setString(indexOf(query.arcrole), "");
            }
            if (ref.getHref() != null) {
                statement.setString(indexOf(query.href), ref.getHref());
            } else {
                statement.setString(indexOf(query.href), "");
            }
            if (ref.getRole() != null) {
                statement.setString(indexOf(query.role), ref.getRole());
            } else {
                statement.setString(indexOf(query.role), "");
            }
            if (ref.getShow() != null) {
                statement.setString(indexOf(query.show), ref.getShow());
            } else {
                statement.setString(indexOf(query.show), "");
            }
            if (ref.getTitle() != null) {
                statement.setString(indexOf(query.title), ref.getTitle());
            } else {
                statement.setString(indexOf(query.title), "");
            }
            if (ref.getOwns() != null) {
                statement.setBoolean(indexOf(query.owns), ref.getOwns());
            } else {
                statement.setBoolean(indexOf(query.owns), false);
            }
            if (ref.getType() != null) {
                statement.setString(indexOf(query.type), ref.getType());
            } else {
                statement.setString(indexOf(query.type), "");
            }
            updateSingleton(statement);
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
}
