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
import org.constellation.swe.v101.CompositePhenomenonTable;
import org.constellation.swe.v101.PhenomenonTable;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.sos.xml.v100.OfferingPhenomenonEntry;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonEntry;
import org.geotoolkit.swe.xml.v101.PhenomenonEntry;
import org.geotoolkit.util.Utilities;

/**
 *
 * @author Guilhem Legal
 */
public class OfferingPhenomenonTable extends SingletonTable<OfferingPhenomenonEntry>{

    /**
     * identifier secondary of the table.
     */
    private String idOffering;
    
    /**
     * a link to the reference table.
     */
    private PhenomenonTable phenomenons;
    
     /**
     * un lien vers la table des phenomenes compose.
     */
    private CompositePhenomenonTable compositePhenomenons;
    
    /**
     * Build a new offering phenomenon table.
     *
     * @param  database Connection to the database.
     */
    public OfferingPhenomenonTable(final Database database) {
        this(new OfferingPhenomenonQuery(database));
    }
    
    /**
     * Build a new offering phenomenon table not shared.
     */
    private OfferingPhenomenonTable(final OfferingPhenomenonTable table) {
        super(table);
    }
    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected OfferingPhenomenonTable clone() {
        return new OfferingPhenomenonTable(this);
    }

    /**
     * Initialize the table identifier.
     */
    private OfferingPhenomenonTable(final OfferingPhenomenonQuery query) {
        super(query,query.byPhenomenon);
    }
    
    
    @Override
    protected OfferingPhenomenonEntry createEntry(final LocalCache lc, final ResultSet results, Comparable<?> identifier) throws CatalogException, SQLException {
        final OfferingPhenomenonQuery query = (OfferingPhenomenonQuery) super.query;
        PhenomenonEntry phenomenon;
        
        if (!results.getString(indexOf(query.phenomenon)).isEmpty()) {
            if (phenomenons == null) {
                phenomenons = getDatabase().getTable(PhenomenonTable.class);
            }
            phenomenon = (PhenomenonEntry)phenomenons.getEntry(results.getString(indexOf(query.phenomenon)));
        } else {
            if (compositePhenomenons == null) {
                compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
        } 
            phenomenon = compositePhenomenons.getEntry(results.getString(indexOf(query.compositePhenomenon)));
        }
        return new OfferingPhenomenonEntry(results.getString(indexOf(query.idOffering)), phenomenon);
    }
    
    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final LocalCache lc, final QueryType type, final PreparedStatement statement) throws SQLException, CatalogException {
        super.configure(lc, type, statement);
        final OfferingPhenomenonQuery query = (OfferingPhenomenonQuery) super.query;
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
    public void getIdentifier(OfferingPhenomenonEntry offPheno) throws SQLException, CatalogException {
        final OfferingPhenomenonQuery query  = (OfferingPhenomenonQuery) super.query;
        String idPheno = "";
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                final Stmt statement = getStatement(lc, QueryType.EXISTS);

                statement.statement.setString(indexOf(query.idOffering), offPheno.getIdOffering());


                if (offPheno.getComponent() instanceof CompositePhenomenonEntry) {
                    if (compositePhenomenons == null) {
                        compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
                    }
                    idPheno = compositePhenomenons.getIdentifier((CompositePhenomenonEntry)offPheno.getComponent());
                    statement.statement.setString(indexOf(query.compositePhenomenon), idPheno);
                    statement.statement.setString(indexOf(query.phenomenon), "");

                } else {
                    if ( phenomenons == null) {
                        phenomenons = getDatabase().getTable(PhenomenonTable.class);
                    }
                    idPheno = phenomenons.getIdentifier(offPheno.getComponent());
                    statement.statement.setString(indexOf(query.phenomenon), idPheno);
                    statement.statement.setString(indexOf(query.compositePhenomenon), "");

                }
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
                insert.statement.setString(indexOf(query.idOffering), offPheno.getIdOffering());
                if (offPheno.getComponent() instanceof CompositePhenomenonEntry) {
                    if (compositePhenomenons == null) {
                        compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
                    }
                    idPheno = compositePhenomenons.getIdentifier((CompositePhenomenonEntry)offPheno.getComponent());
                    insert.statement.setString(indexOf(query.compositePhenomenon), idPheno);
                    insert.statement.setString(indexOf(query.phenomenon), "");
                } else {
                if ( phenomenons == null) {
                        phenomenons = getDatabase().getTable(PhenomenonTable.class);
                }
                idPheno = phenomenons.getIdentifier(offPheno.getComponent());
                    insert.statement.setString(indexOf(query.phenomenon), idPheno);
                    insert.statement.setString(indexOf(query.compositePhenomenon), "");

                }
                updateSingleton(insert.statement);
                release(lc, insert);
                success = true;
            } finally {
                transactionEnd(lc, success);
            }
        }
    }
}
