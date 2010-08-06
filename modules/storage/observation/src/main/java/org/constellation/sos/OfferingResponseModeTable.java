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
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.geotoolkit.sos.xml.v100.OfferingResponseModeEntry;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.geotoolkit.util.Utilities;

/**
 *
 * @author Guilhem Legal
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
    private OfferingResponseModeTable(final OfferingResponseModeTable table) {
        super(table);
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private OfferingResponseModeTable(final OfferingResponseModeQuery query) {
        super(query, query.byMode);
    }
    
    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected OfferingResponseModeTable clone() {
        return new OfferingResponseModeTable(this);
    }

    @Override
    protected OfferingResponseModeEntry createEntry(final LocalCache lc, final ResultSet results, Comparable<?> identifier) throws CatalogException, SQLException {
        final OfferingResponseModeQuery query = (OfferingResponseModeQuery) super.query;
        
        
        final ResponseModeType mode = ResponseModeType.valueOf(results.getString(indexOf(query.mode)));
        return new OfferingResponseModeEntry(results.getString(indexOf(query.idOffering)),
                                          mode);
    }
    
    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final LocalCache lc, final QueryType type, final PreparedStatement statement) throws SQLException, CatalogException {
        super.configure(lc, type, statement);
        final OfferingResponseModeQuery query = (OfferingResponseModeQuery) super.query;
        if (! type.equals(QueryType.INSERT))
            statement.setString(indexOf(query.byOffering), idOffering);
        
    }
    
    
    public String getIdOffering() {
        return idOffering;
    }
    
    public synchronized void setIdOffering(String idOffering) {
        if (!Utilities.equals(this.idOffering, idOffering)) {
            this.idOffering = idOffering;
            fireStateChanged("idOffering");
        }
        
    }
    
     /**
     * Insere un nouveau capteur a un offering dans la base de donnée.
     *
     */
    public void getIdentifier(OfferingResponseModeEntry offres) throws SQLException, CatalogException {
        final OfferingResponseModeQuery query  = (OfferingResponseModeQuery) super.query;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                final Stmt statement = getStatement(lc, QueryType.EXISTS);
                statement.statement.setString(indexOf(query.idOffering), offres.getIdOffering());
                statement.statement.setString(indexOf(query.mode), offres.getMode().name());
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
                insert.statement.setString(indexOf(query.idOffering), offres.getIdOffering());
                insert.statement.setString(indexOf(query.mode), offres.getMode().name() );
                updateSingleton(insert.statement);
                success = true;
                release(lc, insert);
            } finally {
                transactionEnd(lc, success);
            }
        }
    }
    
}
