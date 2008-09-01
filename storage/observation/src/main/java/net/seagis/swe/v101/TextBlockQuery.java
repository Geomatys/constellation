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
package net.seagis.swe.v101;

import net.seagis.catalog.Column;
import net.seagis.catalog.Database;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import net.seagis.catalog.QueryType;
import static net.seagis.catalog.QueryType.*;

/**
 * The query to execute for a {@link TextBlockTable}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class TextBlockQuery extends Query{
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column id, tokenSeparator, blockSeparator, decimalSeparator;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byId, byTokenSeparator, byBlockSeparator, byDecimalSeparator;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public TextBlockQuery(final Database database) {
        super(database, "text_block_encodings");
        final QueryType[] SI  = {SELECT, INSERT};
        final QueryType[] SIEF = {SELECT, INSERT, EXISTS,  FILTERED_LIST};
        id               = addColumn("id_encoding",       SIEF);
        tokenSeparator   = addColumn("token_separator",   SI);
        blockSeparator   = addColumn("block_separator",   SI);
        decimalSeparator = addColumn("decimal_separator", SI);

        byId               = addParameter(id, SELECT, EXISTS);
        byBlockSeparator   = addParameter(blockSeparator,   FILTERED_LIST);
        byDecimalSeparator = addParameter(decimalSeparator, FILTERED_LIST);
        byTokenSeparator   = addParameter(tokenSeparator,   FILTERED_LIST);
    }
    
}
