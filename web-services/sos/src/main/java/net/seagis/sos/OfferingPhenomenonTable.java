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
import java.util.logging.Logger;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.SingletonTable;
import net.seagis.swe.CompositePhenomenonEntry;
import net.seagis.swe.CompositePhenomenonTable;
import net.seagis.swe.PhenomenonEntry;
import net.seagis.swe.PhenomenonTable;
import org.geotools.resources.Utilities;

/**
 *
 * @author Guilhem Legal
 */
public class OfferingPhenomenonTable extends SingletonTable<OfferingPhenomenonEntry>{

    /**
     * A logger debugging purpose
     */    
    private Logger logger = Logger.getLogger("OfferingPhenomenonTable");
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
    public OfferingPhenomenonTable(final OfferingPhenomenonTable table) {
        super(table);
    }
    
    /**
     * Initialize the table identifier.
     */
    private OfferingPhenomenonTable(final OfferingPhenomenonQuery query) {
        super(query);
        setIdentifierParameters(query.byPhenomenon, null);
    }
    
    
    @Override
    protected OfferingPhenomenonEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final OfferingPhenomenonQuery query = (OfferingPhenomenonQuery) super.query;
        PhenomenonEntry phenomenon;
        
        if (!results.getString(indexOf(query.phenomenon)).equals("")) {
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
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException, CatalogException {
        super.configure(type, statement);
        final OfferingPhenomenonQuery query = (OfferingPhenomenonQuery) super.query;
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
    public synchronized void getIdentifier(OfferingPhenomenonEntry offPheno) throws SQLException, CatalogException {
        final OfferingPhenomenonQuery query  = (OfferingPhenomenonQuery) super.query;
        String idPheno = "";
        boolean success = false;
        transactionBegin();
        try {
            PreparedStatement statement = getStatement(QueryType.EXISTS);
        
            statement.setString(indexOf(query.idOffering), offPheno.getIdOffering());
        
         
            if (offPheno.getComponent() instanceof CompositePhenomenonEntry) {
                if (compositePhenomenons == null) {
                    compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
                }
                idPheno = compositePhenomenons.getIdentifier((CompositePhenomenonEntry)offPheno.getComponent());
                statement.setString(indexOf(query.compositePhenomenon), idPheno);
                statement.setString(indexOf(query.phenomenon), "");
        
            } else if (offPheno.getComponent() instanceof PhenomenonEntry) {
                if ( phenomenons == null) {
                    phenomenons = getDatabase().getTable(PhenomenonTable.class);
                }
                idPheno = phenomenons.getIdentifier(offPheno.getComponent());
                statement.setString(indexOf(query.phenomenon), idPheno);
                statement.setString(indexOf(query.compositePhenomenon), "");
            
            }
            ResultSet result = statement.executeQuery();
            if(result.next()) {
                success = true;
                return;
            }
        
            PreparedStatement insert    = getStatement(QueryType.INSERT);
            insert.setString(indexOf(query.idOffering), offPheno.getIdOffering());
            if (offPheno.getComponent() instanceof CompositePhenomenonEntry) {
                if (compositePhenomenons == null) {
                    compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
                }
                idPheno = compositePhenomenons.getIdentifier((CompositePhenomenonEntry)offPheno.getComponent());
                insert.setString(indexOf(query.compositePhenomenon), idPheno);
                insert.setString(indexOf(query.phenomenon), "");
            } else if (offPheno.getComponent() instanceof PhenomenonEntry) {
            if ( phenomenons == null) {
                    phenomenons = getDatabase().getTable(PhenomenonTable.class);
            }
            idPheno = phenomenons.getIdentifier(offPheno.getComponent());
                insert.setString(indexOf(query.phenomenon), idPheno);
                insert.setString(indexOf(query.compositePhenomenon), "");
            
            }
            updateSingleton(insert);
            success = true;
        } finally {
            transactionEnd(success);
        }
              
    }
}
