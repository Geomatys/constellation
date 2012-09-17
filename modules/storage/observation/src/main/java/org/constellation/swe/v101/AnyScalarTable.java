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
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.geotoolkit.swe.xml.v101.AbstractDataComponentType;
import org.geotoolkit.swe.xml.v101.AnyScalarPropertyType;
import org.geotoolkit.swe.xml.v101.BooleanType;
import org.geotoolkit.swe.xml.v101.QuantityType;
import org.geotoolkit.swe.xml.v101.TimeType;
import java.util.Objects;

/**
 * Connexion vers la table des {@linkplain AnyScalarType dataRecord field}.
 *
 *
 * @author Guilhem Legal
 * @version $Id:
 */
public class AnyScalarTable extends SingletonTable<AnyScalarPropertyType> implements Cloneable {

    /**
     * identifiant secondaire de la table
     * (identifiant du DataBlock contenant le dataRecord qui possede ce champ).
     */
    private String idDataBlock;

    /**
     * identifiant secondaire de la table.
     * (identifiant du dataRecord qui possede ce champ).
     */
    private String idDataRecord;

    /**
     * Construit une table des dataRecord field.
     *
     * @param  database Connexion vers la base de données.
     */
    public AnyScalarTable(final Database database) {
        this(new AnyScalarQuery(database));
    }

    /**
     * Initialise l'identifiant de la table.
     */
    private AnyScalarTable(final AnyScalarQuery query) {
        super(query, query.byName);
    }

     /**
     * Un constructeur qui prend en parametre un table partagée afin d'en creer
     * une qui ne l'ai pas.
     */
    private AnyScalarTable(final AnyScalarTable table) {
        super(table);
    }
    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected AnyScalarTable clone() {
        return new AnyScalarTable(this);
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
     * Retourne identifiant du dataRecord qui possede ce champ.
     */
    public String getIdDataRecord() {
        return idDataRecord;
    }

    /**
     * Modifie l'identifiant du dataRecord si il est different de l'actuel.
     *
     * @param idDataRecoed le nouvel identifiant du dataRecord.
     */
    public synchronized void setIdDataRecord(final String idDataRecord) {
        if (!Objects.equals(this.idDataRecord, idDataRecord)) {
            this.idDataRecord = idDataRecord;
            fireStateChanged("idDataRecord");
        }
    }

    /**
     * Construit un data block pour l'enregistrement courant.
     */
    @Override
    protected AnyScalarPropertyType createEntry(final LocalCache lc, final ResultSet results, Comparable<?> identifier) throws SQLException {
        final AnyScalarQuery query = (AnyScalarQuery) super.query;
        AbstractDataComponentType component = null;
        if (results.getString(indexOf(query.type)).equals("Quantity")) {
            component = new QuantityType(results.getString(indexOf(query.definition )),
                                              results.getString(indexOf(query.uomCode)),
                                              results.getString(indexOf(query.uomHref)));
        } else if (results.getString(indexOf(query.type)).equals("Time")) {
            component = new TimeType(results.getString(indexOf(query.definition )),
                                     results.getString(indexOf(query.uomCode)),
                                     results.getString(indexOf(query.uomHref)));
        } else if (results.getString(indexOf(query.type)).equals("Boolean")) {
            component = new BooleanType(results.getString(indexOf(query.definition )),
                                        results.getBoolean(indexOf(query.value)));
        }
        return new AnyScalarPropertyType(
                results.getString(indexOf(query.idDataRecord )),
                results.getString(indexOf(query.name )),
                component);
    }

    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final LocalCache lc, final QueryType type, final PreparedStatement statement) throws SQLException, CatalogException {
        super.configure(lc, type, statement);
        final AnyScalarQuery query = (AnyScalarQuery) super.query;
        if(!type.equals(QueryType.INSERT)){
            statement.setString(indexOf(query.byIdDataRecord), idDataRecord);
            statement.setString(indexOf(query.byIdDataBlock), idDataBlock);
        }

    }

     /**
     * Retourne un nouvel identifier (ou l'identifier du champ passée en parametre si non-null)
     * et enregistre le nouveau champ dans la base de donnée si il n'y est pas deja.
     *
     * @param datarecord le data record a inserer dans la base de donnée.
     */
    public String getIdentifier(final AnyScalarPropertyType field, String blockId, String dataRecordId) throws SQLException, CatalogException {
        final AnyScalarQuery query  = (AnyScalarQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                if (field.getName() != null) {
                    final Stmt statement = getStatement(lc, QueryType.EXISTS);
                    statement.statement.setString(indexOf(query.byIdDataBlock), blockId);
                    statement.statement.setString(indexOf(query.idDataRecord),  dataRecordId);
                    statement.statement.setString(indexOf(query.name),          field.getName());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(lc, statement);
                        return field.getName();
                    } else {
                        id = field.getName();
                    }
                    result.close();
                    release(lc, statement);
                } else {
                    id = searchFreeIdentifier(lc, "field");
                }

                final Stmt statement = getStatement(lc, QueryType.INSERT);
                statement.statement.setString(indexOf(query.idDataRecord), dataRecordId);
                statement.statement.setString(indexOf(query.idDataBlock),  blockId);
                statement.statement.setString(indexOf(query.name),         id);
                if (field.getValue() != null && field.getValue().getDefinition() != null) {
                    statement.statement.setString(indexOf(query.definition),   field.getValue().getDefinition().toString());
                } else {
                    statement.statement.setNull(indexOf(query.definition), java.sql.Types.VARCHAR);
                }

                if (field.getValue() instanceof QuantityType) {
                    final QuantityType q = (QuantityType) field.getValue();

                    statement.statement.setString(indexOf(query.type), "Quantity");
                    if ( q.getUom().getCode() != null)
                        statement.statement.setString(indexOf(query.uomCode), q.getUom().getCode());
                    else
                        statement.statement.setNull(indexOf(query.uomCode), java.sql.Types.VARCHAR);

                    if ( q.getUom().getHref() != null)
                        statement.statement.setString(indexOf(query.uomHref), q.getUom().getHref());
                    else
                        statement.statement.setNull(indexOf(query.uomHref), java.sql.Types.VARCHAR);

                    statement.statement.setNull(indexOf(query.value), java.sql.Types.BOOLEAN);

                } else if (field.getValue() instanceof TimeType) {
                    final TimeType t = (TimeType) field.getValue();

                    statement.statement.setString(indexOf(query.type), "Time");
                    if ( t.getUom() != null && t.getUom().getCode() != null)
                        statement.statement.setString(indexOf(query.uomCode), t.getUom().getCode());
                    else
                        statement.statement.setNull(indexOf(query.uomCode), java.sql.Types.VARCHAR);

                    if ( t.getUom() != null && t.getUom().getHref() != null)
                        statement.statement.setString(indexOf(query.uomHref), t.getUom().getHref());
                    else
                        statement.statement.setNull(indexOf(query.uomHref), java.sql.Types.VARCHAR);

                    statement.statement.setNull(indexOf(query.value), java.sql.Types.BOOLEAN);

                } else if (field.getValue() instanceof BooleanType) {
                    final BooleanType b = (BooleanType) field.getValue();

                    statement.statement.setString(indexOf(query.type), "Boolean");
                    if (b.isValue() != null)
                        statement.statement.setBoolean(indexOf(query.value), b.isValue());
                    else
                        statement.statement.setNull(indexOf(query.value), java.sql.Types.BOOLEAN);

                    statement.statement.setNull(indexOf(query.uomHref), java.sql.Types.VARCHAR);
                    statement.statement.setNull(indexOf(query.uomCode), java.sql.Types.VARCHAR);

                } else {
                    throw new CatalogException("Unexpected scalar Type:" + field.getValue());
                }
                updateSingleton(statement.statement);
                release(lc, statement);
                success = true;
            } finally {
                transactionEnd(lc, success);
            }
        }
        return id;
    }
}
