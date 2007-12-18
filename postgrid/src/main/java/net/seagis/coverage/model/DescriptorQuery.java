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
package net.seagis.coverage.model;

import net.seagis.catalog.Column;
import net.seagis.catalog.Database;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import net.seagis.catalog.QueryType;
import static net.seagis.catalog.QueryType.*;


/**
 * The query to execute for a {@link DescriptorTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class DescriptorQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column identifier, symbol, layer, operation, region, band, distribution;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter bySymbol, byIdentifier;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public DescriptorQuery(final Database database) {
        super(database, "Descriptors");
        final QueryType[] usage  = {SELECT, SELECT_BY_NUMBER, LIST};
        identifier   = addColumn   ("identifier",   usage);
        symbol       = addColumn   ("symbol",       usage);
        layer        = addColumn   ("layer",        usage);
        operation    = addColumn   ("operation",    usage);
        region       = addColumn   ("region",       usage);
        band         = addColumn   ("band",         usage);
        distribution = addColumn   ("distribution", usage);
        bySymbol     = addParameter(symbol,     SELECT);
        byIdentifier = addParameter(identifier, SELECT_BY_NUMBER);
        identifier.setOrdering("ASC", LIST);
    }
}
