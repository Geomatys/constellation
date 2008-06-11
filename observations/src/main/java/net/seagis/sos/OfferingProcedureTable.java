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
import net.seagis.gml.v311.ReferenceEntry;
import net.seagis.gml.v311.ReferenceTable;
import org.geotools.util.Utilities;

/**
 *
 * @author Guilhem Legal
 */
public class OfferingProcedureTable extends SingletonTable<OfferingProcedureEntry>{

        
    /**
     * identifier secondary of the table.
     */
    private String idOffering;
    
    /**
     * a link to the reference table.
     */
    private ReferenceTable process;
    
    /**
     * Construit une table des phenomene composé.
     *
     * @param  database Connection to the database.
     */
    public OfferingProcedureTable(final Database database) {
        this(new OfferingProcedureQuery(database));
    }
    
    /**
     * Build a new table not shared.
     */
    public OfferingProcedureTable(final OfferingProcedureTable table) {
        super(table);
    }
    
    /**
     * Initialize the identifier of the table.
     */
    private OfferingProcedureTable(final OfferingProcedureQuery query) {
        super(query);
        setIdentifierParameters(query.byProcedure, null);
    }
    
    
    @Override
    protected OfferingProcedureEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final OfferingProcedureQuery query = (OfferingProcedureQuery) super.query;
        
        if (process == null) {
            process = getDatabase().getTable(ReferenceTable.class);
        }
        ReferenceEntry procedure = process.getEntry(results.getString(indexOf(query.procedure)));
        
        return new OfferingProcedureEntry(results.getString(indexOf(query.idOffering)), procedure);
    }
    
    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException, CatalogException {
        super.configure(type, statement);
        final OfferingProcedureQuery query = (OfferingProcedureQuery) super.query;
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
    public synchronized void getIdentifier(OfferingProcedureEntry offProc) throws SQLException, CatalogException {
        final OfferingProcedureQuery query  = (OfferingProcedureQuery) super.query;
        String idProc = "";
        boolean success = false;
        transactionBegin();
        try {
            PreparedStatement statement = getStatement(QueryType.EXISTS);
            statement.setString(indexOf(query.idOffering), offProc.getIdOffering());
         
            if (process == null) {
                process = getDatabase().getTable(ReferenceTable.class);
            }
            idProc = process.getIdentifier(offProc.getComponent());
        
            statement.setString(indexOf(query.procedure), idProc);
            ResultSet result = statement.executeQuery();
            if(result.next()) {
                success = true;
                return;
            }
        
            PreparedStatement insert    = getStatement(QueryType.INSERT);
            insert.setString(indexOf(query.idOffering), offProc.getIdOffering());
            insert.setString(indexOf(query.procedure), idProc);
            updateSingleton(insert);
            success = true;
        } finally {
            transactionEnd(success);
        }
    }
    
}
