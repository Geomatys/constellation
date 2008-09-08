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

import net.seagis.catalog.Database;
import net.seagis.catalog.Column;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import net.seagis.catalog.QueryType;
import static net.seagis.catalog.QueryType.*;


/**
 * The query to execute for a {@link LinearModelTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class LinearModelQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column source1, source2, coefficient;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byTarget;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public LinearModelQuery(final Database database) {
        super(database, "LinearModelTerms");
        final Column target;
        final QueryType[] usage = {SELECT};
        target      = addColumn   ("target",      LIST);
        source1     = addColumn   ("source1",     usage);
        source2     = addColumn   ("source2",     usage);
        coefficient = addColumn   ("coefficient", usage);
        byTarget    = addParameter(target, usage);
        source1.setOrdering("ASC", usage);
        source2.setOrdering("ASC", usage);
    }
}
