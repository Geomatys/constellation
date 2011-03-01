/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.swe.v101;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.constellation.gml.v311.ReferenceTable;
import org.geotoolkit.gml.xml.v311.ReferenceType;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.swe.xml.v101.AnyResultType;
import org.geotoolkit.swe.xml.v101.DataArrayType;
import org.geotoolkit.swe.xml.v101.DataArrayPropertyType;

/**
 * Connexion vers la table des {@linkplain AnyResultType AnyResult}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class AnyResultTable extends SingletonTable<AnyResultType>{
    
    /**
     * Connection to the table of {@linkplain Reference reference}.
     * A connection (potentielly shared) will be establish the first time it'll be necesary.
     */
    private ReferenceTable references;
    
    /**
     * Connection to the table of {@linkplain Reference reference}.
     * A connection (potentielly shared) will be establish the first time it'll be necesary.
     */
    private DataArrayTable dataArrays;
    
    /**
     * Construit une table des resultats.
     *
     * @param  database Connexion vers la base de données.
     */
    public AnyResultTable(final Database database) {
        this(new AnyResultQuery(database));
    }

    /**
     * Initialise l'identifiant de la table.
     */
    private AnyResultTable(final AnyResultQuery query) {
        super(query, query.byIdResult);
    }

     /**
     * Construit une nouvelle table non partagée
     */
    private AnyResultTable(final AnyResultTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected AnyResultTable clone() {
        return new AnyResultTable(this);
    }
   
    /**
     * Construit une reference pour l'enregistrement courant.
     */
    @Override
    protected AnyResultType createEntry(final LocalCache lc, final ResultSet results, Comparable<?> identifier) throws CatalogException, SQLException {
         final AnyResultQuery query = (AnyResultQuery) super.query;
         final String idRef = results.getString(indexOf(query.reference));
         if (idRef != null) {
            if(references == null) {
                 references = getDatabase().getTable(ReferenceTable.class);
            }
            final ReferenceType ref = references.getEntry(idRef);
            return new AnyResultType(Integer.toString(results.getInt(indexOf(query.idResult))), ref);
         } else {

            if(dataArrays == null) {
                dataArrays = getDatabase().getTable(DataArrayTable.class);
            }
            final DataArrayType entry = dataArrays.getEntry(results.getString(indexOf(query.definition)));
            final DataArrayType array = new DataArrayType(entry.getId(),
                                                      entry.getElementCount().getCount().getValue(),  
                                                      entry.getElementType(),
                                                      entry.getEncoding(),
                                                      results.getString(indexOf(query.values))); 
            return new AnyResultType(Integer.toString(results.getInt(indexOf(query.idResult))), array);
         }
    }
    
     /**
     * Retourne un nouvel identifier (ou l'identifier du resultat passée en parametre si non-null)
     * et enregistre le nouveau resultat dans la base de donnée.
     *
     * @param result le resultat a inserer dans la base de donnée.
     */
    public String getIdentifier(final Object result) throws SQLException, CatalogException {
        final AnyResultQuery query = (AnyResultQuery) super.query;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                Stmt statement = getStatement(lc, QueryType.LIST);
                ResultSet results;
                if (result instanceof AnyResultType) {
                    final DataArrayType array = ((AnyResultType)result).getArray();
                    statement.statement.setString(indexOf(query.values),array.getValues());
                    statement.statement.setNull(indexOf(query.reference), java.sql.Types.VARCHAR);
                    statement.statement.setString(indexOf(query.definition), array.getId());
                    results = statement.statement.executeQuery();
                } else if (result instanceof DataArrayPropertyType) {
                    final DataArrayType array = ((DataArrayPropertyType)result).getDataArray();
                    if (array == null)
                        throw new CatalogException("The data array is null!");
                    statement.statement.setString(indexOf(query.values),array.getValues());
                    statement.statement.setNull(indexOf(query.reference), java.sql.Types.VARCHAR);
                    statement.statement.setString(indexOf(query.definition), array.getId());
                    results = statement.statement.executeQuery();
                } else if (result instanceof ReferenceType) {
                    statement.statement.setString(indexOf(query.reference), ((ReferenceType)result).getId());
                    statement.statement.setNull(indexOf(query.values), java.sql.Types.VARCHAR);
                    results = statement.statement.executeQuery();
                } else {
                    throw new CatalogException(" this kind of result is not allowed");
                }
                String r = null;
                if(results.next()){
                    r = results.getString(1);
                }
                results.close();
                if (r != null) {
                    success = true;
                    release(lc, statement);
                    return r;
                }
                release(lc, statement);

                final Statement p = lc.connection().createStatement();
                statement = getStatement(lc, QueryType.INSERT);

                results = p.executeQuery("SELECT max(\"id_result\") FROM \"observation\".\"any_results\"");
                if (results.next()) {
                    r = results.getString(1);
                    try {
                        final int id = Integer.parseInt(r);
                        statement.statement.setInt(indexOf(query.idResult), id + 1);
                    } catch (NumberFormatException ex) {
                        log("getIdentifier", new LogRecord(Level.WARNING, "unable to parse the result id:" + r));
                        statement.statement.setInt(indexOf(query.idResult), 1);
                    }
                } else {
                    statement.statement.setInt(indexOf(query.idResult), 1);
                }
                results.close();

                if (result instanceof AnyResultType) {
                    final DataArrayType array = ((AnyResultType)result).getArray();
                    statement.statement.setString(indexOf(query.values), array.getValues());
                    statement.statement.setNull(indexOf(query.reference), java.sql.Types.VARCHAR);
                    if(dataArrays == null) {
                        dataArrays = getDatabase().getTable(DataArrayTable.class);
                    }
                    final String idArray = dataArrays.getIdentifier(array);
                    statement.statement.setString(indexOf(query.definition), idArray);

                } else if (result instanceof DataArrayPropertyType) {
                    final DataArrayType array = ((DataArrayPropertyType)result).getDataArray();
                    //we cleanup a little the values
                    String values = array.getValues();
                    values = values.replace("\n", " ");
                    values = values.replace("\t", " ");
                    while (values.indexOf("  ") != -1) {
                        values = values.replaceAll("  ", "");
                    }
                    statement.statement.setString(indexOf(query.values), values);
                    statement.statement.setNull(indexOf(query.reference), java.sql.Types.VARCHAR);
                    if(dataArrays == null) {
                        dataArrays = getDatabase().getTable(DataArrayTable.class);
                    }
                    final String idArray = dataArrays.getIdentifier(array);
                    statement.statement.setString(indexOf(query.definition), idArray);

                } else if (result instanceof ReferenceType) {
                    final ReferenceType ref = (ReferenceType) result;
                    String idRef;

                    if(references == null) {
                        references = getDatabase().getTable(ReferenceTable.class);
                    }
                    idRef = references.getIdentifier(ref);

                    statement.statement.setString(indexOf(query.reference), idRef);
                    statement.statement.setNull(indexOf(query.values), java.sql.Types.VARCHAR);
                } else {
                    throw new CatalogException(" this kind of result is not allowed");
                }

                updateSingleton(statement.statement);
                release(lc, statement);
                
                //we get the new id generated
                results = p.executeQuery("SELECT max(\"id_result\") FROM \"observation\".\"any_results\"" );
                final String id;
                if (results.next()) {
                    id = results.getString(1);
                } else {
                    id = null;
                }
                results.close();
                p.close();
                success = true;
                return id;
            } finally {
                transactionEnd(lc, success);
            }
        }
    }
    
}
