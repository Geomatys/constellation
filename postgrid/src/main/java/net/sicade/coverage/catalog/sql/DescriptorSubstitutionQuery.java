/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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
package net.sicade.coverage.catalog.sql;

import net.sicade.catalog.Database;
import net.sicade.catalog.Column;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import net.sicade.catalog.QueryType;
import static net.sicade.catalog.QueryType.*;


/**
 * The query to execute for a {@link DescriptorSubstitutionTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class DescriptorSubstitutionQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column symbol1, symbol2;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter bySymbol;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public DescriptorSubstitutionQuery(final Database database) {
        super(database);
        final Column symbol;
        final QueryType[] usage = {SELECT, LIST};
        symbol   = addColumn   ("TemporalGradientDescriptors", "symbol",  LIST);
        symbol1  = addColumn   ("TemporalGradientDescriptors", "symbol1", usage);
        symbol2  = addColumn   ("TemporalGradientDescriptors", "symbol2", usage);
        bySymbol = addParameter(symbol, SELECT);
    }
}
