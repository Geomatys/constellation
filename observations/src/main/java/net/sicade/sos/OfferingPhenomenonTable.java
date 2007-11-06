
package net.sicade.sos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.QueryType;
import net.sicade.catalog.SingletonTable;
import net.sicade.observation.CompositePhenomenonEntry;
import net.sicade.observation.CompositePhenomenonTable;
import net.sicade.observation.PhenomenonEntry;
import net.sicade.observation.PhenomenonTable;
import org.geotools.resources.Utilities;

/**
 *
 * @author legal
 */
public class OfferingPhenomenonTable extends SingletonTable<OfferingPhenomenonEntry>{

        
    /**
     * identifiant secondaire de la table.
     */
    private String idOffering;
    
    /**
     * un lien vers la table des phenomenes.
     */
    private PhenomenonTable phenomenons;
    
     /**
     * un lien vers la table des phenomenes compose.
     */
    private CompositePhenomenonTable compositePhenomenons;
    
    /**
     * Construit une table des phenomene composé.
     *
     * @param  database Connexion vers la base de données.
     */
    public OfferingPhenomenonTable(final Database database) {
        this(new OfferingPhenomenonQuery(database));
    }
    
    /**
     * Construit une nouvelle table non partagée
     */
    public OfferingPhenomenonTable(final OfferingPhenomenonTable table) {
        super(table);
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private OfferingPhenomenonTable(final OfferingPhenomenonQuery query) {
        super(query);
        setIdentifierParameters(query.byPhenomenon, null);
    }
    
    
    @Override
    protected OfferingPhenomenonEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final OfferingPhenomenonQuery query = (OfferingPhenomenonQuery) super.query;
        PhenomenonEntry phenomenon;
        
        if (results.getString(indexOf(query.phenomenon)) != null) {
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
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
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
        
        PreparedStatement statement = getStatement(QueryType.EXISTS);
        
        statement.setString(indexOf(query.idOffering), offPheno.getIdOffering());
        
         
        if (offPheno.getComponent() instanceof CompositePhenomenonEntry) {
             if (compositePhenomenons == null) {
                compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
            }
            idPheno = compositePhenomenons.getIdentifier((CompositePhenomenonEntry)offPheno.getComponent());
            statement.setString(indexOf(query.compositePhenomenon), idPheno);
            statement.setNull(indexOf(query.phenomenon), java.sql.Types.VARCHAR);
        
        } else if (offPheno.getComponent() instanceof PhenomenonEntry) {
            if ( phenomenons == null) {
                phenomenons = getDatabase().getTable(PhenomenonTable.class);
            }
            idPheno = phenomenons.getIdentifier(offPheno.getComponent());
            statement.setString(indexOf(query.phenomenon), idPheno);
            statement.setNull(indexOf(query.compositePhenomenon), java.sql.Types.VARCHAR);
            
        } 
        ResultSet result = statement.executeQuery();
        if(result.next()) {
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
            insert.setNull(indexOf(query.phenomenon), java.sql.Types.VARCHAR);
        } else if (offPheno.getComponent() instanceof PhenomenonEntry) {
            if ( phenomenons == null) {
                phenomenons = getDatabase().getTable(PhenomenonTable.class);
            }
            idPheno = phenomenons.getIdentifier(offPheno.getComponent());
            insert.setString(indexOf(query.phenomenon), idPheno);
            insert.setNull(indexOf(query.compositePhenomenon), java.sql.Types.VARCHAR);
            
        }
        insertSingleton(insert);
              
    }
}
