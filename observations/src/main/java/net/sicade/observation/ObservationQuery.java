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
        final QueryType[] SI  = {SELECT, INSERT};
        final QueryType[] SIE = {SELECT, INSERT, EXISTS};
        
        name                      = addColumn("observations", "name",                        SIE);
        description               = addColumn("observations", "description",                 SI);
        featureOfInterest         = addColumn("observations", "feature_of_interest",         SI);
        featureOfInterestPoint    = addColumn("observations", "feature_of_interest_point",   SI);
        procedure                 = addColumn("observations", "procedure",                   SI);
        observedProperty          = addColumn("observations", "observed_property",           SI);
        observedPropertyComposite = addColumn("observations", "observed_property_composite", SI);
        distribution              = addColumn("observations", "distribution",                SI);
        samplingTimeBegin         = addColumn("observations", "sampling_time_begin",         SI);
        samplingTimeEnd           = addColumn("observations", "sampling_time_end",           SI);
        result                    = addColumn("observations", "result",                      SI);
        resultDefinition          = addColumn("observations", "result_definition",           SI);
/*
        observationMetadata       = addColumn("observations", "observationMetadata",         SI);
        quality                   = addColumn("observations", "quality",                     SI);
        result                    = addColumn("observations", "result",                      SI);
        procedureTime             = addColumn("observations", "procedureTime",               SI);
        procedureParameter        = addColumn("observations", "procedureParameter",          SI);*/
                
        
        byName = addParameter(name, SELECT, EXISTS);
    }
    
}
