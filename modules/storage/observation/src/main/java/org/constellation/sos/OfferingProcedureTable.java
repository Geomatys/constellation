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
import org.constellation.gml.v311.ReferenceTable;
import org.geotoolkit.gml.xml.v311.ReferenceEntry;
import org.geotoolkit.sos.xml.v100.OfferingProcedureEntry;
import org.geotoolkit.util.Utilities;

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
        final ReferenceEntry procedure = process.getEntry(results.getString(indexOf(query.procedure)));
        
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
            final PreparedStatement statement = getStatement(QueryType.EXISTS);
            statement.setString(indexOf(query.idOffering), offProc.getIdOffering());
         
            if (process == null) {
                process = getDatabase().getTable(ReferenceTable.class);
            }
            idProc = process.getIdentifier(offProc.getComponent());
        
            statement.setString(indexOf(query.procedure), idProc);
            final ResultSet result = statement.executeQuery();
            if(result.next()) {
                success = true;
                return;
            }
        
            final PreparedStatement insert    = getStatement(QueryType.INSERT);
            insert.setString(indexOf(query.idOffering), offProc.getIdOffering());
            insert.setString(indexOf(query.procedure), idProc);
            updateSingleton(insert);
            success = true;
        } finally {
            transactionEnd(success);
        }
    }
    
}
