/*
 * OfferingProcedureTable.java
 * 
 * Created on 10 oct. 2007, 17:07:50
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sicade.sos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.QueryType;
import net.sicade.catalog.SingletonTable;
import net.sicade.observation.ProcessEntry;
import net.sicade.observation.ProcessTable;
import org.geotools.resources.Utilities;

/**
 *
 * @author legal
 */
public class OfferingProcedureTable extends SingletonTable<OfferingProcedureEntry>{

        
    /**
     * identifiant secondaire de la table.
     */
    private String idOffering;
    
    /**
     * un lien vers la table des process.
     */
    private ProcessTable process;
    
    /**
     * Construit une table des phenomene composé.
     *
     * @param  database Connexion vers la base de données.
     */
    public OfferingProcedureTable(final Database database) {
        this(new OfferingProcedureQuery(database));
    }
    
    /**
     * Construit une nouvelle table non partagée
     */
    public OfferingProcedureTable(final OfferingProcedureTable table) {
        super(table);
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private OfferingProcedureTable(final OfferingProcedureQuery query) {
        super(query);
        setIdentifierParameters(query.byProcedure, null);
    }
    
    
    @Override
    protected OfferingProcedureEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final OfferingProcedureQuery query = (OfferingProcedureQuery) super.query;
        
        if (process == null) {
            process = getDatabase().getTable(ProcessTable.class);
        }
        ProcessEntry procedure = process.getEntry(results.getString(indexOf(query.procedure)));
        
        return new OfferingProcedureEntry(results.getString(indexOf(query.idOffering)), procedure);
    }
    
    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
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
    
}
