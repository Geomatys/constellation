/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package net.seagis.coverage.model;

import net.seagis.catalog.Column;
import net.seagis.catalog.Database;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import net.seagis.catalog.QueryType;
import static net.seagis.catalog.QueryType.*;


/**
 * The query to execute for a {@link DistributionTable}.
 *
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 * @version $Id$
 */
final class DistributionQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, scale, offset, log;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public DistributionQuery(final Database database) {
        super(database, "Distributions");
        final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] SLIE  = {SELECT, LIST, INSERT, EXISTS};
        name   = addColumn   ("name",   SLIE);
        scale  = addColumn   ("scale",  SLI);
        offset = addColumn   ("offset", SLI);
        log    = addColumn   ("log",    SLI);
        byName = addParameter(name, SELECT, EXISTS);
    }
}
