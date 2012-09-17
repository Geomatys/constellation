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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.swe.xml.v101.AnyScalarPropertyType;
import org.geotoolkit.swe.xml.v101.SimpleDataRecordType;
import java.util.Objects;

/**
 *  Connexion vers la table des {@linkplain SimpleDataRecord simpleDataRecord}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class SimpleDataRecordTable extends SingletonTable<SimpleDataRecordType> implements Cloneable {

    /**
     * identifiant secondaire de la table
     * (identifiant du DataBlock contenant le dataRecord qui possede ce champ).
     */
    private String idDataBlock;


    /**
     * Connexion vers la table des {@linkplain DataRecordField dataRecord field}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private AnyScalarTable fields;

    /**
     * Construit une table des data record.
     *
     * @param  database Connexion vers la base de données.
     */
    public SimpleDataRecordTable(final Database database) {
        this(new SimpleDataRecordQuery(database));
    }

    /**
     * Initialise l'identifiant de la table.
     */
    private SimpleDataRecordTable(final SimpleDataRecordQuery query) {
        super(query,query.byIdDataRecord);
    }

    /**
     * Un constructeur qui prend en parametre un table partagée afin d'en creer
     * une qui ne l'ai pas.
     */
    private SimpleDataRecordTable(final SimpleDataRecordTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected SimpleDataRecordTable clone() {
        return new SimpleDataRecordTable(this);
    }

    /**
     * retourne l'identifiant du DataBlock contenant le dataRecord qui possede ce champ.
     */
    public synchronized String getIdDataBlock() {
        return idDataBlock;
    }

    /**
     * Modifie l'identifiant du dataBlock si il est different de l'actuel.
     *
     * @param idDataBlock le nouvel identifiant du dataBlock.
     */
    public synchronized void setIdDataBlock(final String idDataBlock) {
        if (!Objects.equals(this.idDataBlock, idDataBlock)) {
            this.idDataBlock = idDataBlock;
            fireStateChanged("idDataBlock");
        }
    }

    /**
     * Construit un data record pour l'enregistrement courant.
     */
    @Override
    protected SimpleDataRecordType createEntry(final LocalCache lc, final ResultSet results, Comparable<?> identifier) throws CatalogException, SQLException {
        final SimpleDataRecordQuery query = (SimpleDataRecordQuery) super.query;
        final String idDataBlock = results.getString(indexOf(query.idBlock));
        final String idDataRecord = results.getString(indexOf(query.idDataRecord));

        if (fields == null) {
            fields = getDatabase().getTable(AnyScalarTable.class);
        }

        fields.setIdDataBlock(idDataBlock);
        fields.setIdDataRecord(idDataRecord);
        final Collection<AnyScalarPropertyType> scalars = fields.getEntries();

        return new SimpleDataRecordType(idDataBlock, idDataRecord,
                results.getString(indexOf(query.definition)),
                results.getBoolean(indexOf(query.fixed)),
                scalars);

    }

    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final LocalCache lc, final QueryType type, final PreparedStatement statement) throws SQLException, CatalogException {
        super.configure(lc, type, statement);
        final SimpleDataRecordQuery query = (SimpleDataRecordQuery) super.query;
        if (!type.equals(QueryType.INSERT))
            statement.setString(indexOf(query.byIdBlock), idDataBlock);

    }

    /**
     * Retourne un nouvel identifier (ou l'identifier du data record passée en parametre si non-null)
     * et enregistre le nouveau data record dans la base de donnée si il n'y est pas deja.
     *
     * @param datarecord le data record a inserer dans la base de donnée.
     */
    public String getIdentifier(final SimpleDataRecordType datarecord, String dataBlockId) throws SQLException, CatalogException {
        final SimpleDataRecordQuery query  = (SimpleDataRecordQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                if (datarecord.getId() != null) {
                    final Stmt statement = getStatement(lc, QueryType.EXISTS);
                    statement.statement.setString(indexOf(query.idBlock),      dataBlockId);
                    statement.statement.setString(indexOf(query.idDataRecord), datarecord.getId());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(lc, statement);
                        return datarecord.getId();
                    } else {
                        id = datarecord.getId();
                    }
                    result.close();
                    release(lc, statement);
                } else {
                    id = searchFreeIdentifier(lc, "datarecord");
                }

                final Stmt statement = getStatement(lc, QueryType.INSERT);
                statement.statement.setString(indexOf(query.idDataRecord), id);
                statement.statement.setString(indexOf(query.idBlock),      dataBlockId);
                if (datarecord.getDefinition() != null) {
                    statement.statement.setString(indexOf(query.definition),   datarecord.getDefinition().toString());
                } else {
                    statement.statement.setNull(indexOf(query.definition),  java.sql.Types.VARCHAR);
                }
                statement.statement.setBoolean(indexOf(query.fixed),       datarecord.isFixed());
                updateSingleton(statement.statement);
                release(lc, statement);

                if (fields == null) {
                    fields = getDatabase().getTable(AnyScalarTable.class);
                    fields.setIdDataBlock(dataBlockId);
                    fields.setIdDataRecord(id);
                } else {
                    fields.setIdDataBlock(dataBlockId);
                    fields.setIdDataRecord(id);
                }
                final Iterator<AnyScalarPropertyType> i = datarecord.getField().iterator();

                while (i.hasNext()) {
                   fields.getIdentifier(i.next(), dataBlockId, id);
                }
                success = true;
            } finally {
                transactionEnd(lc, success);
            }
        }
        return id;
    }
}
