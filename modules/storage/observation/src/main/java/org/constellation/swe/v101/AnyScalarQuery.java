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
package org.constellation.swe.v101;

import org.geotoolkit.internal.sql.table.Column;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.Parameter;
import org.geotoolkit.internal.sql.table.Query;
import org.geotoolkit.internal.sql.table.QueryType;
import static org.geotoolkit.internal.sql.table.QueryType.*;

/**
 * The query to execute for a {@link DataRecordFieldTable}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class AnyScalarQuery extends Query{
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idDataRecord, idDataBlock, name, definition, type,  uomCode, value, uomHref;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdDataRecord, byIdDataBlock, byName;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public AnyScalarQuery(final Database database) {
        super (database, "any_scalars", "observation");
        final QueryType[] sli  = {SELECT, LIST, INSERT};
        final QueryType[] slie = {SELECT, LIST, INSERT, EXISTS};
        idDataRecord  = addMandatoryColumn("id_datarecord", slie);
        idDataBlock   = addMandatoryColumn("id_datablock",  slie);
        name          = addOptionalColumn("name",          null, slie);
        definition    = addOptionalColumn("definition",    null, sli);
        type          = addOptionalColumn("type",          null,  sli);
        uomCode       = addOptionalColumn("uom_code",      null, sli);
        uomHref       = addOptionalColumn("uom_href",      null, sli);
        value         = addOptionalColumn("value",         null, sli);
        
        byName         = addParameter(name, SELECT, EXISTS);
        byIdDataRecord = addParameter(idDataRecord, SELECT, LIST, EXISTS);
        byIdDataBlock  = addParameter(idDataBlock,  SELECT, LIST, EXISTS);
    }
    
}
