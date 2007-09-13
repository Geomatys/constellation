
package net.sicade.swe;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;

/**
 * Connexion vers la table des {@linkplain DataBlockEntry dataBlock}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class DataBlockTable extends SingletonTable<DataBlockEntry>{
    
     /**
     * Construit une table des data record.
     *
     * @param  database Connexion vers la base de données.
     */
    public DataBlockTable(final Database database) {
        this(new DataBlockQuery(database));
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private DataBlockTable(final DataBlockQuery query) {
        super(query);
        setIdentifierParameters(query.byIdBlock, null);
    }

    /**
     * Construit un bloc de données pour l'enregistrement courant.
     */
    protected DataBlockEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final DataBlockQuery query = (DataBlockQuery) super.query;
        return new DataBlockEntry(results.getString(indexOf(query.idBlock)),
                                  results.getString(indexOf(query.data)));
        
    }
}
