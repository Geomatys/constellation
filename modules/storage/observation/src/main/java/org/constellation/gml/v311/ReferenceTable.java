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

import java.sql.ResultSet;
import java.sql.SQLException;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.geotoolkit.gml.xml.v311.ReferenceEntry;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;

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
        super(query, query.byIdReference);
    }

    /**
     * Construit une nouvelle table non partagée
     */
    private ReferenceTable(final ReferenceTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected ReferenceTable clone() {
        return new ReferenceTable(this);
    }

    /**
     * Construit une reference pour l'enregistrement courant.
     */
    @Override
    protected ReferenceEntry createEntry(final ResultSet results, Comparable<?> identifier) throws CatalogException, SQLException {
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
    public String getIdentifier(final ReferenceEntry ref) throws SQLException, CatalogException {
        final ReferenceQuery query  = (ReferenceQuery) super.query;
        String id;
        boolean success = false;
        synchronized (getLock()) {
            transactionBegin();
            try {
                if (ref.getId() == null) {
                    final Stmt statement = getStatement(QueryType.LIST_ID);
                    if (ref.getActuate() != null) {
                        statement.statement.setString(indexOf(query.byActuate), ref.getActuate());
                    } else {
                        statement.statement.setNull(indexOf(query.byActuate), java.sql.Types.VARCHAR);
                    }
                    if (ref.getArcrole() != null) {
                        statement.statement.setString(indexOf(query.byArcrole), ref.getArcrole());
                    } else {
                        statement.statement.setNull(indexOf(query.byArcrole), java.sql.Types.VARCHAR);
                    }
                    if (ref.getHref() != null) {
                        statement.statement.setString(indexOf(query.byHref), ref.getHref());
                    } else {
                        statement.statement.setNull(indexOf(query.byHref), java.sql.Types.VARCHAR);
                    }
                    if (ref.getRole() != null) {
                        statement.statement.setString(indexOf(query.byRole), ref.getRole());
                    } else {
                        statement.statement.setNull(indexOf(query.byRole), java.sql.Types.VARCHAR);
                    }
                    if (ref.getShow() != null) {
                        statement.statement.setString(indexOf(query.byShow), ref.getShow());
                    } else {
                        statement.statement.setNull(indexOf(query.byShow), java.sql.Types.VARCHAR);
                    }
                    if (ref.getTitle() != null) {
                        statement.statement.setString(indexOf(query.byTitle), ref.getTitle());
                    } else {
                        statement.statement.setNull(indexOf(query.byTitle), java.sql.Types.VARCHAR);
                    }
                    if (ref.getOwns() != null) {
                        statement.statement.setBoolean(indexOf(query.byOwns), ref.getOwns());
                    } else {
                        statement.statement.setBoolean(indexOf(query.byOwns), false);
                    }
                    if (ref.getType() != null) {
                        statement.statement.setString(indexOf(query.byType), ref.getType());
                    } else {
                        statement.statement.setNull(indexOf(query.byType), java.sql.Types.VARCHAR);
                    }

                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(statement);
                        return result.getString("id_reference");
                    } else {
                        id = searchFreeIdentifier("reference");
                    }
                    result.close();
                    release(statement);
                } else {
                    id = ref.getId();
                    return id;
                }

                final Stmt statement = getStatement(QueryType.INSERT);
                statement.statement.setString(indexOf(query.idReference), id);

                if (ref.getActuate() != null) {
                    statement.statement.setString(indexOf(query.actuate), ref.getActuate());
                } else {
                    statement.statement.setNull(indexOf(query.actuate), java.sql.Types.VARCHAR);
                }
                if (ref.getArcrole() != null) {
                    statement.statement.setString(indexOf(query.arcrole), ref.getArcrole());
                } else {
                    statement.statement.setNull(indexOf(query.arcrole), java.sql.Types.VARCHAR);
                }
                if (ref.getHref() != null) {
                    statement.statement.setString(indexOf(query.href), ref.getHref());
                } else {
                    statement.statement.setNull(indexOf(query.href), java.sql.Types.VARCHAR);
                }
                if (ref.getRole() != null) {
                    statement.statement.setString(indexOf(query.role), ref.getRole());
                } else {
                    statement.statement.setNull(indexOf(query.role), java.sql.Types.VARCHAR);
                }
                if (ref.getShow() != null) {
                    statement.statement.setString(indexOf(query.show), ref.getShow());
                } else {
                    statement.statement.setNull(indexOf(query.show), java.sql.Types.VARCHAR);
                }
                if (ref.getTitle() != null) {
                    statement.statement.setString(indexOf(query.title), ref.getTitle());
                } else {
                    statement.statement.setNull(indexOf(query.title), java.sql.Types.VARCHAR);
                }
                if (ref.getOwns() != null) {
                    statement.statement.setBoolean(indexOf(query.owns), ref.getOwns());
                } else {
                    statement.statement.setBoolean(indexOf(query.owns), false);
                }
                if (ref.getType() != null) {
                    statement.statement.setString(indexOf(query.type), ref.getType());
                } else {
                    statement.statement.setNull(indexOf(query.type), java.sql.Types.VARCHAR);
                }
                updateSingleton(statement.statement);
                release(statement);
                success = true;
            } finally {
                transactionEnd(success);
            }
        }
        return id;
    }
}
