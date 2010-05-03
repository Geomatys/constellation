/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
package org.constellation.sampling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.NoSuchTableException;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.constellation.gml.v311.DirectPositionEntry;
import org.constellation.gml.v311.EnvelopeTable;
import org.constellation.gml.v311.LineStringTable;
import org.geotoolkit.gml.xml.v311.CurvePropertyType;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.EnvelopeEntry;
import org.geotoolkit.gml.xml.v311.FeaturePropertyType;
import org.geotoolkit.gml.xml.v311.LineStringType;
import org.geotoolkit.gml.xml.v311.MeasureType;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.sampling.xml.v100.SamplingCurveType;

/**
 *SamplingPointTable.java
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SamplingCurveTable extends SingletonTable<SamplingCurveType> {

    /**
     * Connexion vers la table des {@linkplain LineStringTable lineStrings}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private LineStringTable linestrings;

    /**
     * Connexion vers la table des {@linkplain LineStringTable lineStrings}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private EnvelopeTable envelopes;

    
    public SamplingCurveTable(final Database database) {
        this(new SamplingCurveQuery(database));
    }
    
     /**
     * Initialise l'identifiant de la table.
     */
    private SamplingCurveTable(final SamplingCurveQuery query) {
        super(query, query.byIdentifier);
    }

    /**
     * Construit une nouvelle table non partagée
     */
    private SamplingCurveTable(final SamplingCurveTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected SamplingCurveTable clone() {
        return new SamplingCurveTable(this);
    }
    
    /**
     * Construit une station pour l'enregistrement courant. L'implémentation par défaut extrait une
     * premiére série d'informations telles que le {@linkplain Station#getName nom de la station},
     * {@linkplain Station#getProvider son fournisseur}, <cite>etc.</cite> et appele la méthode
     * <code>{@linkplain #createEntry(int,String,Platform,DataQuality,Citation,ResultSet)
     * createEntry}(name, identifier, ...)</code> avec ces informations.
     */
    @Override
    protected SamplingCurveType createEntry(final ResultSet result, Comparable<?> identifier) throws CatalogException, SQLException {
        final SamplingCurveQuery query = (SamplingCurveQuery) super.query;
        final String curveId = result.getString(indexOf(query.curveIdentifier));

        linestrings = getLineStringTable();

        final Collection<DirectPositionEntry> entries = linestrings.getEntries(curveId);
        final Collection<DirectPositionType> positions = new ArrayList<DirectPositionType>();
        for (DirectPositionEntry entry : entries) {
            positions.add(entry.getPosition());
        }
                
        final LineStringType p = new LineStringType(curveId, result.getString(indexOf(query.srsName)), positions);

        final String sampledFeature = result.getString(indexOf(query.sampledFeature));
        FeaturePropertyType sampledFeatureProp = null;
        if (sampledFeature != null) {
            sampledFeatureProp = new FeaturePropertyType(sampledFeature);
        }
        final SamplingCurveType entry = new SamplingCurveType(result.getString(indexOf(query.identifier)),
                                    result.getString(indexOf(query.name)),
                                    result.getString(indexOf(query.description)),
                                    sampledFeatureProp,
                                    new CurvePropertyType(p),
                                    new MeasureType(result.getDouble(indexOf(query.lengthValue)), result.getString(indexOf(query.lengthUom))));
        if (envelopes == null) {
            envelopes = getDatabase().getTable(EnvelopeTable.class);
        }
        final EnvelopeEntry env = envelopes.getEntry(result.getString(indexOf(query.boundedby)));
        entry.setBoundedBy(env);
        return entry;
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier de la station passée en parametre si non-null)
     * et enregistre la nouvelle station dans la base de donnée.
     *
     * @param result le resultat a inserer dans la base de donnée.
     */
    public String getIdentifier(final SamplingCurveType station) throws SQLException, CatalogException {
        final SamplingCurveQuery query  = (SamplingCurveQuery) super.query;
        String id;
        boolean success = false;
        synchronized (getLock()) {
            transactionBegin();
            try {
                // the station recived by xml have no ID so we use the name as a second primary key
                if (station.getName() != null) {
                    final Stmt statement = getStatement(QueryType.LIST);
                    statement.statement.setString(indexOf(query.byName), station.getName());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        id = result.getString("id");
                        station.setId(id);
                        result.close();
                        release(statement);
                        return id;
                    } else {
                        if (station.getId() != null) {
                            id = station.getId();
                        } else {
                           id = searchFreeIdentifier("station");
                        }
                    }
                    result.close();
                    release(statement);
                } else {
                   throw new CatalogException("the station must have a name");
                }
                station.setId(id);
                final Stmt statement = getStatement(QueryType.INSERT);
                statement.statement.setString(indexOf(query.identifier), id);

                if (station.getDescription() != null) {
                    statement.statement.setString(indexOf(query.description), station.getDescription());
                } else {
                    statement.statement.setNull(indexOf(query.description), java.sql.Types.VARCHAR);
                }

                statement.statement.setString(indexOf(query.name), station.getName());
                final Iterator<FeaturePropertyType> i = station.getSampledFeatures().iterator();
                if (i.hasNext()) {
                    final FeaturePropertyType fp = i.next();
                    statement.statement.setString(indexOf(query.sampledFeature), (String)fp.getHref());
                } else {
                    statement.statement.setNull(indexOf(query.sampledFeature), java.sql.Types.VARCHAR);
                }

                if( station.getShape() != null && station.getShape().getAbstractCurve() != null && station.getShape().getAbstractCurve().getValue() != null) {
                    final LineStringType lineString = (LineStringType) station.getShape().getAbstractCurve().getValue();

                    statement.statement.setString(indexOf(query.curveIdentifier), lineString.getId());
                    statement.statement.setString(indexOf(query.srsName), lineString.getSrsName());

                    linestrings = getLineStringTable();
                    final List<DirectPositionType> positions = lineString.getPositions();
                    for (DirectPositionType position : positions) {
                        linestrings.getIdentifier(id, position);
                    }

                } else {
                    statement.statement.setNull(indexOf(query.srsName), java.sql.Types.VARCHAR);
                    statement.statement.setNull(indexOf(query.curveIdentifier), java.sql.Types.VARCHAR);
                }

                if (station.getLength() != null) {
                    statement.statement.setDouble(indexOf(query.lengthValue), station.getLength().getValue());
                    statement.statement.setString(indexOf(query.lengthUom), station.getLength().getUom());
                } else {
                    statement.statement.setNull(indexOf(query.lengthUom), java.sql.Types.VARCHAR);
                    statement.statement.setNull(indexOf(query.lengthValue), java.sql.Types.DOUBLE);
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

    public LineStringTable getLineStringTable() throws NoSuchTableException {
        if (linestrings == null) {
            linestrings =  getDatabase().getTable(LineStringTable.class);
        }
        return linestrings;
    }
    
}
