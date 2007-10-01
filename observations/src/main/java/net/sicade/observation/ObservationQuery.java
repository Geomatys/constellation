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
package net.sicade.observation;

// Sicade dependencies
import net.sicade.catalog.Column;
import net.sicade.catalog.Database;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import net.sicade.catalog.QueryType;
import static net.sicade.catalog.QueryType.*;

/**
 * The query to execute for a {@link ObservationTable}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class ObservationQuery extends Query {
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, featureOfInterest, featureOfInterestPoint, procedure, observedProperty, observedPropertyComposite,
            distribution, samplingTimeBegin, samplingTimeEnd, result, resultDefinition, description;
 // quality, , observationMetadata, procedureTime, procedureParameter,
 
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public ObservationQuery(final Database database) {
        super(database);
        final QueryType[] usage = {SELECT};
        name                      = addColumn("observations", "name",                       usage);
        description               = addColumn("observations", "description",                usage);
        featureOfInterest         = addColumn("observations", "feature_of_interest",        usage);
        featureOfInterestPoint    = addColumn("observations", "feature_of_interest_point",  usage);
        procedure                 = addColumn("observations", "procedure",                  usage);
        observedProperty          = addColumn("observations", "observed_property",          usage);
        observedPropertyComposite = addColumn("observations", "observed_property_composite",usage);
        distribution              = addColumn("observations", "distribution",               usage);
        samplingTimeBegin         = addColumn("observations", "sampling_time_begin",        usage);
        samplingTimeEnd           = addColumn("observations", "sampling_time_end",          usage);
        result                    = addColumn("observations", "result",                     usage);
        resultDefinition          = addColumn("observations", "result_definition",          usage);
/*
        observationMetadata = addColumn("observations", "observationMetadata", usage);
        quality             = addColumn("observations", "quality",             usage);
        result              = addColumn("observations", "result",              usage);
        procedureTime       = addColumn("observations", "procedureTime",       usage);
        procedureParameter  = addColumn("observations", "procedureParameter",  usage);*/
                
        
        byName = addParameter(name, SELECT);
    }
    
}
