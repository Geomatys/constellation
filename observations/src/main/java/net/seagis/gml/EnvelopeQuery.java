package net.seagis.gml;

import net.seagis.catalog.Column;
import net.seagis.catalog.Database;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import net.seagis.catalog.QueryType;
import static net.seagis.catalog.QueryType.*;
/**
 * Represent a rectangle in the space. 
 * 
 * @author legal
 */
public class EnvelopeQuery extends Query {
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column id, srsName, lowerCornerX, lowerCornerY, upperCornerX, upperCornerY;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byId;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public EnvelopeQuery(final Database database) {
        super (database);
        final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        id           = addColumn("envelopes", "id",             SLIE);
        srsName      = addColumn("envelopes", "srs_name",       SLI);
        lowerCornerX = addColumn("envelopes", "lower_corner_x", SLI);
        lowerCornerY = addColumn("envelopes", "lower_corner_y", SLI);
        upperCornerX = addColumn("envelopes", "upper_corner_x", SLI);
        upperCornerY = addColumn("envelopes", "upper_corner_y", SLI);
        
        byId         = addParameter(id, SELECT, EXISTS);
    }

}
