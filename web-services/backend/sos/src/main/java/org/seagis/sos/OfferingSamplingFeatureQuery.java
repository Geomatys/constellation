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
package net.seagis.sos;

import net.seagis.catalog.Column;
import net.seagis.catalog.Database;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import net.seagis.catalog.QueryType;
import static net.seagis.catalog.QueryType.*;

/**
 *
 * @author legal
 */
public class OfferingSamplingFeatureQuery extends Query {
    
     /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idOffering, samplingFeature;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byOffering, bySamplingFeature;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public OfferingSamplingFeatureQuery(final Database database) {
        super (database, "offering_sampling_features");
        final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        
        idOffering      = addColumn("id_offering", SLIE);
        samplingFeature = addColumn("sampling_feature",  SLIE);
        
        byOffering        = addParameter(idOffering, SELECT, LIST, EXISTS);
        bySamplingFeature = addParameter(samplingFeature,  SELECT, EXISTS);
    }

}
