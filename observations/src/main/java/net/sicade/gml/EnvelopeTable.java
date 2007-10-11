package net.sicade.gml;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;

/**
 *
 * @author legal
 */
public class EnvelopeTable extends SingletonTable<EnvelopeEntry> {

    
    /**
     * Construit une table des envelope.
     *
     * @param  database Connexion vers la base de données.
     */
    public EnvelopeTable(final Database database) {
        this(new EnvelopeQuery(database));
    }

    /**
     * Initialise l'identifiant de la table.
     */
    private EnvelopeTable(final EnvelopeQuery query) {
        super(query);
        setIdentifierParameters(query.byId, null);
    }
    
    @Override
    protected EnvelopeEntry createEntry(ResultSet results) throws CatalogException, SQLException {
        final EnvelopeQuery query = (EnvelopeQuery) super.query;
        //on lit le premier point
        List<Double> value = new ArrayList<Double>();
        value.add(results.getDouble(indexOf((query.lowerCornerX))));
        value.add(results.getDouble(indexOf((query.lowerCornerY))));
        DirectPositionType lc = new DirectPositionType(null,-1,value);
        
        //puis le second
        value = new ArrayList<Double>();
        value.add(results.getDouble(indexOf((query.upperCornerX))));
        value.add(results.getDouble(indexOf((query.upperCornerY))));
        DirectPositionType uc = new DirectPositionType(null,-1,value);
        return new EnvelopeEntry(results.getString(indexOf(query.id)),
                                 lc, uc,
                                 results.getString(indexOf(query.srsName)));
    }

}
