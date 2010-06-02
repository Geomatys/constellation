/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.observation;

import org.geotoolkit.internal.sql.table.Column;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.Parameter;
import org.geotoolkit.internal.sql.table.Query;
import org.geotoolkit.internal.sql.table.QueryType;
import static org.geotoolkit.internal.sql.table.QueryType.*;

/**
 * The query to execute for a {@link MeasurementTable}.
 *
 * @author Guilhem Legal
 */
public class MeasurementQuery extends ObservationQuery {
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     
    protected final Column name, featureOfInterest, featureOfInterestPoint, featureOfInterestCurve, procedure, observedProperty, observedPropertyComposite,
            distribution, samplingTimeBegin, samplingTimeEnd, result, resultDefinition, description;*/
    // quality, , observationMetadata, procedureTime, procedureParameter,
 
    /**
     * Parameter to appear after the {@code "FROM"} clause.
    
    protected final Parameter byName; */
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public MeasurementQuery(final Database database) {
        super(database, "measurements", "observation");
/*        final QueryType[] si  = {SELECT, INSERT};
        final QueryType[] sie = {SELECT, INSERT, EXISTS};
        
        name                      = addMandatoryColumn("name",                        sie);
        description               = addOptionalColumn("description",             null, si);
        featureOfInterest         = addOptionalColumn("feature_of_interest",         null,si);
        featureOfInterestPoint    = addOptionalColumn("feature_of_interest_point",   null,si);
        featureOfInterestCurve    = addOptionalColumn("feature_of_interest_curve",   null,si);
        procedure                 = addMandatoryColumn("procedure",                 si);
        observedProperty          = addOptionalColumn("observed_property",           null,si);
        observedPropertyComposite = addOptionalColumn("observed_property_composite",null, si);
        distribution              = addOptionalColumn("distribution",                null,si);
        samplingTimeBegin         = addOptionalColumn("sampling_time_begin",        null, si);
        samplingTimeEnd           = addOptionalColumn("sampling_time_end",          null, si);
        result                    = addOptionalColumn("result",                      null,si);
        resultDefinition          = addOptionalColumn("result_definition",           null,si);

        observationMetadata       = addColumn("observationMetadata",         SI);
        quality                   = addColumn("quality",                     SI);
        result                    = addColumn("result",                      SI);
        procedureTime             = addColumn("procedureTime",               SI);
        procedureParameter        = addColumn("procedureParameter",          SI);
                
        
        byName = addParameter(name, SELECT, EXISTS);*/
    }

}
