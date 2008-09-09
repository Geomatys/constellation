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
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.SingletonTable;
import org.geotools.resources.Utilities;

/**
 *
 * @author legal
 */
public class OfferingResponseModeTable extends SingletonTable<OfferingResponseModeEntry>{

        
    /**
     * identifiant secondaire de la table.
     */
    private String idOffering;
    
    
    /**
     * Construit une table des mode de reponse.
     *
     * @param  database Connexion vers la base de données.
     */
    public OfferingResponseModeTable(final Database database) {
        this(new OfferingResponseModeQuery(database));
    }
    
   /**
     * Construit une nouvelle table non partagée
     */
    public OfferingResponseModeTable(final OfferingResponseModeTable table) {
        super(table);
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private OfferingResponseModeTable(final OfferingResponseModeQuery query) {
        super(query);
        setIdentifierParameters(query.byMode, null);
    }
    
    
    @Override
    protected OfferingResponseModeEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final OfferingResponseModeQuery query = (OfferingResponseModeQuery) super.query;
        
        
        ResponseModeType mode = ResponseModeType.valueOf(results.getString(indexOf(query.mode)));
        return new OfferingResponseModeEntry(results.getString(indexOf(query.idOffering)),
                                          mode);
    }
    
    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException, CatalogException {
        super.configure(type, statement);
        final OfferingResponseModeQuery query = (OfferingResponseModeQuery) super.query;
        if (! type.equals(QueryType.INSERT))
            statement.setString(indexOf(query.byOffering), idOffering);
        
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
    public synchronized void getIdentifier(OfferingResponseModeEntry offres) throws SQLException, CatalogException {
        final OfferingResponseModeQuery query  = (OfferingResponseModeQuery) super.query;
        boolean success = false;
        transactionBegin();
        try {
            PreparedStatement statement = getStatement(QueryType.EXISTS);
            statement.setString(indexOf(query.idOffering), offres.getIdOffering());
            statement.setString(indexOf(query.mode), offres.getMode().name());
            ResultSet result = statement.executeQuery();
            if(result.next()) {
                success = true;
                return;
            }
        
            PreparedStatement insert    = getStatement(QueryType.INSERT);
            insert.setString(indexOf(query.idOffering), offres.getIdOffering());
            insert.setString(indexOf(query.mode), offres.getMode().name() );
            updateSingleton(insert);
            success = true;
        } finally {
            transactionEnd(success);
        }
    }
    
}
