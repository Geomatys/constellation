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
package net.sicade.swe;

import net.sicade.catalog.Column;
import net.sicade.catalog.Database;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import static net.sicade.catalog.QueryType.*;
import net.sicade.catalog.QueryType;

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
        super(database);
        final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        idBlock       = addColumn("simple_data_records", "id_datablock",  SLIE);
        idDataRecord  = addColumn("simple_data_records", "id_datarecord", SLIE);
        definition    = addColumn("simple_data_records", "definition",    SLI);
        fixed         = addColumn("simple_data_records", "fixed",         SLI);
        
        byIdBlock       = addParameter(idBlock, SELECT, LIST, EXISTS);
        byIdDataRecord  = addParameter(idDataRecord, SELECT, EXISTS);
        
        
    }
    
}
