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
import static org.geotoolkit.internal.sql.table.QueryType.*;
import org.geotoolkit.internal.sql.table.QueryType;

/**
 * The query to execute for a {@link SimpleDataRecordTable}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class SimpleDataRecordQuery extends Query{
 
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idBlock, idDataRecord, definition, fixed;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdBlock, byIdDataRecord;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public SimpleDataRecordQuery(final Database database) {
        super(database, "simple_data_records", "observation");
        final QueryType[] sli  = {SELECT, LIST, INSERT};
        final QueryType[] slie = {SELECT, LIST, INSERT, EXISTS};
        idBlock       = addMandatoryColumn("id_datablock",  slie);
        idDataRecord  = addMandatoryColumn("id_datarecord", slie);
        definition    = addOptionalColumn("definition",    null, sli);
        fixed         = addOptionalColumn("fixed",         null, sli);
        
        byIdBlock       = addParameter(idBlock, SELECT, LIST, EXISTS);
        byIdDataRecord  = addParameter(idDataRecord, SELECT, EXISTS);
        
        
    }
    
}
