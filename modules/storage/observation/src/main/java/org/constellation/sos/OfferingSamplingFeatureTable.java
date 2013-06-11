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
package org.constellation.sos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.constellation.gml.v311.ReferenceTable;
import org.geotoolkit.gml.xml.v311.ReferenceType;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.sos.xml.v100.OfferingSamplingFeatureType;
import java.util.Objects;

/**
 *
 * @author Guilhem Legal
 */
public class OfferingSamplingFeatureTable extends SingletonTable<OfferingSamplingFeatureType> implements Cloneable {

    /**
     * identifier secondary of the table.
     */
    private String idOffering;

    /*
     * un lien vers la table des sampling feature.
     *
     * private SamplingFeatureTable samplingFeatures;
     *
     * un lien vers la table des sampling point.
     *
     * private SamplingPointTable samplingPoints;
     */

    /**
     * A lnk to the reference table.
     */
     private ReferenceTable samplingFeatures;

    /**
     * Build a new offering sampling feature table.
     *
     * @param  database Connection to the database.
     */
    public OfferingSamplingFeatureTable(final Database database) {
        this(new OfferingSamplingFeatureQuery(database));
    }

    /**
     * Build a new table not shared.
     */
    private OfferingSamplingFeatureTable(final OfferingSamplingFeatureTable table) {
        super(table);
    }

    /**
     * Initialize the table identifier.
     */
    private OfferingSamplingFeatureTable(final OfferingSamplingFeatureQuery query) {
        super(query, query.bySamplingFeature);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected OfferingSamplingFeatureTable clone() {
        return new OfferingSamplingFeatureTable(this);
    }

    @Override
    protected OfferingSamplingFeatureType createEntry(final LocalCache lc, final ResultSet results, Comparable<?> identifier) throws CatalogException, SQLException {
        final OfferingSamplingFeatureQuery query = (OfferingSamplingFeatureQuery) super.query;
        ReferenceType samplingFeature;

        if (samplingFeatures == null) {
                samplingFeatures = getDatabase().getTable(ReferenceTable.class);
            }
        samplingFeature = samplingFeatures.getEntry(results.getString(indexOf(query.samplingFeature)));


        return new OfferingSamplingFeatureType(results.getString(indexOf(query.idOffering)), samplingFeature);
    }

    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final LocalCache lc, final QueryType type, final PreparedStatement statement) throws SQLException, CatalogException {
        super.configure(lc, type, statement);
        final OfferingSamplingFeatureQuery query = (OfferingSamplingFeatureQuery) super.query;
        if (!type.equals(QueryType.INSERT)) {
            statement.setString(indexOf(query.byOffering), idOffering);
        }
    }

    public synchronized String getIdOffering() {
        return idOffering;
    }

    public synchronized void setIdOffering(String idOffering) {
        if (!Objects.equals(this.idOffering, idOffering)) {
            this.idOffering = idOffering;
            fireStateChanged("idOffering");
        }
    }

     /**
     * Insere un nouveau capteur a un offering dans la base de donnée.
     *
     */
    public void getIdentifier(OfferingSamplingFeatureType offSamplingFeature) throws SQLException, CatalogException {
        final OfferingSamplingFeatureQuery query  = (OfferingSamplingFeatureQuery) super.query;
        String idSF = "";
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                final Stmt statement = getStatement(lc, QueryType.EXISTS);
                statement.statement.setString(indexOf(query.idOffering), offSamplingFeature.getIdOffering());
                if ( samplingFeatures == null) {
                    samplingFeatures = getDatabase().getTable(ReferenceTable.class);
                }
                idSF = samplingFeatures.getIdentifier(offSamplingFeature.getComponent());
                statement.statement.setString(indexOf(query.samplingFeature), idSF);

                final ResultSet result = statement.statement.executeQuery();
                if(result.next()) {
                    success = true;
                    result.close();
                    release(lc, statement);
                    return;
                }
                result.close();
                release(lc, statement);

                final Stmt insert    = getStatement(lc, QueryType.INSERT);
                insert.statement.setString(indexOf(query.idOffering), offSamplingFeature.getIdOffering());
                if ( samplingFeatures == null) {
                    samplingFeatures = getDatabase().getTable(ReferenceTable.class);
                }
                idSF = samplingFeatures.getIdentifier(offSamplingFeature.getComponent());
                insert.statement.setString(indexOf(query.samplingFeature), idSF);

                updateSingleton(insert.statement);
                release(lc, insert);

                success = true;
            } finally {
                transactionEnd(lc, success);
            }
        }
    }
}
