package net.seagis.sos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.SingletonTable;
import net.seagis.observation.ProcessEntry;
import net.seagis.observation.ProcessTable;
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
        
        
        ResponseMode mode = ResponseMode.valueOf(results.getString(indexOf(query.mode)));
        return new OfferingResponseModeEntry(results.getString(indexOf(query.idOffering)),
                                          mode);
    }
    
    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
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
