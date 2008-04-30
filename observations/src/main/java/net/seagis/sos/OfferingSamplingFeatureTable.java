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


package net.seagis.sos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.SingletonTable;
import net.seagis.gml.ReferenceEntry;
import net.seagis.gml.ReferenceTable;
import org.geotools.resources.Utilities;

/**
 *
 * @author Guilhem Legal
 */
public class OfferingSamplingFeatureTable extends SingletonTable<OfferingSamplingFeatureEntry> {

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
    public OfferingSamplingFeatureTable(final OfferingSamplingFeatureTable table) {
        super(table);
    }

    /**
     * Initialize the table identifier.
     */
    private OfferingSamplingFeatureTable(final OfferingSamplingFeatureQuery query) {
        super(query);
        setIdentifierParameters(query.bySamplingFeature, null);
    }

    @Override
    protected OfferingSamplingFeatureEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final OfferingSamplingFeatureQuery query = (OfferingSamplingFeatureQuery) super.query;
        ReferenceEntry samplingFeature;
        
        if (samplingFeatures == null) {
                samplingFeatures = getDatabase().getTable(ReferenceTable.class);
            }
        samplingFeature = samplingFeatures.getEntry(results.getString(indexOf(query.samplingFeature)));
        

        return new OfferingSamplingFeatureEntry(results.getString(indexOf(query.idOffering)), samplingFeature);
    }

    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException, CatalogException {
        super.configure(type, statement);
        final OfferingSamplingFeatureQuery query = (OfferingSamplingFeatureQuery) super.query;
        if (!type.equals(QueryType.INSERT)) {
            statement.setString(indexOf(query.byOffering), idOffering);
        }
    }

    public String getIdOffering() {
        return idOffering;
    }

    public void setIdOffering(String idOffering) {
        if (!Utilities.equals(this.idOffering, idOffering)) {
            this.idOffering = idOffering;
            fireStateChanged("idOffering");
        }
    }
    
     /**
     * Insere un nouveau capteur a un offering dans la base de donnée.
     *
     */
    public synchronized void getIdentifier(OfferingSamplingFeatureEntry offSamplingFeature) throws SQLException, CatalogException {
        final OfferingSamplingFeatureQuery query  = (OfferingSamplingFeatureQuery) super.query;
        String idSF = "";
        boolean success = false;
        transactionBegin();
        try {
            PreparedStatement statement = getStatement(QueryType.EXISTS);
            statement.setString(indexOf(query.idOffering), offSamplingFeature.getIdOffering());
            if ( samplingFeatures == null) {
                samplingFeatures = getDatabase().getTable(ReferenceTable.class);
            }
            idSF = samplingFeatures.getIdentifier(offSamplingFeature.getComponent());
            statement.setString(indexOf(query.samplingFeature), idSF);
 
            ResultSet result = statement.executeQuery();
            if(result.next()) {
                success = true;
                return;
            }
            PreparedStatement insert    = getStatement(QueryType.INSERT);
            insert.setString(indexOf(query.idOffering), offSamplingFeature.getIdOffering());
            if ( samplingFeatures == null) {
                samplingFeatures = getDatabase().getTable(ReferenceTable.class);
            }
            idSF = samplingFeatures.getIdentifier(offSamplingFeature.getComponent());
            insert.setString(indexOf(query.samplingFeature), idSF);

            updateSingleton(insert);
            success = true;
        } finally {
            transactionEnd(success);
        }
    }
}